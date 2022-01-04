package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class ThemeKw(script: Script, indentLevel:Int): OptionalDSLKeyword(script, " theme", indentLevel) {

    init {
        this.children.add(1, LineKw(this, script, this.indentLevel+1))
        this.children.add(1, HideKw(this, script, this.indentLevel+1))
        this.children.add(1, ShowKw(this, script, this.indentLevel+1))
        this.children.add(1, ColorKw(this, script, this.indentLevel+1))
        this.children.add(1, DetailsKw(this, script, this.indentLevel+1))
    }

}