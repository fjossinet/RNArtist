package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler
import javafx.scene.Node

class FloatField(parent:DSLElementInt, script: Script, value:String = ""): ParameterField(parent, script, value, true) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val parameterNode = this.text.parent as DSLParameter.ParameterNode
                val index = parameterNode.children.indexOf(this.text)
                val textField = TextField(script, this.text)
                textField.prefWidth = script.width
                textField.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        this.text.text = if (textField.text.trim().length > 0 && !textField.text.trim().contains(Regex("\\."))) "${textField.text.trim()}.0" else textField.text.trim()
                        parameterNode.children.removeAt(index)
                        parameterNode.children.add(index, this.text)
                        script.initScript() //necessary if the new text is empty, then will be replaced witth the datafield
                    }
                }
                parameterNode.children.removeAt(index)
                parameterNode.children.add(index, textField)
                textField.requestFocus()
            }
        }
    }

    override fun clone(): FloatField = FloatField(this.parent, script)

    override fun dumpNodes(nodes: MutableList<Node>) {
        if (editable) {
            if (this.text.text.isEmpty()) {
                val button = DataField(script)
                button.onMouseClicked = EventHandler {
                    val parameterNode = button.parent as DSLParameter.ParameterNode
                    val index = parameterNode.children.indexOf(button)
                    val textField = TextField(script, this.text)
                    textField.prefWidth = script.width
                    textField.focusedProperty().addListener { observableValue, oldValue, newValue ->
                        if (!newValue) {
                            val index = parameterNode.children.indexOf(textField)
                            this.text.text = if (textField.text.trim().length > 0 && !textField.text.trim().contains(Regex("\\."))) "${textField.text.trim()}.0" else textField.text.trim()
                            parameterNode.children.removeAt(index)
                            parameterNode.children.add(index, this.text)
                            script.initScript()
                        }
                    }
                    parameterNode.children.removeAt(index)
                    parameterNode.children.add(index, textField)
                    textField.requestFocus()
                }
                nodes.add(button)
            } else
                nodes.add(this.text)
        } else
            super.dumpNodes(nodes)
    }

    override fun dumpText(text:StringBuilder) {
        if (this.text.text.isNotEmpty())
            text.append(this.text.text)
    }

}