package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor

import javafx.geometry.Insets
import javafx.scene.layout.HBox

class HBox: HBox() {

    init {
        prefHeight = 30.0
        padding = Insets(0.0, 0.0, 0.0, 0.0);
        spacing = 5.0
    }

    override fun getBaselineOffset(): Double {
        return 20.0
    }
}