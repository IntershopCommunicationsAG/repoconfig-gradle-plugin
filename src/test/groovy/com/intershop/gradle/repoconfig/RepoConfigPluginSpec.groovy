/*
 * Copyright 2017 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intershop.gradle.repoconfig

import com.intershop.gradle.test.util.TestDir
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RepoConfigPluginSpec extends Specification {

    @TestDir
    File testDir
    
    Project project
    Gradle gradle

    private Set oldSystemProps

    static final IVY = ['intershopIvyReleasesAll']
    static final IVY_SNAPSHOT = ['intershopIvySnapshotsAll']
    static final IVY_LOCAL = ['intershopIvyLocal']
    static final MAVEN = ['intershopMavenReleasesAll']
    static final MAVEN_SNAPSHOT = ['intershopMavenSnapshotsAll']
    static final MAVEN_LOCAL = ['intershopMavenLocal']
    
    def setup() {
        System.setProperty('GRADLE_USER_HOME', new File(testDir, 'gradleHome').absolutePath)

        project = ProjectBuilder.builder().build()
        gradle = project.gradle

        // save system properties before being altered
        oldSystemProps = new HashSet(System.properties.keySet())
    }
    
    def cleanup() {
        // purge test properties
        System.properties.keySet().retainAll(oldSystemProps)
    }
    
    def 'configuration can be disabled with disableInitDefaults'() {
        given:
        System.properties['disableInitDefaults'] = true
        
        when:
        gradle.apply plugin: RepoConfigPlugin

        then:
        !gradle.ext.has('injectRepositories')
        project.repositories.empty
        project.buildscript.repositories.empty
    }

    def 'extension properties are added for project'() {
        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        then:
        project.corporateIvyPattern == RepoConfigRegistry.ivyPattern
        project.corporateArtifactPattern == RepoConfigRegistry.artifactPattern
        project.corporateIvyAsArtifactPattern == RepoConfigRegistry.ivyAsAnArtifactPattern
    }

    def 'repository URLs are configured correctly (ivy/mvn)'() {
        given:
        System.properties['enableSnapshots'] = 'true'

        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.ivyReleaseRepo = 'https://test2.corporate.com/repo/content/group/releasesIVY'
        gradle.repositoryConfiguration.ivySnapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsIVY'
        gradle.repositoryConfiguration.mvnReleaseRepo = 'https://test2.corporate.com/repo/content/group/releasesMVN'
        gradle.repositoryConfiguration.mvnSnapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsMVN'

        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        then:
        project.repositories.each {
            println it.name
        }
        project.repositories.'intershopIvyReleasesIVY'.url       == URI.create('https://test2.corporate.com/repo/content/group/releasesIVY')
        project.repositories.'intershopMavenReleasesMVN'.url     == URI.create('https://test2.corporate.com/repo/content/group/releasesMVN')
        project.repositories.'intershopIvySnapshotsIVY'.url      == URI.create('https://test2.corporate.com/repo/content/group/snapshotsIVY')
        project.repositories.'intershopMavenSnapshotsMVN'.url    == URI.create('https://test2.corporate.com/repo/content/group/snapshotsMVN')
    }

    def 'repository URLs are configured correctly'() {
        given:
        System.properties['enableSnapshots'] = 'true'

        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        then:
        project.repositories.'intershopIvyReleasesAll'.url       == URI.create('https://test2.corporate.com/repo/content/group/releasesAll')
        project.repositories.'intershopMavenReleasesAll'.url     == URI.create('https://test2.corporate.com/repo/content/group/releasesAll')
        project.repositories.'intershopIvySnapshotsAll'.url      == URI.create('https://test2.corporate.com/repo/content/group/snapshotsAll')
        project.repositories.'intershopMavenSnapshotsAll'.url    == URI.create('https://test2.corporate.com/repo/content/group/snapshotsAll')
        // local repo URLs are tested separately
    }
    
    def 'project repositories can #description'(description, flag, toggle, repos) {
        given:
        System.properties[flag] = toggle
        
        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)
        
        then: 'project repos are configured'
        project.repositories*.name as Set == repos as Set

        where:
        description          | flag               | toggle | repos
        'be configured'      | 'disableRepos'     | false  | IVY + IVY_LOCAL + MAVEN + MAVEN_LOCAL
        'be disabled'        | 'disableRepos'     | true   | IVY_LOCAL + MAVEN_LOCAL
        // default for enableSnapshots is false
        'use snapshots'      | 'enableSnapshots'  | true   | IVY + IVY_SNAPSHOT + IVY_LOCAL + MAVEN + MAVEN_SNAPSHOT + MAVEN_LOCAL
        // default for disableLocalRepo is false
        'disable local repo' | 'disableLocalRepo' | true   | IVY + MAVEN
    }
    
    def 'local repositories are considered first'() {
        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        then:
        def names = project.repositories*.name as List
        def localRepos = names.findAll { it.endsWith('Local') }
        def remoteRepos = names - localRepos
        names[0..(localRepos.size()-1)] == localRepos
        names[(localRepos.size())..names.size()-1] == remoteRepos
    }
    
    def 'local repository defaults to gradle user home'() {
        when:
        gradle.apply plugin: RepoConfigPlugin
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        then:
        def location = new File(gradle.gradleUserHomeDir, '.localRepo').toURI()
        project.repositories.intershopIvyLocal.url == location
        project.repositories.intershopMavenLocal.url == location
    }
    
    def 'local repository location can be configured'() {
        given:
        System.properties['localRepoPath'] = testDir.absolutePath
        
        when:
        gradle.apply plugin: RepoConfigPlugin
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        then:
        def location = testDir.toURI()
        project.repositories.intershopIvyLocal.url == location
        project.repositories.intershopMavenLocal.url == location
    }
    
    def 'local publishing is configured for #plugin'(plugin, repo) {
        when:
        gradle.apply plugin: RepoConfigPlugin
        project.apply plugin: plugin
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        then:
        project.publishing.repositories*.name == [repo]
        
        where:
        plugin          | repo
        'ivy-publish'   | 'intershopIvyLocal'
        'maven-publish' | 'intershopMavenLocal'
    }
    
    def 'remote repositories to foreign hosts are dropped'() {
        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        project.repositories {
            ivy {
                name 'evil'
                url 'http://evil/repository'
            }
        }
        
        then:
        !('evil' in project.repositories*.name)
    }
    
    def 'repositories without url are not dropped'() {
        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        project.repositories {
            ivy {
                name 'noUrl'
                artifactPattern '/foo/bar/in/the/file/system'
            }
        }
        
        then:
        'noUrl' in project.repositories*.name
    }
    
    def 'repositories other than ivy and maven are dropped'() {
        when:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        project.repositories {
            flatDir {
                name 'flatDir'
            }
        }
        
        then:
        !('flatDir' in project.repositories*.name)
    }
    
    def 'snapshot versions are marked as changing'(version, changing) {
        given:
        gradle.apply plugin: RepoConfigPlugin
        gradle.repositoryConfiguration.releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
        gradle.repositoryConfiguration.snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
        gradle.repositoryConfiguration.repoHostList = ['test1.corporate.com', 'test2.corporate.com']
        gradle.repositoryConfiguration.corporateName = 'test2 corporation'
        // explicitly trigger event, required for gradle.allprojects { }
        gradle.buildListenerBroadcaster.projectsLoaded(gradle)

        when:
        // add some configurations
        project.apply plugin: 'java'
        project.dependencies {
            compile "com.example:foo:$version"
        }
        
        then:
        def compileDeps = project.configurations.compile.dependencies
        compileDeps.first().changing == changing
        
        where:
        version          | changing
        '47.11-SNAPSHOT' | true
        '47.11'          | false
    }
}
