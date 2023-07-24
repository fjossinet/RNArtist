package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.Targetable
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.animation.RotateTransition
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.Circle
import javafx.scene.shape.SVGPath
import javafx.util.Duration
import kotlin.random.Random


open abstract class RNArtistColorPicker(val mediator: Mediator) : VBox(), Targetable  {

    val colorWheelGroup = Group()
    val lineWidthKnobGroup = Group()
    val colorsFromWheel = mutableMapOf<Point2D, DoubleArray>()
    val colorWheel = Canvas(120.0, 120.0)
    val saturationBar = FlowPane()
    val brightnessBar = FlowPane()
    val lastColors = FlowPane()
    val targetsComboBox = ComboBox<String>()
    var restrictedToSelection = false
    val lineWheel = SVGPath()
    val currentLineWidth = Circle(0.0, 0.0, 7.0)
    var currentAngle = 0.0

    abstract var behaviors:Map<String, (e:DrawingElement) -> Boolean>

    init {
        this.mediator.colorsPickers.add(this)
        this.minWidth = 200.0
        this.prefWidth = 200.0
        this.maxWidth = 200.0
        this.isFillWidth = true
        this.padding = Insets(15.0, 0.0, 15.0, 0.0)

        val knobPane = Pane()
        knobPane.minWidth = 200.0
        knobPane.prefWidth = 200.0
        knobPane.maxWidth = 200.0
        knobPane.minHeight = 180.0
        knobPane.prefHeight = 180.0
        knobPane.maxHeight = 180.0
        this.children.add(knobPane)
        knobPane.children.add(colorWheelGroup)

        colorWheel.layoutX = 30.0
        colorWheel.layoutY = 25.0
        colorWheelGroup.children.add(colorWheel)
        var i = 0
        while ( i < 12) {
            var rotatedPoint = rotatePoint(
                java.awt.geom.Point2D.Double(90.0, 0.0),
                java.awt.geom.Point2D.Double(90.0, 85.0),
                30.0 * i
            )
            var c = Circle(rotatedPoint.x, rotatedPoint.y, if (i == 0 || i == 3 || i == 6 || i == 9) 5.0 else 2.5)
            c.fill = Color.WHITE
            colorWheelGroup.children.add(c)
            i++
        }

        this.lineWheel.content = "M139.247,92.223C140.811,86.39 150,81.337 150,75.016C150,68.663 140.811,63.61 139.247,57.777C137.651,51.75 143.028,42.788 139.965,37.509C139.815,37.249 139.576,37.06 139.395,36.826C139.265,36.659 139.132,36.492 138.984,36.335C135.108,32.183 126.016,31.967 122.041,27.96C121.747,27.668 121.486,27.373 121.259,27.049C117.961,22.701 117.513,14.499 113.589,10.92C113.503,10.841 113.409,10.771 113.32,10.698C113.054,10.478 112.823,10.212 112.524,10.037C107.647,7.224 99.723,11.515 93.766,11C93.548,10.981 93.328,10.957 93.121,10.924C92.817,10.879 92.514,10.831 92.222,10.755C86.39,9.191 81.338,0 75.017,0C68.662,0 63.611,9.191 57.778,10.755C57.488,10.831 57.187,10.877 56.885,10.924C56.678,10.957 56.458,10.981 56.239,11C50.304,11.514 42.356,7.224 37.509,10.037C35.749,11.048 34.575,12.873 33.663,14.991C31.773,19.357 30.893,25.059 27.992,27.96C23.987,31.967 14.919,32.183 11.022,36.335C10.874,36.492 10.741,36.659 10.61,36.826C10.427,37.06 10.187,37.25 10.037,37.509C6.974,42.788 12.351,51.75 10.753,57.777C9.189,63.61 0,68.663 0,75.016C0,81.338 9.189,86.39 10.753,92.223C12.351,98.25 6.974,107.212 10.037,112.491C10.188,112.749 10.429,112.94 10.612,113.175C10.742,113.341 10.874,113.506 11.021,113.662C14.916,117.815 23.986,118.031 27.992,122.04C28.253,122.301 28.514,122.593 28.775,122.92C32.04,127.264 32.46,135.482 36.422,139.07C36.515,139.153 36.613,139.226 36.707,139.304C36.976,139.522 37.208,139.788 37.509,139.963C42.357,142.776 50.31,138.482 56.245,138.998C56.461,139.019 56.676,139.041 56.88,139.073C57.184,139.119 57.486,139.167 57.777,139.245C63.611,140.809 68.661,150 75.016,150C81.338,150 86.39,140.809 92.222,139.245C92.514,139.167 92.82,139.12 93.125,139.073C93.329,139.041 93.544,139.019 93.759,138.998C99.716,138.48 107.644,142.776 112.524,139.963C114.284,138.952 115.425,137.127 116.338,135.009C118.226,130.643 119.106,124.941 122.041,122.04C126.017,118.031 135.111,117.815 138.987,113.662C139.131,113.506 139.263,113.341 139.393,113.175C139.576,112.94 139.814,112.749 139.964,112.491C143.028,107.212 137.651,98.251 139.247,92.223ZM132.567,74.984C132.567,94.633 122.692,112.003 107.636,122.398C98.35,128.818 87.14,132.565 75.017,132.565C59.506,132.565 45.427,126.406 35.066,116.402C24.18,105.942 17.435,91.244 17.435,74.984C17.435,55.367 27.309,37.997 42.365,27.602C51.653,21.182 62.894,17.435 75.018,17.435C90.528,17.435 104.574,23.594 114.937,33.598C125.82,44.058 132.567,58.756 132.567,74.984Z"
        this.lineWheel.layoutX = 15.0
        this.lineWheel.layoutY = 10.0
        this.lineWheel.stroke = Color.BLACK
        this.lineWheel.strokeWidth = 1.0

        var innerShadow = InnerShadow()
        innerShadow.offsetX = 0.0
        innerShadow.offsetY = 0.0
        innerShadow.color = Color.WHITE
        this.lineWheel.effect = innerShadow
        val radialGradient = RadialGradient(
            0.0,
            0.0,
            75.0,
            75.0,
            180.0,
            false,
            CycleMethod.NO_CYCLE,
            Stop(0.0, Color.DARKSLATEGREY),
            Stop(1.0, Color.WHITE))
        this.lineWheel.fill = radialGradient
        this.colorWheelGroup.onMouseClicked = EventHandler {
            var i = 0
            while (i <= 6) {
                var rotatedPoint = rotatePoint(
                    java.awt.geom.Point2D.Double(90.0, 18.0),
                    java.awt.geom.Point2D.Double(90.0, 85.0),
                    currentAngle - 30.0 * i
                )
                var c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                if (c.contains(Point2D(it.x, it.y))) {
                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                    rt.byAngle = - 30.0 * i
                    currentAngle -= 30.0 * i
                    currentAngle = currentAngle%360
                    rt.play()
                    val t = Theme()
                    t.setConfigurationFor(selection = behaviors[targetsComboBox.value]!!, ThemeParameter.linewidth) {
                        when (currentAngle) {
                            0.0 -> "0.0"
                            30.0, -330.0 -> "0.5"
                            60.0, -300.0 -> "1.0"
                            90.0, -270.0 -> "2.0"
                            120.0, -240.0 -> "3.0"
                            150.0, -210.0 -> "4.0"
                            180.0, -180.0 -> "5.0"
                            210.0, -150.0 -> "6.0"
                            240.0, -120.0 -> "7.0"
                            270.0, -90.0 -> "8.0"
                            300.0, -60.0 -> "9.0"
                            330.0, -30.0 -> "10.0"
                            else -> "1.0"
                        }
                    }
                    if (mediator.canvas2D.getSelection().isNotEmpty()) {
                        mediator.canvas2D.getSelection().forEach {
                            it.applyTheme(t)
                        }
                    } else {
                        mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
                    }
                    mediator.canvas2D.repaint()
                    break
                }
                rotatedPoint = rotatePoint(
                    java.awt.geom.Point2D.Double(90.0, 18.0),
                    java.awt.geom.Point2D.Double(90.0, 85.0),
                    currentAngle + 30.0 * i
                )
                c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                if (c.contains(Point2D(it.x, it.y))) {
                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                    rt.byAngle = 30.0 * i
                    currentAngle += 30.0 * i
                    currentAngle = currentAngle%360
                    rt.play()
                    val t = Theme()
                    t.setConfigurationFor(selection = behaviors[targetsComboBox.value]!!, ThemeParameter.linewidth) {
                        when (currentAngle) {
                            0.0 -> "0.0"
                            30.0, -330.0 -> "0.5"
                            60.0, -300.0 -> "1.0"
                            90.0, -270.0 -> "2.0"
                            120.0, -240.0 -> "3.0"
                            150.0, -210.0 -> "4.0"
                            180.0, -180.0 -> "5.0"
                            210.0, -150.0 -> "6.0"
                            240.0, -120.0 -> "7.0"
                            270.0, -90.0 -> "8.0"
                            300.0, -60.0 -> "9.0"
                            330.0, -30.0 -> "10.0"
                            else -> "1.0"
                        }
                    }
                    if (mediator.canvas2D.getSelection().isNotEmpty()) {
                        mediator.canvas2D.getSelection().forEach {
                            it.applyTheme(t)
                        }
                    } else {
                        mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
                    }
                    mediator.canvas2D.repaint()
                    break
                }
                i++
            }

        }
        lineWidthKnobGroup.children.add(this.lineWheel)

        this.currentLineWidth.fill = Color.WHITE
        this.currentLineWidth.strokeWidth = 1.0
        this.currentLineWidth.stroke = Color.BLACK
        innerShadow = InnerShadow()
        innerShadow.offsetX = 0.0
        innerShadow.offsetY = 0.0
        innerShadow.color = Color.BLACK
        this.currentLineWidth.effect = innerShadow
        this.currentLineWidth.layoutX = 90.0
        this.currentLineWidth.layoutY = 18.0
        this.currentLineWidth.onMousePressed = EventHandler {
            currentLineWidth.fill = Color.DARKORANGE
        }
        this.currentLineWidth.onMouseReleased = EventHandler {
            currentLineWidth.fill = Color.WHITE
        }
        lineWidthKnobGroup.children.add(this.currentLineWidth)

        colorWheelGroup.children.add(lineWidthKnobGroup)

        val optionsVBox = VBox()
        optionsVBox.padding = Insets(10.0)
        optionsVBox.spacing = 5.0
        optionsVBox.minWidth = 200.0
        optionsVBox.prefWidth = 200.0
        optionsVBox.maxWidth = 200.0
        this.children.add(optionsVBox)

        var l = Label("Saturation")
        optionsVBox.children.add(l)
        this.saturationBar.hgap = 5.0
        this.saturationBar.orientation = Orientation.HORIZONTAL
        this.saturationBar.maxWidth = 200.0
        optionsVBox.children.add(saturationBar)

        l = Label("Brightness")
        optionsVBox.children.add(l)
        this.brightnessBar.hgap = 5.0
        this.brightnessBar.orientation = Orientation.HORIZONTAL
        this.brightnessBar.maxWidth = 200.0
        optionsVBox.children.add(brightnessBar)

        l = Label("Last Colors")
        optionsVBox.children.add(l)
        this.lastColors.hgap = 5.0
        this.lastColors.orientation = Orientation.HORIZONTAL
        this.lastColors.maxWidth = 200.0
        (1..6).forEach {
            if (it == 1) {
                val color = doubleArrayOf(Random.nextDouble(0.0,360.0), Random.nextDouble(), 1.0)
                addLastColor(color)
                repaintBrightness(color)
                repaintSaturation(color)
            } else
                addLastColor(doubleArrayOf(Random.nextDouble(0.0,360.0), Random.nextDouble(), 1.0))
        }
        optionsVBox.children.add(lastColors)

        l = Label("Target")
        optionsVBox.children.add(l)
        this.targetsComboBox.minWidth = 150.0
        this.targetsComboBox.maxWidth = 150.0
        optionsVBox.children.add(this.targetsComboBox)

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

        colorWheel.graphicsContext2D.strokeOval(centerX-radius,centerX-radius, radius*2, radius*2)
        colorWheel.onMouseClicked = EventHandler { event ->
            colorsFromWheel.forEach {
                val color = Color.hsb(it.value[0], it.value[1], it.value[2])
                if (event.x >= it.key.x && event.x <= it.key.x + 1.0 && event.y >= it.key.y && event.y <= it.key.y + 1.0) {
                    val t = Theme()
                    t.setConfigurationFor(selection = behaviors[targetsComboBox.value]!!, ThemeParameter.color) {
                        getHTMLColorString(
                            javaFXToAwt(color)
                        )
                    }
                    //we update the last colors for all the color pickers
                    mediator.colorsPickers.forEach { colorPicker ->
                        colorPicker.addLastColor(it.value)
                    }
                    this.repaintBrightness(it.value)
                    this.repaintSaturation(it.value)
                    if (mediator.canvas2D.getSelection().isNotEmpty()) {
                        mediator.canvas2D.getSelection().forEach {
                            it.applyTheme(t)
                        }
                    } else {
                        mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
                    }
                    mediator.canvas2D.repaint()
                    return@EventHandler
                }
            }
        }

        mediator.targetables.add(this)
        this.targetsComboBox.onAction = EventHandler {
            //synchronization with the other targetable widgets
            mediator.targetables.forEach { it.setTarget(this.targetsComboBox.value) }
        }
    }

