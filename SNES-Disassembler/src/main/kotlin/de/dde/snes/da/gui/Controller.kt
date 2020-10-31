package de.dde.snes.da.gui

import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

class Controller(
    private val stage: Stage
) {
    @FXML
    lateinit var root: BorderPane
    lateinit var v: HexView

    @FXML
    private fun initialize() {
        v = HexView()
        root.center = v
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

        v.file = file
    }
}