package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.Button
import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.text.Font
import org.kordamp.ikonli.javafx.FontIcon

open class OptionalDSLParameter(editor: ScriptEditor, var buttonName:String? = null, key:ParameterField, operator:Operator, value:ParameterField, indentLevel:Int, var inFinalScript:Boolean = false, var canBeMultiple:Boolean = false):
    DSLParameter(editor, key, operator, value,indentLevel) {
    val addButton = Button(editor, "+ ${buttonName ?: key.text.text}", null)
    val removeButton = Button(editor, null, FontIcon("fas-trash:15"))

    override var fontSize:Int = RnartistConfig.editorFontSize
        set(value) {
            field = value
            this.text.font = Font.font(RnartistConfig.editorFontName, value.toDouble())
            this.key.fontSize = value
            this.operator.fontSize = value
            this.value.fontSize = value
        }

    override var fontName:String = RnartistConfig.editorFontName
        set(value) {
            field = value
            this.text.font = Font.font(value, RnartistConfig.editorFontSize.toDouble())
            this.key.fontName = value
            this.operator.fontName = value
            this.value.fontName = value
        }

    init {
        this.addToFinalScript(inFinalScript)

        addButton.onAction = EventHandler {
            this.addToFinalScript(true)
            editor.parameterAddedToScript(this)
        }

        removeButton.onAction = EventHandler {
            var nodes = mutableListOf<Node>()
            dumpNodes(nodes, false)
            this.addToFinalScript(false)
            editor.parameterRemovedFromScript(this, nodes.size)
        }
    }

    /**
     * Reorganizes the children elements when this element change its status concerning the final script
     */
    open fun addToFinalScript(add:Boolean) {
        this.children.clear()
        if (add) {
            inFinalScript = true
            this.children.add(key)
            this.children.add(operator)
            this.children.add(value)
            this.children.add(NewLine(editor))
            if (this.canBeMultiple)
                this.children.add(OptionalDSLParameter(editor, buttonName, key.clone(), operator.clone(), value.clone(), indentLevel, false, canBeMultiple))
        } else {
            inFinalScript = false
            this.children.add(NewLine(editor))
        }
    }

    override fun dumpNodes(nodes:MutableList<Node>, withTabs:Boolean) {
        if (withTabs)
            (0 until indentLevel).forEach {
                nodes.add(ScriptTab(editor).text)
            }
        if (inFinalScript) {
            nodes.add(this.removeButton)
            nodes.add(this.text)
        }
        else {
            nodes.add(this.addButton)
        }
        this.children.forEach {
            it.dumpNodes(nodes)
        }

    }

}