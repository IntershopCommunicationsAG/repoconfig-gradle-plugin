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

class RepoConfigExtension {

    /**
     * This is the url of the release repository group.
     * It is used only for actions with read access.
     **/
    String releaseRepo

    /**
     * This is the url of the snapshot repository group.
     * It is used only for actions with read access.
     **/
    String snapshotRepo

    /**
     * This is the url of an IVY release repository group.
     * If this repo is specified, the configuration for releaseRepo is not used.
     * It is used only for actions with read access.
     **/
    String ivyReleaseRepo

    /**
     * This is the url of an IVY release repository group.
     * If this repo is specified, the configuration for snapshotRepo is not used.
     * It is used only for actions with read access.
     **/
    String ivySnapshotRepo

    /**
     * This is the url of an MAVEN release repository group.
     * If this repo is specified, the configuration for releaseRepo is not used.
     * It is used only for actions with read access.
     **/
    String mvnReleaseRepo

    /**
     * This is the url of an MAVEN release repository group.
     * If this repo is specified, the configuration for snapshotRepo is not used.
     * It is used only for actions with read access.
     **/
    String mvnSnapshotRepo

    /**
     * The list of all allowed repository hosts.
     */
    List<String> repoHostList

    /**
     * This is the corporate name for information output.
     */
    String corporateName

    /**
     * This is a separate public maven repository.
     * It is used for special use cases.
     */
    String pulicMavenRepo

    /**
     * Add jcenter to the list of repositories
     */
    boolean activateJCenter = false
}
