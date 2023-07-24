package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.layout
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.Targetable
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import java.awt.geom.Point2D

class JunctionKnob(val label: String, val mediator: Mediator, junction: JunctionDrawing? = null) : VBox(), Targetable {

    val connectors = mutableListOf<JunctionConnector>()
    var connectorRadius = 8.5
    var knobCenterX = 90.0
    var knobCenterY = 90.0
    var knobRadius = 70.0
    val group = Group()
    val targetsComboBox = ComboBox<KnobTarget>()
    var selectedJunction: JunctionDrawing? = null
        set(value) {
            field = value
            this.updateConnectors()
        }

    init {
        this.alignment = Pos.CENTER_LEFT
        this.spacing = 5.0
        this.minWidth = 180.0
        this.prefWidth = 180.0
        this.maxWidth = 180.0
        this.isFillWidth = true
        this.padding = Insets(10.0, 10.0, 20.0, 10.0)

        val c = Circle(knobCenterX, knobCenterY, this.knobRadius - 3 * this.connectorRadius)
        c.strokeWidth = 1.0
        c.stroke = Color.BLACK
        var innerShadow = InnerShadow()
        innerShadow.offsetX = 0.0
        innerShadow.offsetY = 0.0
        innerShadow.color = Color.WHITE
        c.effect = innerShadow
        val radialGradient = RadialGradient(
            0.0,
            0.0,
            knobCenterX,
            knobCenterY,
            this.knobRadius*2.0,
            false,
            CycleMethod.NO_CYCLE,
            Stop(0.0, Color.DARKSLATEGREY),
            Stop(1.0, Color.WHITE))
        c.fill = radialGradient
        this.group.children.add(c)

        for (i in 1 until 16) {
            val p = rotatePoint(
                Point2D.Double(this.knobCenterX, this.knobCenterY + this.knobRadius - 1.65 * connectorRadius),
                Point2D.Double(this.knobCenterX, this.knobCenterY),
                i * 360.0 / 16.0
            )
            val _connector = JunctionConnector(getConnectorId(i), false, p.x, p.y, this)
            this.group.children.add(_connector)
            this.connectors.add(_connector)
        }
        this.group.children.add(Left(this))
        this.group.children.add(Right(this))

        this.group.children.add(Up(this))
        this.group.children.add(Down(this))

        val v = VBox()
        v.children.add(group)

        v.alignment = Pos.CENTER
        var l = Label("N")
        l.font = Font.font("Monospace", FontWeight.BOLD, 13.0)
        l.layoutXProperty().bind(SimpleDoubleProperty(90.0).subtract(l.widthProperty().divide(2)))
        l.layoutYProperty().bind(connectors[7].centerYProperty().subtract(connectorRadius*1.5).subtract(l.heightProperty()))
        this.group.children.add(l)
        l = Label("W")
        l.font = Font.font("Monospace", FontWeight.BOLD, 13.0)
        l.layoutXProperty().bind(connectors[3].centerXProperty().subtract(connectorRadius*1.5).subtract(l.widthProperty()))
        l.layoutYProperty().bind(connectors[3].centerYProperty().subtract(l.heightProperty().divide(2)))
        this.group.children.add(l)
        l = Label("E")
        l.font = Font.font("Monospace", FontWeight.BOLD, 13.0)
        l.layoutXProperty().bind(connectors[11].centerXProperty().add(connectorRadius*1.5))
        l.layoutYProperty().bind(connectors[11].centerYProperty().subtract(l.heightProperty().divide(2)))
        this.group.children.add(l)

        this.children.add(v)
        this.children.add(Label("Target"))

        val labels = listOf(KnobTarget("Any", null),
                            KnobTarget("Apical Loops", JunctionType.ApicalLoop),
                            KnobTarget("Inner Loops", JunctionType.InnerLoop),
                            KnobTarget("3-Ways", JunctionType.ThreeWay),
                            KnobTarget("4-Ways", JunctionType.FourWay),
                            KnobTarget("5-Ways", JunctionType.FiveWay),
                            KnobTarget("6-Ways", JunctionType.SixWay),
                            KnobTarget("7-Ways", JunctionType.SevenWay),
                            KnobTarget("8-Ways", JunctionType.EightWay))
        this.targetsComboBox.items.addAll(labels)
        this.targetsComboBox.selectionModel.select(0)
        this.targetsComboBox.minWidth = 150.0
        this.targetsComboBox.maxWidth = 150.0
        this.targetsComboBox.onAction = EventHandler {
            //synchronization with the other targetable widgets
            mediator.targetables.forEach { it.setTarget(this.targetsComboBox.value.name) }
        }
        this.children.add(targetsComboBox)
        mediator.targetables.add(this)

        this.selectedJunction = junction
    }

