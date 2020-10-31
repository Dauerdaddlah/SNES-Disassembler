package de.dde.snes.da

class ROM(
    val bytesAll: ByteArray
) {
    val bytes: ByteArray = when (bytesAll.size.rem(0x400)) {
        0 -> bytesAll
        0x200 -> bytesAll.copyOfRange(0x200, bytesAll.size)
        else -> error("malformed ROM")
    }

    val byteMeanings = Array<Meaning?>(bytes.size) { null }

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