package io.github.fjossinet.rnartist.io

import javafx.scene.paint.Color
import java.awt.Image
import java.io.InputStream
import java.io.OutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO

fun getImage(imageName: String?): Image? {
    if (imageName == null) {
        return null
    }
    var image: Image? = null
    try {
        image = ImageIO.read({}.javaClass.getResource("images/$imageName"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return image
}

fun javaFXToAwt(c: Color): java.awt.Color {
    return java.awt.Color(
        c.red.toFloat(),
        c.green.toFloat(),
        c.blue.toFloat(),
        c.opacity.toFloat()
    )
}

fun awtColorToJavaFX(c: java.awt.Color): Color {
    return Color.rgb(c.red, c.green, c.blue, c.alpha / 255.0)
}