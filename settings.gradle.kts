pluginManagement {
    repositories {
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
    }
}

rootProject.name = "Control de gastos"
include(":app")
include(":core")
include(":feature:accounts")
include(":feature:analytics")
include(":feature:credit_cards")
include(":feature:reminders")
include(":feature:transactions")
include(":feature:widgets")
 