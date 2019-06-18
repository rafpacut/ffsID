package com.example.ffsid

import org.junit.Test

import org.junit.Assert.*

import BTConnectionWrapper
import Prover
import Verifier
import com.auth.CAWrapper

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//@RunWith(AndroidJUnit4::class)
class AuthTest {

    lateinit var prover : Prover
    lateinit var verifier : Verifier
    lateinit var btCon : BTConnectionWrapper
    lateinit var caHandle : CAWrapper

 //   @BeforeTest
    fun setUp()
    {
        val secParam = 5
        prover = Prover(secParam)
        verifier = Verifier(secParam)
        btCon = BTConnectionWrapper()
        caHandle = CAWrapper()

    }

    @Test
    fun introductionTxTest()
    {
        setUp()

        val intro = prover.getIntroduction();
        btCon.send(intro);

        verifier.fetchIntroduction(btCon);
        assertEquals("Introduction transfer did not succeed.", intro, verifier.receivedIntroduction);
    }

    @Test
    fun xTxTest()
    {
        setUp()

        val x = prover.genX();
        btCon.send(x);

        verifier.fetchX(btCon);
        assertEquals("X transfer did not succeed.", x, verifier.receivedX);
    }

    @Test
    fun challengeTxTest()
    {
        setUp()

        val challenge = verifier.genChallenge();
        btCon.send(challenge);

        prover.fetchChallenge(btCon);
        assertEquals("Challenge transfer did not succeed.", challenge, prover.receivedChallenge);
    }

    @Test
    fun publicKeyTx()
    {
        setUp()

        val (id, pk) = prover.getRegistrationInfo()
        caHandle.register(id, pk)

        verifier.receivedIntroduction = prover.getIntroduction()

        verifier.fetchCert(caHandle)
        assertEquals("Public key tx failed.", prover.publicKey, verifier.proverPublicKey)
    }

    @Test
    fun authTest()
    {
       setUp()

        verifier.receivedX = prover.genX()
        prover.receivedChallenge = verifier.genChallenge()
        verifier.proverPublicKey = prover.retrievePublicKey()
        verifier.receivedY = prover.calcY()
        assertTrue(verifier.verify())
    }

    @Test
    fun successfulAuthTest() {
        ////if N can be const between protocols, then i can re-use v-s and keep them under prover's name in an external server.
        ////I then verify prover's identity. if they cant, I have to come up with different N and only verify that prover knows those numbers.
        ////I cant tie them to identity.

        setUp()

        val intro = prover.getIntroduction();
        btCon.send(intro);
        verifier.fetchIntroduction(btCon);

        val challenge = verifier.genChallenge();
        btCon.send(challenge);
        prover.fetchChallenge(btCon);

        val (id, pk) = prover.getRegistrationInfo()
        caHandle.register(id, pk)
        verifier.fetchCert(caHandle)

        val x = prover.genX();
        btCon.send(x);
        verifier.fetchX(btCon)

        val py = prover.calcY()
        btCon.sendY(py)
        verifier.fetchY(btCon)
        assertTrue(verifier.verify())
    }
}
