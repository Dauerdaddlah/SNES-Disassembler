package de.dde.snes.da.gui

import de.dde.snes.da.Disassembler
import de.dde.snes.da.Disassembler.settings
import de.dde.snes.da.project.Project
import de.dde.snes.da.gui.hex.HexDataSourceByteArray
import de.dde.snes.da.gui.hex.HexViewer
import de.dde.snes.da.gui.table.TableControl
import de.dde.snes.da.project.ProjectLoaderSda
import de.dde.snes.da.memory.ROMFile
import de.dde.snes.da.project.ProjectLoader
import de.dde.snes.da.project.ProjectLoaderDiztinguish
import de.dde.snes.da.util.getValue
import de.dde.snes.da.util.setValue
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
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
    lateinit var tabRomRaw: Tab

    @FXML
    lateinit var lblFile: Label

    @FXML
    lateinit var mnuOpenLast: Menu
    @FXML
    lateinit var mnuItemSaveAs: MenuItem
    @FXML
    lateinit var mnuItemSave: MenuItem

    lateinit var v: HexViewer
    lateinit var table: TableControl

    val fileProperty: ObjectProperty<ROMFile> = SimpleObjectProperty(this, "file", null)
    var file: ROMFile? by fileProperty

    val projectProperty: ObjectProperty<Project> = SimpleObjectProperty(this, "project", null)
    var project: Project? by projectProperty

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        v = HexViewer()
        table = TableControl(this)

        tabRom.content = table.root
        tabRomRaw.content = v

        projectProperty.addListener { _, _, project ->
            lblFile.text = project?.romFile?.file?.name?: ""
            v.hexData = project?.let { HexDataSourceByteArray(it.romFile.bytes) }?: null
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
        chooser.title = Disassembler.resourceBundle.getString("de.dde.snes.da.open.title")

        chooser.extensionFilters.add(FileChooser.ExtensionFilter(Disassembler.resourceBundle.getString("de.dde.snes.da.open.allFile"), "*.*"))
        ProjectLoader.getAllLoaders().forEach {
            chooser.extensionFilters.add(FileChooser.ExtensionFilter(it.translation, "*.${it.fileExtension}"))
        }
        chooser.initialDirectory = (settings.lastFileOpened?.parent ?: Paths.get(".")).toFile()
        val file: File? = chooser.showOpenDialog(stage)

        if (file != null) {
            settings.lastFileOpened = file.toPath()

            if (file.extension.equals("sfc", true)) {
                doOpenRom(file)
            } else {
                doOpenProject(file)
            }
        }
    }

    private fun doOpenProject(project: File) {
        val loaderFactory = ProjectLoader.getAllLoaders().find { it.fileExtension.equals(project.extension, true) }?: ProjectLoaderSda.FACTORY
        val loader = loaderFactory.buildLoader(project.toPath())

        val p = loader.load()
        p.loader = loader
        this.project = p
        this.file = p.romFile

        settings.addLastProject(project.toPath())
        refreshMnuLastOpened()
    }

    private fun doOpenRom(rom: File) {
        val romFile = ROMFile(rom)
        this.file = romFile

        val d = NewProjectDialog(romFile)

        if (d.show()) {
            val project = Project(romFile, d.mappingMode)

            project.markStart()

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
        ProjectLoader.getAllLoaders().forEach {
            chooser.extensionFilters.add(FileChooser.ExtensionFilter(it.translation, "*.${it.fileExtension}"))
        }
        chooser.initialDirectory = (settings.lastFileOpened?.parent ?: Paths.get(".")).toFile()
        chooser.initialFileName = project.romFile.file.name.let { it.substring(0, it.lastIndexOf('.')) }

        val file = chooser.showSaveDialog(stage)
        file?: return

        val loader = ProjectLoader.getAllLoaders().find { it.fileExtension.equals(file.extension, true) }?.buildLoader(file.toPath())?: return
        project.loader = loader

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