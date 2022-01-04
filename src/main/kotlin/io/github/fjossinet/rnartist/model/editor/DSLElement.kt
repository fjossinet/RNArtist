package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text

open class DSLElement(val script: Script, text:String, var indentLevel:Int) {
    val children = mutableListOf<DSLElement>()
    val text = Text()

    var color:Color = Color.web("#000000")
        set(value) {
            field = value
            text.fill = value
        }

    open var fontName:String = RnartistConfig.editorFontName
        set(value) {
            field = value
            this.text.font = Font.font(value, RnartistConfig.editorFontSize.toDouble())
        }

    open var fontSize:Int = RnartistConfig.editorFontSize
        set(value) {
            field = value
            this.text.font = Font.font(RnartistConfig.editorFontName, value.toDouble())
        }

    init {
        this.text.text = text
        this.text.font = Font.font(RnartistConfig.editorFontName, RnartistConfig.editorFontSize.toDouble())
    }

    open fun increaseIndentLevel() {
        this.children.forEach { it.increaseIndentLevel() }
    }

    open fun decreaseIndentLevel() {
        this.children.forEach { it.decreaseIndentLevel() }
    }

    /**
     * Generates the JavaFX Nodes from the editor model
     */
    open fun dumpNodes(nodes:MutableList<Node>) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(script).text)
        }
        nodes.add(this.text)
        this.children.forEach {
            it.dumpNodes(nodes)
        }
    }

    fun searchFirst(query:(DSLElement) -> Boolean ):DSLElement? {
        if (query(this))
            return this
        else
            return this.children.map { it.searchFirst(query) }.filterIsInstance<DSLElement>().firstOrNull()
    }

    fun searchAll(hits:MutableList<DSLElement>, query:(DSLElement) -> Boolean) {
        if (query(this))
            hits.add(this)
        hits.addAll(this.children.map { it.searchAll(hits, query) }.filterIsInstance<DSLElement>())
    }
}