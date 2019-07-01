package com.auth

import android.content.Context
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Log
import java.io.*
import java.lang.Exception
import Introduction
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.io.FileOutputStream

class SecureStorage(val context : Context) {
    fun storeSecretKey(secretKey : List<Long>)
    {
        try {
            val fOutStream = context.openFileOutput("proverSecretKey", Context.MODE_PRIVATE)
            val byteSecretKey = longsToBytes(secretKey)
            fOutStream.write(byteSecretKey)
            fOutStream.close()
        }catch(e : Exception)
        {
            println("Could not store prover's secret key")
            throw e
        }
    }

    fun storePublicKey(publicKey : List<Long>)
    {
        try {
            val fOutStream = context.openFileOutput("proverPublicKey", Context.MODE_PRIVATE)
            val bytePublicKey = longsToBytes(publicKey)
            fOutStream.write(bytePublicKey)
            fOutStream.close()
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
            val fHandle = context.getFileStreamPath("proverPublicKey")
            val bytesPk = fHandle.readBytes()
            val pk = bytesToLongs(bytesPk)
            return pk
        }
        catch(e : Exception)
        {
            println("Could not read prover's public key")
            throw e
        }
    }

    fun retrieveIntroSign() : ByteArray
    {
        try{
            val fHandle = context.getFileStreamPath("introduction.sign")
            return fHandle.readBytes()
        }
        catch(e : Exception)
        {
            println("Could not read prover's introduction signature")
            throw e
        }
    }

    fun retrieveSecretKey() : List<Long>
    {
        try {
            val fHandle = context.getFileStreamPath("proverSecretKey")
            val bytesPk = fHandle.readBytes()
            val pk = bytesToLongs(bytesPk)
            return pk
        }
        catch(e : Exception)
        {
            println("Could not read prover's secret key")
            throw e
        }
    }
}