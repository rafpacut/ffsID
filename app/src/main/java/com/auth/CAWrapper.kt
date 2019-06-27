package com.auth

import java.security.*

class CAWrapper {
    init
    {
        genKeys()
    }

    fun sign(messageHash: ByteArray) : ByteArray
    {
        val sigGen = Signature.getInstance("SHA256withRSA")
        sigGen.initSign(privateKey)
        sigGen.update(messageHash)
        return sigGen.sign()
    }

    private fun genKeys()
    {
        val kp: KeyPair
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")

        kpg.initialize(2048)
        kp = kpg.genKeyPair()

        publicKey = kp.public
        privateKey = kp.private
    }

    lateinit var publicKey : PublicKey
    lateinit var privateKey : PrivateKey

}
