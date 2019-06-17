package com.auth

fun fastPowerMod(b : Int, e: Int, m : Int) : Int
{
    var base = b % m
    var res = 0
    var exp = e
    while(exp > 0)
    {
        if(exp % 2 == 1)
        {
            res = (res * base) % m
        }
        exp /= 2
        base = (base * base) % m
    }
    return res
}