package com.example.ffsid

import org.junit.Test

import org.junit.Assert.*

import Verifier
import com.auth.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class  UtilsUT{
    @Test
    fun convertToBin_isCorrect() {
        val bytes = byteArrayOf(1, 2, 3, 5)
        val bin = convertToBinary(bytes)

        val correct = listOf(1, 1, 0, 1, 1, 1, 0, 1)
        assertEquals(correct, bin)
    }

    @Test
    fun FastPowerMode_isCorrect()
    {
        assertEquals(16, fastPower(2,4))
        assertEquals(2, fastPowerMod(2,4, 14))
        assertNotEquals(-2, fastPowerMod(-2, 3, 6))
    }

    @Test
    fun longToBytesAndBackTest()
    {
        val gt : List<Long> = listOf(1,2, 3, 99999999999)
        val ba = longsToBytes(gt)
        val longsBack = bytesToLongs(ba)

        assertEquals(gt, longsBack)
    }
}
