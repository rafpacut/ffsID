import BTConnectionWrapper
import com.auth.CAWrapper
import com.auth.convertToBinary
import com.auth.fastPowerMod
import com.auth.positiveMod
import java.io.File
import java.lang.Exception
import java.security.*
import java.security.spec.X509EncodedKeySpec

class Verifier(val secParam : Int)
{
    init {
        initCAPublicKey()
    }

    private val p = 1009
    private val q = 1019
    private val N = p*q

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
        //val prng = SecureRandom()
        //var bytes = ByteArray(secParam)
        //prng.nextBytes(bytes)

        //challenge = convertToBinary(bytes)
        challenge = listOf(1, 1, 1, 1, 1)
        return challenge
    }

    fun verify() : Boolean
    {
        val tmp = calcY()
        val tmp2 = fastPowerMod(receivedY, 2, N)
        return fastPowerMod(receivedY, 2, N) == calcY()
    }

    fun verifyIntroduction(introductionHash : ByteArray, signedIntro : ByteArray) : Boolean
    {
        val sigGen = Signature.getInstance("SHA256withRSA")
        sigGen.initVerify(caPublicKey)
        sigGen.update(introductionHash)
        return sigGen.verify(signedIntro)
    }

    private fun calcY() : Int
    {
        var res = receivedX
        for((pkEl, c) in (proverPublicKey zip challenge))
        {
           res = positiveMod(res*fastPowerMod(pkEl, c, N), N)
        }

        val fRes = positiveMod(receivedX*(proverPublicKey zip challenge).fold(1) { acc : Int, (s,c): Pair<Int, Int> -> positiveMod(acc*fastPowerMod(s,c,N),N)}, N)
        return res
    }

    fun initCAPublicKey()
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


    var receivedX : Int = 0
    var receivedY : Int = 0
    lateinit var receivedIntroduction : Introduction
    lateinit var proverPublicKey : List<Int>
    lateinit var challenge : List<Int>
    lateinit var caPublicKey : PublicKey
}