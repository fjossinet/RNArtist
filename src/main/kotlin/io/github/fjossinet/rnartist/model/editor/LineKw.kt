package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class LineKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " line", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor,"value"), Operator(editor,"="), FloatField(editor,"2.0"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"type"), Operator(editor,"="), TypeField(editor,"click me"), this.indentLevel + 1))
            this.children.add(1, LocationKw(editor, this.indentLevel + 1))
            this.children.add(LineKw(editor, indentLevel))
        }
    }
}