package de.dde.snes.da.memory

enum class ROMByteType {
    UNKNOWN,
    INSTRUCTION,
    OPERAND,
    DATA,
    GRAPHICS,
    SOUND,
    TEXT,
    POINTER16,
    POINTER24,
    FILLER
}