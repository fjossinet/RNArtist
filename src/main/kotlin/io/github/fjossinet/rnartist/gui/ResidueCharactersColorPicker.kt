package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.layout.Background
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.SVGPath

class ResidueCharactersColorPicker(mediator: Mediator) : RNArtistColorPicker(mediator) {

    override var behaviors = mapOf(
        Pair("Any") { e: DrawingElement -> e is ResidueLetterDrawing && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Adenines") { e: DrawingElement -> e is A && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Uracils") {e:DrawingElement -> e is U && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Guanines") {e:DrawingElement -> e is G && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Cytosines") {e:DrawingElement -> e is C && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Purines") {e:DrawingElement -> (e is A || e is G) && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Pyrimidines") {e:DrawingElement -> (e is U || e is C) && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true},
        Pair("Unknowns") {e:DrawingElement -> e is X && if (this.restrictedToSelection) e.pathToStructuralDomain().any { mediator.canvas2D.getSelection().contains(it) } else true}
    )

    init {
        val labels = listOf("Any", "Adenines", "Uracils", "Guanines", "Cytosines", "Purines", "Pyrimidines", "Unknowns")
        this.targetsComboBox.items.addAll(labels)
        this.targetsComboBox.value = labels.first()
    }

}