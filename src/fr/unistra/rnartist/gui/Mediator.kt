package fr.unistra.rnartist.gui

import fr.unistra.rnartist.RNArtist
import fr.unistra.rnartist.io.ChimeraDriver
import fr.unistra.rnartist.io.EmbeddedDB
import fr.unistra.rnartist.model.Theme
import fr.unistra.rnartist.model.TertiaryStructure

class Mediator(val rnartist: RNArtist) {

    val embeddedDB = EmbeddedDB(this)
    val toolbox = Toolbox(this)
    val embeddedDBGUI = EmbeddedDBGUI(this)
    val projectManager = ProjectManager(this)

    lateinit var canvas2D: Canvas2D
    val theme = Theme(this)
    val graphicsContext = GraphicContext(this)
    var tertiaryStructure:TertiaryStructure? = null
    var chimeraDriver: ChimeraDriver? = null

}