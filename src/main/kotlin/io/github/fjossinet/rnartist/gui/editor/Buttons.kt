package io.github.fjossinet.rnartist.gui.editor

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.SVGPath
import javafx.scene.text.Font
import javafx.scene.text.Text

class Collapse(val script: Script): Region() {

    private val COLLAPSED_WIDTH = 10.0
    private val COLLAPSED_HEIGHT = 15.0
    private val EXPANDED_WIDTH = 15.0
    private val EXPANDED_HEIGHT = 10.0
    val svg = SVGPath()
    var collapsed = false
        set(value) {
            field = value
            this.svg.content = if (collapsed) "M10.7071,7.29289 C11.0976,7.68342 11.0976,8.31658 10.7071,8.70711 L7.70711,11.7071 C7.31658,12.0976 6.68342,12.0976 6.29289,11.7071 C5.90237,11.3166 5.90237,10.6834 6.29289,10.2929 L8.58579,8 L6.29289,5.70711 C5.90237,5.31658 5.90237,4.68342 6.29289,4.29289 C6.68342,3.90237 7.31658,3.90237 7.70711,4.29289 L10.7071,7.29289 Z"
                                else "M7.29289,10.7071 C7.68342,11.0976 8.31658,11.0976 8.70711,10.7071 L11.7071,7.70711 C12.0976,7.31658 12.0976,6.68342 11.7071,6.29289 C11.3166,5.90237 10.6834,5.90237 10.2929,6.29289 L8,8.58579 L5.70711,6.29289 C5.31658,5.90237 4.68342,5.90237 4.29289,6.29289 C3.90237,6.68342 3.90237,7.31658 4.29289,7.70711 L7.29289,10.7071 Z"
            this.setMinSize(if (collapsed) COLLAPSED_WIDTH else EXPANDED_WIDTH, if (collapsed) COLLAPSED_HEIGHT else EXPANDED_HEIGHT)
            this.setPrefSize(if (collapsed) COLLAPSED_WIDTH else EXPANDED_WIDTH, if (collapsed) COLLAPSED_HEIGHT else EXPANDED_HEIGHT)
            this.setMaxSize(if (collapsed) COLLAPSED_WIDTH else EXPANDED_WIDTH, if (collapsed) COLLAPSED_HEIGHT else EXPANDED_HEIGHT)
        }


    init {
        this.isCenterShape = true
        this.collapsed = this.collapsed
        this.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.keywordEditorColor)};"
        this.shape = svg

        this.onMousePressed = EventHandler {
            this.style = "-fx-background-color: gray;"
        }

        this.onMouseReleased = EventHandler {
            this.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.keywordEditorColor)};"
            fire()
        }

    }

    fun fire() {
        this.collapsed = !collapsed
        script.initScript()
    }

    fun setColor(color:Color) {
        this.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(color))};"
    }

}

class AddKeyWord(val script: Script, label:String): Region() {

    private val REQUIRED_WIDTH = 20.0
    private val REQUIRED_HEIGHT = 20.0
    val innerRegion = Region()
    val text:Text = Text()
    var mouseReleased: (() -> Unit)? = null

    init {
        val hbox = HBox()
        hbox.spacing = 5.0
        hbox.alignment = Pos.CENTER
        this.children.add(hbox)
        innerRegion.isCenterShape = true
        val svg = SVGPath()
        svg.content = "M37.059,16H26V4.941C26,2.224,23.718,0,21,0s-5,2.224-5,4.941V16H4.941C2.224,16,0,18.282,0,21s2.224,5,4.941,5H16v11.059C16,39.776,18.282,42,21,42s5-2.224,5-4.941V26h11.059C39.776,26,42,23.718,42,21S39.776,16,37.059,16z"
        innerRegion.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.keywordEditorColor)};"
        innerRegion.shape = svg
        innerRegion.setMinSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        innerRegion.setPrefSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        innerRegion.setMaxSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)

        innerRegion.onMousePressed = EventHandler {

        }

        innerRegion.onMouseReleased = EventHandler {
            fire()
        }
        text.text = label
        text.fill = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        text.font = Font.font(RnartistConfig.editorFontName, RnartistConfig.editorFontSize.toDouble())
        hbox.children.addAll(innerRegion, text)

    }

    fun fire() {
        mouseReleased?.invoke()
    }

    fun setColor(color:Color) {
        innerRegion.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(color))};"
        text.fill = color
    }

    fun setFontName(fontName:String) {
        text.font =  Font.font(fontName, text.font.size)
    }

    fun setFontSize(fontSize:Int) {
        text.font =  Font.font(text.font.name, fontSize.toDouble())
    }

}

