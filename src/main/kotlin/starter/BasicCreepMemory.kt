package starter

import screeps.api.CreepMemory
import screeps.utils.memory.memory


var CreepMemory.building: Boolean by memory { false }
var CreepMemory.pause: Int by memory { 0 }
var CreepMemory.role by memory(Role.UNASSIGNED)
