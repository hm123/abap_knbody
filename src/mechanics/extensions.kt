package mechanics


import com.google.gson.GsonBuilder
import java.io.File
import java.io.PrintWriter

/*
fun <T> T.cloneJSON():T{
    var str = gson.toJson(this)

    var t : T = this// null//V3(0.0,0.0,0.0)
    var type  = t::class // (t!!)::class.java // Double::class.java
    var ret = gson.fromJson(str, type)
    return ret// ***
}*/

var gson = GsonBuilder().serializeSpecialFloatingPointValues().create()

fun <T> T.saveJson(fn:String){
    val str = gson.toJson(this)
    val writer = PrintWriter(fn)
    writer.write(str)
    writer.close()
}

fun <T> String.loadJson(clazz:Class<T>) : T{
    val str = File(this).readText()
    return gson.fromJson<T>(str,clazz)
}


infix fun DoubleArray.concat(b: DoubleArray): DoubleArray {
    val ret = DoubleArray(this.size + b.size)
    for(i in 0 until this.size) ret[i] = this[i]
    for(i in 0 until b.size) ret[i+this.size] = b[i]
    return ret
}
