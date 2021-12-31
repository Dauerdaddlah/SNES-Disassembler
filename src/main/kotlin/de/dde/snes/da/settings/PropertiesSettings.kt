package de.dde.snes.da.settings

import de.dde.snes.da.gui.table.ActionId
import javafx.scene.input.KeyCombination
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

class PropertiesSettings(
        val propertiesPath: Path,
        default: Properties? = null
) : Settings {
    val p = Properties(default)

    override var language: Locale
        get() = Locale.forLanguageTag(p.getProperty(PROP_LANGUAGE, Locale.GERMAN.toLanguageTag()))
        set(value) {
            p.setProperty(PROP_LANGUAGE, value.toLanguageTag())
            save()
        }
    override var lastFileOpened: Path?
        get() = p.getProperty(PROP_LASTOPENED).let { if (it.isNullOrEmpty()) null else Paths.get(it) }
        set(value) {
            p.setProperty(PROP_LASTOPENED, value?.toAbsolutePath()?.toString()?: "")
            save()
        }
    override var lastProjectsCount: Int
        get() = p.getProperty(PROP_NUMLASTPROJECTS, "10").toInt()
        set(value) {
            p.setProperty(PROP_NUMLASTPROJECTS, value.toString())
            save()
        }

    private var _lastProjects = mutableListOf<Path>()
    override var lastProjects: List<Path>
        get() = _lastProjects
        set(value) {
            var i = 0

            while (i < value.size) {
                p.setProperty("$PROP_LASTPROJECTS$i", value[i].toAbsolutePath().toString())
                i++
            }
            while (i < _lastProjects.size) {
                p.remove("$PROP_LASTPROJECTS$i")
                i++
            }

            _lastProjects.clear()
            _lastProjects.addAll(value)

            save()
        }

    override var inputMap: Map<KeyCombination, ActionId>
        get() {
            val map = mutableMapOf<KeyCombination, ActionId>()

            for (key in p.keys) {
                if (key is String && key.startsWith(PROP_INPUT)) {
                    val name = key.substring(PROP_INPUT.length + 1)
                    val action: ActionId = p.getProperty(key)?: continue

                    val key = KeyCombination.valueOf(name)

                    map[key] = action
                }
            }

            return map
        }
        set(value) {
            for (key in p.keys.toMutableSet()) {
                if (key is String && key.startsWith(PROP_INPUT)) {
                    p.remove(key)
                }
            }

            value.forEach { (key, value) ->
                p["$PROP_INPUT.${key.name}"] = value
            }

            save()
        }


    init {
        load()
    }

    fun load() {
        if (Files.exists(propertiesPath))
            p.load(
                    Files.newBufferedReader(
                            propertiesPath,
                            Charsets.UTF_8
                    ))

        _lastProjects.clear()

        var i = 0
        while (true) {
            val s = p.getProperty("$PROP_LASTPROJECTS.$i", "")

            if (s.isNullOrEmpty())
                break

            val path = Paths.get(s)

            _lastProjects.add(path)

            i++
        }
    }

    fun save() {
        p.store(
                Files.newBufferedWriter(
                        propertiesPath,
                        Charsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING),
                "Configuration for SNES-Disassembler"
        )
    }

    companion object {
        const val PROP_PREFIX = "de.dde.snes.da"

        const val PROP_LANGUAGE = "$PROP_PREFIX.language"
        const val PROP_LASTOPENED = "$PROP_PREFIX.lastOpened"
        const val PROP_NUMLASTPROJECTS = "$PROP_PREFIX.numLastProjects"
        const val PROP_LASTPROJECTS = "$PROP_PREFIX.lastProjects"
        const val PROP_INPUT = "$PROP_PREFIX.input"
    }
}