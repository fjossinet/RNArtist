package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class InteractionKw(parent:DSLElement, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "interaction", indentLevel) {

    init {
        this.children.add(DSLParameter(this, script, key = StringWithoutQuotes(this, script, "pos1") , operator = Operator(this, script, " = "), StringWithoutQuotes(this, script, editable = true),  this.indentLevel + 1))
        this.children.add(DSLParameter(this, script, key = StringWithoutQuotes(this, script, "edge1") , operator = Operator(this, script, " = "), StringWithQuotes(this, script, "wc", editable = true),  this.indentLevel + 1))
        this.children.add(DSLParameter(this, script, key = StringWithoutQuotes(this, script, "pos2") , operator = Operator(this, script, " = "), StringWithoutQuotes(this, script, editable = true),  this.indentLevel + 1))
        this.children.add(DSLParameter(this, script, key = StringWithoutQuotes(this, script, "edge2") , operator = Operator(this, script, " = "), StringWithQuotes(this, script, value = "wc", editable = true),  this.indentLevel + 1))
        this.children.add(DSLParameter(this, script, key = StringWithoutQuotes(this, script, "orientation") , operator = Operator(this, script, " = "), StringWithQuotes(this, script, value = "cis", editable = true),  this.indentLevel + 1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is InteractionKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, InteractionKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            if (this.parent.children.indexOf(this) <this.parent.children.size-1 && this.parent.children.get(this.parent.children.indexOf(this) + 1) is InteractionKw)
                this.parent.children.remove(this)
            else if (this.parent.children.indexOf(this) > 0) {
                val previous = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (previous is InteractionKw && !previous.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }

    }

}