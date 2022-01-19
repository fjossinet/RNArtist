package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.paint.Color

class ColorKw(parent:ThemeKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "color", indentLevel) {

    init {
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"value"), Operator(this, script,"="), ColorField(this, script), this.indentLevel + 1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"scheme"), Operator(this, script,"="), ColorSchemeField(this, script), this.indentLevel + 1))
        this.children.add(LocationKw(this, script, this.indentLevel + 1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"type"), Operator(this, script,"="), TypeField(this, script), this.indentLevel + 1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"to"), Operator(this, script,"="), ColorField(this, script), this.indentLevel + 1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"data"), DataOperatorField(this, script,"gt"),
            FloatField(this, script), this.indentLevel + 1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is ColorKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, ColorKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased = {
            this.inFinalScript = false
            if (this.parent.children.indexOf(this) <this.parent.children.size-1 && this.parent.children.get(this.parent.children.indexOf(this) + 1) is ColorKw)
                this.parent.children.remove(this)
            else if (this.parent.children.indexOf(this) > 0) {
                val previous = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (previous is ColorKw && !previous.inFinalScript)
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
        val parameter = this.searchFirst { it is OptionalDSLParameter && "value".equals(it.key.text.text.trim())} as OptionalDSLParameter
        parameter.value.text.text = "\"$color\""
        parameter.value.text.fill = Color.web(color)
        parameter.addButton.fire()
        this.addButton.fire()
    }

    fun getLocation(): Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.getLocation()

    fun setLocation(location:Location) {
        (this.searchFirst { it is LocationKw } as LocationKw).setLocation(location)
        this.addButton.fire()
    }

}