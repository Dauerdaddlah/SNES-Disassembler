package de.dde.snes.da.memory

import de.dde.snes.da.*
import de.dde.snes.da.processor.*

val allMappingModes: List<MappingMode>
    get() = listOf(LoROM, HiROM, ExLoROM, ExHiROM)

sealed class MappingMode(
    val headerStart: Int,
    val mode: Int,
    val name: String,
    val bankSize: Int = 0x10000
) {
    fun readHeader(rom: ByteArray): RomHeader {
        val headerVersion = when {
            rom[headerStart + 0x2A] == 0x33.toByte() -> 3
            rom[headerStart + 0x24] == 0x00.toByte() -> 2
            else -> 1
        }

        val gameCode = if (headerVersion < 3) "" else String(rom, headerStart + 0x02, 4)
        val flash = if (headerVersion < 3) 0 else rom[headerStart + 0x0C]
        val exRamSize = SizeKB(if (headerVersion < 3) 0 else rom[headerStart + 0x0D])
        val specialVersion = if (headerVersion < 3) 0 else rom[headerStart + 0x0E]

        val coCpuType = if (headerVersion < 2) 0 else rom[headerStart + 0x0F]

        val romName = String(rom, headerStart + 0x10, if (headerVersion == 2) 0x14 else 0x15)
        val mappingMode = rom[headerStart + 0x25]
        val cartridgeType = RomType(rom[headerStart + 0x26])
        val romSize = SizeMB(rom[headerStart + 0x27])
        val ramSize = SizeKB(rom[headerStart + 0x28])
        val region = CountryCode(rom[headerStart + 0x29])
        val devId = Licensee(if (headerVersion < 2) Byte(rom[headerStart + 0x2A]) else Word(rom[headerStart + 0x00], rom[headerStart + 0x01]))
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
        if (rom.size < headerStart + 0x50) return Integer.MIN_VALUE // not enough bytes for a valid header

        var score = 0

        val mapMode = Byte(rom[headerStart + 0x25].toInt() and 0x10.inv())  //ignore FastROM bit
        val complement = Word(rom[headerStart + 0x2c], rom[headerStart + 0x2d])
        val checksum = Word(rom[headerStart + 0x2e], rom[headerStart + 0x2f])
        val resetVector = Word(rom[headerStart + 0x4c], rom[headerStart + 0x4d])

        if(resetVector < 0x8000) return score  //$00:0000-7fff is never ROM data

        val opcode = rom[(headerStart and 0x7fff.inv()) or (resetVector and 0x7fff)]  //first instruction executed

        when (instruction(opcode).operation) {
            //most likely opcodes
            SEI,  //sei
            CLC,  //clc (clc; xce)
            SEC,  //sec (sec; xce)
            STZ,  //stz $nnnn (stz $4200)
            JMP,  //jmp $nnnn
            JML  //jml $nnnnnn
            -> score += 8

            //plausible opcodes
            REP,  //rep #$nn
            SEP,  //sep #$nn
            LDA,  //lda $nnnn
            LDX,  //ldx $nnnn
            LDY,  //ldy $nnnn
            LDA,  //lda $nnnnnn
            //0xa9,  //lda #$nn
            //0xa2,  //ldx #$nn
            //0xa0,  //ldy #$nn
            JSR,  //jsr $nnnn
            JSL  //jsl $nnnnnn
            -> score += 4

            //implausible opcodes
            RTI,  //rti
            RTS,  //rts
            RTL,  //rtl
            CMP,  //cmp $nnnn
            CPX,  //cpx $nnnn
            CPY  //cpy $nnnn
            -> score -= 4

            //least likely opcodes
            BRK,  //brk #$nn
            COP,  //cop #$nn
            STP,  //stp
            WDM,  //wdm
            SBC  //sbc $nnnnnn,x
            -> score -= 8

            else -> {
            }
        }

        if(checksum + complement == 0xffff) score += 4

        if(mapMode == mode) score += 2

        return score
    }

    abstract fun toSnesAddress(byte: ROMByte): Int

    open fun getMemoryAddress(address: Int) = getMemoryAddress(address.longByte(), address.asShort())

    abstract fun getMemoryAddress(bank: Int, address: Int): MemoryAddress

    companion object {
        const val HEADER_SIZE = 0x50
    }
}

