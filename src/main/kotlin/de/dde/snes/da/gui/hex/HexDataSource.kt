package de.dde.snes.da.gui.hex

interface HexDataSource {
    val size: Int
    fun ensure(index: Int, size: Int)
    fun getByte(index: Int): Byte
}