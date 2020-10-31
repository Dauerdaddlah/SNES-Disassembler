package de.dde.snes.da

import java.nio.file.Files
import java.nio.file.Paths

fun loadInsts(): List<Inst> {
    val file = Paths.get(Inst::class.java.classLoader?.getResource("CPU_Instructions.txt")?.toURI()!!)

    val line = Files.readAllLines(file)

    val insts = line.subList(1, (0xFF + 1)).map {
        val split = it.split('\t').map { it.trim() }

        val symbol = split[0].substring(0, 3)
        val alias = if (split[1].isEmpty()) null else split[2]
        val description = split[2]
        val opcode = split[3].toInt(16)
        val addressMode = split[4].let { am ->
            if (am.startsWith("Stack (") || am.endsWith("Interrupt"))
                "Stack"
            else if (am.startsWith("SR"))
                am.replace("SR", "Stack Relative")
            else if (am.startsWith("DP"))
                am.replace("DP", "Direct Page")
            else am
        }
        val flags = split[5].filter { it != '-' }.toSet()
        val (bytes, bytesFlags) = split[6].let { b ->
            if (!b.contains('['))
                b.toInt() to emptySet<Int>()
            else {
                val f = mutableSetOf<Int>()

                val byt = if (b.contains('[')) b.substring(0, b.indexOf('[')).toInt() else b.toInt()

                var s = b

                while (s.contains('[')) {
                    val i = s.indexOf('^')
                    val j = s.indexOf(']')

                    f.add(s.substring(i + 1, j).toInt())

                    s = s.substring(j + 1)
                }

                byt to f
            }
        }
        val (cycles, cyclesFlags) = split[7].let { b ->
            if (!b.contains('['))
                b.toInt() to emptySet<Int>()
            else {
                val f = mutableSetOf<Int>()

                val byt = if (b.contains('[')) b.substring(0, b.indexOf('[')).toInt() else b.toInt()

                var s = b

                while (s.contains('[')) {
                    val i = s.indexOf('^')
                    val j = s.indexOf(']')

                    f.add(s.substring(i + 1, j).toInt())

                    s = s.substring(j + 1)
                }

                byt to f
            }
        }

        Inst(symbol, alias, description, opcode, addressMode, flags, bytes, bytesFlags, cycles, cyclesFlags)
    }

    return insts.sortedBy { it.opcode }
}

data class Inst(
    val symbol: String,
    val alias: String?,
    val description: String,
    val opcode: Int,
    val addressMode: String,
    val flags: Set<Char>,
    val bytes: Int,
    val bytesFlags: Set<Int>,
    val cycles: Int,
    val cyclesFlags: Set<Int>
)