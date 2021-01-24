package de.dde.snes.da.settings

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

class PropertiesSettings(
        val propertiesPath: Path,
        val default: Properties? = null
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
    val _lastProjects = mutableListOf<Path>()
    override val lastProjects: List<Path>
        get() = _lastProjects

    init {
        load()
    }

    override fun addLastProject(project: Path) {
        val p = project.toAbsolutePath()

        if (p in _lastProjects)
            _lastProjects.remove(p)

        // ensure the last one opened is always on top
        _lastProjects.add(0, p)

        val cnt = lastProjectsCount
        while (_lastProjects.size > cnt)
            _lastProjects.removeLast()

        _lastProjects.forEachIndexed { index, path -> this.p.setProperty("$PROP_LASTPROJECTS.$index", path.toString()) }

        save()
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
    }
}