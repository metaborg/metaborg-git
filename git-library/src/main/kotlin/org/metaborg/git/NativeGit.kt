package org.metaborg.git

import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Interact with Git through the installed Git command.
 */
class NativeGit(
    /** The Git command to use. */
    private val gitCommand: String = "git",
): Git {
    override fun getGitVersion(): String? {
        return try {
            runCommand(listOf(gitCommand, "--version"), null).substringBefore('\n')
        } catch (e: CommandException) {
            null
        }
    }

    override fun open(directory: File, globalConfig: File?): GitRepo {
        val environment = mutableMapOf<String, String>()
        globalConfig?.let { environment["GIT_CONFIG_GLOBAL"] = it.absolutePath }
        return NativeGitRepo(directory, environment)
    }


    /**
     * Calls Git commands for a specific repository.
     *
     * This implementation assumes a local installation of Git, reachable through the `git` command.
     */
    private inner class NativeGitRepo(
        /** The current working directory. */
        override val directory: File,
        /** Environment variables to apply. */
        val environment: Map<String, String> = emptyMap(),
    ) : GitRepo {

        override fun getCurrentBranch(): String {
            // NOTE: `git rev-parse --abbrev-ref HEAD` would often also work to obtain the branch name.
            //  However, if the current repository has no commits yet, it will fail where `git branch --show-current` will not.
            //  See also: https://stackoverflow.com/a/78100106/146622
            return git("branch", "--show-current")
        }

        override fun getCurrentCommitHash(short: Boolean): String {
            return git("rev-parse",
                *(if (short) listOf("--short=7") else emptyList()).toTypedArray(),
                "--verify",
                "HEAD",
            )
        }

        override fun getIsClean(): Boolean {
            return getStatus().isEmpty()
        }

        override fun getTagDescription(
            vararg patterns: String,
            withHash: Boolean,
            firstParentOnly: Boolean,
            commit: String,
        ): String {
            if (withHash) {
                return git(
                    "describe",
                    // Both annotated and non-annotated tags
                    "--tags",
                    // Abbreviate the commit hash to 7 or more characters (however many are needed to make it unique)
                    "--abbrev=7",
                    // Just the abbreviated commit hash if no tag is found
                    "--always",
                    // Follow only the first parent of merge commits
                    *(if (firstParentOnly) listOf("--first-parent") else emptyList()).toTypedArray(),
                    // Match the pattern
                    *patterns.map { "--match=$it" }.toTypedArray(),
                    commit,
                )
            } else {
                try {
                    return git(
                        "describe",
                        // Both annotated and non-annotated tags
                        "--tags",
                        // Leave out the commit hash
                        "--abbrev=0",
                        // Follow only the first parent of merge commits
                        *(if (firstParentOnly) listOf("--first-parent") else emptyList()).toTypedArray(),
                        // Match the pattern
                        *patterns.map { "--match=$it" }.toTypedArray(),
                        commit,
                    )
                } catch (ex: CommandException) {
                    if (ex.exitCode == 128 && "No names found, cannot describe anything" in ex.stderr) {
                        // No tags found
                        return ""
                    } else {
                        throw ex
                    }
                }
            }
        }

        override fun getStatus(showUntracked: Boolean): String {
            return git("status", "--porcelain", "--untracked-files=${if (showUntracked) "normal" else "no"}")
        }

        override fun init() {
            git("init", "--initial-branch=main")
        }

        override fun addAll() {
            git("add", "--all")
        }

        override fun commit(message: String, allowEmpty: Boolean) {
            git("commit",
                "-m", message,
                *(if (allowEmpty) listOf("--allow-empty") else emptyList()).toTypedArray()
            )
        }

        override fun detach() {
            git("checkout", "--detach")
        }

        override fun tag(tagName: String) {
            git("tag", tagName)
        }

        override fun createBranch(branchName: String) {
            git("switch", "--create", branchName)
        }

        /**
         * Runs a Git command.
         *
         * @param args The command arguments.
         * @return The output of the command.
         * @throws CommandException If the command fails or returns a non-zero exit code.
         * @throws IOException If an I/O error occurs.
         */
        @Throws(IOException::class)
        private fun git(vararg args: String): String {
            return runCommand(
                command = listOf(this@NativeGit.gitCommand) + args.asList(),
                workingDirectory = directory,
                environment = environment,
            )
        }
    }
}