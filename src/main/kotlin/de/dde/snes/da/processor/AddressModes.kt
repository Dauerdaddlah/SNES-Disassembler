package de.dde.snes.da.processor

val allAddressModes: List<AddressMode>
    get() = listOf(
            ImmediateAccumulator,
            ImmediateIndex,
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
    abstract fun neededBytes(processor: Processor): Int
}

class AddressModeSimple(val bytesNeeded: Int) : AddressMode() {
    override fun neededBytes(processor: Processor): Int {
        return bytesNeeded
    }
}

/** # */
object ImmediateAccumulator : AddressMode() {
    override fun neededBytes(processor: Processor): Int {
        return if (processor.a.size16.isTrue()) 2 else 1
    }
}
/** # */
object ImmediateIndex : AddressMode() {
    override fun neededBytes(processor: Processor): Int {
        return if (processor.x.size16.isTrue()) 2 else 1
    }
}
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
val DirectIndexedX = AddressModeSimple(1)
/** d,y */
val DirectIndexedY = AddressModeSimple(1)
/** (d) - several, like ORA, AND, CMP, ... */
val DirectIndirect = AddressModeSimple(1)
/** (d,x) - several, like ORA, AND, CMP, ... */
val DirectIndexedXIndirect = AddressModeSimple(1)
/** (d),y - several, like ORA, AND, CMP, ... */
val DirectIndirectIndexedY = AddressModeSimple(1)
/** [d] - several, like ORA, AND, CMP, ... */
val DirectIndirectLong = AddressModeSimple(1)
/** [d],y - several, like ORA, AND, CMP, ... */
val DirectIndirectIndexedYLong = AddressModeSimple(1)
/** a */
val Absolute = AddressModeSimple(2)
/** a,x */
val AbsoluteIndexedX = AddressModeSimple(2)
/** a,y */
val AbsoluteIndexedY = AddressModeSimple(2)
/** al */
val AbsoluteLong = AddressModeSimple(3)
/** al,x */
val AbsoluteIndexedLong = AddressModeSimple(3)
/** s */
val Stack = AddressModeSimple(0)
/** d,s */
val StackRelative = AddressModeSimple(1)
/** (d,s),y - several, like ORA, AND, CMP, ... */
val StackRelativeIndirectIndexedY = AddressModeSimple(1)
/** (a) - JMP */
val AbsoluteIndirect = AddressModeSimple(2)
/** [al] - JML */
val AbsoluteIndirectLong = AddressModeSimple(3)
/** (a,x) - JMP, JSR */
val AbsoluteIndexedIndirect = AddressModeSimple(2)
/** A */
val Accumulator = AddressModeSimple(0)
/** xyc */
val BlockMove = AddressModeSimple(2)

