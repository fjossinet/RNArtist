package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.core.model.DrawingElement
import io.github.fjossinet.rnartist.core.model.ResidueDrawing
import io.github.fjossinet.rnartist.core.model.StructuralDomainDrawing
import java.awt.Rectangle
import java.awt.geom.Rectangle2D

/**
 * A surface to plot RNA 2Ds
 */
interface Canvas2D {

    var transX:Double
    var transY:Double

    fun getCanvasBounds(): Rectangle

    fun repaint()

    fun centerDisplayOn(frame: Rectangle2D)

    fun fitStructure(selectionFrame: Rectangle2D?, ratio: Double = 1.0)

    fun clearSelection()

    fun getSelection(): List<DrawingElement>

    fun getSelectedPositions(): List<Int>

    fun getSelectedResidues():List<ResidueDrawing>

    fun getSelectionFrame(): Rectangle2D?

    fun isSelected(el: DrawingElement?): Boolean

    fun addToSelection(el: List<DrawingElement>)

    fun addToSelection(el: DrawingElement?)

    fun removeFromSelection(el: DrawingElement?)

    fun structuralDomainsSelected(): List<StructuralDomainDrawing>

}