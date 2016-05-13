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

import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is the registry for all configurations. It is used by the RepoConfigPlugin.
 */
@TypeChecked
class RepoConfigRegistry {

    final static Logger log = LoggerFactory.getLogger(RepoConfigRegistry)

    // settings for the environment or system property configuration

    /*
     * Repository user name
     */
    final static String REPO_USER_NAME_ENV = 'repoUserName'
    final static String REPO_USER_NAME_SYS = 'REPO_USER_NAME'

    /*
     * Repository user password
     */
    final static String REPO_USER_PASSWD_ENV = 'repoUserPasswd'
    final static String REPO_USER_PASSWD_SYS = 'REPO_USER_PASSWD'

    /*
     * Settings for disable all defaults
     */
    final static String DISABLE_DEFAULTS_ENV = 'disableInitDefaults'
    final static String DISABLE_DEFAULTS_SYS = 'DISABLE_INITDEFAULTS'

    /*
     * Settings for enable all snapshot repositories
     */
    final static String ENABLE_SNAPSHOTS_ENV = 'enableSnapshots'
    final static String ENABLE_SNAPSHOTS_SYS = 'ENABLE_SNAPSHOTS'

    /*
     * Settings for disable all other repositories
     */
    final static String DISABLE_REPOS_ENV = 'disableRepos'
    final static String DISABLE_REPOS_SYS = 'DISABLE_REPOS'

    /*
     * Settings for enable all build script snapshot repositories
     */
    final static String ENABLE_BUILDSCIPT_SNAPSHOTS_ENV = 'enableBuildscriptSnapshots'
    final static String ENABLE_BUILDSCIPT_SNAPSHOTS_SYS = 'ENABLE_BUILDSCRIPT_SNAPSHOTS'

    /*
     * Settings for disable all other build script repositories
     */
    final static String DISABLE_BUILDSCIPT_REPOS_ENV = 'disableBuildscriptRepos'
    final static String DISABLE_BUILDSCIPT_REPOS_SYS = 'DISABLE_BUILDSCRIPT_REPOS'

    /*
     * Settings for disable local build script repositories
     */
    final static String DISABLE_BUILDSCIPT_LOCAL_REPO_ENV = 'disableBuildscriptLocalRepo'
    final static String DISABLE_BUILDSCIPT_LOCAL_REPO_SYS = 'DISABLE_BUILDSCRIPT_LOCAL_REPO'

    /*
     * Setting for disable local repository publishing and usage
     */
    final static String DISABLE_LOCAL_REPO_ENV = 'disableLocalRepo'
    final static String DISABLE_LOCAL_REPO_SYS = 'DISABLE_LOCAL_REPO'

    /*
     * Setting for disable publish ivy pattern
     */
    final static String DISABLE_IVYPATTERN_PUBLISH_ENV = 'disableIvyPatternPublish'
    final static String DISABLE_IVYPATTERN_PUBLISH_SYS = 'DISABLE_IVYPATTERN_PUBLISH'

    /*
     * Setting for disable buildscript ivy pattern
     */
    final static String DISABLE_IVYPATTERN_BUILDSCRIPT_ENV = 'disableIvyPatternBuildscript'
    final static String DISABLE_IVYPATTERN_BUILDSCRIPT_SYS = 'DISABLE_IVYPATTERN_BUILDSCRIPT'

    /*
     * Setting for disable buildscript ivy pattern
     */
    final static String DISABLE_IVYPATTERN_ENV = 'disableIvyPattern'
    final static String DISABLE_IVYPATTERN_SYS = 'DISABLE_IVYPATTERN'

    /*
     * Setting for local repository path
     */
    final static String LOCAL_REPO_PATH_ENV = 'localRepoPath'
    final static String LOCAL_REPO_PATH_SYS = 'LOCAL_REPO_PATH'

    /**
     * For secured repository access it is necessary to specify user credentials
     * This is the user name
     */
    final String repoUserName = ''

    /**
     * For secured repository access it is necessary to specify user credentials
     * This is the user password
     */
    final String repoUserPasswd = ''

    /**
     * Disable default settings
     */
    final boolean disableInitDefaults

    /**
     * Disable usage of repositories
     */
    final boolean disableRepos

    /**
     * Disable usage of repositories for the buildscript configuration
     */
    final boolean disableBuildscriptRepos

    /**
     * Enable usage of snapshot repositories
     */
    final boolean enableSnapshots

    /**
     * Enable usage of snapshot repositories for the buildscript configuration
     */
    final boolean enableBuildscriptSnapshots

    /**
     * Disable usage of local repository
     */
    final boolean disableLocalRepository

    /**
     * Disable usage of local repository for build scripts
     */
    final boolean disableBuildscriptLocalRepo

    /**
     * Disable corporate ivy pattern for publishing
     */
    final boolean disableIvyPatternPublish

    /**
     * Disable corporate ivy pattern for Buildscript repositories
     */
    final boolean disableIvyPatternBuildscript

    /**
     * Disable corporate ivy pattern for build repositories
     */
    final boolean disableIvyPattern

    /**
     * Path for local repository
     */
    final String localRepositoryPath

