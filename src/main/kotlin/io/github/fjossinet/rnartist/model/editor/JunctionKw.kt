package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class JunctionKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " junction", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"out_ids"), Operator(editor,"="), StringValueWithQuotes(editor,"nnw nne", editable = true), this.indentLevel + 1))
            this.children.add(1, LocationKw(editor, this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"type"), Operator(editor,"="), IntegerField(editor,3), this.indentLevel + 1))
            this.children.add(JunctionKw(editor, indentLevel))
        }
    }
}