package de.dde.snes.da.gui

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import kotlin.reflect.KProperty

operator fun <T> ObjectProperty<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = this.get()
operator fun <T> ObjectProperty<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T?) = this.set(value)

operator fun DoubleProperty.getValue(thisRef: Any?, property: KProperty<*>): Double = this.get()
operator fun DoubleProperty.setValue(thisRef: Any?, property: KProperty<*>, value: Double) = this.set(value)