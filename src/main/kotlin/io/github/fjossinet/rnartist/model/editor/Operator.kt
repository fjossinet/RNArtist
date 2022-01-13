package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.gui.editor.TextField
import javafx.event.EventHandler
import javafx.scene.input.KeyCode

open class Operator(parent:DSLElementInt, script: Script, operator:String, editable: Boolean = false): StringWithoutQuotes(parent, script, " ${operator.trim()} ", editable) {

    init {
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val index = script.children.indexOf(this.text)
                val textField = TextField(script, this.text)
                textField.onKeyPressed = EventHandler {
                    if (it.code == KeyCode.ENTER) {
                        this.text.text = " ${textField.text.trim()} "
                        script.children.removeAt(index)
                        script.children.add(index, this.text)
                    }
                }
                script.children.removeAt(index)
                script.children.add(index, textField)
            }
        }
    }

    override fun clone():Operator = Operator(this.parent, script, this.text.text, this.editable)
}