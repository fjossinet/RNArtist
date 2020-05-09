package fr.unistra.rnartist.gui

import fr.unistra.rnartist.RNArtist
import fr.unistra.rnartist.io.ChimeraDriver
import fr.unistra.rnartist.io.getImage
import fr.unistra.rnartist.model.SecondaryStructureDrawing
import fr.unistra.rnartist.model.TertiaryStructure
import fr.unistra.rnartist.model.io.EmbeddedDB
import javafx.collections.FXCollections
import java.awt.image.RenderedImage
import java.io.File
import javax.imageio.ImageIO


class Mediator(val rnartist: RNArtist) {

    val allStructures = FXCollections.observableArrayList<SecondaryStructureDrawing>()
    val embeddedDB = EmbeddedDB()
    val toolbox = Toolbox(this)
    val embeddedDBGUI = EmbeddedDBGUI(this)
    val projectManager = ProjectManager(this)
    var webBrowser: WebBrowser? = WebBrowser(this)

    lateinit var canvas2D: Canvas2D
    var tertiaryStructure:TertiaryStructure? = null
        get() {
            return this.canvas2D.secondaryStructureDrawing.get()?.secondaryStructure?.tertiaryStructure
        }
    var chimeraDriver: ChimeraDriver? = null

    init {
        val img = File(File(File(embeddedDB.rootDir,"images"),"user"),"New Project.png");
        if (!img.exists())
            ImageIO.write(getImage("New Project.png") as RenderedImage, "png", img)
    }


}