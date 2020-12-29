package de.dde.snes.da.settings

import java.nio.file.Path
import java.util.*

interface Settings {
    var language: Locale

    var lastFileOpened: Path?

    var lastProjectsCount: Int
    val lastProjects: List<Path>

    fun addLastProject(project: Path)
}