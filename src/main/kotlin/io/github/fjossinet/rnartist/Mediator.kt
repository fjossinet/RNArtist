package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.ChimeraXDriver
import io.github.fjossinet.rnartist.gui.Full2DButtonsPanel
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistDrawing
import io.github.fjossinet.rnartist.gui.DBExplorer
import io.github.fjossinet.rnartist.gui.Actions3DButtonsPanel
import javafx.beans.property.SimpleObjectProperty

class Mediator(val rnartist: RNArtist) {

    var currentDrawing: SimpleObjectProperty<RNArtistDrawing?> = SimpleObjectProperty<RNArtistDrawing?>(null)
    val embeddedDB = EmbeddedDB()
    var chimeraDriver = ChimeraXDriver(this)
    val DBExplorer = DBExplorer(this)
    val colorsPickers = mutableListOf<RNArtistColorPicker>()
    lateinit var tertiaryStructureButtonsPanel:Actions3DButtonsPanel
    lateinit var canvas2D: Canvas2D

    //++++++ some shortcuts
    private val secondaryStructure: SecondaryStructure?
        get() {
            return this.currentDrawing.get()?.drawing?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val workingSession: WorkingSession?
        get() {
            return this.currentDrawing.get()?.drawing?.workingSession
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
        this.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            canvas2D.repaint()
        }
    }

    fun focusInChimera() {
        this.currentDrawing.get()?.let { drawingDisplayed ->
            chimeraDriver.setFocus(
                canvas2D.getSelectedPositions()
            )
        }
    }

    fun pivotInChimera() {
        this.currentDrawing.get()?.let { drawingDisplayed->
            chimeraDriver.setPivot(
                canvas2D.getSelectedPositions()
            )
        }
    }

}