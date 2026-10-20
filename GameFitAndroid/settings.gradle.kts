// Репозитории для Gradle-плагинов и зависимостей проекта.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Запрещаем модулям объявлять собственные репозитории.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Имя Android-проекта и подключённый модуль приложения.
rootProject.name = "GameFitAndroid"
include(":app")
