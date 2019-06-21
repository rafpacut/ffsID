import java.security.SecureRandom
import BTConnectionWrapper
import com.auth.CAWrapper
import com.auth.fastPowerMod

class Verifier(val secParam : Int)
{
    val p = 1009
    val q = 1019
    val N = p*q

    fun fetchIntroduction(btCon : BTConnectionWrapper)
    {
        receivedIntroduction = btCon.introduction
    }

    fun fetchCert(caHandle : CAWrapper)
    {
        //based on introduction get name, photo and public key of prover
        proverPublicKey = caHandle.get(receivedIntroduction).publicKey
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

    fun convertToBinary(bytes : ByteArray) : List<Int>
    {
        var binary = mutableListOf<Int>()
        val ints =  bytes.map { b : Byte -> b.toInt() }
        val positions : List<Int> = listOf(64, 32, 16, 8, 4, 2, 1)
        for(i : Int in ints)
        {
            var noLeadingOne : Boolean = true
            var j = i
            for( pos in positions) {
                if(j < pos && noLeadingOne){ continue }

                if (j / pos >= 1) {
                    binary.add(1)
                    noLeadingOne = false
                    j -= pos
                }
                else
                {
                    binary.add(0)
                }
            }
        }
        return binary
    }

    private fun calcY() : Int
    {
        return receivedX*(proverPublicKey zip challenge).fold(1) { acc : Int, (s,c): Pair<Int, Int> -> acc*fastPowerMod(s,c,N)}
    }


    var receivedX : Int = 0
    var receivedY : Int = 0
    lateinit var receivedIntroduction : String
    lateinit var proverPublicKey : List<Int>
    lateinit var challenge : List<Int>
}