    fun getCurrentLayout(): List<ConnectorId> {
        val layout = mutableListOf<ConnectorId>()
        this.connectors.forEach {
            if (it.selected)
                layout.add(it.connectorId)
        }
        return layout
    }

    fun updateConnectors() {
        selectedJunction?.let { junction ->
            targetsComboBox.value = targetsComboBox.items.filter { it.junctionType == junction.junctionType }.first()
            this.connectors.forEach {
                it.selected = junction.currentLayout.contains(it.connectorId)
            }
        } ?: run {
            this.connectors.forEach {
                it.selected = false
                it.fill = Color.DARKGRAY
            }
        }
    }

    override fun setTarget(target: String) {
        this.targetsComboBox.items.forEach {
            if (it.name.equals(target)) {
                this.targetsComboBox.value = it
                return
            }
        }

    }

}

class KnobTarget(val name:String, val junctionType:JunctionType?= null) {
    override fun toString(): String {
        return this.name
    }
}

open class JunctionConnector(
    val connectorId: ConnectorId,
    selected: Boolean = false,
    centerX: Double,
    centerY: Double,
    private val knob: JunctionKnob
) : Circle(centerX, centerY, knob.connectorRadius) {

    var selected = selected
        set(value) {
            field = value
            if (selected)
                this.fill = Color.DARKORANGE
            else
                this.fill = Color.WHITE
        }

    init {
        this.strokeWidth = 2.0
        this.stroke = Color.BLACK
        this.fill = Color.WHITE

        val innerShadow = InnerShadow()
        innerShadow.offsetX = 1.0
        innerShadow.offsetY = 1.0
        innerShadow.color = Color.WHITE
        this.effect = innerShadow
        this.onMouseClicked = EventHandler {
            knob.selectedJunction?.let { junction ->
                var selectedCount = knob.connectors.count { it.selected }
                if (selectedCount < junction.junctionType.value - 1) {
                    this.selected = !this.selected
                } else if (selectedCount >= junction.junctionType.value - 1) { //we can only unselect
                    this.selected = false
                }
                //after the click, if we have the selected circles corresponding to helixCount-1 (-1 since the inner helix in red doesn't count)
                selectedCount = knob.connectors.count { it.selected }
                if (selectedCount == junction.junctionType.value - 1) {
                    if (junction.junctionType != JunctionType.ApicalLoop)
                        junction.applyLayout(layout {
                            junction {
                                name = junction.name
                                out_ids = knob.getCurrentLayout().map { it.toString() }.joinToString(separator = " ")
                            }
                        }!!)
                }
                knob.mediator.canvas2D.repaint()
            }
        }
    }
}

class Up(val knob: JunctionKnob) : Path() {

    init {
        val moveTo = MoveTo()
        moveTo.x = knob.knobCenterX
        moveTo.y = knob.knobCenterY - 4 * knob.connectorRadius
        this.elements.add(moveTo)
        var lineTo = LineTo()
        lineTo.x = knob.knobCenterX - knob.connectorRadius * 1.5
        lineTo.y = knob.knobCenterY - 2 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX + knob.connectorRadius * 1.5
        lineTo.y = knob.knobCenterY - 2 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX
        lineTo.y = knob.knobCenterY - 4 * knob.connectorRadius
        this.elements.add(lineTo)

        this.stroke = Color.BLACK
        this.strokeWidth = 1.0
        this.fill = Color.WHITE
        val innerShadow = InnerShadow()
        innerShadow.offsetX = 0.0
        innerShadow.offsetY = 0.0
        innerShadow.color = Color.BLACK
        this.effect = innerShadow

        this.onMousePressed = EventHandler {
            this.fill = Color.DARKORANGE
            if (knob.mediator.canvas2D.getSelection().isNotEmpty()) {
                knob.mediator.canvas2D.getSelection().filter {it is JunctionDrawing }.map { it as JunctionDrawing }.forEach { junction ->
                    if (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType) {
                        junction.radiusRatio += 0.1
                        junction.applyLayout(layout {
                            junction {
                                name = junction.name
                                radius = Math.round(junction.initialRadius * junction.radiusRatio * 100.0) / 100.0
                            }
                        }!!)
                        this.knob.mediator.canvas2D.repaint()
                    }
                }
            } else {
                knob.mediator.drawingDisplayed.get()?.let {
                    it.drawing.allJunctions.forEach { junction ->
                        if (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType) {
                            junction.radiusRatio += 0.1
                            junction.applyLayout(layout {
                                junction {
                                    name = junction.name
                                    radius = Math.round(junction.initialRadius * junction.radiusRatio * 100.0) / 100.0
                                }
                            }!!)
                            this.knob.mediator.canvas2D.repaint()
                        }
                    }
                }
            }
        }
        this.onMouseReleased = EventHandler {
            this.fill = Color.WHITE
        }
    }
}

