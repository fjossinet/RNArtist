package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import java.util.*

class SecondaryStructureKw(script: Script, indentLevel:Int = 0, val id:String= UUID.randomUUID().toString()): OptionalDSLKeyword(script, " ss", indentLevel, inFinalScript = false) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            script.mediator.drawingDisplayed?.get()?.drawing?.secondaryStructure?.let { secondaryStructure ->
                var lastHelixKw:HelixKw? = null
                secondaryStructure.helices.forEach { helix ->
                    lastHelixKw?.let {
                        lastHelixKw = it.searchFirst { it is HelixKw && !it.inFinalScript } as HelixKw
                        lastHelixKw!!.helix = helix
                        lastHelixKw!!.addToFinalScript(true)
                    } ?: run {
                        lastHelixKw = HelixKw( script, indentLevel+1, helix)
                        lastHelixKw!!.addToFinalScript(true)
                        this.children.add(1, lastHelixKw!!)
                    }
                }
                val rnaKw = RnaKw(script, this.indentLevel + 1, secondaryStructure.rna)
                this.children.add(1, rnaKw)
            } ?: run {
                this.children.add(1, InteractionKw(script, this.indentLevel + 1))
                this.children.add(1, HelixKw(script, this.indentLevel + 1))
                this.children.add(1, RnaKw(script, this.indentLevel + 1))
            }
            //we need to remove the ss element in the themeAndLayoutScript
            script.mediator.scriptEditor.themeAndLayoutScript.removeSS()
        }
    }

}