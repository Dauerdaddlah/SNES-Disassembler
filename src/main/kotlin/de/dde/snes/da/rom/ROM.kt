package de.dde.snes.da.rom

class ROM(
        val romFile: ROMFile,
        val mappingMode: MappingMode = romFile.mappingMode?: throw IllegalArgumentException("only valid RomFiles can be used for a ROM")
) {
    val header = if (mappingMode == romFile.mappingMode) romFile.snesHeader else mappingMode.readHeader(romFile.bytes)

    val bytes: ByteArray
        get() = romFile.bytes

    val byteMeanings = Array<Meaning?>(bytes.size) { null }

    init {
        if (!romFile.valid) throw IllegalArgumentException("only valid RomFiles can be used for a ROM")
    }

}

data class Meaning(val type: MeaningType)

enum class MeaningType {
    HEADER_NAME,
    HEADER_NAME_VERSION,
    HEADER_MAPPINGMODE,
    HEADER_CARTRIDGETYPE,
    HEADER_ROMSIZE,
    HEADER_RAMSIZE,
    HEADER_REGION,
    HEADER_DEVID,
    HEADER_DEVID_VERSION,
    HEADER_ROMVERSION,
    HEADER_CHECKSUM,
    HEADER_CHECKSUM_COMPLEMENT,

    HEADER_UNUSED,
    HEADER_COCPUTYPE,

    HEADER_GAMECODE,
    HEADER_FLASHMEMORY,
    HEADER_EXRAMSIZE,
    HEADER_SPECIALVERSION,

    VECTOR_EMULATION_UNUSED,
    VECTOR_EMULATION_COP,
    VECTOR_EMULATION_ABORT,
    VECTOR_EMULATION_NMI,
    VECTOR_EMULATION_RESET,
    VECTOR_EMULATION_IRQBRK,

    VECTOR_NATIVE_UNUSED,
    VECTOR_NATIVE_COP,
    VECTOR_NATIVE_BRK,
    VECTOR_NATIVE_ABORT,
    VECTOR_NATIVE_NMI,
    VECTOR_NATIVE_RESET,
    VECTOR_NATIVE_IRQ,

    VECTOR,
    DATA,
    OPERATION,
    PARAM
}