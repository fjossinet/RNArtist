package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.text.Text

class PDBKw (parent: SecondaryStructureInputKw, script: Script, indentLevel:Int): InputFileKw(parent, script,  "pdb", indentLevel) {

    init {
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"file"), Operator(this, script,"="), FileField(this, script), this.indentLevel+1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"id"), Operator(this, script,"="), StringValueWithQuotes(this, script, editable = true), this.indentLevel+1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"name"), Operator(this, script,"="), StringValueWithQuotes(this, script, editable = true), this.indentLevel+1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is PDBKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, PDBKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            if (this.parent.children.indexOf(this) <this.parent.children.size-1 && this.parent.children.get(this.parent.children.indexOf(this) + 1) is PDBKw)
                this.parent.children.remove(this)
            else if (this.parent.children.indexOf(this) > 0) {
                val previous = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (previous is PDBKw && !previous.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

    //for a PDB file, the file parameter is optional since the user can set the id to download the PDB entry
    override open fun getFileField(): FileField {
        return (this.searchFirst { it is OptionalDSLParameter && "file".equals(it.key.text.text.trim()) } as OptionalDSLParameter).value as FileField
    }

}