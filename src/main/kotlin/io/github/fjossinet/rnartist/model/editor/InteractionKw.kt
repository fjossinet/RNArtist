package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class InteractionKw(script: Script, indentLevel:Int): OptionalDSLKeyword(script, " interaction", indentLevel) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "orientation") , operator = Operator(script, " = "), StringWithQuotes(script, "cis", editable = true),  this.indentLevel + 1))
            this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "pos2") , operator = Operator(script, " = "), IntegerField(script, 1),  this.indentLevel + 1))
            this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "edge2") , operator = Operator(script, " = "), StringWithQuotes(script, "wc", editable = true),  this.indentLevel + 1))
            this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "pos1") , operator = Operator(script, " = "), IntegerField(script, 1),  this.indentLevel + 1))
            this.children.add(1, DSLParameter(script, key = StringWithoutQuotes(script, "edge1") , operator = Operator(script, " = "), StringWithQuotes(script, "wc", editable = true),  this.indentLevel + 1))
            this.children.add(InteractionKw(script, this.indentLevel))
        }
    }

}