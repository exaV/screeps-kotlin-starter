# Screeps Kotlin Starter

Screeps Kotlin Starter is a starting point for a Screeps AI written in Kotlin.
It provides everything you need to start writing your AI including types and automated deployment.
The included AI is roughly what you have after completing the official tutorial.

### Deployment

Deployment is automated with gradle. 
The branch 'kotlin-start' is used by default, make sure it exists.
Usage:

    ./gradlew deploy


Credentials must be provided in a 'gradle.properties' file in the root folder of the project.
    
    screepsUser=<your-username>
    screepsPassword=<your-password>

### Types
Standalone types are available here: https://github.com/exaV/screeps-kotlin-types