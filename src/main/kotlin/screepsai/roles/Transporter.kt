package screepsai.roles

import screeps.api.*
import screeps.api.structures.*
import kotlin.math.abs

val FILLABLE_STRUCTURES = setOf(
    STRUCTURE_SPAWN,
    STRUCTURE_EXTENSION,
    STRUCTURE_TOWER,
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
        val fillableStructures = creep.room.find(FIND_MY_STRUCTURES).filter {
            it.structureType in FILLABLE_STRUCTURES
        }.map { it as StoreOwner }.filter {
            (it.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0) > 0
        }.groupBy {
            when (it.unsafeCast<Structure>().structureType) {
                // TODO: Determine priority level more intelligently
                STRUCTURE_SPAWN     -> 1
                STRUCTURE_EXTENSION -> 2
                STRUCTURE_TOWER     -> 3
                STRUCTURE_STORAGE   -> 4
                else                -> 5
            }
        }

        return fillableStructures.getOrElse(fillableStructures.keys.minOrNull() ?: 2) { emptyList() }
    }

    private fun storeEnergy() {
        val fillableStructures = findFillableStructures()

        if (fillableStructures.isEmpty()) {
            info("No structures to fill with energy")
            return
        }

        val structureType = (fillableStructures[0] as Structure).structureType
        val fillableStructure = if (structureType == STRUCTURE_TOWER) {
            fillableStructures.maxByOrNull { it.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0 }
        }
        else {
            fillableStructures.minByOrNull { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
        }

        if (fillableStructure == null) {
            error("No structures to fill with energy!")
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

        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }
}