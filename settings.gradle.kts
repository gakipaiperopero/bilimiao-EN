pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
    }
}
rootProject.name = "bilimiao"
include(":app")
include(":desktop-app")
include(":bilimiao-comm", ":bilimiao-cover", ":bilimiao-download", "bilimiao-appwidget", "bilimiao-compose")
fun findAndroidSdk(): Boolean {
    val sdkDir = System.getenv("ANDROID_HOME")
    if (sdkDir != null && java.io.File(sdkDir).exists()) return true
    val localProps = java.io.File(rootDir, "local.properties")
    if (localProps.exists()) {
        val props = java.util.Properties()
        localProps.inputStream().use { props.load(it) }
        val dir = props.getProperty("sdk.dir") ?: return false
        return java.io.File(dir).exists()
    }
    return false
}
if (findAndroidSdk()) {
    include(":DanmakuFlameMaster")
}
include(":danmaku-engine")
include(":benchmark")
include(":grpc-generator")
