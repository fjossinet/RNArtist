package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.RNA
import io.github.fjossinet.rnartist.gui.editor.Script

class RnaKw(script: Script, indentLevel: Int, var rna: RNA? = null) : DSLKeyword(script, " rna", indentLevel) {

    init {
        this.rna?.let { rna ->
            this.children.add(
                1,
                OptionalDSLParameter(
                    script,
                    null,
                    StringWithoutQuotes(script, "name"),
                    Operator(script, "="),
                    StringValueWithQuotes(script, rna.name, editable = true),
                    this.indentLevel + 1,
                    inFinalScript = true
                )
            )
            this.children.add(
                1,
                OptionalDSLParameter(
                    script,
                    null,
                    StringWithoutQuotes(script, "seq"),
                    Operator(script, "="),
                    StringValueWithQuotes(script, rna.seq, editable = true),
                    this.indentLevel + 1,
                    inFinalScript = true
                )
            )
            this.children.add(
                1,
                OptionalDSLParameter(
                    script,
                    null,
                    StringWithoutQuotes(script, "length"),
                    Operator(script, "="),
                    IntegerField(script, 100),
                    this.indentLevel + 1
                )
            )

        } ?: run {
            this.children.add(
                1,
                OptionalDSLParameter(
                    script,
                    null,
                    StringWithoutQuotes(script, "name"),
                    Operator(script, "="),
                    StringValueWithQuotes(script, "my RNA", editable = true),
                    this.indentLevel + 1
                )
            )
            this.children.add(
                1,
                OptionalDSLParameter(
                    script,
                    null,
                    StringWithoutQuotes(script, "seq"),
                    Operator(script, "="),
                    StringValueWithQuotes(script, "click me to set your sequence", editable = true),
                    this.indentLevel + 1
                )
            )
            this.children.add(
                1,
                OptionalDSLParameter(
                    script,
                    null,
                    StringWithoutQuotes(script, "length"),
                    Operator(script, "="),
                    IntegerField(script, 100),
                    this.indentLevel + 1
                )
            )
        }
    }

}