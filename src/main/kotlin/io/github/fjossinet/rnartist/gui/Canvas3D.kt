package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import javafx.scene.paint.Color

class Canvas3D: Pane() {

    init {
        this.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        this.prefWidth = USE_COMPUTED_SIZE
        this.prefHeight = USE_COMPUTED_SIZE
    }

}