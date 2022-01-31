package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import javafx.scene.Node
import javafx.scene.text.Font
import javafx.scene.text.Text

open class OptionalDSLKeyword(parent:DSLElementInt, script: Script, text:String, indentLevel:Int):
    DSLKeyword(parent, script,text,indentLevel) {

    var inFinalScript = false
        protected set(value) {
            field = value
        }
    val addButton = AddKeyWord(script, text)
    val removeButton = Remove(script)

    override var fontSize:Int = RnartistConfig.editorFontSize
        set(value) {
            field = value
            this.text.font = Font.font(RnartistConfig.editorFontName, value.toDouble())
        }

    init {
        this.text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)

        addButton.mouseReleased = {
            this.inFinalScript = true
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            script.initScript()
        }
    }

    override fun dumpNodes(nodes: MutableList<Node>) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(this, script).text)
        }
        if (inFinalScript) {
            nodes.add(KeywordNode(removeButton, this.text, collapseButton, this.openedCurly.text))
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
        else {
            nodes.add(this.addButton)
            nodes.add(Text(System.lineSeparator()))
        }
    }

    override fun dumpText(text: StringBuilder, useAbsolutePath: Boolean) {
        if (inFinalScript) {
            (0 until indentLevel).forEach {
                text.append(" ")
            }
            text.append(this.text.text, " ", this.openedCurly.text.text, System.lineSeparator())
            children.forEach { it.dumpText(text, useAbsolutePath) }
            this.closedCurly.dumpText(text, useAbsolutePath)
        }
    }

}