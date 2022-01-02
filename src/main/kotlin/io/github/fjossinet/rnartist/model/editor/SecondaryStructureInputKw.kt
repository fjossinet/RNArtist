package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class SecondaryStructureInputKw(script: Script, indentLevel:Int): OptionalDSLKeyword(script, " ss", indentLevel) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(this.children.size - 1, BracketNotationKw(this, script, this.indentLevel + 1))
            this.children.add(this.children.size - 1, CtKw(this, script, this.indentLevel + 1))
            this.children.add(this.children.size - 1, BpseqKw(this, script, this.indentLevel + 1))
            this.children.add(this.children.size - 1, ViennaKw(this, script, this.indentLevel + 1))
            this.children.add(this.children.size - 1, PDBKw(this, script, this.indentLevel + 1))
            this.children.add(this.children.size - 1, StockholmKw(this, script, this.indentLevel + 1))
            this.children.add(this.children.size - 1, RfamKw(this, script, this.indentLevel + 1))
        }
    }

}