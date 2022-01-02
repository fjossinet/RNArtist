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

open class OptionalDSLKeyword(script: Script, text:String, indentLevel:Int, var inFinalScript:Boolean = false):
    DSLKeyword(script,text,indentLevel) {
    val addButton = Button(script,  "+ $text", null)
    val removeButton = Button(script, null, FontIcon("fas-trash:15"))

    override var fontSize:Int = RnartistConfig.editorFontSize
        set(value) {
            field = value
            this.text.font = Font.font(RnartistConfig.editorFontName, value.toDouble())
        }

    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        this.addToFinalScript(inFinalScript)

        addButton.onAction = EventHandler {
            this.addToFinalScript(true)
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.addToFinalScript(false)
            script.initScript()
        }
    }

    /**
     * Reorganizes the children elements when this element changes its status concerning the final script
     */
    open fun addToFinalScript(add:Boolean) {
        if (add) {
            inFinalScript = true
            this.children.add(OpenedCurly(script))
            this.children.add(ClosedCurly(script, indentLevel))
        } else {
            inFinalScript = false
            val toRemove = mutableListOf<DSLElement>()
            for (e in this.children) {
                toRemove.add(e)
                if (e is ClosedCurly)
                    break
            }
            this.children.removeAll(toRemove)
        }
    }

    override fun dumpNodes(nodes:MutableList<Node>, withTabs:Boolean) {
        if (inFinalScript || !inFinalScript && this.children.isEmpty()) {
            if (withTabs)
                (0 until indentLevel).forEach {
                    nodes.add(ScriptTab(script).text)
                }
    }
        if (inFinalScript) {
            nodes.add(this.removeButton)
            nodes.add(this.text)
        }
        else {
            if (this.children.isEmpty()) { //if not empty, we have a keyword that can be multiple. And there is a children allowing to add a new one
                nodes.add(this.addButton)
                nodes.add(Text("\n"))
            }
        }
        this.children.forEach {
            it.dumpNodes(nodes, withTabs)
        }
    }

}