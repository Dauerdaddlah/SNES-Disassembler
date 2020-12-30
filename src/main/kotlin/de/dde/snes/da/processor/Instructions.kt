package de.dde.snes.da.processor

import de.dde.snes.da.Byte
import de.dde.snes.da.Long
import de.dde.snes.da.longByte
import de.dde.snes.da.memory.ROMByte
import de.dde.snes.da.project.Project
import java.nio.file.Files
import java.nio.file.Paths

fun loadInsts(): List<Inst> {
    val file = Paths.get(Inst::class.java.classLoader?.getResource("CPU_Instructions.txt")?.toURI()!!)

    val line = Files.readAllLines(file)

    val insts = line.subList(1, (0xFF + 2)).map {
        val split = it.split('\t').map { it.trim() }

        val symbol = split[0].substring(0, 3)
        val alias = if (split[1].isEmpty()) null else split[2]
        val description = split[2]
        val opcode = split[3].toInt(16)
        val addressMode = split[4].let { am ->
            if (am.startsWith("Stack (") || am.endsWith("Interrupt"))
                "Stack"
            else if (am.startsWith("SR"))
                am.replace("SR", "Stack Relative")
            else if (am.startsWith("DP"))
                am.replace("DP", "Direct Page")
            else am
        }
        val flags = split[5].filter { it != '-' }.toSet()
        val (bytes, bytesFlags) = split[6].let { b ->
            if (!b.contains('['))
                b.toInt() to emptySet<Int>()
            else {
                val f = mutableSetOf<Int>()

                val byt = if (b.contains('[')) b.substring(0, b.indexOf('[')).toInt() else b.toInt()

                var s = b

                while (s.contains('[')) {
                    val i = s.indexOf('^')
                    val j = s.indexOf(']')

                    f.add(s.substring(i + 1, j).toInt())

                    s = s.substring(j + 1)
                }

                byt to f
            }
        }
        val (cycles, cyclesFlags) = split[7].let { b ->
            if (!b.contains('['))
                b.toInt() to emptySet<Int>()
            else {
                val f = mutableSetOf<Int>()

                val byt = if (b.contains('[')) b.substring(0, b.indexOf('[')).toInt() else b.toInt()

                var s = b

                while (s.contains('[')) {
                    val i = s.indexOf('^')
                    val j = s.indexOf(']')

                    f.add(s.substring(i + 1, j).toInt())

                    s = s.substring(j + 1)
                }

                byt to f
            }
        }

        Inst(symbol, alias, description, opcode, addressMode, flags, bytes, bytesFlags, cycles, cyclesFlags)
    }

    return insts.sortedBy { it.opcode }
}

data class Inst(
    val symbol: String,
    val alias: String?,
    val description: String,
    val opcode: Int,
    val addressMode: String,
    val flags: Set<Char>,
    val bytes: Int,
    val bytesFlags: Set<Int>,
    val cycles: Int,
    val cyclesFlags: Set<Int>
)

