package fr.vco.codingame.puzzles.nintendo

fun main () {

    println(encode("00000083 000000e5", 32))

}

fun encode(input: String, size : Int) : String {
    val a = input.split(" ").map{it.toLong(16)}
    val b = LongArray(a.size){0}
    for (i in 0 until size) {
        for (j in 0 until size) {
            b[(i+j)/32] = b[(i+j)/32].xor(
                ((a[i/32] shr (i%32)) and (a[j/32 + size/32] shr (j%32)) and 1 ) shl ((i+j)%32)
            )
        }
    }

    return b.joinToString(" ") {it.toString(16).padStart(8,'0')}
}

fun decode(size: Int, input: String): List<String> {

    return emptyList()
}