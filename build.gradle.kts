import org.jetbrains.kotlin.gradle.dsl.KotlinJsDce
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org._10ne.gradle.rest.RestTask
import java.io.File
import java.util.*

plugins {
    id("kotlin2js") version "1.3.0"
    id("kotlin-dce-js") version "1.3.0"
    id("org.tenne.rest") version "0.4.2"
}

repositories {
    jcenter()
}

dependencies {
    implementation("ch.delconte.screeps-kotlin:screeps-kotlin-types:1.0.3")
}

val screepsUser: String by project
val screepsPassword: String by project
val screepsHost: String? by project
val screepsBranch: String? by project
val branch = screepsBranch ?: "kotlin-start"
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

    register("deploy", RestTask::class) {
        group = "screeps"
        dependsOn("build")
        val modules = mutableMapOf<String, String>()

        httpMethod = "post"
        uri = "$host/api/user/code"
        requestHeaders = mapOf("Authorization" to "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        contentType = groovyx.net.http.ContentType.JSON
        requestBody = mapOf("branch" to branch, "modules" to modules)

        doFirst {
            println("pushing $screepsUser's code to branch $branch on server $host")

            modules.putAll(File("$buildDir/kotlin-js-min/main")
                .listFiles { _, name -> name.endsWith(".js") }
                .associate { it.nameWithoutExtension to it.readText() })
        }

    }
}

