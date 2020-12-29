package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.DrawingConfigurationParameter
import io.github.fjossinet.rnartist.core.model.HelixDrawing
import io.github.fjossinet.rnartist.core.model.StructuralDomain
import io.github.fjossinet.rnartist.core.model.radiusConst
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D

interface Knob {
    fun draw(g: Graphics2D, at: AffineTransform)

    fun update()

    fun contains(x: Double, y: Double, at: AffineTransform): Boolean

    fun getStructuralDomain(): StructuralDomain
}

class HelixKnob(val helix: HelixDrawing, val mediator: Mediator) : Knob {

    //lateinit var innerCircle: Ellipse2D
    lateinit var outerCircle: Ellipse2D

    init {
        this.update()
    }

    override fun getStructuralDomain(): StructuralDomain {
        return this.helix
    }

    override fun draw(g: Graphics2D, at: AffineTransform) {
        g.color = Color.DARK_GRAY
        g.fill(at.createTransformedShape(this.outerCircle))
        g.color = Color.LIGHT_GRAY
        g.draw(at.createTransformedShape(this.outerCircle))
        /*if (this.helix.drawingConfiguration.params.containsKey(DrawingConfigurationParameter.FullDetails.toString()) && this.helix.drawingConfiguration.params[DrawingConfigurationParameter.FullDetails.toString()].equals(
                "true"
            )
        )
            g.color = Color.GREEN
        else if (this.helix.drawingConfiguration.params.containsKey(DrawingConfigurationParameter.FullDetails.toString()) && this.helix.drawingConfiguration.params[DrawingConfigurationParameter.FullDetails.toString()].equals(
                "false"
            )
        )
            g.color = Color.RED
        else
            g.color = Color.DARK_GRAY
        g.fill(at.createTransformedShape(this.innerCircle))*/
    }

    override fun update() {
        /*this.innerCircle = Ellipse2D.Double(
            (helix.line.x1 + helix.line.x2) / 2.0 - 0.5*radiusConst,
            (helix.line.y1 + helix.line.y2) / 2.0 - 0.5*radiusConst,
            radiusConst,
            radiusConst
        )*/
        this.outerCircle = Ellipse2D.Double(
            (helix.line.x1 + helix.line.x2) / 2.0 - radiusConst,
            (helix.line.y1 + helix.line.y2) / 2.0 - radiusConst,
            radiusConst *2.0,
            radiusConst *2.0
        )
    }

    override fun contains(x: Double, y: Double, at: AffineTransform): Boolean {
        /*if (at.createTransformedShape(this.innerCircle).contains(x, y)) {
            var result: String? = "true"
            if (this.helix.drawingConfiguration.params.containsKey(DrawingConfigurationParameter.FullDetails.toString())) {
                if ("true".equals(this.helix.drawingConfiguration.fullDetails.toString()))
                    result = "false"
                else if ("false".equals(this.helix.drawingConfiguration.fullDetails.toString()))
                    result = null
            }
            this.mediator.explorer.getTreeViewItemFor(
                this.mediator.explorer.treeTableView.root,
                this.helix
            ).value.fullDetails = result
            this.mediator.canvas2D.repaint()
            this.mediator.explorer.refresh()
            return true
        }*/
        return false
    }

}