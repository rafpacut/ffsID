package com.auth

import kotlin.math.abs

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

//fun chunkInt(intVal : Int) : List<Int>
//{
//    //what about negative numbers?
//    val offsets = List(4, {j -> fastPower(2,4*j)})
//    val maxByteVal = 127
//    var x : MutableList<Int> = mutableListOf()
//
//    //gosh, damn, how to do it functionally?
//    for(offset in offsets)
//    {
//        val remainder = intVal.rem(maxByteVal * offset)
//        if(remainder >= intVal)
//        {
//            x.add(0)
//        }
//        else {
//            x.add(remainder)
//        }
//    }
//    return x.toList()
//    //return List(4, {j -> intVal.rem(maxByteVal * offsets[j]) }).map {i -> }
//}
//
//fun bigIntListToBytes(ints : List<Int>) : ByteArray
//{
//    val chunks = ints.flatMap { i -> chunkInt(i) }
//    return chunks.map({ i-> i.toByte() }).toByteArray()
//}
//
//fun getIntAt(bytes: ByteArray): Int
//{
//   return bytes.foldIndexed(0) { j, acc, b -> acc + b.toInt()* fastPower(2, j*4) }
//}
//
//fun byteArrayToBigInt(bytes : ByteArray): List<Int>
//{
//    return List(bytes.size/8, { i -> getIntAt(bytes.sliceArray(i..i+8))})
//}
//
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

