package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.RnartistConfig
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

open class DSLParameter(editor: ScriptEditor, var key:ParameterField, var operator:Operator, var value:ParameterField, indentLevel:Int): DSLElement(editor, " ", indentLevel) {

    init {
        this.children.add(key)
        key.color = awtColorToJavaFX(RnartistConfig.keyParamEditorColor)
        this.children.add(operator)
        operator.color = awtColorToJavaFX(RnartistConfig.operatorParamEditorColor)
        this.children.add(value)
        value.color = awtColorToJavaFX(RnartistConfig.valueParamEditorColor)
        this.children.add(DSLElement(editor,  "\n" ,  0))
    }

}