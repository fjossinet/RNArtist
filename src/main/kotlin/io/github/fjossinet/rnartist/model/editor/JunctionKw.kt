package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class JunctionKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " junction", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"out_ids"), Operator(editor,"="), StringValueWithQuotes(editor,"nnw nne", editable = true), this.indentLevel + 1))
            this.children.add(1, LocationKw(editor, this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(editor, null, StringWithoutQuotes(editor,"type"), Operator(editor,"="), IntegerField(editor,3), this.indentLevel + 1))
            this.children.add(JunctionKw(editor, indentLevel))
        }
    }

    fun setOutIds(outIds:String) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val parameter = this.searchFirst { it is OptionalDSLParameter && "out_ids".equals(it.key.text.text.trim())} as OptionalDSLParameter
        if (!parameter.inFinalScript)
            parameter.addButton.fire()
        parameter.value.text.text = outIds
        if (!this.inFinalScript)
            this.addButton.fire()
    }

    fun getLocation():Location? = (this.searchFirst { it is LocationKw } as LocationKw?)?.location

    /**
     * No argument needed. The function addToFinalScript (fired with the button) uses the current selection
     */
    fun setLocation() {
        if (!this.inFinalScript)
            this.addButton.fire()
        val l = (this.searchFirst { it is LocationKw } as LocationKw?)!!
        if (editor.mediator.canvas2D.getSelection().isNotEmpty() && !l.inFinalScript) //if there is a selection, the location needs to be added to the script
            l.addButton.fire()
    }

    fun setType(type: String) {
        if (!this.inFinalScript)
            this.addButton.fire()
        val parameter = this.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text.trim())} as OptionalDSLParameter
        if (!parameter.inFinalScript)
            parameter.addButton.fire()
        parameter.value.text.text = type
    }

}