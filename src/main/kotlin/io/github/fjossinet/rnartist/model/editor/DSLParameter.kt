package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.DataField
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.text.Text

open class DSLParameter(parent:DSLElement, script: Script, var key:ParameterField, var operator:Operator, var value:ParameterField, indentLevel:Int): DSLElement(parent, script, " ", indentLevel) {

    class ParameterNode(vararg children: Node): HBox() {

        val text:String
            get() {
                if (this.children.filterIsInstance<DataField>().isNotEmpty())
                    throw Exception("Some informations are missing in your script")
                return "${(this.children.get(this.children.size-3) as Text).text} ${(this.children.get(this.children.size-2) as Text).text} ${(this.children.get(this.children.size-1) as Text).text} ${System.lineSeparator()}"
            }

        init {
            this.children.addAll(children)
            this.spacing = 5.0
            this.alignment = Pos.CENTER
            this.padding = Insets(2.0,2.0,2.0,2.0)
            //this.border = Border(BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii(2.0), BorderWidths(2.0)))
        }
    }

    init {
        this.children.add(key)
        key.color = awtColorToJavaFX(RnartistConfig.keyParamEditorColor)
        this.children.add(operator)
        operator.color = awtColorToJavaFX(RnartistConfig.operatorParamEditorColor)
        this.children.add(value)
        value.color = awtColorToJavaFX(RnartistConfig.valueParamEditorColor)
    }

    override fun increaseIndentLevel() {
        this.key.indentLevel ++
        super.increaseIndentLevel()
    }

    override fun decreaseIndentLevel() {
        this.key.indentLevel --
        super.decreaseIndentLevel()
    }

    override fun dumpNodes(nodes:MutableList<Node>) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(this, script).text)
        }
        val _nodes = mutableListOf<Node>()
        this.children.forEach {
            it.dumpNodes(_nodes)
        }
        nodes.add(ParameterNode(*_nodes.toTypedArray()))
        nodes.add(Text(System.lineSeparator()))
    }

    override fun dumpText(text:StringBuilder) {
        (0 until indentLevel).forEach {
            text.append(" ")
        }
        children.forEach { it.dumpText(text) }
        text.append(System.lineSeparator())
    }

}