package org.metaborg.git

import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Runs a command.
 *
 * @param command The command.
 * @param workingDirectory The working directory to use; or `null` to use the current JVM working directory.
 * @param environment The environment variables to use.
 * @return The standard output of the command.
 * @throws CommandException If the command fails or returns a non-zero exit code.
 * @throws IOException If an I/O error occurs.
 */
@Throws(CommandException::class, IOException::class, InterruptedException::class)
internal fun runCommand(
    command: List<String>,
    workingDirectory: File?,
    environment: Map<String, String> = emptyMap(),
): String {
    require(command.isNotEmpty()) { "No command arguments provided." }

    val processBuilder = ProcessBuilder().command(*command.toTypedArray())
    processBuilder.environment().putAll(environment)
    processBuilder.directory(workingDirectory)

    // THROWS: IOException, SecurityException, UnsupportedOperationException
    val process = processBuilder.start()
    // NOTE: We don't close streams that we didn't open.
    val stdout = process.inputStream.bufferedReader().readText()
    val stderr = process.errorStream.bufferedReader().readText()
    // THROWS: InterruptedException
    val exitCode = process.waitFor()
    if (exitCode != 0) throw CommandException(command.joinToString(" "), exitCode, stderr)
    return stdout.trim()
}
