package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.RNA
import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import io.github.fjossinet.rnartist.gui.editor.Script

class SequenceBnParameter(parent:DSLElement, script: Script, val bnValue: DSLParameter, indentLevel: Int, inFinalScript: Boolean = false, sequence:String = "") :
    OptionalDSLParameter(parent,
        script,
        null,
        StringWithoutQuotes(parent, script, "seq"),
        Operator(parent, script, "="),
        StringValueWithQuotes(parent, script, sequence, true),
        indentLevel,
        inFinalScript
    ) {

    init {
        var sequence = ""
        script.mediator.drawingDisplayed.get()?.let {
            sequence = it.drawing.secondaryStructure.rna.seq
        } ?: run {
            (bnValue.children.get(2) as? ParameterField)?.let { bnFieldValue ->
                val bn = bnFieldValue.text.text.replace("\"", "")
                if (bn.matches(Regex("^[\\.\\(\\)\\{\\}\\[\\]A-Za-z\\-]+$"))) {
                    val rna = RNA(
                        "fake",
                        (1..bn.length).map { listOf("A", "U", "G", "C").random() }.joinToString(separator = "")
                    )
                    val ss = SecondaryStructure(rna, bracketNotation = bn)
                    ss.randomizeSeq()
                    sequence = ss.rna.seq
                }
            }
        }
        (children.get(2) as? ParameterField)?.let { sequenceFieldValue ->
            sequenceFieldValue.text.text = "\"${sequence}\""
        }
    }
}