class Down(val knob: JunctionKnob) : Path() {

    init {
        val moveTo = MoveTo()
        moveTo.x = knob.knobCenterX
        moveTo.y = knob.knobCenterY + 4 * knob.connectorRadius
        this.elements.add(moveTo)
        var lineTo = LineTo()
        lineTo.x = knob.knobCenterX - knob.connectorRadius * 1.5
        lineTo.y = knob.knobCenterY + 2 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX + knob.connectorRadius * 1.5
        lineTo.y = knob.knobCenterY + 2 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX
        lineTo.y = knob.knobCenterY + 4 * knob.connectorRadius
        this.elements.add(lineTo)

        this.stroke = Color.BLACK
        this.strokeWidth = 1.0
        this.fill = Color.WHITE
        val innerShadow = InnerShadow()
        innerShadow.offsetX = 0.0
        innerShadow.offsetY = 0.0
        innerShadow.color = Color.BLACK
        this.effect = innerShadow

        this.onMousePressed = EventHandler {
            this.fill = Color.DARKORANGE
            if (knob.mediator.canvas2D.getSelection().isNotEmpty()) {
                knob.mediator.canvas2D.getSelection().filter {it is JunctionDrawing }.map { it as JunctionDrawing }.forEach { junction ->
                    if (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType) {
                        junction.radiusRatio -= 0.1
                        junction.applyLayout(layout {
                            junction {
                                name = junction.name
                                radius = Math.round(junction.initialRadius * junction.radiusRatio * 100.0) / 100.0
                            }
                        }!!)
                        this.knob.mediator.canvas2D.repaint()
                    }
                }
            } else {
                knob.mediator.drawingDisplayed.get()?.let {
                    it.drawing.allJunctions.forEach { junction ->
                        if (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType) {
                            junction.radiusRatio -= 0.1
                            junction.applyLayout(layout {
                                junction {
                                    name = junction.name
                                    radius = Math.round(junction.initialRadius * junction.radiusRatio * 100.0) / 100.0
                                }
                            }!!)
                            this.knob.mediator.canvas2D.repaint()
                        }
                    }
                }
            }
        }
        this.onMouseReleased = EventHandler {
            this.fill = Color.WHITE
        }
    }

}

class Left(val knob: JunctionKnob) : Path() {

    init {
        val moveTo = MoveTo()
        moveTo.x = knob.knobCenterX - 4 * knob.connectorRadius
        moveTo.y = knob.knobCenterY
        this.elements.add(moveTo)
        var lineTo = LineTo()
        lineTo.x = knob.knobCenterX - 2 * knob.connectorRadius
        lineTo.y = knob.knobCenterY - 1.5 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX - 2 * knob.connectorRadius
        lineTo.y = knob.knobCenterY + 1.5 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX - 4 * knob.connectorRadius
        lineTo.y = knob.knobCenterY
        this.elements.add(lineTo)

        this.stroke = Color.BLACK
        this.strokeWidth = 1.0
        this.fill = Color.WHITE
        val innerShadow = InnerShadow()
        innerShadow.offsetX = 0.0
        innerShadow.offsetY = 0.0
        innerShadow.color = Color.BLACK
        this.effect = innerShadow

        this.onMousePressed = EventHandler {
            this.fill = Color.DARKORANGE
            if (knob.mediator.canvas2D.getSelection().isNotEmpty()) {
                knob.mediator.canvas2D.getSelection().filter {it is JunctionDrawing }.map { it as JunctionDrawing }.forEach { junction ->
                    if (junction.junction.junctionType != JunctionType.ApicalLoop && (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType)) {
                        val currentLayout = junction.currentLayout
                        if (currentLayout.first() != ConnectorId.ssw) {
                            val newLayout = mutableListOf<ConnectorId>()
                            currentLayout.forEach {
                                newLayout.add(getConnectorId(it.value-1))
                            }
                            junction.applyLayout(layout {
                                junction {
                                    name = junction.name
                                    out_ids = newLayout.map { it.toString() }.joinToString(separator = " ")
                                }
                            }!!)
                            this.knob.mediator.canvas2D.repaint()
                            this.knob.updateConnectors()
                        }
                    }
                }
            } else if (knob.targetsComboBox.value.junctionType != JunctionType.ApicalLoop) {
                knob.mediator.drawingDisplayed.get()?.let {
                    it.drawing.allJunctions.forEach { junction ->
                        if (junction.junction.junctionType != JunctionType.ApicalLoop && (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType)) {
                            val currentLayout = junction.currentLayout
                            if (currentLayout.first() != ConnectorId.ssw) {
                                val newLayout = mutableListOf<ConnectorId>()
                                currentLayout.forEach {
                                    newLayout.add(getConnectorId(it.value-1))
                                }
                                junction.applyLayout(layout {
                                    junction {
                                        name = junction.name
                                        out_ids = newLayout.map { it.toString() }.joinToString(separator = " ")
                                    }
                                }!!)
                                this.knob.mediator.canvas2D.repaint()
                                this.knob.updateConnectors()
                            }
                        }
                    }
                }
            }
        }
        this.onMouseReleased = EventHandler {
            this.fill = Color.WHITE
        }
    }

}

