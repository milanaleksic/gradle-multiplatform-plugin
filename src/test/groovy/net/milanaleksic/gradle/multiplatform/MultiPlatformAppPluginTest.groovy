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
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'multiplatform'

        project.multiplatform.artifacts {
            installation('win32', FAMILY_WINDOWS, ARCH_X86) {
            }
            installation('win64', FAMILY_WINDOWS, ARCH_X86_64) {
            }
            tar('linux32', FAMILY_UNIX, ARCH_X86) {
            }
            tar('linux64', FAMILY_UNIX, ARCH_X86_64) {
            }
        }

        try {
            project.evaluate()
            fail('configuration is not set, expected exception to be raised')
        } catch (ProjectConfigurationException exception) {
            assertThat(exception?.cause?.cause, not(nullValue()))
            assertThat(exception?.cause?.cause, instanceOf(UnknownConfigurationException))
        }
    }

}
