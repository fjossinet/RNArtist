package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.RNA
import io.github.fjossinet.rnartist.gui.editor.Script

class RnaKw(script: Script, indentLevel: Int) : DSLKeyword(script, " rna ", indentLevel) {

    init {
        this.children.add(
            1,
            OptionalDSLParameter(this,
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
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(script, "seq"),
                Operator(script, "="),
                StringValueWithQuotes(script, "", editable = true),
                this.indentLevel + 1
            )
        )
        this.children.add(
            1,
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(script, "length"),
                Operator(script, "="),
                StringWithoutQuotes(script, editable = true),
                this.indentLevel + 1
            )
        )
    }

    fun setRna(rna:RNA) {
        this.children.forEach { child ->
            (child as? OptionalDSLParameter)?.let {
                when (child.key.text.text.trim()) {
                    "name" -> {
                        (child.searchFirst { it == child.value } as StringValueWithQuotes).setText(rna.name)
                        child.addButton.fire()
                    }
                    "seq" -> {
                        (child.searchFirst { it == child.value } as StringValueWithQuotes).setText(rna.seq)
                        child.addButton.fire()
                    }
                }

            }
        }
    }

}