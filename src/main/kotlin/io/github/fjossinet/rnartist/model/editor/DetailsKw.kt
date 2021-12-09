package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class DetailsKw(editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor, " details", indentLevel) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," type "), StringWithoutQuotes(editor," = "), TypeField(editor,"A U"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," location "), StringWithoutQuotes(editor," = "), LocationField(editor), this.indentLevel + 1))
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor," value "), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"full"), this.indentLevel + 1))
            this.children.add(DetailsKw(editor, indentLevel))
        }
    }
}