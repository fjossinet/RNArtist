package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class SVGKw (editor: ScriptEditor, indentLevel:Int): OptionalDSLKeyword(editor,  " svg", indentLevel, false) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"width"), Operator(editor,"="), FloatField(editor,"800.0"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"height"), Operator(editor,"="), FloatField(editor,"800.0"), this.indentLevel + 1))
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor,"path"), Operator(editor,"="), DirectoryField(editor), this.indentLevel+1))
        } else {
        }
    }

}