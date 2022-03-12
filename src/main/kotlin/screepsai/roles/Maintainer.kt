package screepsai.roles

import screeps.api.*

class Maintainer(creep: Creep) : Role(creep) {
    override fun run() {
        when (state) {
            CreepState.GET_ENERGY -> {
                getEnergy()
                if (creep.store.getFreeCapacity() == 0) {
                    info("Energy full", say = true)
                    state = CreepState.DO_WORK
                }
            }
            CreepState.DO_WORK    -> {
                repairBuildings()
            }
        }
    }

    private fun getEnergy() {
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            pickupEnergy()
            return
        }

        val code = creep.withdraw(storage, RESOURCE_ENERGY)
        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(storage)
        }
        else if (code != OK) {
            error("Couldn't withdraw from storage due to error: $code")
        }
    }

    private fun repairBuildings() {
        val building =
            creep.room.find(FIND_STRUCTURES).filter { it.structureType in MAINTENANCE_REQUIRED_BUILDING_TYPES }
                .minByOrNull { it.hits.toFloat() / it.hitsMax.toFloat() }

        if (building == null) {
            error("No available buildings to repair!")
            return
        }

        val status = creep.repair(building)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(building)
        }
        else if (status == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        }
        else if (status != OK) {
            error("Repair failed with code $status", say = true)
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}