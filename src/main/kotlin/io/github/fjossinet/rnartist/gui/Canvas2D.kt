package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import javax.swing.JPanel


class Canvas2D(val mediator: Mediator) : JPanel() {

    lateinit private var offScreenBuffer: Image
    var translateX = 0.0
    var translateY = 0.0

    init {
        this.mediator.canvas2D = this
    }

    fun centerDisplayOn(frame: Rectangle2D) {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            drawingDisplayed.secondaryStructureDrawing.workingSession.viewX = 0.0
            drawingDisplayed.secondaryStructureDrawing.workingSession.viewY = 0.0
            var at = AffineTransform()
            at.translate(drawingDisplayed.secondaryStructureDrawing.viewX, drawingDisplayed.secondaryStructureDrawing.viewY)
            at.scale(drawingDisplayed.secondaryStructureDrawing.zoomLevel, drawingDisplayed.secondaryStructureDrawing.zoomLevel)
            var transformedBounds = at.createTransformedShape(frame)
            //we center the view on the new structure
            drawingDisplayed.secondaryStructureDrawing.workingSession.viewX += this.getBounds().bounds2D.centerX - transformedBounds.bounds2D.centerX
            drawingDisplayed.secondaryStructureDrawing.workingSession.viewY += this.getBounds().bounds2D.centerY - transformedBounds.bounds2D.centerY
            this.repaint()
        }
    }

    fun fitStructure(selectionFrame:Rectangle2D?, ratio:Double = 1.0) {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            selectionFrame?.let {
                drawingDisplayed.secondaryStructureDrawing.fitViewTo(this.bounds, it, ratio)
            } ?: run {
                drawingDisplayed.secondaryStructureDrawing.fitViewTo(this.bounds)
            }
            this.repaint()
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2.color = Color.white;
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.color = Color.BLACK
        this.mediator.currentDrawing.get()?.let { rnArtistDrawing ->
            val start = System.currentTimeMillis()
            val at = AffineTransform()
            at.translate(rnArtistDrawing.secondaryStructureDrawing.viewX, rnArtistDrawing.secondaryStructureDrawing.viewY)
            at.scale(rnArtistDrawing.secondaryStructureDrawing.zoomLevel, rnArtistDrawing.secondaryStructureDrawing.zoomLevel)

            val drawingArea = Rectangle2D.Double(0.0, 0.0, this.size.getWidth(), this.size.getHeight())
            rnArtistDrawing.secondaryStructureDrawing
                .draw(g2, at, drawingArea, rnArtistDrawing.selectedDrawings)

            if (!rnArtistDrawing.secondaryStructureDrawing.quickDraw) {
                rnArtistDrawing.selectedDrawings.forEach { selectedElement ->
                    g2.stroke = mediator.drawingHighlighted.get()?.let { highlightedElement ->
                        if (highlightedElement == selectedElement) BasicStroke(
                            2f*rnArtistDrawing.secondaryStructureDrawing.zoomLevel.toFloat(), BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER
                        ) else BasicStroke(
                            1f*rnArtistDrawing.secondaryStructureDrawing.zoomLevel.toFloat(), BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER
                        )
                    } ?: run {
                        BasicStroke(
                            2f*rnArtistDrawing.secondaryStructureDrawing.zoomLevel.toFloat(), BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER
                        )
                    }
                    g2.color = mediator.drawingHighlighted.get()?.let { highlightedElement ->
                        if (highlightedElement == selectedElement) if (selectedElement is ResidueLetterDrawing) Color.BLACK else selectedElement.getColor() else Color.LIGHT_GRAY
                    } ?: run {
                        if (selectedElement is ResidueLetterDrawing) Color.BLACK else selectedElement.getColor()
                    }

                    val s = at.createTransformedShape(selectedElement.selectionShape)
                    if (drawingArea.contains(Point2D.Double(s.bounds2D.centerX, s.bounds2D.centerY)))
                        g2.draw(s)
                }
            }
            //println((System.currentTimeMillis() - start) / 1000.0)
        }
    }

    override fun update(g: Graphics) {
        val gr: Graphics2D
        if (this.offScreenBuffer == null ||
            !(offScreenBuffer.getWidth(this) == this.size.width
                    && offScreenBuffer.getHeight(this) == this.size.height)
        ) {
            this.offScreenBuffer = BufferedImage(this.size.width, this.size.height,BufferedImage.TYPE_INT_ARGB)
        }
        // We need to use our buffer Image as a Graphics object:
        gr = this.offScreenBuffer.getGraphics() as Graphics2D
        paintComponent(gr)
        g.drawImage(this.offScreenBuffer, 0, 0, this)
    }

    //++++++ Selection ++++++++++++

    fun clearSelection() {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            drawingDisplayed.selectedDrawings.clear()
            //mediator.chimeraDriver.selectionCleared()
            repaint()
        }

    }

    fun getSelection(): List<DrawingElement> {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            return drawingDisplayed.selectedDrawings.map { it }
        }
        return listOf()
    }

    fun getSelectedPositions(): List<Int> {
        val absPositions = mutableSetOf<Int>()
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            drawingDisplayed.selectedDrawings.map { it }.forEach {
                absPositions.addAll(it.location.positions)
            }
        }
        return absPositions.toList()
    }

    fun getSelectedResidues():List<ResidueDrawing> {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            return drawingDisplayed.secondaryStructureDrawing.getResiduesFromAbsPositions(*getSelectedPositions().toIntArray())
        }
        return listOf<ResidueDrawing>()
    }

    fun getSelectionFrame(): Rectangle2D? {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            val firstShape = drawingDisplayed.selectedDrawings.firstOrNull()?.selectionShape
            firstShape?.let { firstShape ->
                var frame = firstShape.bounds2D
                drawingDisplayed.selectedDrawings.forEach {
                    frame = frame.createUnion(it.selectionShape.bounds2D)
                }
                return frame
            }
        }
        return null
    }

    fun isSelected(el: DrawingElement?): Boolean {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            return drawingDisplayed.selectedDrawings.any { it == el }
        }
        return false
    }

    fun addToSelection(el: List<DrawingElement>) {
        el.forEach {
            this.addToSelection(it)
        }
    }

    fun addToSelection(el: DrawingElement?) {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            el?.let { el ->
                //we want to show only one knob and only if the junction is the only element selected (this is needed for the script editor to add a junction keyword that get precisely the junction location and not a location composed of several elements selected
                drawingDisplayed.selectedDrawings.add(el)
                repaint()
                /*mediator.chimeraDriver.selectResidues(
                    getSelectedPositions()
                )*/
            }
        }
    }

    fun removeFromSelection(el: DrawingElement?) {
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            drawingDisplayed.selectedDrawings.removeIf { it == el }
            repaint()
        }
    }

    fun structuralDomainsSelected(): List<StructuralDomainDrawing> {
        val domains = mutableListOf<StructuralDomainDrawing>()
        this.mediator.currentDrawing.get()?.let { drawingDisplayed ->
            domains.addAll(drawingDisplayed.selectedDrawings.filter { it is StructuralDomainDrawing }
                .map { it as StructuralDomainDrawing })
        }
        return domains
    }

    //code to animate the 2D
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