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

    ArtifactsModel(Project project) {
        this.project = project
        tarConfigurations = []
        installationConfigurations = []
        coreFiles = project.copySpec {}
    }

    void installation(String id, String family, List<String> archs, Closure closure) {
        def definition = new InstallationDefinition(id, family, archs)
        ConfigureUtil.configure(closure, definition)
        if (!definition.nsisSetupScript)
            definition.nsisSetupScript = nsisSetupScript // fallback to global nsisClassPath
        installationConfigurations << definition
    }

    void installation(String id, String family, String arch, Closure closure) {
        installation(id, family, [arch], closure)
    }

    void tar(String id, String family, List<String> archs, Closure closure) {
        def definition = new TarDefinition(id, family, archs)
        ConfigureUtil.configure(closure, definition)
        tarConfigurations << definition
    }

    void tar(String id, String family, String arch, Closure closure) {
        tar(id, family, [arch], closure)
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
