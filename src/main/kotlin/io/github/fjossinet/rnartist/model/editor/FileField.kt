package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.File

class FileField(script: Script, value:String = ""): StringWithQuotes(script, value, true) {

    init {
        this.text.fill = Color.web("#4d4d4d")
        this.text.onMouseClicked = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.initialDirectory = File(script.mediator.rnartist.getInstallDir(),"samples")
            val file = fileChooser.showOpenDialog(script.mediator.scriptEditor.stage)
            file?.let {
                text.text = "\"${file.absolutePath.replace("\\", "/")}\""
            }
        }
    }

    override fun clone():FileField = FileField(script, this.text.text.replace("\"", ""))

    override fun dumpNodes(nodes: MutableList<Node>, enterInCollapsedNode: Boolean) {
        if (this.text.text.replace("\"","").isEmpty()) {
            val button = DataField(script)
            button.onMouseClicked = EventHandler {
                val fileChooser = FileChooser()
                fileChooser.initialDirectory = File(script.mediator.rnartist.getInstallDir(),"samples")
                val file = fileChooser.showOpenDialog(script.mediator.scriptEditor.stage)
                file?.let {
                    text.text = "\"${file.absolutePath.replace("\\", "/")}\""
                }
                script.initScript()
            }
            nodes.add(button)
        }
        else
            nodes.add(this.text)
    }
}