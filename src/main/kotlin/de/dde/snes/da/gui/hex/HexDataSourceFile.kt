package de.dde.snes.da.gui.hex

import java.io.File
import java.io.RandomAccessFile

class HexDataSourceFile(
        file: File
) : HexDataSource {
    private val f = RandomAccessFile(file, "r")

    override val size: Int
        get() = f.length().toInt()

    private var bufferStart = -1
    private var bufferSize = 0
    private var buffer = byteArrayOf()

    override fun ensure(index: Int, size: Int) {
        if (index > this.size)
            return

        if(index < bufferStart || index + size > bufferStart + bufferSize) {
            if (buffer.size < size) {
                buffer = ByteArray(size)
            }

            f.seek(index.toLong())
            bufferStart = index
            bufferSize = f.read(buffer)
        }
    }

    override fun getByte(index: Int): Byte {
        if (index < bufferStart || index >= bufferStart + bufferSize) {
            return 0
        }

        return buffer[index - bufferStart]
    }


}