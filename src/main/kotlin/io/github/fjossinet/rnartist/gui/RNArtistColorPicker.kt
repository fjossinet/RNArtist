package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.animation.RotateTransition
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.Circle
import javafx.scene.shape.SVGPath
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import kotlin.random.Random


open abstract class RNArtistColorPicker(val mediator: Mediator) : VBox()  {

    val colorWheelGroup = Group()
    val lineWidthKnobGroup = Group()
    val colorsFromWheel = mutableMapOf<Point2D, DoubleArray>()
    val colorWheel = Canvas(120.0, 120.0)
    val saturationBar = HBox()
    val brightnessBar = HBox()
    val lastColors = HBox()
    val targetsComboBox = ComboBox<String>()
    val lineWheel = SVGPath()
    val currentLineWidth = Circle(0.0, 0.0, 7.0)
    var currentAngle = 0.0
    var applyOnFont = false
    var lastNonFontColor: Color? = null
    var lastFontColor: Color? = null
    var lastLineWidth: Double? = null

    init {
        this.alignment = Pos.TOP_CENTER
        this.mediator.colorsPickers.add(this)
        this.isFillWidth = true
        this.padding = Insets(15.0, 0.0, 15.0, 0.0)

        val knobPane = Pane()
        knobPane.minHeight = 180.0
        knobPane.prefHeight = 180.0
        knobPane.maxHeight = 180.0
        val hbox = HBox()
        hbox.alignment = Pos.TOP_CENTER
        hbox.children.add(knobPane)
        this.children.add(hbox)
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
        optionsVBox.alignment = Pos.TOP_LEFT
        optionsVBox.padding = Insets(20.0, 0.0, 0.0, 0.0)
        optionsVBox.spacing = 5.0
        this.children.add(optionsVBox)

        var l = Label("Saturation")
        optionsVBox.children.add(l)
        saturationBar.spacing = 5.0
        optionsVBox.children.add(saturationBar)

        l = Label("Brightness")
        optionsVBox.children.add(l)
        brightnessBar.spacing = 5.0
        optionsVBox.children.add(brightnessBar)

        l = Label("Last Colors")
        optionsVBox.children.add(l)
        (1..6).forEach {
            if (it == 1) {
                val color = doubleArrayOf(Random.nextDouble(0.0,360.0), Random.nextDouble(), 1.0)
                addLastColor(color)
                repaintBrightness(color)
                repaintSaturation(color)
            } else
                addLastColor(doubleArrayOf(Random.nextDouble(0.0,360.0), Random.nextDouble(), 1.0))
        }
        lastColors.spacing = 5.0
        optionsVBox.children.add(lastColors)

        l = Label("Target")
        //optionsVBox.children.add(l)
        this.targetsComboBox.minWidth = 150.0
        this.targetsComboBox.maxWidth = 150.0
        //optionsVBox.children.add(this.targetsComboBox)

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

        var c = Circle(0.0, 0.0, 20.0)
        val applyOnFontB = Button(null, FontIcon("fas-font:15"))
        applyOnFontB.style = "-fx-base: darkgray"
        (applyOnFontB.graphic as FontIcon).fill = Color.WHITE
        applyOnFontB.onMouseClicked = EventHandler {
            this.applyOnFont = !this.applyOnFont
            when(this.applyOnFont) {
                true -> {
                    applyOnFontB.style = "-fx-base: red"
                    (applyOnFontB.graphic as FontIcon).fill = Color.WHITE
                }
                false -> {
                    applyOnFontB.style = "-fx-base: darkgray"
                    (applyOnFontB.graphic as FontIcon).fill = Color.WHITE
                }
            }
        }
        applyOnFontB.shape = c
        applyOnFontB.layoutX = 0.0
        applyOnFontB.layoutY = 150.0
        applyOnFontB.setMinSize(30.0, 30.0)
        applyOnFontB.setMaxSize(30.0, 30.0)
        colorWheelGroup.children.add(applyOnFontB)

    }

    abstract fun addLastColor(c:DoubleArray)

    abstract protected fun repaintBrightness(c:DoubleArray)

    abstract protected fun repaintSaturation(c:DoubleArray)

    protected fun getDrawingElementsSelector(): (DrawingElement) -> Boolean {
        return if (this.applyOnFont) {
            { el -> el.type == SecondaryStructureType.A || el.type == SecondaryStructureType.U || el.type == SecondaryStructureType.G || el.type == SecondaryStructureType.C || el.type == SecondaryStructureType.X }
        } else
            { el -> el.type != SecondaryStructureType.A && el.type != SecondaryStructureType.U && el.type != SecondaryStructureType.G && el.type != SecondaryStructureType.C && el.type != SecondaryStructureType.X }
    }

    abstract protected fun updateDSLScript()

}