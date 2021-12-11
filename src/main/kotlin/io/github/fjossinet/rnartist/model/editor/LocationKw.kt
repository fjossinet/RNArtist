package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class LocationKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " location", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add)
            this.children.add(1, OptionalDSLParameter(editor, "range", IntegerField(editor,1), Operator(editor,"to"), IntegerField(editor,10,), this.indentLevel + 1, canBeMultiple = true))
    }
}