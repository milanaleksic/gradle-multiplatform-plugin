package net.milanaleksic.gradle.multiplatform.model

import groovy.transform.ToString

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 19/08/2013
 */
@ToString(includeNames = true, includeSuper = true)
class InstallationDefinition extends ArtifactDefinition {

    String nsisSetupScript

    InstallationDefinition(String id) {
        super(id)
    }
}
