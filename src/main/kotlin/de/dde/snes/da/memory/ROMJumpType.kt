package de.dde.snes.da.memory

enum class ROMJumpType {
    BRANCH,
    JUMP,
    CALL,
    BRANCH_TARGET,
    JUMP_TARGET,
    CALL_TARGET
}