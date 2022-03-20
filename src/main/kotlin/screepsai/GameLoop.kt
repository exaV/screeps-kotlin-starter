package screepsai


import screeps.api.*
import screeps.api.structures.StructureTower
import screepsai.roles.*


fun getCreepsByRole(): Map<Room, Map<CreepRole, List<Creep>>> {

    val creepsByRoomAndRole = HashMap<Room, Map<CreepRole, List<Creep>>>()
    Game.creeps.values.groupBy { it.room }.forEach {
        creepsByRoomAndRole[it.key] = it.value.groupBy { creep -> creep.getRole() }
    }

    return creepsByRoomAndRole
}

// Desired number of creeps in each role
val roleMemberCount = mapOf(
    CreepRole.HARVESTER to 2,
    CreepRole.TRANSPORTER to 2,
    CreepRole.MAINTAINER to 2,
    CreepRole.UPGRADER to 6,
    CreepRole.BUILDER to 1
)

fun runRoom(room: Room, creepsByRole: Map<CreepRole, List<Creep>>) {
    room.find(FIND_MY_STRUCTURES).filter { it.structureType == STRUCTURE_TOWER }.map { it as StructureTower }
        .forEach { runTower(it) }

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
                spawnCreeps(creepRole, room)
            }
        }

        // Set up role object and run role for each creep
        val creeps = creepsByRole[roleCount.key] ?: continue
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
}


fun gameLoop() {
    val startCpu = Game.cpu.tickLimit
    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    val creepsByRoomAndRole = getCreepsByRole()

    for (roomToRole in creepsByRoomAndRole) {
        val roomStartCpu = Game.cpu.tickLimit
        val room = roomToRole.key
        val creepsByRole = roomToRole.value

        runRoom(room, creepsByRole)
        console.log("${room} used ${roomStartCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
    }

    console.log("Used ${startCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
}
