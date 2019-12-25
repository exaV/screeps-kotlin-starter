package io.r2n.screeps.ai

import screeps.api.GlobalMemory
import screeps.api.Source
import screeps.utils.memory.memory

var GlobalMemory.sources: Array<Source> by memory { emptyArray<Source>() }
var GlobalMemory.lastMinerAssignment by memory { 0 }