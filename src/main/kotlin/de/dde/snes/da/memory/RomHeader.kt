package de.dde.snes.da.memory

import java.util.*

data class RomHeader(
        val headerVersion: Int,
        val romName: String,
        val mappingMode: Byte,
        val cartridgeType: RomType,
        val romSize: SizeMB,
        val ramSize: SizeKB,
        val region: CountryCode,
        val devId: Licensee, // may be only Byte in headerVersion 1 else a Short
        val romVersion: Byte,
        val complement: Int,
        val checksum: Int,
        val coCpuType: Byte,
        val gameCode: String,
        val flash: Byte,
        val exRamSize: SizeKB,
        val specialVersion: Byte,
        val emulationVectors: Vectors,
        val nativeVectors: Vectors
)

data class Vectors(
        val cop: Int,
        val brk: Int,
        val abort: Int,
        val nmi: Int,
        val reset: Int,
        val irq: Int
)

inline class CountryCode(val code: Byte) {
    fun name() =
            when (code) {
                0.toByte() -> "Japan"
                1.toByte() -> "USA"
                2.toByte() -> "Australia, Europe, Oceania and Asia"
                3.toByte() -> "Sweden"
                4.toByte() -> "Finland"
                5.toByte() -> "Denmark"
                6.toByte() -> "France"
                7.toByte() -> "Holland"
                8.toByte() -> "Spain"
                9.toByte() -> "Germany, Austria and Switzerland"
                10.toByte() -> "Italy"
                11.toByte() -> "Hong Kong and China"
                12.toByte() -> "Indonesia"
                13.toByte() -> "Korea"
                else -> "Unknown"
            }

    fun system() =
            when (code) {
                0.toByte() -> VideoSystem.NTSC
                1.toByte() -> VideoSystem.NTSC
                2.toByte() -> VideoSystem.PAL
                3.toByte() -> VideoSystem.PAL
                4.toByte() -> VideoSystem.PAL
                5.toByte() -> VideoSystem.PAL
                6.toByte() -> VideoSystem.PAL
                7.toByte() -> VideoSystem.PAL
                8.toByte() -> VideoSystem.PAL
                9.toByte() -> VideoSystem.PAL
                10.toByte() -> VideoSystem.PAL
                11.toByte() -> VideoSystem.PAL
                12.toByte() -> VideoSystem.PAL
                13.toByte() -> VideoSystem.PAL
                else -> VideoSystem.UNKNOWN
            }
}

