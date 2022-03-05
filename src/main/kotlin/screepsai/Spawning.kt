package screepsai

import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete

val BODYPART_COST = hashMapOf(
    MOVE to 50,
    WORK to 100,
    ATTACK to 80,
    CARRY to 50,
    HEAL to 250,
    RANGED_ATTACK to 150,
    TOUGH to 10,
    CLAIM to 600
)

class Body(val parts: Array<BodyPartConstant>) {
    val cost = parts.sumOf { part -> BODYPART_COST[part]!! }
}

val BASE_BODY = Body(arrayOf(WORK, MOVE, CARRY))

val HARVESTER_BODIES = arrayOf(
    BASE_BODY,
)

fun getBody(role: ScreepRole, energyAvailable: Int): Body {
    when (role) {
        ScreepRole.HARVESTER -> return HARVESTER_BODIES.last { it.cost <= energyAvailable }
    }
}

fun spawnCreeps(
    role: ScreepRole,
    spawn: StructureSpawn
) {

    val body = try {
        getBody(role, spawn.room.energyCapacityAvailable)
    } catch (error: NoSuchElementException) {
        BASE_BODY
    }

    val newName = "creep_${Game.time}"

    when (val code = spawn.spawnCreep(body.parts, newName)) {
        OK -> console.log("spawning $newName with body $body")
        ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> run { console.log("Not enough energy") }
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