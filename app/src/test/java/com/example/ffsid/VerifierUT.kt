package com.example.ffsid

import org.junit.Test

import org.junit.Assert.*

import Verifier

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class  VerifierUT{
    @Test
    fun convertToBin_isCorrect() {
        val verifier = Verifier(5)
        val bytes = byteArrayOf(1, 2, 3, 5)
        val bin = verifier.convertToBinary(bytes)

        val correct = listOf(1, 1, 0, 1, 1, 1, 0, 1)
        assertEquals(correct, bin)
    }
}
