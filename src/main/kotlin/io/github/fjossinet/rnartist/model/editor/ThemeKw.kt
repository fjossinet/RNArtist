package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class ThemeKw(script: Script, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(script, " theme", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, LineKw(script, this.indentLevel+1))
            this.children.add(1, HideKw(script, this.indentLevel+1))
            this.children.add(1, ShowKw(script, this.indentLevel+1))
            this.children.add(1, ColorKw(script, this.indentLevel+1))
            this.children.add(1, DetailsKw(script, this.indentLevel+1))
        }
    }

    fun getDetailsKw():DetailsKw? = this.searchFirst { it is DetailsKw } as DetailsKw?

}