package de.dde.snes.da.settings

import de.dde.snes.da.Disassembler
import de.dde.snes.da.gui.table.ActionId
import javafx.scene.input.KeyCombination
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.prefs.Preferences

class PreferencesSettings : Settings {
    val root = Preferences.userNodeForPackage(Disassembler::class.java)

    override var lastFileOpened: Path?
        get() = root.get(LASTFILEOPENED, null)?.let { Paths.get(it) }
        set(value) { root.put(LASTFILEOPENED, value.toString()) }

    override var language: Locale
        get() = Locale.forLanguageTag(root.get(LANGUAGE, Locale.ENGLISH.toLanguageTag()))
        set(value) { root.put(LANGUAGE, value.toLanguageTag()) }

    override var lastProjectsCount: Int
        get() = root.getInt(LASTPROJECTSCOUNT, 10)
        set(value) { if (value in 1..20) root.putInt(LASTPROJECTSCOUNT, value) }

    override var lastProjects: List<Path>
        get() = mutableListOf<Path>().also {
            var i = 0
            while (true) {
                val s = root.get("$LASTPROJECT$i", null)

                s?: break

                it.add(Paths.get(s))
                i++
            }
        }
        set(value) { value.forEachIndexed { index, path -> root.put("$LASTPROJECT$index", path.toString()) } }

    override var inputMap: Map<KeyCombination, ActionId>
        get() {
            val input = root.node(NODE_INPUT)

            val ret = mutableMapOf<KeyCombination, ActionId>()

            for (key in input.keys()) {
                val action: ActionId? = input.get(key, null)

                action?: continue

                TODO("parse key combination")
            }

            return ret
        }
        set(value) {
            val input = root.node(NODE_INPUT)
            input.clear()
            value.forEach { (key, value) -> input.put(key.name, value) }
        }

    companion object {
        private const val LASTFILEOPENED = "lastFileOpened"
        private const val LASTPROJECTSCOUNT = "LastProjectsCount"
        private const val LASTPROJECT = "LastProject"
        private const val LANGUAGE = "Language"
        private const val NODE_INPUT = "inputs"
    }
}