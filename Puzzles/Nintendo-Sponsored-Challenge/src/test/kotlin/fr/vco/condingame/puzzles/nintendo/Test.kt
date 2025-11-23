package fr.vco.condingame.puzzles.nintendo

import fr.vco.codingame.puzzles.nintendo.decode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


data class Game(val size: Int, val input: String, val expected: List<String>)


class Test : FunSpec({

    val tests = listOf(
        Game(
            32, "000073af 00000000", listOf(
                "00000001 000073af",
                "00000083 000000e5",
                "000000e5 00000083",
                "000073af 00000001",
            )
        ),
        Game(
            32, "738377c1 00000000", listOf(
                "00000001 738377c1",
                "0000b0c5 0000cd55",
                "0000cd55 0000b0c5",
                "738377c1 00000001",
            )
        ),
        Game(
            32, "46508fb7 6677e201", listOf(
                "b0c152f9 ebf2831f",
                "ebf2831f b0c152f9",
            )
        ),
        Game(
            64, "f3268b49 661859eb 0b324559 65ee6bda", listOf(
                "0cf5c2bf 9aba68ef c18fb79b de70eef7",
                "c18fb79b de70eef7 0cf5c2bf 9aba68ef",
            )
        ),
        Game(
            256, "4af6fc33 39029380 465c5267 c72f6a8b 0906e6d0 ca60550f 14a5e47c 42ad10fb 4a3bb446 bb74360a 5ea02b9c 23c68553 3fade253 e270ba24 39e141ad 6c38c43d", listOf(
                "320a18d5 b61b13f6 1aaaa61c 0afe2a41 1a4ff107 84cc2efc 956ff31d fa595299 33749a7f 6cc9659d dc503569 ef4d0ef5 73b746c5 b8fb36d3 7616e9d6 b21251c4",
                "33749a7f 6cc9659d dc503569 ef4d0ef5 73b746c5 b8fb36d3 7616e9d6 b21251c4 320a18d5 b61b13f6 1aaaa61c 0afe2a41 1a4ff107 84cc2efc 956ff31d fa595299",
            )
        ),
        Game(
            128, "a91db473 fcea8db4 f3bb434a 8dba2f16 51abc87e 92c44759 5c1a16d3 6111c6f4", listOf(
                "a30d28bd bda19675 3f95d074 b6f69434 c58f4047 d73fe36a 24be2846 e2ebe432",
                "c58f4047 d73fe36a 24be2846 e2ebe432 a30d28bd bda19675 3f95d074 b6f69434",
            )
        )
    )

    tests.forEachIndexed{i,(size, input, expected) ->

        test("Game ${i+1}") {
            decode(size, input) shouldBe expected
        }

    }


})