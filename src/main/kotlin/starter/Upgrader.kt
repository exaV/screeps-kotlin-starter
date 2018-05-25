package starter

import types.base.global.ERR_NOT_IN_RANGE
import types.base.prototypes.Creep
import types.base.prototypes.findEnergy
import types.base.prototypes.structures.StructureController


fun upgrader(creep: Creep, controller: StructureController) {
    if (creep.carry.energy == 0) {
        val sources = creep.room.findEnergy()
        if (creep.harvest(sources[0]) == ERR_NOT_IN_RANGE) {
            creep.moveTo(sources[0].pos)
        }
    } else {
        if (creep.upgradeController(controller) == ERR_NOT_IN_RANGE) {
            creep.moveTo(controller.pos)
        }
    }
}

