package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import javafx.geometry.Insets
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polygon
import java.awt.geom.Point2D

private val ratio = 2.5

class JunctionKnobFX(val junctionCircle: JunctionDrawing, val mediator: Mediator) : Pane(){

    private val connectors = mutableListOf<Connector>()

    init {
        this.background = Background(BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY));
        this.setPrefSize(150.0/ratio,150.0/ratio)
        this.setMaxWidth(150.0/ratio)
        val connector = Connector(ConnectorId.s)
        connector.setOnMouseClicked {
            var selectedCount = connectors.count { it.selected }
            if (selectedCount < this.junctionCircle.junctionType.value - 1 && !connector.isInId ) {
                connector.selected = !connector.selected
                connector.fill = if (connector.selected) Color.STEELBLUE else Color.LIGHTGRAY
                connector.stroke = if (connector.selected) Color.DARKBLUE else Color.BLACK
            } else if (selectedCount >= this.junctionCircle.junctionType.value - 1 && !connector.isInId ) { //we can only unselect
                connector.selected = false
                connector.fill = Color.LIGHTGRAY
                connector.stroke = Color.BLACK
            }
            //after the click, if we have the selected circles corresponding to helixCount-1 (-1 since the inner helix in red doesn't count)
            selectedCount = connectors.count { it.selected }
            if (selectedCount == this.junctionCircle.junctionType.value - 1) {
                junctionCircle.layout = this.getJunctionLayout().toMutableList()
                this.mediator.drawingDisplayed.get()!!.drawing.computeResidues(junctionCircle)
                //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                mediator.explorer.refresh()
                this.mediator.canvas2D.repaint()
            }
        }
        connector.fill = Color.LIGHTGRAY
        connector.stroke = Color.BLACK
        connector.relocate(75.0/ratio-10/ratio, 140.0/ratio-10/ratio)
        connectors.add(connector)
        this.getChildren().addAll(connector);
        for (i in 1 until 16) {
            val _connector = Connector(getConnectorId(i))
            _connector.setOnMouseClicked {
                var selectedCount = connectors.count { it.selected }
                if (selectedCount < this.junctionCircle.junctionType.value - 1 && !_connector.isInId ) {
                    _connector.selected = !_connector.selected
                    _connector.fill = if (_connector.selected) Color.STEELBLUE else Color.LIGHTGRAY
                    _connector.stroke = if (_connector.selected) Color.DARKBLUE else Color.BLACK
                }  else if (selectedCount >= this.junctionCircle.junctionType.value - 1 && !_connector.isInId ) { //we can only unselect
                    _connector.selected = false
                    _connector.fill = Color.LIGHTGRAY
                    _connector.stroke = Color.BLACK
                }
                //after the click, if we have the selected circles corresponding to helixCount-1 (-1 since the inner helix in red doesn't count)
                selectedCount = connectors.count { it.selected }
                if (selectedCount == this.junctionCircle.junctionType.value - 1) {
                    junctionCircle.layout = this.getJunctionLayout().toMutableList()
                    this.mediator.drawingDisplayed.get()!!.drawing.computeResidues(junctionCircle)
                    //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                    mediator.explorer.refresh()
                    this.mediator.canvas2D.repaint()
                }
            }
            this.connectors.add(_connector)
            val p = rotatePoint(
                Point2D.Double(75.0/ratio, 140.0/ratio),
                Point2D.Double(75.0/ratio, 75.0/ratio),
                i * 360.0 / 16.0
            )
            _connector.relocate(p.x-10.0/ratio, p.y-10.0/ratio)
            this.getChildren().addAll(_connector);
        }

        var up = Polygon()
        up.fill = Color.LIGHTGRAY
        up.stroke = Color.BLACK
        up.strokeWidth = 0.5
        up.getPoints().addAll(arrayOf(
                75.0/ratio-15.0/ratio, 75.0/ratio-21.0/ratio,
                75.0/ratio+15.0/ratio, 75.0/ratio-21.0/ratio,
                75.0/ratio, 75.0/ratio-42.0/ratio))
        up.setOnMousePressed {
            up.fill = Color.BLACK
            junctionCircle.radius = junctionCircle.radius * 1.1
            junctionCircle.layout = junctionCircle.layout //a trick to recompute the stuff
            this.mediator.drawingDisplayed.get()!!.drawing.computeResidues(junctionCircle)
            this.mediator.canvas2D.repaint()
        }
        up.setOnMouseReleased {
            up.fill = Color.LIGHTGRAY
        }
        this.getChildren().addAll(up);

        var bottom = Polygon()
        bottom.fill = Color.LIGHTGRAY
        bottom.stroke = Color.BLACK
        bottom.strokeWidth = 0.5
        bottom.getPoints().addAll(arrayOf(
                75.0/ratio-15.0/ratio, 75.0/ratio+21.0/ratio,
                75.0/ratio+15.0/ratio, 75.0/ratio+21.0/ratio,
                75.0/ratio, 75.0/ratio+42.0/ratio))
        bottom.setOnMousePressed {
            bottom.fill = Color.BLACK
            junctionCircle.radius = junctionCircle.radius * 0.9
            junctionCircle.layout = junctionCircle.layout //a trick to recompute the stuff
            this.mediator.drawingDisplayed.get()!!.drawing.computeResidues(junctionCircle)
            this.mediator.canvas2D.repaint()
        }
        bottom.setOnMouseReleased {
            bottom.fill = Color.LIGHTGRAY
        }
        this.getChildren().addAll(bottom);

