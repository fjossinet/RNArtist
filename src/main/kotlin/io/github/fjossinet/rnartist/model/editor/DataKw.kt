package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class DataKw(editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor, " data", indentLevel) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add)
            this.children.add(1, OptionalDSLParameter(editor, "value", StringWithQuotes(editor,"1", editable = true), StringWithoutQuotes(editor," to "), FloatField(editor,"200.7"), this.indentLevel + 1, canBeMultiple = true))
    }
}