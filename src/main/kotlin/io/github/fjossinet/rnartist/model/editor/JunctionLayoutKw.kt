package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script

class JunctionLayoutKw(val parent: LayoutKw, script: Script, indentLevel:Int): OptionalDSLKeyword(script, " junction ", indentLevel) {

    init {
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"out_ids"), Operator(script,"="), StringValueWithQuotes(script,"nnw nne", editable = true), this.indentLevel + 1))
        this.children.add(1, LocationKw(script, this.indentLevel + 1))
        this.children.add(1, OptionalDSLParameter(this, script, null, StringWithoutQuotes(script,"type"), Operator(script,"="), StringWithoutQuotes(script, editable = true), this.indentLevel + 1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is JunctionLayoutKw)
                this.parent.children.add(this.parent.children.indexOf(this)+1, JunctionLayoutKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this)+1)
            if (childAfter is JunctionLayoutKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is JunctionLayoutKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
            script.initScript()
        }
    }

    fun setOutIds(outIds:String) {
        val parameter = this.searchFirst { it is OptionalDSLParameter && "out_ids".equals(it.key.text.text.trim())} as OptionalDSLParameter
        parameter.value.text.text = outIds
        parameter.addButton.fire()
        this.addButton.fire()
    }

    fun getLocation(): Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.getLocation()

    fun setLocation(location:Location) {
        (this.searchFirst { it is LocationKw } as LocationKw).setLocation(location)
        this.addButton.fire()
    }

    fun setType(type: String) {
        val parameter = this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter
        parameter.value.text.text = type
        parameter.addButton.fire()
        this.addButton.fire()

    }

}