package starter

import types.base.global.ERR_NOT_IN_RANGE
import types.base.prototypes.Creep
import types.base.prototypes.Room
import types.base.prototypes.findConstructionSites
import types.base.prototypes.findEnergy

fun builder(creep: Creep, assignedRoom: Room = creep.room) {
    with(creep) {
        if (memory.building && carry.energy == 0) {
            memory.building = false
            say("🔄 harvest")
        }
        if (!memory.building && carry.energy == carryCapacity) {
            memory.building = true
            say("🚧 build")
        }

        if (memory.building) {
            val targets = assignedRoom.findConstructionSites()
            if (targets.isNotEmpty()) {
                if (build(targets[0]) == ERR_NOT_IN_RANGE) {
                    moveTo(targets[0].pos)
                }
            }
        } else {
            val sources = room.findEnergy()
            if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
                moveTo(sources[0].pos)
            }
        }
    }


}

