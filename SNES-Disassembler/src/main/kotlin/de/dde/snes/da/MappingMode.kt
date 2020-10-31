package de.dde.snes.da

sealed class MappingMode(
    val headerStart: Int,
    val mode: Int
) {
    fun readHeader(rom: ByteArray): RomHeader {
        val headerVersion = when {
            rom[headerStart + 0x2A] == 0x33.toByte() -> 3
            rom[headerStart + 0x24] == 0x00.toByte() -> 2
            else -> 1
        }

        val gameCode = if (headerVersion < 3) "" else String(rom, headerStart + 0x02, 4)
        val flash = if (headerVersion < 3) 0 else rom[headerStart + 0x0C]
        val exRamSize = if (headerVersion < 3) 0 else rom[headerStart + 0x0D]
        val specialVersion = if (headerVersion < 3) 0 else rom[headerStart + 0x0E]

        val coCpuType = if (headerVersion < 2) 0 else rom[headerStart + 0x0F]

        val romName = String(rom, headerStart + 0x10, if (headerVersion == 2) 0x14 else 0x15)
        val mappingMode = rom[headerStart + 0x25]
        val cartridgeType = rom[headerStart + 0x26]
        val romSize = rom[headerStart + 0x27]
        val ramSize = rom[headerStart + 0x28]
        val region = rom[headerStart + 0x29]
        val devId = if (headerVersion < 2) rom[headerStart + 0x2A] else Word(rom[headerStart + 0x00], rom[headerStart + 0x01])
        val romVersion = rom[headerStart + 0x2B]
        val complement = Word(rom[headerStart + 0x2C], rom[headerStart + 0x2D])
        val checksum = Word(rom[headerStart + 0x2E], rom[headerStart + 0x2F])

        val emulationVectors = Vectors(
            Word(rom[headerStart + 0x34], rom[headerStart + 0x35]),
            Word(rom[headerStart + 0x3E], rom[headerStart + 0x3F]),
            Word(rom[headerStart + 0x38], rom[headerStart + 0x39]),
            Word(rom[headerStart + 0x3A], rom[headerStart + 0x3B]),
            Word(rom[headerStart + 0x3C], rom[headerStart + 0x3D]),
            Word(rom[headerStart + 0x3E], rom[headerStart + 0x3F]),
        )

        val nativeVectors = Vectors(
            Word(rom[headerStart + 0x44], rom[headerStart + 0x45]),
            Word(rom[headerStart + 0x46], rom[headerStart + 0x47]),
            Word(rom[headerStart + 0x48], rom[headerStart + 0x49]),
            Word(rom[headerStart + 0x4A], rom[headerStart + 0x4B]),
            Word(rom[headerStart + 0x4C], rom[headerStart + 0x4D]),
            Word(rom[headerStart + 0x4E], rom[headerStart + 0x4F]),
        )

        return RomHeader(headerVersion, romName, mappingMode, cartridgeType, romSize, ramSize, region, devId, romVersion, complement, checksum, coCpuType, gameCode, flash, exRamSize, specialVersion, emulationVectors, nativeVectors)
    }

    open fun score(rom: ByteArray): Int {
        // function borrowed from bsnes
        if (rom.size < headerStart + 0x50) return 0 // not enough bytes for a valid header

        var score = 0

        val mapMode = Byte(rom[headerStart + 0x25].toInt() and 0x10.inv())  //ignore FastROM bit
        val complement = Word(rom[headerStart + 0x2c], rom[headerStart + 0x2d])
        val checksum = Word(rom[headerStart + 0x2e], rom[headerStart + 0x2f])
        val resetVector = Word(rom[headerStart + 0x4c], rom[headerStart + 0x4d])

        if(resetVector < 0x8000) return score;  //$00:0000-7fff is never ROM data

        val opcode = rom[(headerStart and 0x7fff.inv()) or (resetVector and 0x7fff)]  //first instruction executed

        when (Byte(opcode)) {
            //most likely opcodes
            0x78,  //sei
            0x18,  //clc (clc; xce)
            0x38,  //sec (sec; xce)
            0x9c,  //stz $nnnn (stz $4200)
            0x4c,  //jmp $nnnn
            0x5c  //jml $nnnnnn
            -> score += 8

            //plausible opcodes
            0xc2,  //rep #$nn
            0xe2,  //sep #$nn
            0xad,  //lda $nnnn
            0xae,  //ldx $nnnn
            0xac,  //ldy $nnnn
            0xaf,  //lda $nnnnnn
            0xa9,  //lda #$nn
            0xa2,  //ldx #$nn
            0xa0,  //ldy #$nn
            0x20,  //jsr $nnnn
            0x22  //jsl $nnnnnn
            -> score += 4

            //implausible opcodes
            0x40,  //rti
            0x60,  //rts
            0x6b,  //rtl
            0xcd,  //cmp $nnnn
            0xec,  //cpx $nnnn
            0xcc  //cpy $nnnn
            -> score -= 4

            //least likely opcodes
            0x00,  //brk #$nn
            0x02,  //cop #$nn
            0xdb,  //stp
            0x42,  //wdm
            0xff  //sbc $nnnnnn,x
            -> score -= 8
        }

        if(checksum + complement == 0xffff) score += 4;

        if(mapMode == mode) score += 2;

        return score
    }

    open fun getByte(snes: SNES, address: Int): Int {
        return getByte(snes, address.longByte(), address.asShort())
    }

    abstract fun getByte(snes: SNES, bank: Int, address: Int): Int

    fun annotateRom(rom: ROM) {
        val header = readHeader(rom.bytes)

        rom.byteMeanings[headerStart + 0x10] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x11] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x12] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x13] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x14] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x15] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x16] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x17] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x18] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x19] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x1A] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x1B] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x1C] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x1D] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x1E] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x1F] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x20] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x21] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x22] = Meaning(MeaningType.HEADER_NAME)
        rom.byteMeanings[headerStart + 0x23] = Meaning(MeaningType.HEADER_NAME)

        rom.byteMeanings[headerStart + 0x25] = Meaning(MeaningType.HEADER_MAPPINGMODE)
        rom.byteMeanings[headerStart + 0x26] = Meaning(MeaningType.HEADER_CARTRIDGETYPE)
        rom.byteMeanings[headerStart + 0x27] = Meaning(MeaningType.HEADER_ROMSIZE)
        rom.byteMeanings[headerStart + 0x28] = Meaning(MeaningType.HEADER_RAMSIZE)
        rom.byteMeanings[headerStart + 0x29] = Meaning(MeaningType.HEADER_REGION)
        rom.byteMeanings[headerStart + 0x3B] = Meaning(MeaningType.HEADER_ROMVERSION)
        rom.byteMeanings[headerStart + 0x3C] = Meaning(MeaningType.HEADER_CHECKSUM)
        rom.byteMeanings[headerStart + 0x3D] = Meaning(MeaningType.HEADER_CHECKSUM)
        rom.byteMeanings[headerStart + 0x3E] = Meaning(MeaningType.HEADER_CHECKSUM_COMPLEMENT)
        rom.byteMeanings[headerStart + 0x3F] = Meaning(MeaningType.HEADER_CHECKSUM_COMPLEMENT)


        when (header.headerVersion) {
            1 -> {
                rom.byteMeanings[headerStart + 0x24] = Meaning(MeaningType.HEADER_NAME)
                rom.byteMeanings[headerStart + 0x3A] = Meaning(MeaningType.HEADER_DEVID)
            }
            2 -> {
                rom.byteMeanings[headerStart + 0x24] = Meaning(MeaningType.HEADER_NAME_VERSION)
                rom.byteMeanings[headerStart + 0x3A] = Meaning(MeaningType.HEADER_DEVID)

                rom.byteMeanings[headerStart + 0x00] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x01] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x02] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x03] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x04] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x05] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x06] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x07] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x08] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x09] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0A] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0B] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0C] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0D] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0E] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0F] = Meaning(MeaningType.HEADER_COCPUTYPE)
            }
            3 -> {
                rom.byteMeanings[headerStart + 0x24] = Meaning(MeaningType.HEADER_NAME)
                rom.byteMeanings[headerStart + 0x3A] = Meaning(MeaningType.HEADER_DEVID_VERSION)

                rom.byteMeanings[headerStart + 0x00] = Meaning(MeaningType.HEADER_DEVID)
                rom.byteMeanings[headerStart + 0x01] = Meaning(MeaningType.HEADER_DEVID)
                rom.byteMeanings[headerStart + 0x02] = Meaning(MeaningType.HEADER_GAMECODE)
                rom.byteMeanings[headerStart + 0x03] = Meaning(MeaningType.HEADER_GAMECODE)
                rom.byteMeanings[headerStart + 0x04] = Meaning(MeaningType.HEADER_GAMECODE)
                rom.byteMeanings[headerStart + 0x05] = Meaning(MeaningType.HEADER_GAMECODE)
                rom.byteMeanings[headerStart + 0x06] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x07] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x08] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x09] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0A] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0B] = Meaning(MeaningType.HEADER_UNUSED)
                rom.byteMeanings[headerStart + 0x0C] = Meaning(MeaningType.HEADER_FLASHMEMORY)
                rom.byteMeanings[headerStart + 0x0D] = Meaning(MeaningType.HEADER_EXRAMSIZE)
                rom.byteMeanings[headerStart + 0x0E] = Meaning(MeaningType.HEADER_SPECIALVERSION)
                rom.byteMeanings[headerStart + 0x0F] = Meaning(MeaningType.HEADER_COCPUTYPE)
            }
        }

        rom.byteMeanings[headerStart + 0x40] = Meaning(MeaningType.VECTOR_EMULATION_UNUSED)
        rom.byteMeanings[headerStart + 0x41] = Meaning(MeaningType.VECTOR_EMULATION_UNUSED)
        rom.byteMeanings[headerStart + 0x42] = Meaning(MeaningType.VECTOR_EMULATION_UNUSED)
        rom.byteMeanings[headerStart + 0x43] = Meaning(MeaningType.VECTOR_EMULATION_UNUSED)
        rom.byteMeanings[headerStart + 0x44] = Meaning(MeaningType.VECTOR_EMULATION_COP)
        rom.byteMeanings[headerStart + 0x45] = Meaning(MeaningType.VECTOR_EMULATION_COP)
        rom.byteMeanings[headerStart + 0x46] = Meaning(MeaningType.VECTOR_EMULATION_UNUSED)
        rom.byteMeanings[headerStart + 0x47] = Meaning(MeaningType.VECTOR_EMULATION_UNUSED)
        rom.byteMeanings[headerStart + 0x48] = Meaning(MeaningType.VECTOR_EMULATION_ABORT)
        rom.byteMeanings[headerStart + 0x49] = Meaning(MeaningType.VECTOR_EMULATION_ABORT)
        rom.byteMeanings[headerStart + 0x4A] = Meaning(MeaningType.VECTOR_EMULATION_NMI)
        rom.byteMeanings[headerStart + 0x4B] = Meaning(MeaningType.VECTOR_EMULATION_NMI)
        rom.byteMeanings[headerStart + 0x4C] = Meaning(MeaningType.VECTOR_EMULATION_RESET)
        rom.byteMeanings[headerStart + 0x4D] = Meaning(MeaningType.VECTOR_EMULATION_RESET)
        rom.byteMeanings[headerStart + 0x4E] = Meaning(MeaningType.VECTOR_EMULATION_IRQBRK)
        rom.byteMeanings[headerStart + 0x4F] = Meaning(MeaningType.VECTOR_EMULATION_IRQBRK)
        rom.byteMeanings[headerStart + 0x50] = Meaning(MeaningType.VECTOR_NATIVE_UNUSED)
        rom.byteMeanings[headerStart + 0x51] = Meaning(MeaningType.VECTOR_NATIVE_UNUSED)
        rom.byteMeanings[headerStart + 0x52] = Meaning(MeaningType.VECTOR_NATIVE_UNUSED)
        rom.byteMeanings[headerStart + 0x53] = Meaning(MeaningType.VECTOR_NATIVE_UNUSED)
        rom.byteMeanings[headerStart + 0x54] = Meaning(MeaningType.VECTOR_NATIVE_COP)
        rom.byteMeanings[headerStart + 0x55] = Meaning(MeaningType.VECTOR_NATIVE_COP)
        rom.byteMeanings[headerStart + 0x56] = Meaning(MeaningType.VECTOR_NATIVE_BRK)
        rom.byteMeanings[headerStart + 0x57] = Meaning(MeaningType.VECTOR_NATIVE_BRK)
        rom.byteMeanings[headerStart + 0x58] = Meaning(MeaningType.VECTOR_NATIVE_ABORT)
        rom.byteMeanings[headerStart + 0x59] = Meaning(MeaningType.VECTOR_NATIVE_ABORT)
        rom.byteMeanings[headerStart + 0x5A] = Meaning(MeaningType.VECTOR_NATIVE_NMI)
        rom.byteMeanings[headerStart + 0x5B] = Meaning(MeaningType.VECTOR_NATIVE_NMI)
        rom.byteMeanings[headerStart + 0x5C] = Meaning(MeaningType.VECTOR_NATIVE_RESET)
        rom.byteMeanings[headerStart + 0x5D] = Meaning(MeaningType.VECTOR_NATIVE_RESET)
        rom.byteMeanings[headerStart + 0x5E] = Meaning(MeaningType.VECTOR_NATIVE_IRQ)
        rom.byteMeanings[headerStart + 0x5F] = Meaning(MeaningType.VECTOR_NATIVE_IRQ)
    }
}

