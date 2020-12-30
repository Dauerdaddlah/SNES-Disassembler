package de.dde.snes.da.util

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import kotlin.reflect.KProperty

operator fun <T> ReadOnlyProperty<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = this.value
operator fun <T> Property<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T?) { this.value = value }

operator fun DoubleProperty.getValue(thisRef: Any?, property: KProperty<*>): Double = this.get()
operator fun DoubleProperty.setValue(thisRef: Any?, property: KProperty<*>, value: Double) = this.set(value)

operator fun BooleanProperty.getValue(thisRef: Any?, property: KProperty<*>): Boolean = this.get()
operator fun BooleanProperty.setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) = this.set(value)

fun <S, T> callback(f: (S) -> T)
    = javafx.util.Callback<S, T> { f(it) }

fun <S, T> treeTableCell(f: TreeTableCell<S, T>.(T?, Boolean) -> Unit) = callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> {
    object : TreeTableCell<S, T>() {
        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)

            f(item, empty)
        }
    }
}