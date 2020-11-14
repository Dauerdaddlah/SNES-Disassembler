package de.dde.snes.da.gui.hex

import de.dde.snes.da.toAscii
import javafx.application.Platform
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.VPos
import javafx.scene.control.Label
import javafx.scene.control.ScrollBar
import javafx.scene.control.SkinBase
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.scene.text.Font
import kotlin.math.abs

class HexViewerSkin(hexViewer: HexViewer): SkinBase<HexViewer>(hexViewer) {
    private val font = Font("Courier New", 12.0)

    private val scrollVertical = ScrollBar()
    private val header = Labels()

    private val rows = mutableListOf<Labels>()

    private val grid = GridPane()

    /** count of 16-byte rows within the file */
    private var numRows = 0
    /** how many hex-digits are needed to display the offset of the last row */
    private var rowsHexSize = 0

    private var updated = false

    init {
        scrollVertical.orientation = Orientation.VERTICAL
        scrollVertical.min = 0.0
        scrollVertical.max = 0.0
        registerChangeListener(scrollVertical.valueProperty()) {
            requestUpdate()
        }

        header.labelIndex.text = "Offset (h)"
        header.labelText.text = "Decoded Text"
        header.labelsHex.forEachIndexed { index, label -> label.text = "%02X".format(index) }

        children.add(grid)
        grid.minHeight = -1.0
        grid.prefHeight = -1.0
        children.add(scrollVertical)
        scrollVertical.minHeight = -1.0
        scrollVertical.prefHeight = -1.0

        grid.isManaged = false
        grid.hgap = 5.0
        scrollVertical.isManaged = false

        grid.columnConstraints.setAll(
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.RIGHT, true),
                ColumnConstraints(5.0, 5.0, 5.0, Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(5.0, 5.0, 5.0, Priority.NEVER, HPos.CENTER, false),
                ColumnConstraints(-1.0, -1.0,-1.0,Priority.NEVER, HPos.LEFT, true),
        )

        grid.rowConstraints.setAll(
                RowConstraints(-1.0, -1.0, -1.0, Priority.NEVER, VPos.TOP, false),
                RowConstraints(5.0, 5.0, 5.0, Priority.NEVER, VPos.CENTER, false)
        )

        grid.add(header.labelIndex, 0, 0)
        grid.add(header.labelText, 17 + 2, 0)
        header.labelsHex.forEachIndexed { index, label -> grid.add(label, index + 1 + 1, 0) }

        registerChangeListener(skinnable.hexDataProperty) {
            val v = it.value as HexDataSource?

            if (v == null) {
                numRows = 0
                rowsHexSize = 0
                scrollVertical.visibleAmount = 0.0
            } else {
                numRows = (v.size / 16) + 1
                rowsHexSize = "%x".format((v.size - 1) / 16 * 16).length
                scrollVertical.max = numRows.toDouble() - 2
            }

            requestUpdate()
        }

        addBehavior()
    }

    private fun addBehavior() {
        consumeMouseEvents(true)

        skinnable.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            skinnable.requestFocus()
        }

        skinnable.addEventHandler(ScrollEvent.SCROLL) { e ->
            skinnable.requestFocus()

            if (e.deltaY < 0) {
                scrollVertical.value = minOf(scrollVertical.max, scrollVertical.value + abs(e.deltaY) * scrollVertical.unitIncrement)
            } else {
                scrollVertical.value = minOf(scrollVertical.max, scrollVertical.value - e.deltaY * scrollVertical.unitIncrement)
            }

            e.consume()
        }

        val keys = mutableMapOf<KeyCode, () -> Unit>()

