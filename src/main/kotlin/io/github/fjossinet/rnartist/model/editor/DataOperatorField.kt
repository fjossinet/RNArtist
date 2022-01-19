package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.ChoiceBox
import javafx.scene.paint.Color

class DataOperatorField(parent:DSLElementInt, script: Script, value:String =""): Operator(parent, script, "${value}", true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val parameterNode = this.text.parent as DSLParameter.ParameterNode
                val index = parameterNode.children.indexOf(this.text)
                val chooser = ChoiceBox(FXCollections.observableArrayList("lt", "eq", "gt"))
                chooser.value = text.text
                chooser.focusedProperty().addListener { observableValue, oldValue, newValue ->
                    if (!newValue) {
                        val parameterNode = chooser.parent as DSLParameter.ParameterNode
                        text.text = " ${chooser.value} "
                        parameterNode.children.removeAt(index)
                        parameterNode.children.add(index, this.text)
                    }
                }
                parameterNode.children.removeAt(index)
                parameterNode.children.add(index, chooser)
                chooser.requestFocus()
            }
        }
    }

    override fun clone():DataOperatorField = DataOperatorField(this.parent, script)
}