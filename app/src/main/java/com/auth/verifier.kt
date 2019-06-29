import BTConnectionWrapper
import com.auth.*
import java.io.File
import java.lang.Exception
import java.security.*
import java.security.spec.X509EncodedKeySpec

class Verifier(val secParam : Int)
{
    init {
        initCAPublicKey()
    }

    private val p : Long = 1009
    private val q : Long = 1019
    val N : Long = p*q

    fun fetchIntroduction(btCon : BTConnectionWrapper)
    {
        receivedIntroduction = btCon.introduction
        proverPublicKey = receivedIntroduction.publicKey
        val introductionSignature = btCon.introductionSignature
        if(!verifyIntroduction(receivedIntroduction.getHash(), introductionSignature))
        {
            throw Exception("Prover's introduction signature rejected.")
        }
    }

    fun fetchX(btCon : BTConnectionWrapper)
    {
        receivedX = btCon.X
    }

    fun fetchY(btCon : BTConnectionWrapper)
    {
        receivedY = btCon.Y
    }

    fun genChallenge() : List<Int>
    {
        val prng = SecureRandom()
        var bytes = ByteArray(secParam)
        prng.nextBytes(bytes)

        challenge = convertToBinary(bytes)
        return challenge
    }

    fun verify() : Boolean
    {
        return fastPowerMod(receivedY, 2, N) == calcY()
    }

    fun verifyIntroduction(introductionHash : ByteArray, signedIntro : ByteArray) : Boolean
    {
        val sigGen = Signature.getInstance("SHA256withRSA")
        sigGen.initVerify(caPublicKey)
        sigGen.update(introductionHash)
        return sigGen.verify(signedIntro)
    }

    private fun calcY() : Long
    {
        return positiveMod(receivedX*
                (proverPublicKey zip challenge).fold(1) {
                 acc : Long, (s,c): Pair<Long, Int> -> positiveMod(acc*fastPower(s,c), N)
                }, N)
    }

    private fun initCAPublicKey()
    {
        try {
            val encodedKey = File("/home/rafal/college/Crypto/ffsID/CAPublicKey.key").readBytes()
            val factory = KeyFactory.getInstance("RSA")
            val encodedKeySpec = X509EncodedKeySpec(encodedKey)
            caPublicKey = factory.generatePublic(encodedKeySpec)
        }catch(e : Exception)
        {
            println("Could not read CA public key from file")
            throw e
        }

    }


    var receivedX : Long = 0
    var receivedY : Long = 0
    lateinit var receivedIntroduction : Introduction
    lateinit var proverPublicKey : List<Long>
    lateinit var challenge : List<Int>
    lateinit var caPublicKey : PublicKey
}
