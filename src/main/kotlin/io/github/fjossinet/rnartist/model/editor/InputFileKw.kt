package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

abstract class InputFileKw(parent:SecondaryStructureInputKw, script: Script, text:String, indentLevel:Int): OptionalDSLKeyword(parent, script,  text, indentLevel) {

    open fun getFileField(): FileField {
        return (this.searchFirst { it is DSLParameter && "file".equals(it.key.text.text.trim()) } as DSLParameter).value as FileField
    }
}