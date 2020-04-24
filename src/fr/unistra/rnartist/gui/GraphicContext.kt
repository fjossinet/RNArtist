package fr.unistra.rnartist.gui

import java.awt.geom.Rectangle2D

class GraphicContext(val mediator:Mediator) {

    var viewX = 0.0
    var viewY = 0.0
    var finalZoomLevel = 1.0
    var screen_capture = false
    var screen_capture_area: Rectangle2D? = null

    fun moveView(transX: Double, transY: Double) {
        viewX += transX
        viewY += transY
    }

    fun setZoom(zoomFactor: Double) {
        finalZoomLevel *= zoomFactor
    }
}