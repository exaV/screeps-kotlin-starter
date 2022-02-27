package screepsai

import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete
import screeps.utils.unsafe.jsObject

val SIMPLE_BODY: Array<BodyPartConstant> = arrayOf(WORK, MOVE, CARRY)

fun spawnCreeps(
    spawn: StructureSpawn
) {

    val body = SIMPLE_BODY

    val newName = "creep_${Game.time}"
    val code = spawn.spawnCreep(body, newName, options {
        memory = jsObject<CreepMemory> { this.state = HarvesterState.GATHER.value }
    })

    when (code) {
        OK -> console.log("spawning $newName with body $body")
        ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> run {console.log("Not enough energy") }
        else -> console.log("unhandled error code $code")
    }
}

fun houseKeeping(creeps: Record<String, Creep>) {
    if (Game.creeps.isEmpty()) return  // this is needed because Memory.creeps is undefined

    for ((creepName, _) in Memory.creeps) {
        if (creeps[creepName] == null) {
            console.log("deleting obsolete memory entry for creep $creepName")
            delete(Memory.creeps[creepName])
        }
    }
}