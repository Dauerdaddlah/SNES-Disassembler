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

sealed class AddressMode
/** # */
object ImmediateAccumulator : AddressMode()
/** # */
object ImmediateIndex : AddressMode()
/** # */
object Immediate8 : AddressMode()
/** i */
object Implied : AddressMode()
/** r */
object ProgramCounterRelative : AddressMode()
/** rl */
object ProgramCounterRelativeLong : AddressMode()
/** d */
object Direct : AddressMode()
/** d,x */
object DirectIndexedX : AddressMode()
/** d,y */
object DirectIndexedY : AddressMode()
/** (d) - several, like ORA, AND, CMP, ... */
object DirectIndirect : AddressMode()
/** (d,x) - several, like ORA, AND, CMP, ... */
object DirectIndexedXIndirect : AddressMode()
/** (d),y - several, like ORA, AND, CMP, ... */
object DirectIndirectIndexedY : AddressMode()
/** [d] - several, like ORA, AND, CMP, ... */
object DirectIndirectLong : AddressMode()
/** [d],y - several, like ORA, AND, CMP, ... */
object DirectIndirectIndexedYLong : AddressMode()
/** a */
object Absolute : AddressMode()
/** a,x */
object AbsoluteIndexedX : AddressMode()
/** a,y */
object AbsoluteIndexedY : AddressMode()
/** al */
object AbsoluteLong : AddressMode()
/** al,x */
object AbsoluteIndexedLong : AddressMode()
/** s */
object Stack : AddressMode()
/** d,s */
object StackRelative : AddressMode()
/** (d,s),y - several, like ORA, AND, CMP, ... */
object StackRelativeIndirectIndexedY : AddressMode()
/** (a) - JMP */
object AbsoluteIndirect : AddressMode()
/** [al] - JML */
object AbsoluteIndirectLong : AddressMode()
/** (a,x) - JMP, JSR */
object AbsoluteIndexedIndirect : AddressMode()
/** A */
object Accumulator : AddressMode()
/** xyc */
object BlockMove : AddressMode()

