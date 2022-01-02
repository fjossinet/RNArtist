package io.github.fjossinet.rnartist.gui.editor

import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.text.Font
import javafx.scene.text.FontPosture

class Button(val editor: Script, s:String?, n: Node?): Button(s,n) {
    init {
        this.minHeight = 30.0
        this.prefHeight = 30.0
        this.font =  Font.font("Helvetica", FontPosture.REGULAR, 15.0)
    }

    override fun getBaselineOffset(): Double {
        return 20.0
    }
}