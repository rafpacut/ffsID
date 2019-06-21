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

    fun send(value : String)
    {
        introduction = value;
    }

    fun send(value : List<Int>)
    {
        challenge = value;
    }

    var X : Int = 0
    var Y : Int = 0
    lateinit var challenge : List<Int>
    lateinit var introduction : String
}