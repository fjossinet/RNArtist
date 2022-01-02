package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import java.io.File

class DirectoryField(script: Script, value:String = "Click me to choose your directory"): StringWithQuotes(script, value, true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val directoryChooser = DirectoryChooser()
                directoryChooser.initialDirectory = File(System.getProperty("user.home"))
                val dir = directoryChooser.showDialog(script.mediator.scriptEditor.stage)
                dir?.let {
                    text.text = "\"${dir.absolutePath.replace("\\", "/")}\""
                }
            }
        }
    }

    override fun clone():DirectoryField = DirectoryField(script, this.text.text.replace("\"", ""))

}