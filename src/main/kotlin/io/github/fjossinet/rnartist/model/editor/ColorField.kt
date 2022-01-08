package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.gui.editor.ColorPicker
import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TypeChooser
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color

class ColorField(script: Script, value:String = ""): StringWithQuotes(script, value, true) {

    init {
        if (value.startsWith("#")) {
            this.text.text = "\"$value\""
            this.text.fill = Color.web(value)
        }
        else
            this.text.fill =  Color.web("#4d4d4d")
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

    override fun clone():ColorField = ColorField(script, this.text.text.replace("\"", ""))

    override fun dumpNodes(nodes: MutableList<Node>, enterInCollapsedNode: Boolean) {
        if (this.text.text.replace("\"", "").isEmpty()) {
            val button = DataField(script)
            button.onMouseClicked = EventHandler {
                val index = script.children.indexOf(button)
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
                        script.initScript()
                    }
                }
                script.children.removeAt(index)
                script.children.add(index, colorPicker)
                colorPicker.requestFocus()
            }
            nodes.add(button)
        } else
            nodes.add(this.text)
    }
}