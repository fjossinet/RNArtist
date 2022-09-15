package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.io.ChimeraXDriver
import io.github.fjossinet.rnartist.gui.Actions2DButtonsPanel
import io.github.fjossinet.rnartist.gui.DrawingLoaded
import io.github.fjossinet.rnartist.gui.DrawingsLoadedPanel
import io.github.fjossinet.rnartist.gui.Actions3DButtonsPanel
import io.github.fjossinet.rnartist.model.editor.RfamKw
import javafx.beans.property.SimpleObjectProperty

class Mediator(val rnartist: RNArtist) {

    var drawingDisplayed: SimpleObjectProperty<DrawingLoaded?> = SimpleObjectProperty<DrawingLoaded?>(null)
    val embeddedDB = EmbeddedDB()
    var chimeraDriver = ChimeraXDriver(this)
    val scriptEditor = ScriptEditor(this)
    val drawingsLoadedPanel = DrawingsLoadedPanel(this)
    val projectsPanel = ProjectsPanel(this)
    lateinit var actions2DButtonsPanel:Actions2DButtonsPanel
    lateinit var tertiaryStructureButtonsPanel:Actions3DButtonsPanel
    lateinit var canvas2D: Canvas2D

    //++++++ some shortcuts
    private val secondaryStructure: SecondaryStructure?
        get() {
            return this.drawingDisplayed.get()?.drawing?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val workingSession: WorkingSession?
        get() {
            return this.drawingDisplayed.get()?.drawing?.workingSession
        }

    val viewX: Double?
        get() {
            return this.workingSession?.viewX
        }

    val viewY: Double?
        get() {
            return this.workingSession?.viewY
        }

    val zoomLevel: Double?
        get() {
            return this.workingSession?.zoomLevel
        }


    init {

        this.chimeraDriver.connectToRestServer() //to see if at start, RNArtist can connect to ChimeraX rest Server with the current parameters

        this.drawingDisplayed.addListener {
                observableValue, oldValue, newValue ->

            if (newValue == null) { //this means that the menu of 2Ds loaded has been cleared
                this.chimeraDriver.closeSession()
            }
            drawingDisplayed.get()?.drawing?.let {  drawing ->
                drawing.secondaryStructure.source?.let { source ->
                    if (source.toString().startsWith("db:rfam")) { //we record in the script the molecule chosen for this Rfam alignment
                        (scriptEditor.script.getScriptRoot().getSecondaryStructureKw().searchFirst  { it is RfamKw && it.inFinalScript && it.getId().equals(source.getId()) } as RfamKw).setName(drawing.secondaryStructure.name)
                    } else if (source.toString().startsWith("db:pdb") || source.toString().startsWith("local:file") && source.toString().endsWith("pdb")) {
                        chimeraDriver.displayCurrent3D()
                    }
                }
            }
        }

    }

    fun focusInChimera() {
        this.drawingDisplayed.get()?.let { drawingDisplayed ->
            chimeraDriver.setFocus(
                canvas2D.getSelectedPositions()
            )
        }
    }

    fun pivotInChimera() {
        this.drawingDisplayed.get()?.let { drawingDisplayed->
            chimeraDriver.setPivot(
                canvas2D.getSelectedPositions()
            )
        }
    }

}