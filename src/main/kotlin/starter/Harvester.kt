package starter

import types.base.global.ERR_NOT_IN_RANGE
import types.base.global.RESOURCE_ENERGY
import types.base.global.STRUCTURE_EXTENSION
import types.base.global.STRUCTURE_SPAWN
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.findEnergy
import types.base.prototypes.findStructures
import types.base.prototypes.structures.StructureSpawn


fun harvester(creep: Creep, fromRoom: Room = creep.room, toRoom: Room = creep.room) {
    if (creep.carry.energy < creep.carryCapacity) {
        val sources = fromRoom.findEnergy()
        if (creep.harvest(sources[0]) == ERR_NOT_IN_RANGE) {
            creep.moveTo(sources[0].pos)
        }
    } else {
        val targets = toRoom.findStructures()
                .filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
                .map { (it as StructureSpawn) }
                .filter { it.energy < it.energyCapacity }

        if (targets.isNotEmpty()) {
            if (creep.transfer(targets[0], RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
                creep.moveTo(targets[0].pos)
            }
        }
    }
}

