package screepsai.roles

import screeps.api.*

class Transporter(creep: Creep) : Role(creep) {
    override fun run() {
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
        pickupEnergy()

        if (creep.store.getFreeCapacity() == 0) {
            info("Energy full", say = true)
            state = CreepState.DO_WORK
        }
    }

    private fun storeEnergy() {
        // TODO: Find other targets like extensions and storage
        val spawn = creep.room.find(FIND_MY_SPAWNS).first()

        val status = creep.transfer(spawn, RESOURCE_ENERGY)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(spawn)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        } else if (status != OK) {
            error("Transfer failed with code $status", say = true)
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}