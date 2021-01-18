package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.JPanel

class Canvas2D(val mediator: Mediator) : JPanel() {

    var knobs: MutableList<JunctionKnob> = arrayListOf()
    private var selectionShapes: MutableList<SelectionShape> = arrayListOf()
    lateinit private var offScreenBuffer: Image
    var translateX = 0.0
    var translateY = 0.0
    private var fps: Long = 0

    init {
        this.mediator.canvas2D = this
    }

    fun load2D(drawing: SecondaryStructureDrawing, chimeraSession:File? = null, pdbFile:File? = null) {
        val previousPdbSource = this.mediator.secondaryStructureDrawingProperty.get()?.secondaryStructure?.rna?.source //to avoid to re-open the same PDB file if we load 2Ds from the same 3Ds
        this.mediator.secondaryStructureDrawingProperty.set(drawing)
        this.fps = 0
        this.knobs.clear()
        this.selectionShapes.clear()
        this.mediator.secondaryStructureDrawingProperty.get()?.let { it ->
            mediator.explorer.load(it)
            if (chimeraSession != null && pdbFile != null)
                mediator.chimeraDriver.restoreSession(chimeraSession, pdbFile)
            else {
                if (!it.secondaryStructure.rna.source.equals(previousPdbSource))
                    mediator.chimeraDriver.closeSession()
                if (it.secondaryStructure.rna.source.startsWith("file:") && it.secondaryStructure.rna.source.endsWith(".pdb"))
                    mediator.chimeraDriver.loadTertiaryStructure(
                        File(
                            it.secondaryStructure.rna.source.split(":").last()
                        )
                    )
            }
        }
        this.repaint();
        /*
        Timer("", false).schedule(1000) {
            mediator.explorer.getTreeViewItemFor(mediator.explorer.treeTableView.root, secondaryStructureDrawing).value.lineWidth = "2.0"
            mediator.explorer.getTreeViewItemFor(mediator.explorer.treeTableView.root, secondaryStructureDrawing).value.color = getHTMLColorString(Color(18,114,27))
            val hits = arrayListOf<TreeItem<ExplorerItem>>()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                HelixDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.fullDetails = "true"
                it.value.color = getHTMLColorString(Color(18,114,27))
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                JunctionDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.fullDetails = "true"
                it.value.color = getHTMLColorString(Color(18,114,27))
                if ((it.value.drawingElement as JunctionDrawing).junctionCategory == JunctionType.FourWay) {
                    for (c in it.parent.children.first().children) {
                        c.value.fullDetails = "true"
                        for (_c in c.children)
                            _c.value.fullDetails = "false"
                    }
                }
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                ResidueDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.lineWidth = "0.5"
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                InteractionSymbolDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.fullDetails = "false"
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                ResidueLetterDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.fullDetails = "false"
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                AShapeDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.color = getHTMLColorString(Color(194,19,19))
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                GShapeDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.color = getHTMLColorString(Color(208, 150,58))
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                UShapeDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.color = getHTMLColorString(Color(54,98,125))
            }
            hits.clear()
            mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                CShapeDrawing::class.java.isInstance(
                    drawingElement
                )
            })
            hits.forEach {
                it.value.color = getHTMLColorString(Color(104,53,134))
            }
            hits.clear()
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        var i = 0
        Timer("", false).schedule(2000, period = 1000) {
            i++
            val hits = arrayListOf<TreeItem<ExplorerItem>>()
            if (i%3 == 0) {
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    AShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(208, 150,58))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    GShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(104,53,134))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    UShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(194,19,19))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    CShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(54,98,125))
                }
                hits.clear()
            }
            else if (i%2 == 0) {
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    AShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(54,98,125))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    GShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(194,19,19))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    UShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(104,53,134))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    CShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(208, 150,58))
                }
                hits.clear()
            } else {
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    AShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(104,53,134))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    GShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(194,19,19))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    UShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(54,98,125))
                }
                hits.clear()
                mediator.explorer.getAllTreeViewItems(hits, mediator.explorer.treeTableView.root, { drawingElement: DrawingElement? ->
                    CShapeDrawing::class.java.isInstance(
                        drawingElement
                    )
                })
                hits.forEach {
                    it.value.color = getHTMLColorString(Color(194,19,19))
                }
                hits.clear()
            }
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }*/
    }

    fun centerDisplayOn(frame: Rectangle2D) {
        this.mediator.secondaryStructureDrawingProperty.get()?.let { drawing ->
            drawing.workingSession.viewX = 0.0
            drawing.workingSession.viewY = 0.0
            var at = AffineTransform()
            at.translate(drawing.viewX, drawing.viewY)
            at.scale(drawing.zoomLevel, drawing.zoomLevel)
            var transformedBounds = at.createTransformedShape(frame)
            //we center the view on the new structure
            drawing.workingSession.viewX += this.getBounds().bounds2D.centerX - transformedBounds.bounds2D.centerX
            drawing.workingSession.viewY += this.getBounds().bounds2D.centerY - transformedBounds.bounds2D.centerY
            this.repaint()
        }
    }

