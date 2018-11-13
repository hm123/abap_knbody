package display

import mechanics.Matrix
import mechanics.Matrix3
import mechanics.newMatrix
import toBitmap
import java.awt.BorderLayout
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

val frameLast = JFrame("FrameDemo")

fun show(m : Matrix) {
    val frame = frameLast

//2. Optional: What happens when the frame closes?
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

//3. Create components and put them in the frame.
//...create emptyLabel...
    var img = m.toBitmap()
    val picLabel = JLabel(ImageIcon(img))
    frame.contentPane.removeAll()
    frame.contentPane.add(picLabel, BorderLayout.CENTER)

//4. Size the frame.
    frame.pack()

//5. Show it.
    frame.isVisible = true
}


fun show(m : Matrix3) {
    val frame = frameLast

//2. Optional: What happens when the frame closes?
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

//3. Create components and put them in the frame.
//...create emptyLabel...
    var img = m.toBitmap()
    val picLabel = JLabel(ImageIcon(img))
    frame.contentPane.removeAll()
    frame.contentPane.add(picLabel, BorderLayout.CENTER)

//4. Size the frame.
    frame.pack()

//5. Show it.
    frame.isVisible = true

}