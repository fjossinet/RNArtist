package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class BracketNotationKw(parent:SecondaryStructureInputKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script,  "bn", indentLevel) {

    init {
        var bracketNotation = ""
        script.mediator.drawingDisplayed.get()?.let {
            bracketNotation = it.drawing.secondaryStructure.toBracketNotation()
        }
        val bnParameter = DSLParameter(this, script, StringWithoutQuotes(this, script,"value"), Operator(this, script,"="), StringValueWithQuotes(this, script, bracketNotation, true), this.indentLevel+1)
        this.children.add(bnParameter)

        var sequence = ""
        var inFinalScript = false
        script.mediator.drawingDisplayed.get()?.let {
            sequence = it.drawing.secondaryStructure.rna.seq
            inFinalScript = true
        }
        val seq = SequenceBnParameter(this, script, bnParameter, this.indentLevel+1, inFinalScript, sequence)
        this.children.add(seq)
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"name"), Operator(this, script,"="), StringValueWithQuotes(this, script,"A", true), this.indentLevel+1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            script.mediator.drawingDisplayed.get()?.let {
                val bnParameter = searchFirst { it is DSLParameter && "value".equals(it.key.text.text.trim()) } as DSLParameter
                bnParameter.value.text.text = "\"${it.drawing.secondaryStructure.toBracketNotation()}\""

                val seqParameter = searchFirst { it is SequenceBnParameter } as SequenceBnParameter
                seqParameter.value.text.text = "\"${it.drawing.secondaryStructure.rna.seq}\""

                val nameParameter = searchFirst { it is OptionalDSLParameter && "name".equals(it.key.text.text.trim()) } as OptionalDSLParameter
                nameParameter.value.text.text = "\"${it.drawing.secondaryStructure.rna.name}\""

            }
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is BracketNotationKw)
                this.parent.children.add(this.parent.children.indexOf(this)+1, BracketNotationKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this)+1)
            if (childAfter is BracketNotationKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is BracketNotationKw)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

}