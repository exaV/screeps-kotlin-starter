package screepsai

import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.utils.memory.memory

var CreepMemory.state: Int by memory { CreepState.GET_ENERGY.ordinal }

enum class CreepState {
    GET_ENERGY,
    DO_WORK;
}

fun getState(state: Int): CreepState {

    return CreepState.values().firstOrNull { it.ordinal == state } ?: CreepState.GET_ENERGY
}


abstract class Role(val creep: Creep) {
    var state: CreepState = getState(creep.memory.state)
        set(value) {
            field = value
            creep.memory.state = value.ordinal
        }

    private fun log(level: String, message: String, say: Boolean = false) {
        if (say) {
            creep.say(message)
        }
        console.log("$level ${creep.name}: $message")
    }

    fun debug(message: String, say: Boolean = false) {
        log("DEBUG", message, say = say)
    }

    fun info(message: String, say: Boolean = false) {
        log("INFO", message, say = say)
    }

    fun warning(message: String, say: Boolean = false) {
        log("WARNING", message, say = say)
    }

    fun error(message: String, say: Boolean = false) {
        log("ERROR", message, say = say)
    }

    abstract fun run()
}

enum class ScreepRole {
    HARVESTER,
    TRANSPORTER,
    UPGRADER
}

