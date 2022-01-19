package io.github.fjossinet.rnartist.gui.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import javafx.collections.FXCollections
import javafx.scene.control.ComboBox
import javafx.scene.text.Text

class ColorSchemeChooser(val editor:Script, text: Text):ComboBox<String>() {

    init {
        this.minHeight = 30.0
        this.prefHeight = 30.0

        val items = FXCollections.observableArrayList(RnartistConfig.colorSchemes.keys.sorted())
        this.items = items

    }

    fun getSelection() = this.value

    override fun getBaselineOffset(): Double {
        return 20.0
    }

}