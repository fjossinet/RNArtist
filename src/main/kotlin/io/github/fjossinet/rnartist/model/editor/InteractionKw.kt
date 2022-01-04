package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class InteractionKw(var parent:DSLElement, script: Script, indentLevel:Int): OptionalDSLKeyword(script, " interaction", indentLevel) {

    init {
        this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "orientation") , operator = Operator(script, " = "), StringWithQuotes(script, "cis", editable = true),  this.indentLevel + 1))
        this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "pos2") , operator = Operator(script, " = "), IntegerField(script, 1),  this.indentLevel + 1))
        this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "edge2") , operator = Operator(script, " = "), StringWithQuotes(script, "wc", editable = true),  this.indentLevel + 1))
        this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "pos1") , operator = Operator(script, " = "), IntegerField(script, 1),  this.indentLevel + 1))
        this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "edge1") , operator = Operator(script, " = "), StringWithQuotes(script, "wc", editable = true),  this.indentLevel + 1))

        addButton.onAction = EventHandler {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is InteractionKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, InteractionKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is InteractionKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is InteractionKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }

    }

}