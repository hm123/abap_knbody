package mechanics


class V3(var x:Double=0.0, var y:Double=0.0, var z:Double=0.0){
    operator fun minus(a : V3) = V3(x - a.x, y - a.y,z - a.z)
    operator fun plus(a : V3) = V3(x + a.x, y + a.y,z + a.z)
    operator fun times(dt: Double) = V3(x*dt,y*dt,z*dt)
    operator fun rem(a:V3) = Math.sqrt((x-a.x)*(x-a.x) + (y-a.y)*(y-a.y) + (z-a.z)*(z-a.z))
    operator fun times(a:V3) = V3(y * a.z - z * a.y, z * a.x - x * a.z, x * a.y - y * a.x)

    fun length2() = x*x + y*y + z*z
    fun length() = Math.sqrt(x*x+y*y+z*z)
    infix fun dot(d: V3) = x*d.x + y * d.y + z*d.z
    infix fun dist(d: V3) = Math.sqrt((x -d.x)*(x -d.x)+(y -d.y)*(y -d.y)+(z -d.z)*(z -d.z))
    infix fun dist2(d: V3) = ((x -d.x)*(x -d.x)+(y -d.y)*(y -d.y)+(z -d.z)*(z -d.z))

    override fun toString() = "($x,$y,$z)"
    fun copy() = V3(x,y,z)
    fun addAndScaleIP(d: V3, dt: Double) {
        x+=d.x * dt
        y+=d.y * dt
        z+=d.z * dt
    }
    fun addIP(d: V3) {
        x+=d.x
        y+=d.y
        z+=d.z
    }
    fun subIP(d: V3) {
        x-=d.x
        y-=d.y
        z-=d.z
    }
    fun scale(dt: Double) = V3(x*dt,y*dt,z*dt)
    fun isEqual(v3: V3) = x==v3.x&&y==v3.y&&z==v3.z
    fun normalise() = scale(1.0 / this.length())
    fun isFinite() = x.isFinite() && y.isFinite() && z.isFinite()
    fun toPov() = "<$x,$y,$z>"
    fun toDoubleArray() = doubleArrayOf(x,y,z)
    fun scaleIP(d: Double) {
        x*=d
        y*=d
        z*=d
    }

}

fun Array<V3>.copy() = Array(size){i->this[i].copy()}

fun main(args:Array<String>){
    val a = V3(1.2,2.5,-0.2)
    val b = V3(1.6,-2.5,0.2)

    println("a=$a")
    println("b=$b")
    val c = a + b * 2.0    // scaling
    val d = a dot c        // infix operators
    val e = a * b + V3(d,0.0,0.0)          // cross product

    println("a + b = $c")
    println("a + b = ${a+b}")
    println("e = $e")
}

private fun getSpherePoints(n: Int, theta0:Double): List<V3> {
    var ret = mutableListOf<V3>()
    var theta = theta0
    var increment = Math.PI * (3.0 - Math.sqrt(5.0));
    for(i in 0 until n){
        //var z = ((i+0.5)  / n - 0.5) * 2
        //var r = Math.sqrt(1-z*z)
        theta += increment
    }
    return ret
}

