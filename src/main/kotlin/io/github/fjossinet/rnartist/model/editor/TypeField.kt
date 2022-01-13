package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TextField
import io.github.fjossinet.rnartist.gui.editor.TypeChooser
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color

class TypeField(parent:DSLElementInt, script: Script, value:String = "", val restrictedTypes:List<String>? = null): ParameterField(parent, script, "\"${value}\"", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        this.text.onMouseClicked = EventHandler {
            val index = script.children.indexOf(this.text)
            val chooser = TypeChooser(script, this.text, restrictedTypes)
            chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                if (!newValue) {
                    this.text.text = "\"${chooser.getSelection()}\""
                    script.children.removeAt(index)
                    script.children.add(index, this.text)
                    script.initScript() //necessary if the new text is empty, then will be replaced witth the datafield
                }
            }
            script.children.removeAt(index)
            script.children.add(index, chooser)
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
                val chooser = TypeChooser(script, this.text, restrictedTypes)
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