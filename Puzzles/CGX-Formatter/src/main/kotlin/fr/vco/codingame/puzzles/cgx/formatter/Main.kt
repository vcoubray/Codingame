package fr.vco.codingame.puzzles.cgx.formatter

import java.util.*

fun main() {
    val input = Scanner(System.`in`)
    val N = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }

    var isString = false
    val trimmedContent = List(N) { input.nextLine() }.joinToString("")
        .map {
            if (it == '\'') isString = !isString
            if (isString || !it.isWhitespace()) it else ""
        }
        .joinToString("")

    var indent = 0
    var shouldNewLine = false
    var firstLine = true
    val formattedContent = trimmedContent.map {

        if (!isString && it == ')') indent--

        val before = when {
            isString ||firstLine -> ""
            it != ';' && shouldNewLine -> newLine(indent)
            it in "()" -> newLine(indent)
            else -> ""
        }
        firstLine = false

        if (!isString && it == '(') indent++
        if (it == '\'') isString = !isString
        shouldNewLine = it in "();"

        "$before$it"
    }.joinToString("")
    println(formattedContent)
}

fun newLine(indent: Int) = "\n" + "".padEnd(indent * 4,' ')
