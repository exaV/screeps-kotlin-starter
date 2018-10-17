package starter


import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.delete
import screeps.utils.unsafe.jsObject


private val minPopulations = arrayOf(Role.HARVESTER to 2, Role.UPGRADER to 1, Role.BUILDER to 2)

fun gameLoop() {
    val mainSpawn: StructureSpawn = Game.spawns["Spawn1"] ?: return

    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    //make sure we have at least some creeps
    spawnCreeps(minPopulations, Game.creeps.values, mainSpawn)

    //spawn a big creep if we have plenty of energy
    for ((_, room) in Game.rooms) {
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
                "HarvesterBig_${Game.time}",
                options {
                    memory = jsObject<CreepMemory> {
                        this.role = Role.HARVESTER
                    }
                }
            )
        }
    }

    for ((_, creep) in Game.creeps) {
        when (creep.memory.role) {
            Role.HARVESTER -> creep.harvest()
            Role.BUILDER -> creep.build()
            Role.UPGRADER -> creep.upgrade(mainSpawn.room.controller!!)
            else -> creep.pause()
        }
    }

}

private fun spawnCreeps(
    minPopulations: Array<Pair<Role, Int>>,
    creeps: Array<Creep>,
    spawn: StructureSpawn
) {
    for ((role, min) in minPopulations) {
        val current = creeps.filter { it.memory.role == role }
        if (current.size < min) {
            val newName = "${role.name}_${Game.time}"
            val body = arrayOf<BodyPartConstant>(WORK, CARRY, MOVE)
            val code = spawn.spawnCreep(body, newName, options {
                memory = jsObject<CreepMemory> { this.role = role }
            })

            when (code) {
                OK -> console.log("spawning $newName with body $body")
                ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> run { } // do nothing
                else -> console.log("unhandled error code $code")
            }

        }
    }
}

private fun houseKeeping(creeps: Record<String, Creep>) {
    for ((creepName, _) in Memory.creeps) {
        if (creeps[creepName] == null) {
            console.log("deleting obsolete memory entry for creep $creepName")
            delete(Memory.creeps[creepName])
        }
    }
}
