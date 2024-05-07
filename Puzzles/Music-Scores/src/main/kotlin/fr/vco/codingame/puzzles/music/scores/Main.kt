package fr.vco.codingame.puzzles.music.scores

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

typealias Image = List<List<Boolean>>

fun main() {
    val (width, _) = readln().split(" ").map { it.toInt() }
    val image = readln()

    println(decode(width, image).getTones().joinToString(" "))
}

fun decode(width: Int, image: String): Image {
    return image.split(" ").chunked(2)
        .flatMap { (letter, count) -> List(count.toInt()) { letter == "B" } }
        .chunked(width)
}

fun Image.getTones(): List<String> {
    val image = this.transpose()

    val margin = image.getLeftMargin()
    val linesPattern = image[margin]
    val toneMap = linesPattern.toToneMap()
    val linesIndices = linesPattern.getLinesIndices()

    val tones = mutableListOf<String>()
    var start = margin
    var isTone = false
    for (i in margin until image.size) {
        if (!isTone && image[i].hasNote(linesIndices)) {
            start = i
            isTone = true
        } else if (isTone && !image[i].hasNote(linesIndices)) {
            val left = image.getNoteRange(start, linesIndices)
            val right = image.getNoteRange(i - 1, linesIndices)
            val centerX = getCenter(start, i - 1)
            val centerY = getCenter(max(right.first, left.first), min(right.last, left.last))
            tones.add("${toneMap[centerY]}${if (image[centerX][centerY]) "Q" else "H"}")
            isTone = false
        }
    }
    return tones
}

fun Image.transpose() = List(this.first().size) { i -> this.map { line -> line[i] } }
fun Image.getLeftMargin() = this.indexOfFirst { column -> column.any { it } }

fun List<Boolean>.getLinesIndices(): List<Int> {
    val firstLine = this.takeWhile { !it }.size
    val lineSize = this.drop(firstLine).takeWhile { it }.size
    val interline = this.drop(firstLine + lineSize).takeWhile { !it }.size

    return List(6) { iLine ->
        List(lineSize) { firstLine + ((interline + lineSize) * iLine) + it }
    }.flatten()
}

fun List<Boolean>.toToneMap(): Map<Int, Char> {
    val firstLine = this.takeWhile { !it }.size
    val lineSize = this.drop(firstLine).takeWhile { it }.size
    val interline = this.drop(firstLine + lineSize).takeWhile { !it }.size

    val diff = (lineSize + interline) / 2.0
    val firstTone = firstLine - interline / 2.0

    return listOf('G', 'F', 'E', 'D', 'C', 'B', 'A', 'G', 'F', 'E', 'D', 'C')
        .mapIndexed { i, tone -> (firstTone + i * diff).roundToInt() to tone }
        .toMap()
}

fun List<Boolean>.hasNote(linesIndices: List<Int>) = this.indices.any{this[it] && it !in linesIndices}


fun Image.getNoteRange(column: Int, linesIndices: List<Int>) : IntRange {
    val start = this[column].indices.first { this[column][it] && it !in linesIndices }
    val end = this[column].indices.last { this[column][it] && it !in linesIndices }
    return start..end
}

fun getCenter(start: Int, end: Int) = (start + (end - start) / 2.0).roundToInt()

