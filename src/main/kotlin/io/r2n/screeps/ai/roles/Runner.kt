package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.api.structures.Structure
import screeps.utils.unsafe.jsObject

object Runner : EmployedCreep {
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { role = Role.RUNNER }
    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        //TODO make dynamic
        return arrayOf(CARRY, CARRY, MOVE, MOVE)
    }


}

fun Creep.run() {
    if (carry.energy < carryCapacity && !memory.working) {
        val energy = room.find(FIND_DROPPED_RESOURCES)
                .filter { it.resourceType == RESOURCE_ENERGY }
                .maxBy { it.amount }
        if (energy != null && pickup(energy) == ERR_NOT_IN_RANGE) {
            moveTo(energy.pos)
        }
    } else {
        memory.working = true
        val target: Structure? = findEnergyStructures()
                .filter { it.energy < it.energyCapacity }
                .mapNotNull { it as? Structure }
                .minBy { pos.getRangeTo(it) }
        moveToAndTransferEnergy(target)
    }
}