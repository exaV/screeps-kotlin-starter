package io.r2n.screeps.ai.roles

import io.r2n.screeps.ai.getBodyCost
import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.api.structures.Structure
import screeps.api.structures.StructureSpawn

interface EmployedCreep {
    val options: SpawnOptions
    fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant>

    fun spawn(spawn: StructureSpawn,
              minEnergy: Int = 0,
              maxEnergy: Int = spawn.room.energyAvailable,
              name: String = "${options.memory?.role?.name}_${Game.time}"
    ): ScreepsReturnCode {
        if (spawn.room.energyAvailable < minEnergy) {
            console.log("room energy check failed for ${name}")
            return ERR_NOT_ENOUGH_ENERGY
        }

        val body = calculateOptimalBodyParts(maxEnergy)
        val code = spawn.spawnCreep(body, name, options)
        when (code) {
            OK -> console.log("spawning $name with body [$body]")
            ERR_BUSY -> run {} //do nothing
            ERR_NOT_ENOUGH_ENERGY -> console.log("not enough energy to spawn $name" +
                    " [energyAvailable=${spawn.room.energyAvailable}," +
                    " body=${body}," +
                    " cost=${getBodyCost(body)}]")
            else -> console.log("unhandled error code $code")
        }
        return code

    }
}

fun Creep.findEnergyStructures(): List<EnergyContainer> {
    return room.find(FIND_STRUCTURES)
            .filter {
                it.structureType == STRUCTURE_EXTENSION ||
                        it.structureType == STRUCTURE_SPAWN
            }
            .mapNotNull {
                @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                it as? EnergyContainer
            }
}

fun Creep.findNearestEnergyStructure(): Structure? {
    return findEnergyStructures()
            .map { it as Structure }
            .minBy { pos.getRangeTo(it) }
}

fun Creep.moveToAndTransferEnergy(target: Structure?) {
    if (target != null) {
        if (transfer(target, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
            moveTo(target.pos)
        } else if (transfer(target, RESOURCE_ENERGY) == ERR_NOT_ENOUGH_ENERGY) {
            memory.working = false
        }
    }
}

fun Creep.moveToAndWithdrawEnergy(fromStructure: Structure?) {
    if (fromStructure != null) {
        if (withdraw(fromStructure, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
            moveTo(fromStructure.pos)
        } else if (withdraw(fromStructure, RESOURCE_ENERGY) == ERR_NOT_ENOUGH_ENERGY) {
            memory.working = false
        }
    }
}
