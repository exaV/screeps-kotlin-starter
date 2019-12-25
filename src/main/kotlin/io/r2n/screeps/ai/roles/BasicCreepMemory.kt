package io.r2n.screeps.ai.roles

import screeps.api.CreepMemory
import screeps.utils.memory.memory


var CreepMemory.working: Boolean by memory { false }
var CreepMemory.pause: Int by memory { 0 }
var CreepMemory.role: Enum<Role> by memory(Role.UNASSIGNED)
