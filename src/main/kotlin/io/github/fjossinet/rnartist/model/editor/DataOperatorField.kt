package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.ChoiceBox
import javafx.scene.paint.Color

class DataOperatorField(script: Script, value:String): Operator(script, "${value}", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = script.children.indexOf(this.text)
                val chooser = ChoiceBox(FXCollections.observableArrayList("lt", "eq", "gt"))
                chooser.value = text.text
                chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        text.text = " ${chooser.value} "
                        script.children.removeAt(index)
                        script.children.add(index, this.text)
                    }
                }
                script.children.removeAt(index)
                script.children.add(index, chooser)
                chooser.requestFocus()
            }
        }
    }

    override fun clone():DataOperatorField = DataOperatorField(script, this.text.text)
}