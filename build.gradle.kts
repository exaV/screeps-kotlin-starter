import java.net.HttpURLConnection
import java.net.URL
import java.util.*

plugins {
    kotlin("js") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.exav:screeps-kotlin-types:1.13.0")
    testImplementation(kotlin("test-js"))
}

val screepsUser: String? by project
val screepsPassword: String? by project
val screepsToken: String? by project
val screepsHost: String? by project
val screepsBranch: String? by project
val branch = screepsBranch ?: "default"
val host = screepsHost ?: "https://screeps.com"
val minifiedJsDirectory: String = File(buildDir, "minified-js").absolutePath

kotlin {
    js {
        useCommonJs()
        browser {
            @Suppress("EXPERIMENTAL_API_USAGE")
            dceTask {
                dceOptions {
                    outputDirectory = minifiedJsDirectory
                }
                keep(
                    "${project.name}.loop"
                )
            }

            testTask {
                useMocha()
            }
        }
    }
}


val processDceKotlinJs by tasks.getting(org.jetbrains.kotlin.gradle.dsl.KotlinJsDce::class)
fun String.encodeBase64() = Base64.getEncoder().encodeToString(this.toByteArray())


tasks.register("deploy") {
    group = "screeps"
    dependsOn(processDceKotlinJs)

    doFirst { // use doFirst to avoid running this code in configuration phase
        if (screepsToken == null && (screepsUser == null || screepsPassword == null)) {
            throw InvalidUserDataException("you need to supply either screepsUser and screepsPassword or screepsToken before you can upload code")
        }
        val minifiedCodeLocation = File(minifiedJsDirectory)
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

        // upload using very old school HttpURLConnection as it is available in jdk < 9
        val url = URL("$host/api/user/code")
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        if (screepsToken != null) {
            connection.setRequestProperty("X-Token", screepsToken)
        } else {
            connection.setRequestProperty("Authorization", "Basic " + "$screepsUser:$screepsPassword".encodeBase64())
        }
        connection.outputStream.use {
            it.write(uploadContentJson.byteInputStream().readBytes())
        }

        val code = connection.responseCode
        val message = connection.responseMessage
        if (code in 200..299) {
            val body = connection.inputStream.bufferedReader().readText()
            logger.lifecycle("Upload done! $body")
        } else {
            val body = connection.errorStream.bufferedReader().readText()
            val shortMessage = "Upload failed! $code $message"

            logger.lifecycle(shortMessage)
            logger.lifecycle(body)
            logger.error(shortMessage)
            logger.error(body)
        }
        connection.disconnect()

    }

}
