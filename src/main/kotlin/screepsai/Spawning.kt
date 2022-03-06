package screepsai

import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete
import screepsai.roles.*

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
    Body(arrayOf(WORK, WORK, MOVE)),
)

val UPGRADER_BODIES = arrayOf(
    Body(arrayOf(WORK, MOVE, MOVE, CARRY, CARRY))
)

val TRANSPORTER_BODIES = arrayOf(
    Body(arrayOf(MOVE, MOVE, MOVE, CARRY, CARRY, CARRY))
)

fun getBody(role: CreepRole, energyAvailable: Int): Body {
    val bodies = when (role) {
        CreepRole.UNASSIGNED -> return BASE_BODY
        CreepRole.HARVESTER -> HARVESTER_BODIES
        CreepRole.UPGRADER -> UPGRADER_BODIES
        CreepRole.TRANSPORTER -> TRANSPORTER_BODIES
    }

    return bodies.last { it.cost <= energyAvailable }
}

fun spawnCreeps(
    role: CreepRole,
    spawn: StructureSpawn
) {

    val body = try {
        getBody(role, spawn.room.energyCapacityAvailable)
    } catch (error: NoSuchElementException) {
        BASE_BODY
    }

    val newName = "creep_${role.name}_${Game.time}"
    val code = spawn.spawnCreep(body.parts, newName)
    when (code) {
        OK -> console.log("spawning $newName with body $body")
        ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> console.log("Not enough energy to spawn a new ${role.name}")
        else -> console.log("unhandled error code $code")
    }

    if (code != OK) {
        return
    }

    val creep = Game.creeps[newName]!!
    creep.setRole(role)
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