package net.milanaleksic.gradle.multiplatform.model

import groovy.transform.ToString

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 10/11/2013
 */
@ToString(includeNames = true)
class DependencyMappingDefinition {

    String id

    String family

    List<String> archs

    DependencyMappingDefinition(String id, String family, List<String> archs) {
        this.id = id
        this.family = family
        this.archs = archs
    }

}
