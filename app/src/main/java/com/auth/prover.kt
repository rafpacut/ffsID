import java.security.SecureRandom
import android.security.KeyChain
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import BTConnectionWrapper
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.SecretKey
import android.R.attr.password
import java.io.FileInputStream
import android.R.attr.password
import com.auth.*
import java.security.MessageDigest
import javax.security.cert.Certificate

data class Introduction(val name : String, val publicKey : List<Long>)
{
    fun getHash() : ByteArray
    {
        val hasher = MessageDigest.getInstance("SHA-256")
        return hasher.digest(this.toString().toByteArray())
    }
}

class Prover(val secParam : Int)
{
    init {
        loadKeys()
    }

    private val p : Long= 1009
    private val q : Long= 1019
    val N : Long = p*q
    private var r : Long = 0

    fun getIntroduction() : Pair<Introduction,ByteArray>
    {
       return Pair(Introduction("Peggy", publicKey), signedIntroduction)
    }

    fun genX() : Long
    {
        r = positiveMod(genRandomBytes(), N)
        return fastPowerMod(r, 2, N)
    }

    fun fetchChallenge(btCon: BTConnectionWrapper)
    {
        receivedChallenge = btCon.challenge;
    }

    fun calcY() : Long
    {
        return positiveMod(r*
            (secretKey zip receivedChallenge).fold(1) {
                acc : Long, (s,c): Pair<Long, Int> -> positiveMod(acc*fastPower(s,c), N)
        }, N)
    }

    private fun loadKeys()
    {
        if(! ::secretKey.isInitialized)
        {
            val secureStorage = SecureStorage()
            val (sk, pk) = secureStorage.retrieveKeys()
            secretKey = sk
            publicKey = pk
        }
    }

    private fun genRandomBytes() : Long
    {
        var bytes = ByteArray(secParam+5)
        prng.nextBytes(bytes)

        return ByteBuffer.wrap(bytes).getLong()
    }

    private val prng = SecureRandom()


    lateinit var receivedChallenge : List<Int>
    lateinit var publicKey : List<Long>
    lateinit var secretKey : List<Long>
    private val basePath = "/home/rafal/college/Crypto/ffsID"
    var signedIntroduction = File(basePath+"/introduction.sign").readBytes()
}