    RepoConfigRegistry() {
        repoUserName = getConfigurationValue(REPO_USER_NAME_ENV, REPO_USER_NAME_SYS, '')
        repoUserPasswd = getConfigurationValue(REPO_USER_PASSWD_ENV, REPO_USER_PASSWD_SYS, '')

        disableInitDefaults = getConfigurationValue(DISABLE_DEFAULTS_ENV, DISABLE_DEFAULTS_SYS, 'false').toBoolean()

        disableRepos = getConfigurationValue(DISABLE_REPOS_ENV, DISABLE_REPOS_SYS, 'false').toBoolean()
        disableBuildscriptRepos = getConfigurationValue(DISABLE_BUILDSCIPT_REPOS_ENV, DISABLE_BUILDSCIPT_REPOS_SYS, 'false').toBoolean()

        enableSnapshots = getConfigurationValue(ENABLE_SNAPSHOTS_ENV, ENABLE_SNAPSHOTS_SYS, 'false').toBoolean()
        enableBuildscriptSnapshots = getConfigurationValue(ENABLE_BUILDSCIPT_SNAPSHOTS_ENV, ENABLE_BUILDSCIPT_SNAPSHOTS_SYS, 'false').toBoolean()

        disableLocalRepository = getConfigurationValue(DISABLE_LOCAL_REPO_ENV, DISABLE_LOCAL_REPO_SYS, 'false').toBoolean()
        disableBuildscriptLocalRepo = getConfigurationValue(DISABLE_BUILDSCIPT_LOCAL_REPO_ENV, DISABLE_BUILDSCIPT_LOCAL_REPO_SYS, 'false').toBoolean()
        localRepositoryPath = getConfigurationValue(LOCAL_REPO_PATH_ENV, LOCAL_REPO_PATH_SYS, '')

        disableIvyPatternPublish = getConfigurationValue(DISABLE_IVYPATTERN_PUBLISH_ENV, DISABLE_IVYPATTERN_PUBLISH_SYS, 'false').toBoolean()
        disableIvyPatternBuildscript = getConfigurationValue(DISABLE_IVYPATTERN_BUILDSCRIPT_ENV, DISABLE_IVYPATTERN_BUILDSCRIPT_SYS, 'false').toBoolean()
        disableIvyPattern = getConfigurationValue(DISABLE_IVYPATTERN_ENV, DISABLE_IVYPATTERN_SYS, 'false').toBoolean()
    }

    /**
     * Calculates the setting for special configuration from the system
     * or java environment.
     *
     * @param envVar        name of environment variable (-D...)
     * @param systemVar     name of system environemnt variable
     * @param defaultValue  default value
     * @return              the string configuration
     */
    private static String getConfigurationValue(String envVar, String systemVar, String defaultValue) {
        if(System.properties[envVar]) {
            return System.properties[envVar].toString().trim()
        } else if(System.getenv(systemVar)) {
            return System.getenv(systemVar).toString().trim()
        }
        return defaultValue
    }

    /**
     * Pattern for the ivy files
     **/
    final static String ivyPattern = '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'

    /**
     * Pattern used to retreive artifacts. Note: Although we use the extra attribute 'classifier' to mark platform specific
     * artifacts and there can be multiple artifacts of the same type (mostly 'local') in a component.
     **/
    final static String artifactPattern = '[organisation]/[module]/[revision]/[ext]s/[artifact]-[type](-[classifier])-[revision].[ext]'

    /**
     * This pattern is used to be able to retrieve the Ivy.xml file as an artifact - which is currently
     * the only way to retrieve it using Gradle's API.
     */
    final static String ivyAsAnArtifactPattern = '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'

    /**
     * Add ivy repository configuration
     * @param repohandler
     * @param path
     * @param user
     * @param passwd
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    void addIvyRepoConfig(RepositoryHandler repohandler, String path, String user = repoUserName, String passwd = repoUserPasswd) {
        if(path) {
            // only use last part of a URL as repo ID
            def id = path.contains('/') ? path.substring(path.lastIndexOf('/') + 1) : path

            repohandler.ivy {
                name "ivy${id.capitalize()}"
                url "${path}"
                if (user && passwd) {
                    credentials {
                        username user
                        password passwd
                    }
                }
            }
        }
    }

    /**
     * Add maven repository configuration
     * @param repohandler
     * @param path
     * @param user
     * @param passwd
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    void addMavenRepo(RepositoryHandler repohandler, String path, String user = repoUserName, String passwd = repoUserPasswd) {
        if(path) {
            // only use last part of a URL as repo ID
            def id = path.contains('/') ? path.substring(path.lastIndexOf('/') + 1) : path

            repohandler.maven {
                name = "maven${id.capitalize()}"
                url "${path}"
                if (user && passwd) {
                    credentials {
                        username user
                        password passwd
                    }
                }
            }
        }
    }

    /**
     * Add local Ivy repository configuration
     * @param repohandler
     * @param path
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    void addLocalIvyRepo(RepositoryHandler repohandler, String path) {
        repohandler.ivy {
            name 'ivyLocal'
            url "file://${path}"
        }
    }

    /**
     * Add local Maven repository configuration
     * @param repohandler
     * @param path
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    void addLocalMavenRepo(RepositoryHandler repohandler, String path) {
        repohandler.maven {
            name = 'mavenLocal'
            url "file://${path}"
        }
    }

    /**
     * Gradle will check the remote repository even if a dependency with the same version is already in the local cache.
     * @param configurations
     * @param suffix
     */
    void setChangingModules(ConfigurationContainer configurations, String suffix = '-SNAPSHOT') {
        configurations.all { Configuration config ->
            config.dependencies.withType(ExternalModuleDependency) { ExternalModuleDependency dependency ->
                if (dependency.version) {
                    dependency.changing = dependency.version.endsWith(suffix)
                }
            }
        }
    }
}
