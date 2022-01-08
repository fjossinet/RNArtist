package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color
import java.util.*

class FloatField(script: Script, value:String = ""): ParameterField(script, value, true) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = script.children.indexOf(this.text)
                val textField = TextField(script, this.text)
                textField.prefWidth = script.width
                textField.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        this.text.text = if (textField.text.trim().length > 0 && !textField.text.trim().contains(Regex("\\."))) "${textField.text.trim()}.0" else textField.text.trim()
                        script.children.removeAt(index)
                        script.children.add(index, this.text)
                        script.initScript() //necessary if the new text is empty, then will be replaced witth the datafield
                    }
                }
                script.children.removeAt(index)
                script.children.add(index, textField)
                textField.requestFocus()
            }
        }
    }

    override fun clone(): FloatField = FloatField(script, this.text.text)

    override fun dumpNodes(nodes: MutableList<Node>, enterInCollapsedNode: Boolean) {
        if (editable) {
            if (this.text.text.isEmpty()) {
                val button = DataField(script)
                button.onMouseClicked = EventHandler {
                    val index = script.children.indexOf(button)
                    val textField = TextField(script, this.text)
                    textField.prefWidth = script.width
                    textField.focusedProperty().addListener { observableValue, oldValue, newValue ->
                        if (!newValue) {
                            this.text.text = if (textField.text.trim().length > 0 && !textField.text.trim().contains(Regex("\\."))) "${textField.text.trim()}.0" else textField.text.trim()
                            script.children.removeAt(index)
                            script.children.add(index, this.text)
                            script.initScript()
                        }
                    }
                    script.children.removeAt(index)
                    script.children.add(index, textField)
                    textField.requestFocus()
                }
                nodes.add(button)
            } else
                nodes.add(this.text)
        } else
            super.dumpNodes(nodes, enterInCollapsedNode)
    }

}