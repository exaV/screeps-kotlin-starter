package io.r2n.screeps.ai.roles

import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject

object Miner : EmployedCreep {
    private val bodyRatio = mapOf(
            WORK to 2,
            MOVE to 1
    )
    private const val MAX_WORK = 5;
    //TODO miner
    override val role = Role.HARVESTER
    override val options: SpawnOptions = options {
        memory = jsObject<CreepMemory> { this.role = role }
    }


    override fun spawn(
            spawn: StructureSpawn,
            maxEnergy: Int,
            name: String
    ): ScreepsReturnCode {
        return spawn.spawnCreep(calculateOptimalBodyParts(maxEnergy), name, options)
    }

    private fun calculateOptimalBodyParts(maxEnergy: Int): Array<BodyPartConstant> {
        var body = emptyList<BodyPartConstant>()
        var usedEnergy = 0;
        while (usedEnergy <= maxEnergy) {
            when {
                thereAreMoreWorkThanMoveParts(body, bodyRatio[WORK] ?: 0) &&
                        body.count { it == WORK } <= MAX_WORK -> {
                    body += WORK
                    usedEnergy += BODYPART_COST[WORK]!!
                }
                else -> {
                    body += MOVE
                    usedEnergy += BODYPART_COST[MOVE]!!
                }

            }
        }
        return body.toTypedArray()
    }

    private fun thereAreMoreWorkThanMoveParts(bodyParts: List<BodyPartConstant>, delta: Int = 1): Boolean {
        return bodyParts.count { it == WORK } > bodyParts.count { it == MOVE } + delta - 1
    }
}