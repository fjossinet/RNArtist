package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.gui.editor.ColorPicker
import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color

class ColorField(parent :DSLElementInt, script: Script, value:String = ""): StringWithQuotes(parent, script, value, true) {

    init {
        if (value.startsWith("#")) {
            this.text.text = "\"$value\""
            this.text.fill = Color.web(value)
        }
        else
            this.text.fill =  Color.web("#4d4d4d")
            this.text.onMouseClicked = EventHandler {
                val parameterNode = this.text.parent as DSLParameter.ParameterNode
                val index = parameterNode.children.indexOf(this.text)
                val colorPicker = try {
                    ColorPicker(script, Color.web(text.text.replace("\"", "")))
                } catch (e: Exception) {
                    ColorPicker(script, Color.BLANCHEDALMOND)
                }
                colorPicker.onAction = EventHandler {
                    val parameterNode = colorPicker.parent as DSLParameter.ParameterNode
                    val c = colorPicker.value
                    text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                    text.fill = c
                    parameterNode.children.removeAt(index)
                    parameterNode.children.add(index, this.text)
                }
                colorPicker.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        val parameterNode = colorPicker.parent as DSLParameter.ParameterNode
                        val c = colorPicker.value
                        text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                        text.fill = c
                        parameterNode.children.removeAt(index)
                        parameterNode.children.add(index, this.text)
                    }
                }
                parameterNode.children.removeAt(index)
                parameterNode.children.add(index, colorPicker)
                colorPicker.requestFocus()
            }

    }

    override fun clone():ColorField = ColorField(parent, script)

    override fun dumpNodes(nodes: MutableList<Node>) {
        if (this.text.text.replace("\"", "").isEmpty()) {
            val button = DataField(script)
            button.onMouseClicked = EventHandler {
                val parameterNode = button.parent as DSLParameter.ParameterNode
                val index = parameterNode.children.indexOf(button)
                val colorPicker = try {
                    ColorPicker(script, Color.web(text.text.replace("\"", "")))
                } catch (e: Exception) {
                    ColorPicker(script, Color.BLANCHEDALMOND)
                }
                colorPicker.onAction = EventHandler {
                    val parameterNode = colorPicker.parent as DSLParameter.ParameterNode
                    val c = colorPicker.value
                    text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                    text.fill = c
                    parameterNode.children.removeAt(index)
                    parameterNode.children.add(index, this.text)
                }
                colorPicker.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        val parameterNode = colorPicker.parent as DSLParameter.ParameterNode
                        val c = colorPicker.value
                        text.text = "\"${getHTMLColorString(javaFXToAwt(c))}\""
                        text.fill = c
                        parameterNode.children.removeAt(index)
                        parameterNode.children.add(index, this.text)
                        script.initScript()
                    }
                }
                parameterNode.children.removeAt(index)
                parameterNode.children.add(index, colorPicker)
                colorPicker.requestFocus()
            }
            nodes.add(button)
        } else
            nodes.add(this.text)
    }

    override fun dumpText(text:StringBuilder) {
        if (this.text.text.replace("\"", "").isNotEmpty())
            text.append(this.text.text)
    }
}