package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class RfamKw(val parent:SecondaryStructureKw?, editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor,  " rfam", indentLevel, false) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.parent?.disableAddbuttons(true)
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor," name"), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"AJ009730.1/1-133"), this.indentLevel+1))
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor," id"), StringWithoutQuotes(editor," = "), StringWithQuotes(editor,"RF00072"), this.indentLevel+1))
        } else {
            this.parent?.disableAddbuttons(false)
        }
    }

}