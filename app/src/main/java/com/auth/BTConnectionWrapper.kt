class BTConnectionWrapper
{
    fun send(value : Long)
    {
        X = value;
    }

    fun sendY(value : Long)
    {
        Y = value
    }

    fun send(intro : Introduction, signature : ByteArray)
    {
        introduction = intro
        introductionSignature = signature
    }

    fun send(value : List<Int>)
    {
        challenge = value;
    }

    var X : Long = 0
    var Y : Long = 0
    lateinit var challenge : List<Int>
    lateinit var introduction : Introduction
    lateinit var introductionSignature : ByteArray
}