package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.RnartistConfig.save
import io.github.fjossinet.rnartist.core.layout
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.theme
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistDrawing
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistTask
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.animation.AnimationTimer
import javafx.animation.RotateTransition
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.embed.swing.SwingNode
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.effect.InnerShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.*
import javafx.util.Duration
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.javafx.Icon
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.script.ScriptEngineManager
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.random.Random


class RNArtist : Application() {

    val mediator: Mediator
    val verticalSplitPane: SplitPane
    private val upperPanel: UpperPanel
    lateinit var stage: Stage
    private val root: BorderPane

    companion object {
        val RNArtistGUIColor = Color(51.0 / 255.0, 51.0 / 255.0, 51.0 / 255.0, 1.0)
    }

    /*fun startComputingAnimation() {
        this.computingAnimation.schedule(object : TimerTask() {
            override fun run() {
                if (leftComputingBar.isOn && rightComputingBar.isOn) {
                    leftComputingBar.isOn = true
                    rightComputingBar.isOn = false
                } else {
                    leftComputingBar.isOn = !leftComputingBar.isOn
                    rightComputingBar.isOn = !rightComputingBar.isOn
                }
            }
        },0, 200)
    }

    fun stopComputingAnimation() {
        this.computingAnimation.cancel()
        leftComputingBar.isOn = true
        rightComputingBar.isOn = !rightComputingBar.isOn
    }*/

    fun getInstallDir(): String {
        return File(
            this::class.java.protectionDomain.codeSource.location
                .toURI()
        ).parentFile.parent
    }

