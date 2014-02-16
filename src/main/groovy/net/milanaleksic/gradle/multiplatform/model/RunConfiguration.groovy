package net.milanaleksic.gradle.multiplatform.model

import groovy.transform.ToString

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 18/08/2013
 */
@ToString
class RunConfiguration {

    String mainClassName

    String workingDir = '.'

    /**
     * Array of string arguments to pass to the JVM when running the application
     */
    Iterable<String> applicationDefaultJvmArgs = []

}
