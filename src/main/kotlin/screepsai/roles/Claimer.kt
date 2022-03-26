package screepsai.roles

import screeps.api.*
import screeps.api.structures.StructureController
import screeps.utils.memory.memory
import screepsai.utils.*

var FlagMemory.spawnerId: String? by memory()

class Claimer(creep: Creep) : Role(creep) {

    private val targetFlag: Flag? = Game.flags["NextRoom"]
    private val targetController: StructureController? = targetFlag?.room?.controller

    override fun run() {
        claimRoom()
    }

    private fun moveToFlag() {
        if (targetFlag == null) {
            error("No target room flag located!")
            return
        }

        val path = getPathToTarget(creep.pos, targetFlag.pos)
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

        val spawner = room.find(FIND_CONSTRUCTION_SITES).first()
        targetFlag.memory.spawnerId = spawner.id

        if (code == OK) {
            info("${room} successfully initialized, construction may begin!")
        }
    }
}