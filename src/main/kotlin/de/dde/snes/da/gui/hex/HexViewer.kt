package de.dde.snes.da.gui.hex

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control

import de.dde.snes.da.util.*
import javafx.scene.control.Skin

class HexViewer : Control() {
    val hexDataProperty: ObjectProperty<HexDataSource?> = SimpleObjectProperty(this, "hexData")
    var hexData: HexDataSource? by hexDataProperty

    override fun createDefaultSkin(): Skin<*> {
        return HexViewerSkin(this)
    }
}