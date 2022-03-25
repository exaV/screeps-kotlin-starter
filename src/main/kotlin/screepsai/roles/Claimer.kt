package screepsai.roles

import screeps.api.*
import screeps.api.structures.StructureController
import screeps.utils.unsafe.jsObject

class Claimer(creep: Creep) : Role(creep) {

    private val targetFlag: Flag? = Game.flags["NextRoom"]
    private val targetController: StructureController? = targetFlag?.room?.controller

    override fun run() {
        claimRoom()
    }

    private fun getRoomCostMatrix(roomName: String): CostMatrix {
        val costMatrix = PathFinder.CostMatrix()
        val room = Game.rooms[roomName]
        if (room == null) {
            debug("${roomName} not known, returning default cost matrix")
            return costMatrix
        }

        for (structure in room.find(FIND_STRUCTURES)) {
            if (structure.structureType == STRUCTURE_ROAD) {
                costMatrix.set(structure.pos.x, structure.pos.y, 1)
            }
            else if (structure.structureType == STRUCTURE_RAMPART && structure.my) {
                debug("${structure} is a rampart")
            }
            else if (structure.structureType != STRUCTURE_CONTAINER) {
                // Can't walk through non-walkable buildings
                debug("${structure} is not walkable")
                costMatrix.set(structure.pos.x, structure.pos.y, 750)
            }
        }

        return costMatrix
    }

    private fun getPathToTarget(target: RoomPosition): Array<RoomPosition> {

        val searchResult = PathFinder.search(creep.pos, target, options = jsObject {
            plainCost = 1
            swampCost = 5
            roomCallback = this@Claimer::getRoomCostMatrix
        })

        if (searchResult.incomplete) {
            error("Could not find path to ${target}")
        }

        info("Target controller is ${searchResult.path.size} tiles away")
        for (position in searchResult.path.withIndex()) {
            debug("Position ${position.index}: ${position.value}")
            if (position.value.roomName != creep.room.name) {
                debug("End of positions in current room, there are  ${searchResult.path.size - position.index} positions left in path")
                break
            }
        }

        return searchResult.path
    }

    private fun moveToFlag() {
        if (targetFlag == null) {
            error("No target room flag located!")
            return
        }

        val path = getPathToTarget(targetFlag.pos)
        creep.move(creep.pos.getDirectionTo(path[0]))
    }

    private fun claimRoom() {
        if (targetController == null || targetController.room != creep.room) {
            warning("Not in room with controller, navigating to ${targetFlag} instead")
            moveToFlag()
            return
        }

        if (targetController.my) {
            info("${targetController} already claimed!")
            setupRoom(targetController.room)
            return
        }

        when (val code = creep.claimController(targetController)) {
            OK               -> info("${targetController.room} claimed!")
            ERR_NOT_IN_RANGE -> creep.moveTo(targetController)
            else             -> error("Claiming ${targetController.room} failed: ${code}")
        }
    }

    private fun setupRoom(room: Room) {
        val spawnPos = targetFlag?.pos ?: return error("Could not find flag to create initial spawn")

        val code = room.createConstructionSite(spawnPos, STRUCTURE_SPAWN)

        if (code == OK) {
            info("${room} successfully initialized, construction may begin!")
            targetFlag.remove()
        }
    }
}