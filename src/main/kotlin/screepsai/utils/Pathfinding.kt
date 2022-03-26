package screepsai.utils

import screeps.api.*
import screeps.utils.unsafe.jsObject

fun getRoomCostMatrix(roomName: String): CostMatrix {
    val costMatrix = PathFinder.CostMatrix()
    val room = Game.rooms[roomName]
    if (room == null) {
        console.log("${roomName} not known, returning default cost matrix")
        return costMatrix
    }

    for (structure in room.find(FIND_STRUCTURES)) {
        if (structure.structureType == STRUCTURE_ROAD) {
            costMatrix.set(structure.pos.x, structure.pos.y, 1)
        }
        else if (structure.structureType == STRUCTURE_RAMPART && structure.my) {
            console.log("${structure} is a rampart")
        }
        else if (structure.structureType != STRUCTURE_CONTAINER) {
            // Can't walk through non-walkable buildings
            console.log("${structure} is not walkable")
            costMatrix.set(structure.pos.x, structure.pos.y, 750)
        }
    }

    return costMatrix
}

fun getPathToTarget(start: RoomPosition, target: RoomPosition): Array<RoomPosition> {

    val searchResult = PathFinder.search(start, target, options = jsObject {
        plainCost = 1
        swampCost = 5
        roomCallback = ::getRoomCostMatrix
    })

    if (searchResult.incomplete) {
        console.log("Could not find complete path from ${start} to ${target}")
    }

    console.log("Target is ${searchResult.path.size} tiles away")
    for (position in searchResult.path.withIndex()) {
        console.log("Position ${position.index}: ${position.value}")
        if (position.index > 10) {
            console.log("Truncating position logs, there are  ${searchResult.path.size - position.index} positions left in path")
            break
        }
    }

    return searchResult.path
}