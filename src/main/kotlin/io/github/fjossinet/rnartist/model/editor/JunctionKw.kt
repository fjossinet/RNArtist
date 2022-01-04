package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class JunctionKw(script: Script, indentLevel:Int): OptionalDSLKeyword(script, " junction", indentLevel) {

    init {
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"name"), Operator(script,"="), StringValueWithQuotes(script,"my junction", editable = true), this.indentLevel+1))
        this.children.add(1, LocationKw(script, this.indentLevel + 1))
        this.children.add(JunctionKw(script, this.indentLevel))
    }

}