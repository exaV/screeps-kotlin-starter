package screepsai.roles

import screeps.api.*
import screeps.api.structures.*

val FILLABLE_STRUCTURES = setOf(
    STRUCTURE_SPAWN,
    STRUCTURE_EXTENSION,
    STRUCTURE_STORAGE
)

class Transporter(creep: Creep) : Role(creep) {
    override fun run() {
        when (state) {
            CreepState.GET_ENERGY -> {
                getEnergy()
            }
            CreepState.DO_WORK    -> {
                storeEnergy()
            }
        }
    }

    private fun getEnergy() {
        val status = pickupEnergy()

        if (status == ERR_NOT_FOUND) {
            val storage = creep.room.storage ?: return
            warning("No energy to pick up, gathering from storage")
            val code = creep.withdraw(storage, RESOURCE_ENERGY)
            if (code == ERR_NOT_IN_RANGE) {
                creep.moveTo(storage.pos.x, storage.pos.y)
            }
            else if (status != OK) {
                error("Storage withdraw failed with code $status")
            }
        }

        if (creep.store.getFreeCapacity() == 0) {
            info("Energy full", say = true)
            state = CreepState.DO_WORK
        }
    }

    private fun findFillableStructures(): List<StoreOwner> {
        return creep.room.find(FIND_MY_STRUCTURES).filter {
            it.structureType in FILLABLE_STRUCTURES
        }.map { it as StoreOwner }.filter {
            (it.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0) > 0
        }.sortedBy {
            when (it.unsafeCast<Structure>().structureType) {
                STRUCTURE_SPAWN     -> 1
                STRUCTURE_EXTENSION -> 2
                STRUCTURE_STORAGE   -> 3
                else                -> 4
            }
        }
    }

    private fun storeEnergy() {
        val fillableStructure = findFillableStructures().firstOrNull()
        if (fillableStructure == null) {
            info("No structures to fill with energy")
            return
        }

        val status = creep.transfer(fillableStructure, RESOURCE_ENERGY)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(fillableStructure)
        }
        else if (status == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        }
        else if (status != OK) {
            error("Transfer failed with code $status", say = true)
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}