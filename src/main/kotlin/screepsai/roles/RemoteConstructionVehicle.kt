package screepsai.roles

import screeps.api.*
import screeps.utils.memory.memory
import screepsai.utils.getPathToTarget
import kotlin.math.abs

var FlagMemory.complete: Boolean by memory { false }

class RemoteConstructionVehicle(creep: Creep) : Role(creep) {
    private val targetFlag: Flag? = Game.flags["NextRoom"]

    override fun run() {
        if (targetFlag == null) {
            warning("No target to work with!")
        }
        else {
            if (targetFlag.room?.find(FIND_MY_SPAWNS)?.firstOrNull() != null) {
                info("Spawn construction completed!")
                targetFlag.memory.complete = true
            }
            // Move to target room if not in room
            if (creep.room != targetFlag.room) {
                creep.move(creep.pos.getDirectionTo(getPathToTarget(creep.pos, targetFlag.pos)[0]))
                return
            }
        }

        if (state == CreepState.GET_ENERGY) {
            getEnergy()
        }
        else if (state == CreepState.DO_WORK) {
            buildBuildings()
        }
    }

    private fun getEnergy() {
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            debug("No storage in room, going to gather instead")
            gatherEnergy()
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

    private fun gatherEnergy() {
        val energySource =
            creep.room.find(FIND_SOURCES).sortedBy { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
                .firstOrNull { it.energy > 0 } ?: return error("No energy available!")

        val code = creep.harvest(energySource)

        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(energySource)
        }
        else if (code != OK) {
            error("Gather failed with code ${code}")
        }

        if ((creep.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            state = CreepState.DO_WORK
        }
    }

    private fun buildBuildings() {
        val constructionSite = findConstructionSite()

        if (constructionSite == null) {
            warning("No available construction site!")
            depositEnergy()
            return
        }

        val status = creep.build(constructionSite)

        if (status == ERR_NOT_IN_RANGE) {
            if (creep.room != constructionSite.room) {
                creep.move(creep.pos.getDirectionTo(getPathToTarget(creep.pos, constructionSite.pos)[0]))
            }
            else {
                creep.moveTo(constructionSite)
            }
        }
        else if (status == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        }
        else if (status != OK) {
            error("Build failed with code $status", say = true)
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }

    private fun findConstructionSite(): ConstructionSite? {
        return if (targetFlag != null) {
            targetFlag.room?.find(FIND_CONSTRUCTION_SITES)?.firstOrNull()
        }
        else {
            creep.room.find(FIND_CONSTRUCTION_SITES)
                .minByOrNull { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
        }
    }

    private fun depositEnergy() {
        val spawner = creep.room.find(FIND_MY_SPAWNS).firstOrNull() ?: return error("No spawner to deposit energy into")

        val code = creep.transfer(spawner, RESOURCE_ENERGY)

        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(spawner)
        }
        else if (code == ERR_NOT_ENOUGH_ENERGY) {
            info("Out of energy", say = true)
            state = CreepState.GET_ENERGY
            return
        }
        else if (code != OK) {
            error("Transfer failed with code ${code}", say = true)
        }

        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) <= 0) {
            state = CreepState.GET_ENERGY
        }
    }

}