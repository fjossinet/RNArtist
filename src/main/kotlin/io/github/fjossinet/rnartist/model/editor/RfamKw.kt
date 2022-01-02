package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class RfamKw(val parent:SecondaryStructureInputKw?, script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script,  " rfam", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            //this.parent?.disableAddbuttons(true)
            this.children.add(1, OptionalDSLParameter(script, "numbering", StringWithoutQuotes(script,"use"), Operator(script,"alignment"), StringWithoutQuotes(script,"numbering"), this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"name"), Operator(script,"="), StringValueWithQuotes(script,"consensus", editable = true), this.indentLevel+1, inFinalScript = true))
            this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"id"), Operator(script,"="), StringValueWithQuotes(script,"RF00072", editable = true), this.indentLevel+1))
            this.children.add(RfamKw(parent, script, indentLevel))
        } else {
            //this.parent?.disableAddbuttons(false)
        }
    }

}