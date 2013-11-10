package net.milanaleksic.gradle.multiplatform.model

import org.gradle.api.Project
/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 10/11/2013
 */
class DependencyMappingsModel {

    private List<DependencyMappingDefinition> dependencyMappingConfigurations
    private Project project

    DependencyMappingsModel(Project project) {
        this.project = project
        dependencyMappingConfigurations = []
    }

    void dependencyMapping(String id, String family, List<String> archs) {
        def definition = new DependencyMappingDefinition(id, family, archs)
        dependencyMappingConfigurations << definition
    }

    void dependencyMapping(String id, String family, String arch) {
        dependencyMapping(id, family, [arch])
    }

    List<DependencyMappingDefinition> getDependencyMappingConfigurations() {
        return dependencyMappingConfigurations
    }
}
