package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import java.awt.*
import java.awt.geom.*

class SelectionShape(val mediator: Mediator, val element: DrawingElement) {

    private val edges = listOf<SelectionEdge>(
        SelectionEdge(0, 1, element),
        SelectionEdge(1, 2, element),
        SelectionEdge(2, 3, element),
        SelectionEdge(3, 0, element)
    )

    fun draw(g: Graphics2D, at: AffineTransform) {
        if (element !is StructuralDomain && !element.isFullDetails())
            return
        val previousStroke = g.stroke
        val previousColor = g.color

        g.color = RnartistConfig.selectionColor
        val width =
            this.mediator.current2DDrawing!!.workingSession.finalZoomLevel.toFloat() * RnartistConfig.selectionWidth
        g.stroke = BasicStroke(width)

        this.edges.forEach {
            it.draw(g, at)
        }

        g.stroke = previousStroke
        g.color = previousColor
    }

    class SelectionEdge(private val p1: Int, private val p2: Int, private val element: DrawingElement) {
        fun draw(g: Graphics2D, at: AffineTransform) {
            if (element.selectionPoints.size - 1 >= p2) {
                val (p1_1, p2_1) = pointsFrom(
                    element.selectionPoints.get(p1),
                    element.selectionPoints.get(p2),
                    spaceBetweenResidues
                )
                g.draw(at.createTransformedShape(Line2D.Double(element.selectionPoints.get(p1), p1_1)))
                g.draw(at.createTransformedShape(Line2D.Double(p2_1, element.selectionPoints.get(p2))))
            }

        }
    }

}