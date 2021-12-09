package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

open class DSLParameter(editor: ScriptEditor, var field:ParameterField, var operator:ParameterField = StringWithQuotes(editor, "= "), var value:ParameterField, indentLevel:Int): DSLElement(editor, "", indentLevel) {

    init {
        this.children.add(field)
        this.children.add(operator)
        this.children.add(value)
        this.children.add(DSLElement(editor,  "\n" ,  0))
    }



}