class Right(private val knob: JunctionKnob) : Path() {

    init {
        val moveTo = MoveTo()
        moveTo.x = knob.knobCenterX + 4 * knob.connectorRadius
        moveTo.y = knob.knobCenterY
        this.elements.add(moveTo)
        var lineTo = LineTo()
        lineTo.x = knob.knobCenterX + 2 * knob.connectorRadius
        lineTo.y = knob.knobCenterY - 1.5 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX + 2 * knob.connectorRadius
        lineTo.y = knob.knobCenterY + 1.5 * knob.connectorRadius
        this.elements.add(lineTo)
        lineTo = LineTo()
        lineTo.x = knob.knobCenterX + 4 * knob.connectorRadius
        lineTo.y = knob.knobCenterY
        this.elements.add(lineTo)

        this.stroke = Color.BLACK
        this.strokeWidth = 1.0
        this.fill = Color.WHITE
        val innerShadow = InnerShadow()
        innerShadow.offsetX = 0.0
        innerShadow.offsetY = 0.0
        innerShadow.color = Color.BLACK
        this.effect = innerShadow

        this.onMousePressed = EventHandler {
            this.fill = Color.DARKORANGE
            if (knob.mediator.canvas2D.getSelection().isNotEmpty()) {
                knob.mediator.canvas2D.getSelection().filter {it is JunctionDrawing }.map { it as JunctionDrawing }.forEach { junction ->
                    if (junction.junction.junctionType != JunctionType.ApicalLoop && (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType)) {
                       val currentLayout = junction.currentLayout
                       if (currentLayout.last() != ConnectorId.sse) {
                           val newLayout = mutableListOf<ConnectorId>()
                           currentLayout.forEach {
                               newLayout.add(getConnectorId(it.value+1))
                           }
                           junction.applyLayout(layout {
                               junction {
                                   name = junction.name
                                   out_ids = newLayout.map { it.toString() }.joinToString(separator = " ")
                               }
                           }!!)
                           this.knob.mediator.canvas2D.repaint()
                           this.knob.updateConnectors()
                       }
                   }
                }
            } else if (knob.targetsComboBox.value.junctionType != JunctionType.ApicalLoop) {
                knob.mediator.drawingDisplayed.get()?.let {
                    it.drawing.allJunctions.forEach { junction ->
                        if (junction.junction.junctionType != JunctionType.ApicalLoop && (knob.targetsComboBox.value.junctionType == null /*meaning any*/ || junction.junction.junctionType == knob.targetsComboBox.value.junctionType)) {
                            val currentLayout = junction.currentLayout
                            if (currentLayout.last() != ConnectorId.sse) {
                                val newLayout = mutableListOf<ConnectorId>()
                                currentLayout.forEach {
                                    newLayout.add(getConnectorId(it.value+1))
                                }
                                junction.applyLayout(layout {
                                    junction {
                                        name = junction.name
                                        out_ids = newLayout.map { it.toString() }.joinToString(separator = " ")
                                    }
                                }!!)
                                this.knob.mediator.canvas2D.repaint()
                                this.knob.updateConnectors()
                            }
                        }
                    }
                }
            }
            this.onMouseReleased = EventHandler {
                this.fill = Color.WHITE
            }
        }
    }

}