        keys[KeyCode.UP] = { scrollVertical.decrement() }
        keys[KeyCode.DOWN] = { scrollVertical.increment() }
        keys[KeyCode.HOME] = { scrollVertical.value = scrollVertical.min }
        keys[KeyCode.END] = { scrollVertical.value = scrollVertical.max }
        keys[KeyCode.PAGE_UP] = { scrollVertical.value = maxOf(scrollVertical.min, scrollVertical.value - scrollVertical.blockIncrement) }
        keys[KeyCode.PAGE_DOWN] = { scrollVertical.value = minOf(scrollVertical.max, scrollVertical.value + scrollVertical.blockIncrement) }


        skinnable.addEventHandler(KeyEvent.KEY_PRESSED) { e ->
            keys[e.code]?.let {
                it()
                e.consume()
            }
        }
    }

    override fun layoutChildren(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double) {
        layoutInArea(scrollVertical, contentX, contentY, contentWidth, contentHeight, -1.0, HPos.RIGHT, VPos.CENTER)

        val refHeight = header.height

        if (refHeight == 0.0) return

        val numRows = ((contentHeight - refHeight - 5) / refHeight).toInt() + 1

        if (numRows != rows.size) {
            scrollVertical.visibleAmount = numRows.toDouble() - 1
            scrollVertical.blockIncrement = scrollVertical.visibleAmount

            while (rows.size < numRows) {
                addRow()
            }

            while (rows.size > numRows) {
                removeRow()
            }
        }

        layoutInArea(grid, contentX, contentY, contentWidth, refHeight * (rows.size + 1) + 5, -1.0, HPos.LEFT, VPos.CENTER)

        requestUpdate()
    }

    private fun addRow() {
        val labels = Labels()
        rows.add(labels)

        val row = grid.rowConstraints.size + 1

        grid.rowConstraints.add(RowConstraints(-1.0, -1.0, -1.0, Priority.NEVER, VPos.TOP, false))

        grid.add(labels.labelIndex, 0, row)
        grid.add(labels.labelText, 17 + 2, row)
        labels.labelsHex.forEachIndexed { index, label -> grid.add(label, index + 1 + 1, row) }
    }

    private fun removeRow() {
        grid.rowConstraints.removeLast()
        val labels = rows.removeLast()

        grid.children.remove(labels.labelText)
        grid.children.remove(labels.labelIndex)
        labels.labelsHex.forEach { grid.children.remove(it) }
    }

    private fun requestUpdate() {
        updated = false
        Platform.runLater { updateData() }
    }

    private fun updateData() {
        if (updated)
            return

        val source = skinnable.hexData

        if (source == null) {
            if (rows.isNotEmpty()) {
                val first = rows.first()
                first.labelIndex.text = "00"
                first.labelText.text = "................"
                first.labelsHex.forEach { it.text = "00" }

                for (i in 1 until rows.size) {
                    val labels = rows[i]
                    labels.labelIndex.text = ""
                    labels.labelText.text = ""
                    labels.labelsHex.forEach { it.text = "" }
                }
            }
        } else {
            if (rows.isNotEmpty()) {
                var start = scrollVertical.value.toInt() * 16

                source.ensure(start, rows.size * 16)

                for (row in rows) {
                    if (start < source.size) {
                        row.labelIndex.text = "%0${rowsHexSize}X".format(start)
                        row.labelText.text = ""

                        for (l in row.labelsHex) {
                            if (start < source.size) {
                                val b = source.getByte(start)
                                l.text = "%02X".format(b)
                                row.labelText.text += b.toAscii()

                                start++
                            } else {
                                l.text = ""
                            }
                        }
                    } else {
                        row.labelIndex.text = ""
                        row.labelText.text = ""
                        row.labelsHex.forEach { it.text = "" }
                    }
                }
            }
        }

        updated = true
    }

    private inner class Labels {
        val labelIndex = Label("Index")
        val labelText = Label("Text")
        val labelsHex = Array(16) { Label("00") }

        init {
            labelIndex.font = font
            labelText.font = font
            labelsHex.forEach { it.font = font }
        }

        val height: Double
            get() = maxOf(labelIndex.height, maxOf(labelText.height, labelsHex.maxOf { it.height }))
    }
}