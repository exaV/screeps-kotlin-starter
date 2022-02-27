package screepsai

import screeps.api.*
import screeps.utils.memory.memory


enum class HarvesterState(val value: Int){
    GATHER(0),
    DEPOSIT(1);
}

fun getState(creep: Creep): HarvesterState{

    val memoryState = creep.memory.state
    for (state in HarvesterState.values()){
        if (state.value == memoryState){
            return state
        }
    }


    console.log("${creep}: Unset memory state, using default")
    return HarvesterState.GATHER
}

var CreepMemory.state: Int by memory { HarvesterState.GATHER.value }

class Harvester (creep: Creep): Role(creep){
    override fun run(){
        val state = getState(creep)
        console.log("${creep.name}: State = ${state.name}")
        if (state == HarvesterState.GATHER){
            getEnergy()
        }else if (state == HarvesterState.DEPOSIT){
            storeEnergy()
        }
        else{
            creep.memory.state = HarvesterState.GATHER.value
            run()
        }
    }

    private fun getEnergy(){
        val energySources = creep.room.find(FIND_SOURCES)
        val energySource = energySources.first()

        val status = creep.harvest(energySource)

        if (status == ERR_NOT_IN_RANGE){
            creep.moveTo(energySource.pos.x, energySource.pos.y)
        }
        else if (status != OK){
            console.log("${creep.name}: ERROR Gather failed $status")
        }

        if (creep.store.getFreeCapacity() == 0){
            console.log("${creep.name}: Energy full")
            creep.memory.state = HarvesterState.DEPOSIT.value
        }
    }

    private fun storeEnergy(){
        val controller = creep.room.controller

        if (controller == null){
            console.log("${creep.name}: No controller")
            return
        }

        val status = creep.upgradeController(controller)

        if (status == ERR_NOT_IN_RANGE){
            creep.moveTo(controller)
        }
        else if (status == ERR_NOT_ENOUGH_ENERGY){
            creep.memory.state = HarvesterState.GATHER.value
            return
        }
        else if (status != OK){
            console.log("${creep.name}: ERROR Upgrade failed: $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0){
            creep.memory.state = HarvesterState.GATHER.value
        }
    }
}