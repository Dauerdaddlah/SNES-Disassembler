package de.dde.snes.da.gui

import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.css.StyleableDoubleProperty
import javafx.css.StyleableProperty
import javafx.css.converter.SizeConverter
import javafx.scene.control.*
import java.io.File

class HexView : Control() {
    val fileProperty: ObjectProperty<File> = SimpleObjectProperty(this, "file", null)
    var file: File? by fileProperty

    val hexGapProperty: DoubleProperty = object : StyleableDoubleProperty(5.0) {
        override fun getBean(): Any {
            return this
        }

        override fun getName(): String {
            return "HexGap"
        }

        override fun getCssMetaData(): CssMetaData<out Styleable, Number> {
            return hexGapMetaData
        }
    }
    var hexGap: Double by hexGapProperty

    init {
        styleClass.add(STYLECLASS)
    }

    override fun createDefaultSkin(): Skin<*> {
        return HexViewSkin(this)
    }

    override fun getControlCssMetaData(): MutableList<CssMetaData<out Styleable, *>> {
        return CSS_META_DATA
    }
}

private const val STYLECLASS = "hexview"

private val hexGapMetaData = object : CssMetaData<HexView, Number>(
    "-hexgap",
    SizeConverter.getInstance(),
    5
) {
    override fun isSettable(styleable: HexView?): Boolean {
        return !(styleable?.hexGapProperty?.isBound ?: false)
    }

    override fun getStyleableProperty(styleable: HexView): StyleableProperty<Number> {
        return styleable.hexGapProperty as StyleableProperty<Number>
    }
}

private val CSS_META_DATA = mutableListOf(*Control.getClassCssMetaData().toTypedArray(), hexGapMetaData)

