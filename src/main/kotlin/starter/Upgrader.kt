package starter

import types.base.global.ERR_NOT_IN_RANGE
import types.base.prototypes.Creep
import types.base.prototypes.findEnergy

object Upgrader {

    fun run(creep: Creep) {
        if (creep.carry.energy == 0) {
            val sources = creep.room.findEnergy()
            if (creep.harvest(sources[0]) == ERR_NOT_IN_RANGE) {
                creep.moveTo(sources[0].pos);
            }
        } else {
            creep.room.controller?.let {
                if (creep.upgradeController(it) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(it.pos);
                }
            }
        }
    }
}
