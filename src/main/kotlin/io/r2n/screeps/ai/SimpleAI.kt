package io.r2n.screeps.ai


import io.r2n.screeps.ai.roles.*
import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete
import screeps.utils.unsafe.jsObject

fun gameLoop() {
    val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return
    if (Memory.sources.isEmpty()) {
        Memory.sources = mainSpawn.room.find(FIND_SOURCES)
    }

    //delete memories of creeps that have passed away
    houseKeeping(Game.creeps)

    //make sure we have at least some creeps
    spawnCreeps(Game.creeps.values, mainSpawn)

    // build a few extensions so we can have 550 energy
    val controller = mainSpawn.room.controller
    if (controller != null && controller.level >= 2) {
        when (controller.room.find(FIND_MY_STRUCTURES).count { it.structureType == STRUCTURE_EXTENSION }) {
            0 -> controller.room.createConstructionSite(29, 27, STRUCTURE_EXTENSION)
            1 -> controller.room.createConstructionSite(28, 27, STRUCTURE_EXTENSION)
            2 -> controller.room.createConstructionSite(27, 27, STRUCTURE_EXTENSION)
            3 -> controller.room.createConstructionSite(26, 27, STRUCTURE_EXTENSION)
            4 -> controller.room.createConstructionSite(25, 27, STRUCTURE_EXTENSION)
            5 -> controller.room.createConstructionSite(24, 27, STRUCTURE_EXTENSION)
            6 -> controller.room.createConstructionSite(23, 27, STRUCTURE_EXTENSION)
        }
    }

    //spawn a big creep if we have plenty of energy
    for ((_, room) in Game.rooms) {
        if (room.energyAvailable >= 550) {
            mainSpawn.spawnCreep(
                    arrayOf(
                            WORK,
                            WORK,
                            WORK,
                            WORK,
                            CARRY,
                            MOVE,
                            MOVE
                    ),
                    "HarvesterBig_${Game.time}",
                    options {
                        memory = jsObject<CreepMemory> {
                            this.role = Role.HARVESTER
                        }
                    }
            )
            console.log("hurray!")
        }
    }

    for ((_, creep) in Game.creeps) {
        when (creep.memory.role) {
            Role.HARVESTER -> creep.harvest()
            Role.MINER -> creep.mine()
            Role.RUNNER -> creep.run()
            Role.BUILDER -> creep.build()
            Role.UPGRADER -> creep.upgrade(mainSpawn.room.controller!!)
            else -> creep.pause()
        }
    }

}

private fun spawnCreeps(creeps: Array<Creep>, spawn: StructureSpawn) {

    val maxHarvesters = 1
    val maxGatherSquad = Memory.sources.size
    val maxUpgraders = 3

    val role: Role = when {
        //First creep should be a harvester, then roll out the big bois
        creeps.none { it.memory.role == Role.MINER } &&
                creeps.count {
                    it.memory.role == Role.HARVESTER
                } <= maxHarvesters -> Role.HARVESTER

        //Setup gather goons first
        creeps.none { it.memory.role == Role.MINER } -> Role.MINER
        creeps.none { it.memory.role == Role.RUNNER } -> Role.RUNNER
        creeps.count { it.memory.role == Role.MINER } <= maxGatherSquad -> Role.MINER
        creeps.count { it.memory.role == Role.RUNNER } <= maxGatherSquad -> Role.RUNNER

        creeps.count { it.memory.role == Role.UPGRADER } <= maxUpgraders -> Role.UPGRADER

        spawn.room.find(FIND_MY_CONSTRUCTION_SITES).isNotEmpty() -> Role.BUILDER

        else -> return
    }


    when (role) {
        Role.MINER -> {
            if (Miner.spawn(spawn, 300) == OK) {
                Memory.lastMinerAssignment = incrementLastMinerAssignment()
            }
        }
        Role.RUNNER -> Runner.spawn(spawn)
        Role.BUILDER -> Builder.spawn(spawn)
        Role.HARVESTER -> Harvester.spawn(spawn)
        Role.UPGRADER -> Upgrader.spawn(spawn)
        else -> console.log("ERROR - can't spawn unassigned creep")
    }
}

fun incrementLastMinerAssignment(): Int {
    console.log("increment last miner")
    return if (Memory.lastMinerAssignment + 1 < Memory.sources.size)
        Memory.lastMinerAssignment + 1
    else
        0

}

fun getBodyCost(bodyParts: Array<BodyPartConstant>): Int {
    return bodyParts.sumBy { BODYPART_COST[it]!! }
}

private fun houseKeeping(creeps: Record<String, Creep>) {
    if (Game.creeps.isEmpty()) return  // this is needed because Memory.creeps is undefined

    for ((creepName, _) in Memory.creeps) {
        if (creeps[creepName] == null) {
            console.log("deleting obsolete memory entry for creep $creepName")
            delete(Memory.creeps[creepName])
        }
    }
}
