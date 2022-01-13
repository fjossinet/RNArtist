package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import io.github.fjossinet.rnartist.gui.editor.Script

class PartsKw(parent: SecondaryStructureInputKw, script: Script, indentLevel:Int = 0): OptionalDSLKeyword(parent, script, "parts", indentLevel) {

    init {
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"source"), Operator(this, script, "="), StringValueWithQuotes(this, script, "",editable = true), indentLevel+1))
        this.children.add(RnaKw(this, script, this.indentLevel + 1))
        this.children.add(HelixKw(this, script, this.indentLevel + 1))
        this.children.add(InteractionKw(this, script, this.indentLevel + 1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            script.mediator.drawingDisplayed?.get()?.drawing?.secondaryStructure?.let { ss ->
                script.allowScriptInit = false
                ss.helices.forEach { helix ->
                    val helixKw = this.searchFirst { it is HelixKw && !it.inFinalScript } as HelixKw
                    helixKw.setHelix(helix)
                    helixKw.collapseButton.collapsed = true
                }
                (this.searchFirst { it is RnaKw } as RnaKw).setRna(ss.rna)
                val p = this.searchFirst { it is OptionalDSLParameter && "source".equals(it.key.text.text.trim())} as OptionalDSLParameter
                p.value.text.text = "\"${ss.source.toString()}\""
                p.addButton.fire()
                script.allowScriptInit = true
                script.initScript()
            } ?: run {
                script.initScript()
            }
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val index = this.parent.children.indexOf(this)
            this.parent.children.remove(this)
            this.parent.children.add(index, PartsKw(this.parent as SecondaryStructureInputKw, script, this.indentLevel))
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

}