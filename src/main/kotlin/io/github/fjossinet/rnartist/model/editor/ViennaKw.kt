package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class ViennaKw(val parent:SecondaryStructureInputKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script,  " vienna ", indentLevel) {

    init {
        this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"file"), Operator(script,"="), FileField(script), this.indentLevel+1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is ViennaKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, ViennaKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is ViennaKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is ViennaKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

}