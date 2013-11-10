package net.milanaleksic.gradle.multiplatform.model

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 18/08/2013
 */
class ApplicationModel {

    private List<RunConfiguration> runConfigurations

    ArtifactsModel artifacts

    DependencyMappingsModel dependencyMappings

    String version

    ApplicationModel(Project project) {
        runConfigurations = []
        artifacts = new ArtifactsModel(project)
        dependencyMappings = new DependencyMappingsModel(project)
    }

    void runner(Closure closure) {
        RunConfiguration configuration = new RunConfiguration()
        ConfigureUtil.configure(closure, configuration)
        runConfigurations << configuration
    }

    void dependencyMappings(Closure closure) {
        ConfigureUtil.configure(closure, dependencyMappings)
    }

    void artifacts(Closure closure) {
        ConfigureUtil.configure(closure, artifacts)
    }

    List<RunConfiguration> getRunConfigurations() {
        return runConfigurations
    }

}
