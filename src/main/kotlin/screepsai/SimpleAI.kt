package screepsai


import screeps.api.*
import screeps.api.structures.StructureSpawn

fun gameLoop() {
    val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return

    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    //make sure we have at least some creeps
    spawnCreeps(ScreepRole.HARVESTER, mainSpawn)

    for ((_, creep) in Game.creeps) {
        val role = Harvester(creep)

        role.run()
    }
}