    fun fitStructure() {
        this.mediator.secondaryStructureDrawingProperty.get()?.let { drawing ->
            drawing.fitTo(this.bounds)
            this.repaint()
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        this.mediator.secondaryStructureDrawingProperty.get()?.let { drawing ->
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g2.background = Color.white
            g2.color = Color.BLACK
            if (drawing.workingSession.is_screen_capture) {
                g.stroke = BasicStroke(drawing.zoomLevel.toFloat() * RnartistConfig.selectionWidth)
                g2.draw(drawing.workingSession.screen_capture_area)
            }
            val start = System.currentTimeMillis()
            val at = AffineTransform()
            at.translate(drawing.viewX, drawing.viewY)
            at.scale(drawing.zoomLevel, drawing.zoomLevel)

            drawing.draw(g2, at, Rectangle2D.Double(0.0, 0.0, this.size.getWidth(), this.size.getHeight()))

            if (!drawing.quickDraw) {
                this.knobs.forEach {
                    it.draw(g, at)
                }
                this.selectionShapes.forEach {
                    it.draw(g, at)
                }
            }

            println((System.currentTimeMillis() - start) / 1000.0)

            //this.fps = if (t> this.fps) t else this.fps
            //println("FPS: ${this.fps}")
        }
    }

    override fun update(g: Graphics) {
        val gr: Graphics2D
        if (this.offScreenBuffer == null ||
            !(offScreenBuffer.getWidth(this) == this.size.width
                    && offScreenBuffer.getHeight(this) == this.size.height)
        ) {
            this.offScreenBuffer = this.createImage(this.size.width, this.size.height)
        }
        // We need to use our buffer Image as a Graphics object:
        gr = this.offScreenBuffer.getGraphics() as Graphics2D
        paintComponent(gr)
        g.drawImage(this.offScreenBuffer, 0, 0, this)
    }

    fun screenCapture(secondaryStructureDrawing: SecondaryStructureDrawing?): BufferedImage? {
        this.mediator.secondaryStructureDrawingProperty.get()?.let { drawing ->
            var bufferedImage: BufferedImage?
            drawing.workingSession.viewX -= drawing.workingSession.screen_capture_area!!.minX
            drawing.workingSession.viewY -= drawing.workingSession.screen_capture_area!!.minY
            bufferedImage = BufferedImage(
                drawing.workingSession.screen_capture_area!!.width.toInt(),
                drawing.workingSession.screen_capture_area!!.height.toInt(), BufferedImage.TYPE_INT_ARGB
            )
            val g2 = bufferedImage.createGraphics()
            g2.color = Color.WHITE
            g2.fill(
                Rectangle2D.Double(
                    0.0, 0.0, drawing.workingSession.screen_capture_area!!.width,
                    drawing.workingSession.screen_capture_area!!.height
                )
            )
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g2.background = Color.white
            val at = AffineTransform()
            at.translate(drawing.viewX, drawing.viewY)
            at.scale(drawing.zoomLevel, drawing.zoomLevel)
            if (secondaryStructureDrawing != null)
                secondaryStructureDrawing.draw(
                    g2,
                    at,
                    Rectangle2D.Double(0.0, 0.0, this.size.getWidth(), this.size.getHeight())
                );
            else
                drawing.draw(g2, at, Rectangle2D.Double(0.0, 0.0, this.size.getWidth(), this.size.getHeight()));
            g2.dispose()
            drawing.workingSession.viewX += drawing.workingSession.screen_capture_area!!.minX
            drawing.workingSession.viewY += drawing.workingSession.screen_capture_area!!.minY
            return bufferedImage
        }
        return null
    }

    fun clearSelection() {
        this.mediator.secondaryStructureDrawingProperty.get()?.let {
            this.knobs.clear()
            this.selectionShapes.clear()
            repaint()
            mediator.chimeraDriver.selectionCleared()
        }

    }

    fun getSelection(): List<DrawingElement> {
        return this.selectionShapes.map { it.element }
    }

    fun getSelectionAbsPositions(): List<Int> {
        val absPositions = mutableSetOf<Int>()
        this.selectionShapes.map { it.element }.forEach {
            absPositions.addAll(it.location.positions)
        }
        return absPositions.toList()
    }

    fun getSelectionFrame(): Rectangle2D? {
        val allSelectionPoints = this.selectionShapes.flatMap { it.element.selectionPoints }
        allSelectionPoints.minByOrNull { it.x }?.x?.let { minX ->
            allSelectionPoints.minByOrNull { it.y }?.y?.let { minY ->
                allSelectionPoints.maxByOrNull { it.x }?.x?.let { maxX ->
                    allSelectionPoints.maxByOrNull { it.y }?.y?.let { maxY ->
                        return Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY)
                    }
                }
            }
        }
        return null

    }

    fun isInSelection(el: DrawingElement): Boolean {
        return this.selectionShapes.any { it.element == el }
    }

    fun addToSelection(el: DrawingElement) {
        this.mediator.secondaryStructureDrawingProperty.get()?.let {
            if (el is JunctionDrawing)
                this.knobs.add(JunctionKnob(mediator, el))
            this.selectionShapes.add(SelectionShape(mediator, el))
            repaint()
            mediator.chimeraDriver.selectResidues(
                getSelectionAbsPositions(),
                it.secondaryStructure.rna.name
            )
            if (mediator.rnartist.isCenterDisplayOnSelection)
                mediator.chimeraDriver.setFocus(
                        getSelectionAbsPositions(),
                        it.secondaryStructure.rna.name
                    )
        }
    }

    fun removeFromSelection(el: DrawingElement) {
        this.mediator.secondaryStructureDrawingProperty.get()?.let {
            this.knobs.removeIf { it.junction == el }
            this.selectionShapes.removeIf { it.element == el }
            repaint()
        }
    }

    fun isSelected(el: DrawingElement): Boolean {
        return this.selectionShapes.any { it.element == el }
    }

    fun structuralDomainsSelected(): List<StructuralDomain> {
        val domains = mutableListOf<StructuralDomain>()
        domains.addAll(this.knobs.map { it.junction })
        domains.addAll(this.selectionShapes.filter { it.element is StructuralDomain }
            .map { it.element as StructuralDomain })
        return domains
    }
}