object LoROM : MappingMode(0x7FB0, 0x20) {
    override fun getByte(snes: SNES, bank: Int, address: Int): Int {
        return when {
            // right half
            bank < 0x7E -> getByte(snes, bank or 0x80, address) // mirror of left half
            bank < 0x80 -> 0x00 // WRAM

            // left lower quarter
            address < 0x8000 -> when {
                bank < 0xC0 -> 0x00 // Hardware addresses
                bank >= 0x70 -> 0x00 // SRAM
                else -> getByte(snes, bank, address or 0x8000) // mirror of top half
            }

            // left upper wuarter -> rom
            else -> {
                val index = (bank and 0x7F) * 0x8000 + (address and 0x7FFF)

                index
            }
        }
    }
}

object HiROM : MappingMode(0xFFB0, 0x21) {
    override fun getByte(snes: SNES, bank: Int, address: Int): Int {
        TODO("Not yet implemented")
    }
}

object ExLoROM : MappingMode(0x407fb0, 0x32) {
    override fun getByte(snes: SNES, bank: Int, address: Int): Int {
        TODO("Not yet implemented")
    }

    override fun score(rom: ByteArray): Int {
        var score = super.score(rom)

        if (score > 0) {
            score += 4
        }

        return score
    }
}

object ExHiROM : MappingMode(0x40ffb0, 0x35) {
    override fun getByte(snes: SNES, bank: Int, address: Int): Int {
        TODO("Not yet implemented")
    }

    override fun score(rom: ByteArray): Int {
        var score = super.score(rom)

        if (score > 0) {
            score += 4
        }

        return score
    }
}

data class RomHeader(
    val headerVersion: Int,
    val romName: String,
    val mappingMode: Byte,
    val cartridgeType: Byte,
    val romSize: Byte,
    val ramSize: Byte,
    val region: Byte,
    val devId: Any,
    val romVersion: Byte,
    val complement: Int,
    val checksum: Int,
    val coCpuType: Byte,
    val gameCode: String,
    val flash: Byte,
    val exRamSize: Byte,
    val specialVersion: Byte,
    val emulationVectors: Vectors,
    val nativeVectors: Vectors
)

data class Vectors(
    val cop: Int,
    val brk: Int,
    val abort: Int,
    val nmi: Int,
    val reset: Int,
    val irq: Int
)