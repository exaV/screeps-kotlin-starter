import org._10ne.gradle.rest.RestTask
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.dsl.KotlinJsDce
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.util.*


plugins {
    id("kotlin2js") version "1.3.31"
    id("kotlin-dce-js") version "1.3.31"
    id("org.tenne.rest") version "0.4.2"
    id("org.sonarqube") version "2.8"
    id("org.ajoberstar.grgit") version "1.7.2"

}

repositories {
    jcenter()
}

dependencies {
    implementation("ch.delconte.screeps-kotlin:screeps-kotlin-types:1.3.0")
    implementation(kotlin("stdlib-js"))
    testImplementation(kotlin("test-js"))
}

val screepsUser: String? by extra(System.getenv("screepsUser"))
val screepsPassword: String? by extra(System.getenv("screepsPassword"))
val screepsToken: String? by extra(System.getenv("screepsToken"))
val screepsHost: String? by extra(System.getenv("screepsHost"))
val screepsBranch: String? by extra(System.getenv("screepsBranch"))
val branch = if (Grgit.open().branch.current.name != "master") {
    "beta-kt"
} else {
    "master-kt"
}
val host = screepsHost ?: "https://screeps.com"

fun String.encodeBase64() = Base64.getEncoder().encodeToString(this.toByteArray())

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            moduleKind = "commonjs"
            outputFile = "${buildDir}/screeps/main.js"
            sourceMap = true
            metaInfo = true
        }
    }

    "runDceKotlinJs"(KotlinJsDce::class) {
        keep("main.loop")
        dceOptions.devMode = false
    }

    register<RestTask>("deploy") {
        group = "screeps"
        dependsOn("build")
        val modules = mutableMapOf<String, String>()
        val minifiedCodeLocation = File("$buildDir/kotlin-js-min/main")

        httpMethod = "post"
        uri = "$host/api/user/code"
        requestHeaders = if (screepsToken != null)
            mapOf("X-Token" to screepsToken)
        else
            mapOf("Authorization" to "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        contentType = groovyx.net.http.ContentType.JSON
        requestBody = mapOf("branch" to branch, "modules" to modules)

        doFirst {
            if (screepsUser == null && screepsPassword == null && screepsToken == null) {
                throw InvalidUserDataException("you need to supply either screepsUser and screepsPassword or screepsToken before you can upload code")
            }
            if (!minifiedCodeLocation.isDirectory) {
                throw InvalidUserDataException("found no code to upload at ${minifiedCodeLocation.path}")
            }

            val jsFiles = minifiedCodeLocation.listFiles { _, name -> name.endsWith(".js") }
            modules.putAll(jsFiles.associate { it.nameWithoutExtension to it.readText() })

            println("uploading ${jsFiles.count()} files to branch $branch on server $host")
        }

    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "Rhinomcd_screeps-kotlin")
        property("sonar.organization", "rhinomcd")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}