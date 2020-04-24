package fr.unistra.rnartist.io

import fr.unistra.rnartist.gui.RNArtist
import fr.unistra.rnartist.utils.RnartistConfig
import java.io.File
import java.io.IOException

fun getTmpDirectory(): File? {
    val tmpDir = File(StringBuffer(RnartistConfig.getUserDir().absolutePath).append(System.getProperty("file.separator")).append("tmp").toString())
    if (!tmpDir.exists()) tmpDir.mkdir()
    return tmpDir
}

@Throws(IOException::class)
fun createTemporaryFile(fileName: String): File? {
    val f = File(getTmpDirectory(), fileName + System.nanoTime())
    f.createNewFile()
    f.deleteOnExit()
    return f
}

@Throws(IOException::class)
fun createTemporaryFile(dir: File?, fileName: String): File? {
    val f = File(dir, fileName + System.nanoTime())
    f.createNewFile()
    f.deleteOnExit()
    return f
}