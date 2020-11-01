package de.dde.snes.da.gui

import com.sun.javafx.scene.control.behavior.BehaviorBase
import de.dde.snes.da.Byte
import de.dde.snes.da.toAscii
import javafx.event.EventDispatcher
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Orientation
import javafx.geometry.VPos
import javafx.scene.control.Label
import javafx.scene.control.ScrollBar
import javafx.scene.control.SkinBase
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.text.Font
import java.io.File
import java.io.RandomAccessFile
import kotlin.math.abs

class HexViewSkin(
    view: HexView
) : SkinBase<HexView>(view) {

    private val scrollVertical = ScrollBar()

    private val font = Font("Courier New", 12.0)

    private val header = HexRow()

    private val rows = mutableListOf(HexRow())

    private var fileData: FileData? = null

    init {
        header.labelOffset.text = "Offset (h)"
        header.labelDecode.text = "Decoded Text"

        scrollVertical.orientation = Orientation.VERTICAL
        scrollVertical.valueProperty().addListener { _, _, n ->
            fileData?.let {
                it.start = n.toLong() * 16
                skinnable.requestLayout()
            }
        }

        children.add(scrollVertical)

        view.fileProperty.addListener { _, _, n ->
            if (n == null) {
                fileData?.close()
                fileData = null

                scrollVertical.isVisible = false
            } else {
                fileData = FileData(n)

                scrollVertical.value = 0.0
                scrollVertical.min = 0.0
                scrollVertical.max = (fileData?.rows ?: 1) - 1.0
            }

            skinnable.requestLayout()
        }

        addBehavior()
    }

    private fun addBehavior() {
        skinnable.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            skinnable.requestFocus()
        }

        skinnable.addEventHandler(ScrollEvent.SCROLL) { e ->
            skinnable.requestFocus()

            if (e.deltaY < 0) {
                repeat (abs(e.deltaY).toInt()) { scrollVertical.increment() }
            } else {
                repeat (e.deltaY.toInt()) { scrollVertical.decrement() }
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

    override fun computeMinWidth(height: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        return header.width
    }

    override fun computeMinHeight(width: Double, topInset: Double, rightInset: Double, bottomInset: Double, leftInset: Double): Double {
        return header.height + 5
    }

    override fun layoutChildren(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight)

        layoutInArea(scrollVertical, contentX + contentWidth - scrollVertical.width, contentY, scrollVertical.width, contentHeight, 0.0, HPos.RIGHT, VPos.CENTER)

        val data = fileData
        var rowsShown = 0L
        if (data == null) {
            rows.forEach { it.visible = false }
        } else {
            rowsShown = prepareData(contentX, contentY, contentWidth, contentHeight, data)
        }

        layout(contentX, contentY, contentWidth, contentHeight, rowsShown)
    }

    private fun prepareData(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double, data: FileData): Long {
        // how many rows could we display
        val numRows = calcNumRows(contentHeight)

        // how many rows do we need to display
        val neededRows = data.rows - (data.start / 16)

        // how many rows will we display in the end
        val rowsShown = minOf(neededRows, numRows.toLong())

        // ensure we have enough HexRows
        while (rowsShown > rows.size) {
            rows.add(HexRow())
        }

        // only show those rows, which are needed
        rows.forEachIndexed { i, r -> r.visible = i < rowsShown }

        // load data from file
        data.load(rowsShown)

        // propagate data to gui
        for (i in 0 until rowsShown.toInt()) {
            val row = rows[i]
            row.labelOffset.text = "%0${data.rowsHexSize}X".format(data.start + i * 16)

            val s = StringBuilder()
            for (j in row.labels.indices) {
                val b = data.buffer[i * 16 + j]
                row.labels[j].text = "%02X".format(b)
                s.append(b.toAscii())
            }

            row.labelDecode.text = s.toString()
        }

        return rowsShown
    }

    private fun layout(contentX: Double, contentY: Double, contentWidth: Double, contentHeight: Double, rowsShown: Long) {
        scrollVertical.visibleAmount = rowsShown.toDouble()
        scrollVertical.isVisible = rowsShown < (fileData?.rows?: 0)

        val offsetWidth = maxOf(rows.maxOf { if (it.visible) it.labelOffset.width else 0.0 }, header.labelOffset.width)
        val decodeWidth = maxOf(rows.maxOf { if (it.visible) it.labelDecode.width else 0.0 }, header.labelDecode.width)

        val startX = contentX
        val startContentX = startX + offsetWidth
        val endX = contentX + contentWidth - (if (scrollVertical.isVisible) scrollVertical.width else 0.0)
        val endContentX = endX - decodeWidth
        val partWidth = (endContentX - startContentX) / 16
        var startY = contentY
        val height = header.height


        layoutInArea(header.labelOffset, startX, startY, offsetWidth, height, 0.0, HPos.LEFT, VPos.TOP)
        layoutInArea(header.labelDecode, endContentX, startY, decodeWidth, height, 0.0, HPos.RIGHT, VPos.TOP)
        header.labels.forEachIndexed { index, label ->
            layoutInArea(label, startContentX + index * partWidth, startY, partWidth, height, 0.0, HPos.CENTER, VPos.TOP)
        }

        startY += header.height + 5.0

        rows.forEach { row ->
            if(row.visible) {
                layoutInArea(row.labelOffset, startX, startY, offsetWidth, height, 0.0, HPos.LEFT, VPos.BOTTOM)
                layoutInArea(row.labelDecode, endContentX, startY, decodeWidth, height, 0.0, HPos.CENTER, VPos.BOTTOM)
                row.labels.forEachIndexed { index, label ->
                    layoutInArea(label, startContentX + index * partWidth, startY, partWidth, height, 0.0, HPos.CENTER, VPos.TOP)
                }
                startY += header.height
            }
        }
    }

    private fun calcNumRows(contentHeight: Double): Int {
        return maxOf(0, ((contentHeight - header.height - 5) / header.height).toInt())
    }

    private inner class HexRow {
        var start: Int = 0
        val labelOffset = Label()
        val labelDecode = Label()
        val labels = Array(16) { Label("00") }

        init {
            labelOffset.font = font
            labelDecode.font = font
            labels.forEach { it.font = font }

            children.add(labelOffset)
            children.add(labelDecode)
            children.addAll(labels)
        }

        val height: Double
            get() = maxOf(labelOffset.height, maxOf(labelDecode.height, labels.first().height))
        val width: Double
            get() = labelOffset.width + labelDecode.width + labels.sumByDouble { it.width } + (17 * skinnable.hexGap)

        var visible: Boolean
            get() = labelOffset.isVisible
            set(value) {
                labelOffset.isVisible = value
                labelDecode.isVisible = value
                labels.forEach { it.isVisible = value }
            }
    }

    private class FileData(
        val file: File
    ) {
        private val ra: RandomAccessFile = RandomAccessFile(file, "r")
        /** length of the file in bytes */
        private val length: Long = ra.length()
        /** count of 16-byte rows within the file */
        val rows = (length / 16) + 1
        /** how many hex-digits are needed to display the offset of the last row */
        val rowsHexSize = "%x".format((length - 1) / 16 * 16).length

        /** start index within the file, from which on data should be loaded */
        var start = 0L
        /** start index within the file, from which data are already loaded */
        private var bufferStart = 0L
        /** data of the file loaded for Display */
        var buffer = ByteArray(0)
            private set
        /** how many bytes of the buffer are actual ly data */
        private var bufferLength = 0

        fun load(rowsShown: Long) {
            var bytesNeeded = rowsShown * 16

            if(start + bytesNeeded > length) {
                bytesNeeded = length - start
            }

            if (bufferStart != start || bufferLength < bytesNeeded) {
                if(buffer.size < bytesNeeded) {
                   buffer = ByteArray(bytesNeeded.toInt())
                }

                ra.seek(start)
                bufferLength = ra.read(buffer, 0, bytesNeeded.toInt())
            }
        }

        fun close() {
            ra.close()
        }
    }
}