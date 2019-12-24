package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.StructureController

enum class Role {
    UNASSIGNED,
    HARVESTER,
    BUILDER,
    UPGRADER
}

fun Creep.upgrade(controller: StructureController) {
    if (carry.energy < carryCapacity && !memory.upgrading) {
        val sources = room.find(FIND_SOURCES)
        if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
            moveTo(sources[0].pos)
        }
    } else {
        memory.upgrading = true
        if (upgradeController(controller) == ERR_NOT_IN_RANGE) {
            moveTo(controller.pos)
        } else if (upgradeController(controller) == ERR_NOT_ENOUGH_ENERGY) {
            memory.upgrading = false
        }
    }
}

fun Creep.pause() {
    if (memory.pause < 10) {
        //blink slowly
        if (memory.pause % 3 != 0) say("\uD83D\uDEAC")
        memory.pause++
    } else {
        memory.pause = 0
        memory.role = Role.HARVESTER
    }
}

fun Creep.build(assignedRoom: Room = this.room) {
    if (memory.building && carry.energy == 0) {
        memory.building = false
        say("🔄 harvest")
    }
    if (!memory.building && carry.energy == carryCapacity) {
        memory.building = true
        say("🚧 build")
    }

    if (memory.building) {
        val assignedBuildingSite = pos.findClosestByPath(FIND_CONSTRUCTION_SITES)
        if (assignedBuildingSite != null &&
                build(assignedBuildingSite) == ERR_NOT_IN_RANGE) {
            moveTo(assignedBuildingSite)
        }
    } else {
        val sources = room.find(FIND_SOURCES)
        if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
            moveTo(sources[0].pos)
        }
    }
}

fun Creep.harvest(fromRoom: Room = this.room, toRoom: Room = this.room) {
    if (carry.energy < carryCapacity) {
        val sources = fromRoom.find(FIND_SOURCES)
        if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
            moveTo(sources[0].pos)
        }
    } else {
        val targets = toRoom.find(FIND_MY_STRUCTURES)
                .filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
                .filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }

        if (targets.isNotEmpty() &&
                transfer(targets[0], RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
            moveTo(targets[0].pos)
        }
    }
}