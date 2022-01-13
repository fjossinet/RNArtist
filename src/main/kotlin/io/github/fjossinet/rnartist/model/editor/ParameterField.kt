package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.KeyCode

abstract open class ParameterField(parent: DSLElementInt, script: Script, value:String, var editable:Boolean = false): DSLElement(parent, script, value, 0) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = script.children.indexOf(this.text)
                val textField = TextField(script, this.text)
                textField.onKeyPressed = EventHandler {
                    if (it.code == KeyCode.ENTER) {
                        this.text.text = "\"${textField.text.trim()}\""
                        script.children.removeAt(index)
                        script.children.add(index, this.text)
                    }
                }
                script.children.removeAt(index)
                script.children.add(index, textField)
            }
        }
    }

    abstract fun clone():ParameterField

}