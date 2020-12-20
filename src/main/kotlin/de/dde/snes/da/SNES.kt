package de.dde.snes.da

import de.dde.snes.da.processor.Processor
import de.dde.snes.da.processor.instruction
import de.dde.snes.da.rom.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.pow

class SNES {

    val processor = Processor()
    val wram = WRAM()

    lateinit var rom: ROM
    lateinit var sram: SRAM
    lateinit var mappingMode: MappingMode
        private set

    init {
        processor.a.value = 0
        processor.pbr.value = 0
        processor.dbr.value = 0
        processor.x.value = 0
        processor.y.value = 0
        processor.d.value = 0
        processor.s.value = 0x1FF
        processor.p.value = 0
        processor.p.irq = true
        processor.p.index = true
        processor.p.memory = true
        processor.checkSizes()
    }


    fun loadROM(romFile: ROMFile) {
        rom = ROM(romFile)
        sram = SRAM((2.0.pow(romFile.snesHeader?.ramSize?.toInt()?: 0) * _1K).toInt())
    }

    fun analyze() {
        processor.pc.value = rom.header?.nativeVectors?.reset!!

        val states = mutableListOf(processor.state())

        // TODO move this out of MappingMode
        mappingMode.annotateRom(rom)

        while (states.isNotEmpty()) {
            val state = states.first()
            processor.load(state)

            val add = mappingMode.getMemoryAddress(processor.pbr.value, processor.pc.value)

            if (add.area != MemoryArea.ROM)
                error("non-rom-area used as instruction")

            val meaning = rom.byteMeanings[add.index]
            if (meaning == null) {
                val m = Meaning(MeaningType.OPERATION)
                rom.byteMeanings[add.index] = m
                val inst = instruction(rom.bytes[add.index])

                processor.pc.value++

                var param = 0

                repeat(inst.addressMode.neededBytes(processor)) {
                    val address = mappingMode.getMemoryAddress(processor.pbr.value, processor.pc.value)
                    processor.pc.value++

                    rom.byteMeanings[address.index] = Meaning(MeaningType.PARAM)

                    param = (param shl 8) or Byte(rom.bytes[address.index])
                }

                inst.operation.process(this, processor, param)

            } else if (meaning.type == MeaningType.OPERATION) {
                continue
            } else {
                error("rom-byte already used as ${meaning.type}")
            }
        }
    }
}