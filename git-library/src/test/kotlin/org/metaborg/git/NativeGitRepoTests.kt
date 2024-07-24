package org.metaborg.git

import io.kotest.core.spec.style.FunSpec
import org.metaborg.git.GitTestUtils.copyTestGitConfig
import java.io.File

/** Tests the [NativeGit] class. These tests assume a local `git` installation is present. */
class NativeGitRepoTests: FunSpec({

    val gitConfigPath: File = copyTestGitConfig()

    include(GitRepoTests { dir ->
        NativeGit().open(dir, globalConfig = gitConfigPath)
    })

})
