package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class BpseqKw(val parent:SecondaryStructureKw?, editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor,  " bpseq", indentLevel, false) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.parent?.disableAddbuttons(true)
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor," file"), StringWithoutQuotes(editor, " = "), FileField(editor), this.indentLevel+1))
        } else {
            this.parent?.disableAddbuttons(false)
        }
    }

}