package starter

import types.base.global.CreepMemory

var CreepMemory.building: Boolean
    get() = this.asDynamic().building ?: false
    set(value) {
        this.asDynamic().building = value
    }

var CreepMemory.role: Role
    get() {
        val roleString: String? = this.asDynamic().role
        return if (roleString != null) Role.valueOf(roleString) else Role.UNASSIGNED
    }
    set(value) {
        this.asDynamic().role = value.name
    }
