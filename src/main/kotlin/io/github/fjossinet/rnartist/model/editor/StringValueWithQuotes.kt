package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.scene.paint.Color

class StringValueWithQuotes(editor: ScriptEditor, value:String, editable: Boolean = false):StringWithQuotes(editor, value, editable) {

    init {
        this.text.fill = Color.web("4d4d4d")
    }

    override fun clone(): StringValueWithQuotes = StringValueWithQuotes(editor, this.text.text.replace("\"", ""), this.editable)

}