package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class RfamKw(val parent:SecondaryStructureInputKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script,  " rfam", indentLevel) {

    init {
        this.children.add(1, OptionalDSLParameter(this, script, "numbering", StringWithoutQuotes(script,"use"), Operator(script,"alignment"), StringWithoutQuotes(script,"numbering"), this.indentLevel+1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"name"), Operator(script,"="), StringValueWithQuotes(script,"consensus", editable = true), this.indentLevel+1))
        this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"id"), Operator(script,"="), StringValueWithQuotes(script,"RF00072", editable = true), this.indentLevel+1))

        addButton.onAction = EventHandler {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is RfamKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, RfamKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is RfamKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is RfamKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

}