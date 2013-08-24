package net.milanaleksic.gradle.multiplatform.model

import groovy.transform.ToString

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 19/08/2013
 */
@ToString(includeNames = true, includeSuper = true, excludes = "nsisClassPath")
class InstallationDefinition extends ArtifactDefinition {

    String nsisClassPath

    String nsisSetupScript

    InstallationDefinition(String id, String family, List<String> archs) {
        super(id, family, archs)
    }
}
