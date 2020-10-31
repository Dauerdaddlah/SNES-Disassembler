package de.dde.snes.da

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.pow

class SNES {

    val wram = WRAM()

    lateinit var rom: ROM
    lateinit var sram: SRAM
    lateinit var mappingMode: MappingMode
        private set


    fun loadROM(file: Path) {
        val bytes = Files.readAllBytes(file)

        rom = ROM(bytes)

        setMappingMode(checkMappingMode(rom))
    }

    private fun checkMappingMode(rom: ROM): MappingMode {
        val modes = listOf(LoROM, HiROM, ExLoROM, ExHiROM)

        return modes.maxByOrNull { it.score(rom.bytes) }!!
    }

    fun setMappingMode(mappingMode: MappingMode) {
        this.mappingMode = mappingMode

        val header = mappingMode.readHeader(rom.bytes)

        sram = SRAM((2.0.pow(header.ramSize.toInt()) * _1K).toInt())
    }

    fun analyze() {
        mappingMode.annotateRom(rom)
        val state = SnesState(
            ProcessorMode.EMULATION,
        )
    }
}