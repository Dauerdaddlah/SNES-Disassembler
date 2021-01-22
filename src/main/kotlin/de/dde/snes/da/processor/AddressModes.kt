package de.dde.snes.da.processor

import de.dde.snes.da.Byte
import de.dde.snes.da.memory.ROMByte
import de.dde.snes.da.memory.SNESState

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

sealed class AddressMode(
        val xRelative: Boolean = false,
        val yRelative: Boolean = false,
        val sRelative: Boolean = false,
        val x16Relative: Boolean = false,
        val m16Relative: Boolean = false,
        val indirect: Boolean = false,
        val indirectLong: Boolean = false,
        val immediate: Boolean = false
) {
    abstract fun neededBytes(state: SNESState): Int

    open fun format(operand: List<ROMByte>): String {
        if (operand.isEmpty())
            return ""

        var address = 0
        for (op in operand)
            address = (address shl 8) or Byte(op.b)

        var s = "$%0${2 * operand.size}X".format(address)

        if (immediate)
            return "#$s"

        if (xRelative)
            s = "$s,x"
        else if (sRelative)
            s = "$s,s"

        if (indirectLong)
            s = "[$s]"
        else if (indirect)
            s = "($s)"

        if (yRelative)
            s = "$s,y"

        return s
    }
}

open class AddressModeSimple(
        val bytesNeeded: Int,
        xRelative: Boolean = false,
        yRelative: Boolean = false,
        sRelative: Boolean = false,
        x16Relative: Boolean = false,
        m16Relative: Boolean = false,
        indirect: Boolean = false,
        indirectLong: Boolean = false,
        immediate: Boolean = false
) : AddressMode(
        xRelative,
        yRelative,
        sRelative,
        x16Relative,
        m16Relative,
        indirect,
        indirectLong,
        immediate,
) {
    override fun neededBytes(state: SNESState): Int {
        return bytesNeeded
    }
}

/** # */
object ImmediateAccumulator : AddressMode(immediate = true) {
    override fun neededBytes(state: SNESState): Int {
        return if (state.m16) 2 else 1
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
object ImmediateIndex : AddressMode(immediate = true) {
    override fun neededBytes(state: SNESState): Int {
        return if (state.x16) 2 else 1
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
val Immediate8 = AddressModeSimple(1, immediate = true)

/** i */
val Implied = AddressModeSimple(0)

/** r */
val ProgramCounterRelative = AddressModeSimple(1)

/** rl */
val ProgramCounterRelativeLong = AddressModeSimple(2)

/** d */
val Direct = AddressModeSimple(1)

/** d,x */
val DirectIndexedX = object : AddressModeSimple(1, xRelative = true) {}

/** d,y */
val DirectIndexedY = object : AddressModeSimple(1, yRelative = true) {}

/** (d) - several, like ORA, AND, CMP, ... */
val DirectIndirect = object : AddressModeSimple(1, indirect = true) {}

/** (d,x) - several, like ORA, AND, CMP, ... */
val DirectIndexedXIndirect = object : AddressModeSimple(1, xRelative = true, indirect = true) {}

/** (d),y - several, like ORA, AND, CMP, ... */
val DirectIndirectIndexedY = object : AddressModeSimple(1, yRelative = true, indirect = true) {}
/** [d] - several, like ORA, AND, CMP, ... */
val DirectIndirectLong = object : AddressModeSimple(1, indirectLong = true) {}

/** [d],y - several, like ORA, AND, CMP, ... */
val DirectIndirectIndexedYLong = object : AddressModeSimple(1, yRelative = true, indirectLong = true) {}

/** a */
val Absolute = AddressModeSimple(2)

/** a,x */
val AbsoluteIndexedX = object : AddressModeSimple(2, xRelative = true) {}

/** a,y */
val AbsoluteIndexedY = object : AddressModeSimple(2, yRelative = true) {}

/** al */
val AbsoluteLong = AddressModeSimple(3)

/** al,x */
val AbsoluteIndexedLong = object : AddressModeSimple(3, xRelative = true) {}

/** s */
val Stack = AddressModeSimple(0)

/** d,s */
val StackRelative = object : AddressModeSimple(1, sRelative = true) {}

/** (d,s),y - several, like ORA, AND, CMP, ... */
val StackRelativeIndirectIndexedY = object : AddressModeSimple(1, yRelative = true, sRelative = true, indirect = true) {}

/** (a) - JMP */
val AbsoluteIndirect = object : AddressModeSimple(2, indirect = true) {}

/** [al] - JML */
val AbsoluteIndirectLong = object : AddressModeSimple(3, indirectLong = true) {}

/** (a,x) - JMP, JSR */
val AbsoluteIndexedIndirect = object : AddressModeSimple(2, xRelative = true, indirect = true) {}

/** A */
val Accumulator = AddressModeSimple(0)

/** xyc */
val BlockMove = object : AddressModeSimple(2) {
    override fun format(operand: List<ROMByte>): String {
        return "%02X,%02X".format(operand[1].b, operand[0].b)
    }
}

