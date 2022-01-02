package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.Script

open class DSLParameter(script: Script, var key:ParameterField, var operator:Operator, var value:ParameterField, indentLevel:Int): DSLElement(script, " ", indentLevel) {

    init {
        this.children.add(key)
        key.color = awtColorToJavaFX(RnartistConfig.keyParamEditorColor)
        this.children.add(operator)
        operator.color = awtColorToJavaFX(RnartistConfig.operatorParamEditorColor)
        this.children.add(value)
        value.color = awtColorToJavaFX(RnartistConfig.valueParamEditorColor)
        this.children.add(NewLine(script))
    }

    override fun increaseIndentLevel() {
        this.key.indentLevel ++
        super.increaseIndentLevel()
    }

    override fun decreaseIndentLevel() {
        this.key.indentLevel --
        super.decreaseIndentLevel()
    }

}