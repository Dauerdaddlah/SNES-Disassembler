package de.dde.snes.da.processor

import de.dde.snes.da.memory.ROMByte
import java.lang.StringBuilder

val allAddressModes: List<AddressMode>
    get() = listOf(
            ImmediateAccumulator,
            ImmediateIndex,
            BrkCop,
            Immediate8,
            Implied,
            ProgramCounterRelative,
            ProgramCounterRelativeLong,
            Direct,
            DirectIndexedX,
            DirectIndexedY,
            DirectIndirect,
            DirectIndexedXIndirect,
            DirectIndirectIndexedY,
            DirectIndirectLong,
            DirectIndirectIndexedYLong,
            Absolute,
            AbsoluteIndexedX,
            AbsoluteIndexedY,
            AbsoluteLong,
            AbsoluteIndexedLong,
            Stack,
            StackRelative,
            StackRelativeIndirectIndexedY,
            AbsoluteIndirect,
            AbsoluteIndirectLong,
            AbsoluteIndexedIndirect,
            Accumulator,
            BlockMove
    )

sealed class AddressMode {
    abstract fun neededBytes(m16: Boolean, i16: Boolean): Int

    abstract fun format(operand: List<ROMByte>): String
}

open class AddressModeSimple(val bytesNeeded: Int) : AddressMode() {
    override fun neededBytes(m16: Boolean, i16: Boolean): Int {
        return bytesNeeded
    }

    override fun format(operand: List<ROMByte>): String {
        var s = ""
        for (o in operand) {
            s = "%02X%s".format(o.b, s)
        }

        return "$$s"
    }
}

/** # */
object ImmediateAccumulator : AddressMode() {
    override fun neededBytes(m16: Boolean, i16: Boolean): Int {
        return if (m16) 2 else 1
    }

    override fun format(operand: List<ROMByte>): String {
        var s = ""
        for (o in operand) {
            s = "%02X%s".format(o.b, s)
        }

        return "#$$s"
    }
}
/** # */
object ImmediateIndex : AddressMode() {
    override fun neededBytes(m16: Boolean, i16: Boolean): Int {
        return if (i16) 2 else 1
    }

    override fun format(operand: List<ROMByte>): String {
        var s = ""
        for (o in operand) {
            s = "%02X%s".format(o.b, s)
        }

        return "#$$s"
    }
}

/** s */
val BrkCop = AddressModeSimple(1)

/** # */
val Immediate8 = AddressModeSimple(1)

/** i */
val Implied = AddressModeSimple(0)

/** r */
val ProgramCounterRelative = AddressModeSimple(1)

/** rl */
val ProgramCounterRelativeLong = AddressModeSimple(2)

/** d */
val Direct = AddressModeSimple(1)

/** d,x */
val DirectIndexedX = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "$s,X"
    }
}

/** d,y */
val DirectIndexedY = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)
        return "$s,Y"
    }
}

/** (d) - several, like ORA, AND, CMP, ... */
val DirectIndirect = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "($s)"
    }
}

/** (d,x) - several, like ORA, AND, CMP, ... */
val DirectIndexedXIndirect = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "($s,X)"
    }
}

/** (d),y - several, like ORA, AND, CMP, ... */
val DirectIndirectIndexedY = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "($s),Y"
    }
}
/** [d] - several, like ORA, AND, CMP, ... */
val DirectIndirectLong = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "[$s]"
    }
}

/** [d],y - several, like ORA, AND, CMP, ... */
val DirectIndirectIndexedYLong = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "[$s],Y"
    }
}

/** a */
val Absolute = AddressModeSimple(2)

/** a,x */
val AbsoluteIndexedX = object : AddressModeSimple(2) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "$s,X"
    }
}

/** a,y */
val AbsoluteIndexedY = object : AddressModeSimple(2) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "$s,Y"
    }
}

/** al */
val AbsoluteLong = AddressModeSimple(3)

/** al,x */
val AbsoluteIndexedLong = object : AddressModeSimple(3) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "$s,X"
    }
}

/** s */
val Stack = AddressModeSimple(0)

/** d,s */
val StackRelative = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "$s,S"
    }
}

/** (d,s),y - several, like ORA, AND, CMP, ... */
val StackRelativeIndirectIndexedY = object : AddressModeSimple(1) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "($s,S),Y"
    }
}

/** (a) - JMP */
val AbsoluteIndirect = object : AddressModeSimple(2) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "($s)"
    }
}

/** [al] - JML */
val AbsoluteIndirectLong = object : AddressModeSimple(3) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "[$s]"
    }
}

/** (a,x) - JMP, JSR */
val AbsoluteIndexedIndirect = object : AddressModeSimple(2) {
    override fun format(operand: List<ROMByte>): String {
        val s = super.format(operand)

        return "($s,X)"
    }
}

/** A */
val Accumulator = AddressModeSimple(0)

/** xyc */
val BlockMove = object : AddressModeSimple(2) {
    override fun format(operand: List<ROMByte>): String {
        return "%02X,%02X".format(operand[1].b, operand[0].b)
    }
}

