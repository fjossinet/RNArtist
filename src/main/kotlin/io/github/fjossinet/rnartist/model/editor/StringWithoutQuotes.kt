package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler

open class StringWithoutQuotes(script: Script, value:String, editable: Boolean = false): ParameterField(script, value, editable) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = script.children.indexOf(this.text)
                val textField = TextField(script, this.text)
                textField.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        this.text.text = "${textField.text.trim()}"
                        script.children.removeAt(index)
                        script.children.add(index, this.text)
                    }
                }
                script.children.removeAt(index)
                script.children.add(index, textField)
            }
        }
    }

    override fun clone():StringWithoutQuotes = StringWithoutQuotes(script, this.text.text, this.editable)

}