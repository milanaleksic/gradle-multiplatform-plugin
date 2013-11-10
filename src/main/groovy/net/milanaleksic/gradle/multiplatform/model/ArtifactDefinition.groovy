package net.milanaleksic.gradle.multiplatform.model

import groovy.transform.ToString

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 18/08/2013
 */
@ToString(includeNames = true)
abstract class ArtifactDefinition {

    String id

    File overrideDir

    ArtifactDefinition(String id) {
        this.id = id
    }
}
