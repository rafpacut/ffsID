package com.auth

import java.nio.ByteBuffer
import java.security.SecureRandom
import com.auth.SecureStorage
import kotlin.math.abs

class ffsKeyGenerator(val secParam : Int)
{
    private val p : Long = 1009
    private val q : Long = 1019
    private val N = p*q

    fun generateKeyPair() : Pair<List<Long>,List<Long>>
    {
        val sk = genSecretKey()
        val pk = genPublicKey(sk)
        return Pair(sk,pk)
    }

    private fun genSecretKey() : List<Long>
    {
        return List(secParam) { positiveMod(genRandomBytes(), N) }
    }

    private fun genPublicKey(secretKey : List<Long>) : List<Long>
    {
       return List(secParam) { i -> positiveMod(secretKey[i] * secretKey[i],N) }
    }

    private fun genRandomBytes() : Long
    {
        var bytes = ByteArray(secParam+3)
        prng.nextBytes(bytes)

        return ByteBuffer.wrap(bytes).getLong()
    }

    fun genAndStoreKeys()
    {
        val secureStorage = SecureStorage()

        val (sk,pk) = generateKeyPair()
        secureStorage.storePublicKey(pk)
        secureStorage.storeSecretKey(sk)
    }


    private val prng = SecureRandom()


}