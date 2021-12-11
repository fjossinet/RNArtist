package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ColorPicker
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.event.EventHandler
import javafx.scene.paint.Color

class ColorField(editor: ScriptEditor, value:String = "click me to choose your color"): StringWithQuotes(editor, value, true) {

    init {
        this.text.fill =  Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = editor.editorPane.children.indexOf(this.text)
                val colorPicker = try {
                    ColorPicker(editor, Color.web(text.text.replace("\"", "")))
                } catch (e: Exception) {
                    ColorPicker(editor, Color.BLANCHEDALMOND)
                }
                colorPicker.onAction = EventHandler {
                    val c = colorPicker.value
                    text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                    text.fill = c
                    editor.editorPane.children.removeAt(index)
                    editor.editorPane.children.add(index, this.text)
                }
                colorPicker.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        val c = colorPicker.value
                        text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                        text.fill = c
                        editor.editorPane.children.removeAt(index)
                        editor.editorPane.children.add(index, this.text)
                    }
                }
                editor.editorPane.children.removeAt(index)
                editor.editorPane.children.add(index, colorPicker)
                colorPicker.requestFocus()
            }
        }
    }

    override fun clone():ColorField = ColorField(editor, this.text.text.replace("\"", ""))

}