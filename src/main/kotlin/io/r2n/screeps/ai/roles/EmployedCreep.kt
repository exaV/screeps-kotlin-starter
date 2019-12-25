package io.r2n.screeps.ai.roles

import screeps.api.Game
import screeps.api.ScreepsReturnCode
import screeps.api.structures.SpawnOptions
import screeps.api.structures.StructureSpawn

interface EmployedCreep {
    val role: Role
    val options: SpawnOptions
    fun spawn(spawn: StructureSpawn,
              maxEnergy: Int = spawn.room.energyAvailable,
              name: String = "${role.name}_${Game.time}"): ScreepsReturnCode
}