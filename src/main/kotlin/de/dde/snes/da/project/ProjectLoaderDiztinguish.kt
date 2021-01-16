package de.dde.snes.da.project

import de.dde.snes.da.*
import de.dde.snes.da.memory.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipInputStream

class ProjectLoaderDiztinguish(
        override val path: Path,
        val compressed: Boolean = path.toFile().extension == "diz"
): ProjectLoader {
    override fun save(project: Project) {
        val bytes = saveRaw(project)

        var stream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
        if (compressed)
            stream = GZIPOutputStream(stream)

        stream.use { it.write(bytes) }
    }

    private fun saveRaw(project: Project): ByteArray {
        val writer = DataWriter()

        writer.writeByte(0) // version
        writer.writeString("DiztinGUIsh")
        writer.writeBytes(0, 244) // project information

        writer.writeByte(when (project.mappingMode) {
            LoROM -> 0
            HiROM -> 1
            ExHiROM -> 2
            ExLoROM -> 7
        })

        writer.writeBoolean(project.header.mappingMode.toInt() and 0x10 != 0x00)

        writer.writeInt(project.romBytes.size)

        writer.writeString("%-21s".format(project.header.romName))

        writer.writeShort(project.header.complement)
        writer.writeShort(project.header.checksum)

        writer.writeString(project.romFile.file.absolutePath, true)

        project.romBytes.forEach { writer.writeByte(it.state.dbr) }
        project.romBytes.forEach { writer.writeByte(it.state.direct.lowByte()) }
        project.romBytes.forEach { writer.writeByte(it.state.direct.highByte()) }
        project.romBytes.forEach { writer.writeBoolean(it.state.index) }
        project.romBytes.forEach { writer.writeBoolean(it.state.memory) }

        project.romBytes.forEach {
            writer.writeByte(when (it.type) {
                ROMByteType.UNKNOWN -> 0x00
                ROMByteType.INSTRUCTION -> 0x10
                ROMByteType.OPERAND -> 0x11
                ROMByteType.DATA -> 0x20
                ROMByteType.FILL -> 0x23
                ROMByteType.TEXT -> 0x60
                ROMByteType.GRAPHICS -> 0x21
                ROMByteType.SOUND -> 0x22
                ROMByteType.POINTER16 -> 0x31
                ROMByteType.POINTER24 -> 0x41
            })
        }

        project.romBytes.forEach { writer.writeByte(0) } // archetecture
        project.romBytes.forEach { writer.writeByte(0) } // points

        val labels = project.romBytes.filter { it.label?.isNotEmpty() ?: false }
        writer.writeInt(labels.size)
        labels.forEach {
            writer.writeInt(it.index)
            writer.writeString(it.label?: "", true)
        }

        val comments = project.romBytes.filter { it.comment?.isNotEmpty() ?: false }
        writer.writeInt(comments.size)
        comments.forEach {
            writer.writeInt(it.index)
            writer.writeString(it.comment?: "", true)
        }

        return writer.bytes()
    }

    override fun load(): Project {
        val bytes = if (compressed) {
            val b = path.toFile().readBytes()
            val zis = GZIPInputStream(ByteArrayInputStream(b))

            zis.readAllBytes()
        } else {
            path.toFile().readBytes()
        }

        val reader = DataReader(bytes)

        val version = reader.nextByte().toInt()

        if (version != 0)
            error("unknown Diztinguish-version $version")

        val diztinguish = reader.nextString(11)

        if (diztinguish != "DiztinGUIsh")
            error("wrong file format - keyword \"DiztinGUIsh\" not found")

        reader.skip(244) // project information

        val mappingMode = when (val mode = reader.nextByte().toInt()) {
            0 -> LoROM
            1 -> HiROM
            2 -> ExHiROM
            3 -> error("SA-1 not supported yet")
            4 -> error("ExSA-1 not supported yet")
            5 -> error("SuperFX not supported yet")
            6 -> error("SuperMMC not supported yet")
            7 -> ExLoROM
            else -> error("unknown mapping mode $mode")
        }

        val speed = reader.nextByte()

        val size = reader.nextInt()

        val internalName = reader.nextString(21)

        val romChecksumComplement = reader.nextShort()

        val romChecksum = reader.nextShort()

        val pathString = reader.nextString()

        val project = Project(ROMFile(Paths.get(pathString).toFile()), mappingMode)

        if (internalName.trim() != project.header.romName.trim()
                || size != project.romBytes.size
                || romChecksum != project.header.checksum
                || romChecksumComplement != project.header.complement
        )
            error("incompatible data in ROM and DiztinGUIsh-Project")

        for (i in 0 until size) {
            project.romBytes[i].state.dbr = Byte(reader.nextByte())
        }

        for (i in 0 until size) {
            project.romBytes[i].state.direct = Byte(reader.nextByte())
        }
        for (i in 0 until size) {
            project.romBytes[i].state.direct = Word(project.romBytes[i].state.direct, Byte(reader.nextByte()))
        }

        for (i in 0 until size) {
            project.romBytes[i].state.index = reader.nextByte() == 1.toByte()
        }

        for (i in 0 until size) {
            project.romBytes[i].state.memory = reader.nextByte() == 1.toByte()
        }

        for (i in 0 until size) {
            project.romBytes[i].type = when (val b = Byte(reader.nextByte())) {
                0x00 -> ROMByteType.UNKNOWN
                0x10 -> ROMByteType.INSTRUCTION
                0x11 -> ROMByteType.OPERAND
                0x20 -> ROMByteType.DATA // Data8Bit
                0x21 -> ROMByteType.GRAPHICS // Graphics
                0x22 -> ROMByteType.SOUND // Music
                0x23 -> ROMByteType.FILL // Empty
                0x30 -> ROMByteType.DATA // Data16Bit
                0x31 -> ROMByteType.POINTER16 // Pointer16Bit
                0x40 -> ROMByteType.DATA // Data24Bit
                0x41 -> ROMByteType.POINTER24 // Pointer24Bit
                0x50 -> ROMByteType.DATA // Data32Bit
                0x51 -> ROMByteType.DATA // Pointer32Bit
                0x60 -> ROMByteType.TEXT // Text
                else -> error("unknown byteType ${"%02X".format(b)}")
            }
        }

        reader.skip(size) // archetecture
        reader.skip(size) // In/Out/End/Read Point

        val lblCount = reader.nextInt()

        repeat (lblCount) {
            val pc = reader.nextInt()
            val lbl = reader.nextString()

            project.romBytes[pc].label = lbl
        }

        val comCount = reader.nextInt()

        repeat (comCount) {
            val pc = reader.nextInt()
            val com = reader.nextString()

            project.romBytes[pc].comment = com
        }

        return project
    }

    private class DataWriter() {
        private val stream = ByteArrayOutputStream()

        fun writeByte(b: Int) {
            stream.write(b)
        }

        fun writeBytes(b: Int, count: Int) {
            repeat (count) {
                writeByte(b)
            }
        }

        fun writeShort(s: Int) {
            writeByte(s.lowByte())
            writeByte(s.highByte())
        }

        fun writeInt(i: Int) {
            writeByte(i.lowByte())
            writeByte(i.highByte())
            writeByte(i.longByte())
            writeByte((i shr 24).asByte())
        }

        fun writeString(s: String, terminator: Boolean = false) {
            stream.writeBytes(s.toByteArray(Charsets.US_ASCII))

            if(terminator)
                writeByte(0)
        }

        fun writeBoolean(b: Boolean) {
            writeByte(if (b) 1 else 0)
        }

        fun bytes() = stream.toByteArray()
    }

    private class DataReader(val bytes: ByteArray) {
        var offset = 0

        fun nextByte() = bytes[offset++]

        fun nextShort(): Int {
            val s = Word(bytes[offset], bytes[offset + 1])

            offset += 2

            return s
        }

        fun nextInt(): Int {
            val i = (Byte(bytes[offset]) shl  0) or (Byte(bytes[offset + 1]) shl 8) or (Byte(bytes[offset + 2]) shl 16) or (Byte(bytes[offset + 3]) shl 24)

            offset += 4

            return i
        }

        fun nextString(): String {
            var length = 1

            while (bytes[offset + length] != 0.toByte())
                length++

            val s = nextString(length)

            offset++ // skip the terminating 00

            return s
        }

        fun nextString(length: Int): String {
            val s = String(bytes, offset, length, Charsets.US_ASCII)

            offset += length

            return s
        }

        fun skip(offset: Int) {
            this.offset += offset
        }
    }

    companion object {
        val FACTORY = object : ProjectLoaderFactory {
            override val translation: String
                get() = Disassembler.resourceBundle.getString("de.dde.snes.da.open.dizFile")
            override val fileExtension: String
                get() = "diz"

            override fun buildLoader(path: Path): ProjectLoader {
                return ProjectLoaderDiztinguish(path, true)
            }
        }

        val FACTORY_RAW = object : ProjectLoaderFactory {
            override val translation: String
                get() = Disassembler.resourceBundle.getString("de.dde.snes.da.open.dizFileRaw")
            override val fileExtension: String
                get() = "dizraw"

            override fun buildLoader(path: Path): ProjectLoader {
                return ProjectLoaderDiztinguish(path, false)
            }
        }
    }
}