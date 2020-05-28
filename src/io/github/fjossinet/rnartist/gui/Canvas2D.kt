package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import javax.swing.JPanel

class Canvas2D(val mediator: Mediator): JPanel() {

    var secondaryStructureDrawing: SecondaryStructureDrawing? = null
    lateinit private var offScreenBuffer: Image
    var translateX = 0.0
    var translateY = 0.0
    var fps:Long = 0

    init {
        this.mediator.canvas2D = this
    }

    fun load2D(drawing: SecondaryStructureDrawing) {
        this.secondaryStructureDrawing = drawing
        this.fps = 0
        this.secondaryStructureDrawing?.let { drawing ->
            mediator.toolbox.junctionKnobs.children.clear()
            mediator.addToSelection(null, true, null)
            mediator.explorer.load(this.secondaryStructureDrawing)
            for (jc in drawing.allJunctions)
                mediator.toolbox.addJunctionKnob(jc)
            this.repaint()
        }
    }

    fun centerDisplayOn(frame:Rectangle2D) {
        this.secondaryStructureDrawing?.let { drawing ->
            drawing.workingSession.viewX = 0.0
            drawing.workingSession.viewY = 0.0
            var at = AffineTransform()
            at.translate(drawing.workingSession.viewX, drawing.workingSession.viewY)
            at.scale(drawing.workingSession.finalZoomLevel, drawing.workingSession.finalZoomLevel)
            var transformedBounds = at.createTransformedShape(frame)
            //we center the view on the new structure
            drawing.workingSession.viewX += this.getBounds().bounds2D.centerX - transformedBounds.bounds2D.centerX
            drawing.workingSession.viewY += this.getBounds().bounds2D.centerY - transformedBounds.bounds2D.centerY
            this.repaint()
        }
    }

    fun fitDisplayOn(frame:Rectangle2D) {
        this.secondaryStructureDrawing?.let { drawing ->
            drawing.workingSession.viewX = 0.0
            drawing.workingSession.viewY = 0.0
            drawing.workingSession.finalZoomLevel = 1.0
            var at = AffineTransform()
            at.translate(drawing.workingSession.viewX, drawing.workingSession.viewY)
            at.scale(drawing.workingSession.finalZoomLevel, drawing.workingSession.finalZoomLevel)
            var transformedBounds = at.createTransformedShape(frame)
            //we compute the zoomLevel to fit the structure in the frame of the canvas2D
            val widthRatio = transformedBounds.bounds2D.width / this.getBounds().width
            val heightRatio = transformedBounds.bounds2D.height / this.getBounds().height
            drawing.workingSession.finalZoomLevel = if (widthRatio > heightRatio) 1.0 / widthRatio else 1.0 / heightRatio
            //We recompute the bounds of the structure with this new zoom level
            at = AffineTransform()
            at.translate(drawing.workingSession.viewX, drawing.workingSession.viewY)
            at.scale(drawing.workingSession.finalZoomLevel, drawing.workingSession.finalZoomLevel)
            transformedBounds = at.createTransformedShape(frame)
            //we center the view on the new structure
            drawing.workingSession.viewX += this.getBounds().bounds2D.centerX - transformedBounds.bounds2D.centerX
            drawing.workingSession.viewY += this.getBounds().bounds2D.centerY - transformedBounds.bounds2D.centerY
            this.repaint()
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        this.secondaryStructureDrawing?.let { drawing ->
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g2.background = Color.white
            g2.color = Color.BLACK
            if (drawing.workingSession.screen_capture)
                g2.draw(drawing.workingSession.screen_capture_area)
            val start = System.currentTimeMillis()
            drawing.draw(g2)
            val t = 1000/(System.currentTimeMillis()-start)
            this.fps = if (t> this.fps) t else this.fps
            //println("FPS: ${this.fps}")
        }
    }

    override fun update(g: Graphics) {
        val gr: Graphics2D
        if (this.offScreenBuffer == null ||
                !(offScreenBuffer.getWidth(this) == this.size.width
                        && offScreenBuffer.getHeight(this) == this.size.height)) {
            this.offScreenBuffer = this.createImage(this.size.width, this.size.height)
        }
        // We need to use our buffer Image as a Graphics object:
        gr = this.offScreenBuffer.getGraphics() as Graphics2D
        paintComponent(gr)
        g.drawImage(this.offScreenBuffer, 0, 0, this)
    }

    fun screenCapture(secondaryStructureDrawing: SecondaryStructureDrawing?): BufferedImage? {
        this.secondaryStructureDrawing?.let { drawing ->
            var bufferedImage: BufferedImage?
            drawing.workingSession.viewX -= drawing.workingSession.screen_capture_area!!.minX
            drawing.workingSession.viewY -= drawing.workingSession.screen_capture_area!!.minY
            bufferedImage = BufferedImage(drawing.workingSession.screen_capture_area!!.width.toInt(),
                    drawing.workingSession.screen_capture_area!!.height.toInt(), BufferedImage.TYPE_INT_ARGB)
            val g2 = bufferedImage.createGraphics()
            g2.color = Color.WHITE
            g2.fill(Rectangle2D.Double(0.0, 0.0, drawing.workingSession.screen_capture_area!!.width,
                    drawing.workingSession.screen_capture_area!!.height))
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g2.background = Color.white
            if (secondaryStructureDrawing != null)
                secondaryStructureDrawing.draw(g2);
            else
                drawing.draw(g2);
            g2.dispose()
            drawing.workingSession.viewX += drawing.workingSession.screen_capture_area!!.minX
            drawing.workingSession.viewY += drawing.workingSession.screen_capture_area!!.minY
            return bufferedImage
        }
        return null
    }
}