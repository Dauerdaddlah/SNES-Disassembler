package de.dde.snes.da.memory

import de.dde.snes.da.util.*
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import java.util.*

class ROMByte(
        val index: Int,
        val b: Byte
) {
    val labelProperty: StringProperty = SimpleStringProperty(this, "label", null)
    var label: String? by labelProperty

    val commentProperty: StringProperty = SimpleStringProperty(this, "comment", null)
    var comment: String? by commentProperty

    val typeProperty: ObjectProperty<ROMByteType> = SimpleObjectProperty(this, "type", ROMByteType.UNKNOWN)
    var type: ROMByteType? by typeProperty

    val jumpTypeProperty: ReadOnlySetProperty<ROMJumpType> = ReadOnlySetWrapper(this, "jumpType", FXCollections.observableSet(EnumSet.noneOf(ROMJumpType::class.java))).readOnlyProperty
    val jumpType: ObservableSet<ROMJumpType> get() = jumpTypeProperty.value
}