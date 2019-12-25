package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.api.structures.Structure
import screeps.api.structures.StructureController
import screeps.utils.unsafe.jsObject

object Upgrader : EmployedCreep {
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { role = Role.RUNNER }

    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        //TODO make dynameic
        return arrayOf(WORK, CARRY, MOVE, MOVE)
    }
}

fun Creep.upgrade(controller: StructureController) {
    if (carry.energy < carryCapacity && !memory.working) {
        val energySource = findEnergyStructures()
                .filter { it.energy > 0 }
                .map { it as Structure }
                .minBy { pos.getRangeTo(it) }
        moveToAndWithdrawEnergy(energySource)
    } else {
        memory.working = true
        if (upgradeController(controller) == ERR_NOT_IN_RANGE) {
            moveTo(controller.pos)
        } else if (upgradeController(controller) == ERR_NOT_ENOUGH_ENERGY) {
            memory.working = false
        }
    }
}

