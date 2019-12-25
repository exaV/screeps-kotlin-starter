package io.r2n.screeps.ai.roles

import screeps.api.Creep

abstract class EmployedCreep : Creep() {
    abstract fun spawn()
}