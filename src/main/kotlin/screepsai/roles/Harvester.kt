package screepsai.roles

import screeps.api.*

class Harvester(creep: Creep) : Role(creep) {
    override fun run() {
        harvestEnergy()
    }

    private fun harvestEnergy() {
        val energySources = creep.room.find(FIND_SOURCES)
        val energySource = energySources.first()

        val status = creep.harvest(energySource)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(energySource.pos.x, energySource.pos.y)
        } else if (status != OK) {
            error("Gather failed with code $status")
        }
    }
}
