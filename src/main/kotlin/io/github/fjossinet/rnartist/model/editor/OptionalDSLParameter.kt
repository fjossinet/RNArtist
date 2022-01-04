package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.editor.Button
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.text.Font
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon

open class OptionalDSLParameter(var parent:DSLElement, script: Script, var buttonName:String? = null, key:ParameterField, operator:Operator, value:ParameterField, indentLevel:Int, var canBeMultiple:Boolean = false):
    DSLParameter(script, key, operator, value,indentLevel) {

    var inFinalScript = false
        protected set(value) {
            field = value
        }
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
        if (this.canBeMultiple) {
            addButton.onAction = EventHandler {
                this.inFinalScript = true
                this.parent.children.add(this.parent.children.indexOf(this)+1, OptionalDSLParameter(parent, script, buttonName, key.clone(), operator.clone(), value.clone(),indentLevel, canBeMultiple))
                script.initScript()
            }

            removeButton.onAction = EventHandler {
                this.inFinalScript = false
                val childAfter = this.parent.children.get(this.parent.children.indexOf(this)+1)
                if (childAfter is OptionalDSLParameter)
                    this.parent.children.remove(this)
                else {
                    val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                    if (childBefore is OptionalDSLParameter && !childBefore.inFinalScript)
                        this.parent.children.remove(this)
                }
                script.initScript()
            }
        }
        else {

            addButton.onAction = EventHandler {
                this.inFinalScript = true
                script.initScript()
            }

            removeButton.onAction = EventHandler {
                this.inFinalScript = false
                script.initScript()
            }
        }
    }

    override fun dumpNodes(nodes:MutableList<Node>) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(script).text)
        }
        if (inFinalScript) {
            nodes.add(this.removeButton)
            nodes.add(this.text)
            this.children.forEach {
                it.dumpNodes(nodes)
            }
        } else {
            nodes.add(this.addButton)
            nodes.add(Text("\n"))
        }

    }

}