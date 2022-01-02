package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import java.io.File

class FileField(script: Script, value:String = "Click me to choose your file"): StringWithQuotes(script, value, true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        if (editable) {
            this.text.onMouseClicked = EventHandler {
                val fileChooser = FileChooser()
                fileChooser.initialDirectory = File(script.mediator.rnartist.getInstallDir(),"samples")
                val file = fileChooser.showOpenDialog(script.mediator.scriptEditor.stage)
                file?.let {
                    text.text = "\"${file.absolutePath.replace("\\", "/")}\""
                }
            }
        }
    }

    override fun clone():FileField = FileField(script, this.text.text.replace("\"", ""))

}