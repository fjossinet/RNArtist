package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.gui.editor.ColorPicker
import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.event.EventHandler
import javafx.scene.paint.Color

class ColorField(script: Script, value:String = "click me to choose your color"): StringWithQuotes(script, value, true) {

    init {
        if (value.startsWith("#")) {
            this.text.text = "\"$value\""
            this.text.fill = Color.web(value)
        }
        else
            this.text.fill =  Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = script.children.indexOf(this.text)
                val colorPicker = try {
                    ColorPicker(script, Color.web(text.text.replace("\"", "")))
                } catch (e: Exception) {
                    ColorPicker(script, Color.BLANCHEDALMOND)
                }
                colorPicker.onAction = EventHandler {
                    val c = colorPicker.value
                    text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                    text.fill = c
                    script.children.removeAt(index)
                    script.children.add(index, this.text)
                }
                colorPicker.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        val c = colorPicker.value
                        text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                        text.fill = c
                        script.children.removeAt(index)
                        script.children.add(index, this.text)
                    }
                }
                script.children.removeAt(index)
                script.children.add(index, colorPicker)
                colorPicker.requestFocus()
            }
        }
    }

    override fun clone():ColorField = ColorField(script, this.text.text.replace("\"", ""))

}