package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.*


class LineWidthButton(val mediator: Mediator, label:String): VBox() {

    val knobRadius = 30.0
    val buttonShape = Group()
    val buttonWidth = 70.0

    init {
        this.minWidth = buttonWidth
        this.prefWidth = buttonWidth
        this.maxWidth = buttonWidth
        this.isFillWidth = true
        this.spacing = 10.0
        this.alignment = Pos.BOTTOM_CENTER
        this.padding = Insets(10.0, 0.0, 10.0, 0.0)

        this.children.add(buttonShape)

        val l = Label(label)
        this.children.add(l)

        val innerCircle = Circle(buttonWidth/2.0, buttonWidth/2.0, knobRadius)
        val innerShadow = InnerShadow()
        innerShadow.offsetX = 4.0
        innerShadow.offsetY = 4.0
        innerShadow.color = Color.LIGHTGRAY
        innerCircle.effect = innerShadow
        val radialGradient = RadialGradient(0.0,
            knobRadius,
            buttonWidth/2.0-knobRadius/2.0,
            buttonWidth/2.0-knobRadius/2.0,
            knobRadius,
            false,
            CycleMethod.NO_CYCLE,
            Stop(0.0, Color.WHITE),
        Stop(1.0, Color.SILVER))
        innerCircle.fill = radialGradient
        innerCircle.stroke = Color.BLACK
        innerCircle.strokeWidth = 5.0
        buttonShape.children.add(innerCircle)

        val selector = Circle(buttonWidth/2.0, buttonWidth/2.0-knobRadius+10.0, 5.0)
        selector.fill = Color.BLACK
        buttonShape.children.add(selector)

        var tick = Line(buttonWidth/2.0, buttonWidth/2.0-knobRadius-4.0, buttonWidth/2.0, buttonWidth/2.0-knobRadius-8.0)
        tick.strokeLineCap = StrokeLineCap.SQUARE
        tick.strokeWidth = 3.0
        tick.fill = Color.WHITE
        tick.stroke = Color.WHITE
        buttonShape.children.add(tick)

        tick = Line(buttonWidth/2.0-knobRadius-4.0, buttonWidth/2.0, buttonWidth/2.0-knobRadius-8.0, buttonWidth/2.0)
        tick.strokeLineCap = StrokeLineCap.SQUARE
        tick.strokeWidth = 3.0
        tick.fill = Color.WHITE
        tick.stroke = Color.WHITE
        buttonShape.children.add(tick)

        tick = Line(buttonWidth/2.0+knobRadius+4.0, buttonWidth/2.0, buttonWidth/2.0+knobRadius+8.0, buttonWidth/2.0)
        tick.strokeLineCap = StrokeLineCap.SQUARE
        tick.strokeWidth = 3.0
        tick.fill = Color.WHITE
        tick.stroke = Color.WHITE
        buttonShape.children.add(tick)
    }
}