    private inner class UpperPanel() : GridPane() {

        //private var scrollCounter = 0
        private val upl: UpperLeftPanel
        private val upr: UpperRightPanel

        init {
            val rowConstraint = RowConstraints()
            rowConstraint.vgrow = Priority.ALWAYS

            val cConstraints1 = ColumnConstraints()
            cConstraints1.hgrow = Priority.NEVER
            val cConstraints2 = ColumnConstraints()
            cConstraints2.hgrow = Priority.ALWAYS
            val cConstraints3 = ColumnConstraints()
            cConstraints3.hgrow = Priority.NEVER

            this.columnConstraints.addAll(cConstraints1, cConstraints2, cConstraints3)
            this.rowConstraints.add(rowConstraint)

            //++++++ Canvas2D
            val swingNode = SwingNode()
            swingNode.onMouseMoved = EventHandler { mouseEvent: MouseEvent? ->
                mediator.currentDrawing.get()?.secondaryStructureDrawing?.let {
                    it.quickDraw = false //a trick if after the scroll event the quickdraw is still true
                }
            }
            swingNode.onMouseClicked = EventHandler { mouseEvent: MouseEvent ->
                if (mouseEvent.button == MouseButton.PRIMARY) {
                    mediator.currentDrawing.get()?.let mouseClicked@{ drawingLoaded ->
                        val at = AffineTransform()
                        at.translate(
                            drawingLoaded.secondaryStructureDrawing.workingSession.viewX,
                            drawingLoaded.secondaryStructureDrawing.workingSession.viewY
                        )
                        at.scale(
                            drawingLoaded.secondaryStructureDrawing.workingSession.zoomLevel,
                            drawingLoaded.secondaryStructureDrawing.workingSession.zoomLevel
                        )
                        for (h in drawingLoaded.secondaryStructureDrawing.workingSession.helicesDrawn) {
                            var shape = h.selectionShape
                            if (at.createTransformedShape(shape)
                                    .contains(mouseEvent.x, mouseEvent.y)
                            ) {
                                for (r in h.residues) {
                                    shape = r.selectionShape
                                    if (r.isFullDetails() && at.createTransformedShape(shape)
                                            .contains(mouseEvent.x, mouseEvent.y)
                                    ) {
                                        if (!mediator.canvas2D.isSelected(r) && !mediator.canvas2D.isSelected(r.parent) && !mediator.canvas2D.isSelected(
                                                r.parent!!.parent
                                            )
                                        ) {
                                            mediator.canvas2D.addToSelection(r)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent) && !mediator.canvas2D.isSelected(
                                                r.parent!!.parent
                                            )
                                        ) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent!!.parent)) {
                                            mediator.canvas2D.removeFromSelection(r.parent)
                                            mediator.canvas2D.addToSelection(r.parent!!.parent)
                                            return@mouseClicked
                                        }
                                    }
                                }
                                for (bondDrawing in h.phosphoBonds) {
                                    shape = bondDrawing.selectionShape
                                    if (at.createTransformedShape(shape)
                                            .contains(mouseEvent.x, mouseEvent.y)
                                    ) {
                                        if (!mediator.canvas2D.isSelected(bondDrawing) && !mediator.canvas2D.isSelected(
                                                bondDrawing.parent
                                            )
                                        ) {
                                            mediator.canvas2D.addToSelection(bondDrawing)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(bondDrawing.parent)) {
                                            mediator.canvas2D.removeFromSelection(bondDrawing)
                                            mediator.canvas2D.addToSelection(bondDrawing.parent)
                                            return@mouseClicked
                                        }
                                    }
                                }
                                for (interaction in h.secondaryInteractions) {
                                    shape = interaction.selectionShape
                                    if (interaction.isFullDetails() && at.createTransformedShape(shape)
                                            .contains(mouseEvent.x, mouseEvent.y)
                                    ) {
                                        if (!mediator.canvas2D.isSelected(interaction.interactionSymbol) && !mediator.canvas2D.isSelected(
                                                interaction
                                            ) && !mediator.canvas2D.isSelected(
                                                interaction.parent
                                            )
                                        ) {
                                            mediator.canvas2D.addToSelection(interaction.interactionSymbol)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(interaction) && !mediator.canvas2D.isSelected(
                                                interaction.parent
                                            )
                                        ) {
                                            mediator.canvas2D.removeFromSelection(interaction.interactionSymbol)
                                            mediator.canvas2D.addToSelection(interaction)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(interaction.parent)) {
                                            mediator.canvas2D.removeFromSelection(interaction)
                                            mediator.canvas2D.addToSelection(interaction.parent)
                                            return@mouseClicked
                                        }
                                    }
                                }
                                if (!mediator.canvas2D.isSelected(h)) {
                                    mediator.canvas2D.addToSelection(h)
                                    return@mouseClicked
                                } else {
                                    var p = h.parent
                                    while (p != null && mediator.canvas2D.isSelected(p)) {
                                        p = p.parent
                                    }
                                    if (p == null) {
                                        mediator.canvas2D.addToSelection(h)
                                    } else {
                                        mediator.canvas2D.addToSelection(p)
                                    }
                                    return@mouseClicked
                                }
                            }
                        }
                        for (j in drawingLoaded.secondaryStructureDrawing.workingSession.junctionsDrawn) {
                            var shape = j.selectionShape
                            if (at.createTransformedShape(shape)
                                    .contains(mouseEvent.x, mouseEvent.y)
                            ) {
                                for (r in j.residues) {
                                    shape = r.selectionShape
                                    if (r.isFullDetails() && at.createTransformedShape(shape)
                                            .contains(mouseEvent.x, mouseEvent.y)
                                    ) {
                                        if (!mediator.canvas2D.isSelected(r) && !mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.addToSelection(r)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            return@mouseClicked
                                        }
                                    }
                                }

                                for (bondDrawing in j.phosphoBonds) {
                                    shape = bondDrawing.selectionShape
                                    if (at.createTransformedShape(shape)
                                            .contains(mouseEvent.x, mouseEvent.y)
                                    ) {
                                        if (!mediator.canvas2D.isSelected(bondDrawing) && !mediator.canvas2D.isSelected(
                                                bondDrawing.parent
                                            )
                                        ) {
                                            mediator.canvas2D.addToSelection(bondDrawing)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(bondDrawing.parent)) {
                                            mediator.canvas2D.removeFromSelection(bondDrawing)
                                            mediator.canvas2D.addToSelection(bondDrawing.parent)
                                            return@mouseClicked
                                        }
                                    }
                                }

                                if (!mediator.canvas2D.isSelected(j)) {
                                    mediator.canvas2D.addToSelection(j)
                                    return@mouseClicked
                                } else {
                                    var p = j.parent
                                    while (p != null && mediator.canvas2D.isSelected(p)) {
                                        p = p.parent
                                    }
                                    if (p == null) {
                                        mediator.canvas2D.addToSelection(j)
                                    } else {
                                        mediator.canvas2D.addToSelection(p)
                                    }
                                    return@mouseClicked
                                }
                            }
                        }
                        for (ss in drawingLoaded.secondaryStructureDrawing.workingSession.singleStrandsDrawn) {
                            var shape = ss.selectionShape
                            if (at.createTransformedShape(shape)
                                    .contains(mouseEvent.x, mouseEvent.y)
                            ) {
                                for (r in ss.residues) {
                                    shape = r.selectionShape
                                    if (r.isFullDetails() && at.createTransformedShape(shape)
                                            .contains(mouseEvent.x, mouseEvent.y)
                                    ) {
                                        if (!mediator.canvas2D.isSelected(r) && !mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.addToSelection(r)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            return@mouseClicked
                                        }
                                    }
                                }
                                if (!mediator.canvas2D.isSelected(ss)) {
                                    mediator.canvas2D.addToSelection(ss)
                                    return@mouseClicked
                                }
                            }
                        }
                        if (mouseEvent.clickCount == 2) {
                            //no selection
                            mediator.canvas2D.clearSelection()
                        }
                    }
                }
            }
            swingNode.onMouseDragged = EventHandler { mouseEvent: MouseEvent ->
                if (mouseEvent.button == MouseButton.SECONDARY) {
                    mediator.currentDrawing.get()?.secondaryStructureDrawing?.let {
                        it.quickDraw = true
                        val transX: Double = mouseEvent.x - mediator.canvas2D.translateX
                        val transY: Double = mouseEvent.y - mediator.canvas2D.translateY
                        it.workingSession.moveView(transX, transY)
                        mediator.canvas2D.translateX = mouseEvent.x
                        mediator.canvas2D.translateY = mouseEvent.y
                        mediator.canvas2D.repaint()
                    }
                }
            }
            swingNode.onMouseReleased = EventHandler { mouseEvent: MouseEvent ->
                if (mouseEvent.button == MouseButton.SECONDARY) {
                    mediator.currentDrawing.get()?.secondaryStructureDrawing?.let {
                        it.quickDraw = false
                        mediator.canvas2D.translateX = 0.0
                        mediator.canvas2D.translateY = 0.0
                        mediator.canvas2D.repaint()
                    }
                }
            }
            swingNode.onMousePressed = EventHandler { mouseEvent: MouseEvent ->
                if (mouseEvent.button == MouseButton.SECONDARY) {
                    mediator.currentDrawing.get()?.let { drawingLoaded ->
                        mediator.canvas2D.translateX = mouseEvent.x
                        mediator.canvas2D.translateY = mouseEvent.y
                    }
                }
            }
            /*swingNode.onScroll = EventHandler { scrollEvent: ScrollEvent ->
                mediator.currentDrawing.get()?.let { drawingLoaded ->
                    drawingLoaded.drawing.quickDraw = true
                    scrollCounter++
                    val th = Thread {
                        try {
                            Thread.sleep(100)
                            if (scrollCounter == 1) {
                                drawingLoaded.drawing.quickDraw = false
                                mediator.canvas2D.repaint()
                            }
                            scrollCounter--
                        } catch (e1: Exception) {
                            e1.printStackTrace()
                        }
                    }
                    th.isDaemon = true
                    th.start()
                    val realMouse =
                        Point2D.Double(
                            (scrollEvent.x - drawingLoaded.drawing.workingSession.viewX) / drawingLoaded.drawing.workingSession.zoomLevel,
                            (scrollEvent.y - drawingLoaded.drawing.workingSession.viewY) / drawingLoaded.drawing.workingSession.zoomLevel
                        )
                    val notches = scrollEvent.deltaY
                    if (notches < 0) drawingLoaded.drawing.workingSession.setZoom(1.25)
                    if (notches > 0) drawingLoaded.drawing.workingSession.setZoom(1.0 / 1.25)
                    val newRealMouse =
                        Point2D.Double(
                            (scrollEvent.x - drawingLoaded.drawing.workingSession.viewX) / drawingLoaded.drawing.workingSession.zoomLevel,
                            (scrollEvent.y - drawingLoaded.drawing.workingSession.viewY) / drawingLoaded.drawing.workingSession.zoomLevel
                        )
                    drawingLoaded.drawing.workingSession.moveView(
                        (newRealMouse.getX() - realMouse.getX()) * drawingLoaded.drawing.workingSession.zoomLevel,
                        (newRealMouse.getY() - realMouse.getY()) * drawingLoaded.drawing.workingSession.zoomLevel
                    )
                    mediator.canvas2D.repaint()
                }
            }*/
            createSwingContent(swingNode)

            val vBox = VBox()
            VBox.setVgrow(swingNode, Priority.ALWAYS)
            vBox.padding = Insets(10.0, 10.0, 20.0, 10.0)
            vBox.background = Background(BackgroundFill(Color.WHITE, CornerRadii(10.0), Insets(5.0)))
            vBox.effect = DropShadow()
            vBox.children.add(swingNode)
            this.upl = UpperLeftPanel()
            this.add(this.upl, 0, 0, 1, 2)
            this.add(vBox, 1, 0, 1, 1)
            val hBox = HBox()
            hBox.alignment = Pos.CENTER
            //hBox.children.add(leftComputingBar)
            hBox.children.add(NavigationBar())
            hBox.children.add(UndoRedoThemeBar())
            hBox.children.add(UndoRedoLayoutBar())
            //hBox.children.add(AnimationBar())
            hBox.children.add(LateralPanelsBar())
            //hBox.children.add(rightComputingBar)
            this.add(hBox, 1, 1, 1, 1)
            this.upr = UpperRightPanel()
            this.add(this.upr, 2, 0, 1, 2)
        }

        fun removeLeftPanel() = this.children.remove(this.upl)
        fun restoreLeftPanel() = this.add(this.upl, 0, 0)
        fun removeRightPanel() = this.children.remove(this.upr)
        fun restoreRightPanel() = this.add(this.upr, 2, 0)

        private fun createSwingContent(swingNode: SwingNode) {
            Platform.runLater {
                val canvas = Canvas2D(mediator)
                swingNode.content = canvas
                swingNode.isCache = true
                swingNode.cacheHint = CacheHint.SPEED
            }
        }

        private inner class UpperLeftPanel() : VerticalMainPanel() {

            init {
                this.addMenuBarButton("fas-paint-brush:15", DrawingConfigurationPanel())
                this.addMenuBarButton("fas-tools:15", SettingsPanel())
            }

            private inner class DrawingConfigurationPanel() : Panel() {
                init {

                    val vbox = VBox()
                    //vbox.children.add(NavigationSubPanel())
                    vbox.children.add(DetailsLevelSubPanel())
                    vbox.children.add(ColorLineWidthSubPanel())

                    val sp = ScrollPane(vbox)
                    sp.padding = Insets.EMPTY
                    sp.isFitToWidth = true
                    sp.isFitToHeight = true
                    sp.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    sp.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    this.children.add(sp)
                }

                private inner class NavigationSubPanel() : SubPanel("Navigation") {

                    init {
                        val buttonsPanel = LargeButtonsPanel()
                        this.children.add(buttonsPanel)

                        mediator.currentDrawing.addListener { _, _, newValue ->
                            buttonsPanel.buttons.forEach {
                                if (it.tooltip.text in listOf("Zoom +", "Zoom -", "Fit 2D to View"))
                                    it.isDisable = newValue == null
                            }
                        }

                        mediator.currentDrawing.addListener { _, oldValue, newValue ->
                            newValue?.let {
                                it.selectedDrawings.addListener(ListChangeListener {
                                    if (it.list.isEmpty()) {
                                        mediator.lastDrawingHighlighted.set(null)
                                        buttonsPanel.buttons.forEach {
                                            if (it.tooltip.text in listOf(
                                                    "Highlight former Selection",
                                                    "Highlight next Selection"
                                                )
                                            )
                                                it.isDisable = true
                                        }
                                    } else if (it.list.size == 1) {
                                        mediator.lastDrawingHighlighted.set(it.list.first())
                                        buttonsPanel.buttons.forEach {
                                            if (it.tooltip.text in listOf(
                                                    "Highlight former Selection",
                                                    "Highlight next Selection"
                                                )
                                            )
                                                it.isDisable = false
                                        }
                                    } else {
                                        mediator.lastDrawingHighlighted.set(null)
                                        buttonsPanel.buttons.forEach {
                                            if (it.tooltip.text in listOf(
                                                    "Highlight former Selection",
                                                    "Highlight next Selection"
                                                )
                                            )
                                                it.isDisable = false
                                        }
                                    }
                                })
                            }
                        }

                        buttonsPanel.addButton("fas-plus:15", "Zoom +").onMouseClicked = EventHandler {
                            mediator.workingSession?.zoomView(
                                mediator.canvas2D.bounds.centerX,
                                mediator.canvas2D.bounds.centerY,
                                true
                            )
                            mediator.canvas2D.repaint()
                        }

                        buttonsPanel.addButton("fas-minus:15", "Zoom -").onMouseClicked = EventHandler {
                            mediator.workingSession?.zoomView(
                                mediator.canvas2D.bounds.centerX,
                                mediator.canvas2D.bounds.centerY,
                                false
                            )
                            mediator.canvas2D.repaint()
                        }

                        buttonsPanel.addButton("fas-expand-arrows-alt:15", "Fit 2D to View").onMouseClicked =
                            EventHandler {
                                mediator.canvas2D.fitStructure(null)
                            }

                        buttonsPanel.addButton("fas-chevron-left:15", "Highlight former Selection").onMouseClicked =
                            EventHandler { mouseEvent ->
                                val sortedSelection = mediator.currentDrawing.get()?.selectedDrawings?.map { it }
                                    ?.sortedBy { (it as? JunctionDrawing)?.junction?.location?.end ?: it.location.end }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    val currentPos = sortedSelection?.indexOf(it)!!
                                    val newPos =
                                        if (currentPos == 0) sortedSelection.size - 1 else currentPos - 1
                                    mediator.lastDrawingHighlighted.set(
                                        sortedSelection.get(newPos)
                                    )
                                } ?: run {
                                    mediator.lastDrawingHighlighted.set(sortedSelection?.last())
                                }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    it.selectionShape?.let {
                                        mediator.canvas2D.centerDisplayOn(
                                            it.bounds2D
                                        )
                                    }
                                }

                            }

                        buttonsPanel.addButton("fas-chevron-right:15", "Highlight next Selection").onMouseClicked =
                            EventHandler { mouseEvent ->
                                val sortedSelection = mediator.currentDrawing.get()?.selectedDrawings?.map { it }
                                    ?.sortedBy {
                                        (it as? JunctionDrawing)?.junction?.location?.start ?: it.location.start
                                    }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    val currentPos = sortedSelection?.indexOf(it)!!
                                    val newPos =
                                        if (currentPos == sortedSelection.size - 1) 0 else currentPos + 1
                                    mediator.lastDrawingHighlighted.set(
                                        sortedSelection.get(newPos)
                                    )
                                } ?: run {
                                    mediator.lastDrawingHighlighted.set(sortedSelection?.first())
                                }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    it.selectionShape?.let {
                                        mediator.canvas2D.centerDisplayOn(
                                            it.bounds2D
                                        )
                                    }
                                }
                            }

                    }

                }

                private inner class DetailsLevelSubPanel() : SubPanel("Details Level") {

                    init {
                        val buttonsPanel = LargeButtonsPanel()
                        this.children.add(buttonsPanel)

                        mediator.currentDrawing.addListener { _, _, newValue ->
                            buttonsPanel.buttons.forEach {
                                it.isDisable = newValue == null
                            }
                        }

                        buttonsPanel.addButton("met-number-one:25").onAction = EventHandler {
                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                val t = Theme()
                                t.addConfiguration(
                                    ThemeProperty.fulldetails,
                                    { el -> "false" }
                                )
                                currentDrawing.secondaryStructureDrawing.applyTheme(t)
                                mediator.canvas2D.repaint()
                                setDetailsLvlForFull2D(currentDrawing.rnArtistEl, 1)
                            }

                        }

                        buttonsPanel.addButton("met-number-two:25").onAction = EventHandler {
                            val t = Theme()
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(
                                    SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond
                                )
                            )

                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "false" },
                                listOf(
                                    SecondaryStructureType.InteractionSymbol,
                                    SecondaryStructureType.AShape,
                                    SecondaryStructureType.UShape,
                                    SecondaryStructureType.GShape,
                                    SecondaryStructureType.CShape,
                                    SecondaryStructureType.XShape
                                )
                            )

                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                currentDrawing.secondaryStructureDrawing.applyTheme(t)
                                mediator.canvas2D.repaint()
                                currentDrawing.rnArtistEl?.let {
                                    setDetailsLvlForFull2D(it, 2)
                                }
                            }
                        }

                        buttonsPanel.addButton("met-number-three:25").onAction = EventHandler {
                            val t = Theme()
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(
                                    SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond,
                                    SecondaryStructureType.AShape,
                                    SecondaryStructureType.UShape,
                                    SecondaryStructureType.GShape,
                                    SecondaryStructureType.CShape,
                                    SecondaryStructureType.XShape
                                )
                            )

                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "false" },
                                listOf(
                                    SecondaryStructureType.InteractionSymbol,
                                    SecondaryStructureType.A,
                                    SecondaryStructureType.U,
                                    SecondaryStructureType.G,
                                    SecondaryStructureType.C,
                                    SecondaryStructureType.X
                                )
                            )

                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                currentDrawing.secondaryStructureDrawing.applyTheme(t)
                                mediator.canvas2D.repaint()
                                setDetailsLvlForFull2D(currentDrawing.rnArtistEl, 3)
                            }
                        }

                        buttonsPanel.addButton("met-number-four:25").onAction = EventHandler {
                            val t = Theme()
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                listOf(
                                    SecondaryStructureType.Helix,
                                    SecondaryStructureType.SecondaryInteraction,
                                    SecondaryStructureType.Junction,
                                    SecondaryStructureType.SingleStrand,
                                    SecondaryStructureType.PhosphodiesterBond,
                                    SecondaryStructureType.AShape,
                                    SecondaryStructureType.UShape,
                                    SecondaryStructureType.GShape,
                                    SecondaryStructureType.CShape,
                                    SecondaryStructureType.XShape,
                                    SecondaryStructureType.A,
                                    SecondaryStructureType.U,
                                    SecondaryStructureType.G,
                                    SecondaryStructureType.C,
                                    SecondaryStructureType.X
                                )
                            )

                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "false" },
                                listOf(SecondaryStructureType.InteractionSymbol)
                            )

                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                currentDrawing.secondaryStructureDrawing.applyTheme(t)
                                mediator.canvas2D.repaint()
                                setDetailsLvlForFull2D(currentDrawing.rnArtistEl, 4)
                            }
                        }

                        buttonsPanel.addButton("met-number-five:25").onAction = EventHandler {
                            val t = Theme()
                            t.addConfiguration(
                                ThemeProperty.fulldetails,
                                { _ -> "true" },
                                SecondaryStructureType.entries
                            )

                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                currentDrawing.secondaryStructureDrawing.applyTheme(t)
                                mediator.canvas2D.repaint()
                                setDetailsLvlForFull2D(currentDrawing.rnArtistEl, 5)
                            }
                        }
                    }

                }


                /*private class TertiaryStructureButtonsPanel(mediator: Mediator):CircularButtonsPanel(mediator = mediator, panelRadius = 60.0) {

                    val linkChimeraX = Button(null, FontIcon("fas-link:15"))
                    val waitingForConnection = Timeline()

                    init {
                        waitingForConnection.cycleCount = Timeline.INDEFINITE
                        waitingForConnection.keyFrames.add(KeyFrame(Duration.seconds(0.5), KeyValue(linkChimeraX.styleProperty(), "-fx-base: grey")))
                        waitingForConnection.keyFrames.add(KeyFrame(Duration.seconds(1.0), KeyValue(linkChimeraX.styleProperty(), "-fx-base: red")))
                        val chimeraRemoteRest = TextField("${RnartistConfig.chimeraHost}:${RnartistConfig.chimeraPort}")
                        chimeraRemoteRest.minWidth = 150.0
                        chimeraRemoteRest.maxWidth = 150.0
                        this.linkChimeraX.maxWidth = Double.MAX_VALUE
                        this.linkChimeraX.style = "-fx-base: red"
                        (this.linkChimeraX.graphic as FontIcon).fill = Color.WHITE
                        this.linkChimeraX.onMouseClicked = EventHandler {
                            try {
                                this.linkChimeraX.style = "-fx-base: red"
                                waitingForConnection.play()
                                RnartistConfig.chimeraHost = chimeraRemoteRest.text.split(":").first().trim { it <= ' ' }
                                RnartistConfig.chimeraPort = chimeraRemoteRest.text.split(":").last().trim { it <= ' ' }.toInt()
                                val sessionFile = mediator.chimeraDriver.sessionFile
                                mediator.chimeraDriver = ChimeraXDriver(mediator)
                                mediator.chimeraDriver.sessionFile = sessionFile
                                mediator.chimeraDriver.connectToRestServer()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        this.linkChimeraX.tooltip = Tooltip("Link ChimeraX")
                        this.addButton(this.linkChimeraX)

                        val focus3D = Button(null, FontIcon("fas-crosshairs:15"))
                        focus3D.onMouseClicked = EventHandler { mediator.focusInChimera() }
                        focus3D.tooltip = Tooltip("Focus 3D on Selection")
                        this.addButton(focus3D)

                        val paintSelectionin3D = Button(null, FontIcon("fas-fill:15"))
                        paintSelectionin3D.setOnMouseClicked {
                            mediator.chimeraDriver.color3D(
                                if (mediator.canvas2D.getSelectedResidues()
                                        .isNotEmpty()
                                ) mediator.canvas2D.getSelectedResidues() else mediator.currentDrawing.get()!!.drawing.residues
                            )
                        }
                        paintSelectionin3D.tooltip = Tooltip("Paint 3D selection")
                        this.addButton(paintSelectionin3D)

                        val reload3D = Button(null, FontIcon("fas-redo:15"))
                        reload3D.onMouseClicked = EventHandler { mediator.chimeraDriver.displayCurrent3D() }
                        reload3D.tooltip = Tooltip("Reload 3D")
                        this.addButton(reload3D)

                        val clearTheme = Button(null, FontIcon("fas-undo:15"))
                        clearTheme.tooltip = Tooltip("Clear Theme")
                        clearTheme.maxWidth = Double.MAX_VALUE
                        clearTheme.onAction = EventHandler {
                            mediator.currentDrawing.get()?.let {
                                it.drawing.clearTheme()
                                mediator.canvas2D.repaint()
                            }
                        }
                        this.addButton(clearTheme)

                        this.children.add(Label("ChimeraX Remote Rest"))
                        this.children.add(chimeraRemoteRest)
                    }

                    fun chimeraConnected(connected:Boolean) {
                        waitingForConnection.stop()
                        this.linkChimeraX.style =
                            if (connected) "-fx-base: green" else "-fx-base: red"
                        if (connected) {
                            mediator.chimeraDriver.displayCurrent3D()
                        }
                    }
                }*/

                private inner class ColorLineWidthSubPanel() : AbstractColorLineWidthSubPanel() {

                    val schemesComboBox = ComboBox<String>()

                    init {
                        mediator.currentDrawing.addListener { _, _, newValue ->
                            this.disableFontButton(newValue == null)
                        }

                        val schemes = listOf(
                            "Persian Carolina",
                            "Snow Lavender",
                            "Fuzzy French",
                            "Chestnut Navajo",
                            "Irresistible Turquoise",
                            "Charm Jungle",
                            "Atomic Xanadu",
                            "Pale Coral",
                            "Maximum Salmon",
                            "Pacific Dream",
                            "New York Camel",
                            "Screamin' Olive",
                            "Baby Lilac",
                            "Celeste Olivine",
                            "Midnight Paradise",
                            "African Lavender",
                            "Charcoal Lazuli",
                            "Pumpkin Vegas",
                            "Structural Domains"
                        )
                        this.schemesComboBox.items.addAll(schemes)
                        this.schemesComboBox.selectionModel.select(0)
                        this.schemesComboBox.onAction = EventHandler {
                            mediator.currentDrawing.get()?.secondaryStructureDrawing?.applyTheme(theme {
                                color {
                                    scheme  {
                                        value = schemesComboBox.value
                                    }
                                }
                            })
                            mediator.canvas2D.repaint()
                            //since we have applied a theme, all the colors elements are removed
                            mediator.currentDrawing.get()?.rnArtistEl?.let {
                                setSchemeForFull2D(it, schemesComboBox.value)
                            }
                        }
                        val hbox = HBox()
                        hbox.padding = Insets(0.0, 0.0, 15.0, 0.0)
                        hbox.spacing = 5.0
                        hbox.alignment = Pos.CENTER_LEFT
                        hbox.children.add(Label("Scheme"))
                        hbox.children.add(this.schemesComboBox)
                        HBox.setHgrow(this.schemesComboBox, Priority.ALWAYS)
                        this.children.add(1, hbox)
                        this.colorWheel.onMouseClicked = EventHandler { event ->
                            colorsFromWheel.forEach {
                                val color = Color.hsb(it.value[0], it.value[1], it.value[2])
                                if (event.x >= it.key.x && event.x <= it.key.x + 1.0 && event.y >= it.key.y && event.y <= it.key.y + 1.0) {
                                    val t = Theme()
                                    t.addConfiguration(ThemeProperty.color, {
                                        getHTMLColorString(
                                            javaFXToAwt(color)
                                        )
                                    }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                    applyTheme(t)

                                    this.repaintBrightness(it.value)
                                    this.repaintSaturation(it.value)
                                    addLastColor(it.value)

                                    return@EventHandler
                                }
                            }
                        }

                        this.colorWheelGroup.onMouseClicked = EventHandler {
                            var i = 0
                            while (i <= 6) {
                                var rotatedPoint = rotatePoint(
                                    Point2D.Double(90.0, 18.0),
                                    Point2D.Double(90.0, 85.0),
                                    currentAngle - 30.0 * i
                                )
                                var c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                                if (c.contains(javafx.geometry.Point2D(it.x, it.y))) {
                                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                                    rt.byAngle = -30.0 * i
                                    currentAngle -= 30.0 * i
                                    currentAngle = currentAngle % 360
                                    rt.play()
                                    val t = Theme()
                                    val lineWidth = when (currentAngle) {
                                        0.0 -> 0.0
                                        30.0, -330.0 -> 0.5
                                        60.0, -300.0 -> 1.0
                                        90.0, -270.0 -> 2.0
                                        120.0, -240.0 -> 3.0
                                        150.0, -210.0 -> 4.0
                                        180.0, -180.0 -> 5.0
                                        210.0, -150.0 -> 6.0
                                        240.0, -120.0 -> 7.0
                                        270.0, -90.0 -> 8.0
                                        300.0, -60.0 -> 9.0
                                        330.0, -30.0 -> 10.0
                                        else -> 1.0
                                    }
                                    t.addConfiguration(ThemeProperty.linewidth, {
                                        "$lineWidth"
                                    }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                    applyTheme((t))
                                    break
                                }
                                rotatedPoint = rotatePoint(
                                    Point2D.Double(90.0, 18.0),
                                    Point2D.Double(90.0, 85.0),
                                    currentAngle + 30.0 * i
                                )
                                c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                                if (c.contains(javafx.geometry.Point2D(it.x, it.y))) {
                                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                                    rt.byAngle = 30.0 * i
                                    currentAngle += 30.0 * i
                                    currentAngle = currentAngle % 360
                                    rt.play()
                                    val t = Theme()
                                    val lineWidth = when (currentAngle) {
                                        0.0 -> 0.0
                                        30.0, -330.0 -> 0.5
                                        60.0, -300.0 -> 1.0
                                        90.0, -270.0 -> 2.0
                                        120.0, -240.0 -> 3.0
                                        150.0, -210.0 -> 4.0
                                        180.0, -180.0 -> 5.0
                                        210.0, -150.0 -> 6.0
                                        240.0, -120.0 -> 7.0
                                        270.0, -90.0 -> 8.0
                                        300.0, -60.0 -> 9.0
                                        330.0, -30.0 -> 10.0
                                        else -> 1.0
                                    }
                                    t.addConfiguration(
                                        ThemeProperty.linewidth,
                                        {
                                            "$lineWidth"
                                        }, getSecondaryStructureTypes()
                                    ).gatherThemedSelements = true
                                    applyTheme(t)
                                    break
                                }
                                i++
                            }

                        }
                    }

                    override fun addLastColor(c: DoubleArray) {
                        val r = Circle(0.0, 0.0, 10.0)
                        val color = Color.hsb(c[0], c[1], c[2])
                        r.onMouseClicked = EventHandler { event ->
                            val t = Theme()
                            t.addConfiguration(ThemeProperty.color, {
                                getHTMLColorString(
                                    javaFXToAwt(color)
                                )
                            }, getSecondaryStructureTypes()).gatherThemedSelements = true
                            applyTheme(t)

                            this.repaintBrightness(c)
                            this.repaintSaturation(c)

                        }
                        r.fill = color
                        r.stroke = Color.BLACK
                        r.strokeWidth = 1.0
                        if (this.lastColors.children.size >= 6) {
                            val children = mutableListOf<Node>()
                            this.lastColors.children.forEach {
                                children.add(it)
                            }
                            children.removeAt(children.size - 1)
                            children.add(0, r)
                            this.lastColors.children.clear()
                            this.lastColors.children.addAll(*children.toTypedArray())
                        } else
                            this.lastColors.children.add(r)
                    }

                    override fun repaintBrightness(c: DoubleArray) {
                        this.brightnessBar.children.clear()
                        (0..5).reversed().forEach {
                            val r = Circle(0.0, 0.0, 10.0)
                            val color = Color.hsb(c[0], c[1], 0.2 * it)
                            r.onMouseClicked = EventHandler { event ->
                                val t = Theme()
                                t.addConfiguration(ThemeProperty.color, {
                                    getHTMLColorString(
                                        javaFXToAwt(color)
                                    )
                                }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                applyTheme(t)
                            }
                            r.fill = color
                            r.stroke = Color.BLACK
                            r.strokeWidth = 1.0
                            this.brightnessBar.children.add(r)
                        }
                    }

                    override fun repaintSaturation(c: DoubleArray) {
                        this.saturationBar.children.clear()
                        (0..5).reversed().forEach {
                            val r = Circle(0.0, 0.0, 10.0)
                            val color = Color.hsb(c[0], 0.2 * it, c[2])
                            r.onMouseClicked = EventHandler { event ->
                                val t = Theme()
                                t.addConfiguration(ThemeProperty.color, {
                                    getHTMLColorString(
                                        javaFXToAwt(color)
                                    )
                                }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                applyTheme(t)
                            }
                            r.fill = color
                            r.stroke = Color.BLACK
                            r.strokeWidth = 1.0
                            this.saturationBar.children.add(r)
                        }
                    }

                    /**
                     * Any element is selected since we are painting the full 2D
                     */
                    override fun getSelector() = { _: DrawingElement -> true }

                    override fun applyTheme(theme: Theme) {
                        mediator.currentDrawing.get()?.secondaryStructureDrawing?.applyTheme(theme)
                        mediator.canvas2D.repaint()
                        mediator.currentDrawing.get()?.let { currentDrawing ->
                            val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1
                            theme.configurations.forEach { configuration ->

                                when (configuration.propertyName) {
                                    ThemeProperty.color.toString() -> {
                                        configuration.themedElements?.first()?.getColor()
                                            ?.let { color -> //we extract the color to save in the script from the first element stored during the theming process
                                                setColorForFull2D(
                                                    currentDrawing.rnArtistEl,
                                                    applyThemeOnFont,
                                                    color,
                                                    step
                                                )
                                            }
                                    }

                                    ThemeProperty.linewidth.toString() -> {
                                        configuration.themedElements?.first()?.getLineWidth()
                                            ?.let { width -> //we extract the width to save in the script from the first element stored during the theming process
                                                setLineWidthForFull2D(currentDrawing.rnArtistEl, width, step)
                                            }
                                    }
                                }

                            }
                        }
                    }
                }
            }

            private inner class SettingsPanel() : Panel() {

            }

        }

        private inner class UpperRightPanel() : VerticalMainPanel() {

            init {
                this.addMenuBarButton("fas-paint-brush:15", DrawingConfigurationPanel())
                this.addMenuBarButton("fas-sitemap:15", StructureExplorerPanel())
                this.addMenuBarButton("fas-drafting-compass:15", JunctionPanel())
            }

            private inner class DrawingConfigurationPanel() : Panel() {
                init {

                    val vbox = VBox()
                    vbox.children.add(SelectionSubPanel())
                    vbox.children.add(DetailsLevelSubPanel())
                    vbox.children.add(ColorLineWidthSubPanel())

                    val sp = ScrollPane(vbox)
                    sp.background =
                        Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
                    sp.padding = Insets.EMPTY
                    sp.isFitToWidth = true
                    sp.isFitToHeight = true
                    sp.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    sp.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    this.children.add(sp)

                    /*l = Label("Junctions Layout")
                    l.padding = Insets(10.0, 0.0, 0.0, 0.0)
                    l.textFill = Color.WHITE
                    leftToolBar.children.add(l)
                    s = Separator()
                    s.padding = Insets(5.0, 0.0, 5.0, 0.0)
                    leftToolBar.children.add(s)
                    leftToolBar.children.add(this.junctionSelectionKnob)*/
                }

                private inner class SelectionSubPanel() : SubPanel("Selection") {

                    val typesComboBox = ComboBox<String>()

                    init {
                        val buttonsPanel = LargeButtonsPanel()
                        this.children.add(buttonsPanel)

                        mediator.currentDrawing.addListener { _, _, newValue ->
                            buttonsPanel.buttons.forEach {
                                it.isDisable = newValue == null
                            }
                        }

                        buttonsPanel.addButton(
                            icon = "fas-search:15"
                        ).onMouseClicked = EventHandler {
                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                val selectedLocation = with(mediator.canvas2D.getSelectedPositions()) {
                                    if (this.isEmpty())
                                        null
                                    else
                                        Location(this.toIntArray())
                                }

                                val selector = getSelector(selectedLocation)

                                selectedLocation?.let {
                                    mediator.canvas2D.clearSelection()
                                }
                                val allElements = mutableSetOf<DrawingElement>()
                                val elements = currentDrawing.secondaryStructureDrawing.select({ el ->
                                    selector(
                                        el
                                    )
                                })
                                mediator.canvas2D.addToSelection(elements)
                                elements.forEach {
                                    it.show()?.let {
                                        allElements.addAll(it)
                                    }
                                }

                                val typesAndLocation =
                                    dumpIntoTypeAndLocation(
                                        allElements.toList(),
                                        currentDrawing.secondaryStructureDrawing
                                    )

                                val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1

                                typesAndLocation.forEach {
                                    setDetailsLvl(
                                        currentDrawing.rnArtistEl,
                                        true,
                                        it.second,
                                        it.first,
                                        step
                                    )
                                }
                            }
                        }

                        buttonsPanel.addButton(icon = "fas-search-plus:15").onMouseClicked = EventHandler {
                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                val selector = getSelector()
                                val allElements = mutableSetOf<DrawingElement>()
                                val elements = currentDrawing.secondaryStructureDrawing.select({ el ->
                                    selector(
                                        el
                                    )
                                })
                                mediator.canvas2D.addToSelection(elements)
                                allElements.addAll(elements)
                                elements.forEach {
                                    it.show()?.let {
                                        allElements.addAll(it)
                                    }
                                }

                                val typesAndLocation =
                                    dumpIntoTypeAndLocation(
                                        allElements.toList(),
                                        currentDrawing.secondaryStructureDrawing
                                    )

                                val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1

                                typesAndLocation.forEach {
                                    setDetailsLvl(
                                        currentDrawing.rnArtistEl,
                                        true,
                                        it.second,
                                        it.first,
                                        step
                                    )
                                }
                            }
                        }

                        buttonsPanel.addButton(icon = "fas-exchange-alt:15").onMouseClicked = EventHandler {
                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                val selectedLocation = with(mediator.canvas2D.getSelectedPositions()) {
                                    if (this.isEmpty())
                                        null
                                    else
                                        Location(this.toIntArray())
                                }

                                val fullLocation = Location(1, mediator.rna!!.length)

                                val selector = selectedLocation?.let {
                                    mediator.canvas2D.clearSelection()
                                    getSelector(fullLocation.differenceOf(selectedLocation))
                                } ?: run {
                                    getSelector(fullLocation)
                                }

                                val allElements = mutableSetOf<DrawingElement>()
                                val elements = currentDrawing.secondaryStructureDrawing.select({ el ->
                                    selector(
                                        el
                                    )
                                })
                                mediator.canvas2D.addToSelection(elements)
                                allElements.addAll(elements)
                                elements.forEach {
                                    it.show()?.let {
                                        allElements.addAll(it)
                                    }
                                }

                                val typesAndLocation =
                                    dumpIntoTypeAndLocation(
                                        allElements.toList(),
                                        currentDrawing.secondaryStructureDrawing
                                    )

                                val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1

                                typesAndLocation.forEach {
                                    setDetailsLvl(
                                        currentDrawing.rnArtistEl,
                                        true,
                                        it.second,
                                        it.first,
                                        step
                                    )
                                }
                            }
                        }

                        buttonsPanel.addButton(
                            icon = "fas-trash:15"
                        ).onMouseClicked = EventHandler {
                            mediator.canvas2D.clearSelection()
                        }

                        val types = listOf(
                            "Helices",
                            "Junctions",
                            "Apical Loops",
                            "Inner Loops",
                            "3-Ways",
                            "4-Ways",
                            "Single-Strands",
                            "Secondary Interactions",
                            "Phosphodiester Bonds",
                            "Interaction Symbol",
                            "Residues",
                            "Adenines",
                            "Uracils",
                            "Guanines",
                            "Cytosines"
                        )
                        this.typesComboBox.value = "Helices"
                        this.typesComboBox.items.addAll(types)
                        this.typesComboBox.selectionModel.select(0)
                        this.typesComboBox.minWidth = 150.0
                        this.typesComboBox.maxWidth = 150.0
                        this.typesComboBox.onAction = EventHandler {

                        }
                        val hbox = HBox()
                        hbox.padding = Insets(5.0, 0.0, 0.0, 0.0)
                        hbox.spacing = 5.0
                        hbox.alignment = Pos.CENTER_LEFT
                        hbox.children.add(Label("Type"))
                        hbox.children.add(typesComboBox)
                        this.children.add(hbox)

                    }

                    private fun getSelector(selectedLocation: Location? = null): (DrawingElement) -> Boolean {
                        return when (this.typesComboBox.value) {
                            "Residues" -> { el -> el is ResidueDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Adenines" -> { el -> el is AShapeDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Uracils" -> { el -> el is UShapeDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Guanines" -> { el -> el is GShapeDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Cytosines" -> { el -> el is CShapeDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Helices" -> { el -> el is HelixDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Secondary Interactions" -> { el ->
                                el is SecondaryInteractionDrawing && selectedLocation?.contains(
                                    el.location
                                ) ?: true
                            }

                            "Phosphodiester Bonds" -> { el ->
                                if (el is PhosphodiesterBondDrawing) {
                                    selectedLocation?.let { selectedLocation ->
                                        el.parent?.let { parent ->
                                            if ((parent is JunctionDrawing || parent is SingleStrandDrawing) && selectedLocation.contains(
                                                    parent.location
                                                )
                                            ) {
                                                true//this is a trick to select all the phospho bons in a junction/single_strand drawing. Otherwise, the first and last phospho bonds will not be selected if the selected location is exactly the location of the parent (since the location of a JunctionDrawing/SingleStrandDrawing doesn't contain the surrounding helical base-pairs)
                                            } else
                                                selectedLocation.contains(el.location)
                                        } ?: run {
                                            selectedLocation.contains(el.location)
                                        }
                                    } ?: run {
                                        true
                                    }
                                } else
                                    false
                            }

                            "Junctions" -> { el -> el is JunctionDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Apical Loops" -> { el ->
                                el is JunctionDrawing && el.junctionType == JunctionType.ApicalLoop && selectedLocation?.contains(
                                    el.location
                                ) ?: true
                            }

                            "Inner Loops" -> { el ->
                                el is JunctionDrawing && el.junctionType == JunctionType.InnerLoop && selectedLocation?.contains(
                                    el.location
                                ) ?: true
                            }

                            "3-Ways" -> { el ->
                                el is JunctionDrawing && el.junctionType == JunctionType.ThreeWay && selectedLocation?.contains(
                                    el.location
                                ) ?: true
                            }

                            "4-Ways" -> { el ->
                                el is JunctionDrawing && el.junctionType == JunctionType.FourWay && selectedLocation?.contains(
                                    el.location
                                ) ?: true
                            }

                            "Single-Strands" -> { el -> el is SingleStrandDrawing && selectedLocation?.contains(el.location) ?: true }

                            "Interaction Symbol" -> { el ->
                                el is InteractionSymbolDrawing && selectedLocation?.contains(
                                    el.location
                                ) ?: true
                            }

                            else -> { _ -> false }
                        }
                    }
                }

                private inner class DetailsLevelSubPanel() :
                    SubPanel("Details Level") {

                    init {
                        val buttonsPanel = LargeButtonsPanel()
                        buttonsPanel.alignment = Pos.CENTER
                        this.children.add(buttonsPanel)

                        mediator.currentDrawing.addListener { _, _, newValue ->
                            newValue?.let {
                                it.selectedDrawings.addListener(ListChangeListener {
                                    if (it.list.isEmpty()) {
                                        buttonsPanel.buttons.forEach {
                                            it.isDisable = true
                                        }
                                    } else {
                                        buttonsPanel.buttons.forEach {
                                            it.isDisable = false
                                        }
                                    }
                                })
                            }
                        }

                        buttonsPanel.addButton("fas-minus:15").onAction = EventHandler {
                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                val elements = mutableListOf<DrawingElement>()
                                mediator.lastDrawingHighlighted.get()?.let {
                                    elements.addAll(it.hideDetailsUntilNextLevel())
                                } ?: run {
                                    currentDrawing.selectedDrawings.forEach {
                                        elements.addAll(it.hideDetailsUntilNextLevel())
                                    }
                                }
                                val typesAndLocation =
                                    dumpIntoTypeAndLocation(elements, currentDrawing.secondaryStructureDrawing)

                                val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1

                                typesAndLocation.forEach {
                                    setDetailsLvl(
                                        currentDrawing.rnArtistEl,
                                        false,
                                        it.second,
                                        it.first,
                                        step
                                    )
                                }
                            }
                            mediator.canvas2D.repaint()
                        }

                        buttonsPanel.addButton("fas-plus:15").onAction = EventHandler {
                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                val elements = mutableListOf<DrawingElement>()
                                mediator.lastDrawingHighlighted.get()?.let {
                                    elements.addAll(it.showUntilNextLevel())
                                } ?: run {
                                    currentDrawing.selectedDrawings.forEach {
                                        elements.addAll(it.showUntilNextLevel())
                                    }
                                }
                                val typesAndLocation =
                                    dumpIntoTypeAndLocation(elements, currentDrawing.secondaryStructureDrawing)

                                val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1

                                typesAndLocation.forEach {
                                    setDetailsLvl(
                                        currentDrawing.rnArtistEl,
                                        true,
                                        it.second,
                                        it.first,
                                        step
                                    )
                                }
                            }
                            mediator.canvas2D.repaint()
                        }

                    }

                    fun applyTheme(
                        theme: Theme,
                        checkStopBefore: ((DrawingElement) -> Boolean)? = null,
                        checkStopAfter: ((DrawingElement) -> Boolean)? = null
                    ) {
                        mediator.lastDrawingHighlighted.get()?.applyTheme(theme, checkStopBefore, checkStopAfter)
                            ?: mediator.canvas2D.getSelection().forEach {
                                it.applyTheme(theme, checkStopBefore, checkStopAfter)
                            }
                        mediator.canvas2D.repaint()

                        theme.configurations.first().themedElements?.let {

                            val selectedElements = it.toList()

                            mediator.currentDrawing.get()?.let { currentDrawing ->
                                val typesAndLocation =
                                    dumpIntoTypeAndLocation(selectedElements, currentDrawing.secondaryStructureDrawing)

                                val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1

                                typesAndLocation.forEach {
                                    setDetailsLvl(
                                        currentDrawing.rnArtistEl,
                                        selectedElements.first().isFullDetails(),
                                        it.second,
                                        it.first,
                                        step
                                    )
                                }
                            }
                        }
                    }

                    fun applyDetails(applicator: (DrawingElement) -> List<DrawingElement>?) {
                        val selectedElements = mutableListOf<DrawingElement>()
                        mediator.lastDrawingHighlighted.get()?.let {
                            applicator(it)?.let {
                                selectedElements.addAll(it)
                            }
                        } ?: run {
                            mediator.canvas2D.getSelection().forEach {
                                applicator(it)?.let {
                                    selectedElements.addAll(it)
                                }
                            }
                        }
                        mediator.canvas2D.repaint()

                        mediator.currentDrawing.get()?.let { currentDrawing ->
                            val typesAndLocation =
                                dumpIntoTypeAndLocation(selectedElements, currentDrawing.secondaryStructureDrawing)

                            val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1

                            typesAndLocation.forEach {
                                setDetailsLvl(
                                    currentDrawing.rnArtistEl,
                                    selectedElements.first().isFullDetails(),
                                    it.second,
                                    it.first,
                                    step
                                )
                            }
                        }
                    }

                }

                private inner class ColorLineWidthSubPanel() : AbstractColorLineWidthSubPanel() {
                    init {
                        mediator.currentDrawing.addListener { _, _, newValue ->
                            newValue?.let {
                                it.selectedDrawings.addListener(ListChangeListener {
                                    this.disableFontButton(it.list.flatMap { it.allChildren }
                                        .count { it is ResidueLetterDrawing } == 0)
                                })
                            }
                        }

                        var c = Circle(0.0, 0.0, 20.0)
                        val getSelectionColor = Button(null, FontIcon("fas-eye-dropper:15"))
                        getSelectionColor.isDisable = true
                        mediator.lastDrawingHighlighted.addListener { _, _, newValue ->
                            getSelectionColor.isDisable = (newValue == null)
                        }
                        getSelectionColor.background = null
                        (getSelectionColor.graphic as FontIcon).iconColor = Color.WHITE
                        getSelectionColor.onMouseClicked = EventHandler {
                            mediator.lastDrawingHighlighted.get()?.let {
                                val javafxColor = awtColorToJavaFX(it.getColor())
                                val _c = doubleArrayOf(javafxColor.hue, javafxColor.saturation, javafxColor.brightness)
                                this.repaintBrightness(_c)
                                this.repaintSaturation(_c)
                                addLastColor(_c)
                            }

                        }
                        getSelectionColor.shape = c
                        getSelectionColor.layoutX = 0.0
                        getSelectionColor.layoutY = 0.0
                        getSelectionColor.setMinSize(15.0, 15.0)
                        getSelectionColor.setMaxSize(15.0, 15.0)
                        colorWheelGroup.children.add(getSelectionColor)

                        this.colorWheel.onMouseClicked = EventHandler { event ->
                            colorsFromWheel.forEach {
                                val color = Color.hsb(it.value[0], it.value[1], it.value[2])
                                if (event.x >= it.key.x && event.x <= it.key.x + 1.0 && event.y >= it.key.y && event.y <= it.key.y + 1.0) {
                                    val t = Theme()
                                    t.addConfiguration(getSelector(), ThemeProperty.color, {
                                        getHTMLColorString(
                                            javaFXToAwt(color)
                                        )
                                    }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                    applyTheme(t)

                                    this.repaintBrightness(it.value)
                                    this.repaintSaturation(it.value)
                                    addLastColor(it.value)

                                    return@EventHandler
                                }
                            }
                        }

                        this.colorWheelGroup.onMouseClicked = EventHandler {
                            var i = 0
                            while (i <= 6) {
                                var rotatedPoint = rotatePoint(
                                    Point2D.Double(90.0, 18.0),
                                    Point2D.Double(90.0, 85.0),
                                    currentAngle - 30.0 * i
                                )
                                var c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                                if (c.contains(javafx.geometry.Point2D(it.x, it.y))) {
                                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                                    rt.byAngle = -30.0 * i
                                    currentAngle -= 30.0 * i
                                    currentAngle = currentAngle % 360
                                    rt.play()
                                    val t = Theme()
                                    t.addConfiguration(getSelector(), ThemeProperty.linewidth, {
                                        when (currentAngle) {
                                            0.0 -> "0.0"
                                            30.0, -330.0 -> "0.5"
                                            60.0, -300.0 -> "1.0"
                                            90.0, -270.0 -> "2.0"
                                            120.0, -240.0 -> "3.0"
                                            150.0, -210.0 -> "4.0"
                                            180.0, -180.0 -> "5.0"
                                            210.0, -150.0 -> "6.0"
                                            240.0, -120.0 -> "7.0"
                                            270.0, -90.0 -> "8.0"
                                            300.0, -60.0 -> "9.0"
                                            330.0, -30.0 -> "10.0"
                                            else -> "1.0"
                                        }
                                    }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                    applyTheme(t)
                                    break
                                }
                                rotatedPoint = rotatePoint(
                                    Point2D.Double(90.0, 18.0),
                                    Point2D.Double(90.0, 85.0),
                                    currentAngle + 30.0 * i
                                )
                                c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                                if (c.contains(javafx.geometry.Point2D(it.x, it.y))) {
                                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                                    rt.byAngle = 30.0 * i
                                    currentAngle += 30.0 * i
                                    currentAngle = currentAngle % 360
                                    rt.play()
                                    val t = Theme()
                                    t.addConfiguration(getSelector(), ThemeProperty.linewidth, {
                                        when (currentAngle) {
                                            0.0 -> "0.0"
                                            30.0, -330.0 -> "0.5"
                                            60.0, -300.0 -> "1.0"
                                            90.0, -270.0 -> "2.0"
                                            120.0, -240.0 -> "3.0"
                                            150.0, -210.0 -> "4.0"
                                            180.0, -180.0 -> "5.0"
                                            210.0, -150.0 -> "6.0"
                                            240.0, -120.0 -> "7.0"
                                            270.0, -90.0 -> "8.0"
                                            300.0, -60.0 -> "9.0"
                                            330.0, -30.0 -> "10.0"
                                            else -> "1.0"
                                        }
                                    }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                    applyTheme(t)
                                    break
                                }
                                i++
                            }

                        }
                    }

                    override fun addLastColor(c: DoubleArray) {
                        val r = Circle(0.0, 0.0, 10.0)
                        val color = Color.hsb(c[0], c[1], c[2])
                        r.onMouseClicked = EventHandler { event ->
                            val t = Theme()
                            t.addConfiguration(getSelector(), ThemeProperty.color, {
                                getHTMLColorString(
                                    javaFXToAwt(color)
                                )
                            }, getSecondaryStructureTypes()).gatherThemedSelements = true
                            applyTheme(t)

                            this.repaintBrightness(c)
                            this.repaintSaturation(c)

                        }
                        r.fill = color
                        r.stroke = Color.BLACK
                        r.strokeWidth = 1.0
                        if (this.lastColors.children.size >= 6) {
                            val children = mutableListOf<Node>()
                            this.lastColors.children.forEach {
                                children.add(it)
                            }
                            children.removeAt(children.size - 1)
                            children.add(0, r)
                            this.lastColors.children.clear()
                            this.lastColors.children.addAll(*children.toTypedArray())
                        } else
                            this.lastColors.children.add(r)
                    }

                    override fun repaintBrightness(c: DoubleArray) {
                        this.brightnessBar.children.clear()
                        (0..5).reversed().forEach {
                            val r = Circle(0.0, 0.0, 10.0)
                            val color = Color.hsb(c[0], c[1], 0.2 * it)
                            r.onMouseClicked = EventHandler { event ->
                                val t = Theme()
                                t.addConfiguration(getSelector(), ThemeProperty.color, {
                                    getHTMLColorString(
                                        javaFXToAwt(color)
                                    )
                                }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                applyTheme(t)
                            }
                            r.fill = color
                            r.stroke = Color.BLACK
                            r.strokeWidth = 1.0
                            this.brightnessBar.children.add(r)
                        }
                    }

                    override fun repaintSaturation(c: DoubleArray) {
                        this.saturationBar.children.clear()
                        (0..5).reversed().forEach {
                            val r = Circle(0.0, 0.0, 10.0)
                            val color = Color.hsb(c[0], 0.2 * it, c[2])
                            r.onMouseClicked = EventHandler { event ->
                                val t = Theme()
                                t.addConfiguration(getSelector(), ThemeProperty.color, {
                                    getHTMLColorString(
                                        javaFXToAwt(color)
                                    )
                                }, getSecondaryStructureTypes()).gatherThemedSelements = true
                                applyTheme(t)
                            }
                            r.fill = color
                            r.stroke = Color.BLACK
                            r.strokeWidth = 1.0
                            this.saturationBar.children.add(r)
                        }
                    }

                    /**
                     * An element is selected for the theme configuration if its location is inside the current selectedLocation.
                     * Consequently, any type of DrawingElement is themed if in the selected location
                     */
                    override fun getSelector() = { el: DrawingElement ->
                        with(mediator.canvas2D.getSelectedPositions()) {
                            val selectedLocation = Location(this.toIntArray())
                            if (el is PhosphodiesterBondDrawing && (el.parent is JunctionDrawing || el.parent is SingleStrandDrawing) && selectedLocation.contains(
                                    el.parent!!.location
                                ) ?: true
                            )
                                true//this is a trick to select all the phospho bonds in a junction drawing. Otherwise, the first and last phospho bonds will not be selected if the selected location is exactly the location of the parent JunctionDrawing (since the location of a JunctionDrawing doesn't contain the surrounding helical base-pairs)
                            else
                                selectedLocation.contains(el.location) ?: true
                        }

                    }

                    /**
                     * Apply a theme to the selection highlighted or the entire selection.
                     *
                     */
                    override fun applyTheme(theme: Theme) {

                        //theme applied
                        mediator.lastDrawingHighlighted.get()?.let { drawingElement ->
                            drawingElement.applyTheme(theme)
                            mediator.canvas2D.repaint()
                        } ?: run {
                            mediator.canvas2D.getSelection().forEach {
                                it.applyTheme(theme)
                            }
                            mediator.canvas2D.repaint()
                        }

                        //DSLElement tree synchronized with theme
                        mediator.currentDrawing.get()?.let { currentDrawing ->
                            val step = currentDrawing.rnArtistEl.getThemeOrNew().lastStep + 1
                            theme.configurations.forEach { configuration ->

                                configuration.themedElements?.let { elements ->
                                    val typesAndLocation =
                                        dumpIntoTypeAndLocation(elements, currentDrawing.secondaryStructureDrawing)

                                    when (configuration.propertyName) {
                                        ThemeProperty.color.toString() ->
                                            typesAndLocation.forEach {
                                                if (applyThemeOnFont) { //the user cannot select ResidueLetter by himself, we search for the first ResidueLetter in the current selection to get the color
                                                    mediator.canvas2D.getSelection()
                                                        .firstOrNull()?.allChildren?.firstOrNull { it is ResidueLetterDrawing }
                                                        ?.getColor()?.let { color ->
                                                            setColor(
                                                                currentDrawing.rnArtistEl,
                                                                color,
                                                                it.second,
                                                                it.first,
                                                                step
                                                            )
                                                        }
                                                } else
                                                    mediator.canvas2D.getSelection().firstOrNull()?.getColor()
                                                        ?.let { color ->
                                                            setColor(
                                                                currentDrawing.rnArtistEl,
                                                                color,
                                                                it.second,
                                                                it.first,
                                                                step
                                                            )
                                                        }
                                            }

                                        ThemeProperty.linewidth.toString() ->
                                            typesAndLocation.forEach {
                                                mediator.canvas2D.getSelection().firstOrNull()?.getLineWidth()
                                                    ?.let { width ->
                                                        setLineWidth(
                                                            currentDrawing.rnArtistEl,
                                                            width,
                                                            it.second,
                                                            it.first,
                                                            step
                                                        )
                                                    }
                                            }

                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }

            }

            private inner class StructureExplorerPanel() : Panel() {

            }

            private inner class JunctionPanel() : Panel() {

                init {
                    val vbox = VBox()
                    vbox.children.add(NavigationSubPanel())
                    vbox.children.add(LayoutSubPanel())

                    val sp = ScrollPane(vbox)
                    sp.padding = Insets.EMPTY
                    sp.isFitToWidth = true
                    sp.isFitToHeight = true
                    sp.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    sp.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    this.children.add(sp)

                }

                private inner class NavigationSubPanel() : SubPanel("Navigation") {

                    init {
                        val buttonsPanel = LargeButtonsPanel()
                        buttonsPanel.alignment = Pos.CENTER
                        this.children.add(buttonsPanel)

                        mediator.currentDrawing.addListener { _, oldValue, newValue ->
                            newValue?.let {
                                it.selectedDrawings.addListener(ListChangeListener {
                                    if (it.list.isEmpty()) {
                                        buttonsPanel.buttons.forEach {
                                            if (it.tooltip.text in listOf(
                                                    "Highlight former Junction",
                                                    "Highlight next Junction"
                                                )
                                            )
                                                it.isDisable = true
                                        }
                                    } else if (it.list.size == 1 && it.list.first() is JunctionDrawing) {
                                        buttonsPanel.buttons.forEach {
                                            if (it.tooltip.text in listOf(
                                                    "Highlight former Junction",
                                                    "Highlight next Junction"
                                                )
                                            )
                                                it.isDisable = false
                                        }
                                    } else if (it.list.filter { it is JunctionDrawing }.size > 1) {
                                        buttonsPanel.buttons.forEach {
                                            if (it.tooltip.text in listOf(
                                                    "Highlight former Junction",
                                                    "Highlight next Junction"
                                                )
                                            )
                                                it.isDisable = false
                                        }
                                    }
                                })
                            }
                        }

                        buttonsPanel.addButton("fas-chevron-left:15", "Highlight former Junction").onMouseClicked =
                            EventHandler { mouseEvent ->
                                val sortedSelectedJunctions =
                                    mediator.currentDrawing.get()?.selectedDrawings?.filter { it is JunctionDrawing }
                                        ?.map { it }?.sortedBy { (it as JunctionDrawing).junction.location.end }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    if (it is JunctionDrawing) {
                                        val currentPos = sortedSelectedJunctions?.indexOf(it)!!
                                        val newPos =
                                            if (currentPos == 0) sortedSelectedJunctions.size - 1 else currentPos - 1
                                        mediator.lastDrawingHighlighted.set(
                                            sortedSelectedJunctions.get(newPos)
                                        )
                                    } else {
                                        mediator.lastDrawingHighlighted.set(sortedSelectedJunctions?.last())
                                    }
                                } ?: run {
                                    mediator.lastDrawingHighlighted.set(sortedSelectedJunctions?.last())
                                }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    it.selectionShape?.let {
                                        mediator.canvas2D.centerDisplayOn(
                                            it.bounds2D
                                        )
                                    }
                                }
                            }

                        buttonsPanel.addButton("fas-chevron-right:15", "Highlight next Junction").onMouseClicked =
                            EventHandler { mouseEvent ->
                                val sortedSelectedJunctions =
                                    mediator.currentDrawing.get()?.selectedDrawings?.filter { it is JunctionDrawing }
                                        ?.map { it }?.sortedBy { (it as JunctionDrawing).junction.location.start }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    if (it is JunctionDrawing) {
                                        val currentPos = sortedSelectedJunctions?.indexOf(it)!!
                                        val newPos =
                                            if (currentPos == sortedSelectedJunctions.size - 1) 0 else currentPos + 1
                                        mediator.lastDrawingHighlighted.set(
                                            sortedSelectedJunctions.get(newPos)
                                        )
                                    } else
                                        mediator.lastDrawingHighlighted.set(sortedSelectedJunctions?.first())
                                } ?: run {
                                    mediator.lastDrawingHighlighted.set(sortedSelectedJunctions?.first())
                                }
                                mediator.lastDrawingHighlighted.get()?.let {
                                    it.selectionShape?.let {
                                        mediator.canvas2D.centerDisplayOn(
                                            it.bounds2D
                                        )
                                    }
                                }
                            }

                    }

                }

                private inner class LayoutSubPanel() : SubPanel("Layout") {
                    init {
                        val knob = JunctionKnob(mediator)
                        this.children.add(knob)
                        mediator.lastDrawingHighlighted.addListener() { _, _, newValue ->
                            (newValue as? JunctionDrawing)?.let {
                                knob.selectedJunction = newValue
                            } ?: run {
                                knob.selectedJunction = null
                            }

                        }
                    }
                }
            }
        }
    }

    private inner class LowerPanel() : HorizontalMainPanel() {

        init {
            this.addMenuBarButton("fas-database:15", DBExplorerPanel())
            this.addMenuBarButton("fas-chart-area:15", ChartsPanel())
        }

        private inner class DBExplorerPanel() : Panel() {

            private val dbExplorerSubPanel = DBExplorerSubPanel()

            init {
                this.children.add(this.dbExplorerSubPanel)
                setVgrow(this.dbExplorerSubPanel, Priority.ALWAYS)
            }

            private inner class DBExplorerSubPanel() : SubPanel() {

                private val loadDB: Button
                private val reloadDB: Button
                private val viewRNAClass: Button
                private val treeView = TreeView<StructuralClass>()
                private val drawingsDirName = "drawings"
                var lastThumbnailCellClicked: ThumbnailCell? = null
                val saveCurrentDrawing = {
                    mediator.currentDrawing.get()?.let { currentDrawing ->
                        //we replace the dsl script in the file of the currentDrawing with the dsl script in memory
                        with(File(currentDrawing.dslScriptAbsolutePath)) {
                            currentDrawing.rnArtistEl.getThemeOrNew().cleanHistory()
                            currentDrawing.rnArtistEl.getLayoutOrNew().cleanHistory()
                            val content = currentDrawing.rnArtistEl.dump().toString()
                            this.writeText(content)
                            mediator.scriptEngine.eval(content)

                            lastThumbnailCellClicked?.let {
                                Platform.runLater {
                                    it.item.layoutAndThemeUpdated.value =
                                        !it.item.layoutAndThemeUpdated.value
                                }
                            }
                        }

                    }
                }

                //we explicitly create the observable list of thumbnails to link it to an extractor that will automatically update the picture when the layout and/or theme of the current drawing is saved back in the DSL script.
                private val thumbnailsList: ObservableList<Thumbnail> =
                    FXCollections.observableArrayList { thumbnail: Thumbnail ->
                        arrayOf(
                            thumbnail.layoutAndThemeUpdated
                        )
                    }

                val thumbnails = GridView<Thumbnail>(thumbnailsList)

                val dirsWithStructuralDataAlreadyIndexed = mutableListOf<String>()
                var rootDB: String? = null
                    set(value) {
                        field = value
                        rootDB?.let {
                            this.treeView.root = TreeItem(StructuralClass(Path.of(it).name, it))
                            dirsWithStructuralDataAlreadyIndexed.clear()
                        }

                    }

                init {
                    this.spacing = 10.0
                    val buttonsPanel = LargeButtonsPanel()
                    this.children.add(buttonsPanel)

                    this.loadDB = buttonsPanel.addButton("fas-folder-open", "Load database")
                    this.loadDB.onMouseClicked = EventHandler { _ ->
                        val directoryChooser = DirectoryChooser()
                        directoryChooser.showDialog(null)?.let {
                            clearItems()
                            rootDB = it.absolutePath
                            val w = RNArtistTaskDialog(mediator)
                            w.task = LoadDB(mediator)
                        }
                    }
                    this.loadDB.isDisable = false

                    this.reloadDB = buttonsPanel.addButton("fas-sync:15", "ReLoad database")
                    this.reloadDB.onMouseClicked = EventHandler { _ ->
                        val w = RNArtistTaskDialog(mediator)
                        w.task = LoadDB(mediator)
                    }

                    this.viewRNAClass = buttonsPanel.addButton("fas-eye:15", "Load Structures")
                    this.viewRNAClass.onMouseClicked = EventHandler { _ ->
                        this.treeView.selectionModel.selectedItem?.let { selectedItem ->
                            class LoadDBFolder(mediator: Mediator) : RNArtistTask(mediator) {
                                init {
                                    setOnSucceeded { _ ->
                                        this.resultNow().second?.printStackTrace()
                                        this.rnartistTaskDialog.stage.hide()
                                        loadDB.isDisable = false
                                        reloadDB.isDisable = false
                                        viewRNAClass.isDisable = false
                                    }
                                }

                                override fun call(): Pair<Any?, Exception?> {
                                    try {
                                        rootDB?.let { rootDB ->

                                            Platform.runLater {
                                                clearItems()
                                                loadDB.isDisable = true
                                                reloadDB.isDisable = true
                                                viewRNAClass.isDisable = true
                                            }
                                            Thread.sleep(100)

                                            //we will process the folder for the item selected and all the indexed folders
                                            val folders2View = mutableListOf(selectedItem.value.absPath)

                                            folders2View.addAll(dirsWithStructuralDataAlreadyIndexed.filter {
                                                it.startsWith(
                                                    selectedItem.value.absPath
                                                ) && it != selectedItem.value.absPath /*to not add it twice if indexed*/
                                            })

                                            folders2View.forEach { absPath ->
                                                Platform.runLater {
                                                    updateProgress(-1.toDouble(), Double.MAX_VALUE)
                                                    stepProperty.value = null
                                                }
                                                Thread.sleep(100)
                                                val path = Paths.get(
                                                    rootDB,
                                                    drawingsDirName,
                                                    *absPath.split(rootDB).last().removePrefix("/")
                                                        .removeSuffix("/").split("/").toTypedArray()
                                                )
                                                val drawingsDir = File(path.toUri())
                                                val dataDir = File(absPath)

                                                val script = File(File(absPath).parent, "${dataDir.name}.kts")
                                                val containsStructuralData =
                                                    containsStructuralData(dataDir.toPath())
                                                if (containsStructuralData) {
                                                    var totalStructures = 0
                                                    val scriptContent: String
                                                    if (!script.exists()) {
                                                        script.createNewFile()

                                                        val rnartistEl = RNArtistEl()

                                                        val pngEl = rnartistEl.addPNG()

                                                        pngEl.setPath(path.absolutePathString())

                                                        pngEl.setWidth(250.0)
                                                        pngEl.setHeight(250.0)

                                                        val ssEl = rnartistEl.addSS()

                                                        dataDir.listFiles(FileFilter { it.name.endsWith(".vienna") })
                                                            ?.let {
                                                                if (it.isNotEmpty()) {
                                                                    val viennaEl = ssEl.addVienna()
                                                                    viennaEl.setPath(dataDir.absolutePath)
                                                                    totalStructures += it.size
                                                                }
                                                            }

                                                        dataDir.listFiles(FileFilter { it.name.endsWith(".bpseq") })
                                                            ?.let {
                                                                if (it.isNotEmpty()) {
                                                                    val bpSeqEl = ssEl.addBPSeq()
                                                                    bpSeqEl.setPath(dataDir.absolutePath)
                                                                    totalStructures += it.size
                                                                }
                                                            }

                                                        dataDir.listFiles(FileFilter { it.name.endsWith(".ct") })
                                                            ?.let {
                                                                if (it.isNotEmpty()) {
                                                                    val ctEl = ssEl.addCT()
                                                                    ctEl.setPath(dataDir.absolutePath)
                                                                    totalStructures += it.size
                                                                }
                                                            }

                                                        dataDir.listFiles(FileFilter { it.name.endsWith(".pdb") })
                                                            ?.let {
                                                                if (it.isNotEmpty()) {
                                                                    val pdbEl = ssEl.addPDB()
                                                                    pdbEl.setPath(dataDir.absolutePath)
                                                                    totalStructures += it.size
                                                                }
                                                            }

                                                        val themeEl = rnartistEl.addTheme()
                                                        with (themeEl.addDetails()) {
                                                            this.setValue(4)
                                                            this.setStep(1)
                                                        }

                                                        with (themeEl.addScheme()) {
                                                            this.setValue("Midnight Paradise")
                                                            this.setStep(1)
                                                        }

                                                        with(themeEl.addLine()) {
                                                            this.setValue(2.5)
                                                            this.setStep(1)
                                                        }

                                                        scriptContent = rnartistEl.dump().toString()
                                                        script.writeText(scriptContent)
                                                    } else {
                                                        scriptContent = script.readText()
                                                        val structuralFiles = dataDir.listFiles(FileFilter {
                                                            it.name.endsWith(".ct") || it.name.endsWith(".vienna") || it.name.endsWith(".bpseq")  || it.name.endsWith(".pdb")})?.size ?: 0
                                                        val scripts =
                                                            dataDir.listFiles(FileFilter { it.name.endsWith(".kts") })?.size
                                                                ?: 0
                                                        totalStructures = structuralFiles - scripts
                                                    }

                                                    if (totalStructures > 0) {
                                                        Platform.runLater {
                                                            updateMessage("Computing $totalStructures 2Ds for ${dataDir.name}, please wait...")
                                                        }
                                                        Thread.sleep(1000)

                                                        val manager = ScriptEngineManager()
                                                        val engine = manager.getEngineByExtension("kts")
                                                        engine.eval(
                                                            "import io.github.fjossinet.rnartist.core.*${
                                                                System.getProperty(
                                                                    "line.separator"
                                                                )
                                                            }${
                                                                System.getProperty(
                                                                    "line.separator"
                                                                )
                                                            } $scriptContent"
                                                        )

                                                        Platform.runLater {
                                                            updateMessage("Computing done!")
                                                        }
                                                        Thread.sleep(1000)

                                                    }
                                                }

                                                val total =
                                                    drawingsDir.listFiles(FileFilter { it.name.endsWith(".png") })?.size
                                                        ?: 0

                                                var i = 0
                                                Platform.runLater {
                                                    updateProgress(i.toDouble(), total.toDouble())
                                                }
                                                Thread.sleep(100)
                                                drawingsDir.listFiles(FileFilter { it.name.endsWith(".png") })
                                                    ?.forEach {
                                                        Platform.runLater {
                                                            updateMessage(
                                                                "Loading 2D for ${
                                                                    it.name.split(".png").first()
                                                                } in ${dataDir.name}"
                                                            )
                                                            val t = Thumbnail(
                                                                this.mediator,
                                                                it,
                                                                File(
                                                                    dataDir,
                                                                    "${it.name.split(".png").first()}.kts"
                                                                ).absolutePath
                                                            )
                                                            thumbnails.items.add(t)
                                                            updateProgress((++i).toDouble(), total.toDouble())
                                                            stepProperty.value = Pair(i, total)
                                                        }
                                                        Thread.sleep(100)
                                                    }
                                            }
                                        }
                                        return Pair(null, null)
                                    } catch (e: Exception) {
                                        return Pair(null, e)
                                    }
                                }

                            }

                            val w = RNArtistTaskDialog(mediator)
                            w.task = LoadDBFolder(mediator)

                        }
                    }

                    //buttonsPanel.addSeparator()

                    this.thumbnails.padding = Insets(10.0)
                    this.thumbnails.background =
                        Background(BackgroundFill(Color.WHITE, CornerRadii(10.0), Insets.EMPTY))
                    this.thumbnails.horizontalCellSpacing = 5.0
                    this.thumbnails.verticalCellSpacing = 5.0
                    this.thumbnails.cellWidth = 250.0
                    this.thumbnails.cellHeight = 350.0
                    this.thumbnails.setCellFactory { ThumbnailCell() }

                    this.treeView.padding = Insets(10.0)
                    this.treeView.background =
                        Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
                    this.treeView.border = Border(
                        BorderStroke(
                            Color.LIGHTGRAY,
                            BorderStrokeStyle.SOLID, CornerRadii(10.0), BorderWidths(1.5)
                        )
                    )
                    this.treeView.isEditable = true
                    this.treeView.root = TreeItem(StructuralClass("No database selected", ""))
                    this.treeView.onMouseClicked = EventHandler {
                    }

                    val splitPane = SplitPane()
                    splitPane.orientation = Orientation.HORIZONTAL
                    splitPane.background =
                        Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
                    val s = ScrollPane(this.treeView)
                    s.isFitToHeight = true
                    s.isFitToWidth = true
                    s.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    s.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    splitPane.items.add(s)
                    splitPane.items.add(this.thumbnails)
                    splitPane.setDividerPositions(0.15)
                    setVgrow(splitPane, Priority.ALWAYS)
                    this.children.add(splitPane)

                }

                private fun containsStructuralData(path: Path): Boolean {
                    var containsStructuralData = false
                    path.toFile().listFiles(FileFilter {
                        it.name.endsWith(".vienna") || it.name.endsWith(".bpseq") || it.name.endsWith(".ct" ) || it.name.endsWith(".pdb" )
                    })?.let {
                        containsStructuralData = it.isNotEmpty()
                    }
                    return containsStructuralData
                }


                private fun getNonIndexedDirsWithStructuralData(dirs: MutableList<File>, dir: Path) {
                    try {
                        Files.newDirectoryStream(dir).use { stream ->
                            for (path in stream) {
                                if (path.toFile().isDirectory() && path.name != drawingsDirName) {
                                    val containsStructuralData = containsStructuralData(path)
                                    if (containsStructuralData && !dirsWithStructuralDataAlreadyIndexed.contains(
                                            path.absolutePathString()
                                        )
                                    )
                                        dirs.add(path.toFile())
                                    getNonIndexedDirsWithStructuralData(dirs, path)
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                private fun findIntermediateDirs(absolutePath2StructuralFiles: String) =
                    absolutePath2StructuralFiles.split(rootDB!!).last().removePrefix("/").removeSuffix("/")
                        .split("/")

                private fun addNewStructuralClassToTreeView(absolutePath2StructuralFiles: String) {
                    rootDB?.let { _ ->
                        val intermediateDirs = findIntermediateDirs(absolutePath2StructuralFiles)
                        var currentParent = this.treeView.root
                        for (i in 0 until intermediateDirs.size) {
                            if (i == intermediateDirs.size - 1) {
                                currentParent.children.find { intermediateDirs[i] == it.value.name }?.let {
                                    //this child already exists in the treeview
                                } ?: run {
                                    currentParent.children.add(
                                        TreeItem(
                                            StructuralClass(
                                                intermediateDirs[i],
                                                absolutePath2StructuralFiles
                                            )
                                        )
                                    )
                                }
                            } else {
                                val item = currentParent.children.find { intermediateDirs[i] == it.value.name }
                                item?.let {
                                    currentParent = item
                                } ?: run {
                                    val treeItem = TreeItem(
                                        StructuralClass(
                                            intermediateDirs[i],
                                            Path.of(
                                                absolutePath2StructuralFiles.split(intermediateDirs[i]).first(),
                                                intermediateDirs[i]
                                            ).absolutePathString()
                                        )
                                    )
                                    currentParent.children.add(treeItem)
                                    currentParent = treeItem
                                }
                            }


                        }

                    }
                }

                fun count() = this.thumbnails.items.size

                fun removeItem(thumbnail: Thumbnail) {
                    val index = this.thumbnails.items.indexOfFirst { it == thumbnail }
                    this.thumbnails.items.removeAt(index)
                }

                fun clearItems() {
                    this.thumbnails.items.clear()
                }

                private inner class StructuralClass(var name: String, var absPath: String) {
                    override fun toString(): String {
                        return this.name
                    }
                }

                private inner class ThumbnailCell : GridCell<Thumbnail>() {
                    private val icon = ImageView()
                    private val content: VBox = VBox()
                    private val titlePanel = TitlePanel()
                    private val buttonsPanel = ButtonsPanel()

                    override fun updateItem(thumbnail: Thumbnail?, empty: Boolean) {
                        super.updateItem(thumbnail, empty)
                        graphic = null
                        text = null
                        if (!empty && thumbnail != null) {
                            icon.image = thumbnail.image
                            val title = File(thumbnail.dslScriptAbsolutePath).name.removeSuffix(".kts")
                            titlePanel.setTitle(title)
                            buttonsPanel.buttons.subList(1,3).forEach {
                                it.isDisable = !matchCurrentDrawing(mediator.currentDrawing.get())
                            }
                            graphic = content
                        }
                    }

                    init {
                        content.alignment = Pos.BOTTOM_CENTER
                        content.children.add(icon)
                        content.children.add(titlePanel)
                        content.children.add(buttonsPanel)
                        buttonsPanel.addButton("fas-eye:15").onMouseClicked = EventHandler { event ->
                            lastThumbnailCellClicked = this

                            class LoadStructure(mediator: Mediator) : RNArtistTask(mediator) {
                                init {
                                    setOnSucceeded { _ ->
                                        this.resultNow().second?.printStackTrace()
                                        this.rnartistTaskDialog.stage.hide()
                                    }
                                }

                                override fun call(): Pair<Any?, Exception?> {
                                    try {
                                        Platform.runLater {
                                            updateMessage("Loading 2D for ${item.dslScriptAbsolutePath.removeSuffix(".kts").split(System.getProperty(
                                                "file.separator"
                                            )).last()}...")
                                        }
                                        Thread.sleep(100)

                                        val scriptContent = File(item.dslScriptAbsolutePath).readLines()

                                        val result = mediator.scriptEngine.eval(
                                            "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                                System.getProperty(
                                                    "line.separator"
                                                )
                                            } ${scriptContent.joinToString("\n")}"
                                        ) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>
                                        result?.let {
                                            val drawing =
                                                RNArtistDrawing(mediator, it.first.first(), item.dslScriptAbsolutePath, it.second)
                                            mediator.currentDrawing.set(drawing)
                                            if (drawing.secondaryStructureDrawing.viewX == 0.0 && drawing.secondaryStructureDrawing.viewY == 0.0 && drawing.secondaryStructureDrawing.zoomLevel == 1.0) {
                                                //it seems it is a first opening, then we fit to the display
                                                mediator.canvas2D.fitStructure(null)
                                            }
                                        }
                                        return Pair(null, null)
                                    } catch (e: Exception) {
                                        return Pair(null, e)
                                    }
                                }

                            }

                            val w = RNArtistTaskDialog(mediator)
                            w.task = LoadStructure(mediator)
                        }

                        val save = buttonsPanel.addButton("fas-save:15")
                        save.isDisable = true
                        mediator.currentDrawing.addListener { _, _, newValue ->
                            save.isDisable = !matchCurrentDrawing(newValue)
                        }
                        save.onMouseClicked = EventHandler { _ ->
                            class Save2D(mediator: Mediator) : RNArtistTask(mediator) {
                                init {
                                    setOnSucceeded { _ ->
                                        this.rnartistTaskDialog.stage.hide()
                                    }
                                }

                                override fun call(): Pair<Any?, Exception?> {
                                    return try {
                                        Platform.runLater {
                                            updateMessage("Saving 2D for ${item.dslScriptAbsolutePath.removeSuffix(".kts").split(System.getProperty(
                                                "file.separator"
                                            )).last()}...")
                                        }
                                        Thread.sleep(100)
                                        saveCurrentDrawing()
                                        Pair(null, null)
                                    } catch (e: Exception) {
                                        Pair(null, e)
                                    }
                                }

                            }

                            val w = RNArtistTaskDialog(mediator)
                            w.task = Save2D(mediator)

                        }

                        val applyToAll = buttonsPanel.addButton("fas-th:15")
                        applyToAll.isDisable = true
                        mediator.currentDrawing.addListener { _, _, newValue ->
                            applyToAll.isDisable = !matchCurrentDrawing(newValue)
                        }
                        applyToAll.onMouseClicked = EventHandler { _ ->
                            class UpdateAll2Ds(mediator: Mediator) : RNArtistTask(mediator) {
                                init {
                                    setOnSucceeded { _ ->
                                        this.rnartistTaskDialog.stage.hide()
                                    }
                                }

                                override fun call(): Pair<Any?, Exception?> {
                                    try {
                                        mediator.currentDrawing.get()?.let { currentDrawing ->
                                            //first the thumbnail of the current drawing
                                            //we replace the dsl script in the file of the currentDrawing with the dsl script in memory
                                            Platform.runLater {
                                                updateMessage("Saving 2D for ${item.dslScriptAbsolutePath.removeSuffix(".kts").split(System.getProperty(
                                                    "file.separator"
                                                )).last()}...")
                                            }
                                            saveCurrentDrawing()

                                            Thread.sleep(100)

                                            //then the other thumbnails
                                            thumbnails.items.forEach { item ->
                                                if (item != lastThumbnailCellClicked?.item) {
                                                    Platform.runLater {
                                                        updateMessage("Updating 2D for ${item.dslScriptAbsolutePath.removeSuffix(".kts").split(System.getProperty(
                                                            "file.separator"
                                                        )).last()}...")
                                                    }
                                                    Thread.sleep(100)
                                                    val rnartistEl = (mediator.scriptEngine.eval(File(item.dslScriptAbsolutePath).readText()) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.second

                                                    rnartistEl?.let {
                                                        //we replace the theme and layout elements in the dsl script for this thumbnail with the ones from the current drawing
                                                        rnartistEl.addTheme(currentDrawing.rnArtistEl.getThemeOrNew())
                                                        rnartistEl.addLayout(currentDrawing.rnArtistEl.getLayoutOrNew())

                                                        with (File(item.dslScriptAbsolutePath)) {
                                                            val content = rnartistEl.dump().toString()
                                                            this.writeText(content)
                                                            mediator.scriptEngine.eval(content)
                                                            Platform.runLater {
                                                                item.layoutAndThemeUpdated.value =
                                                                    !item.layoutAndThemeUpdated.value
                                                            }
                                                            Thread.sleep(100)
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                        return Pair(null, null)
                                    } catch (e: Exception) {
                                        return Pair(null, e)
                                    }
                                }

                            }

                            val w = RNArtistTaskDialog(mediator)
                            w.task = UpdateAll2Ds(mediator)

                        }

                        val exportAsSVG = buttonsPanel.addButton("fas-file-download:15")
                        mediator.currentDrawing.addListener { _, _, newValue ->
                            exportAsSVG.isDisable = newValue == null
                        }
                        exportAsSVG.onMouseClicked = EventHandler { _ ->
                            class ExportTask(mediator: Mediator, val file: File) : RNArtistTask(mediator) {
                                init {
                                    setOnSucceeded { _ ->
                                        mediator.currentDrawing.get()?.rnArtistEl?.removeSVG()
                                        this.rnartistTaskDialog.stage.hide()
                                    }
                                }

                                override fun call(): Pair<Any?, Exception?> {
                                    try {
                                        mediator.currentDrawing.get()?.let { currentDrawing ->
                                            with(currentDrawing.rnArtistEl.addSVG()) {
                                                this.setName(file.name.removeSuffix(".svg"))
                                                this.setPath(file.parent)
                                                currentDrawing.secondaryStructureDrawing.getFrame()?.let {
                                                    this.setWidth(it.width * 1.1)
                                                    this.setHeight(it.height * 1.1)
                                                }
                                            }
                                            mediator.scriptEngine.eval(currentDrawing.rnArtistEl.dump().toString())
                                        }
                                        return Pair(null, null)
                                    } catch (e: Exception) {
                                        return Pair(null, e)
                                    }
                                }

                            }

                            with (FileChooser()) {
                                this.getExtensionFilters()
                                    .add(FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg"))
                                val file = this.showSaveDialog(mediator.rnartist.stage)
                                file?.let {
                                    val w = RNArtistTaskDialog(mediator)
                                    w.task = ExportTask(mediator, it)
                                }
                            }

                        }

                    }

                    /**
                     * Test if this thumbnail cell (more precisely its title) matches the name of the drawing displayed.
                     * Used to disable or not some stuff in this thumbnail cell
                     */
                    private fun matchCurrentDrawing(drawing: RNArtistDrawing?):Boolean {
                        return drawing?.let {
                            drawing.rnArtistEl.getSSOrNull()?.let { ssEl ->
                                var match = false
                                ssEl.getViennaOrNull()?.getFile()?.let { fileProperty ->
                                    match = File(fileProperty.value).name.removeSuffix(".vienna").equals(titlePanel.getTitle())
                                }
                                ssEl.getBPSeqOrNull()?.getFile()?.let { fileProperty ->
                                    match = File(fileProperty.value).name.removeSuffix(".bpseq").equals(titlePanel.getTitle())
                                }
                                ssEl.getCTOrNull()?.getFile()?.let { fileProperty ->
                                    match = File(fileProperty.value).name.removeSuffix(".ct").equals(titlePanel.getTitle())
                                }
                                ssEl.getPDBOrNull()?.getFile()?.let { fileProperty ->
                                    match = File(fileProperty.value).name.removeSuffix(".pdb").equals(titlePanel.getTitle())
                                }
                                match
                            } ?: run {
                                false
                            }
                        } ?: run {
                            false
                        }
                    }

                    private inner class TitlePanel() : VBox() {
                        private val title = Label()

                        init {
                            this.alignment = Pos.CENTER
                            this.padding = Insets(10.0)
                            this.background =
                                Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets(5.0, 0.0, 5.0, 0.0)))
                            this.effect = DropShadow()
                            this.children.add(this.title)
                        }

                        fun setTitle(title:String) {
                            this.title.text = title
                        }

                        fun getTitle() = this.title.text

                    }

                    private inner class ButtonsPanel(val buttonRadius: Double = 15.0) : HBox() {
                        val group = Group()
                        val buttons = mutableListOf<Button>()
                        var bottomPanel: Pair<Double, Node>? = null
                        var leftPanelRemoved = false
                        var rightPanelRemoved = false

                        init {
                            this.spacing = 0.0
                            this.padding = Insets(
                                0.0,
                                buttonRadius,
                                buttonRadius / 2.0,
                                buttonRadius
                            )
                            this.alignment = Pos.CENTER
                            this.children.add(this.group)
                        }

                        fun addButton(icon: String): Button {
                            val button = Button(null, FontIcon(icon))
                            button.background = Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
                            (button.graphic as FontIcon).iconColor = Color.WHITE
                            val innerShadow = InnerShadow()
                            innerShadow.offsetX = 0.0
                            innerShadow.offsetY = 0.0
                            innerShadow.color = Color.WHITE
                            button.effect = innerShadow

                            buttons.add(button)
                            val p = if (this.buttons.size == 1)
                                Point2D.Double(buttonRadius, buttonRadius)
                            else {
                                Point2D.Double(this.buttons[this.buttons.size - 2].layoutX + 3.5 * buttonRadius, buttonRadius)
                            }
                            val c = Circle(0.0, 0.0, buttonRadius)
                            button.setShape(c)
                            button.layoutX = p.x - buttonRadius
                            button.layoutY = p.y - buttonRadius
                            button.setMinSize(2 * buttonRadius, 2 * buttonRadius)
                            button.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
                            this.group.children.add(button)

                            return button
                        }
                    }
                }


                private inner class Thumbnail(
                    val mediator: Mediator,
                    pngFile: File,
                    val dslScriptAbsolutePath: String
                ) {

                    var image: Image? = null
                    val layoutAndThemeUpdated = SimpleBooleanProperty()

                    init {
                        image = Image(pngFile.toPath().toUri().toString())
                        this.layoutAndThemeUpdated.addListener { _, _, _ ->
                            image = Image(pngFile.toPath().toUri().toString())
                        }
                    }

                }

                private inner class LoadDB(mediator: Mediator) : RNArtistTask(mediator) {
                    init {
                        setOnSucceeded { _ ->
                            this.rnartistTaskDialog.stage.hide()
                            loadDB.isDisable = false
                            reloadDB.isDisable = false
                            viewRNAClass.isDisable = false
                        }
                    }

                    override fun call(): Pair<Any?, Exception?> {
                        return try {
                            Platform.runLater {
                                loadDB.isDisable = true
                                viewRNAClass.isDisable = true
                                updateMessage("Loading database, please wait...")
                            }
                            Thread.sleep(100)
                            rootDB?.let {
                                val dbIndex = File(File(it), ".rnartist_db_index")
                                val pw: PrintWriter
                                if (dbIndex.exists()) {
                                    Platform.runLater {
                                        updateMessage("Found database index, please wait...")
                                    }
                                    Thread.sleep(2000)
                                    dbIndex.readLines().forEach {
                                        if (!dirsWithStructuralDataAlreadyIndexed.contains(it)) { //otherwise the reload button which call this task will add one more time the dirs stored in the index file
                                            addNewStructuralClassToTreeView(it)
                                            dirsWithStructuralDataAlreadyIndexed.add(it)
                                        }
                                    }
                                    pw = PrintWriter(FileWriter(dbIndex, true))
                                } else {
                                    dbIndex.createNewFile()
                                    pw = PrintWriter(dbIndex)
                                }

                                Platform.runLater {
                                    updateMessage("Searching for structural data in non-indexed folders, please wait...")
                                }
                                Thread.sleep(2000)
                                val dirsWithStructuralData = mutableListOf<File>()
                                getNonIndexedDirsWithStructuralData(
                                    dirsWithStructuralData,
                                    File(it).toPath()
                                )
                                if (dirsWithStructuralData.isNotEmpty()) {
                                    Platform.runLater {
                                        updateMessage("Found ${dirsWithStructuralData.size} non-indexed folders with structural data!")
                                    }
                                    Thread.sleep(2000)
                                    var i = 1
                                    dirsWithStructuralData.forEach {
                                        pw.println(it.absolutePath)
                                        addNewStructuralClassToTreeView(it.absolutePath)
                                        dirsWithStructuralDataAlreadyIndexed.add(it.absolutePath)
                                        i++
                                    }
                                    Platform.runLater {
                                        updateMessage("Indexing done!")
                                    }
                                    Thread.sleep(2000)
                                }
                                pw.close()
                            }
                            Pair(null, null)
                        } catch (e: Exception) {
                            Pair(null, e)
                        }
                    }
                }
            }

        }

        private inner class ChartsPanel() : Panel() {}

    }

    /**
     * A MainPanel is made with a menu bar, each icon in this menu allowing to display a Panel containing SubPanels
     */
    abstract inner class VerticalMainPanel() : VBox() {

        private val menuBar: MenuBar
            get() = this.children.first() as MenuBar

        init {
            this.children.add(MenuBar(true))
        }

        fun addMenuBarButton(icon: String, panel: Panel): Button {
            val b = this.menuBar.addButton(icon)
            setVgrow(panel, Priority.ALWAYS)
            b.onMouseClicked = EventHandler { mouseEvent ->
                this.children.removeAt(1)
                this.children.add(panel)
                this.menuBar.switchSelectedButton(b)
            }
            if (this.menuBar.size == 1)
                this.children.add(panel)
            return b
        }

    }

    abstract inner class HorizontalMainPanel() : HBox() {

        private val menuBar: MenuBar
            get() = this.children.first() as MenuBar

        init {
            this.children.add(MenuBar(false))
        }

        fun addMenuBarButton(icon: String, panel: Panel): Button {
            val b = this.menuBar.addButton(icon)
            setHgrow(panel, Priority.ALWAYS)
            b.onMouseClicked = EventHandler { mouseEvent ->
                this.children.removeAt(1)
                this.children.add(panel)
                this.menuBar.switchSelectedButton(b)
            }
            if (this.menuBar.size == 1)
                this.children.add(panel)
            return b
        }

    }

    private inner class MenuBar(isHorizontal: Boolean = false) : VBox() {

        private val buttons = SmallButtonsPanel(isHorizontal = isHorizontal)
        val size: Int
            get() = buttons.size

        init {
            this.padding = Insets(5.0)
            if (isHorizontal)
                this.background =
                    Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets(5.0, 5.0, 1.5, 5.0)))
            else
                this.background =
                    Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets(5.0, 1.5, 5.0, 5.0)))
            this.effect = DropShadow()

            this.children.add(buttons)

        }

        fun addButton(icon: String): Button {
            return this.buttons.addButton(icon)
        }

        fun switchSelectedButton(button: Button) {
            this.buttons.switchSelectedButton(button)
        }

        private inner class SmallButtonsPanel(
            val buttonRadius: Double = 15.0,
            val isHorizontal: Boolean = true
        ) : HBox() {

            val group = Group()
            private val buttons = mutableListOf<Button>()
            private var selectedButton: Button? = null
            val size: Int
                get() = buttons.size

            init {
                this.padding = if (isHorizontal) Insets(
                    buttonRadius / 2.0,
                    buttonRadius,
                    buttonRadius / 2.0,
                    buttonRadius
                ) else Insets(buttonRadius, buttonRadius / 2.0, buttonRadius, buttonRadius / 2.0)
                this.alignment = if (isHorizontal) Pos.CENTER_LEFT else Pos.TOP_CENTER
                this.children.add(this.group)
            }

            fun addButton(icon: String): Button {
                val button = Button(null, FontIcon(icon))
                button.background = null
                (button.graphic as FontIcon).iconColor = Color.WHITE
                button.onMouseEntered = EventHandler {
                    if (button != selectedButton) {
                        button.background =
                            Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                        (button.graphic as FontIcon).iconColor = Color.BLACK
                    }
                }
                button.onMouseExited = EventHandler {
                    if (button != selectedButton) {
                        button.background =
                            Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
                        (button.graphic as FontIcon).iconColor = Color.WHITE
                    }
                }
                buttons.add(button)
                val p = if (buttons.size == 1)
                    Point2D.Double(buttonRadius, buttonRadius)
                else {
                    if (isHorizontal)
                        Point2D.Double(buttons[buttons.size - 2].layoutX + 3.5 * buttonRadius, buttonRadius)
                    else
                        Point2D.Double(buttonRadius, buttons[buttons.size - 2].layoutY + 3.5 * buttonRadius)
                }
                val c = Circle(0.0, 0.0, buttonRadius)
                button.setShape(c)
                button.layoutX = p.x - buttonRadius
                button.layoutY = p.y - buttonRadius
                button.setMinSize(2 * buttonRadius, 2 * buttonRadius)
                button.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
                if (buttons.size == 1) {
                    button.background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (button.graphic as FontIcon).iconColor = Color.BLACK
                    val innerShadow = InnerShadow()
                    innerShadow.offsetX = 0.0
                    innerShadow.offsetY = 0.0
                    innerShadow.color = Color.WHITE
                    button.effect = innerShadow
                    selectedButton = button
                }
                this.group.children.add(button)
                return button
            }

            fun switchSelectedButton(button: Button) {
                selectedButton?.let {
                    it.background = Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
                    (it.graphic as FontIcon).iconColor = Color.WHITE
                    it.effect = null
                }
                button.background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                (button.graphic as FontIcon).iconColor = Color.BLACK
                val innerShadow = InnerShadow()
                innerShadow.offsetX = 0.0
                innerShadow.offsetY = 0.0
                innerShadow.color = Color.WHITE
                button.effect = innerShadow
                selectedButton = button
            }

        }

    }

    abstract inner class Panel() : VBox() {

        init {
            this.alignment = Pos.TOP_LEFT
            this.spacing = 10.0
            this.padding = Insets(20.0, 10.0, 10.0, 10.0)
            this.background =
                Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets(5.0, 5.0, 5.0, 5.0)))
            this.effect = DropShadow()
        }

    }

    /**
     * A SubPanel allows to define some drawing properties
     */
    abstract inner class SubPanel(title: String? = null) : VBox() {
        init {
            this.spacing = 5.0
            this.padding = Insets(0.0, 10.0, 10.0, 10.0)
            this.background = Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
            title?.let {
                val header = VBox()
                var l = Label(title)
                l.padding = Insets.EMPTY
                l.textFill = Color.WHITE
                header.children.add(l)
                var s = Separator()
                s.id = "sub"
                s.padding = Insets(2.0, 0.0, 5.0, 0.0)
                header.children.add(s)
                this.children.add(header)
            }
        }

        protected inner class LargeButtonsPanel(
            val buttonRadius: Double = 15.0,
            val buttomsPerRow: Int = 5
        ) : VBox() {

            val group = Group()
            val buttons = mutableListOf<Button>()

            init {
                this.padding = Insets.EMPTY
                this.spacing = 10.0
                this.alignment = Pos.CENTER_LEFT
                this.children.add(this.group)
            }

            fun addSeparator() {
                val separator = Separator()
                buttons.add(separator)
                val p = Point2D.Double(buttons[buttons.size - 2].layoutX + 2.75 * buttonRadius, buttonRadius)
                val c = Circle(p.x, p.y, buttonRadius)
                separator.setShape(c)
                separator.layoutX = p.x - buttonRadius
                separator.layoutY = p.y - buttonRadius
                separator.setMinSize(2 * buttonRadius, 2 * buttonRadius)
                separator.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
                this.group.children.add(separator)
            }

            fun addButton(icon: String, toolTip: String? = null): Button {
                val button = Button(null, FontIcon(icon))
                toolTip?.let {
                    button.tooltip = Tooltip(it)
                }
                button.background = null
                button.isDisable = true
                (button.graphic as FontIcon).iconColor = Color.WHITE
                buttons.add(button)
                val p = if (buttons.size == 1)
                    Point2D.Double(buttonRadius + 2.75 * buttonRadius, buttonRadius)
                else {
                    (buttons[buttons.size - 2] as? Separator)?.let {
                        Point2D.Double(buttons[buttons.size - 2].layoutX + 2.75 * buttonRadius, buttonRadius)
                    } ?: run {
                        Point2D.Double(buttons[buttons.size - 2].layoutX + 3.75 * buttonRadius, buttonRadius)
                    }
                }
                val c = Circle(p.x, p.y, buttonRadius)
                c.fill = Color.TRANSPARENT
                c.strokeWidth = 1.0
                c.stroke = if (button.isDisable) Color.DARKGRAY else Color.WHITE
                button.disableProperty().addListener { _, oldValue, newValue ->
                    c.stroke = if (newValue) Color.DARKGRAY else Color.WHITE
                }
                this.group.children.add(c)
                val buttonShape = Circle(p.x, p.y, buttonRadius)
                button.setShape(buttonShape)
                button.layoutX = p.x - buttonRadius
                button.layoutY = p.y - buttonRadius
                button.setMinSize(2 * buttonRadius, 2 * buttonRadius)
                button.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
                this.group.children.add(button)

                button.onMouseEntered = EventHandler {
                    button.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (button.graphic as FontIcon).iconColor = Color.BLACK
                }
                button.onMouseExited = EventHandler {
                    button.background = null
                    (button.graphic as FontIcon).iconColor = Color.WHITE
                }
                button.onMousePressed = EventHandler {
                    button.background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (button.graphic as FontIcon).iconColor = Color.BLACK
                }
                button.onMouseReleased = EventHandler {
                    button.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (button.graphic as FontIcon).iconColor = Color.BLACK
                }

                return button
            }

            fun addButtonWithPin(icon: String, pinIcon: String, toolTip: String? = null): Button {
                val button = this.addButton(icon, toolTip)
                val pin = Button(null, FontIcon(pinIcon))
                pin.background = null
                (pin.graphic as FontIcon).iconColor = if (button.isDisable) Color.DARKGRAY else Color.WHITE
                button.disableProperty().addListener { _, oldValue, newValue ->
                    (pin.graphic as FontIcon).iconColor = if (button.isDisable) Color.DARKGRAY else Color.WHITE
                }
                val c = Circle(
                    button.layoutX - buttonRadius / 2.0 + buttonRadius / 2.0 + 0.5,
                    button.layoutY - buttonRadius / 2.0 + buttonRadius / 2.0 + 0.5,
                    buttonRadius / 2.0 + 3.0
                )
                c.fill = RNArtistGUIColor
                c.strokeWidth = 1.0
                c.stroke = if (button.isDisable) Color.DARKGRAY else Color.WHITE
                button.disableProperty().addListener { _, oldValue, newValue ->
                    c.stroke = if (button.isDisable) Color.DARKGRAY else Color.WHITE
                }
                group.children.add(c)
                val pinShape = Circle(0.0, 0.0, buttonRadius / 2.0)
                pin.setShape(pinShape)
                pin.layoutX = button.layoutX - buttonRadius / 2.0
                pin.layoutY = button.layoutY - buttonRadius / 2.0
                pin.setMinSize(2 * buttonRadius / 2.0, 2 * buttonRadius / 2.0)
                pin.setMaxSize(2 * buttonRadius / 2.0, 2 * buttonRadius / 2.0)
                group.children.add(pin)
                return button
            }

            private inner class Separator : Button(null, FontIcon("fas-ellipsis-v:15")) {
                init {
                    this.background = null
                    (this.graphic as FontIcon).iconColor = Color.WHITE
                }
            }

        }
    }

    /*private class TopToolBar(mediator: Mediator) : HBox() {
        init {
            this.background =
                Background(BackgroundFill(RNArtistGUIColor, CornerRadii(0.0, 0.0, 10.0, 10.0, false), Insets.EMPTY))
            this.alignment = Pos.CENTER
            this.padding = Insets(10.0)
            this.spacing = 25.0

            var l = Label("Font")
            l.textFill = Color.WHITE

            val fontNames = ComboBox(
                FXCollections.observableList(Font.getFamilies().stream().distinct().collect(Collectors.toList()))
            )
            fontNames.onAction = EventHandler {
                mediator.currentDrawing.get()?.let { drawingLoaded ->
                    //drawingLoaded.drawing.workingSession.fontName = fontNames.value
                    mediator.canvas2D.repaint()
                }
            }
            fontNames.maxWidth = Double.MAX_VALUE

            var g = HBox()
            g.alignment = Pos.CENTER
            g.spacing = 5.0
            g.children.addAll(l, fontNames)
            this.children.add(g)

            val deltaXRes = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, 0)
            deltaXRes.prefWidth = 50.0
            deltaXRes.valueProperty().addListener { observable, oldValue, newValue ->
                mediator.currentDrawing.get()?.let { drawingLoaded ->
                    //drawingLoaded.drawing.workingSession.deltafontx = deltaXRes.value
                    mediator.canvas2D.repaint()
                }
            }
            l = Label("x")
            l.textFill = Color.WHITE
            g = HBox()
            g.alignment = Pos.CENTER
            g.spacing = 5.0
            g.children.addAll(l, deltaXRes)
            this.children.add(g)

            val deltaYRes = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, 0)
            deltaYRes.prefWidth = 50.0
            deltaYRes.valueProperty().addListener { observable, oldValue, newValue ->
                mediator.currentDrawing.get()?.let { drawingLoaded ->
                    //drawingLoaded.drawing.workingSession.deltafonty = deltaYRes.value
                    mediator.canvas2D.repaint()
                }
            }
            l = Label("y")
            l.textFill = Color.WHITE
            g = HBox()
            g.alignment = Pos.CENTER
            g.spacing = 5.0
            g.children.addAll(l, deltaYRes)
            this.children.add(g)

            val deltaFontSize = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, 0)
            deltaFontSize.prefWidth = 50.0
            deltaFontSize.valueProperty().addListener { observable, oldValue, newValue ->
                mediator.currentDrawing.get()?.let { drawingLoaded ->
                    //drawingLoaded.drawing.workingSession.deltafontsize = deltaFontSize.value
                    mediator.canvas2D.repaint()
                }
            }
            l = Label("size")
            l.textFill = Color.WHITE
            g = HBox()
            g.alignment = Pos.CENTER
            g.spacing = 5.0
            g.children.addAll(l, deltaFontSize)
            this.children.add(g)
        }
    }*/

    abstract private inner class AbstractColorLineWidthSubPanel() :
        SubPanel("Colors & Lines") {

        val colorWheelGroup = Group()
        val lineWidthKnobGroup = Group()
        val colorsFromWheel = mutableMapOf<javafx.geometry.Point2D, DoubleArray>()
        val colorWheel = Canvas(120.0, 120.0)
        val saturationBar = HBox()
        val brightnessBar = HBox()
        val lastColors = HBox()
        val lineWheel = SVGPath()
        val currentLineWidth = Circle(0.0, 0.0, 7.0)
        var currentAngle = 0.0
        val fontButton = FontButton()
        var applyThemeOnFont: Boolean = false
            get() = fontButton.applyThemeOnFont

        init {
            val knobPane = Pane()
            knobPane.minHeight = 180.0
            knobPane.prefHeight = 180.0
            knobPane.maxHeight = 180.0
            val hbox = HBox()
            hbox.padding = Insets(10.0, 0.0, 0.0, 0.0)
            hbox.alignment = Pos.TOP_CENTER
            hbox.children.add(knobPane)
            this.children.add(hbox)
            knobPane.children.add(colorWheelGroup)

            colorWheel.layoutX = 30.0
            colorWheel.layoutY = 25.0
            colorWheelGroup.children.add(colorWheel)
            var i = 0
            while (i < 12) {
                var rotatedPoint = rotatePoint(
                    Point2D.Double(90.0, 0.0),
                    Point2D.Double(90.0, 85.0),
                    30.0 * i
                )
                var c =
                    Circle(rotatedPoint.x, rotatedPoint.y, if (i == 0 || i == 3 || i == 6 || i == 9) 5.0 else 2.5)
                c.fill = Color.WHITE
                colorWheelGroup.children.add(c)
                i++
            }

            this.lineWheel.content =
                "M139.247,92.223C140.811,86.39 150,81.337 150,75.016C150,68.663 140.811,63.61 139.247,57.777C137.651,51.75 143.028,42.788 139.965,37.509C139.815,37.249 139.576,37.06 139.395,36.826C139.265,36.659 139.132,36.492 138.984,36.335C135.108,32.183 126.016,31.967 122.041,27.96C121.747,27.668 121.486,27.373 121.259,27.049C117.961,22.701 117.513,14.499 113.589,10.92C113.503,10.841 113.409,10.771 113.32,10.698C113.054,10.478 112.823,10.212 112.524,10.037C107.647,7.224 99.723,11.515 93.766,11C93.548,10.981 93.328,10.957 93.121,10.924C92.817,10.879 92.514,10.831 92.222,10.755C86.39,9.191 81.338,0 75.017,0C68.662,0 63.611,9.191 57.778,10.755C57.488,10.831 57.187,10.877 56.885,10.924C56.678,10.957 56.458,10.981 56.239,11C50.304,11.514 42.356,7.224 37.509,10.037C35.749,11.048 34.575,12.873 33.663,14.991C31.773,19.357 30.893,25.059 27.992,27.96C23.987,31.967 14.919,32.183 11.022,36.335C10.874,36.492 10.741,36.659 10.61,36.826C10.427,37.06 10.187,37.25 10.037,37.509C6.974,42.788 12.351,51.75 10.753,57.777C9.189,63.61 0,68.663 0,75.016C0,81.338 9.189,86.39 10.753,92.223C12.351,98.25 6.974,107.212 10.037,112.491C10.188,112.749 10.429,112.94 10.612,113.175C10.742,113.341 10.874,113.506 11.021,113.662C14.916,117.815 23.986,118.031 27.992,122.04C28.253,122.301 28.514,122.593 28.775,122.92C32.04,127.264 32.46,135.482 36.422,139.07C36.515,139.153 36.613,139.226 36.707,139.304C36.976,139.522 37.208,139.788 37.509,139.963C42.357,142.776 50.31,138.482 56.245,138.998C56.461,139.019 56.676,139.041 56.88,139.073C57.184,139.119 57.486,139.167 57.777,139.245C63.611,140.809 68.661,150 75.016,150C81.338,150 86.39,140.809 92.222,139.245C92.514,139.167 92.82,139.12 93.125,139.073C93.329,139.041 93.544,139.019 93.759,138.998C99.716,138.48 107.644,142.776 112.524,139.963C114.284,138.952 115.425,137.127 116.338,135.009C118.226,130.643 119.106,124.941 122.041,122.04C126.017,118.031 135.111,117.815 138.987,113.662C139.131,113.506 139.263,113.341 139.393,113.175C139.576,112.94 139.814,112.749 139.964,112.491C143.028,107.212 137.651,98.251 139.247,92.223ZM132.567,74.984C132.567,94.633 122.692,112.003 107.636,122.398C98.35,128.818 87.14,132.565 75.017,132.565C59.506,132.565 45.427,126.406 35.066,116.402C24.18,105.942 17.435,91.244 17.435,74.984C17.435,55.367 27.309,37.997 42.365,27.602C51.653,21.182 62.894,17.435 75.018,17.435C90.528,17.435 104.574,23.594 114.937,33.598C125.82,44.058 132.567,58.756 132.567,74.984Z"
            this.lineWheel.layoutX = 15.0
            this.lineWheel.layoutY = 10.0
            this.lineWheel.stroke = Color.BLACK
            this.lineWheel.strokeWidth = 1.0

            var innerShadow = InnerShadow()
            innerShadow.offsetX = 0.0
            innerShadow.offsetY = 0.0
            innerShadow.color = Color.WHITE
            this.lineWheel.effect = innerShadow
            val radialGradient = RadialGradient(
                0.0,
                0.0,
                75.0,
                75.0,
                180.0,
                false,
                CycleMethod.NO_CYCLE,
                Stop(0.0, Color.DARKSLATEGREY),
                Stop(1.0, Color.WHITE)
            )
            this.lineWheel.fill = radialGradient
            lineWidthKnobGroup.children.add(this.lineWheel)

            this.currentLineWidth.fill = Color.WHITE
            this.currentLineWidth.strokeWidth = 1.0
            this.currentLineWidth.stroke = Color.BLACK
            innerShadow = InnerShadow()
            innerShadow.offsetX = 0.0
            innerShadow.offsetY = 0.0
            innerShadow.color = Color.BLACK
            this.currentLineWidth.effect = innerShadow
            this.currentLineWidth.layoutX = 90.0
            this.currentLineWidth.layoutY = 18.0
            this.currentLineWidth.onMousePressed = EventHandler {
                currentLineWidth.fill = Color.DARKORANGE
            }
            this.currentLineWidth.onMouseReleased = EventHandler {
                currentLineWidth.fill = Color.WHITE
            }
            lineWidthKnobGroup.children.add(this.currentLineWidth)

            colorWheelGroup.children.add(lineWidthKnobGroup)

            val optionsVBox = VBox()
            optionsVBox.alignment = Pos.TOP_LEFT
            optionsVBox.padding = Insets(20.0, 0.0, 0.0, 0.0)
            optionsVBox.spacing = 5.0
            this.children.add(optionsVBox)

            var l = Label("Saturation")
            optionsVBox.children.add(l)
            saturationBar.spacing = 5.0
            optionsVBox.children.add(saturationBar)

            l = Label("Brightness")
            optionsVBox.children.add(l)
            brightnessBar.spacing = 5.0
            optionsVBox.children.add(brightnessBar)

            l = Label("Last Colors")
            optionsVBox.children.add(l)
            (1..6).forEach {
                if (it == 1) {
                    val color = doubleArrayOf(Random.nextDouble(0.0, 360.0), Random.nextDouble(), 1.0)
                    addLastColor(color)
                    repaintBrightness(color)
                    repaintSaturation(color)
                } else
                    addLastColor(doubleArrayOf(Random.nextDouble(0.0, 360.0), Random.nextDouble(), 1.0))
            }
            lastColors.spacing = 5.0
            optionsVBox.children.add(lastColors)

            var xOffset: Double
            var yOffset: Double
            val centerX = 60.0
            val centerY = 60.0
            val radius = 60.0
            var centreOffset: Double
            var centreAngle: Double
            for (x in 0 until 120) {
                for (y in 0 until 120) {
                    xOffset = x - centerX
                    yOffset = y - centerY
                    centreOffset = Math.hypot(xOffset, yOffset)
                    if (centreOffset <= radius) {
                        centreAngle = (Math.toDegrees(Math.atan2((yOffset), (xOffset))) + 360.0) % 360.0
                        val color = Color.hsb(centreAngle, (centreOffset / radius), 1.0)
                        colorsFromWheel[javafx.geometry.Point2D(x.toDouble(), y.toDouble())] =
                            doubleArrayOf(centreAngle, (centreOffset / radius), 1.0)
                        colorWheel.graphicsContext2D.fill = color
                        colorWheel.graphicsContext2D.fillOval(x.toDouble() - 0.5, y.toDouble() - 0.5, 1.0, 1.0)
                    }
                }
            }

            colorWheel.graphicsContext2D.strokeOval(centerX - radius, centerX - radius, radius * 2, radius * 2)

        }

        abstract fun addLastColor(c: DoubleArray)

        abstract protected fun repaintBrightness(c: DoubleArray)

        abstract protected fun repaintSaturation(c: DoubleArray)

        abstract protected fun getSelector(): (DrawingElement) -> Boolean

        /**
         * If the font button is not disabled and on, the types are those for the letters. Otherwise, it is all the types except those for the letters
         */
        protected open fun getSecondaryStructureTypes(): List<SecondaryStructureType> {
            val types = mutableListOf<SecondaryStructureType>()
            if (this.applyThemeOnFont) {
                types.add(SecondaryStructureType.A)
                types.add(SecondaryStructureType.U)
                types.add(SecondaryStructureType.G)
                types.add(SecondaryStructureType.C)
                types.add(SecondaryStructureType.X)
            } else
                SecondaryStructureType.entries.forEach {
                    if (it != SecondaryStructureType.A && it != SecondaryStructureType.U && it != SecondaryStructureType.G && it != SecondaryStructureType.C && it != SecondaryStructureType.X)
                        types.add(it)
                }
            return types
        }

        abstract protected fun applyTheme(theme: Theme)

        fun disableFontButton(disable: Boolean) {
            this.fontButton.isDisable = disable
        }

        protected inner class FontButton() : Button(null, FontIcon("fas-font:15")) {

            private var applyOnFont = false
            var applyThemeOnFont: Boolean = false
                get() = applyOnFont && !this.isDisable

            init {
                this.isDisable = true
                var c = Circle(0.0, 0.0, 20.0)
                this.background = null
                (this.graphic as FontIcon).iconColor = Color.WHITE
                this.onMouseClicked = EventHandler {
                    this.applyOnFont = !this.applyOnFont
                    when (this.applyOnFont) {
                        true -> {
                            (this.graphic as FontIcon).iconColor = Color.DARKORANGE
                        }

                        false -> {
                            (this.graphic as FontIcon).iconColor = Color.WHITE
                        }
                    }
                }
                this.shape = c
                this.layoutX = 0.0
                this.layoutY = 150.0
                this.setMinSize(15.0, 15.0)
                this.setMaxSize(15.0, 15.0)
                colorWheelGroup.children.add(this)
            }
        }

    }

    private class JunctionKnob(
        val mediator: Mediator,
        val label: String? = null,
        junction: JunctionDrawing? = null
    ) : VBox() {

        val connectors = mutableListOf<JunctionConnector>()
        var connectorRadius = 8.5
        var knobCenterX = 90.0
        var knobCenterY = 90.0
        var knobRadius = 70.0
        val group = Group()

        //val targetsComboBox = ComboBox<KnobTarget>()
        var selectedJunction: JunctionDrawing? = null
            set(value) {
                field = value
                this.updateConnectors()
            }

        init {
            this.background = Background(BackgroundFill(RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
            this.alignment = Pos.TOP_CENTER
            this.spacing = 5.0
            this.isFillWidth = true
            this.padding = Insets(10.0, 10.0, 20.0, 10.0)

            val c = Circle(knobCenterX, knobCenterY, this.knobRadius - 3 * this.connectorRadius)
            c.strokeWidth = 1.0
            c.stroke = Color.BLACK
            val innerShadow = InnerShadow()
            innerShadow.offsetX = 0.0
            innerShadow.offsetY = 0.0
            innerShadow.color = Color.WHITE
            c.effect = innerShadow
            val radialGradient = RadialGradient(
                0.0,
                0.0,
                knobCenterX,
                knobCenterY,
                this.knobRadius * 2.0,
                false,
                CycleMethod.NO_CYCLE,
                Stop(0.0, Color.DARKSLATEGREY),
                Stop(1.0, Color.WHITE)
            )
            c.fill = radialGradient
            this.group.children.add(c)

            for (i in 1 until 16) {
                val p = rotatePoint(
                    Point2D.Double(
                        this.knobCenterX,
                        this.knobCenterY + this.knobRadius - 1.65 * connectorRadius
                    ),
                    Point2D.Double(this.knobCenterX, this.knobCenterY),
                    i * 360.0 / 16.0
                )
                val _connector = JunctionConnector(getConnectorId(i), false, p.x, p.y, this)
                this.group.children.add(_connector)
                this.connectors.add(_connector)
            }
            this.group.children.add(Left(this))
            this.group.children.add(Right(this))

            this.group.children.add(Up(this))
            this.group.children.add(Down(this))

            val v = VBox()
            v.children.add(group)

            v.alignment = Pos.CENTER
            var l = Label("N")
            l.font = Font.font("Monospace", FontWeight.BOLD, 13.0)
            l.layoutXProperty().bind(SimpleDoubleProperty(90.0).subtract(l.widthProperty().divide(2)))
            l.layoutYProperty().bind(
                connectors[7].centerYProperty().subtract(connectorRadius * 1.5).subtract(l.heightProperty())
            )
            this.group.children.add(l)
            l = Label("W")
            l.font = Font.font("Monospace", FontWeight.BOLD, 13.0)
            l.layoutXProperty().bind(
                connectors[3].centerXProperty().subtract(connectorRadius * 1.5).subtract(l.widthProperty())
            )
            l.layoutYProperty().bind(connectors[3].centerYProperty().subtract(l.heightProperty().divide(2)))
            this.group.children.add(l)
            l = Label("E")
            l.font = Font.font("Monospace", FontWeight.BOLD, 13.0)
            l.layoutXProperty().bind(connectors[11].centerXProperty().add(connectorRadius * 1.5))
            l.layoutYProperty()
                .bind(connectors[11].centerYProperty().subtract(l.heightProperty().divide(2)))
            this.group.children.add(l)

            this.children.add(v)

            /*val labels = listOf(
                 KnobTarget("Any", null),
                 KnobTarget("Apical Loops", JunctionType.ApicalLoop),
                 KnobTarget("Inner Loops", JunctionType.InnerLoop),
                 KnobTarget("3-Ways", JunctionType.ThreeWay),
                 KnobTarget("4-Ways", JunctionType.FourWay),
                 KnobTarget("5-Ways", JunctionType.FiveWay),
                 KnobTarget("6-Ways", JunctionType.SixWay),
                 KnobTarget("7-Ways", JunctionType.SevenWay),
                 KnobTarget("8-Ways", JunctionType.EightWay)
             )
             this.targetsComboBox.items.addAll(labels)
             this.targetsComboBox.selectionModel.select(0)
             this.targetsComboBox.minWidth = 150.0
             this.targetsComboBox.maxWidth = 150.0
             this.targetsComboBox.onAction = EventHandler {

             }*/

            /*val hbox = HBox()
            hbox.padding = Insets(5.0, 0.0, 0.0, 0.0)
            hbox.spacing = 5.0
            hbox.alignment = Pos.CENTER_LEFT
            hbox.children.add(Label("Target"))
            hbox.children.add(targetsComboBox)
            this.children.add(hbox)*/

            this.selectedJunction = junction
        }

        fun getCurrentLayout(): List<ConnectorId> {
            val layout = mutableListOf<ConnectorId>()
            this.connectors.forEach {
                if (it.selected)
                    layout.add(it.connectorId)
            }
            return layout
        }

        fun updateConnectors() {
            selectedJunction?.let { junction ->
                this.connectors.forEach {
                    it.selected = junction.currentLayout.contains(it.connectorId)
                }
            } ?: run {
                this.connectors.forEach {
                    it.selected = false
                    it.fill = Color.DARKGRAY
                }
            }
        }

        private class KnobTarget(val name: String, val junctionType: JunctionType? = null) {
            override fun toString(): String {
                return this.name
            }
        }

        private open class JunctionConnector(
            val connectorId: ConnectorId,
            selected: Boolean = false,
            centerX: Double,
            centerY: Double,
            private val knob: JunctionKnob
        ) : Circle(centerX, centerY, knob.connectorRadius) {

            var selected = selected
                set(value) {
                    field = value
                    if (selected)
                        this.fill = Color.DARKORANGE
                    else
                        this.fill = Color.WHITE
                }

            init {
                this.strokeWidth = 2.0
                this.stroke = Color.BLACK
                this.fill = Color.WHITE

                val innerShadow = InnerShadow()
                innerShadow.offsetX = 1.0
                innerShadow.offsetY = 1.0
                innerShadow.color = Color.WHITE
                this.effect = innerShadow
                this.onMouseClicked = EventHandler {
                    knob.selectedJunction?.let { junction ->
                        var selectedCount = knob.connectors.count { it.selected }
                        if (selectedCount < junction.junctionType.value - 1) {
                            this.selected = !this.selected
                        } else if (selectedCount >= junction.junctionType.value - 1) { //we can only unselect
                            this.selected = false
                        }
                        //after the click, if we have the selected circles corresponding to helixCount-1 (-1 since the inner helix in red doesn't count)
                        selectedCount = knob.connectors.count { it.selected }
                        if (selectedCount == junction.junctionType.value - 1) {
                            if (junction.junctionType != JunctionType.ApicalLoop) {
                                junction.applyLayout(layout {
                                    junction {
                                        name = junction.name
                                        out_ids = knob.getCurrentLayout().map { it.toString() }
                                            .joinToString(separator = " ")
                                    }
                                })
                                knob.mediator.currentDrawing.get()?.let { currentDrawing ->
                                    val step = currentDrawing.rnArtistEl.getLayoutOrNew().lastStep + 1
                                    setJunction(
                                        currentDrawing.rnArtistEl,
                                        outIds = knob.getCurrentLayout().map { it.toString() }
                                            .joinToString(separator = " "),
                                        location = junction.location,
                                        step = step)
                                }
                                knob.mediator.canvas2D.repaint()
                            }
                        }

                    }
                }
            }
        }

        private inner class Up(val knob: JunctionKnob) : javafx.scene.shape.Path() {

            init {
                val moveTo = MoveTo()
                moveTo.x = knob.knobCenterX
                moveTo.y = knob.knobCenterY - 4 * knob.connectorRadius
                this.elements.add(moveTo)
                var lineTo = LineTo()
                lineTo.x = knob.knobCenterX - knob.connectorRadius * 1.5
                lineTo.y = knob.knobCenterY - 2 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX + knob.connectorRadius * 1.5
                lineTo.y = knob.knobCenterY - 2 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX
                lineTo.y = knob.knobCenterY - 4 * knob.connectorRadius
                this.elements.add(lineTo)

                this.stroke = Color.BLACK
                this.strokeWidth = 1.0
                this.fill = Color.WHITE
                val innerShadow = InnerShadow()
                innerShadow.offsetX = 0.0
                innerShadow.offsetY = 0.0
                innerShadow.color = Color.BLACK
                this.effect = innerShadow

                this.onMousePressed = EventHandler {
                    this.fill = Color.DARKORANGE
                    selectedJunction?.let {
                        it.radiusRatio += 0.1
                        it.applyLayout(layout {
                            junction {
                                location {
                                    it.location.blocks.forEach {
                                        it.start to it.end
                                    }
                                }
                                radius =
                                    Math.round(it.initialRadius * it.radiusRatio * 100.0) / 100.0
                            }
                        })
                        mediator.currentDrawing.get()?.let { currentDrawing ->
                            val step = currentDrawing.rnArtistEl.getLayoutOrNew().lastStep + 1
                            setJunction(
                                currentDrawing.rnArtistEl,
                                radius = Math.round(it.initialRadius * it.radiusRatio * 100.0) / 100.0,
                                location = it.location,
                                step = step
                            )
                        }
                        this.knob.mediator.canvas2D.repaint()
                    }
                }
                this.onMouseReleased = EventHandler {
                    this.fill = Color.WHITE
                }
            }
        }

        private inner class Down(val knob: JunctionKnob) : javafx.scene.shape.Path() {

            init {
                val moveTo = MoveTo()
                moveTo.x = knob.knobCenterX
                moveTo.y = knob.knobCenterY + 4 * knob.connectorRadius
                this.elements.add(moveTo)
                var lineTo = LineTo()
                lineTo.x = knob.knobCenterX - knob.connectorRadius * 1.5
                lineTo.y = knob.knobCenterY + 2 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX + knob.connectorRadius * 1.5
                lineTo.y = knob.knobCenterY + 2 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX
                lineTo.y = knob.knobCenterY + 4 * knob.connectorRadius
                this.elements.add(lineTo)

                this.stroke = Color.BLACK
                this.strokeWidth = 1.0
                this.fill = Color.WHITE
                val innerShadow = InnerShadow()
                innerShadow.offsetX = 0.0
                innerShadow.offsetY = 0.0
                innerShadow.color = Color.BLACK
                this.effect = innerShadow

                this.onMousePressed = EventHandler {
                    this.fill = Color.DARKORANGE
                    selectedJunction?.let {
                        it.radiusRatio -= 0.1
                        it.applyLayout(layout {
                            junction {
                                name = it.name
                                radius =
                                    Math.round(it.initialRadius * it.radiusRatio * 100.0) / 100.0
                            }
                        })
                        mediator.currentDrawing.get()?.let { currentDrawing ->
                            val step = currentDrawing.rnArtistEl.getLayoutOrNew().lastStep + 1
                            setJunction(
                                currentDrawing.rnArtistEl,
                                radius = Math.round(it.initialRadius * it.radiusRatio * 100.0) / 100.0,
                                location = it.location,
                                step = step
                            )
                        }
                        this.knob.mediator.canvas2D.repaint()
                    }
                }
                this.onMouseReleased = EventHandler {
                    this.fill = Color.WHITE
                }
            }

        }

        private inner class Left(val knob: JunctionKnob) : javafx.scene.shape.Path() {

            init {
                val moveTo = MoveTo()
                moveTo.x = knob.knobCenterX - 4 * knob.connectorRadius
                moveTo.y = knob.knobCenterY
                this.elements.add(moveTo)
                var lineTo = LineTo()
                lineTo.x = knob.knobCenterX - 2 * knob.connectorRadius
                lineTo.y = knob.knobCenterY - 1.5 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX - 2 * knob.connectorRadius
                lineTo.y = knob.knobCenterY + 1.5 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX - 4 * knob.connectorRadius
                lineTo.y = knob.knobCenterY
                this.elements.add(lineTo)

                this.stroke = Color.BLACK
                this.strokeWidth = 1.0
                this.fill = Color.WHITE
                val innerShadow = InnerShadow()
                innerShadow.offsetX = 0.0
                innerShadow.offsetY = 0.0
                innerShadow.color = Color.BLACK
                this.effect = innerShadow

                this.onMousePressed = EventHandler {
                    this.fill = Color.DARKORANGE
                    selectedJunction?.let {
                        if (it.junctionType != JunctionType.ApicalLoop) {
                            val currentLayout = it.currentLayout
                            if (currentLayout.first() != ConnectorId.ssw) {
                                val newLayout = mutableListOf<ConnectorId>()
                                currentLayout.forEach {
                                    newLayout.add(getConnectorId(it.value - 1))
                                }
                                it.applyLayout(layout {
                                    junction {
                                        name = it.name
                                        out_ids = newLayout.map { it.toString() }
                                            .joinToString(separator = " ")
                                    }
                                })
                                mediator.currentDrawing.get()?.let { currentDrawing ->
                                    val step = currentDrawing.rnArtistEl.getLayoutOrNew().lastStep + 1
                                    setJunction(currentDrawing.rnArtistEl,
                                        outIds = newLayout.map { it.toString() }
                                        .joinToString(separator = " "),
                                        location = it.location,
                                        step = step)
                                }
                                this.knob.mediator.canvas2D.repaint()
                                this.knob.updateConnectors()
                            }
                        }
                    }
                }
                this.onMouseReleased = EventHandler {
                    this.fill = Color.WHITE
                }
            }

        }

        private inner class Right(private val knob: JunctionKnob) : javafx.scene.shape.Path() {

            init {
                val moveTo = MoveTo()
                moveTo.x = knob.knobCenterX + 4 * knob.connectorRadius
                moveTo.y = knob.knobCenterY
                this.elements.add(moveTo)
                var lineTo = LineTo()
                lineTo.x = knob.knobCenterX + 2 * knob.connectorRadius
                lineTo.y = knob.knobCenterY - 1.5 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX + 2 * knob.connectorRadius
                lineTo.y = knob.knobCenterY + 1.5 * knob.connectorRadius
                this.elements.add(lineTo)
                lineTo = LineTo()
                lineTo.x = knob.knobCenterX + 4 * knob.connectorRadius
                lineTo.y = knob.knobCenterY
                this.elements.add(lineTo)

                this.stroke = Color.BLACK
                this.strokeWidth = 1.0
                this.fill = Color.WHITE
                val innerShadow = InnerShadow()
                innerShadow.offsetX = 0.0
                innerShadow.offsetY = 0.0
                innerShadow.color = Color.BLACK
                this.effect = innerShadow

                this.onMousePressed = EventHandler {
                    this.fill = Color.DARKORANGE
                    selectedJunction?.let {
                        if (it.junctionType != JunctionType.ApicalLoop) {
                            val currentLayout = it.currentLayout
                            if (currentLayout.last() != ConnectorId.sse) {
                                val newLayout = mutableListOf<ConnectorId>()
                                currentLayout.forEach {
                                    newLayout.add(getConnectorId(it.value + 1))
                                }
                                it.applyLayout(layout {
                                    junction {
                                        name = it.name
                                        out_ids = newLayout.map { it.toString() }
                                            .joinToString(separator = " ")
                                    }
                                })
                                mediator.currentDrawing.get()?.let { currentDrawing ->
                                    val step = currentDrawing.rnArtistEl.getLayoutOrNew().lastStep + 1
                                    setJunction(currentDrawing.rnArtistEl,
                                        outIds = newLayout.map { it.toString() }
                                        .joinToString(separator = " "),
                                        location = it.location,
                                        step = step)
                                }
                                this.knob.mediator.canvas2D.repaint()
                                this.knob.updateConnectors()
                            }
                        }
                    }
                    this.onMouseReleased = EventHandler {
                        this.fill = Color.WHITE
                    }
                }
            }

        }

    }

    private open inner class AlwaysVisibleBar(val buttonRadius: Double = 15.0) : HBox() {
        val group = Group()
        private val buttons = mutableListOf<Button>()
        var bottomPanel: Pair<Double, Node>? = null
        var leftPanelRemoved = false
        var rightPanelRemoved = false

        init {
            this.padding = Insets(
                buttonRadius / 2.0,
                buttonRadius,
                buttonRadius / 2.0,
                buttonRadius
            )
            this.background =
                Background(BackgroundFill(RNArtistGUIColor, CornerRadii(5.0), Insets(1.5, 5.0, 5.0, 5.0)))
            this.effect = DropShadow()
            this.alignment = Pos.CENTER
            this.children.add(this.group)
        }

        fun addButton(icon: String, clickable: Boolean = true, color: Color = Color.WHITE): Button {
            val button = Button(null, FontIcon(icon))
            button.background = null
            (button.graphic as FontIcon).iconColor = color
            buttons.add(button)
            val p = if (this.buttons.size == 1)
                Point2D.Double(buttonRadius, buttonRadius)
            else {
                Point2D.Double(this.buttons[this.buttons.size - 2].layoutX + 3.5 * buttonRadius, buttonRadius)
            }
            val c = Circle(0.0, 0.0, buttonRadius)
            button.setShape(c)
            button.layoutX = p.x - buttonRadius
            button.layoutY = p.y - buttonRadius
            button.setMinSize(2 * buttonRadius, 2 * buttonRadius)
            button.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
            this.group.children.add(button)

            if (clickable) {
                button.onMouseEntered = EventHandler {
                    button.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (button.graphic as FontIcon).iconColor = Color.BLACK
                }
                button.onMouseExited = EventHandler {
                    button.background = null
                    (button.graphic as FontIcon).iconColor = Color.WHITE
                }
                button.onMousePressed = EventHandler {
                    button.background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (button.graphic as FontIcon).iconColor = Color.BLACK
                }
                button.onMouseReleased = EventHandler {
                    button.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (button.graphic as FontIcon).iconColor = Color.BLACK
                }
            }

            return button
        }
    }

    private inner class NavigationBar : AlwaysVisibleBar() {

        init {

            mediator.currentDrawing.addListener { _, _, newValue ->
                this.group.children.filterIsInstance<Button>().subList(0, 3).forEach {
                    it.isDisable = newValue == null
                }
            }

            mediator.currentDrawing.addListener { _, oldValue, newValue ->
                newValue?.let {
                    it.selectedDrawings.addListener(ListChangeListener {
                        if (it.list.isEmpty()) {
                            mediator.lastDrawingHighlighted.set(null)
                            this.group.children.filterIsInstance<Button>()
                                .subList(3, this.group.children.filterIsInstance<Button>().size).forEach {
                                    it.isDisable = true
                                }
                        } else if (it.list.size == 1) {
                            mediator.lastDrawingHighlighted.set(it.list.first())
                            this.group.children.filterIsInstance<Button>()
                                .subList(3, this.group.children.filterIsInstance<Button>().size).forEach {
                                    it.isDisable = false
                                }
                        } else {
                            mediator.lastDrawingHighlighted.set(null)
                            this.group.children.filterIsInstance<Button>()
                                .subList(3, this.group.children.filterIsInstance<Button>().size).forEach {
                                    it.isDisable = false
                                }
                        }
                    })
                }
            }

            this.addButton("fas-plus:15").onMouseClicked = EventHandler {
                mediator.workingSession?.zoomView(
                    mediator.canvas2D.bounds.centerX,
                    mediator.canvas2D.bounds.centerY,
                    true
                )
                mediator.canvas2D.repaint()
            }

            this.addButton("fas-minus:15").onMouseClicked = EventHandler {
                mediator.workingSession?.zoomView(
                    mediator.canvas2D.bounds.centerX,
                    mediator.canvas2D.bounds.centerY,
                    false
                )
                mediator.canvas2D.repaint()
            }

            this.addButton("fas-expand-arrows-alt:15").onMouseClicked =
                EventHandler {
                    mediator.canvas2D.fitStructure(null)
                }

            this.addButton("fas-chevron-left:15").onMouseClicked =
                EventHandler { mouseEvent ->
                    val sortedSelection = mediator.currentDrawing.get()?.selectedDrawings?.map { it }
                        ?.sortedBy { (it as? JunctionDrawing)?.junction?.location?.end ?: it.location.end }
                    mediator.lastDrawingHighlighted.get()?.let {
                        val currentPos = sortedSelection?.indexOf(it)!!
                        val newPos =
                            if (currentPos == 0) sortedSelection.size - 1 else currentPos - 1
                        mediator.lastDrawingHighlighted.set(
                            sortedSelection.get(newPos)
                        )
                    } ?: run {
                        mediator.lastDrawingHighlighted.set(sortedSelection?.last())
                    }
                    mediator.lastDrawingHighlighted.get()?.let {
                        it.selectionShape.let {
                            mediator.canvas2D.centerDisplayOn(
                                it.bounds2D
                            )
                        }
                    }

                }

            this.addButton("fas-chevron-right:15").onMouseClicked =
                EventHandler { mouseEvent ->
                    val sortedSelection = mediator.currentDrawing.get()?.selectedDrawings?.map { it }
                        ?.sortedBy {
                            (it as? JunctionDrawing)?.junction?.location?.start ?: it.location.start
                        }
                    mediator.lastDrawingHighlighted.get()?.let {
                        val currentPos = sortedSelection?.indexOf(it)!!
                        val newPos =
                            if (currentPos == sortedSelection.size - 1) 0 else currentPos + 1
                        mediator.lastDrawingHighlighted.set(
                            sortedSelection.get(newPos)
                        )
                    } ?: run {
                        mediator.lastDrawingHighlighted.set(sortedSelection?.first())
                    }
                    mediator.lastDrawingHighlighted.get()?.let {
                        it.selectionShape.let {
                            mediator.canvas2D.centerDisplayOn(
                                it.bounds2D
                            )
                        }
                    }
                }

            this.group.children.filterIsInstance<Button>().forEach {
                it.isDisable = true
            }
        }

    }

    private inner class UndoRedoThemeBar : AlwaysVisibleBar() {

        val playButton: Button
        var animationRunning = false

        init {

            mediator.currentDrawing.addListener { _, _, newValue ->
                this.group.children.filterIsInstance<Button>().forEach {
                    it.isDisable = newValue == null
                }
            }

            this.addButton("fas-angle-double-left:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.currentDrawing.get()?.let { currentDrawing ->
                    mediator.rollbackThemeHistoryToStart()
                }
            }

            this.addButton("fas-angle-left:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.currentDrawing.get()?.let { currentDrawing ->
                    mediator.rollbackToPreviousThemeInHistory()
                }
            }

            this.playButton = this.addButton("fas-paint-brush:15", clickable = false)

            this.playButton.background =
                Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
            (this.playButton.graphic as FontIcon).iconColor = Color.BLACK
            val innerShadow = InnerShadow()
            innerShadow.offsetX = 0.0
            innerShadow.offsetY = 0.0
            innerShadow.color = Color.WHITE
            this.playButton.effect = innerShadow

            this.playButton.onMouseClicked = EventHandler<MouseEvent> {
                if (animationRunning) {
                    this.timer.stop()
                    this.playButton.background =
                        Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (this.playButton.graphic as FontIcon).iconColor = Color.BLACK
                    animationRunning = false
                } else {
                    this.timer.start()
                    animationRunning = true
                }
            }

            this.addButton("fas-angle-right:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.currentDrawing.get()?.let { currentDrawing ->
                    mediator.applyNextThemeInHistory()
                }
            }

            this.addButton("fas-angle-double-right:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.currentDrawing.get()?.let { currentDrawing ->
                    mediator.applyThemeInHistoryFromNextToEnd()
                }
            }

            this.group.children.filterIsInstance<Button>().forEach {
                it.isDisable = true
            }
        }

        private var timer: AnimationTimer = object : AnimationTimer() {
            var count = 0
            var increment = true

            override fun handle(now: Long) {
                count++
                if (count % 30 == 0) {
                    if (count / 30 % 2 == 0) {
                        playButton.background = Background(
                            BackgroundFill(
                                if (increment) Color.GREEN else Color.ORANGE,
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                            )
                        )
                        (playButton.graphic as Icon).iconColor = Color.WHITE
                    } else {
                        playButton.background =
                            Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                        (playButton.graphic as FontIcon).iconColor = Color.BLACK
                    }

                    mediator.currentDrawing.get()?.let { currentDrawing ->
                        val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
                        if (themeEl.undoRedoCursor == 0)
                            increment = true
                        else if (themeEl.undoRedoCursor == themeEl.historyLength)
                            increment = false
                        if (increment)
                            mediator.applyNextThemeInHistory()
                        else
                            mediator.rollbackToPreviousThemeInHistory()
                    }
                }
            }

        }

    }

    private inner class UndoRedoLayoutBar : AlwaysVisibleBar() {

        val playButton: Button
        var animationRunning = false

        init {

            mediator.currentDrawing.addListener { _, _, newValue ->
                this.group.children.filterIsInstance<Button>().forEach {
                    it.isDisable = newValue == null
                }
            }

            this.addButton("fas-angle-double-left:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.currentDrawing.get()?.let { currentDrawing ->
                    mediator.rollbackLayoutHistoryToStart()
                }
            }

            this.addButton("fas-angle-left:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.rollbackToPreviousJunctionLayoutInHistory()
            }

            this.playButton = this.addButton("fas-drafting-compass:15", clickable = false)

            this.playButton.background =
                Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
            (this.playButton.graphic as FontIcon).iconColor = Color.BLACK
            val innerShadow = InnerShadow()
            innerShadow.offsetX = 0.0
            innerShadow.offsetY = 0.0
            innerShadow.color = Color.WHITE
            this.playButton.effect = innerShadow

            this.playButton.onMouseClicked = EventHandler<MouseEvent> {
                if (animationRunning) {
                    this.timer.stop()
                    this.playButton.background =
                        Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (this.playButton.graphic as FontIcon).iconColor = Color.BLACK
                    animationRunning = false
                } else {
                    this.timer.start()
                    animationRunning = true
                }
            }


            this.addButton("fas-angle-right:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.applyNextLayoutInHistory()
            }

            this.addButton("fas-angle-double-right:20").onMouseClicked = EventHandler { mouseEvent ->
                mediator.applyLayoutsInHistoryFromNextToEnd()
            }

            this.group.children.filterIsInstance<Button>().forEach {
                it.isDisable = true
            }
        }

        private var timer: AnimationTimer = object : AnimationTimer() {
            var count = 0
            var increment = true

            override fun handle(now: Long) {
                count++
                if (count % 30 == 0) {
                    if (count / 30 % 2 == 0) {
                        playButton.background = Background(
                            BackgroundFill(
                                if (increment) Color.GREEN else Color.ORANGE,
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                            )
                        )
                        (playButton.graphic as Icon).iconColor = Color.WHITE
                    } else {
                        playButton.background =
                            Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                        (playButton.graphic as FontIcon).iconColor = Color.BLACK
                    }

                    mediator.currentDrawing.get()?.let { currentDrawing ->
                        val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
                        if (layoutEl.undoRedoCursor == 0)
                            increment = true
                        else if (layoutEl.undoRedoCursor == layoutEl.historyLength)
                            increment = false
                        if (increment)
                            mediator.applyNextLayoutInHistory()
                        else
                            mediator.rollbackToPreviousJunctionLayoutInHistory()
                    }
                }
            }

        }

    }

    private inner class LateralPanelsBar : AlwaysVisibleBar() {

        init {

            var b = this.addButton("bxs-dock-left:20")
            b.onMouseClicked = EventHandler { mouseEvent ->
                if (leftPanelRemoved) {
                    upperPanel.restoreLeftPanel()
                    leftPanelRemoved = false
                } else {
                    upperPanel.removeLeftPanel()
                    leftPanelRemoved = true
                }
            }

            b = this.addButton("bxs-dock-bottom:20")
            b.onMouseClicked = EventHandler { mouseEvent ->
                bottomPanel?.let {
                    verticalSplitPane.items.add(it.second)
                    verticalSplitPane.setDividerPosition(0, it.first)
                    bottomPanel = null
                } ?: run {
                    bottomPanel =
                        Pair(verticalSplitPane.dividerPositions.first(), verticalSplitPane.items.removeAt(1))
                }
            }

            b = this.addButton("bxs-dock-right:20")
            b.onMouseClicked = EventHandler { mouseEvent ->
                if (rightPanelRemoved) {
                    upperPanel.restoreRightPanel()
                    rightPanelRemoved = false
                } else {
                    upperPanel.removeRightPanel()
                    rightPanelRemoved = true
                }
            }
        }

    }

    init {
        RnartistConfig.load()
        this.mediator = Mediator(this)
        this.root = BorderPane()
        this.verticalSplitPane = SplitPane()
        this.verticalSplitPane.orientation = Orientation.VERTICAL
        this.upperPanel = UpperPanel()
        this.verticalSplitPane.items.add(this.upperPanel)
        this.verticalSplitPane.items.add(LowerPanel())
        this.verticalSplitPane.setDividerPositions(0.7)
        root.center = this.verticalSplitPane
        //root.bottom = BottomBar()
    }

    override fun start(stage: Stage) {
        this.stage = stage
        this.stage.setOnCloseRequest { windowEvent: WindowEvent ->
            val alert =
                Alert(Alert.AlertType.CONFIRMATION)
            alert.initStyle(StageStyle.TRANSPARENT);
            alert.initOwner(stage)
            alert.initModality(Modality.WINDOW_MODAL)
            alert.dialogPane.background =
                Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets.EMPTY))
            alert.title = "Confirm Exit"
            alert.headerText = null
            alert.contentText = "Are you sure to exit RNArtist?"
            val alerttStage = alert.dialogPane.scene.window as Stage
            alerttStage.isAlwaysOnTop = true
            alerttStage.toFront()
            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                try {
                    Platform.exit()
                    save()
                    System.exit(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                windowEvent.consume()
            }
        }

        this.stage.widthProperty()
            .addListener { obs: ObservableValue<out Number?>?, oldVal: Number?, newVal: Number? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
            }

        this.stage.heightProperty()
            .addListener { obs: ObservableValue<out Number?>?, oldVal: Number?, newVal: Number? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
            }

        this.stage.fullScreenProperty()
            .addListener { obs: ObservableValue<out Boolean?>?, oldVal: Boolean?, newVal: Boolean? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
            }

        this.stage.maximizedProperty()
            .addListener { obs: ObservableValue<out Boolean?>?, oldVal: Boolean?, newVal: Boolean? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
            }

        val screen = Screen.getPrimary()

        val scene = Scene(this.root, screen.bounds.width, screen.bounds.height)
        stage.scene = scene
        stage.title = "RNArtist"

        val screenSize = Screen.getPrimary().bounds
        scene.window.width = screenSize.width
        scene.window.height = screenSize.height
        scene.window.x = 0.0
        scene.window.y = 0.0
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        SplashWindow(this.mediator)
    }

    fun main() {
        launch()
    }


}
