package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.RnartistConfig.displayTertiariesInSelection
import io.github.fjossinet.rnartist.core.model.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.getImage
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
    var webBrowser: WebBrowser? =
        WebBrowser(this)
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

    val viewX: Double?
        get() {
            return this.workingSession?.viewX
        }

    val viewY: Double?
        get() {
            return this.workingSession?.viewY
        }

    val finalZoomLevel: Double?
        get() {
            return this.workingSession?.finalZoomLevel
        }


    init {
        val img = File(File(File(embeddedDB.rootDir,"images"),"user"),"New Project.png");
        if (!img.exists())
            ImageIO.write(getImage("New Project.png") as RenderedImage, "png", img)
    }

    fun addToSelection(clearPreviousSelection:Boolean = false, vararg absolutePositions:Int) {
        this.selectedResidues?.let { selection ->
            this.secondaryStructureDrawing?.let { drawing ->
                if (!absolutePositions.isEmpty()) {
                    if (clearPreviousSelection)
                        selection.clear()
                    selection.addAll(drawing.getResiduesFromAbsPositions(*absolutePositions))
                    if (displayTertiariesInSelection && !drawing.tertiaryInteractions.isEmpty()) {
                        var residues2Add = mutableListOf<ResidueCircle>()
                        do {
                            residues2Add.clear()
                            for (selectedResidue in selection) {
                                for (tertiary in drawing.tertiaryInteractions) {
                                    if (tertiary.start == selectedResidue.absPos) {
                                        val c: ResidueCircle =
                                            drawing.getResiduesFromAbsPositions(
                                                tertiary.end
                                            ).first()
                                        if (c !in selection)
                                            residues2Add.add(c)
                                    } else if (tertiary.end == selectedResidue.absPos) {
                                        val c: ResidueCircle =
                                            drawing.getResiduesFromAbsPositions(
                                                tertiary.start
                                            ).first()
                                        if (c !in selection)
                                            residues2Add.add(c)
                                    }
                                }
                            }
                            selection.addAll(residues2Add)
                        } while (!residues2Add.isEmpty())
                    }
                    if (RnartistConfig.fitDisplayOnSelection) { //fit first since fit will center too
                        this.workingSession?.selectionBounds?.let { selectionBounds ->
                            this.canvas2D.fitDisplayOn(selectionBounds)
                        }
                    }
                    else if (RnartistConfig.centerDisplayOnSelection) {
                        this.workingSession?.selectionBounds?.let { selectionBounds ->
                            this.canvas2D.centerDisplayOn(selectionBounds)
                        }
                    }
                }
            }
        }

    }


}