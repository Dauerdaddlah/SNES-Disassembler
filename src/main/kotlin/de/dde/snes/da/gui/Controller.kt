package de.dde.snes.da.gui

import de.dde.snes.da.project.Project
import de.dde.snes.da.gui.hex.HexDataSourceByteArray
import de.dde.snes.da.gui.hex.HexViewer
import de.dde.snes.da.memory.ROMByte
import de.dde.snes.da.memory.ROMByteType
import de.dde.snes.da.processor.instruction
import de.dde.snes.da.project.ProjectLoader
import de.dde.snes.da.rom.ROMFile
import de.dde.snes.da.settings.PreferencesSettings
import de.dde.snes.da.settings.Settings
import de.dde.snes.da.util.callback
import de.dde.snes.da.util.getValue
import de.dde.snes.da.util.setValue
import de.dde.snes.da.util.treeTableCell
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

class Controller(
    private val stage: Stage
) : Initializable {

    @FXML
    lateinit var root: BorderPane
    @FXML
    lateinit var tabRom: Tab

    @FXML
    lateinit var lblFile: Label

    @FXML
    lateinit var mnuOpenLast: Menu
    @FXML
    lateinit var mnuItemSaveAs: MenuItem
    @FXML
    lateinit var mnuItemSave: MenuItem

    @FXML
    lateinit var tblRom: TreeTableView<ROMByte>
    @FXML
    lateinit var tblColLbl: TreeTableColumn<ROMByte, String>
    @FXML
    lateinit var tblColOff: TreeTableColumn<ROMByte, Any>
    @FXML
    lateinit var tblColAdd: TreeTableColumn<ROMByte, Number>
    @FXML
    lateinit var tblColVal: TreeTableColumn<ROMByte, Any>
    @FXML
    lateinit var tblColIns: TreeTableColumn<ROMByte, Any>
    @FXML
    lateinit var tblColCom: TreeTableColumn<ROMByte, String>

    lateinit var v: HexViewer

    val settings: Settings = PreferencesSettings()

    val fileProperty: ObjectProperty<ROMFile> = SimpleObjectProperty(this, "file", null)
    var file: ROMFile? by fileProperty

    val projectProperty: ObjectProperty<Project> = SimpleObjectProperty(this, "project", null)
    var project: Project? by projectProperty

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        v = HexViewer()

        tabRom.content = v
        tblRom.isShowRoot = false

        tblColLbl.cellValueFactory = callback { it?.value?.value?.labelProperty }
        tblColCom.cellValueFactory = callback { it?.value?.value?.commentProperty }

        tblColLbl.cellFactory = TextFieldTreeTableCell.forTreeTableColumn()
        tblColCom.cellFactory = TextFieldTreeTableCell.forTreeTableColumn()

        tblColOff.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                treeTableRow.item?.let { "%06X".format(it.index) } ?: ""
        }

        tblColAdd.cellFactory = treeTableCell { _, empty ->
            text = if (empty) ""
            else treeTableRow.item?.let { project?.mappingMode?.toSnesAddress(it.index)?.let { addr -> "%06X".format(addr) } }?: ""
        }

        tblColVal.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                treeTableRow.item?.let { "%02X".format(it.b) } ?: ""
        }

        tblColIns.cellFactory = treeTableCell { _, empty ->
            text = if (empty)
                ""
            else
                treeTableRow.item?.b?.let { instruction(it).operation.symbol }?: ""
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

        projectProperty.addListener { _, _, project ->
            lblFile.text = project?.romFile?.file?.name?: ""
            v.hexData = project?.let { HexDataSourceByteArray(it.romFile.bytes) }?: null

            val root = TreeItem<ROMByte>(null)
            project?.romBytes?.forEach { root.children.add(TreeItem(it)) }
            tblRom.root = root
        }

        mnuOpenLast.disableProperty().bind(Bindings.isEmpty(mnuOpenLast.items))
        refreshMnuLastOpened()
    }

    @FXML
    fun doClose() {
        exitProcess(0)
    }

    @FXML
    fun doOpen() {
        val chooser = FileChooser()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("SNES ROM", "*.sfc"))
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("SNES Disassembly Project", "*.sda"))
        chooser.initialDirectory = (settings.lastFileOpened?.parent ?: Paths.get(".")).toFile()
        val file: File? = chooser.showOpenDialog(stage)

        if (file != null) {
            settings.lastFileOpened = file.toPath()

            if (file.extension.toLowerCase() == "sda") {
                doOpenProject(file)
            } else {
                doOpenRom(file)
            }
        }
    }

    private fun doOpenProject(project: File) {
        val loader = ProjectLoader(project.toPath())
        this.project = loader.load()
        this.project?.loader = loader
    }

    private fun doOpenRom(rom: File) {
        val romFile = ROMFile(rom)
        this.file = romFile

        val d = NewProjectDialog(romFile)

        if (d.show()) {
            val project = Project(romFile, d.mappingMode)

            this.project = project
        }
    }

    @FXML
    fun doOpenFileInfos() {
        file?.let { FileInfosView(it).show() }
    }

    @FXML
    fun doSave() {
        val project = project ?: return

        project.loader?.save(project)

        project.loader?.path?.let { settings.addLastProject(it); refreshMnuLastOpened() }
    }

    @FXML
    fun doSaveAs() {
        val project = project?: return

        val chooser = FileChooser()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("SNES ROM", "*.sda"))
        chooser.initialDirectory = (settings.lastFileOpened?.parent ?: Paths.get(".")).toFile()
        chooser.initialFileName = project.romFile.file.name.let { it.substring(0, it.lastIndexOf('.')) + ".sda" }

        val file = chooser.showSaveDialog(stage)
        file?: return

        project.loader = ProjectLoader(file.toPath())

        doSave()
    }

    private fun refreshMnuLastOpened() {
        mnuOpenLast.items.clear()

        for (path in settings.lastProjects) {
            val item = MenuItem(path.last().toString())
            item.onAction = EventHandler { doOpenProject(path.toFile()) }

            mnuOpenLast.items.add(item)
        }
    }
}