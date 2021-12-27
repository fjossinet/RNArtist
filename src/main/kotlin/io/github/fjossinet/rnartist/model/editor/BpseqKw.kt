package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor


class BpseqKw(val parent:SecondaryStructureKw?, editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor,  " bpseq", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            //this.parent?.disableAddbuttons(true)
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor,"file"), Operator(editor, "="), FileField(editor), this.indentLevel+1))
            this.children.add(BpseqKw(parent, editor, indentLevel))
        } else {
            //this.parent?.disableAddbuttons(false)
        }
    }

}