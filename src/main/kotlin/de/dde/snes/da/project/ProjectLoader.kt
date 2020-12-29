package de.dde.snes.da.project

import de.dde.snes.da.rom.MappingMode
import de.dde.snes.da.rom.ROMFile
import de.dde.snes.da.rom.allMappingModes
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ProjectLoader(
        val path: Path
) {
    fun save(project: Project) {
        Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE).use {
            it.writeLn(project.romFile.file.absolutePath)
            it.writeLn(project.mappingMode.name)
        }
    }

    fun load(): Project {
        return Files.newBufferedReader(path).use {
            val romFile = ROMFile(Paths.get(it.readLine()).toFile())
            val modeName = it.readLine()
            val mappingMode = allMappingModes.find { mode -> mode.name == modeName } ?: error("unknown mappingmode<$modeName>")

            Project(romFile, mappingMode)
        }
    }

    private fun BufferedWriter.writeLn(line: String) {
        write(line)
        newLine()
    }
}