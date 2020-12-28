package de.dde.snes.da.gui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import java.lang.Exception

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
        try {
            // https://icon-library.com/icon/super-nintendo-icon-11.html
            // <a href="https://icon-library.net/icon/super-nintendo-icon-11.html">Super Nintendo Icon #94543</a>
            primaryStage.icons.add(Image(javaClass.getResource("super-nintendo-icon-11.jpg").toExternalForm()))
        } catch(e: Exception) {
            e.printStackTrace()
        }
        primaryStage.show()
    }
}