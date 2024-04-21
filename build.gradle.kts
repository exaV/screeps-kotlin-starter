import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*


plugins {
    kotlin("multiplatform") version "2.0.0-RC1"
    kotlin("plugin.js-plain-objects") version "2.0.0-RC1"
}

repositories {
    mavenCentral()
}


val screepsUser: String? by project
val screepsPassword: String? by project
val screepsToken: String? by project
val screepsHost: String? by project
val screepsBranch: String? by project
val screepsSkipSslVerify: Boolean? by project
val branch = screepsBranch ?: "default"
val host = screepsHost ?: "https://screeps.com"
val minifiedJsDirectory = layout.buildDirectory.dir("minified-js")
val skipSsl = screepsSkipSslVerify ?: false

kotlin {

    sourceSets {
        jsMain {
            dependencies {
                implementation("io.github.exav:screeps-kotlin-types:2.0.2")
            }

        }
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    js {

        browser {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory.set(minifiedJsDirectory)
            }

            testTask {
                useKarma()
            }

            webpackTask {
            }

            binaries.executable()
        }

    }
}




tasks.register("deploy") {
    group = "screeps"
    dependsOn("assemble")

    doFirst { // use doFirst to avoid running this code in configuration phase
        if (screepsToken == null && (screepsUser == null || screepsPassword == null)) {
            throw InvalidUserDataException("you need to supply either screepsUser and screepsPassword or screepsToken before you can upload code")
        }
        val minifiedCodeLocation = minifiedJsDirectory.get().asFile
        if (!minifiedCodeLocation.isDirectory) {
            throw InvalidUserDataException("found no code to upload at ${minifiedCodeLocation.path}")
        }

        /*
        The screeps server expects us to upload our code in the following json format
        https://docs.screeps.com/commit.html#Using-direct-API-access
        {
            "branch":"<branch-name>"
            "modules": {
                "main":<main script as a string, must contain the "loop" function>
                "module1":<a module that is imported in the main script>
            }
        }
        The following code extracts the generated js code from the build folder and writes it to a string that has the
        correct format
         */

        val jsFiles = minifiedCodeLocation.listFiles { _, name -> name.endsWith(".js") }.orEmpty()
        val (mainModule, otherModules) = jsFiles.partition { it.nameWithoutExtension == project.name }
        val main = mainModule.firstOrNull()
            ?: throw IllegalStateException("Could not find js file corresponding to main module in ${minifiedCodeLocation.absolutePath}. Was looking for ${project.name}.js")
        val modules = mutableMapOf<String, String>()
        modules["main"] = main.readText()
        modules.putAll(otherModules.associate { it.nameWithoutExtension to it.readText() })
        val uploadContent = mapOf("branch" to branch, "modules" to modules)
        val uploadContentJson = groovy.json.JsonOutput.toJson(uploadContent)

        logger.lifecycle("Uploading ${jsFiles.count()} files to branch '$branch' on server $host")
        logger.debug("Request Body: $uploadContentJson")

        // upload using java 11 http client -> requires java 11
        val url = URL("$host/api/user/code")
        val request = HttpRequest.newBuilder()
            .uri(url.toURI())
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(uploadContentJson))


        if (screepsToken != null) {
            request.header("X-Token", screepsToken)
        } else {
            fun String.encodeBase64() = Base64.getEncoder().encodeToString(this.toByteArray())
            request.header("Authorization", "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        }

        val response = HttpClient.newBuilder()
            .build()
            .send(request.build(), HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() in 200..299) {
            logger.lifecycle("Upload done! ${response.body()}")
        } else {
            val shortMessage = "Upload failed! ${response.statusCode()}"

            logger.lifecycle(shortMessage)
            logger.lifecycle(response.body())
            logger.error(shortMessage)
            logger.error(response.body())
        }

    }

}

