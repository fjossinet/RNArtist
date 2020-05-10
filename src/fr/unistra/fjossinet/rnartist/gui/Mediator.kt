package fr.unistra.fjossinet.rnartist.gui

import fr.unistra.fjossinet.rnartist.io.ChimeraDriver
import fr.unistra.fjossinet.rnartist.io.getImage
import fr.unistra.fjossinet.rnartist.model.io.EmbeddedDB
import fr.unistra.fjossinet.rnartist.*
import fr.unistra.fjossinet.rnartist.model.*
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
    var chimeraDriver: ChimeraDriver? = null

    //++++++ shortcuts
    val secondaryStructureDrawing: SecondaryStructureDrawing?
        get() {
            return this.canvas2D.secondaryStructureDrawing
        }
    var tertiaryStructure: TertiaryStructure? = null
        get() {
            return this.secondaryStructure?.tertiaryStructure
        }
    val selectedResidues:MutableList<ResidueCircle>?
        get() {
            return this.workingSession?.selectedResidues
        }
    val secondaryStructure: SecondaryStructure?
        get() {
            return this.secondaryStructureDrawing?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val theme: Theme?
        get() {
            return this.secondaryStructureDrawing?.theme
        }
    val workingSession: WorkingSession?
        get() {
            return this.secondaryStructureDrawing?.workingSession
        }

    init {
        val img = File(File(File(embeddedDB.rootDir,"images"),"user"),"New Project.png");
        if (!img.exists())
            ImageIO.write(getImage("New Project.png") as RenderedImage, "png", img)
    }


}