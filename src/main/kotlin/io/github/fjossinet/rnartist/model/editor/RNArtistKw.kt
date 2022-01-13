package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.Node
import javafx.scene.paint.Color
import java.util.*

class Root:DSLElementInt {
    override val children: MutableList<DSLElementInt>
        get() = TODO("Not yet implemented")
    override var fontName: String
        get() = TODO("Not yet implemented")
        set(value) {}
    override var fontSize: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var color: Color
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun increaseIndentLevel() {
        TODO("Not yet implemented")
    }

    override fun decreaseIndentLevel() {
        TODO("Not yet implemented")
    }

    override fun dumpNodes(nodes: MutableList<Node>) {
        TODO("Not yet implemented")
    }

    override fun dumpText(text: StringBuilder) {
        TODO("Not yet implemented")
    }

    override fun searchFirst(query: (DSLElementInt) -> Boolean): DSLElementInt? {
        TODO("Not yet implemented")
    }

    override fun searchAll(hits: MutableList<DSLElementInt>, query: (DSLElementInt) -> Boolean) {
        TODO("Not yet implemented")
    }
}

class RNArtistKw(script: Script, indentLevel:Int = 0, val id:String= UUID.randomUUID().toString()): DSLKeyword(Root(), script, "rnartist", indentLevel) {

    init {
        this.children.add(SecondaryStructureInputKw(this, script, this.indentLevel+1))
        this.children.add(DataKw(this, script, this.indentLevel+1))
        this.children.add(LayoutKw(this, script, this.indentLevel+1))
        this.children.add(ThemeKw(this, script, this.indentLevel+1))
        this.children.add(SVGKw(this, script, this.indentLevel+1))
        this.children.add(PNGKw(this, script, this.indentLevel+1))
        this.children.add(ChimeraKw(this, script, this.indentLevel+1))
    }

    fun getThemeKw():ThemeKw = this.searchFirst { it is ThemeKw } as ThemeKw

    fun getLayoutKw():LayoutKw = this.searchFirst { it is LayoutKw } as LayoutKw

    fun getSecondaryStructureKw():SecondaryStructureInputKw = this.searchFirst { it is SecondaryStructureInputKw } as SecondaryStructureInputKw

}