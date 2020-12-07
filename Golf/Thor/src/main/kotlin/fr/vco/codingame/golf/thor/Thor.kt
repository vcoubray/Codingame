package fr.vco.codingame.golf.thor

fun main(){var(a,b,c,d)=readLine()!!.split(" ").map{it.toInt()}
while(1>0)println(when{d++<b->"S";--d>b->{b++;"N"};else->""}+when{c++<a->"E";--c>a->{a++;"W"};else->""})}
