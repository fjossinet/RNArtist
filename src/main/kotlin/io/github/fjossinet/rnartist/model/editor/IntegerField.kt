package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.event.EventHandler
import javafx.scene.control.Spinner
import javafx.scene.input.KeyCode

class IntegerField(editor: ScriptEditor, value:Int, val min:Int = 1, val max:Int = Integer.MAX_VALUE, editable: Boolean = true): ParameterField(editor, "${value}", editable) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val spinner = Spinner<Integer>(min, max, value)
                spinner.onKeyPressed = EventHandler {
                    if (it.code == KeyCode.ENTER) {
                        text.text = spinner.value.toString()
                        editor.editorPane.children.removeAt(index)
                        editor.editorPane.children.add(index, this.text)
                    }
                }
                editor.editorPane.children.removeAt(index)
                editor.editorPane.children.add(index, spinner)
            }
        }
    }

}