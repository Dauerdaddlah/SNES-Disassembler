package de.dde.snes.da

data class SnesState(
    val mode: ProcessorMode = ProcessorMode.EMULATION,
    val pc: Register = Register(),
    val pbr: Register = Register(),
    val dbr: Register = Register(),
    val a: Register = Register(),
    val x: Register = Register(),
    val y: Register = Register(),
    val d: Register = Register(),
    val s: Register = Register(),
    val p: StatusRegister = StatusRegister()
)

enum class ProcessorMode {
    EMULATION,
    NATIVE
}

inline class Register(val r: Int = R_UNKNOWN) {
    val unknown: Boolean
        get() = r == R_UNKNOWN
}

private const val R_UNKNOWN = -1

class StatusRegister(
    val iN: Int = R_UNKNOWN,
    val iV: Int = R_UNKNOWN,
    val iM: Int = R_UNKNOWN,
    val iX: Int = R_UNKNOWN,
    val iD: Int = R_UNKNOWN,
    val iI: Int = R_UNKNOWN,
    val iZ: Int = R_UNKNOWN,
    val iC: Int = R_UNKNOWN
)