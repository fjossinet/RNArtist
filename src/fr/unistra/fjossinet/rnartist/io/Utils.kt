package fr.unistra.fjossinet.rnartist.io

import java.awt.Image
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