inline class Licensee(val code: Int) {
    fun name() =
            when (code) {
                1 -> "Nintendo"
                3 -> "Imagineer-Zoom"
                5 -> "Zamuse"
                6 -> "Falcom"
                8 -> "Capcom"
                9 -> "HOT-B"
                10 -> "Jaleco"
                11 -> "Coconuts"
                12 -> "Rage Software"
                14 -> "Technos"
                15 -> "Mebio Software"
                18 -> "Gremlin Graphics"
                19 -> "Electronic Arts"
                21 -> "COBRA Team"
                22 -> "Human/Field"
                23 -> "KOEI"
                24 -> "Hudson Soft"
                26 -> "Yanoman"
                28 -> "Tecmo"
                30 -> "Open System"
                31 -> "Virgin Games"
                32 -> "KSS"
                33 -> "Sunsoft"
                34 -> "POW"
                35 -> "Micro World"
                38 -> "Enix"
                39 -> "Loriciel/Electro Brain"
                40 -> "Kemco"
                41 -> "Seta Co.,Ltd."
                45 -> "Visit Co.,Ltd."
                49 -> "Carrozzeria"
                50 -> "Dynamic"
                51 -> "Nintendo"
                52 -> "Magifact"
                53 -> "Hect"
                60 -> "Empire Software"
                61 -> "Loriciel"
                64 -> "Seika Corp."
                65 -> "UBI Soft"
                70 -> "System 3"
                71 -> "Spectrum Holobyte"
                73 -> "Irem"
                75 -> "Raya Systems/Sculptured Software"
                76 -> "Renovation Products"
                77 -> "Malibu Games/Black Pearl"
                79 -> "U.S. Gold"
                80 -> "Absolute Entertainment"
                81 -> "Acclaim"
                82 -> "Activision"
                83 -> "American Sammy"
                84 -> "GameTek"
                85 -> "Hi Tech Expressions"
                86 -> "LJN Toys"
                90 -> "Mindscape"
                93 -> "Tradewest"
                95 -> "American Softworks Corp."
                96 -> "Titus"
                97 -> "Virgin Interactive Entertainment"
                98 -> "Maxis"
                103 -> "Ocean"
                105 -> "Electronic Arts"
                107 -> "Laser Beam"
                110 -> "Elite"
                111 -> "Electro Brain"
                112 -> "Infogrames"
                113 -> "Interplay"
                114 -> "LucasArts"
                115 -> "Parker Brothers"
                117 -> "STORM"
                120 -> "THQ Software"
                121 -> "Accolade Inc."
                122 -> "Triffix Entertainment"
                124 -> "Microprose"
                127 -> "Kemco"
                128 -> "Misawa"
                129 -> "Teichio"
                130 -> "Namco Ltd."
                131 -> "Lozc"
                132 -> "Koei"
                134 -> "Tokuma Shoten Intermedia"
                136 -> "DATAM-Polystar"
                139 -> "Bullet-Proof Software"
                140 -> "Vic Tokai"
                142 -> "Character Soft"
                143 -> "I''Max"
                144 -> "Takara"
                145 -> "CHUN Soft"
                146 -> "Video System Co., Ltd."
                147 -> "BEC"
                149 -> "Varie"
                151 -> "Kaneco"
                153 -> "Pack in Video"
                154 -> "Nichibutsu"
                155 -> "TECMO"
                156 -> "Imagineer Co."
                160 -> "Telenet"
                164 -> "Konami"
                165 -> "K.Amusement Leasing Co."
                167 -> "Takara"
                169 -> "Technos Jap."
                170 -> "JVC"
                172 -> "Toei Animation"
                173 -> "Toho"
                175 -> "Namco Ltd."
                177 -> "ASCII Co. Activison"
                178 -> "BanDai America"
                180 -> "Enix"
                182 -> "Halken"
                186 -> "Culture Brain"
                187 -> "Sunsoft"
                188 -> "Toshiba EMI"
                189 -> "Sony Imagesoft"
                191 -> "Sammy"
                192 -> "Taito"
                194 -> "Kemco"
                195 -> "Square"
                196 -> "Tokuma Soft"
                197 -> "Data East"
                198 -> "Tonkin House"
                200 -> "KOEI"
                202 -> "Konami USA"
                203 -> "NTVIC"
                205 -> "Meldac"
                206 -> "Pony Canyon"
                207 -> "Sotsu Agency/Sunrise"
                208 -> "Disco/Taito"
                209 -> "Sofel"
                210 -> "Quest Corp."
                211 -> "Sigma"
                214 -> "Naxat"
                216 -> "Capcom Co., Ltd."
                217 -> "Banpresto"
                218 -> "Tomy"
                219 -> "Acclaim"
                221 -> "NCS"
                222 -> "Human Entertainment"
                223 -> "Altron"
                224 -> "Jaleco"
                226 -> "Yutaka"
                228 -> "T&ESoft"
                229 -> "EPOCH Co.,Ltd."
                231 -> "Athena"
                232 -> "Asmik"
                233 -> "Natsume"
                234 -> "King Records"
                235 -> "Atlus"
                236 -> "Sony Music Entertainment"
                238 -> "IGS"
                241 -> "Motown Software"
                242 -> "Left Field Entertainment"
                243 -> "Beam Software"
                244 -> "Tec Magik"
                249 -> "Cybersoft"
                255 -> "Hudson Soft"
                else -> "Unknown"
            }
}

inline class SizeKB(val size: Byte) {
    fun sizeKb() = if (size == 0.toByte()) 0 else (1 shl (size + 3));
}

inline class SizeMB(val size: Byte) {
    fun sizeMb() = 1 shl (size - 7)
}

// meaning of cType
// 00 - ROM only
// 01 - ROM + RAM
// 02 - ROM + RAM + SRAM
//
// Enhancement chips
// 0* - DSP
// 1* - SuperFX
// 2* - OBC1
// 3* - SA-1
// E* - Other
// F* - Custom Chip
//
// *3 - ROM + Enhancement Chip
// *4 - ROM + Enhancement Chip + RAM
// *5 - ROM + Enhancement Chip + RAM + SRAM
// *6 - ROM + Enhancement Chip + SRAM
inline class RomType(val romType: Byte) {
    fun hasRAM() = when (romType.toInt() and 0xF) {
        1, 2, 4, 5 -> true
        else -> false
    }

    fun hasSRAM() = when (romType.toInt() and 0xF) {
        2, 5, 6 -> true
        else -> false
    }

    fun hasCustomChip() = romType.toInt() and 0xF in 3..6

    fun hasDsp() = when {
        romType.toInt() and 0xF0 == 0 && romType.toInt() and 0xF > 2 -> true
        else -> false
    }
    fun hasSuperFx() = romType.toInt() and 0xF0 == 0x10
    fun hasOBC1() = romType.toInt() and 0xF0 == 0x20
    fun hasSA1() = romType.toInt() and 0xF0 == 0x30
    fun hasOther() = romType.toInt() and 0xF0 == 0xE0
    fun hasCustom() = romType.toInt() and 0xF0 == 0xF0

    override fun toString(): String {
        val s = StringJoiner(" + ", "ROM", "")

        when {
            hasDsp() -> s.add("DSP")
            hasSuperFx() -> s.add("SuperFX")
            hasOBC1() -> s.add("OBC1")
            hasSA1() -> s.add("SA-1")
            hasOther() -> s.add("Other")
            hasCustom() -> s.add("Custom")
        }

        if (hasRAM())
            s.add("RAM")

        if (hasSRAM())
            s.add("SRAM")

        return s.toString()
    }
}

enum class VideoSystem {
    UNKNOWN,
    NTSC,
    PAL
}