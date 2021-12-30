package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.event.EventHandler
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import java.io.File

class DirectoryField(editor: ScriptEditor, value:String = "Click me to choose your directory"): StringWithQuotes(editor, value, true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val directoryChooser = DirectoryChooser()
                directoryChooser.initialDirectory = File(System.getProperty("user.home"))
                val dir = directoryChooser.showDialog(editor.stage)
                dir?.let {
                    text.text = "\"${dir.absolutePath.replace("\\", "/")}\""
                }
            }
        }
    }

    override fun clone():DirectoryField = DirectoryField(editor, this.text.text.replace("\"", ""))

}