package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class LayoutKw(parent:RNArtistKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "layout", indentLevel) {

    init {
        this.children.add(JunctionLayoutKw(this, script, this.indentLevel+1))

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val index = this.parent.children.indexOf(this)
            this.parent.children.remove(this)
            this.parent.children.add(index, LayoutKw(this.parent as RNArtistKw, script, this.indentLevel))
            script.initScript()
        }
    }

    /**
     * Returns the first JunctionLayoutKx not in the final script
     */
    fun getJunctionLayoutKw():JunctionLayoutKw = this.searchFirst { it is JunctionLayoutKw && !it.inFinalScript } as JunctionLayoutKw

}