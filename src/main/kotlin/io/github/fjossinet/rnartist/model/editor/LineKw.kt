package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class LineKw(editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor, " line", indentLevel) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," value "), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"2.0"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," type "), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"phosphodiester_bond N"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," location "), StringWithoutQuotes(editor," = "), LocationField(editor), this.indentLevel + 1))
            this.children.add(LineKw(editor, indentLevel))
        }
    }
}