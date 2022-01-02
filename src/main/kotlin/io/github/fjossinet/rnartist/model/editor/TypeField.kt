package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TypeChooser
import javafx.event.EventHandler
import javafx.scene.paint.Color

class TypeField(script: Script, value:String, types:List<String>? = null): ParameterField(script, "\"${value}\"", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = script.children.indexOf(this.text)
                val chooser = TypeChooser(script, this.text, types)
                chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        this.text.text = "\"${chooser.getSelection()}\""
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

    override fun clone():TypeField = TypeField(script, this.text.text.replace("\"",""))
}