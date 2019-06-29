package com.example.ffsid

import org.junit.Test

import org.junit.Assert.*

import com.auth.SecureStorage

class  SecureStorageUT{
    @Test
    fun storesKeysCorrectly()
    {
        val ss = SecureStorage()
        val (sk, pk) = ss.retrieveKeys()

        ss.storePublicKey(pk)
        ss.storeSecretKey(sk)

        val (skStored, pkStored) = ss.retrieveKeys()

        assertEquals("secret key storage fails", sk, skStored)
        assertEquals("public key storage fails", pk, pkStored)
    }
}


