import java.security.SecureRandom
import BTConnectionWrapper
import com.auth.CAWrapper

class Verifier(val secParam : Int)
{
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

    fun genChallenge() : List<Int>
    {
        val prng = SecureRandom()
        var bytes = ByteArray(secParam)
        prng.nextBytes(bytes)

        return convertToBinary(bytes)
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

    var receivedX : Int = 0
    lateinit var receivedIntroduction : String
    lateinit var proverPublicKey : List<Int>
}