package fr.vco.codingame.puzzles.the.resistance

import java.util.*
import kotlin.math.min

val MORSE_MAP = mapOf(
    ".-" to 'A', "-..." to 'B', "-.-." to 'C', "-.." to 'D',
    "." to 'E', "..-." to 'F', "--." to 'G', "...." to 'H',
    ".." to 'I', ".---" to 'J', "-.-" to 'K', ".-.." to 'L',
    "--" to 'M', "-." to 'N', "---" to 'O', ".--." to 'P',
    "--.-" to 'Q', ".-." to 'R', "..." to 'S', "-" to 'T',
    "..-" to 'U', "...-" to 'V', ".--" to 'W', "-..-" to 'X',
    "-.--" to 'Y', "--.." to 'Z'
)

class DictionaryTree(
    var isWord: Boolean = false,
    val children: MutableMap<Char, DictionaryTree> = mutableMapOf(),
) {
    fun add(word: String) {
        val child = children.computeIfAbsent(word.first()) { DictionaryTree() }
        if (word.length > 1) child.add(word.drop(1))
        else child.isWord = true
    }
}

fun main() {
    val input = Scanner(System.`in`)
    val morse = input.next()

    /* Store Dictionary as a Tree */
    val dictionary = DictionaryTree()
    repeat(input.nextInt()) {
        dictionary.add(input.next())
    }

    /* Compute all possible letters for each position in the morse string */
    val possibleLetters = List(morse.length) { mutableListOf<Pair<Char, Int>>() }
    for (i in morse.indices) {
        for (j in i until min(i + 4, morse.length)) {
            val morseChar = morse.substring(i, j + 1)
            MORSE_MAP[morseChar]?.let { char -> possibleLetters[i].add(char to j + 1) }
        }
    }

    /* Count all possible words combination for all possible letters */
    val sentenceCount = MutableList(morse.length + 1) { 0L }
    sentenceCount[0] = 1L
    for (i in possibleLetters.indices) {
        countValidWords(i, i, possibleLetters, dictionary, sentenceCount)
    }

    println(sentenceCount.last())
}

fun countValidWords(
    startLetter: Int,
    startWord: Int,
    possibleLetters: List<List<Pair<Char, Int>>>,
    dictionary: DictionaryTree,
    sentenceCount: MutableList<Long>,
) {
    for ((letter, j) in possibleLetters[startLetter]) {
        dictionary.children[letter]?.let { subDictionary ->
            if (subDictionary.isWord) sentenceCount[j] += sentenceCount[startWord]
            if (j < possibleLetters.size) {
                countValidWords(j, startWord, possibleLetters, subDictionary, sentenceCount)
            }
        }
    }
}