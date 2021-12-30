package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import java.util.*

class RNArtistKw(editor: ScriptEditor, indentLevel:Int = 0, val id:String= UUID.randomUUID().toString()): DSLKeyword(editor, "rnartist", indentLevel) {

    init {
        this.children.add(1, LayoutKw(editor, this.indentLevel+1))
        this.children.add(1, ThemeKw(editor, this.indentLevel+1))
        this.children.add(1, DataKw(editor, this.indentLevel+1))
        this.children.add(1, SecondaryStructureKw(editor, this.indentLevel+1))
        this.children.add(1, SVGKw(editor, this.indentLevel+1))
        this.children.add(1, PNGKw(editor, this.indentLevel+1))
    }

    fun getThemeKw():ThemeKw? = this.searchFirst { it is ThemeKw } as ThemeKw?

    fun getLayoutKw():LayoutKw? = this.searchFirst { it is LayoutKw } as LayoutKw?

    fun getSecondaryStructureKw():SecondaryStructureKw = this.searchFirst { it is SecondaryStructureKw } as SecondaryStructureKw

}