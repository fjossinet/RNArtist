package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler
import javafx.scene.input.KeyCode

class FloatField(editor: ScriptEditor, value:String, editable: Boolean = true): ParameterField(editor, "${value}", editable) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val textField = TextField(editor, this.text)
                textField.onKeyPressed = EventHandler {
                    if (it.code == KeyCode.ENTER) {
                        this.text.text = "${textField.text.trim()}"
                        editor.editorPane.children.removeAt(index)
                        editor.editorPane.children.add(index, this.text)
                    }
                }
                editor.editorPane.children.removeAt(index)
                editor.editorPane.children.add(index, textField)
            }
        }
    }

}