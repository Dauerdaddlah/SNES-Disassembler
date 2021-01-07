package de.dde.snes.da.memory

import de.dde.snes.da.clearBit
import de.dde.snes.da.isBitSet
import de.dde.snes.da.setBit
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import kotlin.reflect.KProperty

class SNESState : ObservableValue<SNESState> {
    var pbr: Int = 0
        set(value) {
            field = value
            fireEvent()
        }
    var dbr: Int = 0
        set(value) {
            field = value
            fireEvent()
        }
    var status: Int = BIT_IRQ_DISABLE or BIT_INDEX or BIT_MEMORY or BIT_EMULATION
        set(value) {
            field = value
            fireEvent()
        }
    var direct: Int = 0
        set(value) {
            field = value
            fireEvent()
        }

    var mode: SNESMode
        get() = SNESMode.fromBoolean(emulation)
        set(value) { emulation = value.toBoolean() }

    var emulation: Boolean by BIT_EMULATION
    var carry: Boolean by BIT_CARRY
    var zero: Boolean by BIT_ZERO
    var irq: Boolean by BIT_IRQ_DISABLE
    var decimal: Boolean by BIT_DECIMAL
    var index: Boolean by BIT_INDEX
    var memory: Boolean by BIT_MEMORY
    var overflow: Boolean by BIT_OVERFLOW
    var negative: Boolean by BIT_NEGATIVE

    val m16 get() = !emulation && memory
    val x16 get() = !emulation && index

    private operator fun Int.getValue(thisRef: Any?, property: KProperty<*>): Boolean = status.isBitSet(this)
    private operator fun Int.setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        status = if (value) status.setBit(this) else status.clearBit(this)
    }

    private val invalidationListeners = mutableListOf<InvalidationListener>()
    private val changeListeners = mutableListOf<ChangeListener<in SNESState>>()

    override fun addListener(listener: InvalidationListener) {
        invalidationListeners.add(listener)
    }

    override fun removeListener(listener: InvalidationListener) {
        invalidationListeners.remove(listener)
    }

    override fun getValue(): SNESState {
        return this
    }

    override fun addListener(listener: ChangeListener<in SNESState>) {
        changeListeners.add(listener)
    }

    override fun removeListener(listener: ChangeListener<in SNESState>) {
        changeListeners.remove(listener)
    }

    fun fireEvent() {
        invalidationListeners.forEach { it.invalidated(this) }
        changeListeners.forEach { it.changed(this, this, this) }
    }
}

enum class SNESMode {
    EMULATION,
    NATIVE;

    fun toBoolean() = this == EMULATION

    companion object {
        fun fromBoolean(emulationFlag: Boolean) = if (emulationFlag) EMULATION else NATIVE
    }
}

const val BIT_CARRY       =  0x01
const val BIT_ZERO        =  0x02
const val BIT_IRQ_DISABLE =  0x04
const val BIT_DECIMAL     =  0x08
const val BIT_INDEX       =  0x10
const val BIT_MEMORY      =  0x20
const val BIT_OVERFLOW    =  0x40
const val BIT_NEGATIVE    =  0x80
const val BIT_EMULATION   = 0x100