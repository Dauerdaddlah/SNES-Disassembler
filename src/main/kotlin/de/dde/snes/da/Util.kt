package de.dde.snes.da

fun Int.asByte() = this and 0xFF
fun Int.asShort() = this and 0xFFFF
fun Int.asLong() = this and 0xFFFFFF

fun Int.lowByte() = this.asByte()
fun Int.highByte() = (this shr 8).asByte()
fun Int.longByte() = (this shr 16).asByte()

fun Byte(b: Byte): Int = Byte(b.toInt())
fun Byte(b: Int): Int = b and 0xFF
fun Word(lowByte: Byte, hiByte: Byte): Int = (Byte(lowByte) or (Byte(hiByte) shl 8))

fun Int.toAscii(def: Char = '.') = if (Character.isISOControl(this)) def else this.toChar()
fun Byte.toAscii(def: Char = '.') = Byte(this).toAscii(def)

val _1K = 0x400
val _1BANK = 0x10000
val HALF_BANK = 0x08000
val _1PAGE = 0x100