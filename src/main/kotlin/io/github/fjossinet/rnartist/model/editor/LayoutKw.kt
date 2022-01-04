package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class LayoutKw(script: Script, indentLevel:Int): OptionalDSLKeyword(script, " layout", indentLevel) {

    init {
        this.children.add(1, JunctionLayoutKw(this, script, this.indentLevel+1))
    }

    /**
     * Returns the first JunctionLayoutKx not in the final script
     */
    fun getJunctionLayoutKw():JunctionLayoutKw = this.searchFirst { it is JunctionLayoutKw && !it.inFinalScript } as JunctionLayoutKw

}