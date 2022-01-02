package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script

class LineKw(script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script, " line", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"value"), Operator(script,"="), FloatField(script,"2.0"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"type"), Operator(script,"="), TypeField(script,"click me"), this.indentLevel + 1))
            this.children.add(1, LocationKw(script, this.indentLevel + 1))
            this.children.add(LineKw(script, indentLevel))
        }
    }

    fun setTypes(types:String) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val parameter = this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter
        if (!parameter.inFinalScript)
            parameter.addButton.fire()
        parameter.value.text.text = "\"${types}\""
        if (!parameter.inFinalScript)
            parameter.addButton.fire()
    }

    fun getTypes():String? = (this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter?)?.value?.text?.text?.replace("\"","")

    fun setWidth(value:String) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val parameter = this.searchFirst { it is DSLParameter && "value".equals(it.key.text.text.trim())} as DSLParameter
        parameter.value.text.text = value
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

    fun removeLocation() {
        (this.searchFirst { it is LocationKw } as LocationKw?)?.removeButton?.fire()
    }
}