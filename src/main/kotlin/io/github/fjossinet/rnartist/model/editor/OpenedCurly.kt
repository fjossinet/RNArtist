package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

class OpenedCurly(editor: ScriptEditor): DSLElement(editor, " {\n", 0) {
    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.bracesEditorColor)
        this.text.font = Font.font(RnartistConfig.editorFontName, FontWeight.BOLD, RnartistConfig.editorFontSize.toDouble())
    }
}