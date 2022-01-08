package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.Collapse
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.Node

abstract class DSLKeyword(script: Script, text:String, indentLevel:Int): DSLElement(script, text, indentLevel) {

    val collapseButton = Collapse(script)

    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        this.children.add(OpenedCurly(script))
        this.children.add(ClosedCurly(script, indentLevel))
    }

    override fun increaseIndentLevel() {
        this.indentLevel ++
        super.increaseIndentLevel()
    }

    override fun decreaseIndentLevel() {
        this.indentLevel--
        super.decreaseIndentLevel()
    }

    override fun dumpNodes(nodes: MutableList<Node>, enterInCollapsedNode: Boolean) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(script).text)
        }
        nodes.add(this.text)
        nodes.add(collapseButton)
        if (!collapseButton.collapsed || enterInCollapsedNode) {
            this.children.forEach {
                it.dumpNodes(nodes, enterInCollapsedNode)
            }
        }
        else {
            this.children.forEach {
                if (it is OpenedCurly || it is ClosedCurly)
                    it.dumpNodes(nodes, enterInCollapsedNode)
            }
        }

    }
}