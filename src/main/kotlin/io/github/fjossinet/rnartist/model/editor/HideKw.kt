package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class HideKw(script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script, " hide", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"data"), DataOperatorField(script,"gt"), FloatField(script,"20.7"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"type"), Operator(script,"="), TypeField(script,"click me"), this.indentLevel + 1))
            this.children.add(1, LocationKw(script, this.indentLevel + 1))
            this.children.add(HideKw(script, indentLevel))
        }
    }
}