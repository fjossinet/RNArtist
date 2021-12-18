package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class ShowKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " show", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"data"), Operator(editor,"gt", editable = true), FloatField(editor,"20.7"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"type"), Operator(editor,"="), TypeField(editor,"click me"), this.indentLevel + 1))
            this.children.add(1, LocationKw(editor, this.indentLevel + 1))
            this.children.add(ShowKw(editor, indentLevel))
        }
    }
}