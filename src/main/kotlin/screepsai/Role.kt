package screepsai

import screeps.api.Creep

abstract class Role(val creep: Creep) {
    abstract fun run()
}