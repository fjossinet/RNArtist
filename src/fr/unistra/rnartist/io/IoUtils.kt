package fr.unistra.rnartist.io

import java.awt.Image
import java.io.*
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

@Throws(IOException::class)
fun copyFile(source: File, target: File) {
    if (source.isDirectory) {
        if (!target.exists()) {
            target.mkdir()
        }
        val children = source.list()
        for (i in children.indices) {
            copyFile(File(source, children[i]), File(target, children[i]))
        }
    } else {
        val `in`: InputStream = FileInputStream(source)
        val out: OutputStream = FileOutputStream(target)

        // Copy the bits from instream to outstream
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    }
}