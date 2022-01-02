package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class BracketNotationKw(val parent:SecondaryStructureInputKw?, script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script,  " bn", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            //this.parent?.disableAddbuttons(true)
            var bracketNotation = "click me to set your structure"
            script.mediator.drawingDisplayed.get()?.let {
                bracketNotation = it.drawing.secondaryStructure.toBracketNotation()
            }
            val bnParameter = DSLParameter(script, StringWithoutQuotes(script,"value"), Operator(script,"="), StringValueWithQuotes(script, bracketNotation, true), this.indentLevel+1)
            this.children.add(1, bnParameter)

            var sequence = "click me to set your sequence"
            var inFinalScript = false
            script.mediator.drawingDisplayed.get()?.let {
                sequence = it.drawing.secondaryStructure.rna.seq
                inFinalScript = true
            }
            val seq = SequenceBnParameter(script, bnParameter, this.indentLevel+1, inFinalScript, sequence)
            this.children.add(1, seq)
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"name"), Operator(script,"="), StringValueWithQuotes(script,"A", true), this.indentLevel+1))
            this.children.add(BracketNotationKw(parent, script, indentLevel))
        } else {
            //this.parent?.disableAddbuttons(false)
        }
    }

}