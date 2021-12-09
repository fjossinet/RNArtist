package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class HideKw(editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor, " hide", indentLevel) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," data "), StringWithoutQuotes(editor," gt ", editable = true), FloatField(editor,"20.7"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," type "), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"A U"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," location "), StringWithoutQuotes(editor," = "), LocationField(editor), this.indentLevel + 1))
            this.children.add(HideKw(editor, indentLevel))
        }
    }
}