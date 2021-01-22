package de.dde.snes.da.processor

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

sealed class Operation(val symbol: String, val mRelative: Boolean = false, val xRelative: Boolean = false)

object ADC : Operation("ADC", mRelative = true)
object AND : Operation("AND", mRelative = true)
object ASL : Operation("ASL", mRelative = true)
object BCC : Operation("BCC")
object BCS : Operation("BCS")
object BEQ : Operation("BEQ")
object BIT : Operation("BIT", mRelative = true)
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
object CMP : Operation("CMP", mRelative = true)
object COP : Operation("COP")
object CPX : Operation("CPX", xRelative = true)
object CPY : Operation("CPY", xRelative = true)
object DEC : Operation("DEC", mRelative = true)
object DEX : Operation("DEX", xRelative = true)
object DEY : Operation("DEY", xRelative = true)
object EOR : Operation("EOR", mRelative = true)
object INC : Operation("INC", mRelative = true)
object INX : Operation("INX", xRelative = true)
object INY : Operation("INY", xRelative = true)
object JML : Operation("JML")
object JMP : Operation("JMP")
object JSL : Operation("JSL")
object JSR : Operation("JSR")
object LDA : Operation("LDA", mRelative = true)
object LDX : Operation("LDX", xRelative = true)
object LDY : Operation("LDY", xRelative = true)
object LSR : Operation("LSR", mRelative = true)
object MVN : Operation("MVN")
object MVP : Operation("MVP")
object NOP : Operation("NOP")
object ORA : Operation("ORA", mRelative = true)
object PEA : Operation("PEA")
object PEI : Operation("PEI")
object PER : Operation("PER")
object PHA : Operation("PHA", mRelative = true)
object PHB : Operation("PHB")
object PHD : Operation("PHD")
object PHK : Operation("PHK")
object PHP : Operation("PHP")
object PHX : Operation("PHX", xRelative = true)
object PHY : Operation("PHY", xRelative = true)
object PLA : Operation("PLA", mRelative = true)
object PLB : Operation("PLB")
object PLD : Operation("PLD")
object PLP : Operation("PLP")
object PLX : Operation("PLX", xRelative = true)
object PLY : Operation("PLY", xRelative = true)
object REP : Operation("REP")
object ROL : Operation("ROL", mRelative = true)
object ROR : Operation("ROR", mRelative = true)
object RTI : Operation("RTI")
object RTL : Operation("RTL")
object RTS : Operation("RTS")
object SBC : Operation("SBC", mRelative = true)
object SEP : Operation("SEP")
object SEC : Operation("SEC")
object SED : Operation("SED")
object SEI : Operation("SEI")
object STA : Operation("STA", mRelative = true)
object STP : Operation("STP")
object STX : Operation("STX", xRelative = true)
object STY : Operation("STY", xRelative = true)
object STZ : Operation("STZ", mRelative = true)
object TAX : Operation("TAX", xRelative = true)
object TAY : Operation("TAY", xRelative = true)
object TCD : Operation("TCD")
object TCS : Operation("TCS")
object TDC : Operation("TDC")
object TRB : Operation("TRB", mRelative = true)
object TSB : Operation("TSB", mRelative = true)
object TSC : Operation("TSC")
object TSX : Operation("TSX", xRelative = true)
object TXA : Operation("TXA", mRelative = true)
object TXS : Operation("TXS")
object TXY : Operation("TXY", xRelative = true)
object TYA : Operation("TYA", mRelative = true)


object TYX : Operation("TYX")

object WAI : Operation("WAI")
object WDM : Operation("WDM")

object XBA : Operation("XBA")

object XCE : Operation("XCE")
