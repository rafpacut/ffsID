package com.auth

import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import java.io.*
import java.lang.Exception
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.io.FileOutputStream



class SecureStorage {
    fun storeSecretKey(secretKey : List<Long>)
    {
        val secretKeyByteArray = longsToBytes(secretKey)
        loadKeyStore()
        val secretKeyObj = SecretKeySpec(secretKeyByteArray, "ffs")
        val pkEntry = KeyStore.SecretKeyEntry(secretKeyObj)
        val protectionParam = KeyStore.PasswordProtection(ksPasswd)
        keyStore.setEntry(secretKeyAlias, pkEntry, protectionParam)
        saveKeyStore()
    }

    fun storePublicKey(publicKey : List<Long>)
    {
        try {
            var fHandle = File("${basePath}/proverPublicKey")
            fHandle.writeText("")
            publicKey.map { i -> fHandle.appendText("${i.toString()}\n") }
        }
        catch(e : Exception)
        {
            println("Could not store prover's public key")
            throw e
        }
    }

    fun retrieveKeys() : Pair<List<Long>,List<Long>>
    {
        val pk = retrievePublicKey()
        val sk = retrieveSecretKey()
        return Pair(sk,pk)
    }

    private fun retrievePublicKey() : List<Long>
    {
        try {
            val stringPk : List<String> = File("${basePath}/proverPublicKey").readLines()
            return List<Long>(stringPk.size, {i -> stringPk[i].toLong()})
        }
        catch(e : Exception)
        {
            println("Could not read prover's public key")
            throw e
        }
    }

    fun retrieveSecretKey() : List<Long>
    {
        loadKeyStore()
        val protectionParam = KeyStore.PasswordProtection(ksPasswd)
        val entry = keyStore.getEntry(secretKeyAlias, protectionParam)
        val skEntry : KeyStore.SecretKeyEntry = entry as KeyStore.SecretKeyEntry

        return bytesToLongs(skEntry.secretKey.encoded)
    }

    private fun loadKeyStore()
    {
        if(File(keyStorePath).exists()) {
            try {
                if (!isKeyStoreLoaded) {
                    FileInputStream(keyStorePath).use { fis -> keyStore.load(fis, ksPasswd) }
                    isKeyStoreLoaded = true
                }
            } catch (e: IOException) {
                println("IO Error loading keyStore")
                throw e
            }
        }
        else
        {
            println("Creating empty keystore")
            createKeyStore()
        }
    }

    private fun createKeyStore()
    {
        keyStore.load(null)
    }

    private fun saveKeyStore()
    {
        try{
            FileOutputStream(keyStorePath).use { fos -> keyStore.store(fos, ksPasswd) }
        }catch(e: Exception)
        {
            println("Error saving key store")
            throw e
        }
    }

    private val ksPasswd = "password".toCharArray()
    private val keyStoreType = "jceks"
    private var keyStore = KeyStore.getInstance(keyStoreType)
    var isKeyStoreLoaded = false
    val secretKeyAlias = "alias"
    private val basePath = "/home/rafal/college/Crypto/ffsID"
    val keyStorePath = "${basePath}/keyStore.${keyStoreType}"
}