package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.Button
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.event.EventHandler
import javafx.scene.Node
import org.kordamp.ikonli.javafx.FontIcon

open class OptionalDSLParameter(editor: ScriptEditor, var buttonName:String? = null, key:ParameterField, operator:ParameterField = StringWithQuotes(editor, "= "), value:ParameterField, indentLevel:Int, var inFinalScript:Boolean = false, var canBeMultiple:Boolean = false):
    DSLParameter(editor, key, operator, value,indentLevel) {
    val addButton = Button(editor, "+ ${buttonName ?: key.text.text}", null)
    val removeButton = Button(editor, null, FontIcon("fas-trash:15"))

    init {
        this.addToFinalScript(inFinalScript)

        addButton.onMouseClicked = EventHandler {
            this.addToFinalScript(true)
            editor.parameterAddedToScript(this)
        }

        removeButton.onMouseClicked = EventHandler {
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
            this.children.add(field)
            this.children.add(operator)
            this.children.add(value)
            this.children.add(DSLElement(editor,  "\n" ,  0))
            if (this.canBeMultiple)
                this.children.add(OptionalDSLParameter(editor, buttonName, field, operator, cloneValue(value), indentLevel, false, canBeMultiple))
        } else {
            inFinalScript = false
            this.children.add(DSLElement(editor, "\n",0))
        }
    }

    override fun dumpNodes(nodes:MutableList<Node>, withTabs:Boolean) {
        if (withTabs)
            (0 until indentLevel).forEach {
                nodes.add(ScriptTab(editor).text)
            }
        if (inFinalScript) {
            nodes.add(this.removeButton)
        }
        else {
            nodes.add(this.addButton)
        }
        this.children.forEach {
            it.dumpNodes(nodes)
        }

    }

    private fun cloneValue(value:ParameterField):ParameterField {
        return when (value) {
            is ColorField -> ColorField(editor, text.text.replace("\"", ""))
            is IntegerField -> IntegerField(editor, value.text.text.toInt(), value.min, value.max)
            is FloatField -> FloatField(editor, value.text.text)
            is FileField -> FileField(editor)
            is StringWithQuotes -> StringWithQuotes(editor, value.text.text.replace("\"", ""))
            else -> throw Exception()
        }
    }

}