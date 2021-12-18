package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class ThemeKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " theme", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, LineKw(editor, this.indentLevel+1))
            this.children.add(1, HideKw(editor, this.indentLevel+1))
            this.children.add(1, ShowKw(editor, this.indentLevel+1))
            this.children.add(1, ColorKw(editor, this.indentLevel+1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"details_lvl"), Operator(editor,"="), IntegerField(editor,5, 1, 5), this.indentLevel + 1, inFinalScript = true))
        }
    }
}