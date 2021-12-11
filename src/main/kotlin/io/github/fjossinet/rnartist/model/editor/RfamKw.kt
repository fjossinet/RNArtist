package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class RfamKw(val parent:SecondaryStructureKw?, editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor,  " rfam", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.parent?.disableAddbuttons(true)
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"name"), Operator(editor,"="), StringValueWithQuotes(editor,"AJ009730.1/1-133", editable = true), this.indentLevel+1, inFinalScript = true))
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor,"id"), Operator(editor,"="), StringValueWithQuotes(editor,"RF00072"), this.indentLevel+1))
        } else {
            this.parent?.disableAddbuttons(false)
        }
    }

}