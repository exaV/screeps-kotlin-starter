package screepsai


import screeps.api.*
import screeps.api.structures.StructureTower
import screeps.utils.unsafe.delete
import screepsai.roles.*

fun getCreepsByRole(): Map<CreepRole, Map<Room, List<Creep>>> {

    val creepsByRoleAndRoom = HashMap<CreepRole, Map<Room, List<Creep>>>()
    Game.creeps.values.groupBy { it.getRole() }.forEach {
        creepsByRoleAndRoom[it.key] = it.value.groupBy { creep -> creep.room }
    }

    return creepsByRoleAndRoom
}

// Desired number of creeps in each role
val roleMemberCount = mapOf(
    CreepRole.HARVESTER to 2,
    CreepRole.TRANSPORTER to 2,
    CreepRole.MAINTAINER to 2,
    CreepRole.UPGRADER to 8,
    CreepRole.BUILDER to 1
)

fun runRoom(room: Room, creepsByRoleAndRoom: Map<CreepRole, Map<Room, List<Creep>>>) {
    room.find(FIND_MY_STRUCTURES).filter { it.structureType == STRUCTURE_TOWER }.map { it as StructureTower }
        .forEach { runTower(it) }

    var minCreepsInUnfilledRole = 1000
    val creepsByRole = HashMap<CreepRole, List<Creep>>()

    for (role in CreepRole.values()) {
        val creepsByRoom = creepsByRoleAndRoom[role] ?: hashMapOf()
        val creeps = creepsByRoom[room] ?: emptyList()
        creepsByRole[role] = creeps

        if (creeps.size < minCreepsInUnfilledRole && creeps.size < (roleMemberCount[role] ?: 0)) {
            console.log("${role} only has ${creeps.size}")
            minCreepsInUnfilledRole = creeps.size
        }
    }

    if (minCreepsInUnfilledRole != 1000) {
        console.log("Least populated unfilled role in ${room} only has ${minCreepsInUnfilledRole} creeps")
    }
    else {
        console.log("All roles filled in ${room}")
    }

    for (record in creepsByRole) {
        val creepRole = record.key
        val creeps = record.value
        val creepCount = creeps.size
        val maxCreepsInRole = roleMemberCount[creepRole] ?: 0
        console.log("${room} ${creepRole}: ${creepCount}/${maxCreepsInRole}")
        // Spawn more creeps if we are not at the desired volume
        if (creepCount < maxCreepsInRole) {
            if (creepCount <= minCreepsInUnfilledRole) {
                spawnNewCreep(creepRole, room)
            }
            else {
                console.log("Not spawning new ${creepRole} since there are ${creeps.size} and another role only has ${minCreepsInUnfilledRole}")
            }
        }

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
            catch (error: Exception) {
                console.log("${creep.name} failed to run due to error: ${error}")
            }
        }
    }
}

fun claimNewRooms(creepsByRoomAndRole: Map<CreepRole, Map<Room, List<Creep>>>) {
    val nextRoomFlag = Game.flags["NextRoom"] ?: return console.log("No NextRoom flag, skipping claim room routine")

    if (nextRoomFlag.memory.complete) {
        console.log("Current room claim target has been successfully claimed")
        if (Memory.flags?.get(nextRoomFlag.name) != null) {
            delete(Memory.flags!![nextRoomFlag.name])
        }
        nextRoomFlag.remove()
        return
    }
    if (nextRoomFlag.memory.spawnerId == null) {
        creepsByRoomAndRole[CreepRole.CLAIMER]?.flatMap { it.value }?.firstOrNull()
            ?: spawnCrossRoomCreep(
                CreepRole.CLAIMER, nextRoomFlag
            )
            ?: return console.log("No claimer creep could be located or created")
    }
    else {
        val builders =
            creepsByRoomAndRole[CreepRole.REMOTE_CONSTRUCTION]?.flatMap { it.value }?.toMutableList() ?: mutableListOf()

        if (builders.size < 2) {
            spawnCrossRoomCreep(
                CreepRole.REMOTE_CONSTRUCTION, nextRoomFlag
            ) ?: return console.log("No RCV could be located or created")
        }
    }
}


fun gameLoop() {
    val startCpu = Game.cpu.tickLimit
    console.log("${startCpu} CPU available on tick ${Game.time}")
    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    val creepsByRoomAndRole = getCreepsByRole()

    for (room in Game.rooms.values) {
        val roomStartCpu = Game.cpu.tickLimit

        runRoom(room, creepsByRoomAndRole)
        console.log("${room} used ${roomStartCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
    }

    claimNewRooms(creepsByRoomAndRole)

    console.log("Used ${startCpu - Game.cpu.tickLimit} CPU on tick ${Game.time}")
}
