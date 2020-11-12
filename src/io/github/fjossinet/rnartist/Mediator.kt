package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.getImage
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.MenuItem
import javafx.scene.layout.VBox
import java.awt.image.RenderedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class Mediator(val rnartist: RNArtist) {

    enum class SelectionEmitter {
        CANVAS2D, JUNCTIONKNOB, EXPLORER
    }

    val _2DDrawingsLoaded = FXCollections.observableArrayList<SecondaryStructureDrawing>()
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
    var current2DDrawing: SecondaryStructureDrawing?
        set(value) {
            this.canvas2D.secondaryStructureDrawing = value
        }
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
    val theme: Theme
        get() {
            return this.current2DDrawing?.let {
                it.theme
            } ?: Theme()
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
        val img = File(File(File(embeddedDB.rootDir, "images"), "user"), "New Project.png");
        if (!img.exists())
            ImageIO.write(getImage("New Project.png") as RenderedImage, "png", img)

        //+++++list of the structural elements selected listening to theme modifications
        structureElementsSelected.addListener(ListChangeListener { change ->
            if (structureElementsSelected.size == 1) { //if only a single element i, the list (and it always should be the global theme) we select it
                toolbox.getStructureElementsSelectedComboBox().value = structureElementsSelected.get(0)
            }
        })

        this._2DDrawingsLoaded.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    for (drawing in change.addedSubList) {
                        val item = MenuItem(drawing.name)
                        item.userData = drawing
                        item.setOnAction { actionEvent ->
                            canvas2D.load2D(item.userData as SecondaryStructureDrawing)
                            canvas2D.fitDisplayOn(current2DDrawing!!.getBounds())
                        }
                        rnartist.load2DForMenu.items.add(item)
                    }
                } else if (change.wasRemoved()) {
                    val toDelete = mutableListOf<MenuItem>()
                    for (drawing in change.removed) {
                        for (item in rnartist.load2DForMenu.items) {
                            if (item.userData == drawing) {
                                toDelete.add(item)
                            }
                        }
                    }
                    rnartist.load2DForMenu.items.removeAll(toDelete)
                }
            }
            if (!this._2DDrawingsLoaded.isEmpty()) {
                rnartist.clearAll2DsItem.isDisable = false
                rnartist.clearAll2DsExceptCurrentItem.isDisable = false
            }
            else {
                current2DDrawing = null
                rnartist.clearAll2DsItem.isDisable = true
                rnartist.clearAll2DsExceptCurrentItem.isDisable = true
                canvas2D.repaint()
            }
        })

    }

    /*
    This function test if a SecondaryStructureElement, depending of its type, is selected or not. This function is used by the function addToSelection
    The criteria is a little bit different than the isSelected in RNArtistCore, since we take care of the parents too here.
     */
    private fun isSelected(structureElement: DrawingElement): Boolean {
        return when (structureElement) {
            is ResidueDrawing -> {
                (structureElement as Object) in this.structureElementsSelected
                        || (structureElement.parent is JunctionDrawing) && (structureElement.parent as Object) in this.structureElementsSelected
                        || (structureElement.parent is SingleStrandDrawing) && (structureElement.parent as Object) in this.structureElementsSelected
                        || ((structureElement.parent is SecondaryInteractionDrawing) && ((structureElement.parent as Object) in this.structureElementsSelected) || (structureElement.parent?.parent is HelixDrawing) && (structureElement.parent?.parent as Object) in this.structureElementsSelected)
            }
            is SecondaryInteractionDrawing -> { //Since a SecondaryInteractionLine has been removed from the structureElementsSelected list, it is considered Selected if its parent is selected too
                (structureElement as Object) in this.structureElementsSelected || (structureElement.parent as Object) in this.structureElementsSelected
            } //any other structural element is considered selected if it is itself in the structureElementsSelected list
            is TertiaryInteractionDrawing -> {
                (structureElement as Object) in this.structureElementsSelected
            }
            else -> (structureElement as Object) in this.structureElementsSelected
        }
    }

    fun addToSelection(selectionEmitter: SelectionEmitter?, clearCurrentSelection: Boolean = false, structureElement: DrawingElement?) {
        if (clearCurrentSelection) {
            this.structureElementsSelected.clear()
            toolbox.defaultTargetsComboBox.value = null //to trigger a changed event if the previous value was still "Full 2D"
            toolbox.defaultTargetsComboBox.value = "Full 2D"
            if (selectionEmitter != SelectionEmitter.EXPLORER)
                this.selectInExplorer()
            this.selectInCanvas2D()
            if (selectionEmitter != SelectionEmitter.JUNCTIONKNOB)
                this.selectInJunctionKnobs()
        }
        structureElement?.let {
            if (isSelected(structureElement)) { //if the element is already selected we will select the next one according to the 2D structure
                when (structureElement) {
                    is ResidueDrawing -> {
                        this.structureElementsSelected.remove(structureElement as Object) //if the residue is selected, we remove it to add its parent (SecondaryInteraction Line or JunctionCircle) to the current selection. We remove it in order to avoid to set its theme if the user choose "All selected elements"
                        this.addToSelection(selectionEmitter, false, structureElement.parent)
                    }
                    is SecondaryInteractionDrawing -> {  //if the SecondaryInteractionLine is selected, we remove it to add its parent (HelixLine) to the current selection. We remove it in order to avoid to set its theme if the user choose "All selected elements"
                        this.structureElementsSelected.remove(structureElement as Object)
                        this.addToSelection(selectionEmitter, false, structureElement.parent)
                    }
                    is JunctionDrawing -> { //if the JunctionCircle is selected, we keep it and add its outer helices to the current selection
                        for (helix in (structureElement as JunctionDrawing).helices) {
                            this.addToSelection(selectionEmitter, false, helix)
                        }
                    }
                    is HelixDrawing -> { //if the HelixLine is selected, we keep it and add the junction fof which this helic is the inHelix
                        for (jc in current2DDrawing!!.allJunctions)
                            if (jc.inHelix == (structureElement as HelixDrawing).helix) {
                                this.addToSelection(selectionEmitter, false, jc)
                            }
                    }
                    is SingleStrandDrawing -> { //if the SingleStrandLine is selected, we keep it and do nothing more

                    }
                    is TertiaryInteractionDrawing -> { //if the SingleStrandLine is selected, we keep it and do nothing more

                    }
                    else -> {

                    }
                }
            } else {
                if (structureElementsSelected.size == 1)
                    structureElementsSelected.add(0, "All Selected Elements" as Object);
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
            }
        }
    }

    private fun selectInExplorer() {
        this.explorer.clearSelection()
        this.structureElementsSelected.forEach {
            (it as? DrawingElement)?.let {
                this.explorer.select(it as DrawingElement)
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
            (it as? JunctionDrawing)?.let {
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
        this.workingSession?.selectedResidues?.let { selection ->
            this.current2DDrawing?.let { drawing ->
                //clear
                selection.clear()
                this.structureElementsSelected.forEach {
                    (it as? DrawingElement)?.let {
                        selection.addAll(it.residues)
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

    public fun focusInChimera() {
        this.chimeraDriver?.let { chimeraDriver ->
            val positions: MutableList<String> = ArrayList(1)
            (this.tertiaryStructure as? TertiaryStructure)?.let { tertiaryStructure ->
                for (absPos in workingSession!!.selectedAbsPositions) {
                    (tertiaryStructure.getResidue3DAt(absPos) as Residue3D)?.let {
                        positions.add(if (it.label != null) it.label!! else "" + (absPos + 1))
                    }
                }
                chimeraDriver.selectResidues(positions, this.secondaryStructure?.rna?.name)
                chimeraDriver.setFocus(positions, this.secondaryStructure?.rna?.name)
            }
        }
    }

    public fun pivotInChimera() {
        this.chimeraDriver?.let { chimeraDriver ->
            val positions: MutableList<String> = ArrayList(1)
            (this.tertiaryStructure as? TertiaryStructure)?.let { tertiaryStructure ->
                for (absPos in workingSession!!.selectedAbsPositions) {
                    (tertiaryStructure.getResidue3DAt(absPos) as Residue3D)?.let {
                        positions.add(if (it.label != null) it.label!! else "" + (absPos + 1))
                    }
                }
                chimeraDriver.selectResidues(positions, this.secondaryStructure?.rna?.name)
                chimeraDriver.setPivot(positions, this.secondaryStructure?.rna?.name)
            }
        }
    }

}