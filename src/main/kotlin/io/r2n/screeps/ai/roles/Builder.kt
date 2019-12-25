package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.utils.unsafe.jsObject


object Builder : EmployedCreep {
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { role = Role.BUILDER }
    }

    override fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        //TODO Implement this
        return arrayOf(WORK, CARRY, MOVE)
    }

}

fun Creep.build(assignedRoom: Room = this.room) {
    if (memory.working && carry.energy == 0) {
        memory.working = false
        say("🔄 harvest")
    }
    if (!memory.working && carry.energy == carryCapacity) {
        memory.working = true
        say("🚧 build")
    }

    if (memory.working) {
        val assignedBuildingSite = pos.findClosestByPath(FIND_CONSTRUCTION_SITES)
        if (assignedBuildingSite != null &&
                build(assignedBuildingSite) == ERR_NOT_IN_RANGE) {
            moveTo(assignedBuildingSite)
        }
    } else {
        moveToAndWithdrawEnergy(findNearestEnergyStructure())
    }
}
