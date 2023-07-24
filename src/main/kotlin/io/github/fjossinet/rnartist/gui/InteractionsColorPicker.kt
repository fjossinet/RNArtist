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
    }

}