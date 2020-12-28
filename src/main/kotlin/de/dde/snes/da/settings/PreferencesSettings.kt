package de.dde.snes.da.settings

import de.dde.snes.da.SNES
import java.nio.file.Path
import java.nio.file.Paths
import java.util.prefs.Preferences

class PreferencesSettings : Settings {
    val root = Preferences.userNodeForPackage(SNES::class.java)

    override var lastFileOpened: Path?
        get() = root.get(LASTFILEOPENED, null)?.let { Paths.get(it) }
        set(value) { root.put(LASTFILEOPENED, value.toString()) }

    companion object {
        private const val LASTFILEOPENED = "lastFileOpened"
    }
}