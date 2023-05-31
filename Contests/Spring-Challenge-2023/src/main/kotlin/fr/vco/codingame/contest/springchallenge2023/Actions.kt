package fr.vco.codingame.contest.springchallenge2023

typealias Action = String

fun beacon(cellId: Int, strength: Int) = "BEACON $cellId $strength"
fun beacon(cell: Cell, strength: Int) = "BEACON ${cell.id} $strength"
fun line(sourceId: Int, targetId: Int, strength: Int) = "LINE $sourceId $targetId $strength"
fun line(source: Cell, target: Cell, strength: Int) = "LINE ${source.id} ${target.id} $strength"
fun wait() = "WAIT"
fun message(message: String) = "MESSAGE $message"