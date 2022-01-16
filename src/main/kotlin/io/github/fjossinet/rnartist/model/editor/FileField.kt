package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import java.io.File

class FileField(parent: DSLElementInt, script: Script, value: String = "") :
    StringWithQuotes(parent, script, value, true) {

    companion object {
        var useAbsolutePath = false
    }

    init {
        this.text.fill = Color.web("#4d4d4d")
        this.text.onMouseClicked = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.initialDirectory = File(script.mediator.rnartist.getInstallDir(), "samples")
            val file = fileChooser.showOpenDialog(script.mediator.sideWindow.stage)
            file?.let {
                text.text = "\"${file.absolutePath.replace("\\", "/")}\""
            }
        }
    }

    override fun clone(): FileField = FileField(this.parent, script)

    override fun dumpNodes(nodes: MutableList<Node>) {
        if (this.text.text.replace("\"", "").isEmpty()) {
            val button = DataField(script)
            button.onMouseClicked = EventHandler {
                val fileChooser = FileChooser()
                fileChooser.initialDirectory = File(script.mediator.rnartist.getInstallDir(), "samples")
                val file = fileChooser.showOpenDialog(script.mediator.sideWindow.stage)
                file?.let {
                    text.text = "\"${file.absolutePath.replace("\\", "/")}\""
                }
                script.initScript()
            }
            nodes.add(button)
        } else
            nodes.add(this.text)
    }

    override fun dumpText(text: StringBuilder) {
        if (this.text.text.replace("\"", "").isNotEmpty()) {
            var inputPath = this.text.text.replace("\"", "")
            if (useAbsolutePath && !inputPath.startsWith("/") /*unix*/ && !inputPath.matches(Regex("^[A-Z]:/.+")) /*windows*/ && script.mediator.scriptEditor.currentScriptLocation != null) {
                inputPath =
                    "${script.mediator.scriptEditor.currentScriptLocation?.absolutePath?.replace("\\", "/")}/$inputPath"
            }
            text.append("\"${inputPath}\"")
        }

    }
}