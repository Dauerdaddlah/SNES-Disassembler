package de.dde.snes.da.gui

import de.dde.snes.da.gui.hex.HexDataSourceFile
import de.dde.snes.da.gui.hex.HexDataSourceFileCached
import de.dde.snes.da.gui.hex.HexViewer
import de.dde.snes.da.rom.ROMFile
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.Tab
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

    lateinit var v: HexViewer

    val fileProperty: ObjectProperty<ROMFile> = SimpleObjectProperty(this, "file", null)
    var file: ROMFile? by fileProperty

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        v = HexViewer()
        tabRom.content = v
    }

    @FXML
    fun doClose() {
        exitProcess(0)
    }

    @FXML
    fun doOpen() {
        val chooser = FileChooser()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("SNES ROM", "*.sfc"))
        chooser.initialDirectory = Paths.get(".").toFile()
        val file: File? = chooser.showOpenDialog(stage)

        if (file != null) {
            v.hexData = HexDataSourceFileCached(file)

            this.file = ROMFile(file)
            lblFile.text = file.name ?: ""
        }
    }

    @FXML
    fun doOpenFileInfos() {
        file?.let { FileInfosView(it).show() }
    }
}