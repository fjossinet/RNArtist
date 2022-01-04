package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class DataKw(script: Script, indentLevel:Int): OptionalDSLKeyword(script, " data", indentLevel) {

    init {
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"file"), Operator(script,"="), FileField(script), this.indentLevel+1))
        this.children.add(
            1,
            OptionalDSLParameter(this,
                script,
                "value",
                StringWithQuotes(script, "1", editable = true),
                Operator(script, "to"),
                FloatField(script, "200.7"),
                this.indentLevel + 1,
                canBeMultiple = true
            )
        )
    }
}