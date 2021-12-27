package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.ChoiceBox
import javafx.scene.paint.Color

class DataOperatorField(editor: ScriptEditor, value:String): Operator(editor, "${value}", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val chooser = ChoiceBox(FXCollections.observableArrayList("lt", "eq", "gt"))
                chooser.value = text.text
                chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        text.text = " ${chooser.value} "
                        editor.editorPane.children.removeAt(index)
                        editor.editorPane.children.add(index, this.text)
                    }
                }
                editor.editorPane.children.removeAt(index)
                editor.editorPane.children.add(index, chooser)
                chooser.requestFocus()
            }
        }
    }

    override fun clone():DataOperatorField = DataOperatorField(editor, this.text.text)
}