fun main() {
    val insts = loadInsts()

    insts.sortedBy { it.opcode }.forEach {
        val mode = when (it.addressMode) {
            "Absolute" -> "Absolute"
            "Absolute Indexed Indirect" -> "AbsoluteIndexedIndirect"
            "Absolute Indexed,X" -> "AbsoluteIndexedX"
            "Absolute Indexed,Y" -> "AbsoluteIndexedY"
            "Absolute Indirect" -> "AbsoluteIndirect"
            "Absolute Indirect Long" -> "AbsoluteIndirectLong"
            "Absolute Long" -> "AbsoluteLong"
            "Absolute Long Indexed,X" -> "AbsoluteIndexedLong"
            "Accumulator" -> "Accumulator"
            "Block Move" -> "BlockMove"
            "Direct Page" -> "Direct"
            "Direct Page Indexed Indirect,X" -> "DirectIndexedXIndirect"
            "Direct Page Indexed,X" -> "DirectIndexedX"
            "Direct Page Indexed,Y" -> "DirectIndexedY"
            "Direct Page Indirect" -> "DirectIndirect"
            "Direct Page Indirect Indexed, Y" -> "DirectIndirectIndexedY"
            "Direct Page Indirect Long" -> "DirectIndirectLong"
            "Direct Page Indirect Long Indexed, Y" -> "DirectIndirectIndexedYLong"
            "Immediate" -> {
                if (it.bytesFlags.contains(12)) "ImmediateAccumulator"
                else if (it.bytesFlags.contains(14)) "ImmediateIndex"
                else "Immediate8"
            }
            "",
            "Implied" -> "Implied"
            "Program Counter Relative" -> "ProgramCounterRelative"
            "Program Counter Relative Long" -> "ProgramCounterRelativeLong"
            "Stack" -> "Stack"
            "Stack Relative" -> "StackRelative"
            "Stack Relative Indirect Indexed,Y" ->"StackRelativeIndirectIndexedY"
            else -> error(it.toString())
        }

        println("/** ${"0x%02X".format(it.opcode)} */ Instruction(${it.symbol}, $mode),")
    }

    //insts.groupBy { it.addressMode }.toSortedMap().forEach { (am, codes) -> println("$am - ${codes.sortedBy { it.symbol }.joinToString { it.symbol }}") }
    //insts.groupBy { it.symbol }.toSortedMap().forEach { (oc, modes) -> println("$oc - ${modes.sortedBy { it.addressMode }.joinToString { it.addressMode }}") }
    insts.groupBy { it.symbol }.toSortedMap().forEach { (oc, modes) -> println("$oc - ${modes.map { it.flags }.distinct()[0]}") }
}

data class Instruction(
        val operation: Operation,
        val addressMode: AddressMode
) {
    fun getOperandBytes(project: Project, byte: ROMByte): List<ROMByte> {
        return when (val b = addressMode.neededBytes(byte.state.memory, byte.state.index)) {
            0 -> emptyList()
            else -> {
                val iSnesStart = project.mappingMode.toSnesAddress(byte.index + 1)
                val iSnesEnd = iSnesStart + b - 1

                if (iSnesStart.longByte() == iSnesEnd.longByte()) {
                    val memoryArea = project.mappingMode.getMemoryAddress(iSnesStart)

                    return project.romBytes.subList(memoryArea.index, memoryArea.index + b)
                } else {
                    val ret = mutableListOf<ROMByte>()

                    var index = iSnesStart

                    repeat(b) {
                        index = Long(index + 1, iSnesStart.longByte())
                        val memoryArea = project.mappingMode.getMemoryAddress(iSnesStart)

                        ret.add(project.romBytes[memoryArea.index])
                    }

                    ret
                }
            }
        }
    }
}

fun instruction(opCode: Byte) = allInstructions[Byte(opCode)]

