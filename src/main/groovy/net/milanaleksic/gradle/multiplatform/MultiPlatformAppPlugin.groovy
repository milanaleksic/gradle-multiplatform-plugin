package net.milanaleksic.gradle.multiplatform

import net.milanaleksic.gradle.multiplatform.model.*
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

import java.util.concurrent.atomic.AtomicReference

import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS

class MultiPlatformAppPlugin implements Plugin<Project> {

    private static final Logger log = Logging.getLogger(ApplicationModel.class);

    public static final String ARCH_X86 = 'x86'
    public static final List<String> ARCH_X86_64 = ['x86_64', 'amd64']

    static final String APPLICATION_PLUGIN_NAME = 'multiplatform'
    static final String APPLICATION_GROUP = APPLICATION_PLUGIN_NAME

    static final String TASK_RUN_NAME = 'run'
    static final String TASK_TAR_NAME_PREFIX = 'tar'
    static final String TASK_INSTALL_NAME_PREFIX = 'install'
    private static final String CONFIGURATION_ARCHIVES = 'archives'

    private Project project
    private ApplicationModel model
    private AtomicReference<RunConfiguration> currentRunConfiguration = new AtomicReference<>()

    void apply(final Project project) {
        this.project = project
        project.plugins.apply(JavaPlugin)
        model = project.extensions.create(APPLICATION_PLUGIN_NAME, ApplicationModel, project)
        project.afterEvaluate {
            project.dependencies.add(JavaPlugin.COMPILE_CONFIGURATION_NAME, getConfigurationForThisPlatform())
            model.runConfigurations.each { addRunConfiguration(it, model.artifacts) }
            if (model.artifacts.tarConfigurations.empty && model.artifacts.installationConfigurations.empty)
                return
            model.artifacts.tarConfigurations.each { addTarConfiguration(it, model.artifacts) }
            model.artifacts.installationConfigurations.each { addInstallationConfiguration(it, model.artifacts) }
        }
    }

    Configuration getConfigurationForThisPlatform() {
        List<DependencyMappingDefinition> definitions =
                model.dependencyMappings?.dependencyMappingConfigurations?.findAll {
                    DependencyMappingDefinition definition ->
                        isFamily(definition.family) && definition.archs.find { Os.isArch(it) }
                }
        if (definitions.size() != 1) {
            log.error("Dumping information since no suitable configuration found for build platform")
            log.error("Dependency mapping size: " + model.dependencyMappings?.dependencyMappingConfigurations?.size())
            model.dependencyMappings.dependencyMappingConfigurations?.each {
                DependencyMappingDefinition definition ->
                    log.error("isFamily(definition.family)=${isFamily(definition.family)} for definition.family=${definition.family}")
                    definition.archs.each {
                        log.error("Os.isArch(it)=${Os.isArch(it)} for arch=${it}")
                    }
                    log.error "definition.archs.find { Os.isArch(it) }=${definition.archs.find { Os.isArch(it) }}"
            }
            throw new StopExecutionException("OS not supported via unique multiplatform archive: " +
                    "${System.getProperty('os.name').toLowerCase()} / ${System.getProperty('os.arch').toLowerCase()}")
        }
        return project.configurations.getByName(definitions[0].id)
    }

    /* since windows 8 is not recognized by Ant 1.8.4 */
    private static boolean isFamily(String family) {
        final def OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
        if (OS_NAME == "windows 8" && family == FAMILY_WINDOWS)
            return true
        else
            return Os.isFamily(family)
    }

