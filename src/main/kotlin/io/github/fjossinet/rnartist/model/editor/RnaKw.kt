package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.RNA
import io.github.fjossinet.rnartist.gui.editor.Script

class RnaKw(parent:PartsKw, script: Script, indentLevel: Int) : DSLKeyword(parent, script, "rna", indentLevel) {

    init {
        this.children.add(
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(this, script, "name"),
                Operator(this, script, "="),
                StringValueWithQuotes(this, script, "my RNA", editable = true),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(this, script, "seq"),
                Operator(this, script, "="),
                StringValueWithQuotes(this, script, "", editable = true),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(this, script, "length"),
                Operator(this, script, "="),
                StringWithoutQuotes(this, script, editable = true),
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