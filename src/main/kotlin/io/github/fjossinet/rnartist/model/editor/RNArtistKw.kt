package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import java.util.*

class RNArtistKw(editor: ScriptEditor, indentLevel:Int = 0, val id:String= UUID.randomUUID().toString()): DSLKeyword(editor, "rnartist", indentLevel) {

    init {
        this.children.add(1, DataKw(editor, this.indentLevel+1))
        this.children.add(1, LayoutKw(editor, this.indentLevel+1))
        this.children.add(1, ThemeKw(editor, this.indentLevel+1, inFinalScript = true))
        this.children.add(1, SecondaryStructureKw(editor, this.indentLevel+1))
    }

}