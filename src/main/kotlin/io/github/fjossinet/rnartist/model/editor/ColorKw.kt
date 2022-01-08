package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import javafx.scene.paint.Color

class ColorKw(var parent:ThemeKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script, " color ", indentLevel) {

    init {
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"data"), DataOperatorField(script,"gt"),
            FloatField(script), this.indentLevel + 1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"to"), Operator(script,"="), ColorField(script), this.indentLevel + 1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"type"), Operator(script,"="), TypeField(script), this.indentLevel + 1))
        this.children.add(1, LocationKw(script, this.indentLevel + 1))
        this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"value"), Operator(script,"="), ColorField(script), this.indentLevel + 1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is ColorKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, ColorKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased = {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is ColorKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is ColorKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

    fun setTypes(types:String) {
        val parameter = this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter
        parameter.value.text.text = "\"${types}\""
        parameter.addButton.fire()
        this.addButton.fire()
    }

    fun getTypes():String? = (this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter?)?.value?.text?.text?.replace("\"","")

    fun setColor(color:String) {
        val parameter = this.searchFirst { it is DSLParameter && "value".equals(it.key.text.text.trim())} as DSLParameter
        parameter.value.text.text = "\"$color\""
        parameter.value.text.fill = Color.web(color)
        this.addButton.fire()
    }

    fun getLocation(): Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.getLocation()

    fun setLocation(location:Location) {
        (this.searchFirst { it is LocationKw } as LocationKw).setLocation(location)
        this.addButton.fire()
    }

}