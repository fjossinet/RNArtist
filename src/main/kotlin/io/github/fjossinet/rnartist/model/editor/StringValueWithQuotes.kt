package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.scene.paint.Color

class StringValueWithQuotes(script: Script, value:String, editable: Boolean = false):StringWithQuotes(script, value, editable) {

    init {
        this.text.fill = Color.web("4d4d4d")
    }



    override fun clone(): StringValueWithQuotes = StringValueWithQuotes(script, this.text.text.replace("\"", ""), this.editable)

}