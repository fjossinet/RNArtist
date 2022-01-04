package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class DetailsKw(var parent:ThemeKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script, " details", indentLevel) {

    init {
        this.children.add(1, LocationKw(script, this.indentLevel + 1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"type"), Operator(script,"="), TypeField(script,"click me", listOf("helix", "single_strand", "junction")), this.indentLevel + 1))
        this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"value"), Operator(script,"="), StringWithoutQuotes(script, "1", editable = true), this.indentLevel+1))

        addButton.onAction = EventHandler {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is DetailsKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, DetailsKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is DetailsKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is DetailsKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

    fun setlevel(level:String) {
        val parameter = this.searchFirst { it is DSLParameter && "value".equals(it.key.text.text.trim())} as DSLParameter
        parameter.value.text.text = level
        this.addButton.fire()
    }

    fun getLocation(): Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.getLocation()

    fun setLocation(location:Location) {
        (this.searchFirst { it is LocationKw } as LocationKw).setLocation(location)
        this.addButton.fire()
    }

}