package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class PDBKw (val parent: SecondaryStructureInputKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script,  " pdb ", indentLevel) {

    init {
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"file"), Operator(script,"="), FileField(script), this.indentLevel+1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"name"), Operator(script,"="), StringValueWithQuotes(script, editable = true), this.indentLevel+1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"id"), Operator(script,"="), StringValueWithQuotes(script, editable = true), this.indentLevel+1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is PDBKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, PDBKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is PDBKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is PDBKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

}