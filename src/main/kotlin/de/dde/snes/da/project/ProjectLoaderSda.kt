package de.dde.snes.da.project

import de.dde.snes.da.Disassembler
import de.dde.snes.da.memory.ROMByteType
import de.dde.snes.da.memory.ROMFile
import de.dde.snes.da.memory.allMappingModes
import java.io.BufferedWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ProjectLoaderSda(
        override val path: Path
) : ProjectLoader {
    override fun save(project: Project) {
        Files.newBufferedWriter(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE).use {
            it.writeLn(project.romFile.file.absolutePath)
            it.writeLn(project.mappingMode.name)

            for (byte in project.romBytes) {
                if (byte.label.isNullOrBlank())
                    byte.label = null

                if (byte.comment.isNullOrBlank())
                    byte.comment = null

                if (byte.label != null || byte.comment != null || byte.type != ROMByteType.UNKNOWN) {
                    it.writeLn(byte.index.toString(16))
                    it.write(if (byte.label != null) "1" else "0")
                    it.write(if (byte.comment != null) "1" else "0")
                    it.write(if (byte.type != ROMByteType.UNKNOWN) "1" else "0")
                    it.newLine()

                    byte.label?.let { l -> it.writeLn(l) }
                    byte.comment?.let { c -> it.writeLn(c) }
                    if (byte.type != ROMByteType.UNKNOWN)
                        it.writeLn(byte.type.name)
                }
            }
        }
    }

    override fun load(): Project {
        return Files.newBufferedReader(path).use {
            val romFile = ROMFile(Paths.get(it.readLine()).toFile())
            val modeName = it.readLine()
            val mappingMode = allMappingModes.find { mode -> mode.name == modeName } ?: error("unknown mappingmode<$modeName>")

            val p = Project(romFile, mappingMode)

            while (true) {
                val index = it.readLine()?.toInt(16)?: break
                val bits = it.readLine()?: break

                val label  = if (bits[0] == '1') { it.readLine() ?: break } else null
                val comment = if (bits[1] == '1') { it.readLine() ?: break } else null
                val type = if (bits[2] == '1') { ROMByteType.valueOf(it.readLine() ?: break) } else ROMByteType.UNKNOWN

                with (p.romBytes[index]) {
                    this.label = label
                    this.comment = comment
                    this.type = type
                }
            }

            p
        }
    }

    private fun BufferedWriter.writeLn(line: String) {
        write(line)
        newLine()
    }

    companion object {
        val FACTORY = object : ProjectLoaderFactory {
            override val translation: String
                get() = Disassembler.resourceBundle.getString("de.dde.snes.da.open.sdaFile")
            override val fileExtension: String
                get() = "sda"

            override fun buildLoader(path: Path): ProjectLoader {
                return ProjectLoaderSda(path)
            }
        }
    }
}