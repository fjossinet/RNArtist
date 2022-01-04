package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.Button
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon

open class OptionalDSLKeyword(script: Script, text:String, indentLevel:Int):
    DSLKeyword(script,text,indentLevel) {

    var inFinalScript = false
        protected set(value) {
            field = value
        }
    val addButton = Button(script,  "+ $text", null)
    val removeButton = Button(script, null, FontIcon("fas-trash:15"))

    override var fontSize:Int = RnartistConfig.editorFontSize
        set(value) {
            field = value
            this.text.font = Font.font(RnartistConfig.editorFontName, value.toDouble())
        }

    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)

        addButton.onAction = EventHandler {
            this.inFinalScript = true
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.inFinalScript = false
            script.initScript()
        }
    }

    override fun dumpNodes(nodes:MutableList<Node>) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(script).text)
        }
        if (inFinalScript) {
            nodes.add(this.removeButton)
            nodes.add(this.text)
            this.children.forEach {
                it.dumpNodes(nodes)
            }
        }
        else {
            nodes.add(this.addButton)
            nodes.add(Text("\n"))
        }

    }

}