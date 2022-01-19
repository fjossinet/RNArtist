package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script

class JunctionLayoutKw(parent: LayoutKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "junction", indentLevel) {

    init {
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"out_ids"), Operator(this, script,"="), StringValueWithQuotes(this, script,"nnw nne", editable = true), this.indentLevel + 1))
        this.children.add(LocationKw(this, script, this.indentLevel + 1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"type"), Operator(this, script,"="), StringWithoutQuotes(this, script, editable = true), this.indentLevel + 1))
        this.children.add(OptionalDSLParameter(this, script, null, StringWithoutQuotes(this, script,"radius"), Operator(this, script,"="), FloatField(this, script), this.indentLevel + 1))

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is JunctionLayoutKw)
                this.parent.children.add(this.parent.children.indexOf(this)+1, JunctionLayoutKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            if (this.parent.children.indexOf(this) <this.parent.children.size-1 && this.parent.children.get(this.parent.children.indexOf(this) + 1) is JunctionLayoutKw)
                this.parent.children.remove(this)
            else if (this.parent.children.indexOf(this) > 0) {
                val previous = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (previous is JunctionLayoutKw && !previous.inFinalScript)
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

    fun setRadius(radius:Double) {
        val parameter = this.searchFirst { it is OptionalDSLParameter && "radius".equals(it.key.text.text.trim())} as OptionalDSLParameter
        parameter.value.text.text = radius.toString()
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