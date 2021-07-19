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

    ./gradlew deploy

Deployment is automated with gradle. 
The branch `default` branch is used unless you [create a branch](https://support.screeps.com/hc/en-us/articles/203852251-New-feature-code-branches) and change the configuration as described below.

Credentials must be provided in a `gradle.properties` file in the root folder of the project or in `$HOME/.gradle`
    
    screepsUser=<your-username>
    screepsPassword=<your-password>
    screepsHost=https://screeps.com (optional)
    screepsBranch=my-branch (optional)

Alternatively, you can set up an [auth token](https://screeps.com/a/#!/account/auth-tokens) instead of a password (only for official servers)

    screepsToken=<your-token>
    screepsHost=https://screeps.com (optional)
    screepsBranch=kotlin-start (optional)

### Types
Standalone types are available here: https://github.com/exaV/screeps-kotlin-types

### Performance
Kotlin compiles to plain javascript, similar to Typescript. There is no runtime overhead.
The major difference is that kotlin ships with a separate 1.5MB standard library. 
We use the Dead-Code-Elimination 'kotlin-dce-js' to reduce the size of all dependencies drastically.

### A note on `Object`
Kotlin's `Object` Singletons persist over multiple ticks. 
This can be very useful to store non-essential but expensive-to-calculate data, especially in combination with `lazy()`

### Troubleshooting

#### Important! To upload code to a private server, you must have [screepsmod-auth](https://github.com/ScreepsMods/screepsmod-auth) installed and configured!
If you're using the 'private server' option in Steam, you can install `screepsmod-auth` from the Steam Workshop.

#### Gradle says it deployed correctly, but my code doesnt show up.
Make sure you deployed to a branch which exists on the Server, if not it will fail silently.

#### Imports
Make sure to `import screeps.api.*` as it includes many useful extension functions which are otherwise hard to find
