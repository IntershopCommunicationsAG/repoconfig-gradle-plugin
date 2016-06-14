/*
 * Copyright 2015 Intershop Communications AG.
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

import com.intershop.gradle.test.AbstractIntegrationSpec
import spock.lang.Unroll

@Unroll
class RepoConfigPluginIntegSpec extends AbstractIntegrationSpec {

    def 'CorporatePlugin is compatible with Gradle #gradleVersion'(gradleVersion) {

        given:

        String libs = ''
        pluginClasspath.each {
            libs += "classpath files { new File('${it.absolutePath.replace('\\', '/')}') }\n"
        }

        new File(testProjectDir, 'init.gradle') << """\
        initscript {
            dependencies {
                ${libs}
            }
        }
        apply plugin: com.intershop.gradle.repoconfig.RepoConfigPlugin

        repositoryConfiguration {

            releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
            snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
            pulicMavenRepo = 'https://maven.repo.com/maven'
            activateJCenter = true
            repoHostList = ['test1.corporate.com', 'test2.corporate.com']
            corporateName = 'test2 corporation'
        }
        """.stripIndent()

        buildFile << """
        // provide some configurations etc.
        apply plugin: 'java'
        apply plugin: 'ivy-publish'
        apply plugin: 'maven-publish'

        assert repositories*.name as Set == ['ivyLocal', 'mavenLocal', 'ivyReleasesAll', 'mavenReleasesAll', 'mavenMaven', 'BintrayJCenter'] as Set
        assert publishing.repositories*.name as Set == ['ivyLocal', 'mavenLocal'] as Set
        """.stripIndent()
        
        when:
        def result = preparedGradleRunner
                    .withTestKitDir(new File(testProjectDir, 'testkit-tmp'))
                    .withArguments('-I', 'init.gradle', '-s')
                    .withGradleVersion(gradleVersion)
                    .build()
        
        then:
        noExceptionThrown()

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'CorporatePlugin is compatible with Gradle and without JCenter #gradleVersion'(gradleVersion) {

        given:

        String libs = ''
        pluginClasspath.each {
            libs += "classpath files { new File('${it.absolutePath.replace('\\', '/')}') }\n"
        }

        new File(testProjectDir, 'init.gradle') << """\
        initscript {
            dependencies {
                ${libs}
            }
        }
        apply plugin: com.intershop.gradle.repoconfig.RepoConfigPlugin

        repositoryConfiguration {

            releaseRepo = 'https://test2.corporate.com/repo/content/group/releasesAll'
            snapshotRepo = 'https://test2.corporate.com/repo/content/group/snapshotsAll'
            pulicMavenRepo = 'https://maven.repo.com/maven'
            repoHostList = ['test1.corporate.com', 'test2.corporate.com']
            corporateName = 'test2 corporation'
        }
        """.stripIndent()

        buildFile << """
        // provide some configurations etc.
        apply plugin: 'java'
        apply plugin: 'ivy-publish'
        apply plugin: 'maven-publish'

        assert repositories*.name as Set == ['ivyLocal', 'mavenLocal', 'ivyReleasesAll', 'mavenReleasesAll', 'mavenMaven'] as Set
        assert publishing.repositories*.name as Set == ['ivyLocal', 'mavenLocal'] as Set
        """.stripIndent()

        when:
        def result = preparedGradleRunner
                .withTestKitDir(new File(testProjectDir, 'testkit-tmp'))
                .withArguments('-I', 'init.gradle', '-s')
                .withGradleVersion(gradleVersion)
                .build()

        then:
        noExceptionThrown()

        where:
        gradleVersion << supportedGradleVersions
    }
}
