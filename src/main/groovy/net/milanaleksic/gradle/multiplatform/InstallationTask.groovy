package net.milanaleksic.gradle.multiplatform

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.atomic.AtomicBoolean

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 20/08/2013
 */
class InstallationTask extends Copy {

    private static final Logger log = Logging.getLogger(InstallationTask.class);

    String version
    String classifier
    String nsisSetupScript

    private static AtomicBoolean onceTaskSetupFlag = new AtomicBoolean(false)

    public InstallationTask() {
        super()
        into(temporaryDirFactory)
    }

    @TaskAction
    def setUpNsisTask() {
        if (onceTaskSetupFlag.compareAndSet(false, true)) {
            log.info("Registering NSIS Ant task")
            project.configurations { nsisInstallationOnly }
            project.dependencies {
                nsisInstallationOnly "net.sf:nsisant:1.3"
            }
            project.ant.taskdef(
                    name: "nsis",
                    classname: "com.danielreese.nsisant.Task",
                    classpath: project.configurations.nsisInstallationOnly.asPath
            )
        }
        executeBuild()
    }

    @OutputFile
    def File getOutputFile() {
        def distributionsDir = new File(project.buildDir, "distributions")
        if (!distributionsDir.exists())
            distributionsDir.mkdir()
        return new File(distributionsDir, "${project.name}-${version}-${classifier}.exe");
    }

    def executeBuild() {
        project.ant.nsis(script: nsisSetupScript, verbosity: "2", noconfig: "yes", nocd: "yes") {
            define(name: 'OUTPUT_FILENAME', value: getOutputFile())
            define(name: 'INSTALL_SOURCE_DIR', value: getDestinationDir().absolutePath)
        }
    }

}
