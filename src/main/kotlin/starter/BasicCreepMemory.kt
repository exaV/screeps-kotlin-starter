package starter

import starter.memoryOrDefault
import types.base.global.CreepMemory
import types.extensions.memory.memoryOrDefault

var CreepMemory.building: Boolean by memoryOrDefault(false)
var CreepMemory.pause: Int by memoryOrDefault(0)
var CreepMemory.role: Role by memoryOrDefault(Role.UNASSIGNED)
