package de.dde.snes.da.project

import de.dde.snes.da.memory.ROMByte
import de.dde.snes.da.memory.ROMByteType
import de.dde.snes.da.memory.ROMJumpType
import de.dde.snes.da.processor.instruction
import de.dde.snes.da.memory.MappingMode
import de.dde.snes.da.memory.ROMFile

class Project(
        val romFile: ROMFile,
        val mappingMode: MappingMode
) {
    val header = mappingMode.readHeader(romFile.bytes)

    val romBytes = romFile.bytes.mapIndexed { index, b -> ROMByte(index, b) }

    var loader: ProjectLoader? = null

    init {
        for (vector in listOf(
                header.emulationVectors.reset to "Emulation_Reset",
                header.emulationVectors.nmi to "Emulation_NMI",
                header.emulationVectors.irq to "Emulation_IRQ",
                header.emulationVectors.brk to "Emulation_BRK",
                header.emulationVectors.cop to "Emulation_COP",
                header.emulationVectors.abort to "Emulation_Abort",
                header.nativeVectors.reset to "Native_Reset",
                header.nativeVectors.nmi to "Native_NMI",
                header.nativeVectors.irq to "Native_IRQ",
                header.nativeVectors.brk to "Native_BRK",
                header.nativeVectors.cop to "Native_COP",
                header.nativeVectors.abort to "Native_Abort",
        )) {
            val (address, name) = vector

            if (address < 0x8000 || address == 0xFFFF)
                continue

            val offset = mappingMode.getMemoryAddress(0x00, address).index

            val byte = romBytes[offset]

            if (byte.label == null) {
                markByte(offset, ROMByteType.INSTRUCTION)
                byte.label = name
                byte.jumpType.add(ROMJumpType.CALL_TARGET)
            }
        }

        markHeader()
    }

    private fun markHeader() {
        val headerStart = mappingMode.headerStart

        for (i in 0x10..0x23) {
            romBytes[headerStart + i].comment = "Name[${i - 0x10}]"
        }

        romBytes[headerStart + 0x25].comment = "Mapping Mode"
        romBytes[headerStart + 0x26].comment = "Cartridge Type"
        romBytes[headerStart + 0x27].comment = "ROM Size"
        romBytes[headerStart + 0x28].comment = "RAM Size"
        romBytes[headerStart + 0x29].comment = "Region"
        romBytes[headerStart + 0x2B].comment = "ROM Version"
        romBytes[headerStart + 0x2C].comment = "Checksum (Low)"
        romBytes[headerStart + 0x2D].comment = "Checksum (High)"
        romBytes[headerStart + 0x2E].comment = "Checksum Complement (Low)"
        romBytes[headerStart + 0x2F].comment = "Checksum Complement (High)"


        when (header.headerVersion) {
            1 -> {
                romBytes[headerStart + 0x24].comment = "Name[20]"
                romBytes[headerStart + 0x2A].comment = "Developer ID"
            }
            2 -> {
                romBytes[headerStart + 0x24].comment = "Header Version 2 identifier"
                romBytes[headerStart + 0x2A].comment = "Developer ID"

                for (i in 0x00..0x0E) {
                    romBytes[headerStart + i].comment = "Header Unused"
                }

                romBytes[headerStart + 0x0F].comment = "Co-CPU-Type"
            }
            3 -> {
                romBytes[headerStart + 0x24].comment = "Name[20]"
                romBytes[headerStart + 0x2A].comment = "Header Version 3 Identifier"

                romBytes[headerStart + 0x00].comment = "Developer ID (Low)"
                romBytes[headerStart + 0x01].comment = "Developer ID (High)"

                for (i in 0x02..0x05) {
                    romBytes[headerStart + i].comment = "Gamecode[${i - 0x02}]"
                }

                for (i in 0x06..0x0B) {
                    romBytes[headerStart + i].comment = "Header Unused"
                }
                romBytes[headerStart + 0x0C].comment = "Flash Memory Size"
                romBytes[headerStart + 0x0D].comment = "Extension RAM Size"
                romBytes[headerStart + 0x0E].comment = "Special Version"
                romBytes[headerStart + 0x0F].comment = "Co-CPU-Type"
            }
        }

        romBytes[headerStart + 0x30].comment = "Header Emulation-Vector Unused"
        romBytes[headerStart + 0x31].comment = "Header Emulation-Vector Unused"
        romBytes[headerStart + 0x32].comment = "Header Emulation-Vector Unused"
        romBytes[headerStart + 0x33].comment = "Header Emulation-Vector Unused"
        romBytes[headerStart + 0x34].comment = "Emulation COP-Vector (Low)"
        romBytes[headerStart + 0x35].comment = "Emulation COP-Vector (High)"
        romBytes[headerStart + 0x36].comment = "Header Emulation-Vector Unused"
        romBytes[headerStart + 0x37].comment = "Header Emulation-Vector Unused"
        romBytes[headerStart + 0x38].comment = "Emulation Abort-Vector (Low)"
        romBytes[headerStart + 0x39].comment = "Emulation Abort-Vector (High)"
        romBytes[headerStart + 0x3A].comment = "Emulation NMI-Vector (Low)"
        romBytes[headerStart + 0x3B].comment = "Emulation NMI-Vector (High)"
        romBytes[headerStart + 0x3C].comment = "Emulation Reset-Vector (Low)"
        romBytes[headerStart + 0x3D].comment = "Emulation Reset-Vector (High)"
        romBytes[headerStart + 0x3E].comment = "Emulation IRQ/BRK-Vector (Low)"
        romBytes[headerStart + 0x3F].comment = "Emulation IRQ/BRK-Vector (High)"
        romBytes[headerStart + 0x40].comment = "Header Native-Vector Unused"
        romBytes[headerStart + 0x41].comment = "Header Native-Vector Unused"
        romBytes[headerStart + 0x42].comment = "Header Native-Vector Unused"
        romBytes[headerStart + 0x43].comment = "Header Native-Vector Unused"
        romBytes[headerStart + 0x44].comment = "Native COP-Vector (Low)"
        romBytes[headerStart + 0x45].comment = "Native COP-Vector (High)"
        romBytes[headerStart + 0x46].comment = "Native BRK-Vector (Low)"
        romBytes[headerStart + 0x47].comment = "Native BRK-Vector (High)"
        romBytes[headerStart + 0x48].comment = "Native Abort-Vector (Low)"
        romBytes[headerStart + 0x49].comment = "Native Abort-Vector (High)"
        romBytes[headerStart + 0x4A].comment = "Native NMI-Vector (Low)"
        romBytes[headerStart + 0x4B].comment = "Native NMI-Vector (High)"
        romBytes[headerStart + 0x4C].comment = "Native Reset-Vector (Low)"
        romBytes[headerStart + 0x4D].comment = "Native Reset-Vector (High)"
        romBytes[headerStart + 0x4E].comment = "Native IRQ-Vector (Low)"
        romBytes[headerStart + 0x4F].comment = "Native IRQ-Vector (High)"

        for (i in (if (header.headerVersion == 1) headerStart + 0x10 else headerStart) until (headerStart + MappingMode.HEADER_SIZE))
            romBytes[i].type = ROMByteType.DATA
    }

    fun markByte(offset: Int, type: ROMByteType) {
        romBytes[offset].type = type

        when (type) {
            ROMByteType.INSTRUCTION -> {
                val inst = instruction(romBytes[offset].b)
                inst.getOperandBytes(this, romBytes[offset]).forEach { markByte(it.index, ROMByteType.OPERAND) }
            }
            ROMByteType.POINTER16 -> {
                romBytes[offset + 1].type = type
            }
            ROMByteType.POINTER24 -> {
                romBytes[offset + 1].type = type
                romBytes[offset + 2].type = type
            }
        }
        if (type == ROMByteType.INSTRUCTION) {

        }
    }
}