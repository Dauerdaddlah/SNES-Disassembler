package de.dde.snes.da.gui.table

import de.dde.snes.da.Disassembler
import de.dde.snes.da.gui.Controller
import de.dde.snes.da.memory.ROMByte
import de.dde.snes.da.memory.ROMByteType
import de.dde.snes.da.processor.instruction
import de.dde.snes.da.util.callback
import de.dde.snes.da.util.treeTableCell
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import java.net.URL
import java.util.*

class TableControl(
        val controller: Controller
) : Initializable {
    val root: Parent

    @FXML
    lateinit var tblRom: TreeTableView<ROMByte>
    @FXML
    lateinit var tblColLbl: TreeTableColumn<ROMByte, String>
    @FXML
    lateinit var tblColOff: TreeTableColumn<ROMByte, Int>
    @FXML
    lateinit var tblColAdd: TreeTableColumn<ROMByte, Number>
    @FXML
    lateinit var tblColVal: TreeTableColumn<ROMByte, Any>
    @FXML
    lateinit var tblColIns: TreeTableColumn<ROMByte, Any>
    @FXML
    lateinit var tblColCom: TreeTableColumn<ROMByte, String>

    @FXML
    lateinit var txtBank: TextField
    @FXML
    lateinit var txtPage: TextField

    var numBanks = 0
    var curBank = -1
    var numPages = 0
    var curPage = -1
    val treeItems = mutableListOf<TreeItem<ROMByte>>()

    init {
        val loader = FXMLLoader(javaClass.getResource("TableControl.fxml"))
        loader.setController(this)
        loader.resources = Disassembler.resourceBundle

        root = loader.load()
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        tblRom.isShowRoot = false

        tblColLbl.cellValueFactory = callback { it?.value?.value?.labelProperty }
        tblColCom.cellValueFactory = callback { it?.value?.value?.commentProperty }

        tblColLbl.cellFactory = callback { EditCell() }
        tblColCom.cellFactory = callback { EditCell() }

        tblColOff.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                treeTableRow.item?.let { "%06X".format(it.index) } ?: ""
        }

        tblColAdd.cellFactory = treeTableCell { _, empty ->
            text = if (empty) ""
            else treeTableRow.item?.let { controller.project?.mappingMode?.toSnesAddress(it.index)?.let { addr -> "%06X".format(addr) } }?: ""
        }

        tblColVal.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                treeTableRow.item?.let { "%02X".format(it.b) } ?: ""
        }

        tblColIns.cellFactory = treeTableCell { _, empty ->
            text = if (empty || treeTableRow.item == null)
                ""
            else {
                val byte = treeTableRow.item
                val inst = instruction(byte.b)

                val s = StringBuilder(inst.operation.symbol)

                val operand = inst.getOperandBytes(controller.project?: error(""), byte)

                if (operand.isNotEmpty()) {
                    s.append(' ').append(inst.addressMode.format(operand))
                }

                s.toString()
            }
        }

        tblRom.rowFactory = callback {
            object : TreeTableRow<ROMByte>() {
                val typeListener = ChangeListener<ROMByteType> { _, o, _ -> typeChanged(o) }

                override fun updateItem(item: ROMByte?, empty: Boolean) {
                    val old = this.item?.type

                    if (this.item != null) {
                        this.item.typeProperty.removeListener(typeListener)
                    }

                    super.updateItem(item, empty)

                    if (item != null) {
                        item.typeProperty.addListener(typeListener)
                    }

                    typeChanged(old)
                }

                fun typeChanged(old: ROMByteType?) {

                    old?.let { styleClass.remove(it.name.toLowerCase()) }
                    item?.type?.let { styleClass.add(it.name.toLowerCase()) }
                }
            }
        }

        controller.projectProperty.addListener { _, _, project ->
            treeItems.clear()
            project?.romBytes?.forEach { treeItems.add(TreeItem(it)) }

            if (project == null) {
                numBanks = 0
                numPages = 0
            } else {
                numBanks = project.romFile.bytes.size / project.mappingMode.bankSize
                if (numBanks * project.mappingMode.bankSize < project.romFile.bytes.size)
                    numBanks++

                numPages = project.mappingMode.bankSize / 0x100
            }

            setVisibleData(-1, -1, true)
        }

        tblRom.onKeyPressed = EventHandler{
            if (tblRom.editingCell == null) {
                if (it.code.isLetterKey || it.code.isDigitKey) {
                    val focusedCellPosition = tblRom.focusModel.focusedCell

                    if (focusedCellPosition.tableColumn.isEditable) {
                        startEditKey = it.text
                        tblRom.edit(focusedCellPosition.row, focusedCellPosition.tableColumn)
                        startEditKey = null
                    }
                }
            }
        }

        tblRom.root = TreeItem()

        txtBank.setOnAction {
            try {
                setVisibleData(txtBank.text.toInt(16), curPage)
            } catch(e: NumberFormatException) {
            }
        }

        txtPage.setOnAction {
            try {
                setVisibleData(curBank, txtPage.text.toInt(16))
            } catch(e: NumberFormatException) {
            }
        }
    }

    @FXML
    fun doFirstBank() {
        setVisibleData(-1, curPage)
    }

    @FXML
    fun doPrevBank() {
        setVisibleData(curBank - 1, curPage)
    }

    @FXML
    fun doNextBank() {
        setVisibleData(curBank + 1, curPage)
    }

    @FXML
    fun doLastBank() {
        setVisibleData(numBanks, curPage)
    }

    @FXML
    fun doFirstPage() {
        setVisibleData(curBank, -1)
    }

    @FXML
    fun doPrevPage() {
        setVisibleData(curBank, curPage - 1)
    }

    @FXML
    fun doNextPage() {
        setVisibleData(curBank, curPage + 1)
    }

    @FXML
    fun doLastPage() {
        setVisibleData(curBank, numPages)
    }

    fun setVisibleData(bank: Int, page: Int, updateAlways: Boolean = false) {
        if (!updateAlways && curBank == bank && curPage == page)
            return

        curBank = maxOf(-1, minOf(numBanks - 1, bank))
        curPage = maxOf(-1, minOf(numPages - 1, page))

        txtBank.text = if (curBank < 0) Disassembler.resourceBundle.getString("de.dde.snes.da.rom.allBanks") else "%02X".format(curBank)
        txtPage.text = if (curPage < 0) Disassembler.resourceBundle.getString("de.dde.snes.da.rom.allPages") else "%02X".format(curPage)

        tblRom.root.children.clear()

        if (curBank < 0) {
            tblRom.root.children.setAll(treeItems)
        } else {
            var romStart = (controller.project?.mappingMode?.bankSize ?: 0) * curBank
            var romEnd = romStart + (controller.project?.mappingMode?.bankSize ?: 0)

            if (curPage > -1) {
                romStart += (0x100 * curPage)
                romEnd = romStart + 0x100
            }

            tblRom.root.children.setAll(treeItems.subList(minOf(treeItems.size, romStart), minOf(treeItems.size, romEnd)))
        }
    }

    private var startEditKey: String? = null

    private inner class EditCell<S> : TreeTableCell<S, String>() {
        private val textfield = TextField().also {
            it.focusedProperty().addListener { _, _, focused ->
                if (!focused)
                    commitEdit(it.text)
            }

            it.onKeyPressed = EventHandler { e ->
                when (e.code) {
                    KeyCode.ESCAPE -> {
                        cancelEdit()
                        e.consume()
                    }
                    else -> {
                    }
                }
            }

            it.onAction = EventHandler { _ ->
                commitEdit(it.text)
            }
        }

        override fun startEdit() {
            if (isEditing)
                return

            super.startEdit()

            if (isEditing) {
                graphic = textfield
                text = ""

                textfield.text = item
                textfield.requestFocus()

                if (startEditKey != null) {
                    textfield.text = startEditKey
                    textfield.deselect()
                    textfield.end()
                }
            }
        }

        override fun cancelEdit() {
            if (isEditing
                    && tblRom.focusModel.focusedCell != null
                    && (tblRom.focusModel.focusedCell.row != treeTableRow.index
                            || tblRom.focusModel.focusedCell.tableColumn != tableColumn)) {
                // functionality borrowed and adapted from TreeTableCell.commitEdit to commit edit
                // on focus lost if another cell is selected
                val editingCell = TreeTablePosition(treeTableView, treeTableRow.index, tableColumn)

                // Inform the TableView of the edit being ready to be committed.
                // Inform the TableView of the edit being ready to be committed.
                val editEvent = TreeTableColumn.CellEditEvent(
                        treeTableView,
                        editingCell,
                        TreeTableColumn.editCommitEvent(),
                        textfield.text
                )

                Event.fireEvent(tableColumn, editEvent)
            }
            super.cancelEdit()

            graphic = null
            text = item
        }

        override fun commitEdit(newValue: String?) {
            super.commitEdit(newValue)

            graphic = null
            text = item
        }

        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)

            when {
                empty -> {
                    graphic = null
                    text = ""
                }
                isEditing -> {
                    graphic = textfield
                    text = ""

                    textfield.text = item
                }
                else -> {
                    graphic = null
                    text = item
                }
            }
        }
    }
}