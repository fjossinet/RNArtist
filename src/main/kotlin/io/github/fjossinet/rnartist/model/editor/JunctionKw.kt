package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class JunctionKw(editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor, " junction", indentLevel) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithQuotes(editor," out_ids "), StringWithQuotes(editor,"= "), StringWithQuotes(editor,"nnw nne"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithQuotes(editor," type "), StringWithQuotes(editor,"= "), IntegerField(editor,3), this.indentLevel + 1))
            this.children.add(JunctionKw(editor, indentLevel))
        }
    }
}