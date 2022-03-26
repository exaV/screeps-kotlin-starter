package screepsai

import screeps.api.*
import screeps.api.structures.StructureTower
import kotlin.math.abs

fun repairWalls(tower: StructureTower) {
    if (tower.store.getUsedCapacity(RESOURCE_ENERGY) < 500) {
        console.log("${tower} not repairing to conserve energy")
        return
    }
    val walls = tower.room.find(FIND_STRUCTURES)
        .filter { it.structureType == STRUCTURE_WALL || it.structureType == STRUCTURE_RAMPART }
    var wall = walls.first()
    var maxWallHp = wall.hits
    var minWallHp = wall.hitsMax
    for (it in walls) {
        if (it.hits > maxWallHp) {
            maxWallHp = it.hits
        }
        if (it.hits < minWallHp && it.hits != it.hitsMax) {
            minWallHp = it.hits
            wall = it
        }
    }

    if (wall.hits.toFloat() / maxWallHp.toFloat() > 0.9) {
        console.log("${tower} not repairing since all walls are roughly same hp")
    }

    if (wall.structureType == STRUCTURE_RAMPART && wall.hits > 500000) {
        console.log("${tower} not repairing since ramparts are healthy")
        return
    }

    when (val code = tower.repair(wall)) {
        OK                    -> return
        ERR_NOT_ENOUGH_ENERGY -> console.log("${tower} failed to repair walls: out of energy")
        else                  -> console.log("${tower} failed to repair walls: ${code}")
    }
}

fun runTower(tower: StructureTower) {
    // Shoot bad guys
    val badGuy =
        tower.room.find(FIND_HOSTILE_CREEPS).minByOrNull { abs(it.pos.x - tower.pos.x) + abs(it.pos.y - tower.pos.y) }
            ?: return repairWalls(tower)


    when (val code = tower.attack(badGuy)) {
        OK                    -> console.log("${tower} attacking ${badGuy}")
        ERR_NOT_ENOUGH_ENERGY -> console.log("${tower} failed to attack ${badGuy}: out of energy")
        else                  -> console.log("${tower} failed to attack ${badGuy}: ${code}")
    }
}