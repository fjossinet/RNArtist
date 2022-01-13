package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class HideKw(parent:ThemeKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "hide", indentLevel) {

    init {
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"data"), DataOperatorField(this, script,"gt"), FloatField(this, script), this.indentLevel + 1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"type"), Operator(this, script,"="), TypeField(this, script), this.indentLevel + 1))
        this.children.add(LocationKw(this, script, this.indentLevel + 1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is HideKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, HideKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is HideKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is HideKw)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

    fun getLocation(): Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.getLocation()

    fun setLocation(location: Location) {
        (this.searchFirst { it is LocationKw } as LocationKw).setLocation(location)
        this.addButton.fire()
    }

    fun setTypes(types:String) {
        val parameter = this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter
        parameter.value.text.text = "\"${types}\""
        parameter.addButton.fire()
        this.addButton.fire()
    }
}