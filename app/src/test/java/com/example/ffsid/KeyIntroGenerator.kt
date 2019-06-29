package com.example.ffsid

import org.junit.Test
import org.junit.Assert.*

import Verifier
import Prover

import com.auth.CAWrapper
import com.auth.ffsKeyGenerator
import java.io.File

class KeyIntroGenerator
{
    @Test
    fun introductionGenerator()
    {
        val ca = CAWrapper()
        File("/home/rafal/college/Crypto/ffsID/CAPublicKey.key").writeBytes(ca.publicKey.encoded)

        val prover = Prover(5)
        val verifier = Verifier(5)
        val (intro, _) = prover.getIntroduction()
        val signedIntroduction = ca.sign(intro.getHash())

        File("/home/rafal/college/Crypto/ffsID/introduction.sign").writeBytes(signedIntroduction)

        assertEquals(verifier.caPublicKey, ca.publicKey)
        assertTrue(verifier.verifyIntroduction(intro.getHash(), signedIntroduction))
    }

    @Test
    fun generateProverKeys()
    {
        val secParam = 5
        val gen = ffsKeyGenerator(secParam)
        gen.genAndStoreKeys()
    }
}
