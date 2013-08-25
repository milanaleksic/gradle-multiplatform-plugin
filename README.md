Gradle plugin for multiplatform builds
===========================

Idea
----

This Gradle plugin serves as an alternative to "Application" plugin which is part of the Gradle distribution.
That plugin has "run" task which allows running of the Java application, but the project I had couldn't use
it easily (without hacks) since I had SWT application with different includes needed for each platform.
Thus, this plugin was born.

There are two purposes in this plugin, similar to the one available in Application plugin:
- running
- creating output, final archives - per environment. Available supported outputs are
    - tgz - for linux-like target environments, initiated with **tar** command
    - NSIS-based installation - for windows-like target environments, created with **install** statement

### Override files
If you have files that need to be placed in the per-environment archive, you can place them in separate
directories and then use **overrideDir** property of archive to artifact model object to say where those 
files are placed.

This feature, thus, allows you to have e.g. EXE for windows installations and SH for Linux

### Shell scripts
In case you want to pack shell scripts, they are *always* placed in tgz archive with 755 permissions.
If this is not a suitable behavior, you might need to fork this repo since it is not configurable for now.

DSL
---

The entire DSL is contained in **multiplatform** statement. I decided to present you with the complete working
example since it is the easiest to getting how it works:

```groovy

// import Ant helper static values to differ system families
import static org.apache.tools.ant.taskdefs.condition.Os.*

// import helper static constants to differ system architectures
import static net.milanaleksic.gradle.multiplatform.MultiPlatformAppPlugin.*

// this is where the plugin can be downloaded from
buildscript {
    repositories {
        maven {
            url 'http://maven.milanaleksic.net/release'
        }
    }
    dependencies {
        classpath 'net.milanaleksic.gradle:multiplatform:0.2'
    }
}
apply plugin: 'multiplatform'

// finally, the DSL
multiplatform {
    version = mcsVersion             // mcsVersion: String - a simple 

    runner {
        mainClassName = srcMainClass // srcMainClass: String - class with main()
        workingDir = startupDir      // startupDir: String - startup up directory for the JVM process
    }

    // artifacts domain object is used to define output archives
    artifacts {
        // for configuring NSIS ant task
        nsisSetupScript = nsisSetupScriptLoc            // nsisSetupScriptLoc: String - location of global NSIS script

        coreFiles {
            file("$startupDir/.launcher")
        }

        // e.g. for windows, 32-bit environment, we want to create an archive with "win32" suffix 
        installation('win32', FAMILY_WINDOWS, ARCH_X86) {
            nsisSetupScript = nsisSetupScriptLoc32 // in case you wish to override global NSIS script
            overrideDir = file(buildOverridesWin)  // buildOverridesWin: String - override files for this environment
        }
        installation('win64', FAMILY_WINDOWS, ARCH_X86_64) {
            overrideDir = file(buildOverridesWin)
        }

        tar('linux32', FAMILY_UNIX, ARCH_X86) {
            overrideDir = file(buildOverridesLinux)
        }
        tar('linux64', FAMILY_UNIX, ARCH_X86_64) {
            overrideDir = file(buildOverridesLinux)
        }
    }
}
```
