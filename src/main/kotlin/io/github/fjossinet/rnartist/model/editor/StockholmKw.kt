package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class StockholmKw(val parent:SecondaryStructureInputKw?, script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script,  " stockholm", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            //this.parent?.disableAddbuttons(true)
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"name"), Operator(script,"="), StringValueWithQuotes(script,"consensus", editable = true), this.indentLevel+1))
            this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"file"), Operator(script,"="), FileField(script), this.indentLevel+1))
            this.children.add(StockholmKw(parent, script, indentLevel))
        } else {
            //this.parent?.disableAddbuttons(false)
        }
    }

}