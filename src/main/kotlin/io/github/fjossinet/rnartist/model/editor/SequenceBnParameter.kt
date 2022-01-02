package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.RNA
import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import io.github.fjossinet.rnartist.gui.editor.Script

class SequenceBnParameter(script: Script, val bnValue:DSLParameter, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLParameter(script, null, StringWithoutQuotes(script,"seq"), Operator(script,"="), StringValueWithQuotes(script,"", true), indentLevel, inFinalScript, false) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            (children.get(2) as? ParameterField)?.let { sequenceFieldValue ->
                if (sequenceFieldValue.text.text.replace("\"","").trim().isEmpty()) {
                    (bnValue.children.get(2) as? ParameterField)?.let { bnFieldValue ->
                        val bn = bnFieldValue.text.text.replace("\"", "")
                        val rna = RNA(
                            "fake",
                            (1..bn.length).map { listOf("A", "U", "G", "C").random() }.joinToString(separator = "")
                        )
                        val ss = SecondaryStructure(rna, bracketNotation = bn)
                        ss.randomizeSeq()
                        sequenceFieldValue.text.text = "\"${ss.rna.seq}\""
                    }
                }
            }
        }

    }
}