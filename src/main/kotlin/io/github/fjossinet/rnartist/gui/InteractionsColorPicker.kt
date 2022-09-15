package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.SVGPath

class InteractionsColorPicker(mediator: Mediator) : RNArtistColorPicker(mediator) {

    override var behaviors = mapOf(
        Pair("Secondaries") {e:DrawingElement -> e is SecondaryInteractionDrawing && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) }  else true},
        Pair("Tertiaries") {e:DrawingElement -> e is TertiaryInteractionDrawing && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Phosphodiester") {e:DrawingElement -> e is PhosphodiesterBondDrawing && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Any") {e:DrawingElement -> (e is PhosphodiesterBondDrawing || e is TertiaryInteractionDrawing || e is SecondaryInteractionDrawing) && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true}
    )

    init {
        val labels = listOf("Any", "Secondaries", "Tertiaries", "Phosphodiester")
        this.targetsComboBox.items.addAll(labels)
        this.targetsComboBox.value = labels.first()

        var c = Circle(10.0, 10.0, 10.0)
        var restricted2SelectionPath = SVGPath()
        restricted2SelectionPath.content = "M165,330c63.411,0,115-51.589,115-115c0-29.771-11.373-56.936-30-77.379V85c0-46.869-38.131-85-85-85 S80.001,38.131,80.001,85v52.619C61.373,158.064,50,185.229,50,215C50,278.411,101.589,330,165,330z M180,219.986V240 c0,8.284-6.716,15-15,15s-15-6.716-15-15v-20.014c-6.068-4.565-10-11.824-10-19.986c0-13.785,11.215-25,25-25s25,11.215,25,25 C190,208.162,186.068,215.421,180,219.986z M110.001,85c0-30.327,24.673-55,54.999-55c30.327,0,55,24.673,55,55v29.029 C203.652,105.088,184.91,100,165,100c-19.909,0-38.651,5.088-54.999,14.028V85z"
        restricted2SelectionPath.fill = if (restrictedToSelection) Color.DARKORANGE else Color.LIGHTGRAY
        restricted2SelectionPath.scaleX = 0.07
        restricted2SelectionPath.scaleY = 0.07
        val useSelection = Button(null, restricted2SelectionPath)
        useSelection.onMouseClicked = EventHandler {
            restrictedToSelection = !restrictedToSelection
            restricted2SelectionPath.fill = if (restrictedToSelection) Color.DARKORANGE else Color.LIGHTGRAY
        }
        useSelection.background = Background.EMPTY
        useSelection.layoutX = 0.0
        useSelection.layoutY = 0.0
        useSelection.shape = c
        useSelection.setMinSize(20.0, 20.0)
        useSelection.setMaxSize(20.0, 20.0)
        colorWheelGroup.children.add(useSelection)
    }

}