class AddParameter(val script: Script, label:String): Region() {

    private val REQUIRED_WIDTH = 20.0
    private val REQUIRED_HEIGHT = 20.0
    val innerRegion = Region()
    val text = Text()
    var mouseReleased: (() -> Unit)? = null

    init {
        val hbox = HBox()
        hbox.spacing = 5.0
        hbox.alignment = Pos.CENTER
        this.children.add(hbox)
        innerRegion.isCenterShape = true
        val svg = SVGPath()
        svg.content = "M37.059,16H26V4.941C26,2.224,23.718,0,21,0s-5,2.224-5,4.941V16H4.941C2.224,16,0,18.282,0,21s2.224,5,4.941,5H16v11.059C16,39.776,18.282,42,21,42s5-2.224,5-4.941V26h11.059C39.776,26,42,23.718,42,21S39.776,16,37.059,16z"
        innerRegion.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.keyParamEditorColor)};"
        innerRegion.shape = svg
        innerRegion.setMinSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        innerRegion.setPrefSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        innerRegion.setMaxSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)

        innerRegion.onMousePressed = EventHandler {

        }

        innerRegion.onMouseReleased = EventHandler {
            fire()
        }
        text.text = label
        text.fill = awtColorToJavaFX(RnartistConfig.keyParamEditorColor)
        text.font = Font.font(RnartistConfig.editorFontName, RnartistConfig.editorFontSize.toDouble())
        hbox.children.addAll(innerRegion, text)

    }

    fun fire() {
        mouseReleased?.invoke()
    }

    fun setColor(color:Color) {
        innerRegion.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(color))};"
        text.fill = color
    }

    fun setFontName(fontName:String) {
        text.font =  Font.font(fontName, text.font.size)
    }

    fun setFontSize(fontSize:Int) {
        text.font =  Font.font(text.font.name, fontSize.toDouble())
    }

}

class Remove(val script: Script): Region() {

    private val REQUIRED_WIDTH = 20.0
    private val REQUIRED_HEIGHT = 20.0
    var mouseReleased: (() -> Unit)? = null

    init {
        this.isCenterShape = true
        val svg = SVGPath()
        svg.content = "M17.414 6.586c-.78-.781-2.048-.781-2.828 0l-2.586 2.586-2.586-2.586c-.78-.781-2.048-.781-2.828 0-.781.781-.781 2.047 0 2.828l2.585 2.586-2.585 2.586c-.781.781-.781 2.047 0 2.828.39.391.902.586 1.414.586s1.024-.195 1.414-.586l2.586-2.586 2.586 2.586c.39.391.902.586 1.414.586s1.024-.195 1.414-.586c.781-.781.781-2.047 0-2.828l-2.585-2.586 2.585-2.586c.781-.781.781-2.047 0-2.828z"
        this.style = "-fx-background-color: red;"
        this.shape = svg
        this.setMinSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        this.setPrefSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        this.setMaxSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)

        this.onMousePressed = EventHandler {

        }

        this.onMouseReleased = EventHandler {
            fire()
        }


    }

    fun fire() {
        this.mouseReleased?.invoke()
    }

}

open class DataField(val script: Script): Region() {

    private val REQUIRED_WIDTH = 30.0
    private val REQUIRED_HEIGHT = 15.0

    init {
        this.isCenterShape = true
        val svg = SVGPath()
        //svg.content = "M253.254,420.02L58.883,225.617h127.578c2.569-19.24,6.3-74.921-28.166-117.694C127.838,70.109,75.132,50.942,1.666,50.942L0,33.547c1.252-0.228,31.214-5.926,72.962-5.926c88.106,0,236.631,25.955,252.083,197.996h122.595L253.254,420.02z"
        svg.content = "m 480.6,341.4 c -11.3,0 -20.4,9.1 -20.4,20.4 v 98.4 H 51.8 v -98.4 c 0,-11.3 -9.1,-20.4 -20.4,-20.4 -11.3,0 -20.4,9.1 -20.4,20.4 v 118.8 c 0,11.3 9.1,20.4 20.4,20.4 h 449.2 c 11.3,0 20.4,-9.1 20.4,-20.4 V 361.8 c 0,-11.3 -9.1,-20.4 -20.4,-20.4 z"
        this.style = "-fx-background-color: red;"
        this.shape = svg
        this.setMinSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        this.setPrefSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)
        this.setMaxSize(REQUIRED_WIDTH, REQUIRED_HEIGHT)

    }

}
