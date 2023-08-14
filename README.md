# Screeps Kotlin Starter

Screeps Kotlin Starter is a starting point for a Screeps AI written in Kotlin.
It provides everything you need to start writing your AI including types and automated deployment.
The AI included in this project is roughly what you have after completing the official tutorial.

Feel free to copy/fork this repository to bootstrap your own AI.

Join the official screeps discord on https://discord.com/invite/screeps and join #kotlin for help and general screeps kotlin chat.

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

Deployment is automated with gradle. The 'deploy' task will upload your code to the server.
The branch `default` is used unless you [create a branch](https://support.screeps.com/hc/en-us/articles/203852251-New-feature-code-branches) and change the configuration as described below.

Credentials must be provided in a `gradle.properties` file in the root folder of the project or in `$HOME/.gradle`
    
    screepsUser=<your-username>
    screepsPassword=<your-password>
    screepsHost=https://screeps.com (optional)
    screepsBranch=default (optional)

Alternatively, you can set up an [auth token](https://screeps.com/a/#!/account/auth-tokens) instead of a password (only for official servers)

    screepsToken=<your-token>
    screepsHost=https://screeps.com (optional)
    screepsBranch=default (optional)

### Types
Standalone types are available here: https://github.com/exaV/screeps-kotlin-types

The library also provides some utility functions that are useful for dealing with the api and memory.

### Performance
Kotlin compiles to plain javascript, similar to Typescript. There is no runtime overhead.
The major difference is that kotlin ships with a separate 1.5MB standard library. 
We use the Dead-Code-Elimination 'kotlin-dce-js' to reduce the size of the uploaded code by only uploading 
what you actually use.

### A note on `Object`
Kotlin's `Object` Singletons and top level val/var declarations persist over multiple ticks. 
This can be very useful to store non-essential but expensive-to-calculate data, especially in combination with `lazy()`


### A note on `Memory`
You can only store
* classes which map 1:1 to js objects. See [here for a list](https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript)
* classes which extend an `external interface` and only hold data which fulfills the same conditions.

If you store a class which extends an external interface, you must not call any of its functions, or you will see
an error like "<functionName> is not a function". You may however call extension functions.

Why? The global `Memory` object, as well as `flag.memory`, `creep.memory` etc. which write to global memory, 
is serialized at the end of each tick.
At the start of the next tick the Memory object is deserialized. 
This process is done by the screeps engine and it will cause objects to lose their prototype information,
for kotlin users this means classes won't have any methods. Thus calling class methods will throw an exception.

If you still want to use normal kotlin data structures, you can, if you use a proper serialisation library like
kotlinx.serialisation. With kotlinx.serialization you can even (de)serialize your classes from/to plain js objects which can
be viewed/edited in screep's memory viewer using the
[dynamic encoder](https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/json/jsMain/src/kotlinx/serialization/json/Dynamics.kt)

My recommendation when it comes to storing data in memory is to use basic types and `Array` as much as possible
and sparingly use `external interface` instantiated with `screeps.utils.unsafe.jsObject()` 

### Troubleshooting

#### Important! To upload code to a private server, you must have [screepsmod-auth](https://github.com/ScreepsMods/screepsmod-auth) installed and configured!
If you're using the 'private server' option in Steam, you can install `screepsmod-auth` from the Steam Workshop.

#### Gradle says it deployed correctly, but my code doesnt show up.
Make sure you deployed to a branch which exists on the Server, if not it will fail silently.

#### I see an error like `.iterator() is not a function.`.
You have probably added a collection (List, Map, etc.) or a custom class to memory (creep.memory, room.memory etc.)
You cannot store collections or custom classes in memory. See also ' A note on `Memory`'


#### Imports
Make sure to `import screeps.api.*` as it includes many useful extension functions which are otherwise hard to find
