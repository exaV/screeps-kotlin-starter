# Screeps Kotlin Starter

Screeps Kotlin Starter is a starting point for a Screeps AI written in Kotlin.
It provides everything you need to start writing your AI including types and automated deployment.
The AI included in this project is roughly what you have after completing the official tutorial.

Feel free to copy/fork this repository to bootstrap your own AI.

Join the official screeps slack on https://chat.screeps.com/ and join #kotlin for help and general screeps kotlin chat.

### Getting started

This AI tries to build a 'big' harvester creep with __4__ work parts which cost a hefty 550 energy!

If you are familiar with the official tutorials (if not do them now!) you already know this requires multiple extensions 
and thus RCL 2. Once you run the bot in the simulator you will notice it takes quite a while to get there, because
this bot is quite dumb. Below are a few things you could improve that will also help you learn a bit about screeps-kotlin:

* make the upgrader transport 50 energy per run (instead of only 2)
* harvest from more than just one Source
* harvest from the closest Source first
* don't build the extensions at hardcoded locations

### Code upload

Deployment is automated with gradle. 
The branch `kotlin-start` is used by default, [make sure it exists on the server](https://support.screeps.com/hc/en-us/articles/203852251-New-feature-code-branches) (it will fail silently otherwise).

Credentials must be provided in a `gradle.properties` file in the root folder of the project.
    
    screepsUser=<your-username>
    screepsPassword=<your-password>
    screepsHost=https://screeps.com (optional)
    screepsBranch=kotlin-start (optional)

Alternatively, you can set up an [auth token](https://screeps.com/a/#!/account/auth-tokens) instead of a password (only for official servers)

    screepsToken=<your-token>
    screepsHost=https://screeps.com (optional)
    screepsBranch=kotlin-start (optional)
    
Usage:

    ./gradlew deploy


### Types
Standalone types are available here: https://github.com/exaV/screeps-kotlin-types

### Performance
Kotlin compiles to plain javascript, similar to Typescript. There is no runtime overhead.
The major difference is that kotlin ships with a separate 1.5MB standard library. We recommend to use the the Dead-Code-Elimination-Plugin 'kotlin-dce-js', like this project does, to drastically reduce the size of all dependencies (e.g. stdlib is 180kb afterwards).

### A note on `Object`
Kotlin's `Object` Singletons persist over multiple ticks. 
This can be very useful to store non-essential but expensive-to-calculate data, especially in combination with `lazy()`

### Troubleshooting

#### Gradle says it deployed correctly, but my code doesnt show up.
  Make sure you deployed to a branch which exists on the Server, if not it will fail silently.
