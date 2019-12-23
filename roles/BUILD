load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_js_library", "kt_js_import")
kt_js_import(
    name = "screeps-kotlin-types",
    jars = [
        "@maven//:ch_delconte_screeps_kotlin_screeps_kotlin_types"
    ],
    visibility = ["//:__subpackages__"],
)

kt_js_library(
    name ="Role",
    srcs = [":Role.kt"],
    deps = [
        "screeps-kotlin-types"
    ],
    visibility = ["//:__subpackages__"]
)

kt_js_library(
    name ="Roles",
    srcs = [":Roles.kt"],
    deps = [
        "screeps-kotlin-types",
        ":Role",
        ":BasicCreepMemory"

    ],
    visibility = ["//:__subpackages__"]
)

kt_js_library(
    name ="BasicCreepMemory",
    srcs = [":BasicCreepMemory.kt"],
    deps = [
        "screeps-kotlin-types",
        ":Role"
    ],
    visibility = ["//:__subpackages__"]
)
