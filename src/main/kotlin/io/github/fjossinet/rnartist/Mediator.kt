package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistDrawing
import javafx.beans.property.SimpleObjectProperty
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class Mediator(val rnartist: RNArtist) {

    var currentDrawing: SimpleObjectProperty<RNArtistDrawing?> = SimpleObjectProperty<RNArtistDrawing?>(null)

    //var chimeraDriver = ChimeraXDriver(this)
    val scriptEngine: ScriptEngine
    val lastDrawingHighlighted = SimpleObjectProperty<DrawingElement?>()
    lateinit var canvas2D: Canvas2D

    //++++++ some shortcuts
    private val secondaryStructure: SecondaryStructure?
        get() {
            return this.currentDrawing.get()?.secondaryStructureDrawing?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val workingSession: WorkingSession?
        get() {
            return this.currentDrawing.get()?.secondaryStructureDrawing?.workingSession
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
        this.currentDrawing.addListener { _, _, _ ->
            canvas2D.repaint()
        }
        this.scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
    }

    fun rollbackThemeHistoryToStart() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            if (themeEl.undoRedoCursor != 0) {
                themeEl.undoRedoCursor = 0
                currentDrawing.secondaryStructureDrawing.clearTheme()
                this.canvas2D.repaint()
            }
        }
    }

    fun rollbackToPreviousThemeInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            themeEl.getPreviousThemeInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.clearTheme()
                currentDrawing.secondaryStructureDrawing.applyTheme(it)
                this.canvas2D.repaint()
            }?: run {
                currentDrawing.secondaryStructureDrawing.clearTheme()
                this.canvas2D.repaint()
            }

        }
    }

    fun applyNextThemeInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            themeEl.getNextThemeInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.applyTheme(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun applyThemeInHistoryFromNextToEnd() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            themeEl.getThemeInHistoryFromNextToEnd()?.let {
                currentDrawing.secondaryStructureDrawing.applyTheme(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun rollbackToPreviousJunctionLayoutInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            layoutEl.rollbackToPreviousJunctionLayoutInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.applyLayout(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun rollbackLayoutHistoryToStart() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            if (layoutEl.undoRedoCursor != 0) {
                layoutEl.undoRedoCursor = 0
                currentDrawing.secondaryStructureDrawing.clearLayout()
                this.canvas2D.repaint()
            }
        }
    }

    fun applyNextLayoutInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            layoutEl.getNextJunctionLayoutInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.applyLayout(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun applyLayoutsInHistoryFromNextToEnd() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            layoutEl.getJunctionLayoutInHistoryFromNextToEnd()?.let {
                currentDrawing.secondaryStructureDrawing.applyLayout(it)
                this.canvas2D.repaint()
            }
        }
    }

    /*
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
    }*/

}