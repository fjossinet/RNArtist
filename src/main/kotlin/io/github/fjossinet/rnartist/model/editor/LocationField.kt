package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.Button
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.HBox
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler
import javafx.scene.input.KeyCode
import org.kordamp.ikonli.javafx.FontIcon

class LocationField(editor: ScriptEditor, value:String = "start:length,start:length", editable: Boolean = true): StringWithQuotes(editor, value, editable) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val textField = TextField(editor, this.text)
                textField.onKeyPressed = EventHandler {
                    if (it.code == KeyCode.ENTER) {
                        this.text.text = "\"${textField.text.trim()}\""
                        editor.editorPane.children.removeAt(index)
                        editor.editorPane.children.add(index, this.text)
                    }
                }

                val getSelection = Button(editor, null, FontIcon("fas-eye-dropper:15"))

                val hbox = HBox()
                hbox.children.addAll(textField, getSelection)

                editor.editorPane.children.removeAt(index)
                editor.editorPane.children.add(index, hbox)
            }
        }
    }

}