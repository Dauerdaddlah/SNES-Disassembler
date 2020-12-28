package de.dde.snes.da.gui

import de.dde.snes.da.*
import de.dde.snes.da.rom.*
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*
import kotlin.math.pow

class FileInfosView(
    val file: ROMFile
) : Initializable {
    private val stage: Stage

    @FXML
    lateinit var table: TableView<Pair<String, String>>
    @FXML
    lateinit var colKey: TableColumn<Pair<String, String>, String>
    @FXML
    lateinit var colValue: TableColumn<Pair<String, String>, String>

    init {
        val loader = FXMLLoader(javaClass.getResource("FileInfosView.fxml"))
        loader.setController(this)

        stage = Stage(StageStyle.UTILITY)
        stage.scene = Scene(loader.load())
        stage.focusedProperty().addListener { _, _, n -> if (!n) stage.hide() }

        table.items.addAll(
                "file" to file.file.name,
                "valid" to file.valid.toString(),
                "smcHeader" to file.hasSmcHeader.toString())

        if (file.valid) {
            table.items.addAll(
                    LoROM.name to file.scores[LoROM].toString(),
                    HiROM.name to file.scores[HiROM].toString(),
                    ExLoROM.name to file.scores[ExLoROM].toString(),
                    ExHiROM.name to file.scores[ExHiROM].toString(),

                    "MappingMode" to file.mappingMode?.name.toString())

            file.snesHeader?.let {
                table.items.addAll(
                        "header version" to it.headerVersion.toString(),
                        "ROM name" to it.romName,
                        "Raw Mapping Mode" to "${it.mappingMode} (0x${it.mappingMode.toString(16)})",
                        "cartridgeType" to it.cartridgeType.toString(),
                        "romSize" to "${it.romSize} (${sizeToString(it.romSize.size)})",
                        "ramSize" to "${it.ramSize} (${sizeToString(it.ramSize.size)})",
                        "region" to "${it.region}",
                        "devId" to it.devId.toString(),
                        "ROM version" to it.romVersion.toString(),
                        "complement" to "${it.complement} (0x${it.complement.toString(16)})",
                        "checksum" to "${it.checksum} (0x${it.checksum.toString(16)})"
                )

                if (it.headerVersion > 1) {
                    table.items.add("co CPU Type" to it.coCpuType.toString())
                }

                if(it.headerVersion > 2) {
                    table.items.addAll(
                            "gameCode" to it.gameCode,
                            "flash memory" to "${it.flash} (${sizeToString(it.flash)})",
                            "ex RAM size" to "${it.exRamSize} (${sizeToString(it.exRamSize.size)})",
                            "special version" to it.specialVersion.toString()
                    )
                }

                table.items.addAll(
                        "Emulation BRK" to it.emulationVectors.brk.toString(16),
                        "Emulation ABORT" to it.emulationVectors.abort.toString(16),
                        "Emulation COP" to it.emulationVectors.cop.toString(16),
                        "Emulation IRQ" to it.emulationVectors.irq.toString(16),
                        "Emulation NMI" to it.emulationVectors.nmi.toString(16),
                        "Emulation RESET" to it.emulationVectors.reset.toString(16),

                        "Native BRK" to it.nativeVectors.brk.toString(16),
                        "Native ABORT" to it.nativeVectors.abort.toString(16),
                        "Native COP" to it.nativeVectors.cop.toString(16),
                        "Native IRQ" to it.nativeVectors.irq.toString(16),
                        "Native NMI" to it.nativeVectors.nmi.toString(16),
                        "Native RESET" to it.nativeVectors.reset.toString(16),
                )
            }
        }
    }

    fun sizeToString(size: Byte): String {
        val s = 2.0.pow(size.toInt()).toInt()
        return if (s < _1K) {
            "$s kB"
        } else {
            "${s shr 10} MB"
        }

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        table.items = FXCollections.observableArrayList()

        colKey.cellValueFactory = PropertyValueFactory("first")
        colValue.cellValueFactory = PropertyValueFactory("second")
    }

    fun show() {
        stage.show()
    }
}