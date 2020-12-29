package de.dde.snes.da.settings

import de.dde.snes.da.SNES
import java.nio.file.Path
import java.nio.file.Paths
import java.util.prefs.Preferences

class PreferencesSettings : Settings {
    override var lastProjectsCount: Int
        get() = root.getInt(LASTPROJECTSCOUNT, 10)
        set(value) { if (value in 1..20) root.putInt(LASTPROJECTSCOUNT, value) }
    val _lastProjects = mutableListOf<Path>()
    override val lastProjects: List<Path>
        get() = _lastProjects

    val root = Preferences.userNodeForPackage(SNES::class.java)

    override var lastFileOpened: Path?
        get() = root.get(LASTFILEOPENED, null)?.let { Paths.get(it) }
        set(value) { root.put(LASTFILEOPENED, value.toString()) }

    init {
        for (i in 0..lastProjectsCount) {
            val s = root.get("$LASTPROJECT$i", null)

            s?: break

            _lastProjects.add(Paths.get(s))
        }
    }

    override fun addLastProject(project: Path) {
        val p = project.toAbsolutePath()

        if (p in _lastProjects) {
            _lastProjects.remove(p)
            _lastProjects.add(0, p)
        } else {
            _lastProjects.add(p)
        }

        val cnt = lastProjectsCount
        while (_lastProjects.size > cnt) {
            _lastProjects.removeLast()
        }

        _lastProjects.forEachIndexed { index, path -> root.put("$LASTPROJECT$index", path.toString()) }
    }

    companion object {
        private const val LASTFILEOPENED = "lastFileOpened"
        private const val LASTPROJECTSCOUNT = "LastProjectsCount"
        private const val LASTPROJECT = "LastProject"
    }
}