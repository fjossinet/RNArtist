package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.model.editor.*

class RNACentralKw(parent: SecondaryStructureInputKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script,  "rnacentral", indentLevel) {

    init {
        this.children.add(DSLParameter(this, script, StringWithoutQuotes(this, script,"id"), Operator(this, script,"="), StringValueWithQuotes(this, script, editable = true), this.indentLevel+1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is RNACentralKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, RNACentralKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            if (this.parent.children.indexOf(this) <this.parent.children.size-1 && this.parent.children.get(this.parent.children.indexOf(this) + 1) is RNACentralKw)
                this.parent.children.remove(this)
            else if (this.parent.children.indexOf(this) > 0 && this.parent.children.get(this.parent.children.indexOf(this) - 1) is RNACentralKw)
                this.parent.children.remove(this)
            script.initScript()
        }
    }

}