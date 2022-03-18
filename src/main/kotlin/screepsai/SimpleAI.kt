package screepsai


import screeps.api.*
import screeps.api.structures.StructureSpawn
import screepsai.roles.*


fun getCreepsByRole(): Map<CreepRole, List<Creep>> {
    return Game.creeps.values.groupBy { it.getRole() }
}

// Desired number of creeps in each role
val roleMemberCount = mapOf(
    CreepRole.HARVESTER to 2,
    CreepRole.TRANSPORTER to 2,
    CreepRole.MAINTAINER to 2,
    CreepRole.UPGRADER to 6,
    CreepRole.BUILDER to 1
)


fun gameLoop() {
    val startCpu = Game.cpu.tickLimit
    val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return

    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    val creepsByRole = getCreepsByRole()
    for (roleCount in roleMemberCount) {
        val creepRole = roleCount.key
        val creepCount = creepsByRole[roleCount.key]?.size ?: 0
        console.log("${creepRole}: ${creepCount}")
        // Spawn more creeps if we are not at the desired volume
        if (creepCount < roleCount.value) {
            // Don't spawn an extra creep in a role until every other role has same amount or reached the max
            var spawn = true
            for (creeps_in_role in creepsByRole) {
                // Ignore same role we're trying to spawn
                if (creepRole == creeps_in_role.key) {
                    continue
                }

                // Ignore any roles already maxed out
                if (creeps_in_role.value.size >= roleMemberCount[creeps_in_role.key]) {
                    continue
                }

                // Detect when we have same or more than another role
                if (creepCount > creeps_in_role.value.count()) {
                    console.log("Not spawning ${creepRole} due to not enough ${creeps_in_role.key}")
                    spawn = false
                }
            }
            if (spawn) {
                spawnCreeps(creepRole, mainSpawn)
            }
        }

        // Set up role object and run role for each creep
        val creeps = creepsByRole[roleCount.key] ?: listOf()
        for (creep in creeps) {
            try {
                Role.build(creepRole, creep).run()
            }
            catch (error: IllegalArgumentException) {
                if (creep.body.any { it.type == WORK }) {
                    creep.setRole(CreepRole.UPGRADER)
                }
                else {
                    creep.setRole(CreepRole.TRANSPORTER)
                }
            }
        }
    }

    console.log("Used ${startCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
}
