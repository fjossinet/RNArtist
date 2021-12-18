package io.github.fjossinet.rnartist.gui.editor

import javafx.geometry.Insets
import javafx.scene.control.TextField
import javafx.scene.text.Text

class TextField(var editor: ScriptEditor, text: Text):TextField(text.text.replace("\"", "")) {

    init {
        font = text.font
        prefWidth = text.boundsInParent.width * 2
        minWidth = text.boundsInParent.width * 2
        this.padding = Insets(5.0, 5.0, 5.0, 5.0)
        this.minHeight = 30.0
        this.prefHeight = 30.0
    }

    override fun getBaselineOffset(): Double {
        return 20.0
    }
}