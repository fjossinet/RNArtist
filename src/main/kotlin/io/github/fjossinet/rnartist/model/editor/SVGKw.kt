package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class SVGKw (editor: Script, indentLevel:Int): OptionalDSLKeyword(editor,  " svg", indentLevel, false) {

    override fun addToFinalScript(add:Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"width"), Operator(script,"="), FloatField(script,"800.0"), this.indentLevel + 1))
            this.children.add(1, OptionalDSLParameter(script, null, StringWithoutQuotes(script,"height"), Operator(script,"="), FloatField(script,"800.0"), this.indentLevel + 1))
            this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"path"), Operator(script,"="), DirectoryField(script), this.indentLevel+1))
        } else {
        }
    }

}