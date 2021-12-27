package io.github.fjossinet.rnartist.io

import javafx.scene.paint.Color
import okio.ByteString.Companion.encode
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
    return java.awt.Color(c.red.toFloat(),
            c.green.toFloat(),
            c.blue.toFloat(),
            c.opacity.toFloat())
}

fun awtColorToJavaFX(c: java.awt.Color): Color {
    return Color.rgb(c.red, c.green, c.blue, c.alpha / 255.0)
}

fun sendFile(out: OutputStream, name: String, `in`: InputStream, fileName: String) {
    val o = """
        Content-Disposition: form-data; name="${
        URLEncoder.encode(
            name,
            "UTF-8"
        )
    }"; filename="${URLEncoder.encode(fileName, "UTF-8")}"
        
        
        """.trimIndent()
    out.write(o.encode(StandardCharsets.UTF_8).toByteArray())
    val buffer = ByteArray(2048)
    var n = 0
    while (n >= 0) {
        out.write(buffer, 0, n)
        n = `in`.read(buffer)
    }
    out.write("\r\n".encode(StandardCharsets.UTF_8).toByteArray())
}

fun sendField(out: OutputStream, name: String, field: String) {
    val o = """
        Content-Disposition: form-data; name="${URLEncoder.encode(name, "UTF-8")}"
        
        
        """.trimIndent()
    out.write(o.encode(StandardCharsets.UTF_8).toByteArray())
    out.write(URLEncoder.encode(field, "UTF-8").encode(StandardCharsets.UTF_8).toByteArray())
    out.write("\r\n".encode(StandardCharsets.UTF_8).toByteArray())
}