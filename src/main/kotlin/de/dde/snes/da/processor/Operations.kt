package de.dde.snes.da.processor

import de.dde.snes.da.ProcessorMode
import de.dde.snes.da.SNES

val allOperations: List<Operation>
    get() = listOf(
            ADC,
            AND,
            ASL,
            BCC,
            BCS,
            BEQ,
            BIT,
            BMI,
            BNE,
            BPL,
            BRA,
            BRK,
            BRL,
            BVC,
            BVS,
            CLC,
            CLD,
            CLI,
            CLV,
            CMP,
            COP,
            CPX,
            CPY,
            DEC,
            DEX,
            DEY,
            EOR,
            INC,
            INX,
            INY,
            JML,
            JMP,
            JSL,
            JSR,
            LDA,
            LDX,
            LDY,
            LSR,
            MVN,
            MVP,
            NOP,
            ORA,
            PEA,
            PEI,
            PER,
            PHA,
            PHB,
            PHD,
            PHK,
            PHP,
            PHX,
            PHY,
            PLA,
            PLB,
            PLD,
            PLP,
            PLX,
            PLY,
            REP,
            ROL,
            ROR,
            RTI,
            RTL,
            RTS,
            SBC,
            SEP,
            SEC,
            SED,
            SEI,
            STA,
            STP,
            STX,
            STY,
            STZ,
            TAX,
            TAY,
            TCD,
            TCS,
            TDC,
            TRB,
            TSB,
            TSC,
            TSX,
            TXA,
            TXS,
            TXY,
            TYA,
            TYX,
            WAI,
            WDM,
            XBA,
            XCE
    )

sealed class Operation(val symbol: String) {
    open fun process(snes: SNES, processor: Processor, param: Int) {
    }
}

object ADC : Operation("ADC")
object AND : Operation("AND")
object ASL : Operation("ASL")
object BCC : Operation("BCC")
object BCS : Operation("BCS")
object BEQ : Operation("BEQ")
object BIT : Operation("BIT")
object BMI : Operation("BMI")
object BNE : Operation("BNE")
object BPL : Operation("BPL")
object BRA : Operation("BRA")
object BRK : Operation("BRK")
object BRL : Operation("BRL")
object BVC : Operation("BVC")
object BVS : Operation("BVS")
object CLC : Operation("CLC")
object CLD : Operation("CLD")
object CLI : Operation("CLI")
object CLV : Operation("CLV")
object CMP : Operation("CMP")
object COP : Operation("COP")
object CPX : Operation("CPX")
object CPY : Operation("CPY")
object DEC : Operation("DEC")
object DEX : Operation("DEX")
object DEY : Operation("DEY")
object EOR : Operation("EOR")
object INC : Operation("INC")
object INX : Operation("INX")
object INY : Operation("INY")
object JML : Operation("JML")
object JMP : Operation("JMP")
object JSL : Operation("JSL")
object JSR : Operation("JSR")
object LDA : Operation("LDA")
object LDX : Operation("LDX")
object LDY : Operation("LDY")
object LSR : Operation("LSR")
object MVN : Operation("MVN")
object MVP : Operation("MVP")
object NOP : Operation("NOP")
object ORA : Operation("ORA")
object PEA : Operation("PEA")
object PEI : Operation("PEI")
object PER : Operation("PER")
object PHA : Operation("PHA")
object PHB : Operation("PHB")
object PHD : Operation("PHD")
object PHK : Operation("PHK")
object PHP : Operation("PHP")
object PHX : Operation("PHX")
object PHY : Operation("PHY")
object PLA : Operation("PLA")
object PLB : Operation("PLB")
object PLD : Operation("PLD")
object PLP : Operation("PLP")
object PLX : Operation("PLX")
object PLY : Operation("PLY")
object REP : Operation("REP")
object ROL : Operation("ROL")
object ROR : Operation("ROR")
object RTI : Operation("RTI")
object RTL : Operation("RTL")
object RTS : Operation("RTS")
object SBC : Operation("SBC")
object SEP : Operation("SEP")
object SEC : Operation("SEC")
object SED : Operation("SED")
object SEI : Operation("SEI")
object STA : Operation("STA")
object STP : Operation("STP")
object STX : Operation("STX")
object STY : Operation("STY")
object STZ : Operation("STZ")
object TAX : Operation("TAX")
object TAY : Operation("TAY")
object TCD : Operation("TCD")
object TCS : Operation("TCS")
object TDC : Operation("TDC")
object TRB : Operation("TRB")
object TSB : Operation("TSB")
object TSC : Operation("TSC")
object TSX : Operation("TSX")
object TXA : Operation("TXA")
object TXS : Operation("TXS")
object TXY : Operation("TXY")
object TYA : Operation("TYA") {

}


object TYX : Operation("TYX") {
    override fun process(snes: SNES, processor: Processor, param: Int) {
        val y = processor.y.value
        processor.y.value = processor.x.value
        processor.x.value = y
    }
}

object WAI : Operation("WAI")
object WDM : Operation("WDM")

object XBA : Operation("XBA") {
    override fun process(snes: SNES, processor: Processor, param: Int) {
        processor.a.xba()
    }
}

object XCE : Operation("XCE") {
    override fun process(snes: SNES, processor: Processor, param: Int) {
        val c = processor.p.carry
        processor.p.carry = processor.mode.asBoolean()
        processor.mode = when (c) {
            null -> ProcessorMode.UNKNOWN
            true -> ProcessorMode.EMULATION
            else -> ProcessorMode.NATIVE
        }

        processor.checkSizes()
    }
}
