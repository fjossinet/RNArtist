package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.text.Text

class SecondaryStructureKw(script: Script, indentLevel:Int = 0): OptionalDSLKeyword(script, " ss ", indentLevel) {

    init {
        this.children.add(1, InteractionKw(this, script, this.indentLevel + 1))
        this.children.add(1, HelixKw(this, script, this.indentLevel + 1))
        this.children.add(1, RnaKw(script, this.indentLevel + 1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"source"), Operator(script, "="), StringValueWithQuotes(script, "",editable = true), indentLevel+1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            script.mediator.drawingDisplayed?.get()?.drawing?.secondaryStructure?.let { ss ->
                script.allowScriptInit = false
                ss.helices.forEach { helix ->
                    println("Helix ${helix.name} ${helix.location}")
                    val helixKw = this.searchFirst { it is HelixKw && !it.inFinalScript } as HelixKw
                    helixKw.setHelix(helix)
                }
                println("RNA")
                (this.searchFirst { it is RnaKw } as RnaKw).setRna(ss.rna)
                val p = this.searchFirst { it is OptionalDSLParameter && "source".equals(it.key.text.text.trim())} as OptionalDSLParameter
                p.value.text.text = "\"${ss.source.toString()}\""
                p.addButton.fire()
                script.allowScriptInit = true
                script.initScript()
            }
            //we need to remove the ss element in the themeAndLayoutScript
            script.mediator.scriptEditor.themeAndLayoutScript.removeSecondaryStructure()
            script.initScript()
        }

    }

    fun setSecondaryStructure(ss:SecondaryStructure) {
        ss.helices.forEach { helix ->
            val helixKw = this.searchFirst { it is HelixKw && !it.inFinalScript } as HelixKw
            helixKw.setHelix(helix)
        }
        (this.searchFirst { it is RnaKw } as RnaKw).setRna(ss.rna)
        this.addButton.fire()
    }

    override fun dumpNodes(nodes: MutableList<Node>, enterInCollapsedNode: Boolean) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(script).text)
        }
        if (inFinalScript) {
            nodes.add(this.removeButton)
            nodes.add(this.text)
            this.children.forEach {
                it.dumpNodes(nodes, enterInCollapsedNode)
            }
        }
        else {
            nodes.add(this.addButton)
            nodes.add(Text("\n"))
        }

    }

}