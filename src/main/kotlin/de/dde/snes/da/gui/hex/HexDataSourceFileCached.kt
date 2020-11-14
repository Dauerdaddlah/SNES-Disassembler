package de.dde.snes.da.gui.hex

import java.io.File

class HexDataSourceFileCached(
        file: File
) : HexDataSource {

    private val buffer = file.readBytes()
    override val size: Int
        get() = buffer.size

    override fun ensure(index: Int, size: Int) {
    }

    override fun getByte(index: Int): Byte {
        if (index < 0 || index >= buffer.size) {
            return 0
        }

        return buffer[index]
    }
}