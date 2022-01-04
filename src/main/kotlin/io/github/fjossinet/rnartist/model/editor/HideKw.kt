package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class HideKw(var parent:ThemeKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script, " hide", indentLevel) {

    init {
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"data"), DataOperatorField(script,"gt"), FloatField(script,"20.7"), this.indentLevel + 1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"type"), Operator(script,"="), TypeField(script,"click me"), this.indentLevel + 1))
        this.children.add(1, LocationKw(script, this.indentLevel + 1))

        addButton.onAction = EventHandler {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is HideKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, HideKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is HideKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is HideKw && !childBefore.inFinalScript)
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
}