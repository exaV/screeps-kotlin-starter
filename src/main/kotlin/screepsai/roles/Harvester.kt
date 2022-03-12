package screepsai.roles

import screeps.api.*
import screeps.utils.memory.memory

var CreepMemory.energySource: String? by memory()
var RoomMemory.energySourceAssignments: Array<String?> by memory { arrayOf(null, null) }

class Harvester(creep: Creep) : Role(creep) {
    private var assignedSource: Source? = Game.getObjectById(creep.memory.energySource)
        set(value) {
            creep.memory.energySource = value?.id
            field = value
        }

    override fun run() {
        harvestEnergy()
    }

    private fun findEnergySource(): Source? {
        if (assignedSource != null) {
            return assignedSource as Source
        }

        val energySources = creep.room.find(FIND_SOURCES).sortedBy { it.id }

        if (energySources.isEmpty()) {
            error("There are no sources in the room!")
            return null
        }

        for (energySource in energySources.withIndex()) {
            val otherCreep = Game.getObjectById<Creep>(creep.room.memory.energySourceAssignments[energySource.index])
            if (otherCreep == null) {
                info("Dead creep found, re-assigning energy source")
                assignedSource = energySource.value
                creep.room.memory.energySourceAssignments[energySource.index] = creep.id
                return assignedSource
            }
            else {
                debug("Energy source already taken, checking other source(s)")
            }
        }

        error("All sources in room are taken")
        return null
    }

    private fun harvestEnergy() {
        val energySource = findEnergySource()
        if (energySource == null) {
            error("No sources found to gather from!")
            return
        }

        val status = creep.harvest(energySource)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(energySource.pos.x, energySource.pos.y)
        }
        else if (status != OK) {
            error("Gather failed with code $status")
        }
    }
}
