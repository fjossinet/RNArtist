package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import kotlin.text.StringBuilder

interface DSLElementInt {
    val children:MutableList<DSLElementInt>
    var fontName:String
    var fontSize:Int
    var color:Color

    fun increaseIndentLevel()
    fun decreaseIndentLevel()

    /**
     * Generates the JavaFX Nodes from the editor model
     */
    fun dumpNodes(nodes:MutableList<Node>)

    fun dumpText(text:StringBuilder, useAbsolutePath:Boolean = false)

    fun searchFirst(query:(DSLElementInt) -> Boolean ):DSLElementInt?

    fun searchAll(hits:MutableList<DSLElementInt>, query:(DSLElementInt) -> Boolean)
}

open class DSLElement(val parent:DSLElementInt, val script: Script, text:String?, var indentLevel:Int):DSLElementInt {
    override val children = mutableListOf<DSLElementInt>()

    val text = Text()

    override var color:Color = Color.web("#000000")
        set(value) {
            field = value
            text.fill = value
        }

    override var fontName:String = RnartistConfig.editorFontName
        set(value) {
            field = value
            this.text.font = Font.font(value, RnartistConfig.editorFontSize.toDouble())
        }

    override var fontSize:Int = RnartistConfig.editorFontSize
        set(value) {
            field = value
            this.text.font = Font.font(RnartistConfig.editorFontName, value.toDouble())
        }

    init {
        this.text.text = text
        this.text.font = Font.font(RnartistConfig.editorFontName, RnartistConfig.editorFontSize.toDouble())
    }

    override fun increaseIndentLevel() {
        this.children.forEach { it.increaseIndentLevel() }
    }

    override fun decreaseIndentLevel() {
        this.children.forEach { it.decreaseIndentLevel() }
    }

    /**
     * Generates the JavaFX Nodes from the editor model
     */
    override fun dumpNodes(nodes:MutableList<Node>) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(this, script).text)
        }
        nodes.add(this.text)
        this.children.forEach {
            it.dumpNodes(nodes)
        }
    }

    override fun dumpText(text: StringBuilder, useAbsolutePath: Boolean) {
        (0 until indentLevel).forEach {
            text.append(" ")
        }
        text.append(this.text.text)
        children.forEach { it.dumpText(text, useAbsolutePath) }
    }

    override fun searchFirst(query:(DSLElementInt) -> Boolean ):DSLElementInt? {
        if (query(this))
            return this
        else
            return this.children.map { it.searchFirst(query) }.filterIsInstance<DSLElement>().firstOrNull()
    }

    override fun searchAll(hits:MutableList<DSLElementInt>, query:(DSLElementInt) -> Boolean) {
        if (query(this))
            hits.add(this)
        hits.addAll(this.children.map { it.searchAll(hits, query) }.filterIsInstance<DSLElement>())
    }
}