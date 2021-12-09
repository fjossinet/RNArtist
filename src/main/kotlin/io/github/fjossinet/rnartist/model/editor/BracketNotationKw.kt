package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class BracketNotationKw(val parent:SecondaryStructureKw?, editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor,  " bn", indentLevel, false) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.parent?.disableAddbuttons(true)
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor," value"), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"(((...)))", true), this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," seq"), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"your_sequence", true), this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," name"), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"A", true), this.indentLevel+1))
        } else {
            this.parent?.disableAddbuttons(false)
        }
    }

}