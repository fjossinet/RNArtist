package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ColorSchemeChooser
import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color

class ColorSchemeField(parent:DSLElementInt, script: Script, value:String = ""): ParameterField(parent, script, "\"${value}\"", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        this.text.onMouseClicked = EventHandler {
            val parameterNode = this.text.parent as DSLParameter.ParameterNode
            val index = parameterNode.children.indexOf(this.text)
            val chooser = ColorSchemeChooser(script, this.text)
            chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                if (!newValue) {
                    val parameterNode = chooser.parent as DSLParameter.ParameterNode
                    this.text.text = "\"${chooser.getSelection()}\""
                    parameterNode.children.removeAt(index)
                    parameterNode.children.add(index, this.text)
                    script.initScript() //necessary if the new text is empty, then will be replaced with the datafield
                }
            }
            parameterNode.children.removeAt(index)
            parameterNode.children.add(index, chooser)
            chooser.requestFocus()
        }
    }

    override fun clone():TypeField = TypeField(this.parent, script)

    override fun dumpNodes(nodes: MutableList<Node>) {
        if (this.text.text.replace("\"", "").isEmpty()) {
            val button = DataField(script)
            button.onMouseClicked = EventHandler {
                val parameterNode = button.parent as DSLParameter.ParameterNode
                val index = parameterNode.children.indexOf(button)
                val chooser = ColorSchemeChooser(script, this.text)
                chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        this.text.text = "\"${chooser.getSelection()}\""
                        parameterNode.children.removeAt(index)
                        parameterNode.children.add(index, this.text)
                        script.initScript()
                    }
                }
                parameterNode.children.removeAt(index)
                parameterNode.children.add(index, chooser)
                chooser.requestFocus()
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