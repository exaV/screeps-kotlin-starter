package screepsai

import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.utils.memory.memory

var CreepMemory.state: Int by memory { CreepState.GET_ENERGY.ordinal }

enum class CreepState{
    GET_ENERGY,
    DO_WORK;
}

fun getState(state: Int): CreepState{
    for (creepState in CreepState.values()){
        if (creepState.ordinal == state){
            return creepState
        }
    }

    return CreepState.GET_ENERGY
}

abstract class Role(val creep: Creep) {
    var state: CreepState = getState(creep.memory.state)
        set(value) {
            field = value
            creep.memory.state = value.ordinal
        }
    abstract fun run()
}