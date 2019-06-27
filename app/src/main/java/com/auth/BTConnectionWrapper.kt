class BTConnectionWrapper
{
    fun send(value : Int)
    {
        X = value;
    }

    fun sendY(value : Int)
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

    var X : Int = 0
    var Y : Int = 0
    lateinit var challenge : List<Int>
    lateinit var introduction : Introduction
    lateinit var introductionSignature : ByteArray
}