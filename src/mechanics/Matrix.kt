package mechanics

/*

    The things this needs to be able to do:
    * set an element
    * get an element
    * gaussian elimination
    * determinant

 */

class Matrix3(val red:Matrix, val green:Matrix, val blue:Matrix){
    fun drawLine(x:Double,y:Double,x2:Double,y2:Double,r:Double,g:Double,b:Double){
        red.drawLine(x,y,x2,y2,r)
        green.drawLine(x,y,x2,y2,g)
        blue.drawLine(x,y,x2,y2,b)
    }

    fun drawCircle(x:Double,y:Double,rad:Double,r:Double,g:Double,b:Double){
        red.drawCircle(x,y,rad,r)
        green.drawCircle(x,y,rad,g)
        blue.drawCircle(x,y,rad,b)
    }

    fun fillCircle(x:Double,y:Double,rad:Double,r:Double,g:Double,b:Double){
        red.fillCircle(x,y,rad,r)
        green.fillCircle(x,y,rad,g)
        blue.fillCircle(x,y,rad,b)
    }
}

fun newMatrix3(w: Int, h: Int) = Matrix3(newMatrix(w,h), newMatrix(w,h), newMatrix(w,h))


class Matrix(val a : DoubleArray, val s0 : Int, val s1 : Int){
    var initRow = 0
    fun init(vararg a: Double){
        var j = 0
        for(a2 in a){
            this[initRow,j] = a2
            j++
        }
        initRow++

    }

    /*
    fun eig() : Jama.EigenvalueDecomposition{
        val m = Jama.Matrix(s0,s1)
        for(i in 0 until s0) for (j in 0 until s1) m[i,j] = this[i,j]
        return EigenvalueDecomposition(m)
    }
    */

    operator fun get(i0 : Int, i1 : Int) = a[i1 + i0 * s1]

    operator fun set(i0 : Int, i1 : Int, v: Double) {
        var i = i1 + i0 * s1
        a[i] = v
    }

    fun clone() = Matrix(a.copyOf(), s0, s1)


    operator fun plus(v: Double): Matrix {
        val ret = clone()
        for(i in 0 until a.size) ret.a[i]+=v
        return ret
    }
    operator fun minus(v: Double): Matrix {
        val ret = clone()
        for(i in 0 until a.size) ret.a[i]-=v
        return ret
    }

    operator fun times(v: Double): Matrix {
        val ret = clone()
        for(i in 0 until a.size) ret.a[i]*=v
        return ret
    }

    operator fun times(v: DoubleArray): DoubleArray {
        val ret = DoubleArray(this.s0)
        for(i in 0 until s0) {
            var sum = 0.0
            for(j in 0 until s1){
                sum += this[i,j] * v[j]
            }
            ret[i]=sum
        }
        return ret
    }

    operator fun plus(v: Matrix): Matrix {
        val ret = clone()
        for(i in 0 until a.size)ret.a[i]+=v.a[i]
        return ret
    }

    operator fun minus(v: Matrix): Matrix {
        val ret = clone()
        for(i in 0 until a.size) ret.a[i]-=v.a[i]
        return ret
    }

    fun gaussianEliminationIP(){
        for(i in 0 until s0-1){
            for(j in i+1 until s0){
                val factor = this[j,i] / (this[i,i]+1e-20)
                for(k in 0 until s1){
                    this[j,k] -= this[i,k] * factor
                }
            }
        }
    }

    fun multiplicativeTrace() : Double{
        var ret = 1.0
        for(i in 0 until s0)
            ret *= this[i,i]
        return ret
    }

    override fun toString():String{
        var a = "-------------\n"
        for(i in 0 until s0){
            if (i!=0) a+= "\n"
            a+="| "
            if (i>=10) {a+="...";break;}
            for(j in 0 until s1){
                if (j!=0) a += " "
                if (j>=10){
                    a+="..."
                    break
                }
                a += this[i,j].toString()
            }
            a+= " |"
        }
        return a
    }

    fun transpose() = newMatrix(s1,s0) { i, j->this[j,i]}
    fun dot(m: Matrix): Double {
        var ret = 0.0
        for(i in 0 until s0) for (j in 0 until s1) ret+=this[i,j] * m[i,j]
        return ret
    }
/*
    fun inverse(): Matrix {
        var ret = Jama.Matrix(s0,s1)
        for(i in 0 until s0){
            for(j in 0 until s1){
                ret[i,j] = this[i,j]
            }
        }
        ret = ret.inverse()

        return newMatrix(s0,s1) { i, j->ret[i,j]}
    }
*/
    fun drawLine(getx: Double, gety: Double, getx1: Double, gety1: Double, str: Double) {
        val steps = Math.ceil(Math.max(Math.abs(getx1 - getx), Math.abs(gety1-gety))).toInt()
        var x = getx
        var y= gety
        val dx = ( getx1 - getx) / steps
        val dy = ( gety1 - gety) / steps
        var x1 = -10000
        var y1 = -10000
        for(i in 0..steps){
            val x2 = x.toInt()
            val y2 = y.toInt()
            if (x1!=x2 || y1!=y2){
                drawPoint(x2,y2,str)
            }
            x1 = x2
            y1 = y2
            x+=dx
            y+=dy
        }
    }
    fun drawPoint(x2: Int, y2: Int, str: Double) {
        if (x2 >=0 && y2 >= 0 && x2<s0 && y2<s1){
            this[x2,y2] += str
        }
    }
    fun drawCircle(x: Double, y: Double, rad: Double, str:Double) {
        val steps = 1 + Math.floor(rad * 6.5).toInt()
        var xl = -1
        var yl = -1
        for(i in 0..steps){
            val theta = 2 * Math.PI * i / steps.toDouble()
            val x2 = (x + rad * Math.sin(theta)).toInt()
            val y2 = (y + rad * Math.cos(theta)).toInt()
            if (x2!=xl || y2!=yl) {
                drawPoint(x2, y2,str)
            }
            xl = x2
            yl = y2
        }
    }
    fun fillCircle(x: Double, y: Double, rad: Double, str:Double) {
        for(i in Math.floor(x-rad).toInt() until Math.ceil(x+rad).toInt()){
            for(j in Math.floor(y-rad).toInt() until Math.ceil(y+rad).toInt()){
                val dx = x - i
                val dy = y - j
                val d = dx*dx+dy*dy
                if (d > rad*rad) continue
                drawPoint(i,j,str)
            }
        }
    }
    fun nonZeros() = a.count {it != 0.0}

}


fun newMatrix(s0:Int, s1:Int):Matrix = Matrix(DoubleArray(s0*s1), s0, s1)
fun newMatrix(s0:Int, s1:Int, fn:(Int,Int)->Double):Matrix {
    val ret = newMatrix(s0,s1)
    for(i in 0 until s0) for (j in 0 until s1) ret[i,j] = fn(i,j)
    return ret
}

fun newMatrix(a: List<DoubleArray>) =
        newMatrix(a.size,a[0].size) { i, j->a[i][j]}





