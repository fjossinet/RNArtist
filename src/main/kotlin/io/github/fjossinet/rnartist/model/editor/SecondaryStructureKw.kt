package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class SecondaryStructureKw(editor: ScriptEditor, indentLevel:Int): DSLKeyword(editor, "ss", indentLevel) {

    init {
        this.children.add(this.children.size-1, BracketNotationKw(this, editor, this.indentLevel+1))
        this.children.add(this.children.size-1, CtKw(this, editor, this.indentLevel+1))
        this.children.add(this.children.size-1, BpseqKw(this, editor, this.indentLevel+1))
        this.children.add(this.children.size-1, ViennaKw(this, editor, this.indentLevel+1))
        this.children.add(this.children.size-1, StockholmKw(this, editor, this.indentLevel+1))
        this.children.add(this.children.size-1, RfamKw(this, editor, this.indentLevel+1))
    }

    fun disableAddbuttons(disable:Boolean) {
        this.children.filter { it is OptionalDSLKeyword }.forEach {
            (it as OptionalDSLKeyword).addButton.isDisable = disable
        }
    }
}