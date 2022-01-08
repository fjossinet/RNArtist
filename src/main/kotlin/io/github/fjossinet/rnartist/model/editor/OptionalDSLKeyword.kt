package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import javafx.scene.Node
import javafx.scene.text.Font
import javafx.scene.text.Text

open class OptionalDSLKeyword(script: Script, text:String, indentLevel:Int):
    DSLKeyword(script,text,indentLevel) {

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

    override fun dumpNodes(nodes: MutableList<Node>, enterInCollapsedNode: Boolean) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(script).text)
        }
        if (inFinalScript) {
            nodes.add(removeButton)
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
        else {
            nodes.add(this.addButton)
            nodes.add(Text("\n"))
        }

    }

}