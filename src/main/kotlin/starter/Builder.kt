package starter

import types.base.global.ERR_NOT_IN_RANGE
import types.base.prototypes.Creep
import types.base.prototypes.findConstructionSites
import types.base.prototypes.findEnergy

object Builder {
    fun run(creep: Creep) {

        if (creep.memory.building == true && creep.carry.energy == 0) {
            creep.memory.building = false;
            creep.say("🔄 harvest")
        }
        if (creep.memory.building != true && creep.carry.energy == creep.carryCapacity) {
            creep.memory.building = true;
            creep.say("🚧 build");
        }

        if (creep.memory.building == true) {
            val targets = creep.room.findConstructionSites()
            if (targets.isNotEmpty()) {
                if (creep.build(targets[0]) == ERR_NOT_IN_RANGE) {
                    creep.moveTo(targets[0].pos);
                }
            }
        } else {
            val sources = creep.room.findEnergy()
            if (creep.harvest(sources[0]) == ERR_NOT_IN_RANGE) {
                creep.moveTo(sources[0].pos)
            }
        }
    }
}
