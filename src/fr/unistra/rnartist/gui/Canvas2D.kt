package fr.unistra.rnartist.gui

import fr.unistra.rnartist.model.*
import javafx.beans.property.SimpleObjectProperty
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import javax.swing.JPanel

class Canvas2D(val mediator: Mediator): JPanel() {

    var secondaryStructureDrawing = SimpleObjectProperty<SecondaryStructureDrawing>()
    lateinit private var offScreenBuffer: Image
    var translateX = 0.0
    var translateY = 0.0

    init {
        this.mediator.canvas2D = this
        this.secondaryStructureDrawing.addListener({ _, _, _ ->
            mediator.rnartist.activateSaveButtons()
        })
    }

    fun load2D(drawing:SecondaryStructureDrawing) {
        mediator.toolbox.residues.clear()
        this.secondaryStructureDrawing.value = drawing
        mediator.toolbox.loadTheme(this.secondaryStructureDrawing.get().theme.themeParams)
        /*this.secondaryStructureDrawing.value.secondaryStructure.rna.seq.forEachIndexed { index:Int, res:Char ->
            mediator.toolbox.residues.add(Residue(index+1, res))
        }*/
        mediator.toolbox.junctionKnobs.children.clear()
        for (jc in this.secondaryStructureDrawing.get().allJunctions) {
            mediator.toolbox.addJunctionKnob(jc)
        }
        this.repaint()
    }

    fun center2D() {
        this.secondaryStructureDrawing.get().workingSession.viewX = 0.0
        this.secondaryStructureDrawing.get().workingSession.viewY = 0.0
        var at = AffineTransform()
        at.translate(this.secondaryStructureDrawing.get().workingSession.viewX, this.secondaryStructureDrawing.get().workingSession.viewY)
        at.scale(this.secondaryStructureDrawing.get().workingSession.finalZoomLevel, this.secondaryStructureDrawing.get().workingSession.finalZoomLevel)
        var transformedBounds = at.createTransformedShape(this.secondaryStructureDrawing.get().getBounds())
        //we center the view on the new structure
        this.secondaryStructureDrawing.get().workingSession.viewX += this.getBounds().bounds2D.centerX - transformedBounds.bounds2D.centerX
        this.secondaryStructureDrawing.get().workingSession.viewY += this.getBounds().bounds2D.centerY - transformedBounds.bounds2D.centerY
        this.repaint()
    }

    fun fit2D(drawing: SecondaryStructureDrawing) {
        drawing.workingSession.viewX = 0.0
        drawing.workingSession.viewY = 0.0
        drawing.workingSession.finalZoomLevel = 1.0
        var at = AffineTransform()
        at.translate(drawing.workingSession.viewX, drawing.workingSession.viewY)
        at.scale(drawing.workingSession.finalZoomLevel, drawing.workingSession.finalZoomLevel)
        var transformedBounds = at.createTransformedShape(drawing.getBounds())
        //we compute the zoomLevel to fit the structure in the frame of the canvas2D
        val widthRatio = transformedBounds.bounds2D.width/this.getBounds().width
        val heightRatio = transformedBounds.bounds2D.height/this.getBounds().height
        drawing.workingSession.finalZoomLevel = if (widthRatio > heightRatio) 1.0/widthRatio else 1.0/heightRatio
        //We recompute the bounds of the structure with this new zoom level
        at = AffineTransform()
        at.translate(drawing.workingSession.viewX, drawing.workingSession.viewY)
        at.scale(drawing.workingSession.finalZoomLevel, drawing.workingSession.finalZoomLevel)
        transformedBounds = at.createTransformedShape(drawing.getBounds())
        //we center the view on the new structure
        drawing.workingSession.viewX += this.getBounds().bounds2D.centerX - transformedBounds.bounds2D.centerX
        drawing.workingSession.viewY += this.getBounds().bounds2D.centerY - transformedBounds.bounds2D.centerY
        this.repaint()
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2.background = Color.white
        g2.color = Color.BLACK
        if (this.secondaryStructureDrawing.get() != null && this.secondaryStructureDrawing.get().workingSession.screen_capture)
            g2.draw(this.secondaryStructureDrawing.get().workingSession.screen_capture_area)
        if (this.secondaryStructureDrawing.get() != null)
            this.secondaryStructureDrawing.get().draw(g2)
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

    fun screenCapture(secondaryStructureDrawing:SecondaryStructureDrawing?): BufferedImage? {
        var bufferedImage: BufferedImage?
        this.secondaryStructureDrawing.get().workingSession.viewX -= this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.minX
        this.secondaryStructureDrawing.get().workingSession.viewY -= this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.minY
        bufferedImage = BufferedImage(this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.width.toInt(),
                this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.height.toInt() , BufferedImage.TYPE_INT_ARGB)
        val g2 = bufferedImage.createGraphics()
        g2.color = Color.WHITE
        g2.fill(Rectangle2D.Double(0.0, 0.0, this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.width,
                this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.height))
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2.background = Color.white
        if (secondaryStructureDrawing != null)
            secondaryStructureDrawing.draw(g2);
        else
            this.secondaryStructureDrawing.get().draw(g2);
        g2.dispose()
        this.secondaryStructureDrawing.get().workingSession.viewX += this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.minX
        this.secondaryStructureDrawing.get().workingSession.viewY += this.secondaryStructureDrawing.get().workingSession.screen_capture_area!!.minY
        return bufferedImage
    }
}