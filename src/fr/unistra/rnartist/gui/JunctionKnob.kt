package fr.unistra.rnartist.gui

import fr.unistra.rnartist.model.*
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import java.awt.geom.Point2D


class JunctionKnob(val junctionCircle:JunctionCircle, val mediator:Mediator) : Pane(){

    val connectors = mutableListOf<Connector>()

    init {
        this.setStyle("-fx-background-color: #ffffff; -fx-border-color: darkgray; -fx-border-width: 2px;");
        this.setPrefSize(150.0,150.0)
        this.setOnMouseClicked {
            this.select()
            var selectedCount = connectors.count { it.selected }
            if (selectedCount < this.junctionCircle.junction.type.value - 1) {
                for (c in connectors) {
                    val localMouseClick = c.parentToLocal(it.x,it.y)
                    if (!c.isInId && c.contains(localMouseClick)) {
                        c.selected = !c.selected
                        c.fill = if (c.selected) Color.STEELBLUE else Color.LIGHTGRAY
                        break
                    }
                }
            } else if (selectedCount >= this.junctionCircle.junction.type.value - 1) { //We can only unselect
                for (c in connectors) {
                    val localMouseClick = c.parentToLocal(it.x,it.y)
                    if (!c.isInId && c.contains(localMouseClick) && c.selected) {
                        c.selected = false
                        c.fill = Color.LIGHTGRAY
                        break
                    }
                }
            }
            //after the click, if we have the selected circles corresponding to helixCount-1 (-1 since the inner helix in red doesn't count)
            selectedCount = connectors.count { it.selected }
            if (selectedCount == this.junctionCircle.junction.type.value - 1) {
                junctionCircle.layout = this.getJunctionLayout().toMutableList()
                this.mediator.canvas2D.secondaryStructureDrawing.get().computeResidues(junctionCircle)
                //we need to update the other knobs since the modification of this layout could have produced impacts on other junctions
                mediator.toolbox.junctionKnobs.children.forEach {
                    var junctionKnob  = ((it as VBox).children.first() as JunctionKnob)
                    if (junctionKnob != this)
                        junctionKnob.loadJunctionLayout()
                }
            }
            mediator.graphicsContext.selectedResidues.clear()
            mediator.graphicsContext.selectedResidues.addAll(junctionCircle.junction.location.positions)
            mediator.graphicsContext.selectedResidues.addAll(junctionCircle.inHelix.location.positions)
            for (h in junctionCircle.helices) mediator.graphicsContext.selectedResidues.addAll(h.helix.location.positions)
                mediator.graphicsContext.selectedResidues.addAll(junctionCircle.junction.location.positions)
            this.mediator.canvas2D.repaint()
        }
        val connector = Connector(ConnectorId.s)
        connector.fill = Color.LIGHTGRAY
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
                connector.selected = false
            } else {
                junctionCircle.connectedJunctions.keys.toMutableList().forEach { connectorId ->
                    if (connector.connectorId == connectorId) {
                        connector.fill = Color.STEELBLUE
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
        }
    }

}

class Connector(val connectorId:ConnectorId, var selected:Boolean = false):Circle(10.0, if (selected) Color.STEELBLUE else Color.LIGHTGRAY) {

    var isInId = false

    init {
        this.stroke = Color.DARKGRAY
    }
}