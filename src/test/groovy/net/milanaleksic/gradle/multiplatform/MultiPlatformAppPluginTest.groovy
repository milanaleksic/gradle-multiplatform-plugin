package net.milanaleksic.gradle.multiplatform

import net.milanaleksic.gradle.multiplatform.model.ApplicationModel
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.hamcrest.CoreMatchers.nullValue
import static org.hamcrest.core.IsInstanceOf.instanceOf
import static org.hamcrest.core.IsNot.not
import static org.junit.Assert.assertThat

import static net.milanaleksic.gradle.multiplatform.MultiPlatformAppPlugin.*
import static org.apache.tools.ant.taskdefs.condition.Os.*

class MultiPlatformAppPluginTest {

    @Test
    public void initAsExpected() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'multiplatform'

        assertThat(project.extensions.multiplatform, instanceOf(ApplicationModel))
    }

    @Test
    public void checkForThisPlatform() {
        Project project = prepareProject()
        project.multiplatform.dependencyMappings {
            dependencyMapping('win32', FAMILY_WINDOWS, ARCH_X86)
            dependencyMapping('win64', FAMILY_WINDOWS, ARCH_X86_64)
            dependencyMapping('linux32', FAMILY_UNIX, ARCH_X86)
            dependencyMapping('linux64', FAMILY_UNIX, ARCH_X86_64)
            dependencyMapping('linuxArm', FAMILY_UNIX, ARCH_ARM)
        }
        project.evaluate()
    }

    @Test
    public void checkArchiveBuildingForThisPlatform() {
        Project project = prepareProject()
        project.multiplatform {

            dependencyMappings {
                dependencyMapping('win32', FAMILY_WINDOWS, ARCH_X86)
                dependencyMapping('win64', FAMILY_WINDOWS, ARCH_X86_64)
                dependencyMapping('linux32', FAMILY_UNIX, ARCH_X86)
                dependencyMapping('linux64', FAMILY_UNIX, ARCH_X86_64)
                dependencyMapping('linuxArm', FAMILY_UNIX, ARCH_ARM)
            }

            artifacts {
                nsisSetupScript = 'someLocation'

                coreFiles = project.copySpec {
                    into('bin')
                    from("core")
                }

                installation('win32')
                installation('win64')

                tar('linux32')
                tar('linux64')
                tar('linuxArm')
            }
        }
        project.evaluate()
    }

    private Project prepareProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'multiplatform'

        project.configurations {
            win32
            win64
            linux32
            linux64
            linuxArm
        }
        project
    }

}