object LoROM : MappingMode(0x7FB0, 0x20, "LoROM", 0x8000) {

    override fun getMemoryAddress(bank: Int, address: Int): MemoryAddress {
        return when {
            // right half
            bank < 0x7E -> getMemoryAddress(bank or 0x80, address) // mirror of left half
            bank < 0x80 -> MemoryAddress(MemoryArea.WRAM, (bank - 0x7E) * _1BANK + address) // WRAM

            // left lower quarter
            address < 0x8000 -> when {
                bank < 0xC0 -> {
                    if (address < 0x2000) MemoryAddress(MemoryArea.WRAM, address)
                    else MemoryAddress(MemoryArea.HARDWARE_REGISTER, address)
                } // Hardware addresses
                bank >= 0x70 -> {
                    MemoryAddress(MemoryArea.SRAM, (bank - 0xF0) * HALF_BANK + address)
                } // SRAM
                else -> getMemoryAddress(bank, address or 0x8000) // mirror of top half
            }

            // left upper quarter -> rom
            else -> {
                val index = (bank and 0x7F) * 0x8000 + (address and 0x7FFF)

                MemoryAddress(MemoryArea.ROM, index)
            }
        }
    }

    override fun toSnesAddress(byte: ROMByte): Int {
        val bank = byte.index shr 15
        val addr = (byte.index and 0x7FFF) or 0x8000

        return when {
            byte.state.pbr >= 0x80 -> ((bank shl 16) or 0x800000) or addr
            else -> (bank shl 16) or addr
        }
    }
}

object HiROM : MappingMode(0xFFB0, 0x21, "HiROM") {
    override fun getMemoryAddress(bank: Int, address: Int): MemoryAddress {
        return when {
            // right half
            bank < 0x7E -> getMemoryAddress(bank or 0x80, address) // mirror of left half
            bank < 0x80 -> MemoryAddress(MemoryArea.WRAM, ((bank - 0x7E) * _1BANK) + address) // WRAM

            // left half -> right half
            bank < 0xC0 -> when {
                address < 0x2000 -> MemoryAddress(MemoryArea.WRAM, address)
                address < 0x6000 -> MemoryAddress(MemoryArea.HARDWARE_REGISTER, address)
                address < 0x8000 -> MemoryAddress(MemoryArea.SRAM, ((bank and 0xF) * 0x2000) + (address - 0x6000))
                else -> getMemoryAddress(bank + 0x40, address) // mirror of rom
            }

            // left half -> left half
            else -> MemoryAddress(MemoryArea.ROM, ((bank - 0xC0) * _1BANK) + address)
        }
    }

    override fun toSnesAddress(byte: ROMByte): Int {
        return when {
            byte.state.pbr >= 0xC0 -> byte.index or 0xC00000
            byte.state.pbr >= 0x80 -> byte.index or 0x800000
            byte.state.pbr >= 0x40 -> byte.index or 0x400000
            else -> byte.index
        }
    }
}

object ExLoROM : MappingMode(0x407fb0, 0x32, "ExLoROM") {
    override fun getMemoryAddress(bank: Int, address: Int): MemoryAddress {
        TODO("Not yet implemented")
    }

    override fun score(rom: ByteArray): Int {
        var score = super.score(rom)

        if (score > 0) {
            score += 4
        }

        return score
    }

    override fun toSnesAddress(byte: ROMByte): Int {
        TODO("Not yet implemented")
    }
}

object ExHiROM : MappingMode(0x40ffb0, 0x35, "ExHiROM") {
    override fun getMemoryAddress(bank: Int, address: Int): MemoryAddress {
        TODO("Not yet implemented")
    }

    override fun score(rom: ByteArray): Int {
        var score = super.score(rom)

        if (score > 0) {
            score += 4
        }

        return score
    }

    override fun toSnesAddress(byte: ROMByte): Int {
        TODO("Not yet implemented")
    }
}

enum class MemoryArea {
    HARDWARE_REGISTER,
    WRAM,
    SRAM,
    ROM
}

data class MemoryAddress(
        val area: MemoryArea,
        val index: Int
)