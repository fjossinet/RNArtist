package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script

class DetailsKw(script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script, " details", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, LocationKw(script, this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"type"), Operator(script,"="), TypeField(script,"click me", listOf("helix", "single_strand", "junction")), this.indentLevel + 1))
            this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"value"), Operator(script,"="), StringWithoutQuotes(script, "1", editable = true), this.indentLevel+1))
            this.children.add(DetailsKw(script, indentLevel))
        }
    }

    fun getLocation(): Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.location

    /**
     * No argument needed. The function addToFinalScript (fired with the button) uses the current selection
     */
    fun setLocation() {
        if (!this.inFinalScript)
            this.addButton.fire()
        val l = (this.searchFirst { it is LocationKw } as LocationKw?)!!
        if (script.mediator.canvas2D.getSelection().isNotEmpty() && !l.inFinalScript) //if there is a selection, the location needs to be added to the script
            l.addButton.fire()
    }

    fun setlevel(level:String) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val parameter = this.searchFirst { it is DSLParameter && "value".equals(it.key.text.text.trim())} as DSLParameter
        parameter.value.text.text = level
    }

}