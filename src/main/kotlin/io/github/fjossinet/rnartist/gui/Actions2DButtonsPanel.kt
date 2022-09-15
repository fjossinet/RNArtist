package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.theme
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.Rectangle2D

class Actions2DButtonsPanel(mediator:Mediator):ButtonsPanel(mediator = mediator, panelRadius = 60.0) {

    var centerDisplayOnSelection = false
    val showTertiariesButton = Button(null, FontIcon("fas-eye:15"))
    var showTertiaries = SimpleBooleanProperty(true)

    init {
        mediator.actions2DButtonsPanel = this

        val center2D = Button(null, FontIcon("fas-crosshairs:15"))
        center2D.onMouseClicked = EventHandler { mouseEvent ->
            if (mouseEvent.isShiftDown) {
                centerDisplayOnSelection = !centerDisplayOnSelection
                if (centerDisplayOnSelection) center2D.graphic = FontIcon("fas-lock:15") else center2D.graphic =
                    FontIcon("fas-crosshairs:15")
            } else {
                val selectionFrame: Rectangle2D? = mediator.canvas2D.getSelectionFrame()
                mediator.drawingDisplayed.get()?.drawing?.let {
                    mediator.canvas2D.centerDisplayOn(selectionFrame ?: it.getFrame())
                }
            }
        }
        center2D.tooltip = Tooltip("Center View on 2D or Selection")
        this.addButton(center2D)

        val fit2D = Button(null, FontIcon("fas-expand-arrows-alt:15"))
        fit2D.onMouseClicked = EventHandler {
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.fitStructure(mediator.canvas2D.getSelectionFrame(), 2.0)
            } else
                mediator.canvas2D.fitStructure(null)
        }
        fit2D.tooltip = Tooltip("Fit 2D or Selection to View")
        this.addButton(fit2D)

        this.showTertiaries.addListener { observableValue, oldValue, newValue ->
            this.showTertiariesButton.graphic = if (newValue) FontIcon("fas-eye:15") else FontIcon("fas-eye-slash:15")
        }

        this.showTertiariesButton.onMouseClicked = EventHandler {
            this.showTertiaries.value = !this.showTertiaries.value
            this.showTertiariesButton.graphic = if (this.showTertiaries.value) FontIcon("fas-eye:15") else FontIcon("fas-eye-slash:15")
            val t = if (this.showTertiaries.value) {
                theme {
                    show {
                        type = "tertiary_interaction"
                    }
                }
            } else {
                theme {
                    hide {
                        type = "tertiary_interaction"
                    }
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t) }
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
        }
        showTertiariesButton.tooltip = Tooltip("Show/Hide Tertiaries")
        this.addButton(showTertiariesButton)

        val clearTheme = Button(null, FontIcon("fas-undo:15"))
        clearTheme.tooltip = Tooltip("Clear Theme")
        clearTheme.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.let {
                it.drawing.clearTheme()
                mediator.canvas2D.repaint()
                mediator.scriptEditor.script.getScriptRoot().getThemeKw().removeButton.fire()
            }
        }
        //this.addButton(clearTheme)

        val clearLayout = Button(null, FontIcon("fas-ban:15"))
        clearLayout.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.let {
                mediator.scriptEditor.script.getScriptRoot().getLayoutKw().removeButton.fire()
            }
        }
        //this.addButton(clearLayout)
    }

}