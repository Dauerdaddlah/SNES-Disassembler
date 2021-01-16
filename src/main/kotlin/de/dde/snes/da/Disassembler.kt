package de.dde.snes.da

import de.dde.snes.da.gui.Gui
import de.dde.snes.da.settings.PreferencesSettings
import de.dde.snes.da.settings.Settings
import javafx.application.Application
import java.util.*

fun main(args: Array<String>) {
    Application.launch(Gui::class.java, *args)

    //val insts = loadInsts()

    //val snes = SNES()
    //snes.loadROM(Paths.get(Inst::class.java.classLoader?.getResource("Legend of Zelda, The - A Link to the Past (Germany).sfc")?.toURI()!!))
}

object Disassembler {
    val settings: Settings = PreferencesSettings()
    val resourceBundle = ResourceBundle.getBundle("${javaClass.packageName}.trl.translations", settings.language)
}

fun translate(key: String) = Disassembler.resourceBundle.getString(key)