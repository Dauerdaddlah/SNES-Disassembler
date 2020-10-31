package de.dde.snes.da.gui

import java.awt.BorderLayout
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.system.exitProcess

fun main() {
    SwingUtilities.invokeLater { DisassemblerGui() }
}

class DisassemblerGui {
    val frame: JFrame

    init {
        frame = JFrame()
        frame.title = "SNES Disassembler"
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        
        frame.contentPane.layout = BorderLayout()
        
        frame.jMenuBar = buildMenubar()
        
        frame.contentPane = buildContent()

        frame.pack()
        frame.setLocationRelativeTo(null)

        frame.isVisible = true
    }

    private fun buildContent(): JPanel {
        val content = JPanel()

        content.layout = BorderLayout()
        //content.add(HexViewer())
        return content
    }

    private fun buildMenubar(): JMenuBar {
        val menu = JMenuBar()

        val menuFile = JMenu("File")

        val itemClose = JMenuItem("Close")
        itemClose.addActionListener { exitProcess(0) }

        val itemOpen = JMenuItem("Open")
        itemOpen.addActionListener { openFile() }

        menuFile.add(itemOpen)
        menuFile.add(itemClose)
        menu.add(menuFile)

        return menu
    }

    private fun openFile() {
        val chooser = JFileChooser()
        chooser.addChoosableFileFilter(FileNameExtensionFilter("SNES-ROMS", "sfc"))

        val option = chooser.showOpenDialog(frame)

        if (option == JFileChooser.APPROVE_OPTION) {
            val file = chooser.selectedFile
        }
    }
}
