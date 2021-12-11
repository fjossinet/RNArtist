package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler
import javafx.scene.control.Spinner
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color

class IntegerField(editor: ScriptEditor, value:Int, val min:Int = 1, val max:Int = Integer.MAX_VALUE): ParameterField(editor, "${value}", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val textField = TextField(editor, this.text)
                textField.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        this.text.text = "${textField.text.trim()}"
                        editor.editorPane.children.removeAt(index)
                        editor.editorPane.children.add(index, this.text)
                    }
                }
                editor.editorPane.children.removeAt(index)
                editor.editorPane.children.add(index, textField)
                textField.requestFocus()
            }
        }
    }

    override fun clone(): IntegerField = IntegerField(editor, text.text.toInt(), this.min, this.max)
}