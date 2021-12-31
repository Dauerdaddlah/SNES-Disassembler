package de.dde.snes.da.settings

import de.dde.snes.da.gui.table.ActionId
import javafx.scene.input.KeyCombination
import java.nio.file.Path
import java.util.*

interface Settings {
    var language: Locale

    var lastFileOpened: Path?

    var lastProjectsCount: Int
    var lastProjects: List<Path>

    var inputMap: Map<KeyCombination, ActionId>
}