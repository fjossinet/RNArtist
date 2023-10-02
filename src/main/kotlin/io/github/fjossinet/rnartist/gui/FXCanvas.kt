package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.DrawingElement
import io.github.fjossinet.rnartist.core.model.ResidueDrawing
import io.github.fjossinet.rnartist.core.model.StructuralDomainDrawing
import io.github.fjossinet.rnartist.gui.Canvas2D
import javafx.scene.canvas.Canvas
import java.awt.Rectangle
import java.awt.geom.Rectangle2D

class FXCanvas(val mediator: Mediator, override var transX: Double = 0.0, override var transY: Double = 0.0) :Canvas(), Canvas2D {

    init {
        mediator.canvas2D = this
    }

    override fun getCanvasBounds(): Rectangle {
        return Rectangle()
    }

    override fun repaint() {
    }

    override fun centerDisplayOn(frame: Rectangle2D) {
    }

    override fun fitStructure(selectionFrame: Rectangle2D?, ratio: Double) {
    }

    override fun clearSelection() {
    }

    override fun getSelection(): List<DrawingElement> {
        return listOf()
    }

    override fun getSelectedPositions(): List<Int> {
        return listOf()
    }

    override fun getSelectedResidues(): List<ResidueDrawing> {
        return listOf()
    }

    override fun getSelectionFrame(): Rectangle2D? {
        return null
    }

    override fun isSelected(el: DrawingElement?): Boolean {
        return false
    }

    override fun addToSelection(el: List<DrawingElement>) {
    }

    override fun addToSelection(el: DrawingElement?) {
    }

    override fun removeFromSelection(el: DrawingElement?) {
    }

    override fun structuralDomainsSelected(): List<StructuralDomainDrawing> {
        return listOf()
    }
}