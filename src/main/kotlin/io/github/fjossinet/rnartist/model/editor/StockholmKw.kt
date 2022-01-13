package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class StockholmKw(parent:SecondaryStructureInputKw, script: Script, indentLevel:Int): InputFileKw(parent, script,  "stockholm", indentLevel) {

    init {
        this.children.add(
            DSLParameter(this,
                script,
                StringWithoutQuotes(this, script, "file"),
                Operator(this, script, "="),
                FileField(this, script),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(this, script, "name"),
                Operator(this, script, "="),
                StringValueWithQuotes(this, script, "consensus", editable = true),
                this.indentLevel + 1
            )
        )

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is StockholmKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, StockholmKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is StockholmKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is StockholmKw)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }
}