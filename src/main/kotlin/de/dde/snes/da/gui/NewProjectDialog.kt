package de.dde.snes.da.gui

import de.dde.snes.da.memory.MappingMode
import de.dde.snes.da.memory.ROMFile
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*

class NewProjectDialog(
    val romFile: ROMFile
) {
    @FXML
    private lateinit var txtEmuCop: TextField
    @FXML
    private lateinit var txtEmuBrk: TextField
    @FXML
    private lateinit var txtEmuAbo: TextField
    @FXML
    private lateinit var txtEmuNmi: TextField
    @FXML
    private lateinit var txtEmuRes: TextField
    @FXML
    private lateinit var txtEmuIrq: TextField
    @FXML
    private lateinit var txtNatCop: TextField
    @FXML
    private lateinit var txtNatBrk: TextField
    @FXML
    private lateinit var txtNatAbo: TextField
    @FXML
    private lateinit var txtNatNmi: TextField
    @FXML
    private lateinit var txtNatRes: TextField
    @FXML
    private lateinit var txtNatIrq: TextField

    @FXML
    private lateinit var cmbMode: ComboBox<ComboItem>

    val mappingMode get() = cmbMode.selectionModel.selectedItem.mode

    val alert: Alert

    init {
        alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "New Project ${romFile.file.name}"
        alert.headerText = "bla"
        alert.dialogPane.stylesheets.add(javaClass.getResource("style.css").toExternalForm())

        val loader = FXMLLoader(javaClass.getResource("NewProjectDialog.fxml"))
        loader.setController(this)

        alert.dialogPane.content = loader.load()

        val scores = romFile.scores.filter { it.value != Integer.MIN_VALUE }.map { ComboItem(it.key, it.value) }.sortedByDescending { it.score }
        scores.forEach { cmbMode.items.add(it) }
        cmbMode.selectionModel.select(0)
        cmbMode.selectionModel.selectedItemProperty().addListener { _, _, _ -> cmbSelectionChanged() }
        cmbSelectionChanged()
    }

    fun show(): Boolean {
        alert.showAndWait()
        return alert.result == ButtonType.OK
    }

    private fun cmbSelectionChanged() {
        val header = cmbMode.selectionModel.selectedItem.mode.readHeader(romFile.bytes)

        txtEmuCop.text = "%04X".format(header.emulationVectors.cop)
        txtEmuBrk.text = "%04X".format(header.emulationVectors.brk)
        txtEmuAbo.text = "%04X".format(header.emulationVectors.abort)
        txtEmuNmi.text = "%04X".format(header.emulationVectors.nmi)
        txtEmuRes.text = "%04X".format(header.emulationVectors.reset)
        txtEmuIrq.text = "%04X".format(header.emulationVectors.irq)
        txtNatCop.text = "%04X".format(header.nativeVectors.cop)
        txtNatBrk.text = "%04X".format(header.nativeVectors.brk)
        txtNatAbo.text = "%04X".format(header.nativeVectors.abort)
        txtNatNmi.text = "%04X".format(header.nativeVectors.nmi)
        txtNatRes.text = "%04X".format(header.nativeVectors.reset)
        txtNatIrq.text = "%04X".format(header.nativeVectors.irq)

        val textfields = listOf(txtEmuCop, txtEmuBrk, txtEmuAbo, txtEmuNmi, txtEmuRes, txtEmuIrq, txtNatCop, txtNatBrk, txtNatAbo, txtNatNmi, txtNatRes, txtNatIrq)

        val mapClasses = mutableMapOf<String, String>()
        mapClasses["0000"] = "zero"
        mapClasses["FFFF"] = "ffff"

        var cnt = 1

        for (txt in textfields) {
            val classes = mutableListOf("address")

            if (txt.text !in mapClasses)
                mapClasses[txt.text] = "txt${cnt++}"

            classes.add(mapClasses[txt.text]!!)

            txt.styleClass.setAll(classes)
        }
    }

    private class ComboItem(val mode: MappingMode, val score: Int) {
        override fun toString(): String {
            return "${mode.name} - $score"
        }
    }
}