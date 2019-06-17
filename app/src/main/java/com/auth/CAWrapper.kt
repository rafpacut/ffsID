package com.auth

class CAWrapper {
    fun add(id : String, publicKey : List<Int>)
    {
        certMap.put(id, Certificate(publicKey))
    }

    fun get(id: String) : Certificate
    {
        try {
            return certMap.getValue(id)
        }
        catch(e: Exception)
        {
            println("${id} has no certificate.")
            throw e;
        }
    }

    private var certMap : MutableMap<String, Certificate> = mutableMapOf()
}

class Certificate(val publicKey : List<Int> ) {} //yes, it's a type alias for now.
