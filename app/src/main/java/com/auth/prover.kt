import java.security.SecureRandom
import BTConnectionWrapper
import java.nio.ByteBuffer


class Prover(val secParam: Int)
{
    fun getIntroduction() : String
    {
        return introduction
    }

    fun genX() : Int
    {
       val prng = SecureRandom()

        var bytes = ByteArray(secParam)
        prng.nextBytes(bytes)

        return ByteBuffer.wrap(bytes).getInt()
    }

    fun fetchChallenge(btCon: BTConnectionWrapper)
    {
        receivedChallenge = btCon.challenge;
    }

    //fun genY()
    //{

    //}

    //fun fetchAuthStatus()
    //{

    //}

    //fun isAuthSuccesful()
    //{
        //return authStatus;
    //}

    ////store securely secret key
    ////fetch secret key
    //private fun genSecretKey()
    //{

    //}



    private val introduction = "Peggy"
    lateinit var receivedChallenge : List<Int>
    //private var authStatus;
}