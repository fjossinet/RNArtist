package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.TypeChooser
import javafx.event.EventHandler
import javafx.scene.input.KeyCode

class TypeField(editor: ScriptEditor, value:String, editable: Boolean = true): ParameterField(editor, "\"${value}\"", editable) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val chooser = TypeChooser(editor, this.text)
                chooser.onKeyPressed = EventHandler {
                    if (it.code == KeyCode.ENTER) {
                        this.text.text = "\"${chooser.getSelection()}\""
                        editor.editorPane.children.removeAt(index)
                        editor.editorPane.children.add(index, this.text)
                    }
                    editor.editorPane.children.removeAt(index)
                    editor.editorPane.children.add(index, this.text)
                }
                editor.editorPane.children.removeAt(index)
                editor.editorPane.children.add(index, chooser)
            }
        }
    }

}