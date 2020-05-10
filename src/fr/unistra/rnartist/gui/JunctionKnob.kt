package fr.unistra.rnartist.gui

import fr.unistra.rnartist.model.*
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Polygon
import java.awt.geom.Point2D
import java.util.*

class JunctionKnob(val junctionCircle:JunctionCircle, val mediator:Mediator) : Pane(){

    val connectors = mutableListOf<Connector>()

    init {
        this.setStyle("-fx-background-color: #ffffff; -fx-border-color: darkgray; -fx-border-width: 2px;");
        this.setPrefSize(150.0,150.0)
        this.setMaxWidth(150.0)
        this.setOnMouseClicked {
            this.select()
            var selectedCount = connectors.count { it.selected }
            if (selectedCount < this.junctionCircle.junction.type.value - 1) {
                for (c in connectors) {
                    val localMouseClick = c.parentToLocal(it.x,it.y)
                    if (!c.isInId && c.contains(localMouseClick)) {
                        c.selected = !c.selected
                        c.fill = if (c.selected) Color.STEELBLUE else Color.LIGHTGRAY
                        c.stroke = if (c.selected) Color.DARKBLUE else Color.DARKGRAY
                        break
                    }
                }
            } else if (selectedCount >= this.junctionCircle.junction.type.value - 1) { //We can only unselect
                for (c in connectors) {
                    val localMouseClick = c.parentToLocal(it.x,it.y)
                    if (!c.isInId && c.contains(localMouseClick) && c.selected) {
                        c.selected = false
                        c.fill = Color.LIGHTGRAY
                        c.stroke = Color.DARKGRAY
                        break
                    }
                }
            }
            //after the click, if we have the selected circles corresponding to helixCount-1 (-1 since the inner helix in red doesn't count)
            selectedCount = connectors.count { it.selected }
            if (selectedCount == this.junctionCircle.junction.type.value - 1) {
                junctionCircle.layout = this.getJunctionLayout().toMutableList()
                this.mediator.secondaryStructureDrawing!!.computeResidues(junctionCircle)
                //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                mediator.toolbox.junctionKnobs.children.forEach {
                    var junctionKnob  = ((it as VBox).children.first() as JunctionKnob)
                    if (junctionKnob != this)
                        junctionKnob.loadJunctionLayout()
                }
            }
            mediator.selectedResidues?.clear()
            mediator.selectedResidues?.addAll(this.mediator.secondaryStructureDrawing!!.getResiduesFromAbsPositions(junctionCircle.junction.location.positions))
            mediator.selectedResidues?.addAll(this.mediator.secondaryStructureDrawing!!.getResiduesFromAbsPositions(junctionCircle.inHelix.location.positions))
            for (h in junctionCircle.helices) mediator.selectedResidues?.addAll(this.mediator.secondaryStructureDrawing!!.getResiduesFromAbsPositions(h.helix.location.positions))
                mediator.selectedResidues?.addAll(this.mediator.secondaryStructureDrawing!!.getResiduesFromAbsPositions(junctionCircle.junction.location.positions))
            this.mediator.canvas2D.repaint()
        }
        val connector = Connector(ConnectorId.s)
        connector.fill = Color.LIGHTGRAY
        connector.stroke = Color.DARKGRAY
        connector.relocate(75.0-10, 130.0-10)
        connectors.add(connector)
        this.getChildren().addAll(connector);
        for (i in 1 until 16) {
            val connector = Connector(getConnectorId(i))
            this.connectors.add(connector)
            val p = rotatePoint(Point2D.Double(75.0, 130.0), Point2D.Double(75.0,75.0), i*360.0/ 16.0)
            connector.relocate(p.x-10, p.y-10)
            this.getChildren().addAll(connector);
        }

