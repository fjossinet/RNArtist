package fr.unistra.rnartist.gui

import fr.unistra.rnartist.gui.Mediator
import fr.unistra.rnartist.model.SecondaryStructure
import fr.unistra.rnartist.model.SecondaryStructureDrawing
import javafx.beans.property.SimpleObjectProperty
import java.awt.*
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
        this.secondaryStructureDrawing.addListener({ obs, old, selected ->
            mediator.rnartist.activateSaveButton()
        })
    }

    fun load2D(ss:SecondaryStructure) {
        this.secondaryStructureDrawing.value = SecondaryStructureDrawing(ss, this.bounds, mediator.drawingConfiguration)
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
        if (mediator.graphicsContext.screen_capture)
            g2.draw(mediator.graphicsContext.screen_capture_area)
        if (this.secondaryStructureDrawing.get() != null)
            this.secondaryStructureDrawing.get().draw(g2, mediator.graphicsContext)
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

    fun screenCapture(): BufferedImage? {
        var bufferedImage: BufferedImage? = null
        mediator.graphicsContext.viewX -= mediator.graphicsContext.screen_capture_area!!.minX
        mediator.graphicsContext.viewY -= mediator.graphicsContext.screen_capture_area!!.minY
        bufferedImage = BufferedImage(mediator.graphicsContext.screen_capture_area!!.width.toInt(),
                mediator.graphicsContext.screen_capture_area!!.height.toInt() , BufferedImage.TYPE_INT_ARGB)
        val g2 = bufferedImage.createGraphics()
        g2.color = Color.WHITE
        g2.fill(Rectangle2D.Double(0.0, 0.0, mediator.graphicsContext.screen_capture_area!!.width,
                mediator.graphicsContext.screen_capture_area!!.height))
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2.background = Color.white
        this.secondaryStructureDrawing.get().draw(g2, mediator.graphicsContext);
        g2.dispose()
        mediator.graphicsContext.viewX += mediator.graphicsContext.screen_capture_area!!.minX
        mediator.graphicsContext.viewY += mediator.graphicsContext.screen_capture_area!!.minY
        return bufferedImage
    }
}