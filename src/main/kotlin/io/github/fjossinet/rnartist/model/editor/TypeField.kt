package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.TypeChooser
import javafx.event.EventHandler
import javafx.scene.paint.Color

class TypeField(editor: ScriptEditor, value:String): ParameterField(editor, "\"${value}\"", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val chooser = TypeChooser(editor, this.text)
                chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        this.text.text = "\"${chooser.getSelection()}\""
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

    override fun clone():TypeField = TypeField(editor, this.text.text.replace("\"",""))
}