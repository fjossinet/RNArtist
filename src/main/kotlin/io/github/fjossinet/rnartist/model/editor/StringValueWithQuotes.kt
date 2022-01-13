package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.paint.Color

class StringValueWithQuotes(parent:DSLElementInt, script: Script, value:String = "", editable: Boolean = false):StringWithQuotes(parent, script, value, editable) {

    init {
        this.text.fill = Color.web("4d4d4d")
    }

    override fun clone(): StringValueWithQuotes = StringValueWithQuotes(this.parent, script, "", this.editable)


}