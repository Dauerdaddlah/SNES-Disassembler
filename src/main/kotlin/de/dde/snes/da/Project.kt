package de.dde.snes.da

import de.dde.snes.da.memory.ROMByte
import de.dde.snes.da.rom.MappingMode
import de.dde.snes.da.rom.ROMFile

class Project(
        val romFile: ROMFile,
        val mappingMode: MappingMode
) {
    val header = mappingMode.readHeader(romFile.bytes)

    val romBytes = romFile.bytes.mapIndexed { index, b -> ROMByte(index, b) }
}