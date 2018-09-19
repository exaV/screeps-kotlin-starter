package starter

import types.base.global.CreepMemory
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*
This will be moved to screep-kotlin-types in the future!
 */

open class CreepMemoryMappingDelegate<T>(
    protected val default: T,
    protected val serializer: (T) -> String,
    protected val deserializer: (String) -> T
) : ReadWriteProperty<CreepMemory, T> {

    override fun getValue(thisRef: dynamic, property: KProperty<*>): T {
        val value = thisRef[property.name] as? String

        return if (value != null) {
            deserializer(value)
        } else {
            thisRef[property.name] = serializer(default)
            default
        }
    }

    override fun setValue(thisRef: dynamic, property: KProperty<*>, value: T) {
        thisRef[property.name] = serializer(value)
    }
}

fun <T : Any> memoryOrDefault(default: T, serializer: (T) -> String, deserializer: (String) -> T)
        : ReadWriteProperty<CreepMemory, T> = CreepMemoryMappingDelegate(default, serializer, deserializer)

inline fun <reified T : Enum<T>> memoryOrDefault(default: T)
        : ReadWriteProperty<CreepMemory, T> =
    CreepMemoryMappingDelegate(default, Enum<T>::name) { s -> enumValueOf(s) }
