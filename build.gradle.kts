import org._10ne.gradle.rest.RestTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJsDce
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.util.*

plugins {
    id("kotlin2js") version "1.3.21"
    id("kotlin-dce-js") version "1.3.21"
    id("org.tenne.rest") version "0.4.2"
}

repositories {
    jcenter()
}

dependencies {
    implementation("ch.delconte.screeps-kotlin:screeps-kotlin-types:1.2.0")
    implementation(kotlin("stdlib-js"))
    testImplementation(kotlin("test-js"))
}

val screepsUser: String? by project
val screepsPassword: String? by project
val screepsToken: String? by project
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

        if (screepsUser == null && screepsPassword == null && screepsToken == null) {
            throw InvalidUserDataException("you need to supply either screepsUser and screepsPassword or screepsToken")
        }

        httpMethod = "post"
        uri = "$host/api/user/code"
        requestHeaders = if (screepsToken != null)
            mapOf("X-Token" to screepsToken)
        else
            mapOf("Authorization" to "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        contentType = groovyx.net.http.ContentType.JSON
        requestBody = mapOf("branch" to branch, "modules" to modules)

        doFirst {
            println("pushing your code to branch $branch on server $host")

            modules.putAll(File("$buildDir/kotlin-js-min/main")
                    .listFiles { _, name -> name.endsWith(".js") }
                    .associate { it.nameWithoutExtension to it.readText() })
        }

    }
}

