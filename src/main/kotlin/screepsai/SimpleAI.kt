package screepsai


import screeps.api.*
import screeps.api.structures.StructureSpawn
import screepsai.roles.*


fun getCreepsByRole(): Map<CreepRole, List<Creep>> {
    val creepsByRole = Game.creeps.values.groupBy { it.getRole() }.toMutableMap()

    for (creepRole in CreepRole.values()) {
        if (!creepsByRole.containsKey(creepRole)) {
            creepsByRole[creepRole] = listOf()
        }
    }

    return creepsByRole
}

// Desired number of creeps in each role
val roleMemberCount = mapOf(
    CreepRole.HARVESTER to 3,
    CreepRole.UPGRADER to 6
)


fun gameLoop() {
    val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return

    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    for (entry in getCreepsByRole()) {
        console.log("${entry.key}: ${entry.value.size}")
        val creepRole = entry.key
        val creeps = entry.value
        val creepCount = entry.value.size
        // Spawn more creeps if we are not at the desired volume
        if (creepCount < roleMemberCount[entry.key]) {
            spawnCreeps(creepRole, mainSpawn)
        }

        // Set up role object and run role for each creep
        for (creep in creeps) {
            Role.build(creepRole, creep).run()
        }
    }
}
