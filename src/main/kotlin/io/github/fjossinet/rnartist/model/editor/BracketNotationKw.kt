package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class BracketNotationKw(val parent:SecondaryStructureInputKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script,  " bn", indentLevel) {

    init {
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
        val seq = SequenceBnParameter(this, script, bnParameter, this.indentLevel+1, inFinalScript, sequence)
        this.children.add(1, seq)
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"name"), Operator(script,"="), StringValueWithQuotes(script,"A", true), this.indentLevel+1))

        addButton.onAction = EventHandler {
            this.inFinalScript = true

            this.parent.children.add(this.parent.children.indexOf(this)+1, BracketNotationKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this)+1)
            if (childAfter is BracketNotationKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is BracketNotationKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

}