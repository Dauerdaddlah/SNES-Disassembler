package de.dde.snes.da.gui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(Gui::class.java, *args)
}

class Gui : Application() {
    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("Gui.fxml"))
        loader.setController(Controller(primaryStage))
        primaryStage.scene = Scene(loader.load())
        primaryStage.scene.stylesheets.add(javaClass.getResource("style.css").toExternalForm())
        primaryStage.title = "SNES Disassembler"
        primaryStage.show()
    }
}