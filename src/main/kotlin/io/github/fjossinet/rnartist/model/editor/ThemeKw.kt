package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class ThemeKw(parent:RNArtistKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "theme", indentLevel) {

    init {
        this.children.add(LineKw(this, script, this.indentLevel+1))
        this.children.add(HideKw(this, script, this.indentLevel+1))
        this.children.add(ShowKw(this, script, this.indentLevel+1))
        this.children.add(ColorKw(this, script, this.indentLevel+1))
        this.children.add(DetailsKw(this, script, this.indentLevel+1))

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val index = this.parent.children.indexOf(this)
            this.parent.children.remove(this)
            this.parent.children.add(index, ThemeKw(this.parent as RNArtistKw, script, this.indentLevel))
            script.initScript()
        }

    }

}