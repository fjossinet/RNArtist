package io.github.fjossinet.rnartist.gui.editor

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.model.editor.*
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import kotlin.random.Random

class ScriptEditorColorPicker(val mediator: Mediator) : VBox()  {

    val colorWheelPanel = Group()
    val colorsFromWheel = mutableMapOf<Point2D, DoubleArray>()
    val colorWheel = Canvas(120.0, 120.0)
    val saturationBar = FlowPane()
    val brightnessBar = FlowPane()
    val lastColors = FlowPane()
    val targetsComboBox = ComboBox<String>()

    init {
        this.minWidth = 120.0
        this.prefWidth = 120.0
        this.maxWidth = 120.0
        this.isFillWidth = true
        this.padding = Insets(10.0, 0.0, 10.0, 0.0)
        this.spacing = 3.0

        val labels = listOf("Background", "Keywords", "Braces", "Keys", "Operators", "Values")
        this.targetsComboBox.items.addAll(labels)
        this.targetsComboBox.value = labels.first()

        colorWheel.layoutX = 0.0
        colorWheel.layoutY = 0.0
        colorWheelPanel.children.add(colorWheel)

        this.children.add(colorWheelPanel)

        var l = Label("Saturation")
        this.children.add(l)
        this.saturationBar.hgap = 3.0
        this.saturationBar.orientation = Orientation.HORIZONTAL
        this.saturationBar.maxWidth = 120.0
        this.children.add(saturationBar)

        l = Label("Brightness")
        this.children.add(l)
        this.brightnessBar.hgap = 3.0
        this.brightnessBar.orientation = Orientation.HORIZONTAL
        this.brightnessBar.maxWidth = 120.0
        this.children.add(brightnessBar)

        l = Label("Last Colors")
        this.children.add(l)
        this.lastColors.hgap = 3.0
        this.lastColors.orientation = Orientation.HORIZONTAL
        this.lastColors.maxWidth = 120.0
        (1..5).forEach {
            if (it == 1) {
                val color = doubleArrayOf(Random.nextDouble(0.0,360.0), Random.nextDouble(), 1.0)
                addLastColor(color)
                repaintBrightness(color)
                repaintSaturation(color)
            } else
                addLastColor(doubleArrayOf(Random.nextDouble(0.0,360.0), Random.nextDouble(), 1.0))
        }
        this.children.add(lastColors)

        l = Label("Target")
        this.children.add(l)
        this.targetsComboBox.minWidth = 120.0
        this.targetsComboBox.maxWidth = 120.0
        this.children.add(this.targetsComboBox)

        var xOffset: Double
        var yOffset: Double
        val centerX = 60.0
        val centerY = 60.0
        val radius = 60.0
        var centreOffset: Double
        var centreAngle: Double
        for (x in 0 until 120) {
            for (y in 0 until 120) {
                xOffset = x - centerX
                yOffset = y - centerY
                centreOffset = Math.hypot(xOffset, yOffset)
                if (centreOffset <= radius) {
                    centreAngle = (Math.toDegrees(Math.atan2((yOffset), (xOffset))) + 360.0) % 360.0
                    val color = Color.hsb(centreAngle, (centreOffset / radius), 1.0)
                    colorsFromWheel[Point2D(x.toDouble(),y.toDouble())] = doubleArrayOf(centreAngle, (centreOffset / radius), 1.0)
                    colorWheel.graphicsContext2D.fill = color
                    colorWheel.graphicsContext2D.fillOval(x.toDouble()-0.5,y.toDouble()-0.5, 1.0, 1.0)
                }
            }
        }
        colorWheel.graphicsContext2D.stroke = Color.BLACK
        colorWheel.graphicsContext2D.lineWidth = 1.0
        colorWheel.graphicsContext2D.strokeOval(centerX-radius,centerX-radius, radius*2, radius*2)
        this.onMouseClicked = EventHandler { event ->
            colorsFromWheel.forEach {
                val color = Color.hsb(it.value[0], it.value[1], it.value[2])
                if (event.x >= it.key.x && event.x <= it.key.x + 1.0 && event.y >= it.key.y && event.y <= it.key.y + 1.0) {
                    when (targetsComboBox.value) {
                        "Background" -> {
                            mediator.scriptEditor.script.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(color))}"
                            RnartistConfig.backgroundEditorColor = javaFXToAwt(color)
                        }
                        "Keywords" -> {
                            val hits = mutableListOf<DSLElementInt>()
                            mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLKeyword }
                            hits.forEach {
                                it.color = color
                                (it as DSLKeyword).collapseButton.setColor(color)
                                (it as? OptionalDSLKeyword)?.addButton?.setColor(color)
                            }
                            RnartistConfig.keywordEditorColor = javaFXToAwt(color)
                            mediator.scriptEditor.script.initScript()
                        }
                        "Braces" -> {
                            val hits = mutableListOf<DSLElementInt>()
                            mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is OpenedCurly || it is ClosedCurly }
                            hits.forEach {
                                it.color = color
                            }
                            RnartistConfig.bracesEditorColor = javaFXToAwt(color)
                            mediator.scriptEditor.script.initScript()
                        }
                        "Keys" -> {
                            val hits = mutableListOf<DSLElementInt>()
                            mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                            hits.forEach {
                                (it as DSLParameter).key.color = color
                                (it as? OptionalDSLParameter)?.addButton?.setColor(color)
                            }
                            RnartistConfig.keyParamEditorColor = javaFXToAwt(color)
                            mediator.scriptEditor.script.initScript()
                        }
                        "Operators" -> {
                            val hits = mutableListOf<DSLElementInt>()
                            mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                            hits.forEach {
                                (it as DSLParameter).operator.color = color
                            }
                            RnartistConfig.operatorParamEditorColor = javaFXToAwt(color)
                            mediator.scriptEditor.script.initScript()
                        }
                        "Values" -> {
                            val hits = mutableListOf<DSLElementInt>()
                            mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                            hits.forEach {
                                (it as DSLParameter).value.color = color
                            }
                            RnartistConfig.valueParamEditorColor = javaFXToAwt(color)
                            mediator.scriptEditor.script.initScript()
                        }
                    }
                    this.addLastColor(it.value)
                    this.repaintBrightness(it.value)
                    this.repaintSaturation(it.value)
                }
            }
        }
    }

    fun addLastColor(c:DoubleArray) {
        val r = Circle(0.0, 0.0, 10.0)
        val color = Color.hsb(c[0], c[1], c[2])
        r.onMouseClicked = EventHandler { event ->
            when (targetsComboBox.value) {
                "Background" -> {
                    mediator.scriptEditor.script.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(color))}"
                    RnartistConfig.backgroundEditorColor = javaFXToAwt(color)
                }
                "Keywords" -> {
                    val hits = mutableListOf<DSLElementInt>()
                    mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLKeyword }
                    hits.forEach {
                        it.color = color
                        (it as DSLKeyword).collapseButton.setColor(color)
                        (it as? OptionalDSLKeyword)?.addButton?.setColor(color)
                    }
                    RnartistConfig.keywordEditorColor = javaFXToAwt(color)
                    mediator.scriptEditor.script.initScript()
                }
                "Braces" -> {
                    val hits = mutableListOf<DSLElementInt>()
                    mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is OpenedCurly || it is ClosedCurly }
                    hits.forEach {
                        it.color = color
                    }
                    RnartistConfig.bracesEditorColor = javaFXToAwt(color)
                    mediator.scriptEditor.script.initScript()
                }
                "Keys" -> {
                    val hits = mutableListOf<DSLElementInt>()
                    mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                    hits.forEach {
                        (it as DSLParameter).key.color = color
                        (it as? OptionalDSLParameter)?.addButton?.setColor(color)
                    }
                    RnartistConfig.keyParamEditorColor = javaFXToAwt(color)
                    mediator.scriptEditor.script.initScript()
                }
                "Operators" -> {
                    val hits = mutableListOf<DSLElementInt>()
                    mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                    hits.forEach {
                        (it as DSLParameter).operator.color = color
                    }
                    RnartistConfig.operatorParamEditorColor = javaFXToAwt(color)
                    mediator.scriptEditor.script.initScript()
                }
                "Values" -> {
                    val hits = mutableListOf<DSLElementInt>()
                    mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                    hits.forEach {
                        (it as DSLParameter).value.color = color
                    }
                    RnartistConfig.valueParamEditorColor = javaFXToAwt(color)
                    mediator.scriptEditor.script.initScript()
                }
            }
            this.repaintBrightness(c)
            this.repaintSaturation(c)
        }
        r.fill = color
        r.stroke = Color.BLACK
        r.strokeWidth = 1.0
        if (this.lastColors.children.size >= 5) {
            val children = mutableListOf<Node>()
            this.lastColors.children.forEach {
                children.add(it)
            }
            children.removeAt(children.size-1)
            children.add(0, r)
            this.lastColors.children.clear()
            this.lastColors.children.addAll(*children.toTypedArray())
        }
        else
            this.lastColors.children.add(r)
    }

    private fun repaintBrightness(c:DoubleArray) {
        this.brightnessBar.children.clear()
        (0..4).reversed().forEach {
            val r = Circle(0.0, 0.0, 10.0)
            val color = Color.hsb(c[0], c[1], 0.25*it)
            r.onMouseClicked = EventHandler { event ->
                when (targetsComboBox.value) {
                    "Background" -> {
                        mediator.scriptEditor.script.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(color))}"
                        RnartistConfig.backgroundEditorColor = javaFXToAwt(color)
                    }
                    "Keywords" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLKeyword }
                        hits.forEach {
                            it.color = color
                            (it as DSLKeyword).collapseButton.setColor(color)
                            (it as? OptionalDSLKeyword)?.addButton?.setColor(color)
                        }
                        RnartistConfig.keywordEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Braces" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is OpenedCurly || it is ClosedCurly }
                        hits.forEach {
                            it.color = color
                        }
                        RnartistConfig.bracesEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Keys" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                        hits.forEach {
                            (it as DSLParameter).key.color = color
                            (it as? OptionalDSLParameter)?.addButton?.setColor(color)
                        }
                        RnartistConfig.keyParamEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Operators" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                        hits.forEach {
                            (it as DSLParameter).operator.color = color
                        }
                        RnartistConfig.operatorParamEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Values" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                        hits.forEach {
                            (it as DSLParameter).value.color = color
                        }
                        RnartistConfig.valueParamEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                }
            }
            r.fill = color
            r.stroke = Color.BLACK
            r.strokeWidth = 1.0
            this.brightnessBar.children.add(r)
        }
    }

    private fun repaintSaturation(c:DoubleArray) {
        this.saturationBar.children.clear()
        (0..4).reversed().forEach {
            val r = Circle(0.0, 0.0, 10.0)
            val color = Color.hsb(c[0], 0.25*it, c[2])
            r.onMouseClicked = EventHandler { event ->
                when (targetsComboBox.value) {
                    "Background" -> {
                        mediator.scriptEditor.script.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(color))}"
                        RnartistConfig.backgroundEditorColor = javaFXToAwt(color)
                    }
                    "Keywords" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLKeyword }
                        hits.forEach {
                            it.color = color
                            (it as DSLKeyword).collapseButton.setColor(color)
                            (it as? OptionalDSLKeyword)?.addButton?.setColor(color)
                        }
                        RnartistConfig.keywordEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Braces" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is OpenedCurly || it is ClosedCurly }
                        hits.forEach {
                            it.color = color
                        }
                        RnartistConfig.bracesEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Keys" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                        hits.forEach {
                            (it as DSLParameter).key.color = color
                            (it as? OptionalDSLParameter)?.addButton?.setColor(color)
                        }
                        RnartistConfig.keyParamEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Operators" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                        hits.forEach {
                            (it as DSLParameter).operator.color = color
                        }
                        RnartistConfig.operatorParamEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                    "Values" -> {
                        val hits = mutableListOf<DSLElementInt>()
                        mediator.scriptEditor.script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
                        hits.forEach {
                            (it as DSLParameter).value.color = color
                        }
                        RnartistConfig.valueParamEditorColor = javaFXToAwt(color)
                        mediator.scriptEditor.script.initScript()
                    }
                }
            }
            r.fill = color
            r.stroke = Color.BLACK
            r.strokeWidth = 1.0
            this.saturationBar.children.add(r)
        }
    }

}