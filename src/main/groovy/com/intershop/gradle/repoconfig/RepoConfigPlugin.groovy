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

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.invocation.Gradle
import org.gradle.api.publish.ivy.plugins.IvyPublishPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class RepoConfigPlugin implements Plugin<Gradle> {

    final Logger log = LoggerFactory.getLogger(RepoConfigPlugin)
    RepoConfigRegistry config
    RepoConfigExtension extension

    /**
     * This applies the Gradle plugin.
     * @param gradle
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    void apply (Gradle gradle) {
        initializeSystemPropertiesFromGradleProperties(gradle.gradleUserHomeDir)
        extension = gradle.extensions.create('repositoryConfiguration', RepoConfigExtension)

        config = new RepoConfigRegistry()

        // default local repo path
        String repoPath = config.localRepositoryPath ?: (new File(gradle.gradleUserHomeDir, '.localRepo')).canonicalPath

        int outputProjectCount = 0
        gradle.allprojects(new Action<Project>() {
            @Override
            void execute(Project project) {
                if(outputProjectCount == 0) {
                    println "Defaults for ${extension.getCorporateName() ?: 'project'} are used for inject repositories!"
                    outputProjectCount = 1
                }
                if(!config.disableInitDefaults) {
                    if (!config.disableLocalRepository) {
                        log.debug('Add local repositories for project, build script and publishing for project {}', project.path)

                        //add local repo
                        addLocalRepository(project.repositories, repoPath)

                        //add publishing configuration for local development
                        project.plugins.withType(IvyPublishPlugin) {
                            project.publishing {
                                config.addLocalIvyRepo(repositories, repoPath)
                            }
                        }

                        project.plugins.withType(MavenPublishPlugin) {
                            project.publishing {
                                config.addLocalMavenRepo(repositories, repoPath)
                            }
                        }

                        // add log message for users
                        if (project.gradle.rootProject == project) {
                            project.logger.warn("The local artifacts are stored in ${repoPath}!")
                        }
                    }

                    //add repositories
                    if (!config.disableRepos) {
                        log.debug('Add repositories for project build {}', project.path)
                        addRepositories(project.repositories, project.configurations)
                    }
                    //add snapshot repositories
                    if (config.enableSnapshots) {
                        log.debug('Add snapshot repositories for project {}', project.path)
                        addSnapshotsRepositories(project.repositories, project.configurations)
                    }

                    addPulicMavenRepository(project.repositories)
                    addPulicMavenRepository(project.buildscript.repositories)

                    addJCenter(project.repositories)
                    addJCenter(project.buildscript.repositories)
                }
                // set pattern for publishing
                project.plugins.withType(IvyPublishPlugin) {
                    project.publishing {
                        repositories.withType(IvyArtifactRepository) { IvyArtifactRepository repo ->
                            if(repo.name.startsWith('intershop')) {
                                log.debug("Add pattern to {}", repo.name)
                                repo.patternLayout {
                                    ivy config.ivyPattern
                                    artifact config.artifactPattern
                                }
                            } else {
                                log.debug("Pattern will be not added to {}.", repo.name)
                            }
                        }
                    }
                }

                //set pattern for buildscript repositories
                project.repositories.withType(IvyArtifactRepository) { IvyArtifactRepository repo ->
                    if(repo.name.startsWith('intershop')) {
                        log.debug("Add pattern to {}", repo.name)
                        repo.patternLayout {
                            ivy config.ivyPattern
                            artifact config.artifactPattern
                            artifact config.ivyAsAnArtifactPattern
                        }
                    } else {
                        log.debug("Pattern will be not added to {}.", repo.name)
                    }
                }

                //set pattern for repositories
                project.buildscript.repositories.withType(IvyArtifactRepository) { IvyArtifactRepository repo ->
                    if(repo.name.startsWith('intershop')) {
                        log.debug("Add pattern to {}", repo.name)
                        repo.patternLayout {
                            ivy config.ivyPattern
                            artifact config.artifactPattern
                            artifact config.ivyAsAnArtifactPattern
                        }
                    } else {
                        log.debug("Pattern will be not added to {}.", repo.name)
                    }
                }

                // Remove repositories that are non-local and pointing to our repository server or not maven or ivy repositories
                project.repositories.all { ArtifactRepository repo ->
                    if (!(repo instanceof MavenArtifactRepository) && !(repo instanceof IvyArtifactRepository)) {
                        project.repositories.remove repo
                        project.logger.warn("Repository '{}' of type '{}' on project '{}' removed. Only Maven und Ivy repositories are allowed.",
                                repo.name, repo.getClass().name, project.path)
                    } else if (extension.getRepoHostList() && ! extension.getRepoHostList().isEmpty() && repo.url && !(repo.url.host.toString() in extension.getRepoHostList()) && !(repo.url.scheme == 'file')) {
                        project.repositories.remove repo
                        project.logger.warn("Repository '{}' with url '{}' removed from project '{}'. Only repositories on '{}' are allowed.",
                                repo.name, repo.url, project.path, extension.getRepoHostList().join(','))
                    }
                }

                //add project properties
                project.ext {
                    corporateIvyPattern = config.ivyPattern
                    corporateArtifactPattern = config.artifactPattern
                    corporateIvyAsArtifactPattern = config.ivyAsAnArtifactPattern
                }
            }
        })

    }

    /**
     * Add an additional Maven repository configurations
     * @param repositories
     * @param configurations
     */
    private void addJCenter(RepositoryHandler repositories) {
        if(extension.activateJCenter) {
            log.debug('Add JCenter repository if activateJCenter is true')
            MavenArtifactRepository repo = repositories.jcenter()
            try {
                if( extension.getRepoHostList() && ! extension.getRepoHostList().isEmpty() ) {
                    extension.getRepoHostList().add(repo.url.getHost().toString())
                }
            } catch (Exception ex) {
                log.info("This is not a URL or there is no host in jcenter configuration.", extension.getPublicMavenRepo())
            }
        }
    }

    /**
     * Add an additional Maven repository configurations
     * @param repositories
     * @param configurations
     */
    private void addPulicMavenRepository(RepositoryHandler repositories) {
        log.debug('Add maven repository {}', extension.getPublicMavenRepo())
        config.addMavenRepo(repositories, extension.getPublicMavenRepo(), '', '')

        try {
            if( extension.getRepoHostList() && ! extension.getRepoHostList().isEmpty() ) {
                extension.getRepoHostList().add((new URL(extension.getPublicMavenRepo())).getHost().toString())
            }
        }catch (Exception ex) {
            log.info("This is not a URL or there is no host in {}", extension.getPublicMavenRepo())
        }
    }

    /**
     * Add available snapshot repository configurations
     * @param repositories
     * @param configurations
     */
    private void addSnapshotsRepositories(RepositoryHandler repositories, ConfigurationContainer configurations) {
        if(extension.getIvySnapshotRepo()) {
            // add default repositories
            log.debug('Add snapshot ivy repository {}', extension.getIvySnapshotRepo())
            config.addIvyRepoConfig(repositories, extension.getIvySnapshotRepo())
        }

        if(extension.getMvnSnapshotRepo()) {
            // add default repositories
            log.debug('Add maven repository {}', extension.getMvnSnapshotRepo())
            config.addMavenRepo(repositories, extension.getMvnSnapshotRepo())
        }

        if(extension.getSnapshotRepo()) {
            // add default repositories
            log.debug('Add snapshot ivy repository {}', extension.getSnapshotRepo())
            config.addIvyRepoConfig(repositories, extension.getSnapshotRepo())

            log.debug('Add maven repository {}', extension.getSnapshotRepo())
            config.addMavenRepo(repositories, extension.getSnapshotRepo())
        }
        // check always for new version
        config.setChangingModules(configurations)
    }

    /**
     * Add available repository configurations
     * @param repositories
     * @param configurations
     */
    private void addRepositories(RepositoryHandler repositories, ConfigurationContainer configurations) {
        if(extension.getIvyReleaseRepo()) {
            // add default repositories
            log.debug('Add ivy repository {}', extension.getIvyReleaseRepo())
            config.addIvyRepoConfig(repositories, extension.getIvyReleaseRepo())
        }

        if(extension.getMvnReleaseRepo()) {
            // add default repositories
            log.debug('Add maven repository {}', extension.getMvnReleaseRepo())
            config.addMavenRepo(repositories, extension.getMvnReleaseRepo())
        }

        if(extension.getReleaseRepo()) {
            // add default repositories
            log.debug('Add ivy repository {}', extension.getReleaseRepo())
            config.addIvyRepoConfig(repositories, extension.getReleaseRepo())

            log.debug('Add maven repository {}', extension.getReleaseRepo())
            config.addMavenRepo(repositories, extension.getReleaseRepo())
        }

        // check always for new version
        config.setChangingModules(configurations)
    }

    /**
     * Add local repository
     * @param repositories
     * @param path
     */
    private void addLocalRepository(RepositoryHandler repositories, String path) {
        log.debug('Add ivy repository {}', path)
        config.addLocalIvyRepo(repositories, path)
        log.debug('Add maven repository {}', path)
        config.addLocalMavenRepo(repositories, path)
    }

    private static void initializeSystemPropertiesFromGradleProperties(File gradleUserHome) {
        // load gradle.properties
        def gradlePropsFile = new File(gradleUserHome, 'gradle.properties')
        if (!gradlePropsFile.exists())
            return
        Properties gradleProps = new Properties()
        gradlePropsFile.withReader { r ->
            gradleProps.load(r)
        }
        // set all system properties that are not already defined
        gradleProps.keySet().collect {
            it.toString()
        }.findAll {
            it.startsWith(Project.SYSTEM_PROP_PREFIX) && !System.hasProperty(it)
        }.each {
            def k = it.substring(Project.SYSTEM_PROP_PREFIX.size()+1)
            System.setProperty(k, gradleProps[it].toString())
        }
    }
}
