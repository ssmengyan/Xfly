pluginManagement {
    repositories {
        repositories {
            flatDir {
                dir("plugin_libs")
            }
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Xposed API artifact repository
        maven("https://api.xposed.info/")
    }
}

rootProject.name = "Fake Wifi"
include(":app")
 
