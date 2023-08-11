package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.Theme
import io.github.fjossinet.rnartist.core.model.ThemeParameter
import io.github.fjossinet.rnartist.gui.LinearButtonsPanel
import javafx.event.EventHandler
import javafx.scene.control.Button
import org.kordamp.ikonli.javafx.FontIcon

class SelectionDetailsLevelButtonsPanel(mediator: Mediator) : LinearButtonsPanel(mediator = mediator) {

    init {
        val lowDetails = Button(null, FontIcon("fas-pen:15"))
        lowDetails.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            lowDetails.isDisable = newValue == null
        }
        lowDetails.setStyle("-fx-font-weight: bold")
        lowDetails.onAction = EventHandler {
            mediator.currentDrawing.get()?.let { currentDrawing->
                var t = Theme()
                t.addConfiguration(
                    {  el -> mediator.canvas2D.getSelection().contains(el) },
                    ThemeParameter.fulldetails,
                    { el -> "false"}
                )
                currentDrawing.drawing.applyTheme(t)
                mediator.canvas2D.repaint()
            }
        }
        this.addButton(lowDetails)

        val highDetails = Button(null, FontIcon("fas-palette:15"))
        highDetails.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            highDetails.isDisable = newValue == null
        }
        highDetails.setStyle("-fx-font-weight: bold")
        highDetails.onAction = EventHandler {
            mediator.currentDrawing.get()?.let { currentDrawing->
                var t = Theme()
                t.addConfiguration(
                    {  el -> mediator.canvas2D.getSelection().contains(el) },
                    ThemeParameter.fulldetails,
                    { el -> "true"}
                )
                currentDrawing.drawing.applyTheme(t)
                mediator.canvas2D.repaint()
            }
        }
        this.addButton(highDetails)

    }

}