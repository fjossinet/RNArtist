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
    val scriptEngine:ScriptEngine
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
        this.currentDrawing.addListener {
                _, _, _ ->
            canvas2D.repaint()
        }
        this.scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
    }

    fun reloadTheme() {
        currentDrawing.get()?.let { currentDrawing ->
            val newTheme = this.scriptEngine.eval(
                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                    System.getProperty(
                        "line.separator"
                    )
                } ${currentDrawing.rnArtistEl.getThemeOrNew().dump()}"
            ) as? Theme
            newTheme?.let {
                currentDrawing.secondaryStructureDrawing.clearTheme()
                currentDrawing.secondaryStructureDrawing.applyTheme(newTheme)
                this.canvas2D.repaint()
            }
        }

    }

    fun reloadLayout() {
        currentDrawing.get()?.let { currentDrawing ->
            val newLayout = this.scriptEngine.eval(
                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                    System.getProperty(
                        "line.separator"
                    )
                } ${currentDrawing.rnArtistEl.getLayoutOrNew().dump()}"
            ) as? Layout
            newLayout?.let {
                currentDrawing.secondaryStructureDrawing.clearLayout()
                currentDrawing.secondaryStructureDrawing.applyLayout(newLayout)
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