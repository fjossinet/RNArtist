package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
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
    val explorer = Explorer(this)
    val embeddedDBGUI = EmbeddedDBGUI(this)
    val projectManager = ProjectManager(this)
    var webBrowser: WebBrowser? =
        WebBrowser(this)
    lateinit var canvas2D: Canvas2D
    var chimeraDriver: ChimeraDriver? = null

    //++++++ shortcuts
    val current2DDrawing: SecondaryStructureDrawing?
        get() {
            return this.canvas2D.secondaryStructureDrawing
        }
    var tertiaryStructure: TertiaryStructure? = null
        get() {
            return this.secondaryStructure?.tertiaryStructure
        }
    val secondaryStructure: SecondaryStructure?
        get() {
            return this.current2DDrawing?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val theme: Theme?
        get() {
            return this.current2DDrawing?.theme
        }
    val workingSession: WorkingSession?
        get() {
            return this.current2DDrawing?.workingSession
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

    /*
    This function test if a SecondaryStructureElement, depending of its type, is selected or not. This function is used by the function addToSelection
    The criteria is a little bit different than the isSelected in RNArtistCore, since we take care of the parents too here.
     */
    private fun isSelected(structureElement:SecondaryStructureElement):Boolean {
        return when (structureElement) {
            is ResidueCircle -> {
                (structureElement as Object) in this.structureElementsSelected
                        || (structureElement.parent is JunctionCircle) && (structureElement.parent as Object)in this.structureElementsSelected
                        || (structureElement.parent is SingleStrandLine) && (structureElement.parent as Object)in this.structureElementsSelected
                        || ((structureElement.parent is SecondaryInteractionLine) && ((structureElement.parent as Object) in this.structureElementsSelected) || (structureElement.parent?.parent is HelixLine) && (structureElement.parent?.parent as Object) in this.structureElementsSelected)
            }
            is SecondaryInteractionLine -> { //Since a SecondaryInteractionLine has been removed from the structureElementsSelected list, it is considered Selected if its parent is selected too
                (structureElement as Object) in this.structureElementsSelected || (structureElement.parent as Object) in this.structureElementsSelected
            } //any other structural element is considered selected if it is itself in the structureElementsSelected list
            is TertiaryInteractionLine -> {
                (structureElement as Object) in this.structureElementsSelected
            }
            else -> (structureElement as Object) in this.structureElementsSelected
        }
    }

    fun addToSelection(selectionEmitter:SelectionEmitter?, clearCurrentSelection:Boolean = false, structureElement:SecondaryStructureElement?) {
        if (clearCurrentSelection) {
            this.structureElementsSelected.clear()
            this.structureElementsSelected.add("Full 2D" as Object)
            toolbox.getStructureElementsSelectedComboBox().setVisibleRowCount(1); //just a single row to display. We need to reduce the visible rows if the previous list was larger (to avoid to display empty rows)
            if (selectionEmitter != SelectionEmitter.EXPLORER)
                this.selectInExplorer()
            this.selectInCanvas2D()
            if (selectionEmitter != SelectionEmitter.JUNCTIONKNOB)
                this.selectInJunctionKnobs()
            this.selectInChimera()
        }
        structureElement?.let {
            if (isSelected(structureElement)) { //if the element is already selected we will select the next one according to the 2D structure
                when (structureElement) {
                    is ResidueCircle -> {
                        this.structureElementsSelected.remove(structureElement as Object) //if the residue is selected, we remove it to add its parent (SecondaryInteraction Line or JunctionCircle) to the current selection. We remove it in order to avoid to set its theme if the user choose "All selected elements"
                        this.addToSelection(selectionEmitter, false, structureElement.parent)
                    }
                    is SecondaryInteractionLine -> {  //if the SecondaryInteractionLine is selected, we remove it to add its parent (HelixLine) to the current selection. We remove it in order to avoid to set its theme if the user choose "All selected elements"
                        this.structureElementsSelected.remove(structureElement as Object)
                        this.addToSelection(selectionEmitter, false, structureElement.parent)
                    }
                    is JunctionCircle -> { //if the JunctionCircle is selected, we keep it and add its outer helices to the current selection
                        for (helix in (structureElement as JunctionCircle).helices) {
                            this.addToSelection(selectionEmitter, false, helix)
                        }
                    }
                    is HelixLine -> { //if the HelixLine is selected, we keep it and add the junction fof which this helic is the inHelix
                        for (jc in current2DDrawing!!.allJunctions)
                            if (jc.inHelix == (structureElement as HelixLine).helix) {
                                this.addToSelection(selectionEmitter, false, jc)
                            }
                    }
                    is SingleStrandLine -> { //if the SingleStrandLine is selected, we keep it and do nothing more

                    }
                    is TertiaryInteractionLine -> { //if the SingleStrandLine is selected, we keep it and do nothing more

                    }
                    else -> {

                    }
                }
            } else {
                if (structureElementsSelected.size == 2) //meaning Global Theme + a selected element -> we need to add the option "All Selected Elements"
                    structureElementsSelected.add(1, "All Selected Elements" as Object);
                structureElementsSelected.add(it as Object);
                toolbox.getStructureElementsSelectedComboBox().value = structureElement
                if (("All Selected Elements" as Object) in structureElementsSelected) //more cool like that, since if we have several elements selected, this is to pimp them all in general
                    toolbox.getStructureElementsSelectedComboBox().value = "All Selected Elements"
                toolbox.getStructureElementsSelectedComboBox().setVisibleRowCount(Math.min(toolbox.getStructureElementsSelectedComboBox().items.size, 20)); //max 20 elements listed
                if (selectionEmitter != SelectionEmitter.EXPLORER)
                    this.selectInExplorer()
                this.selectInCanvas2D()
                if (selectionEmitter != SelectionEmitter.JUNCTIONKNOB)
                    this.selectInJunctionKnobs()
                this.selectInChimera()

                //TODO highly CPU consuming -> to improve
                current2DDrawing?.tertiaryInteractions?.forEach {
                    //if is not selected itsel but its residues, we add it to the selection
                    if (!this.isSelected(it) && this.isSelected(it.residue) && this.isSelected(it.pairedResidue)) {
                        this.addToSelection(selectionEmitter, false, it)
                    }
                }
            }
        }
    }

    private fun selectInExplorer() {
        this.explorer.clearSelection()
        this.structureElementsSelected.forEach {
            (it as? SecondaryStructureElement)?.let {
                this.explorer.select(it as SecondaryStructureElement)
            }
        }
    }

    private fun selectInJunctionKnobs() {
        //clear
        for (child in toolbox.getJunctionKnobs().getChildren()) {
            val knob = (child as VBox).children[0] as JunctionKnob
            knob.unselect()
        }
        this.structureElementsSelected.forEach {
            (it as? JunctionCircle)?.let {
                var knobFound: VBox? = null
                for (child in toolbox.getJunctionKnobs().getChildren()) {
                    val knob = (child as VBox).children[0] as JunctionKnob
                    if (knob.junctionCircle == it) {
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
    }

    //the function synchronize the selection list of the working session.
    // Each SecondaryStructureElement will take care of this new selection (to fade or not for example) during its redraw()
    //
    private fun selectInCanvas2D() {
        this.workingSession?.selection?.let { selection ->
            this.current2DDrawing?.let { drawing ->
                //clear
                selection.clear()
                this.structureElementsSelected.forEach {
                    (it as? SecondaryStructureElement)?.let {
                        selection.add(it)
                    }
                }
                if (RnartistConfig.fitDisplayOnSelection) { //fit first since fit will center too
                    this.workingSession?.selectionBounds?.let { selectionBounds ->
                        this.canvas2D.fitDisplayOn(selectionBounds)
                    }
                } else if (RnartistConfig.centerDisplayOnSelection) {
                    this.workingSession?.selectionBounds?.let { selectionBounds ->
                        this.canvas2D.centerDisplayOn(selectionBounds)
                    }
                } else Unit
            }
        }
    }

    private fun selectInChimera() {
        this.chimeraDriver?.let { chimeraDriver ->
            val positions: MutableList<String> = ArrayList(1)
            (this.tertiaryStructure as? TertiaryStructure)?.let { tertiaryStructure ->
                for (absPos in workingSession!!.selectedAbsPositions) {
                    (tertiaryStructure.getResidue3DAt(absPos) as Residue3D)?.let {
                        positions.add(if (it.label!= null) it.label!!  else "" + (absPos + 1))
                    }
                }
                chimeraDriver.selectResidues(positions, this.secondaryStructure?.rna?.name)
                chimeraDriver.setFocus(positions, this.secondaryStructure?.rna?.name)
            }
        }
    }

}