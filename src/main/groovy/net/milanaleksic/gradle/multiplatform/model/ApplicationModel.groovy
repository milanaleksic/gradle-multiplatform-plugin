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

    String version

    ApplicationModel(Project project) {
        runConfigurations = []
        artifacts = new ArtifactsModel(project)
    }

    void runner(Closure closure) {
        RunConfiguration configuration = new RunConfiguration()
        ConfigureUtil.configure(closure, configuration)
        runConfigurations << configuration
    }

    void artifacts(Closure closure) {
        ConfigureUtil.configure(closure, artifacts)
    }

    List<RunConfiguration> getRunConfigurations() {
        return runConfigurations
    }

}
