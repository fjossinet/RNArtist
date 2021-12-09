package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.Text

open class DSLElement(val editor: ScriptEditor, text:String, val indentLevel:Int) {
    val children = mutableListOf<DSLElement>()
    val text = Text()

    init {
        this.text.text = text
        this.text.font = Font.font("Helvetica", FontPosture.REGULAR, 20.0)
        this.text.fill = Color.WHITE
    }

    /**
     * Generates the JavaFX Nodes from the editor model
     */
    open fun dumpNodes(nodes:MutableList<Node>, withTabs:Boolean=true) {
        if (withTabs)
            (0 until indentLevel).forEach {
                nodes.add(ScriptTab(editor).text)
            }
        nodes.add(this.text)
        this.children.forEach {
            it.dumpNodes(nodes)
        }
    }

    fun search(query:(DSLElement) -> Boolean ):DSLElement? {
        if (query(this))
            return this
        else
            return this.children.map { it.search(query) }.filterIsInstance<DSLElement>().firstOrNull()
    }

}