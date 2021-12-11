package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.event.EventHandler
import javafx.scene.paint.Color
import javafx.stage.FileChooser

class FileField(editor: ScriptEditor, value:String = "Click me to choose your file"): StringWithQuotes(editor, value, true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val fileChooser = FileChooser()
                val file = fileChooser.showOpenDialog(editor.stage)
                file?.let {
                    text.text = "\"${file.absolutePath.replace("\\", "/")}\""
                }
            }
        }
    }

    override fun clone():FileField = FileField(editor, this.text.text.replace("\"", ""))

}