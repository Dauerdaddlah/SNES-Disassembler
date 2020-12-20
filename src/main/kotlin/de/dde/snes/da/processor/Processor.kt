package de.dde.snes.da.processor

import de.dde.snes.da.*
import kotlin.reflect.KProperty

class Processor {
    var mode: ProcessorMode = ProcessorMode.EMULATION
    val pc = Register16()
    val pbr = Register8()
    val dbr = Register8()
    val a = Accumulator()
    val x = VariableRegister()
    val y = VariableRegister()
    val d = Register16()
    val s = VariableRegister(0x0100)
    val p = StatusRegister()

    fun load(state: ProcessorState) {
        mode = state.mode
        pc.value = state.pc
        pbr.value = state.pbr
        dbr.value = state.dbr
        a.value = state.a
        x.value = state.x
        y.value = state.y
        d.value = state.d
        s.value = state.s
        p.value = state.p

        checkSizes()
    }

    fun state()
        = ProcessorState(
            mode,
            pc.value,
            pbr.value,
            dbr.value,
            a.value,
            x.value,
            y.value,
            d.value,
            s.value,
            p.value
    )

    fun checkSizes() {
        a.size16 = p.memory
        x.size16 = p.index
        y.size16 = p.index
        s.size16 = mode == ProcessorMode.NATIVE
    }

    class Register16 {
        var value: Int = UNKNOWN
            set(value) {
                field = value.asShort()
            }
    }

    class Register8 {
        var value: Int = UNKNOWN
            set(value) {
                field = value.asByte()
            }
    }

    class VariableRegister(
            private val highByteSize8: Int = 0
    ) {
        var size16: TriStateBoolean = false
        var value: Int = UNKNOWN
            set(value) {
                field = if(size16.isTrue())
                    value.asShort()
                else
                    highByteSize8 or value.asByte()
            }
    }

    class Accumulator {
        var size16: TriStateBoolean = false
        var value: Int = UNKNOWN
            set(value) {
                field = if(size16.isTrue())
                    value.asShort()
                else
                    (field and 0xFF00) or value.asByte()
            }

        fun xba() {
            val s = size16
            size16 = true

            value = Word(value.highByte(), value.lowByte())
            size16 = s
        }
    }

    class StatusRegister {
        var value: Int = -1

        var negative by StatusProperty(BIT_NEGATIVE, BIT_NEGATIVE_UNKNOWN)
        var overflow by StatusProperty(BIT_OVERFLOW, BIT_OVERFLOW_UNKNOWN)
        var memory by StatusProperty(BIT_MEMORY, BIT_MEMORY_UNKNOWN)
        var index by StatusProperty(BIT_INDEX, BIT_INDEX_UNKNOWN)
        var decimal by StatusProperty(BIT_DECIMAL, BIT_DECIMAL_UNKNOWN)
        var irq by StatusProperty(BIT_IRQ_DISABLE, BIT_IRQ_DISABLE_UNKNOWN)
        var zero by StatusProperty(BIT_ZERO, BIT_ZERO_UNKNOWN)
        var carry by StatusProperty(BIT_CARRY, BIT_CARRY_UNKNOWN)

        private fun getBoolean(bit: Int, bitUnknown: Int): TriStateBoolean
            = if(value.isBitSet(bitUnknown)) null else value.isBitSet(bit)

        private fun setBoolean(bit: Int, bitUnknown: Int, v: TriStateBoolean) {
            value = when {
                v.isUnknown() -> {
                    value.setBit(bitUnknown)
                }
                v.isTrue() -> {
                    value.clearBit(bitUnknown).setBit(bit)
                }
                else -> {
                    value.clearBit(bit or bitUnknown)
                }
            }
        }

        inner class StatusProperty(
                val bit: Int,
                val bitUnknown: Int) {

            operator fun getValue(thisRef: Any?, property: KProperty<*>): TriStateBoolean {
                return getBoolean(bit, bitUnknown)
            }

            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: TriStateBoolean) {
                setBoolean(bit, bitUnknown, value)
            }
        }

        companion object {
            const val BIT_CARRY = 0x01
            const val BIT_ZERO = 0x02
            const val BIT_IRQ_DISABLE = 0x04
            const val BIT_DECIMAL = 0x08
            const val BIT_INDEX = 0x10
            const val BIT_MEMORY = 0x20
            const val BIT_OVERFLOW = 0x40
            const val BIT_NEGATIVE = 0x80

            const val BIT_CARRY_UNKNOWN = 0x0100
            const val BIT_ZERO_UNKNOWN = 0x0200
            const val BIT_IRQ_DISABLE_UNKNOWN = 0x0400
            const val BIT_DECIMAL_UNKNOWN = 0x0800
            const val BIT_INDEX_UNKNOWN = 0x1000
            const val BIT_MEMORY_UNKNOWN = 0x2000
            const val BIT_OVERFLOW_UNKNOWN = 0x4000
            const val BIT_NEGATIVE_UNKNOWN = 0x8000
        }
    }
}

data class ProcessorState(
        val mode: ProcessorMode = ProcessorMode.EMULATION,
        val pc: Int,
        val pbr: Int,
        val dbr: Int,
        val a: Int,
        val x: Int,
        val y: Int,
        val d: Int,
        val s: Int,
        val p: Int
)

const val UNKNOWN = -1
fun Int.isUnknown() = this == UNKNOWN

typealias TriStateBoolean = Boolean?
fun TriStateBoolean.isUnknown() = this == null
fun TriStateBoolean.isTrue() = this == true