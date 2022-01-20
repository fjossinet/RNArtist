package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class RfamKw(parent:SecondaryStructureInputKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script,  "rfam", indentLevel) {

    init {
        this.children.add(DSLParameter(this, script, StringWithoutQuotes(this, script,"id"), Operator(this, script,"="), StringValueWithQuotes(this, script, editable = true), this.indentLevel+1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"name"), Operator(this, script,"="), StringValueWithQuotes(this, script,"consensus", editable = true), this.indentLevel+1))
        this.children.add(OptionalDSLParameter(this, script, "numbering", StringWithoutQuotes(this, script,"use"), Operator(this, script,"alignment"), StringWithoutQuotes(this, script,"numbering"), this.indentLevel+1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is RfamKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, RfamKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            if (this.parent.children.indexOf(this) <this.parent.children.size-1 && this.parent.children.get(this.parent.children.indexOf(this) + 1) is RfamKw)
                this.parent.children.remove(this)
            else if (this.parent.children.indexOf(this) > 0) {
                val previous = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (previous is RfamKw && !previous.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

    fun getId() = (this.children.first() as DSLParameter).value.text.text.replace("\"","")

    fun setName(name: String) {
        val param = this.children.get(1) as OptionalDSLParameter
        param.value.text.text = "\"$name\""
        param.addButton.fire()
    }
}