package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class BpseqKw(val parent:SecondaryStructureInputKw?, script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script,  " bpseq", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            //this.parent?.disableAddbuttons(true)
            this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"file"), Operator(script, "="), FileField(script), this.indentLevel+1))
            this.children.add(BpseqKw(parent, script, indentLevel))
        } else {
            //this.parent?.disableAddbuttons(false)
        }
    }

}