    private def generateDistributionContents(AbstractCopyTask task, ArtifactDefinition definition, ArtifactsModel artifacts) {
        def jar = project.tasks[JavaPlugin.JAR_TASK_NAME]
        def runtimeDepsWithoutThisPlatformDeps = ((LinkedHashSet) project.configurations.runtime.getFiles()).clone()
        runtimeDepsWithoutThisPlatformDeps.removeAll(getConfigurationForThisPlatform().getFiles())
        task.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        task.with(model.artifacts.coreFiles)
        task.from(definition.overrideDir) {
            exclude('**/*.sh') // shell scripts are special case. The rest is logical
        }
        task.from(definition.overrideDir) {
            include('**/*.sh')
            fileMode = 0755
        }
        task.into('lib') {
            from(jar)
            if (artifacts.dependsOnProjects) {
                from {
                    def allDeps = []
                    artifacts.dependsOnProjects.each { child ->
                        child.configurations.compile.each { cfg ->
                            if (cfg.isFile())
                                allDeps << cfg
                        }
                        child.configurations.runtime.each { cfg ->
                            if (cfg.isFile())
                                allDeps << cfg
                        }
                    }
                    allDeps.removeAll(getConfigurationForThisPlatform().getFiles())
                    (project.files(allDeps.unique()))
                }
                from {
                    def jars = []
                    artifacts.dependsOnProjects.collect { child ->
                        jars << child.jar.archivePath
                    }
                    (project.files(jars.unique()))
                }
            }
            from(runtimeDepsWithoutThisPlatformDeps)
            from(project.configurations.getByName(definition.id))
        }
    }

    private void addTarConfiguration(TarDefinition definition, ArtifactsModel artifacts) {
        String titleCapitalized = definition.id.substring(0, 1).toUpperCase() + definition.id.substring(1)
        def taskTitle = "$TASK_TAR_NAME_PREFIX$titleCapitalized"
        log.info("Adding Tar task: $definition under task $taskTitle")
        Tar archiveTask = project.tasks.create(taskTitle, Tar)
        archiveTask.description = "Bundles the project as a JVM application with deps for config \"${definition.id}\""
        archiveTask.group = APPLICATION_GROUP
        archiveTask.classifier = definition.id
        archiveTask.compression = Compression.GZIP
        archiveTask.version = model.version ? model.version : ''
        if (artifacts.dependsOnProjects) {
            archiveTask.dependsOn << artifacts.dependsOnProjects.assemble
        }
        generateDistributionContents(archiveTask, definition, artifacts)
        project.artifacts.add(CONFIGURATION_ARCHIVES, archiveTask)
    }

    private void addInstallationConfiguration(InstallationDefinition definition, ArtifactsModel artifacts) {
        String titleCapitalized = definition.id.substring(0, 1).toUpperCase() + definition.id.substring(1)
        def taskTitle = "$TASK_INSTALL_NAME_PREFIX$titleCapitalized"
        if (!isFamily(FAMILY_WINDOWS)) {
            log.warn("Installations are avoided since the build environment is not Windows")
            return
        }
        log.info("Adding Installation: $definition under task $taskTitle")
        InstallationTask installTask = project.tasks.create(taskTitle, InstallationTask)
        installTask.description = "Creates NSIS installation for config \"${definition.id}\""
        installTask.group = APPLICATION_GROUP
        installTask.classifier = definition.id
        installTask.nsisSetupScript = definition.nsisSetupScript
        installTask.version = model.version ? model.version : ''
        if (artifacts.dependsOnProjects) {
            installTask.dependsOn << artifacts.dependsOnProjects.assemble
        }
        generateDistributionContents(installTask, definition, artifacts)
        project.artifacts.add(CONFIGURATION_ARCHIVES, installTask.getOutputFile()) {
            builtBy(installTask)
            type 'exe'
        }
    }

    private void addRunConfiguration(RunConfiguration runConfiguration, ArtifactsModel artifacts) {
        log.info("Received run configuration: $runConfiguration")
        if (currentRunConfiguration.getAndSet(runConfiguration) != null)
            throw new GradleException("It is illegal to set up multiple run configurations!")
        doAddRunConfiguration(runConfiguration, artifacts)
    }

    private void doAddRunConfiguration(RunConfiguration runConfiguration, ArtifactsModel artifacts) {
        def run = project.tasks.create(TASK_RUN_NAME, JavaExec)
        run.description = "Runs this project as a JVM application"
        run.group = APPLICATION_GROUP
        run.classpath = project.sourceSets.main.runtimeClasspath
        run.workingDir = { runConfiguration.workingDir }
        run.conventionMapping.main = { runConfiguration.mainClassName }
        run.conventionMapping.jvmArgs = { runConfiguration.applicationDefaultJvmArgs }
        // depends on projects is now in artifactsmodel
        if (artifacts.dependsOnProjects) {
            run.dependsOn << artifacts.dependsOnProjects.assemble
            artifacts.dependsOnProjects.each { project ->
                run.classpath.add(project.sourceSets.main.runtimeClasspath)
            }
        }
    }
}