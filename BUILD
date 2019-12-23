load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_js_library", "kt_js_import")
kt_js_import(
    name = "screeps-kotlin-types",
    jars = [
        "@maven//:ch_delconte_screeps_kotlin_screeps_kotlin_types"
    ],
    visibility = ["//:__subpackages__"],
)

kt_js_library(
    name ="main",
    srcs = [":Main.kt"],
    deps = [
        ":SimpleAI"
    ],
    visibility = ["//:__subpackages__"]
)

kt_js_library(
    name ="SimpleAI",
    srcs = [":SimpleAI.kt"],
    deps = [
        "screeps-kotlin-types",
        "//roles:Role",
        "//roles:Roles", "//roles:BasicCreepMemory"
    ],
    visibility = ["//:__subpackages__"]
)
