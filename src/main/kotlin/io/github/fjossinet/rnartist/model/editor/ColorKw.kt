package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.scene.paint.Color

class ColorKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " color", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"data"), DataOperatorField(editor,"gt"),
                FloatField(editor,"20.7"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"to"), Operator(editor,"="), ColorField(editor), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"type"), Operator(editor,"="), TypeField(editor,"click me"), this.indentLevel + 1))
            this.children.add(1, LocationKw(editor, this.indentLevel + 1))
            this.children.add(1, DSLParameter(editor, StringWithoutQuotes(editor,"value"), Operator(editor,"="), ColorField(editor), this.indentLevel + 1))
            this.children.add(ColorKw(editor, indentLevel))
        }
    }

    fun setTypes(types:String) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val parameter = this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter
        if (!parameter.inFinalScript)
            parameter.addButton.fire()
        parameter.value.text.text = "\"${types}\""
        if (!parameter.inFinalScript)
            parameter.addButton.fire()
    }

    fun getTypes():String? = (this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter?)?.value?.text?.text?.replace("\"","")

    fun setColor(color:String) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val parameter = this.searchFirst { it is DSLParameter && "value".equals(it.key.text.text.trim())} as DSLParameter
        parameter.value.text.text = "\"$color\""
        parameter.value.text.fill = Color.web(color)
    }

    fun getLocation(): Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.location

    fun setLocation(location: Location) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val l = (this.searchFirst { it is LocationKw } as LocationKw?)!!
        l.location = location
        if (!l.inFinalScript)
            l.addButton.fire()
    }

    fun removeLocation() {
        (this.searchFirst { it is LocationKw } as LocationKw?)?.removeButton?.fire()
    }
}