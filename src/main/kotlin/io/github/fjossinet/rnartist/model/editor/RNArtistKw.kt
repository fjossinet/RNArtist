package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.io.model.editor.ChimeraKw
import javafx.scene.Node
import java.util.*

class RNArtistKw(script: Script, indentLevel:Int = 0, val id:String= UUID.randomUUID().toString()): DSLKeyword(script, "rnartist ", indentLevel) {

    init {
        this.children.add(1, LayoutKw(script, this.indentLevel+1))
        this.children.add(1, ThemeKw(script, this.indentLevel+1))
        this.children.add(1, DataKw(script, this.indentLevel+1))
        this.children.add(1, SecondaryStructureInputKw(script, this.indentLevel+1))
        this.children.add(1, SVGKw(script, this.indentLevel+1))
        this.children.add(1, PNGKw(script, this.indentLevel+1))
        this.children.add(1, ChimeraKw(script, this.indentLevel+1))
    }

    fun getThemeKw():ThemeKw = this.searchFirst { it is ThemeKw } as ThemeKw

    fun getLayoutKw():LayoutKw = this.searchFirst { it is LayoutKw } as LayoutKw

    fun getSecondaryStructureKw():SecondaryStructureInputKw = this.searchFirst { it is SecondaryStructureInputKw } as SecondaryStructureInputKw

    override fun dumpNodes(nodes: MutableList<Node>, enterInCollapsedNode: Boolean) {
        (0 until indentLevel).forEach {
            nodes.add(ScriptTab(script).text)
        }
        nodes.add(this.text)
        this.children.forEach {
            it.dumpNodes(nodes, enterInCollapsedNode)
        }
    }
}