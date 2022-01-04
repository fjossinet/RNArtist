package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.SecondaryStructure
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class SecondaryStructureKw(script: Script, indentLevel:Int = 0): OptionalDSLKeyword(script, " ss", indentLevel) {

    init {
        this.children.add(1, InteractionKw(this, script, this.indentLevel + 1))
        this.children.add(1, HelixKw(this, script, this.indentLevel + 1))
        this.children.add(1, RnaKw(script, this.indentLevel + 1))

        addButton.onAction = EventHandler {
            this.inFinalScript = true
            script.mediator.drawingDisplayed?.get()?.drawing?.secondaryStructure?.let { ss ->
                ss.helices.forEach { helix ->
                    val helixKw = this.searchFirst { it is HelixKw && !it.inFinalScript } as HelixKw
                    helixKw.setHelix(helix)
                }
                (this.searchFirst { it is RnaKw } as RnaKw).setRna(ss.rna)
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

}