package starter


import types.base.delete
import types.base.get
import types.base.global.*
import types.base.iterator
import types.base.prototypes.Creep
import types.base.prototypes.structures.SpawnOptions
import types.base.prototypes.structures.StructureSpawn
import types.base.toMap


class CreepOptions(role: Role) : SpawnOptions {
    override val memory = object : CreepMemory {
        val role: String = role.name
    }
}

val minPopulations = arrayOf(Role.HARVESTER to 2, Role.UPGRADER to 1, Role.BUILDER to 2)

fun gameLoop() {
    val mainSpawn: StructureSpawn = Game.spawns["Spawn1"]!!
    val creeps = Game.creeps.toMap()

    //delete memories of creeps that have passed away
    houseKeeping(creeps)

    //make sure we have at least some creeps
    spawnCreeps(minPopulations, creeps, mainSpawn)

    for ((roomName, room) in Game.rooms) {
        if (room.energyAvailable > 549) {
            mainSpawn.spawnCreep(
                arrayOf(
                    WORK,
                    WORK,
                    WORK,
                    WORK,
                    CARRY,
                    MOVE,
                    MOVE
                ),
                "HarvesterBig",
                CreepOptions(Role.HARVESTER)
            )
        }
    }

    for ((_, creep) in creeps) {

        if (creep.memory.role == Role.HARVESTER) {
            Harvester.run(creep)
        }
        if (creep.memory.role == Role.BUILDER) {
            Builder.run(creep);
        }
        if (creep.memory.role == Role.UPGRADER) {
            Upgrader.run(creep)
        }
    }

}

private fun spawnCreeps(
    minPopulations: Array<Pair<Role, Int>>,
    creeps: Map<String, Creep>,
    spawn: StructureSpawn
) {
    for ((role, min) in minPopulations) {
        val current = creeps.filter { (_, creep) -> creep.memory.role == role }
        if (current.size < min) {
            val newName = "${role.name}_${Game.time}"
            val body = arrayOf<BodyPartConstant>(WORK, CARRY, MOVE)
            val code = spawn.spawnCreep(body, newName, CreepOptions(role))

            when (code) {
                OK -> console.log("spawning $newName with body $body")
                ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> run { } // do nothing
                else -> console.log("unhandled error code $code")
            }

        }
    }
}

fun houseKeeping(creeps: Map<String, Creep>) {
    for ((creepName, _) in Memory.creeps) {
        if (creeps[creepName] == null) {
            console.log("deleting obselete memory entry for creep $creepName")
            delete(Memory.asDynamic().creeps[creepName])
        }
    }
}
