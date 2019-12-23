pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin2js" || requested.id.id == "kotlin-dce-js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}
plugins {
    id("com.gradle.enterprise").version("3.1.1")
}
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
rootProject.name = "screeps-kotlin"
