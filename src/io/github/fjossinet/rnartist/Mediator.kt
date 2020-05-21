package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.RnartistConfig.displayTertiariesInSelection
import io.github.fjossinet.rnartist.core.model.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.getImage
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.layout.VBox
import java.awt.image.RenderedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class Mediator(val rnartist: RNArtist) {

    enum class SelectionEmitter {
        CANVAS2D, JUNCTIONKNOB, EXPLORER
    }

    val allStructures = FXCollections.observableArrayList<SecondaryStructureDrawing>()
    val structureElementsSelected: ObservableList<Object> = FXCollections.observableList(ArrayList())

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

        //+++++list of the structural elements selected listening to theme modifications
        structureElementsSelected.addListener(ListChangeListener { change ->
            if (structureElementsSelected.size == 1) { //if only a single element i, the list (and it always should be the global theme) we select it
                toolbox.getStructureElementsSelectedComboBox().value = structureElementsSelected.get(0)
            }
        })

    }

    fun addToSelection(selectionEmitter:SelectionEmitter?, clearCurrentSelection:Boolean = false, structureElement:SecondaryStructureElement?) {
        if (clearCurrentSelection) {
            for (s in this.structureElementsSelected.toList()) { //we don't remove the SecondaryStructure Drawing
                (s as? SecondaryStructureElement)?.let {
                    this.structureElementsSelected.remove(s)
                }
                (s as? String)?.let {
                    if (s.toString().equals("All Selected Elements"))
                        this.structureElementsSelected.remove(s)
                }
            }
            if (this.structureElementsSelected.isEmpty()) //before the first 2D load, the Global Theme choice is not present (the observable list is empty when RNArtist starts)
                this.structureElementsSelected.add("Full 2D" as Object)
            toolbox.getStructureElementsSelectedComboBox().setVisibleRowCount(1); //just a single row to display. We nned to reduce the visible rows if the previous list was larger (to avoid to display empty rows)
            if (selectionEmitter != SelectionEmitter.EXPLORER)
                this.selectInExplorer(clearCurrentSelection)
            this.selectInCanvas2D(clearCurrentSelection)
            if (selectionEmitter != SelectionEmitter.JUNCTIONKNOB)
                this.selectInJunctionKnobs(clearCurrentSelection)
        }
        structureElement?.let {
            if (structureElementsSelected.size == 2 ) //meaning Global Theme + a selected element -> we need to add the option "All Selected Elements"
                structureElementsSelected.add(1, "All Selected Elements" as Object);
            structureElementsSelected.add(it as Object);
            toolbox.getStructureElementsSelectedComboBox().value = structureElement
            toolbox.getStructureElementsSelectedComboBox().setVisibleRowCount(Math.min(toolbox.getStructureElementsSelectedComboBox().items.size, 20)); //max 20 elements listed
            if (selectionEmitter != SelectionEmitter.EXPLORER)
                this.selectInExplorer(false, structureElement)
            this.selectInCanvas2D(false, structureElement)
            if (selectionEmitter != SelectionEmitter.JUNCTIONKNOB)
                this.selectInJunctionKnobs(false, structureElement)
        }
    }

    private fun selectInExplorer(clearCurrentSelection:Boolean = false, structureElement:SecondaryStructureElement? = null) {
        if (clearCurrentSelection) {
            rnartist.clearSelectionInExplorer()
        }
        structureElement?.let {
            rnartist.selectInExplorer(it)
        }

    }

    private fun selectInJunctionKnobs(clearCurrentSelection:Boolean = false, structureElement:SecondaryStructureElement? = null) {
        if (clearCurrentSelection) {
            for (child in toolbox.getJunctionKnobs().getChildren()) {
                val knob = (child as VBox).children[0] as JunctionKnob
                knob.unselect()
            }
        }
        structureElement?.let {
            var knobFound: VBox? = null
            for (child in toolbox.getJunctionKnobs().getChildren()) {
                val knob = (child as VBox).children[0] as JunctionKnob
                if (knob.junctionCircle == structureElement) {
                    knobFound = child
                    knob.select()
                    break
                }
            }
            if (knobFound != null) {
                toolbox.getJunctionKnobs().getChildren().remove(knobFound)
                toolbox.getJunctionKnobs().getChildren().add(0, knobFound)
            }
        }
    }

    private fun selectInCanvas2D(clearCurrentSelection:Boolean = false, structureElement:SecondaryStructureElement? = null) {
        this.workingSession?.selectedResidues?.let { selection ->
            this.secondaryStructureDrawing?.let { drawing ->
                if (clearCurrentSelection)
                    selection.clear()
                structureElement?.let {
                    val absolutePositions = structureElement.getSinglePositions()
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
                    this.chimeraDriver?.let { chimeraDriver ->
                            val positions: MutableList<String> = ArrayList(1)
                            (this.tertiaryStructure as? TertiaryStructure)?.let { tertiaryStructure ->
                                for (c in selection) {
                                    (tertiaryStructure.getResidue3DAt(c.absPos) as Residue3D)?.let {
                                        positions.add(if (it.label!= null) it.label!!  else "" + (c.absPos + 1))
                                    }
                                }
                                chimeraDriver.selectResidues(positions, drawing.secondaryStructure.rna.name)
                                chimeraDriver.setFocus(positions, drawing.secondaryStructure.rna.name)
                            }

                        }
                    }
                }
            }
        }

}