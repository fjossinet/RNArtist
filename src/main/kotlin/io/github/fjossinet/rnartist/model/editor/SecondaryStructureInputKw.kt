package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class SecondaryStructureInputKw(parent:RNArtistKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "ss", indentLevel) {

    init {
        this.children.add(PartsKw(this, script, this.indentLevel + 1))
        this.children.add(BracketNotationKw(this, script, this.indentLevel + 1))
        this.children.add(CtKw(this, script, this.indentLevel + 1))
        this.children.add(BpseqKw(this, script, this.indentLevel + 1))
        this.children.add(ViennaKw(this, script, this.indentLevel + 1))
        this.children.add(PDBKw(this, script, this.indentLevel + 1))
        this.children.add(StockholmKw(this, script, this.indentLevel + 1))
        this.children.add(RfamKw(this, script, this.indentLevel + 1))
        this.children.add(RNACentralKw(this, script, this.indentLevel + 1))
    }

    fun getRawSSKw():PartsKw = this.searchFirst { it is PartsKw } as PartsKw

}