        var left = Polygon()
        left.fill = Color.LIGHTGRAY
        left.stroke = Color.BLACK
        left.strokeWidth = 0.5
        left.getPoints().addAll(arrayOf(
                75.0/ratio-21.0/ratio, 75.0/ratio-15.0/ratio,
                75.0/ratio-21.0/ratio, 75.0/ratio+15.0/ratio,
                75.0/ratio-42.0/ratio, 75.0/ratio))
        left.setOnMousePressed {
            left.fill = Color.BLACK
            //first we search the connector inID (the red circle
            var inIDIndex:Int = 0
            for (_connector in this.connectors) {
                if (_connector.isInId) {
                    inIDIndex = this.connectors.indexOf(_connector)
                    break
                }
            }
            if (this.connectors[(inIDIndex+1)%16].selected) //we do nothing, a selected circle is just on the left
                it.consume()
            else {
                var currentPos = (inIDIndex+1)%16
                while (currentPos != inIDIndex) {
                    if (!connectors[currentPos].isInId && connectors[currentPos].selected) {
                        connectors[currentPos].selected = false
                        connectors[currentPos].fill = Color.LIGHTGRAY
                        connectors[currentPos].stroke = Color.BLACK
                        connectors[if (currentPos-1 == -1) 15 else currentPos-1].selected = true
                        connectors[if (currentPos-1 == -1) 15 else currentPos-1].fill = Color.STEELBLUE
                        connectors[if (currentPos-1 == -1) 15 else currentPos-1].stroke = Color.DARKBLUE
                    }
                    currentPos = (currentPos+1)%16
                }
                junctionCircle.layout = this.getJunctionLayout().toMutableList()
                this.mediator.drawingDisplayed.get()!!.drawing.computeResidues(junctionCircle)
                this.mediator.canvas2D.repaint()
                //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                mediator.explorer.refresh()
            }
        }
        left.setOnMouseReleased {
            left.fill = Color.LIGHTGRAY
        }
        if (this.junctionCircle.junctionType != JunctionType.ApicalLoop)
            this.getChildren().addAll(left);

        var right = Polygon()
        right.fill = Color.LIGHTGRAY
        right.stroke = Color.BLACK
        right.strokeWidth = 0.5
        right.getPoints().addAll(arrayOf(
                75.0/ratio+21.0/ratio, 75.0/ratio-15.0/ratio,
                75.0/ratio+21.0/ratio, 75.0/ratio+15.0/ratio,
                75.0/ratio+42.0/ratio, 75.0/ratio))
        right.setOnMousePressed {
            right.fill = Color.BLACK
            //first we search the connector inID (the red circle)
            var inIDIndex:Int = 0
            for (_connector in this.connectors) {
                if (_connector.isInId) {
                    inIDIndex = this.connectors.indexOf(_connector)
                    break
                }
            }
            if (this.connectors[if (inIDIndex-1 == -1) 15 else inIDIndex-1].selected) //we do nothing, a selected circle is just on the right
                it.consume()
            else {
                var currentPos = if (inIDIndex-1 == -1) 15 else inIDIndex-1
                while (currentPos != inIDIndex) {
                    if (!connectors[currentPos].isInId && connectors[currentPos].selected) {
                        connectors[currentPos].selected = false
                        connectors[currentPos].fill = Color.LIGHTGRAY
                        connectors[currentPos].stroke = Color.BLACK
                        connectors[(currentPos+1)%16].selected = true
                        connectors[(currentPos+1)%16].fill = Color.STEELBLUE
                        connectors[(currentPos+1)%16].stroke = Color.DARKBLUE
                    }
                    currentPos = if (currentPos-1 == -1) 15 else currentPos-1
                }
                junctionCircle.layout = this.getJunctionLayout().toMutableList()
                this.mediator.drawingDisplayed.get()!!.drawing.computeResidues(junctionCircle)
                this.mediator.canvas2D.repaint()
                //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                mediator.explorer.refresh()
            }
        }
        right.setOnMouseReleased {
            right.fill = Color.LIGHTGRAY
        }
        if (this.junctionCircle.junctionType != JunctionType.ApicalLoop)
            this.getChildren().addAll(right);

        this.loadJunctionLayout()
    }

    private fun getJunctionLayout(): Layout {
        val layout = mutableListOf<ConnectorId>()
        //first we search the circle for the InId
        var startIndex:Int = 0
        this.connectors.forEach { connector ->
           if (connector.isInId) {
               startIndex = this.connectors.indexOf(connector)
           }
        }
        var currentPos = 0
        while (currentPos <= 15) {
            currentPos++
            if (this.connectors[(startIndex+currentPos)%16].selected) {
                layout.add(getConnectorId(currentPos))
            }
        }
        return layout
    }

    private fun loadJunctionLayout() {
        this.clear()
        this.connectors.forEach { connector ->
            if (connector.connectorId == junctionCircle.inId) {
                connector.isInId = true
                connector.fill = Color.RED
                connector.stroke = Color.DARKRED
                connector.selected = false
            } else {
                junctionCircle.connectedJunctions.keys.toMutableList().forEach { connectorId ->
                    if (connector.connectorId == connectorId) {
                        connector.fill = Color.STEELBLUE
                        connector.stroke = Color.DARKBLUE
                        connector.selected = true
                    }
                }
            }
        }
    }

    private fun clear() {
        this.connectors.forEach { connector ->
            connector.isInId = false
            connector.selected = false
            connector.fill = Color.LIGHTGRAY
            connector.stroke = Color.BLACK
        }
    }

}

class Connector(val connectorId: ConnectorId, var selected:Boolean = false):Circle(10.0/ratio, if (selected) Color.STEELBLUE else Color.LIGHTGRAY) {

    var isInId = false

    init {
        this.stroke = if (selected) Color.DARKBLUE else Color.BLACK
        this.strokeWidth = 0.5
    }
}