val allInstructions = arrayOf(
        /** 0x00 */ Instruction(BRK, BrkCop),
        /** 0x01 */ Instruction(ORA, DirectIndexedXIndirect),
        /** 0x02 */ Instruction(COP, BrkCop),
        /** 0x03 */ Instruction(ORA, StackRelative),
        /** 0x04 */ Instruction(TSB, Direct),
        /** 0x05 */ Instruction(ORA, Direct),
        /** 0x06 */ Instruction(ASL, Direct),
        /** 0x07 */ Instruction(ORA, DirectIndirectLong),
        /** 0x08 */ Instruction(PHP, Stack),
        /** 0x09 */ Instruction(ORA, ImmediateAccumulator),
        /** 0x0A */ Instruction(ASL, Accumulator),
        /** 0x0B */ Instruction(PHD, Stack),
        /** 0x0C */ Instruction(TSB, Absolute),
        /** 0x0D */ Instruction(ORA, Absolute),
        /** 0x0E */ Instruction(ASL, Absolute),
        /** 0x0F */ Instruction(ORA, AbsoluteLong),
        /** 0x10 */ Instruction(BPL, ProgramCounterRelative),
        /** 0x11 */ Instruction(ORA, DirectIndirectIndexedY),
        /** 0x12 */ Instruction(ORA, DirectIndirect),
        /** 0x13 */ Instruction(ORA, StackRelativeIndirectIndexedY),
        /** 0x14 */ Instruction(TRB, Direct),
        /** 0x15 */ Instruction(ORA, DirectIndexedX),
        /** 0x16 */ Instruction(ASL, DirectIndexedX),
        /** 0x17 */ Instruction(ORA, DirectIndirectIndexedYLong),
        /** 0x18 */ Instruction(CLC, Implied),
        /** 0x19 */ Instruction(ORA, AbsoluteIndexedY),
        /** 0x1A */ Instruction(INC, Accumulator),
        /** 0x1B */ Instruction(TCS, Implied),
        /** 0x1C */ Instruction(TRB, Absolute),
        /** 0x1D */ Instruction(ORA, AbsoluteIndexedX),
        /** 0x1E */ Instruction(ASL, AbsoluteIndexedX),
        /** 0x1F */ Instruction(ORA, AbsoluteIndexedLong),
        /** 0x20 */ Instruction(JSR, Absolute),
        /** 0x21 */ Instruction(AND, DirectIndexedXIndirect),
        /** 0x22 */ Instruction(JSR, AbsoluteLong),
        /** 0x23 */ Instruction(AND, StackRelative),
        /** 0x24 */ Instruction(BIT, Direct),
        /** 0x25 */ Instruction(AND, Direct),
        /** 0x26 */ Instruction(ROL, Direct),
        /** 0x27 */ Instruction(AND, DirectIndirectLong),
        /** 0x28 */ Instruction(PLP, Stack),
        /** 0x29 */ Instruction(AND, ImmediateAccumulator),
        /** 0x2A */ Instruction(ROL, Accumulator),
        /** 0x2B */ Instruction(PLD, Stack),
        /** 0x2C */ Instruction(BIT, Absolute),
        /** 0x2D */ Instruction(AND, Absolute),
        /** 0x2E */ Instruction(ROL, Absolute),
        /** 0x2F */ Instruction(AND, AbsoluteLong),
        /** 0x30 */ Instruction(BMI, ProgramCounterRelative),
        /** 0x31 */ Instruction(AND, DirectIndirectIndexedY),
        /** 0x32 */ Instruction(AND, DirectIndirect),
        /** 0x33 */ Instruction(AND, StackRelativeIndirectIndexedY),
        /** 0x34 */ Instruction(BIT, DirectIndexedX),
        /** 0x35 */ Instruction(AND, DirectIndexedX),
        /** 0x36 */ Instruction(ROL, DirectIndexedX),
        /** 0x37 */ Instruction(AND, DirectIndirectIndexedYLong),
        /** 0x38 */ Instruction(SEC, Implied),
        /** 0x39 */ Instruction(AND, AbsoluteIndexedY),
        /** 0x3A */ Instruction(DEC, Accumulator),
        /** 0x3B */ Instruction(TSC, Implied),
        /** 0x3C */ Instruction(BIT, AbsoluteIndexedX),
        /** 0x3D */ Instruction(AND, AbsoluteIndexedX),
        /** 0x3E */ Instruction(ROL, AbsoluteIndexedX),
        /** 0x3F */ Instruction(AND, AbsoluteIndexedLong),
        /** 0x40 */ Instruction(RTI, Stack),
        /** 0x41 */ Instruction(EOR, DirectIndexedXIndirect),
        /** 0x42 */ Instruction(WDM, Implied),
        /** 0x43 */ Instruction(EOR, StackRelative),
        /** 0x44 */ Instruction(MVP, BlockMove),
        /** 0x45 */ Instruction(EOR, Direct),
        /** 0x46 */ Instruction(LSR, Direct),
        /** 0x47 */ Instruction(EOR, DirectIndirectLong),
        /** 0x48 */ Instruction(PHA, Stack),
        /** 0x49 */ Instruction(EOR, ImmediateAccumulator),
        /** 0x4A */ Instruction(LSR, Accumulator),
        /** 0x4B */ Instruction(PHK, Stack),
        /** 0x4C */ Instruction(JMP, Absolute),
        /** 0x4D */ Instruction(EOR, Absolute),
        /** 0x4E */ Instruction(LSR, Absolute),
        /** 0x4F */ Instruction(EOR, AbsoluteLong),
        /** 0x50 */ Instruction(BVC, ProgramCounterRelative),
        /** 0x51 */ Instruction(EOR, DirectIndirectIndexedY),
        /** 0x52 */ Instruction(EOR, DirectIndirect),
        /** 0x53 */ Instruction(EOR, StackRelativeIndirectIndexedY),
        /** 0x54 */ Instruction(MVN, BlockMove),
        /** 0x55 */ Instruction(EOR, DirectIndexedX),
        /** 0x56 */ Instruction(LSR, DirectIndexedX),
        /** 0x57 */ Instruction(EOR, DirectIndirectIndexedYLong),
        /** 0x58 */ Instruction(CLI, Implied),
        /** 0x59 */ Instruction(EOR, AbsoluteIndexedY),
        /** 0x5A */ Instruction(PHY, Stack),
        /** 0x5B */ Instruction(TCD, Implied),
        /** 0x5C */ Instruction(JMP, AbsoluteLong),
        /** 0x5D */ Instruction(EOR, AbsoluteIndexedX),
        /** 0x5E */ Instruction(LSR, AbsoluteIndexedX),
        /** 0x5F */ Instruction(EOR, AbsoluteIndexedLong),
        /** 0x60 */ Instruction(RTS, Stack),
        /** 0x61 */ Instruction(ADC, DirectIndexedXIndirect),
        /** 0x62 */ Instruction(PER, Stack),
        /** 0x63 */ Instruction(ADC, StackRelative),
        /** 0x64 */ Instruction(STZ, Direct),
        /** 0x65 */ Instruction(ADC, Direct),
        /** 0x66 */ Instruction(ROR, Direct),
        /** 0x67 */ Instruction(ADC, DirectIndirectLong),
        /** 0x68 */ Instruction(PLA, Stack),
        /** 0x69 */ Instruction(ADC, ImmediateAccumulator),
        /** 0x6A */ Instruction(ROR, Accumulator),
        /** 0x6B */ Instruction(RTL, Stack),
        /** 0x6C */ Instruction(JMP, AbsoluteIndirect),
        /** 0x6D */ Instruction(ADC, Absolute),
        /** 0x6E */ Instruction(ROR, Absolute),
        /** 0x6F */ Instruction(ADC, AbsoluteLong),
        /** 0x70 */ Instruction(BVS, ProgramCounterRelative),
        /** 0x71 */ Instruction(ADC, DirectIndirectIndexedY),
        /** 0x72 */ Instruction(ADC, DirectIndirect),
        /** 0x73 */ Instruction(ADC, StackRelativeIndirectIndexedY),
        /** 0x74 */ Instruction(STZ, DirectIndexedX),
        /** 0x75 */ Instruction(ADC, DirectIndexedX),
        /** 0x76 */ Instruction(ROR, DirectIndexedX),
        /** 0x77 */ Instruction(ADC, DirectIndirectIndexedYLong),
        /** 0x78 */ Instruction(SEI, Implied),
        /** 0x79 */ Instruction(ADC, AbsoluteIndexedY),
        /** 0x7A */ Instruction(PLY, Stack),
        /** 0x7B */ Instruction(TDC, Implied),
        /** 0x7C */ Instruction(JMP, AbsoluteIndexedIndirect),
        /** 0x7D */ Instruction(ADC, AbsoluteIndexedX),
        /** 0x7E */ Instruction(ROR, AbsoluteIndexedX),
        /** 0x7F */ Instruction(ADC, AbsoluteIndexedLong),
        /** 0x80 */ Instruction(BRA, ProgramCounterRelative),
        /** 0x81 */ Instruction(STA, DirectIndexedXIndirect),
        /** 0x82 */ Instruction(BRL, ProgramCounterRelativeLong),
        /** 0x83 */ Instruction(STA, StackRelative),
        /** 0x84 */ Instruction(STY, Direct),
        /** 0x85 */ Instruction(STA, Direct),
        /** 0x86 */ Instruction(STX, Direct),
        /** 0x87 */ Instruction(STA, DirectIndirectLong),
        /** 0x88 */ Instruction(DEY, Implied),
        /** 0x89 */ Instruction(BIT, ImmediateAccumulator),
        /** 0x8A */ Instruction(TXA, Implied),
        /** 0x8B */ Instruction(PHB, Stack),
        /** 0x8C */ Instruction(STY, Absolute),
        /** 0x8D */ Instruction(STA, Absolute),
        /** 0x8E */ Instruction(STX, Absolute),
        /** 0x8F */ Instruction(STA, AbsoluteLong),
        /** 0x90 */ Instruction(BCC, ProgramCounterRelative),
        /** 0x91 */ Instruction(STA, DirectIndirectIndexedY),
        /** 0x92 */ Instruction(STA, DirectIndirect),
        /** 0x93 */ Instruction(STA, StackRelativeIndirectIndexedY),
        /** 0x94 */ Instruction(STY, DirectIndexedX),
        /** 0x95 */ Instruction(STA, DirectIndexedX),
        /** 0x96 */ Instruction(STX, DirectIndexedY),
        /** 0x97 */ Instruction(STA, DirectIndirectIndexedYLong),
        /** 0x98 */ Instruction(TYA, Implied),
        /** 0x99 */ Instruction(STA, AbsoluteIndexedY),
        /** 0x9A */ Instruction(TXS, Implied),
        /** 0x9B */ Instruction(TXY, Implied),
        /** 0x9C */ Instruction(STZ, Absolute),
        /** 0x9D */ Instruction(STA, AbsoluteIndexedX),
        /** 0x9E */ Instruction(STZ, AbsoluteIndexedX),
        /** 0x9F */ Instruction(STA, AbsoluteIndexedLong),
        /** 0xA0 */ Instruction(LDY, ImmediateIndex),
        /** 0xA1 */ Instruction(LDA, DirectIndexedXIndirect),
        /** 0xA2 */ Instruction(LDX, ImmediateIndex),
        /** 0xA3 */ Instruction(LDA, StackRelative),
        /** 0xA4 */ Instruction(LDY, Direct),
        /** 0xA5 */ Instruction(LDA, Direct),
        /** 0xA6 */ Instruction(LDX, Direct),
        /** 0xA7 */ Instruction(LDA, DirectIndirectLong),
        /** 0xA8 */ Instruction(TAY, Implied),
        /** 0xA9 */ Instruction(LDA, ImmediateAccumulator),
        /** 0xAA */ Instruction(TAX, Implied),
        /** 0xAB */ Instruction(PLB, Stack),
        /** 0xAC */ Instruction(LDY, Absolute),
        /** 0xAD */ Instruction(LDA, Absolute),
        /** 0xAE */ Instruction(LDX, Absolute),
        /** 0xAF */ Instruction(LDA, AbsoluteLong),
        /** 0xB0 */ Instruction(BCS, ProgramCounterRelative),
        /** 0xB1 */ Instruction(LDA, DirectIndirectIndexedY),
        /** 0xB2 */ Instruction(LDA, DirectIndirect),
        /** 0xB3 */ Instruction(LDA, StackRelativeIndirectIndexedY),
        /** 0xB4 */ Instruction(LDY, DirectIndexedX),
        /** 0xB5 */ Instruction(LDA, DirectIndexedX),
        /** 0xB6 */ Instruction(LDX, DirectIndexedY),
        /** 0xB7 */ Instruction(LDA, DirectIndirectIndexedYLong),
        /** 0xB8 */ Instruction(CLV, Implied),
        /** 0xB9 */ Instruction(LDA, AbsoluteIndexedY),
        /** 0xBA */ Instruction(TSX, Implied),
        /** 0xBB */ Instruction(TYX, Implied),
        /** 0xBC */ Instruction(LDY, AbsoluteIndexedX),
        /** 0xBD */ Instruction(LDA, AbsoluteIndexedX),
        /** 0xBE */ Instruction(LDX, AbsoluteIndexedY),
        /** 0xBF */ Instruction(LDA, AbsoluteIndexedLong),
        /** 0xC0 */ Instruction(CPY, ImmediateIndex),
        /** 0xC1 */ Instruction(CMP, DirectIndexedXIndirect),
        /** 0xC2 */ Instruction(REP, Immediate8),
        /** 0xC3 */ Instruction(CMP, StackRelative),
        /** 0xC4 */ Instruction(CPY, Direct),
        /** 0xC5 */ Instruction(CMP, Direct),
        /** 0xC6 */ Instruction(DEC, Direct),
        /** 0xC7 */ Instruction(CMP, DirectIndirectLong),
        /** 0xC8 */ Instruction(INY, Implied),
        /** 0xC9 */ Instruction(CMP, ImmediateAccumulator),
        /** 0xCA */ Instruction(DEX, Implied),
        /** 0xCB */ Instruction(WAI, Implied),
        /** 0xCC */ Instruction(CPY, Absolute),
        /** 0xCD */ Instruction(CMP, Absolute),
        /** 0xCE */ Instruction(DEC, Absolute),
        /** 0xCF */ Instruction(CMP, AbsoluteLong),
        /** 0xD0 */ Instruction(BNE, ProgramCounterRelative),
        /** 0xD1 */ Instruction(CMP, DirectIndirectIndexedY),
        /** 0xD2 */ Instruction(CMP, DirectIndirect),
        /** 0xD3 */ Instruction(CMP, StackRelativeIndirectIndexedY),
        /** 0xD4 */ Instruction(PEI, Stack),
        /** 0xD5 */ Instruction(CMP, DirectIndexedX),
        /** 0xD6 */ Instruction(DEC, DirectIndexedX),
        /** 0xD7 */ Instruction(CMP, DirectIndirectIndexedYLong),
        /** 0xD8 */ Instruction(CLD, Implied),
        /** 0xD9 */ Instruction(CMP, AbsoluteIndexedY),
        /** 0xDA */ Instruction(PHX, Stack),
        /** 0xDB */ Instruction(STP, Implied),
        /** 0xDC */ Instruction(JMP, AbsoluteIndirectLong),
        /** 0xDD */ Instruction(CMP, AbsoluteIndexedX),
        /** 0xDE */ Instruction(DEC, AbsoluteIndexedX),
        /** 0xDF */ Instruction(CMP, AbsoluteIndexedLong),
        /** 0xE0 */ Instruction(CPX, ImmediateIndex),
        /** 0xE1 */ Instruction(SBC, DirectIndexedXIndirect),
        /** 0xE2 */ Instruction(SEP, Immediate8),
        /** 0xE3 */ Instruction(SBC, StackRelative),
        /** 0xE4 */ Instruction(CPX, Direct),
        /** 0xE5 */ Instruction(SBC, Direct),
        /** 0xE6 */ Instruction(INC, Direct),
        /** 0xE7 */ Instruction(SBC, DirectIndirectLong),
        /** 0xE8 */ Instruction(INX, Implied),
        /** 0xE9 */ Instruction(SBC, ImmediateAccumulator),
        /** 0xEA */ Instruction(NOP, Implied),
        /** 0xEB */ Instruction(XBA, Implied),
        /** 0xEC */ Instruction(CPX, Absolute),
        /** 0xED */ Instruction(SBC, Absolute),
        /** 0xEE */ Instruction(INC, Absolute),
        /** 0xEF */ Instruction(SBC, AbsoluteLong),
        /** 0xF0 */ Instruction(BEQ, ProgramCounterRelative),
        /** 0xF1 */ Instruction(SBC, DirectIndirectIndexedY),
        /** 0xF2 */ Instruction(SBC, DirectIndirect),
        /** 0xF3 */ Instruction(SBC, StackRelativeIndirectIndexedY),
        /** 0xF4 */ Instruction(PEA, Stack),
        /** 0xF5 */ Instruction(SBC, DirectIndexedX),
        /** 0xF6 */ Instruction(INC, DirectIndexedX),
        /** 0xF7 */ Instruction(SBC, DirectIndirectIndexedYLong),
        /** 0xF8 */ Instruction(SED, Implied),
        /** 0xF9 */ Instruction(SBC, AbsoluteIndexedY),
        /** 0xFA */ Instruction(PLX, Stack),
        /** 0xFB */ Instruction(XCE, Implied),
        /** 0xFC */ Instruction(JSR, AbsoluteIndexedIndirect),
        /** 0xFD */ Instruction(SBC, AbsoluteIndexedX),
        /** 0xFE */ Instruction(INC, AbsoluteIndexedX),
        /** 0xFF */ Instruction(SBC, AbsoluteIndexedLong),
)