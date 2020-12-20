package de.dde.snes.da

import de.dde.snes.da.processor.Inst
import de.dde.snes.da.processor.Processor
import de.dde.snes.da.processor.loadInsts
import java.nio.file.Paths

fun main() {
    val insts = loadInsts()

    val snes = SNES()
    //snes.loadROM(Paths.get(Inst::class.java.classLoader?.getResource("Legend of Zelda, The - A Link to the Past (Germany).sfc")?.toURI()!!))
}

