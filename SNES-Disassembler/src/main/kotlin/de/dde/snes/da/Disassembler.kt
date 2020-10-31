package de.dde.snes.da

import java.nio.file.Paths

fun main() {
    val insts = loadInsts()

    val snes = SNES()
    snes.loadROM(Paths.get(Inst::class.java.classLoader?.getResource("Legend of Zelda, The - A Link to the Past (Germany).sfc")?.toURI()!!))
}

