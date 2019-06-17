import java.security.SecureRandom
import BTConnectionWrapper
import com.auth.CAWrapper
import com.auth.fastPowerMod
import java.nio.ByteBuffer


class Prover(val secParam: Int)
{
    val p = 1009
    val q = 1019
    val N = p*q
    var r = 0

    fun getIntroduction() : String
    {
        return introduction
    }

    fun genX() : Int
    {
        r = genRandomBytes()
        return fastPowerMod(r, 2, N)
    }

    fun fetchChallenge(btCon: BTConnectionWrapper)
    {
        receivedChallenge = btCon.challenge;
    }

    fun calcY() : Int
    {
        return r*(secretKey zip receivedChallenge).fold(1) { acc : Int, (s,c): Pair<Int, Int> -> acc*fastPowerMod(s,c,N)}
    }

    //fun fetchAuthStatus()
    //{

    //}

    //fun isAuthSuccesful()
    //{
        //return authStatus;
    //}
    fun retrievePublicKey() : List<Int>
    {
        if(! ::publicKey.isInitialized) {
            if(! ::secretKey.isInitialized)
            {
                genSecretKey()
            }
            genPublicKey()
        }
        return publicKey
    }

    fun getRegistrationInfo() : Pair<String, List<Int>>
    {
        return Pair(introduction, retrievePublicKey())
    }

    ////store securely secret key
    private fun genSecretKey()
    {
        secretKey = List(secParam) { genRandomBytes() }
    }

    private fun genPublicKey()
    {
        publicKey = List(secParam) { i -> secretKey[i] * secretKey[i] % N}
    }

    private fun genRandomBytes() : Int
    {
        var bytes = ByteArray(secParam)
        prng.nextBytes(bytes)

        return ByteBuffer.wrap(bytes).getInt()
    }


    lateinit var receivedChallenge : List<Int>
    lateinit var publicKey : List<Int>

    private lateinit var secretKey : List<Int>
    private val prng = SecureRandom()
    private val introduction = "Peggy"
    //private var authStatus;
}