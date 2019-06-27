import java.security.SecureRandom
import android.security.KeyChain
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import BTConnectionWrapper
import android.content.Context
import com.auth.CAWrapper
import com.auth.fastPowerMod
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.SecretKey
import android.R.attr.password
import java.io.FileInputStream
import android.R.attr.password
import com.auth.SecureStorage
import com.auth.positiveMod
import java.security.MessageDigest
import javax.security.cert.Certificate

data class Introduction(val name : String, val publicKey : List<Int>)
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

    private val p = 1009
    private val q = 1019
    val N = p*q
    private var r = 0

    fun getIntroduction() : Pair<Introduction,ByteArray>
    {
       return Pair(Introduction("Peggy", publicKey), signedIntroduction)
    }

    fun genX() : Int
    {
        //r = genRandomBytes()
        r = 1
        return fastPowerMod(r, 2, N)
    }

    fun fetchChallenge(btCon: BTConnectionWrapper)
    {
        receivedChallenge = btCon.challenge;
    }

    fun calcY() : Int
    {
        return positiveMod(return r*(secretKey zip receivedChallenge).fold(1) { acc : Int, (s,c): Pair<Int, Int> -> positiveMod(acc*fastPowerMod(s,c,N),N)}, N)
    }

    private fun loadKeys()
    {
        if(! ::secretKey.isInitialized)
        {
            val secureStorage = SecureStorage()
            val (sk, pk) = secureStorage.retrieveKeys("prover")
            secretKey = sk
            publicKey = pk
        }
    }

    private fun genRandomBytes() : Int
    {
        var bytes = ByteArray(secParam)
        prng.nextBytes(bytes)

        return ByteBuffer.wrap(bytes).getInt()
    }

    private val prng = SecureRandom()


    lateinit var receivedChallenge : List<Int>
    lateinit var publicKey : List<Int>
    lateinit var secretKey : List<Int>
    private val basePath = "/home/rafal/college/Crypto/ffsID"
    var signedIntroduction = File(basePath+"/introduction.sign").readBytes()
}