        var up = Polygon()
        up.fill = Color.LIGHTGRAY
        up.stroke = Color.BLACK
        up.getPoints().addAll(arrayOf(
                75.0-15, 75.0-21,
                75.0+15, 75.0-21,
                75.0, 75.0-42))
        up.setOnMousePressed {
            up.fill = Color.BLACK
            junctionCircle.radius = junctionCircle.radius * 1.1
            junctionCircle.layout = junctionCircle.layout //a trick to recompute the stuff
            this.mediator.secondaryStructureDrawing!!.computeResidues(junctionCircle)
            this.mediator.canvas2D.repaint()
        }
        up.setOnMouseReleased {
            up.fill = Color.LIGHTGRAY
        }
        this.getChildren().addAll(up);

        var bottom = Polygon()
        bottom.fill = Color.LIGHTGRAY
        bottom.stroke = Color.BLACK
        bottom.getPoints().addAll(arrayOf(
                75.0-15, 75.0+21,
                75.0+15, 75.0+21,
                75.0, 75.0+42))
        bottom.setOnMousePressed {
            bottom.fill = Color.BLACK
            junctionCircle.radius = junctionCircle.radius * 0.9
            junctionCircle.layout = junctionCircle.layout //a trick to recompute the stuff
            this.mediator.secondaryStructureDrawing!!.computeResidues(junctionCircle)
            this.mediator.canvas2D.repaint()
        }
        bottom.setOnMouseReleased {
            bottom.fill = Color.LIGHTGRAY
        }
        this.getChildren().addAll(bottom);

