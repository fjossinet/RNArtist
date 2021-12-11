package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class BracketNotationKw(val parent:SecondaryStructureKw?, editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor,  " bn", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.parent?.disableAddbuttons(true)
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor,"value"), Operator(editor,"="), StringValueWithQuotes(editor,"click me to set your structure", true), this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"seq"), Operator(editor,"="), StringValueWithQuotes(editor,"click me to set your sequence", true), this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"name"), Operator(editor,"="), StringValueWithQuotes(editor,"A", true), this.indentLevel+1))
        } else {
            this.parent?.disableAddbuttons(false)
        }
    }

}