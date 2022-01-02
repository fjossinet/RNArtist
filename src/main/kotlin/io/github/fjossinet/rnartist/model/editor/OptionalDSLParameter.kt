package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.Button
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon

open class OptionalDSLParameter(script: Script, var buttonName:String? = null, key:ParameterField, operator:Operator, value:ParameterField, indentLevel:Int, var inFinalScript:Boolean = false, var canBeMultiple:Boolean = false):
    DSLParameter(script, key, operator, value,indentLevel) {
    val addButton = Button(script, "+ ${buttonName ?: key.text.text}", null)
    val removeButton = Button(script, null, FontIcon("fas-trash:15"))

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
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.addToFinalScript(false)
            script.initScript()
        }
    }

    /**
     * Reorganizes the children elements when this element change its status concerning the final script
     */
    open fun addToFinalScript(add:Boolean) {
        if (add) {
            inFinalScript = true
            if (!this.children.contains(key)) {
                this.children.add(key)
                this.children.add(operator)
                this.children.add(value)
                this.children.add(NewLine(script))
                if (this.canBeMultiple)
                    this.children.add(
                        OptionalDSLParameter(
                            script,
                            buttonName,
                            key.clone(),
                            operator.clone(),
                            value.clone(),
                            indentLevel,
                            false,
                            canBeMultiple
                        )
                    )
            }
        } else {
            inFinalScript = false
            this.children.remove(key)
            this.children.remove(operator)
            this.children.remove(value)
            this.children.removeAt(0)
        }
    }

    override fun dumpNodes(nodes:MutableList<Node>, withTabs:Boolean) {
        if (inFinalScript || !inFinalScript && this.children.isEmpty()) {
            if (withTabs)
                (0 until indentLevel).forEach {
                    nodes.add(ScriptTab(script).text)
                }
        }
        if (inFinalScript) {
            nodes.add(this.removeButton)
            nodes.add(this.text)
        }
        if (this.children.isEmpty()) { //if not empty, we have a parameter that can be multiple. And there is a children allowing to add a new one
            nodes.add(this.addButton)
            nodes.add(Text("\n"))
        }
        this.children.forEach {
            it.dumpNodes(nodes, withTabs)
        }

    }

}