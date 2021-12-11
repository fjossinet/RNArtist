package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.RnartistConfig
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight

abstract class DSLKeyword(editor: ScriptEditor, text:String, indentLevel:Int): DSLElement(editor, text, indentLevel) {
    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        this.children.add(OpenedCurly(editor))
        this.children.add(ClosedCurly(editor, indentLevel))
    }
}