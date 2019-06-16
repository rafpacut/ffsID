package com.example.ffsid

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AuthTest {
    @Test
    fun authTest() {
        val prover = new Prover();
        val verifier = new Verifier();

        //if N can be const between protocols, then i can re-use v-s and keep them under prover's name in an external server.
        //I then verify prover's identity. if they cant, I have to come up with different N and only verify that prover knows those numbers.
        //I cant tie them to identity.

        prover.sendIntroduction();
        verifier.getIntroduction();

        verifier.fetchCert();
        verifier.sendChallenge();
        verifier.calcY();

        prover.getChallenge();
        prover.calcY();
        prover.sendY();

        verifier.getY();
        verifier.verify();
        assertTrue(verifier.authSuccessfull(), "Verifier did not auth the prover.");

        verifier.sendAuthStatus();
        prover.getAuthStatus();
        assertTrue(prover.authSuccessfull(), "Prover did not authenticate.")
    }
}
