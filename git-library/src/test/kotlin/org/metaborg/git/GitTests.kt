package org.metaborg.git

import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.shouldNotBe

/** Tests implementations of the [Git] interface. */
@Suppress("TestFunctionName")
fun GitTests(gitBuilder: () -> Git) = funSpec {

    context("getGitVersion()") {
        test("returns the version of the locally installed Git") {
            // Arrange
            val git = gitBuilder()

            // Act
            val gitVersion = git.getGitVersion()

            // Assert
            println("Git version: $gitVersion")
            gitVersion shouldNotBe null
        }
    }

}
