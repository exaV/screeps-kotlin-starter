# Screeps Kotlin Starter

Screeps Kotlin Starter is a starting point for a Screeps AI written in Kotlin.
It provides everything you need to start writing your AI including types and automated deployment.
The AI included in this project is roughly what you have after completing the official tutorial.

Feel free to copy/fork this repository to bootstrap your own AI.

Join the official screeps slack on https://chat.screeps.com/ and join #kotlin for help and general screeps kotlin chat.

### Code upload

Deployment is automated with gradle. 
The branch `kotlin-start` is used by default, [make sure it exists](https://support.screeps.com/hc/en-us/articles/203852251-New-feature-code-branches).

Credentials must be provided in a `gradle.properties` file in the root folder of the project.
    
    screepsUser=<your-username>
    screepsPassword=<your-password>
    screepsHost=https://screeps.com (optional)
    screepsBranch=kotlin-start (optional)
    
Usage:

    ./gradlew deploy


### Types
Standalone types are available here: https://github.com/exaV/screeps-kotlin-types

### Performance
Kotlin compiles to plain javascript, similar to Typescript. There is no runtime overhead.
The major difference is that kotlin ships with a separate 1.5MB standard library. While it does not significantly slow us down,
it does increase script load time and more importantly almost fills the code-size limit. 
This project uses the Dead-Code-Elimination-Plugin (kotlin-dce-js) 
to drastically reduce the size of all dependencies (e.g. stdlib is 186KB).

### A note on `Object`
Kotlin's `Object` Singletons persist over multiple ticks. 
This can be very useful to store non-essential but expensive-to-calculate data, especially in combination with `lazy()`

It is highly recommended to use Isolated Virtual Machine (IVM) if you intend to make use of stateful Singletons.


