package screepsai.roles

import screeps.api.*

class Upgrader(creep: Creep) : Role(creep) {
    override fun run() {
        when (state) {
            CreepState.GET_ENERGY -> {
                getEnergy()
            }
            CreepState.DO_WORK -> {
                upgradeController()
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

    private fun upgradeController() {
        val controller = creep.room.controller

        if (controller == null) {
            error("No controller!")
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
            error("Upgrade failed with code $status", say = true)
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}