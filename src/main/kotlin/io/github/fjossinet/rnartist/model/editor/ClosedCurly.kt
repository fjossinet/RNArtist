package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

class ClosedCurly(parent:DSLKeyword, script: Script, indentLevel:Int): DSLElement(parent, script, "}${System.lineSeparator()}", indentLevel) {
    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.bracesEditorColor)
        this.text.font = Font.font(RnartistConfig.editorFontName, FontWeight.BOLD, RnartistConfig.editorFontSize.toDouble())
    }

    override fun increaseIndentLevel() {
        this.indentLevel ++
    }

    override fun decreaseIndentLevel() {
        this.indentLevel --
    }
}