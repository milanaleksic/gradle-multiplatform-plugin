package net.milanaleksic.gradle.multiplatform.model

import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.util.ConfigureUtil

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 21/08/2013
 */
class ArtifactsModel {

    private List<TarDefinition> tarConfigurations
    private List<InstallationDefinition> installationConfigurations
    private Project project

    String nsisSetupScript
    CopySpec coreFiles
    Iterable<Project> dependsOnProjects = []

    ArtifactsModel(Project project) {
        this.project = project
        tarConfigurations = []
        installationConfigurations = []
        coreFiles = project.copySpec {}
    }

    void installation(String id) {
        installation(id, null)
    }

    void installation(String id, Closure closure) {
        def definition = new InstallationDefinition(id)
        ConfigureUtil.configure(closure, definition)
        if (!definition.nsisSetupScript)
            definition.nsisSetupScript = nsisSetupScript // fallback to global nsisClassPath
        installationConfigurations << definition
    }

    void tar(String id) {
        tar(id, null)
    }

    void tar(String id, Closure closure) {
        def definition = new TarDefinition(id)
        ConfigureUtil.configure(closure, definition)
        tarConfigurations << definition
    }

    void coreFiles(CopySpec copySpec) {
        coreFiles = copySpec
    }

    List<TarDefinition> getTarConfigurations() {
        return tarConfigurations
    }

    List<InstallationDefinition> getInstallationConfigurations() {
        return installationConfigurations
    }

}
