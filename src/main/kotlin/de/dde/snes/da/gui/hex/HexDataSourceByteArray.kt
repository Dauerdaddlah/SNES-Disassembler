package de.dde.snes.da.gui.hex

import java.io.File
import java.io.IOException
import java.nio.file.Files

class HexDataSourceByteArray(
    private val buffer: ByteArray
) : HexDataSource {
    override val size: Int
        get() = buffer.size

    @Throws(IOException::class)
    constructor(file: File) : this(Files.readAllBytes(file.toPath()))

    override fun ensure(index: Int, size: Int) {
    }

    override fun getByte(index: Int): Byte {
        if (index < 0 || index >= buffer.size) {
            return 0
        }

        return buffer[index]
    }
}