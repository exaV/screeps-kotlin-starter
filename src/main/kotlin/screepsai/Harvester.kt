package screepsai

import screeps.api.*

class Harvester(creep: Creep) : Role(creep) {
    override fun run() {
        debug("State = ${state.name}")
        when (state) {
            CreepState.GET_ENERGY -> {
                getEnergy()
            }
            CreepState.DO_WORK -> {
                storeEnergy()
            }
        }
    }

    private fun getEnergy() {
        val energySources = creep.room.find(FIND_SOURCES)
        val energySource = energySources.first()

        val status = creep.harvest(energySource)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(energySource.pos.x, energySource.pos.y)
        } else if (status != OK) {
            error("Gather failed with code $status")
        }

        if (creep.store.getFreeCapacity() == 0) {
            info("Energy full", say = true)
            state = CreepState.DO_WORK
        }
    }

    private fun storeEnergy() {
        val controller = creep.room.controller

        if (controller == null) {
            error("No controller!", say = true)
            return
        }

        val status = creep.upgradeController(controller)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(controller)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        } else if (status != OK) {
            error("Upgrade failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
        }
    }
}
