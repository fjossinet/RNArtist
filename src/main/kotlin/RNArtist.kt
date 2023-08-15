package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.RnartistConfig.getRnartistRelease
import io.github.fjossinet.rnartist.core.RnartistConfig.save
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.SelectionColorPicker
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.SelectionButtonsPanel
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.SelectionDetailsLevelButtonsPanel
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.*
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.io.*
import java.util.stream.Collectors

class RNArtist : Application() {

    val mediator: Mediator
    lateinit var stage: Stage
    private var scrollCounter = 0
    private val statusBar: FlowPane
    val swingNode = SwingNode()
    private val root: BorderPane
    val junctionSelectionKnob: JunctionKnob

    companion object {
        val RNArtistGUIColor = Color(51.0 / 255.0, 51.0 / 255.0, 51.0 / 255.0, 1.0)
    }


    fun getInstallDir(): String {
        return File(
            this::class.java.protectionDomain.codeSource.location
                .toURI()
        ).parentFile.parent
    }

    init {
        RnartistConfig.load()
        this.mediator = Mediator(this)

        this.root = BorderPane()

        val leftToolBar = VBox()
        leftToolBar.alignment = Pos.TOP_CENTER
        leftToolBar.padding = Insets(10.0, 10.0, 20.0, 10.0)
        leftToolBar.background = Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets(5.0)))
        leftToolBar.effect = DropShadow()

        var l = Label("Secondary Structure")
        l.font = Font(l.font.size+3.0)
        l.padding = Insets(10.0, 0.0, 0.0, 0.0)
        l.textFill = Color.WHITE
        leftToolBar.children.add(l)

        var s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.children.add(s)
        leftToolBar.children.add(Full2DButtonsPanel(mediator))

        /*l = Label("3D Actions")
        l.textFill = Color.WHITE
        l.maxWidth = 180.0
        layoutsPanel.children.add(l)
        s = Separator()
        s.maxWidth = 180.0
        layoutsPanel.children.add(s)
        layoutsPanel.children.add(Actions3DButtonsPanel(mediator))*/

        l = Label("Details Level")
        l.padding = Insets(10.0, 0.0, 0.0, 0.0)
        l.textFill = Color.WHITE
        leftToolBar.children.add(l)
        s = Separator()
        s.id = "sub"
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.children.add(s)
        leftToolBar.children.add(Full2DDetailsLevelButtonsPanel(mediator))

        l = Label("Lines & Colors")
        l.padding = Insets(10.0, 0.0, 0.0, 0.0)
        l.textFill = Color.WHITE
        leftToolBar.children.add(l)
        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.children.add(s)
        leftToolBar.children.add(Full2DColorPicker(mediator))

        l = Label("Junctions Layout")
        l.padding = Insets(10.0, 0.0, 0.0, 0.0)
        l.textFill = Color.WHITE
        //leftToolBar.children.add(l)
        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        //leftToolBar.children.add(s)
        this.junctionSelectionKnob = JunctionKnob("Selection", mediator)
        //leftToolBar.children.add(this.junctionSelectionKnob)

        var leftToolBarScrollPane = ScrollPane(leftToolBar)
        leftToolBarScrollPane.padding = Insets.EMPTY
        leftToolBarScrollPane.isFitToWidth = true
        leftToolBarScrollPane.isFitToHeight = true
        leftToolBarScrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        leftToolBarScrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        val rightToolBar = VBox()
        rightToolBar.alignment = Pos.TOP_CENTER
        rightToolBar.padding = Insets(10.0, 10.0, 20.0, 10.0)
        rightToolBar.background = Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets(5.0)))
        rightToolBar.effect = DropShadow()

        var rightToolBarScrollPane = ScrollPane(rightToolBar)
        rightToolBarScrollPane.padding = Insets.EMPTY
        rightToolBarScrollPane.isFitToWidth = true
        rightToolBarScrollPane.isFitToHeight = true
        rightToolBarScrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        rightToolBarScrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

        l = Label("Selection")
        l.font = Font(l.font.size+3.0)
        l.padding = Insets(10.0, 0.0, 0.0, 0.0)
        l.textFill = Color.WHITE
        rightToolBar.children.add(l)
        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        rightToolBar.children.add(s)
        rightToolBar.children.add(SelectionButtonsPanel(mediator))

        l = Label("Details Level")
        l.padding = Insets(10.0, 0.0, 0.0, 0.0)
        l.textFill = Color.WHITE
        rightToolBar.children.add(l)
        s = Separator()
        s.id = "sub"
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        rightToolBar.children.add(s)
        rightToolBar.children.add(SelectionDetailsLevelButtonsPanel(mediator))

        l = Label("Lines & Colors")
        l.padding = Insets(10.0, 0.0, 0.0, 0.0)
        l.textFill = Color.WHITE
        rightToolBar.children.add(l)
        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        rightToolBar.children.add(s)
        rightToolBar.children.add(SelectionColorPicker(mediator))

        val topToolBar2D = HBox()
        topToolBar2D.background =
            Background(BackgroundFill(RNArtistGUIColor, CornerRadii(0.0, 0.0, 10.0, 10.0, false), Insets.EMPTY))
        topToolBar2D.alignment = Pos.CENTER
        topToolBar2D.padding = Insets(10.0)
        topToolBar2D.spacing = 25.0

        l = Label("Font")
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
        topToolBar2D.children.add(g)

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
        topToolBar2D.children.add(g)

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
        topToolBar2D.children.add(g)

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
        topToolBar2D.children.add(g)

        //++++++ Canvas2D
        swingNode.onMouseMoved = EventHandler { mouseEvent: MouseEvent? ->
            mediator.currentDrawing.get()?.drawing?.let {
                it.quickDraw = false //a trick if after the scroll event the quickdraw is still true
            }
        }
        swingNode.onMouseClicked = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.PRIMARY) {
                mediator.currentDrawing.get()?.let mouseClicked@{ drawingLoaded ->
                    val at = AffineTransform()
                    at.translate(
                        drawingLoaded.drawing.workingSession.viewX,
                        drawingLoaded.drawing.workingSession.viewY
                    )
                    at.scale(
                        drawingLoaded.drawing.workingSession.zoomLevel,
                        drawingLoaded.drawing.workingSession.zoomLevel
                    )
                    for (h in drawingLoaded.drawing.workingSession.helicesDrawn) {
                        var shape = h.selectionFrame
                        if (shape != null && at.createTransformedShape(shape)
                                .contains(mouseEvent.x, mouseEvent.y)
                        ) {
                            for (r in h.residues) {
                                shape = r.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
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
                            for (interaction in h.secondaryInteractions) {
                                shape = interaction.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
                                        .contains(mouseEvent.x, mouseEvent.y)
                                ) {
                                    if (!mediator.canvas2D.isSelected(interaction) && !mediator.canvas2D.isSelected(
                                            interaction.parent
                                        )
                                    ) {
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
                    for (j in drawingLoaded.drawing.workingSession.junctionsDrawn) {
                        var shape = j.selectionFrame
                        if (shape != null && at.createTransformedShape(shape)
                                .contains(mouseEvent.x, mouseEvent.y)
                        ) {
                            for (r in j.residues) {
                                shape = r.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
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
                    for (ss in drawingLoaded.drawing.workingSession.singleStrandsDrawn) {
                        var shape = ss.selectionFrame
                        if (shape != null && at.createTransformedShape(shape)
                                .contains(mouseEvent.x, mouseEvent.y)
                        ) {
                            for (r in ss.residues) {
                                shape = r.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
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
                mediator.currentDrawing.get()?.drawing?.let {
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
                mediator.currentDrawing.get()?.drawing?.let {
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
        swingNode.onScroll = EventHandler { scrollEvent: ScrollEvent ->
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
        }
        createSwingContent(swingNode)

        val verticalSplitPane = SplitPane()
        verticalSplitPane.orientation = Orientation.VERTICAL

        root.center = verticalSplitPane

        val upperPanel = GridPane()

        val rowConstraint = RowConstraints()
        rowConstraint.vgrow = Priority.ALWAYS

        val cConstraints1 = ColumnConstraints()
        cConstraints1.hgrow = Priority.NEVER
        val cConstraints2 = ColumnConstraints()
        cConstraints2.hgrow = Priority.ALWAYS
        val cConstraints3 = ColumnConstraints()
        cConstraints3.hgrow = Priority.NEVER

        upperPanel.columnConstraints.addAll(cConstraints1, cConstraints2, cConstraints3)
        upperPanel.rowConstraints.add(rowConstraint)

        val vBox = VBox()
        VBox.setVgrow(swingNode, Priority.ALWAYS)
        vBox.padding = Insets(10.0, 10.0, 20.0, 10.0)
        vBox.background = Background(BackgroundFill(Color.WHITE, CornerRadii(10.0), Insets(5.0)))
        vBox.effect = DropShadow()
        vBox.children.add(swingNode)
        upperPanel.add(leftToolBarScrollPane, 0, 0)
        leftToolBarScrollPane.isFitToWidth = true
        upperPanel.add(vBox, 1, 0)
        upperPanel.add(rightToolBarScrollPane, 2, 0)
        rightToolBarScrollPane.isFitToWidth = true

        val lowerHorizontalSplitPane = SplitPane()
        lowerHorizontalSplitPane.orientation = Orientation.HORIZONTAL
        val lowerPanel = TabPane()
        lowerPanel.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        lowerPanel.padding = Insets(10.0, 10.0, 20.0, 10.0)
        lowerPanel.background = Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets(5.0)))
        lowerPanel.effect = DropShadow()

        lowerPanel.tabs.add(Tab("Database", mediator.DBExplorer))

        verticalSplitPane.items.add(upperPanel)
        verticalSplitPane.items.add(lowerPanel)
        verticalSplitPane.setDividerPositions(0.7)

        //### Status Bar
        this.statusBar = FlowPane()
        root.bottom = this.statusBar
        statusBar.setAlignment(Pos.CENTER_RIGHT)
        statusBar.setPadding(Insets(5.0, 10.0, 5.0, 10.0))
        statusBar.setHgap(20.0)

        val release = Label(getRnartistRelease())
        statusBar.getChildren().add(release)

        val shutdown = Button(null, FontIcon("fas-power-off:15"))
        shutdown.tooltip = Tooltip("Exit RNArtist")
        shutdown.onAction = EventHandler { actionEvent: ActionEvent ->
            val alert =
                Alert(Alert.AlertType.CONFIRMATION)
            alert.initOwner(stage)
            alert.initModality(Modality.WINDOW_MODAL)
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
                actionEvent.consume()
            }
        }
        statusBar.getChildren().add(shutdown)

        val windowsBar = FlowPane()
        windowsBar.alignment = Pos.CENTER_LEFT
        windowsBar.padding = Insets(5.0, 10.0, 5.0, 10.0)
        windowsBar.hgap = 10.0

        val bar = GridPane()
        var cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        bar.columnConstraints.addAll(cc, ColumnConstraints())

        bar.add(windowsBar, 0, 0)
        GridPane.setFillWidth(windowsBar, true)
        GridPane.setHalignment(windowsBar, HPos.LEFT)
        bar.add(this.statusBar, 1, 0)
        GridPane.setHalignment(this.statusBar, HPos.RIGHT)

    }

    override fun start(stage: Stage) {
        this.stage = stage
        this.stage.setOnCloseRequest(EventHandler { windowEvent: WindowEvent ->
            val alert =
                Alert(Alert.AlertType.CONFIRMATION)
            alert.initOwner(stage)
            alert.initModality(Modality.WINDOW_MODAL)
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
        })

        this.stage.widthProperty().addListener { obs: ObservableValue<out Number?>?, oldVal: Number?, newVal: Number? ->
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

    private fun createSwingContent(swingNode: SwingNode) {
        Platform.runLater {
            val canvas = Canvas2D(mediator)
            swingNode.content = canvas
            swingNode.isCache = true
            swingNode.cacheHint = CacheHint.SPEED
        }
    }

    fun main() {
        launch();
    }


}
