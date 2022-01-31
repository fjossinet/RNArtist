package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.Collapse
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.*
import javafx.scene.text.Text

abstract class DSLKeyword(parent:DSLElementInt, script: Script, text:String, indentLevel:Int): DSLElement(parent, script, text, indentLevel) {

    class KeywordNode(vararg children:Node): HBox() {

        val text:String
            get() = "${(this.children.get(this.children.size-3) as Text).text} { ${System.lineSeparator()}"

        init {
            this.children.addAll(children)
            this.spacing = 5.0
            this.alignment = Pos.CENTER
            this.padding = Insets(2.0,2.0,2.0,2.0)
            //this.border = Border(BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii(2.0), BorderWidths(2.0)))
        }
    }

    val openedCurly = OpenedCurly(this, script)
    val closedCurly = ClosedCurly(this, script, indentLevel)
    val collapseButton = Collapse(script)

    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
    }

    override fun increaseIndentLevel() {
        this.indentLevel ++
        super.increaseIndentLevel()
    }

    override fun decreaseIndentLevel() {
        this.indentLevel--
        super.decreaseIndentLevel()
    }

    override fun dumpNodes(nodes: MutableList<Node>) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(this, script).text)
        }
        nodes.add(KeywordNode(this.text, collapseButton, this.openedCurly.text))
        nodes.add(Text(System.lineSeparator()))
        if (!collapseButton.collapsed) {
            this.children.forEach {
                it.dumpNodes(nodes)
            }
            this.closedCurly.dumpNodes(nodes)
        }
        else {
            this.closedCurly.dumpNodes(nodes)
        }

    }

    override fun dumpText(text: StringBuilder, useAbsolutePath: Boolean) {
        (0 until indentLevel).forEach {
            text.append(" ")
        }
        text.append(this.text.text, " ", this.openedCurly.text.text, System.lineSeparator())
        children.forEach { it.dumpText(text, useAbsolutePath) }
        this.closedCurly.dumpText(text, useAbsolutePath)
    }

    override fun searchAll(hits:MutableList<DSLElementInt>, query:(DSLElementInt) -> Boolean) {
        if (query(this))
            hits.add(this)
        if (query(this.openedCurly))
            hits.add(this.openedCurly)
        if (query(this.closedCurly))
            hits.add(this.closedCurly)
        hits.addAll(this.children.map { it.searchAll(hits, query) }.filterIsInstance<DSLElement>())
    }
}