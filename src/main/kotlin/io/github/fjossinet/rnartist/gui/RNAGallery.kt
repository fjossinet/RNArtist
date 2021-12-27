package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import javafx.geometry.Insets
import javafx.scene.layout.VBox

class RNAGallery(val mediator: Mediator):VBox() {

    init {
        this.spacing = 10.0
        this.padding = Insets(10.0, 10.0, 10.0, 10.0)

    }

}