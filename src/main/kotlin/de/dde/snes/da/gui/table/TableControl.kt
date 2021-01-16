package de.dde.snes.da.gui.table

import de.dde.snes.da.Disassembler
import de.dde.snes.da.gui.Controller
import de.dde.snes.da.memory.ROMByte
import de.dde.snes.da.memory.ROMByteType
import de.dde.snes.da.memory.SNESState
import de.dde.snes.da.processor.instruction
import de.dde.snes.da.translate
import de.dde.snes.da.util.callback
import de.dde.snes.da.util.treeTableCell
import javafx.beans.value.ChangeListener
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import java.net.URL
import java.util.*
import javax.swing.ActionMap

class TableControl(
        val controller: Controller
) : Initializable {
    val root: Parent

    @FXML
    private lateinit var tblRom: TreeTableView<ROMByte>
    @FXML
    private lateinit var tblColLbl: TreeTableColumn<ROMByte, String>
    @FXML
    private lateinit var tblColOff: TreeTableColumn<ROMByte, Int>
    @FXML
    private lateinit var tblColAdd: TreeTableColumn<ROMByte, Number>
    @FXML
    private lateinit var tblColVal: TreeTableColumn<ROMByte, Any>
    @FXML
    private lateinit var tblColIns: TreeTableColumn<ROMByte, Any>
    @FXML
    private lateinit var tblColCom: TreeTableColumn<ROMByte, String>
    @FXML
    private lateinit var tblColM: TreeTableColumn<ROMByte, SNESState>
    @FXML
    private lateinit var tblColI: TreeTableColumn<ROMByte, SNESState>
    @FXML
    private lateinit var tblColMode: TreeTableColumn<ROMByte, SNESState>
    @FXML
    private lateinit var tblColPbr: TreeTableColumn<ROMByte, SNESState>
    @FXML
    private lateinit var tblColDbr: TreeTableColumn<ROMByte, SNESState>
    @FXML
    private lateinit var tblColDir: TreeTableColumn<ROMByte, SNESState>
    @FXML
    private lateinit var tblColSta: TreeTableColumn<ROMByte, SNESState>
    @FXML
    private lateinit var tblColTyp: TreeTableColumn<ROMByte, ROMByteType>

    @FXML
    private lateinit var txtBank: TextField
    @FXML
    private lateinit var txtPage: TextField

    private var numBanks = 0
    private var curBank = -1
    private var numPages = 0
    private var curPage = -1
    private val treeItems = mutableListOf<TreeItem<ROMByte>>()

    val actionMap = mutableMapOf<ActionId, Action>()
    val inputMap = mutableMapOf<KeyCombination, ActionId>()

    init {
        val loader = FXMLLoader(javaClass.getResource("TableControl.fxml"))
        loader.setController(this)
        loader.resources = Disassembler.resourceBundle

        root = loader.load()

        ROMByteType.values().forEach { type ->
            actionMap["$ACTION_MARK_PREFIX${type.name}"] = { forEachRow { this.type = type } }
        }

        actionMap[ACTION_SWITCH_M] = { forEachRow { state.memory = !state.memory } }
        actionMap[ACTION_SWITCH_X] = { forEachRow { state.index = !state.index } }
        actionMap[ACTION_SWITCH_MODE] = { forEachRow { state.emulation = !state.emulation } }

        val ctx = ContextMenu()

        val menuMark = Menu(translate("de.dde.snes.da.rom.mark"))
        ROMByteType.values().forEach { type ->
            menuMark.items.add(actionMenuItem("$ACTION_MARK_PREFIX${type.name}"))
        }
        ctx.items.add(menuMark)
        ctx.items.add(actionMenuItem(ACTION_SWITCH_M))
        ctx.items.add(actionMenuItem(ACTION_SWITCH_X))
        ctx.items.add(actionMenuItem(ACTION_SWITCH_MODE))

        tblRom.contextMenu = ctx

        inputMap[KeyCodeCombination(KeyCode.M)] = ACTION_SWITCH_M
        inputMap[KeyCodeCombination(KeyCode.X)] = ACTION_SWITCH_X

        root.setOnKeyPressed {
            inputMap.filter {
                e -> e.key.match(it)
            }.forEach {
                e -> actionMap[e.value]?.invoke()
            }
        }
    }

    private fun actionMenuItem(action: String): MenuItem {
        val item = MenuItem(translate("de.dde.snes.da.rom.$action"))
        item.onAction = EventHandler { actionMap[action]?.invoke() }
        return item
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
            else treeTableRow.item?.let { controller.project?.mappingMode?.toSnesAddress(it)?.let { addr -> "%06X".format(addr) } }?: ""
        }

        tblColVal.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                treeTableRow.item?.let { "%02X".format(it.b) } ?: ""
        }

        tblColTyp.cellValueFactory = callback { it.value.value.typeProperty }
        tblColTyp.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                item?.name?.let { it[0] + it.substring(1).toLowerCase() }
        }

        tblColM.cellValueFactory = callback { it.value.value.state }
        tblColM.cellFactory = treeTableCell { _, empty ->
            text = when {
                empty -> ""
                treeTableRow.item.state.m16 -> "16"
                else -> "8"
            }
        }

        tblColI.cellValueFactory = callback { it.value.value.state }
        tblColI.cellFactory = treeTableCell { _, empty ->
            text = when {
                empty -> ""
                treeTableRow.item.state.x16 -> "16"
                else -> "8"
            }
        }

        tblColMode.cellValueFactory = callback { it.value.value.state }
        tblColMode.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                treeTableRow.item.state.mode.name
        }

        tblColPbr.cellValueFactory = callback { it.value.value.state }
        tblColPbr.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                "%02X".format(treeTableRow.item.state.pbr)
        }

        tblColDbr.cellValueFactory = callback { it.value.value.state }
        tblColDbr.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                "%02X".format(treeTableRow.item.state.dbr)
        }

        tblColDir.cellValueFactory = callback { it.value.value.state }
        tblColDir.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                "%04X".format(treeTableRow.item.state.direct)
        }

        tblColSta.cellValueFactory = callback { it.value.value.state }
        tblColSta.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else {
                val s = StringBuilder()

                with (treeTableRow.item.state) {
                    s.append(if (emulation) 'E' else 'N').append('-')
                    s.append(if (negative) 'N' else 'n')
                    s.append(if (overflow) 'V' else 'v')
                    s.append(if (memory) 'M' else 'm')
                    s.append(if (index) 'X' else 'x')
                    s.append(if (decimal) 'D' else 'd')
                    s.append(if (irq) 'I' else 'i')
                    s.append(if (zero) 'Z' else 'z')
                    s.append(if (carry) 'C' else 'c')
                }

                s.toString()
            }
        }

        tblColIns.cellFactory = treeTableCell { _, empty ->
            text = if (empty || treeTableRow.item == null || controller.project == null)
                ""
            else {
                val byte = treeTableRow.item
                val inst = instruction(byte.b)

                val s = StringBuilder(inst.operation.symbol)

                val project = controller.project?: return@treeTableCell

                val operand = inst.getOperandBytes(project, byte)

                if (operand.isNotEmpty()) {
                    s.append(' ').append(inst.addressMode.format(operand))
                }

                s.toString()
            }
        }

        var startIndex = 0

        tblRom.rowFactory = callback {
            object : TreeTableRow<ROMByte>() {
                val typeListener = ChangeListener<ROMByteType> { _, o, _ -> typeChanged(o) }

                init {
                    setOnDragDetected {
                        startFullDrag()
                        startIndex = index
                        treeTableView.selectionModel.clearAndSelect(startIndex)
                    }

                    setOnMouseDragEntered {
                        treeTableView.selectionModel.clearSelection()
                        if (index > startIndex)
                            treeTableView.selectionModel.selectRange(startIndex, index + 1)
                        else
                            treeTableView.selectionModel.selectRange(index, startIndex + 1)
                    }
                }

                override fun updateItem(item: ROMByte?, empty: Boolean) {
                    val old = this.item?.type

                    if (this.item != null) {
                        this.item.typeProperty.removeListener(typeListener)
                    }

                    super.updateItem(item, empty)

                    item?.typeProperty?.addListener(typeListener)

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

        tblRom.treeColumn = tblColLbl

        tblRom.selectionModel.selectionMode = SelectionMode.MULTIPLE

        tblRom.setColumnResizePolicy {
            if (it.column?.isResizable == false)
                return@setColumnResizePolicy false

            val scroll = tblRom.lookup(".scroll-bar:vertical") as ScrollBar?

            val tablewidth = tblRom.width
            // adjust the starting width by the amount of the vertical scrollbar (if shown) to avoid the horizontal scrollbar
            // I do not know where the additional number comes from (margin?) i just guessed it by trial and error
            var remWidth = tablewidth - (if (scroll?.isVisible == true) { scroll.width + 3 } else 0.0)
            val columnsToAdjust = mutableListOf<TreeTableColumn<*, *>>()

            for (column in tblRom.columns) {
                when {
                    !column.isVisible -> {
                        continue
                    }
                    column == it.column -> {
                        column.prefWidth = column.width + it.delta
                        remWidth -= column.prefWidth
                    }
                    column.isResizable -> {
                        columnsToAdjust.add(column)
                        remWidth -= column.width
                    }
                    else -> {
                        remWidth -= column.width
                    }
                }
            }

            val adjustment = remWidth / columnsToAdjust.size

            columnsToAdjust.forEach { c -> c.prefWidth = c.width + adjustment }

            true
        }

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
    private fun doFirstBank() {
        setVisibleData(-1, curPage)
    }

    @FXML
    private fun doPrevBank() {
        setVisibleData(curBank - 1, curPage)
    }

    @FXML
    private fun doNextBank() {
        setVisibleData(curBank + 1, curPage)
    }

    @FXML
    private fun doLastBank() {
        setVisibleData(numBanks, curPage)
    }

    @FXML
    private fun doFirstPage() {
        setVisibleData(curBank, -1)
    }

    @FXML
    private fun doPrevPage() {
        setVisibleData(curBank, curPage - 1)
    }

    @FXML
    private fun doNextPage() {
        setVisibleData(curBank, curPage + 1)
    }

    @FXML
    private fun doLastPage() {
        setVisibleData(curBank, numPages)
    }

    private fun setVisibleData(bank: Int, page: Int, updateAlways: Boolean = false) {
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

    private fun forEachRow(action: ROMByte.() -> Unit) {
        val project = controller.project?: return
        tblRom.selectionModel.selectedIndices.forEach {
            project.romBytes[it].action()
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

    companion object {
        const val ACTION_MARK_PREFIX: ActionId = "markByte"
        const val ACTION_SWITCH_M: ActionId = "switchM"
        const val ACTION_SWITCH_X: ActionId = "switchX"
        const val ACTION_SWITCH_MODE: ActionId = "switchMode"
    }
}

typealias Action = () -> Unit
typealias ActionId = String