        var left = Polygon()
        left.fill = Color.LIGHTGRAY
        left.stroke = Color.BLACK
        left.getPoints().addAll(arrayOf(
                75.0-21, 75.0-15,
                75.0-21, 75.0+15,
                75.0-42, 75.0))
        left.setOnMousePressed {
            left.fill = Color.BLACK
            //first we search the connector inID (the red circle
            var inIDIndex:Int = 0
            for (connector in this.connectors) {
                if (connector.isInId) {
                    inIDIndex = this.connectors.indexOf(connector)
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
                        connectors[currentPos].stroke = Color.DARKGRAY
                        connectors[if (currentPos-1 == -1) 15 else currentPos-1].selected = true
                        connectors[if (currentPos-1 == -1) 15 else currentPos-1].fill = Color.STEELBLUE
                        connectors[if (currentPos-1 == -1) 15 else currentPos-1].stroke = Color.DARKBLUE
                    }
                    currentPos = (currentPos+1)%16
                }
                junctionCircle.layout = this.getJunctionLayout().toMutableList()
                this.mediator.secondaryStructureDrawing!!.computeResidues(junctionCircle)
                //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                mediator.toolbox.junctionKnobs.children.forEach {
                    var junctionKnob  = ((it as VBox).children.first() as JunctionKnob)
                    if (junctionKnob != this)
                        junctionKnob.loadJunctionLayout()
                }
            }
        }
        left.setOnMouseReleased {
            left.fill = Color.LIGHTGRAY
        }
        if (this.junctionCircle.junction.type != JunctionType.ApicalLoop)
            this.getChildren().addAll(left);

        var right = Polygon()
        right.fill = Color.LIGHTGRAY
        right.stroke = Color.BLACK
        right.getPoints().addAll(arrayOf(
                75.0+21, 75.0-15,
                75.0+21, 75.0+15,
                75.0+42, 75.0))
        right.setOnMousePressed {
            right.fill = Color.BLACK
            //first we search the connector inID (the red circle)
            var inIDIndex:Int = 0
            for (connector in this.connectors) {
                if (connector.isInId) {
                    inIDIndex = this.connectors.indexOf(connector)
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
                        connectors[currentPos].stroke = Color.DARKGRAY
                        connectors[(currentPos+1)%16].selected = true
                        connectors[(currentPos+1)%16].fill = Color.STEELBLUE
                        connectors[(currentPos+1)%16].stroke = Color.DARKBLUE
                    }
                    currentPos = if (currentPos-1 == -1) 15 else currentPos-1
                }
                junctionCircle.layout = this.getJunctionLayout().toMutableList()
                this.mediator.secondaryStructureDrawing!!.computeResidues(junctionCircle)
                //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                mediator.toolbox.junctionKnobs.children.forEach {
                    var junctionKnob  = ((it as VBox).children.first() as JunctionKnob)
                    if (junctionKnob != this)
                        junctionKnob.loadJunctionLayout()
                }
            }
        }
        right.setOnMouseReleased {
            right.fill = Color.LIGHTGRAY
        }
        if (this.junctionCircle.junction.type != JunctionType.ApicalLoop)
            this.getChildren().addAll(right);

        var centerViewOnJunction = Circle(10.0)
        centerViewOnJunction.relocate(65.0,65.0)
        centerViewOnJunction.stroke = Color.BLACK
        centerViewOnJunction.fill = Color.BLACK

        centerViewOnJunction.setOnMousePressed {
            mediator.secondaryStructureDrawing?.let { drawing ->
                centerViewOnJunction.fill = Color.LIGHTGRAY
                mediator.selectedResidues!!.clear()
                mediator.selectedResidues!!.addAll(drawing.getResiduesFromAbsPositions(junctionCircle.junction.location.positions))
                mediator.selectedResidues!!.addAll(drawing.getResiduesFromAbsPositions(junctionCircle.inHelix.location.positions))
                for (h in junctionCircle.helices)
                    mediator.selectedResidues!!.addAll(drawing.getResiduesFromAbsPositions(h.helix.location.positions))
                mediator.selectedResidues!!.addAll(drawing.getResiduesFromAbsPositions(junctionCircle.junction.location.positions))
                mediator.workingSession!!.centerFrameOnSelection(mediator.canvas2D.getBounds())
                mediator.canvas2D.repaint()
                if (mediator.chimeraDriver != null && mediator.tertiaryStructure != null) {
                    val positions: MutableList<String?> = ArrayList(1)
                    for (c in mediator.workingSession!!.selectedResidues) {
                        positions.add(if (mediator.tertiaryStructure != null && mediator.tertiaryStructure!!.getResidue3DAt(c.absPos) != null) mediator.tertiaryStructure!!.getResidue3DAt(c.absPos)!!.label else "" + (c.absPos + 1))
                    }
                    mediator.chimeraDriver!!.selectResidues(positions, mediator.rna!!.name)
                    mediator.chimeraDriver!!.setFocus(positions, mediator.rna!!.name)
                }
            }
        }

        centerViewOnJunction.setOnMouseReleased {
            centerViewOnJunction.fill = Color.BLACK
        }
        this.getChildren().addAll(centerViewOnJunction);

        this.loadJunctionLayout()
    }

    fun select() {
        this.mediator.toolbox.junctionKnobs.children.forEach{
            ((it as VBox).children.first() as JunctionKnob).unselect()
        }
        this.setStyle("-fx-background-color: #ffffff; -fx-border-color: darkgray; -fx-border-width: 7px;")
    }

    fun unselect() = this.setStyle("-fx-background-color: #ffffff; -fx-border-color: darkgray; -fx-border-width: 2px;")

    fun getJunctionLayout():Layout {
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
            if (this.connectors.get((startIndex+currentPos)%16).selected) {
                layout.add(getConnectorId(currentPos))
            }
        }
        return layout
    }

    fun loadJunctionLayout() {
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

    fun clear() {
        this.connectors.forEach { connector ->
            connector.isInId = false
            connector.selected = false
            connector.fill = Color.LIGHTGRAY
            connector.stroke = Color.DARKGRAY
        }
    }

}

class Connector(val connectorId:ConnectorId, var selected:Boolean = false):Circle(10.0, if (selected) Color.STEELBLUE else Color.LIGHTGRAY) {

    var isInId = false

    init {
        this.stroke = if (selected) Color.DARKBLUE else Color.DARKGRAY
    }
}