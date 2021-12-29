package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class PDBKw (val parent: SecondaryStructureKw?, editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor,  " pdb", indentLevel, inFinalScript) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            //this.parent?.disableAddbuttons(true)
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"file"), Operator(editor,"="), FileField(editor), this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"name"), Operator(editor,"="), StringValueWithQuotes(editor,"consensus", editable = true), this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"id"), Operator(editor,"="), StringValueWithQuotes(editor,"1EHZ", editable = true), this.indentLevel+1))
            this.children.add(RfamKw(parent, editor, indentLevel))
        } else {
            //this.parent?.disableAddbuttons(false)
        }
    }

}