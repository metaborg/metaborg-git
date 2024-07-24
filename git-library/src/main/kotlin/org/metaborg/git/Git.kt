package org.metaborg.git

import java.io.File
import java.io.IOException

/** Interface for interacting with Git. */
interface Git {

    /**
     * Gets the version of Git that is locally installed.
     *
     * @return The version of Git that is locally installed;
     * or `null` if Git is not installed or the version could not be determined.
     * @throws IOException If an I/O error occurs.
     */
    fun getGitVersion(): String?

    /**
     * Opens the Git repository at the specified directory.
     *
     * @param directory The directory of the Git repository.
     * @param globalConfig The global Git configuration file to use;
     * or `null` to use the default global Git configuration.
     * @return The Git repository object.
     */
    fun open(directory: File, globalConfig: File? = null): GitRepo
}
