package com.example.ffsid

import org.junit.Test
import org.junit.Assert.*

import Verifier
import Prover
import android.content.Context

import com.auth.CAWrapper
import com.auth.ffsKeyGenerator
import com.auth.longsToBytes
import java.io.File

class KeyIntroGeneratorUT
{
    //@Test
    //fun introductionGenerator()
    //{
    //    val ca = CAWrapper()

    //    val caFOutStream = context.openFileOutput("CAPublicKey", Context.MODE_PRIVATE)
    //    caFOutStream.write(ca.publicKey.encoded)
    //    caFOutStream.close()

    //    val prover = Prover(5, context)
    //    val verifier = Verifier(5, context)
    //    val (intro, _) = prover.getIntroduction()
    //    val signedIntroduction = ca.sign(intro.getHash())

    //    assertEquals(verifier.caPublicKey, ca.publicKey)
    //    assertTrue(verifier.verifyIntroduction(intro.getHash(), signedIntroduction))
    //}

    fun generateProverKeys()
    {
        //ffsKeyGenerator(5).genAndStoreKeys()
    }
}
