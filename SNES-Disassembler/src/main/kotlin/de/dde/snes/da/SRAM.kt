package de.dde.snes.da

class SRAM(size: Int) {
    val bytes = IntArray(size) { 0x00 }
}