package screepsai


import screeps.api.*
import screeps.api.structures.StructureSpawn

import screepsai.roles.*

fun gameLoop() {
    val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return

    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    //make sure we have at least some creeps
    spawnCreeps(CreepRole.HARVESTER, mainSpawn)

    for ((_, creep) in Game.creeps) {
        val role = when (creep.getRole()){
            CreepRole.HARVESTER-> Harvester(creep)
            else -> Harvester(creep)
        }

        role.run()
    }
}
