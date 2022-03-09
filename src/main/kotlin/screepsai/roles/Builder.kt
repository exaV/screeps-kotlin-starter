package screepsai.roles

import screeps.api.*

val MAINTENANCE_REQUIRED_BUILDING_TYPES = setOf(
    STRUCTURE_ROAD,
    STRUCTURE_STORAGE
)

class Builder(creep: Creep) : Role(creep) {
    override fun run() {
        when (state) {
            CreepState.GET_ENERGY -> {
                getEnergy()
            }
            CreepState.DO_WORK -> {
                buildBuildings()
            }
        }
    }

    private fun getEnergy() {
        pickupEnergy()

        if (creep.store.getFreeCapacity() == 0) {
            info("Energy full", say = true)
            state = CreepState.DO_WORK
        }
    }

    private fun buildBuildings() {
        val constructionSite = creep.pos.findClosestByPath(FIND_CONSTRUCTION_SITES)

        if (constructionSite == null) {
            debug("No available construction sites!")
            // Fall back to repairing buildings if there are none that need to be built
            repairBuildings()
            return
        }

        val status = creep.build(constructionSite)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(constructionSite)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        } else if (status != OK) {
            error("Build failed with code $status", say = true)
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
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
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        } else if (status != OK) {
            error("Repair failed with code $status", say = true)
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}