package fr.vco.codingame.contests.snakebyte

import kotlin.test.Test


class MainTest {


    @Test
    fun testBoard(){
        val width = 10
        val height = 10

        val grid = List(height) { "".padStart(width, ' ')}

        val board = Board(width, height, grid)



    }
}