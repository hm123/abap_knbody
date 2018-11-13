import mechanics.Matrix
import mechanics.Matrix3
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

fun Matrix.toBitmap(): BufferedImage {
    var m = this
    val img = BufferedImage(m.s0, m.s1, BufferedImage.TYPE_INT_ARGB)
    val srcBuffer = img.raster.dataBuffer as DataBufferInt
    //Grab the graphics object off the image

    for(i in 0 until m.s0){
        for(j in 0 until m.s1) {
            val v = m[i,s1-1-j]
            val ind = i + j * m.s0
            var r: Int
            var g: Int
            var b: Int
            if (v > 0) {
                r = (255 * (1 - Math.exp(-v / 10))).toInt()
                g = (255 * (1 - Math.exp(-v))).toInt()
                b = (255 * (1 - Math.exp(-v / 0.1))).toInt()
            } else {
                b = (255 * (1 - Math.exp(v / 10))).toInt()
                g = (255 * (1 - Math.exp(v))).toInt()
                r = (255 * (1 - Math.exp(v / 0.1))).toInt()
            }

            if (!v.isFinite()) {
                r = 255;g = 0;b = 0;
            }
            val c = b + (g * 256) + (r * 256 * 256) + (-1) * 256 * 256 * 256
            srcBuffer.setElem(ind, c)
        }
    }
    return img
}


fun Matrix3.toBitmap(): BufferedImage {
    var m = this
    val img = BufferedImage(m.red.s0, m.red.s1, BufferedImage.TYPE_INT_ARGB)
    val srcBuffer = img.raster.dataBuffer as DataBufferInt
    //Grab the graphics object off the image
    val red = m.red
    for(i in 0 until red.s0){
        for(j in 0 until red.s1) {
            val vr = m.red[i,red.s1-1-j]
            val vg = m.green[i,red.s1-1-j]
            val vb = m.blue[i,red.s1-1-j]

            val ind = i + j * red.s0
            var r: Int
            var g: Int
            var b: Int
                r = (255 * (1 - Math.exp(-vr))).toInt()
                g = (255 * (1 - Math.exp(-vg))).toInt()
                b = (255 * (1 - Math.exp(-vb))).toInt()


            if (!vr.isFinite()) {
                r = 255;g = 0;b = 0;
            }
            val c = b + (g * 256) + (r * 256 * 256) + (-1) * 256 * 256 * 256
            srcBuffer.setElem(ind, c)
        }
    }
    return img
}
