import org.metaborg.convention.Developer
import org.metaborg.convention.MavenPublishConventionExtension

// Workaround for issue: https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.metaborg.convention.root-project")
    alias(libs.plugins.gitonium)
}

rootProjectConvention {
    // Add `publishAll` and `publish` tasks that delegate to the subprojects and included builds.
    registerPublishTasks.set(true)
}

allprojects {
    apply(plugin = "org.metaborg.gitonium")

    version = gitonium.version
    group = "org.metaborg"

    pluginManager.withPlugin("org.metaborg.convention.maven-publish") {
        extensions.configure(MavenPublishConventionExtension::class.java) {
            repoOwner.set("metaborg")
            repoName.set("metaborg-git")

            metadata {
                inceptionYear.set("2024")
                developers.set(listOf(
                    Developer("Virtlink", "Daniel A. A. Pelsmaeker", "developer@pelsmaeker.net"),
                ))
            }
        }
    }
}