package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.Script

abstract class DSLKeyword(script: Script, text:String, indentLevel:Int): DSLElement(script, text, indentLevel) {
    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        this.children.add(OpenedCurly(script))
        this.children.add(ClosedCurly(script, indentLevel))
    }

    override fun increaseIndentLevel() {
        this.indentLevel ++
        super.increaseIndentLevel()
    }

    override fun decreaseIndentLevel() {
        this.indentLevel--
        super.decreaseIndentLevel()
    }
}