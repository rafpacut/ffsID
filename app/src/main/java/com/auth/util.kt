package com.auth

import kotlin.math.abs
import java.nio.ByteBuffer
import java.nio.LongBuffer


fun fastPower(b : Long, e : Int) : Long
{
    return fastPowerMod(b, e, Long.MAX_VALUE)
}

fun positiveMod(v : Long, m : Long) : Long
{
    return abs(v % m)
}

fun fastPowerMod(b : Long, e: Int, m : Long) : Long
{
    var base = positiveMod(b,m)
    var res : Long = 1
    var exp = e
    while(exp > 0)
    {
        if(exp % 2 == 1)
        {
            res = positiveMod(res * base, m)
        }
        exp /= 2
        base = positiveMod(base * base, m)
    }
    return res
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

fun longsToBytes(x : List<Long>) : ByteArray
{
    var buffer = ByteBuffer.allocate(x.size*java.lang.Long.BYTES)
    for(l in x)
    {
        buffer = buffer.putLong(l)
    }
    return buffer.array()
}

fun bytesToLongs(x : ByteArray) : List<Long>
{
   var buffer = ByteBuffer.wrap(x)
   return List<Long>(x.size/8, {i -> buffer.getLong()})
}


