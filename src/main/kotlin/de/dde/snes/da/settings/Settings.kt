package de.dde.snes.da.settings

import java.nio.file.Path

interface Settings {
    var lastFileOpened: Path?

    var lastProjectsCount: Int
    val lastProjects: List<Path>

    fun addLastProject(project: Path)
}