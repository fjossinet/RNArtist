package io.github.fjossinet.rnartist.gui.editor

import javafx.geometry.Insets
import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color

class ColorPicker(val editor: Script, c: Color): ColorPicker(c) {

    init {
        this.minHeight = 30.0
        this.prefHeight = 30.0
        this.padding = Insets(5.0, 5.0, 5.0, 5.0)
        this.styleClass.add("split-button")
    }

    override fun getBaselineOffset(): Double {
        return 20.0
    }
}