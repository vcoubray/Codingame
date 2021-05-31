package test


fun main(){
    val tree = 5468L
    println(tree.toString(2))

    repeat(10) {
        println((tree shr it) and 1)
    }

    println(tree.countOneBits())

    println((tree or (1L.shl(1))).toString(2))
}