    fun addLastColor(c:DoubleArray) {
        val r = Circle(0.0, 0.0, 10.0)
        val color = Color.hsb(c[0], c[1], c[2])
        r.onMouseClicked = EventHandler { event ->
            val t = Theme()
            t.setConfigurationFor(selection = behaviors[targetsComboBox.value]!!, ThemeParameter.color) {
                getHTMLColorString(
                    javaFXToAwt(color)
                )
            }
            this.repaintBrightness(c)
            this.repaintSaturation(c)
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.getSelection().forEach {
                    it.applyTheme(t)
                }
            } else {
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            }
            mediator.canvas2D.repaint()
        }
        r.fill = color
        r.stroke = Color.BLACK
        r.strokeWidth = 1.0
        if (this.lastColors.children.size >= 6) {
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
        (0..5).reversed().forEach {
            val r = Circle(0.0, 0.0, 10.0)
            val color = Color.hsb(c[0], c[1], 0.2*it)
            r.onMouseClicked = EventHandler { event ->
                val t = Theme()
                t.setConfigurationFor(selection = behaviors[targetsComboBox.value]!!, ThemeParameter.color) {
                    getHTMLColorString(
                        javaFXToAwt(color)
                    )
                }
                if (mediator.canvas2D.getSelection().isNotEmpty()) {
                    mediator.canvas2D.getSelection().forEach {
                        it.applyTheme(t)
                    }
                } else {
                    mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
                }
                mediator.canvas2D.repaint()
            }
            r.fill = color
            r.stroke = Color.BLACK
            r.strokeWidth = 1.0
            this.brightnessBar.children.add(r)
        }
    }

    private fun repaintSaturation(c:DoubleArray) {
        this.saturationBar.children.clear()
        (0..5).reversed().forEach {
            val r = Circle(0.0, 0.0, 10.0)
            val color = Color.hsb(c[0], 0.2*it, c[2])
            r.onMouseClicked = EventHandler { event ->
                val t = Theme()
                t.setConfigurationFor(selection = behaviors[targetsComboBox.value]!!, ThemeParameter.color) {
                    getHTMLColorString(
                        javaFXToAwt(color)
                    )
                }
                if (mediator.canvas2D.getSelection().isNotEmpty()) {
                    mediator.canvas2D.getSelection().forEach {
                        it.applyTheme(t)
                    }
                } else {
                    mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
                }
                mediator.canvas2D.repaint()
            }
            r.fill = color
            r.stroke = Color.BLACK
            r.strokeWidth = 1.0
            this.saturationBar.children.add(r)
        }
    }

    override fun setTarget(target: String) {
        this.targetsComboBox.items.forEach {
            if (it.equals(target)) {
                this.targetsComboBox.value = it
                return
            }
        }

    }

}