Gradle plugin (Application adaptation) for multiplatform builds
===========================

```gradle
buildscript {
    repositories {
        maven {
            url 'http://maven.milanaleksic.net/release'
        }
    }
    dependencies {
        classpath 'net.milanaleksic.gradle:multiplatform:0.1'
    }
}
apply plugin: 'multiplatform'

multiplatform {
    version = mcsVersion

    runner {
        mainClassName = srcMainClass // String - class with main()
        workingDir = startupDir      // String - startup up directory for the run
    }

    artifacts {
        nsisClassPath = configurations.buildOnly.asPath
        nsisSetupScript = nsisSetupScriptLoc

        coreFiles {
            file("$startupDir/.launcher")
        }

        installation('win32', FAMILY_WINDOWS, ARCH_X86) {
            overrideDir = file(buildOverridesWin)
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
