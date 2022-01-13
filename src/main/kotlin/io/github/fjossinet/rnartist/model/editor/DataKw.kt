package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class DataKw(parent:RNArtistKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "data", indentLevel) {

    init {
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"file"), Operator(this, script,"="), FileField(this, script), this.indentLevel+1))
        this.children.add(
            OptionalDSLParameter(this,
                script,
                "value",
                StringWithQuotes(this, script, editable = true),
                Operator(this, script, "to"),
                FloatField(this, script),
                this.indentLevel + 1,
                canBeMultiple = true
            )
        )

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val index = this.parent.children.indexOf(this)
            this.parent.children.remove(this)
            this.parent.children.add(index, DataKw(this.parent as RNArtistKw, script, this.indentLevel))
            script.initScript()
        }
    }
}