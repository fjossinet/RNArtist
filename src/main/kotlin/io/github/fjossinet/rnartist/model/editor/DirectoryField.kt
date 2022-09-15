package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import java.io.File

class DirectoryField(parent:DSLElementInt, script: Script, value: String = "") :
    StringWithQuotes(parent, script, value, true) {

    init {
        val l = script.mediator.scriptEditor?.currentScriptLocation
        this.text.text = if (l == null)
            ""
        else
            "\"${l.absolutePath.replace("\\", "/")}\""
        this.text.fill = Color.web("#4d4d4d")
        this.text.onMouseClicked = EventHandler {
            val directoryChooser = DirectoryChooser()
            directoryChooser.initialDirectory =
                script.mediator.scriptEditor.currentScriptLocation ?: File(System.getProperty("user.home"))
            val dir = directoryChooser.showDialog(script.mediator.rnartist.stage)
            dir?.let {
                text.text = "\"${dir.absolutePath.replace("\\", "/")}\""
            }
        }
    }

    override fun clone(): DirectoryField = DirectoryField(this.parent, script)

    override fun dumpNodes(nodes: MutableList<Node>) {
        if (this.text.text.replace("\"","").isEmpty()) {
            val button = DataField(script)
            button.onMouseClicked = EventHandler {
                val directoryChooser = DirectoryChooser()
                directoryChooser.initialDirectory =
                    script.mediator.scriptEditor.currentScriptLocation ?: File(System.getProperty("user.home"))
                val dir = directoryChooser.showDialog(script.mediator.rnartist.stage)
                dir?.let {
                    text.text = "\"${dir.absolutePath.replace("\\", "/")}\""
                }
                script.initScript()
            }
            nodes.add(button)
        }
        else
            nodes.add(this.text)
    }

    override fun dumpText(text: StringBuilder, useAbsolutePath: Boolean) {
        if (this.text.text.replace("\"", "").isNotEmpty())
            text.append(this.text.text)
    }

}