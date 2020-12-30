package de.dde.snes.da.memory

import java.io.File
import java.nio.file.Files

/**
 * A file that may or may not be a valid rom<br>
 * it contains the whole content of the file as well as a first analyses of a possible smc and snes-header
 */
class ROMFile(
    val file: File
) {
    /** the whole file */
    val bytesAll: ByteArray
    /** the whole file without the smc-header if present */
    val bytes: ByteArray

    /** whether this file could be a valid SNES-ROM */
    val valid: Boolean
        get() = snesHeader != null
    /** whether this file had arecognised smc-header */
    val hasSmcHeader: Boolean
        get() = valid && bytesAll != bytes

    /** the scores each possible mappingMode had. The highest is the most likely mode for this file */
    val scores: Map<MappingMode, Int>

    /** The most likely mappingMode for this file, null if no one shall be possible */
    val mappingMode: MappingMode?
    /** The snesHeader according to the most likely mappingMode */
    val snesHeader: RomHeader?

    init {
        bytesAll = Files.readAllBytes(file.toPath())
        bytes = when (bytesAll.size.rem(0x400)) {
            0 -> bytesAll
            0x200 -> bytesAll.copyOfRange(0x200, bytesAll.size)
            else -> byteArrayOf()
        }

        val modes = allMappingModes
        scores = modes.map { it to it.score(bytes) }.toMap()

        if (scores.any { it.value > 0 }) {
            mappingMode = scores.maxByOrNull { it.value }!!.key
            snesHeader = mappingMode.readHeader(bytes)
        } else {
            mappingMode = null
            snesHeader = null
        }
    }
}