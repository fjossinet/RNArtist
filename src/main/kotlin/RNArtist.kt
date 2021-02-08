package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.RnartistConfig.getRnartistRelease
import io.github.fjossinet.rnartist.core.model.RnartistConfig.isAssemble2DockerImageInstalled
import io.github.fjossinet.rnartist.core.model.RnartistConfig.isDockerInstalled
import io.github.fjossinet.rnartist.core.model.RnartistConfig.load
import io.github.fjossinet.rnartist.core.model.RnartistConfig.save
import io.github.fjossinet.rnartist.core.model.RnartistConfig.selectionWidth
import io.github.fjossinet.rnartist.core.model.io.*
import io.github.fjossinet.rnartist.core.rnartist
import io.github.fjossinet.rnartist.gui.Canvas2D
import io.github.fjossinet.rnartist.gui.SplashWindow
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.Explorer
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.Explorer.DrawingElementFilter
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.model.DrawingLoaded
import io.github.fjossinet.rnartist.model.DrawingLoadedFromFile
import io.github.fjossinet.rnartist.model.DrawingLoadedFromRNArtistDB
import io.github.fjossinet.rnartist.model.ExplorerItem
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.concurrent.Task
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.stage.*
import javafx.util.Duration
import org.apache.commons.lang3.tuple.Pair
import org.dizitart.no2.NitriteId
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.io.*
import java.util.*
import java.util.concurrent.ExecutionException
import javax.swing.SwingUtilities

class RNArtist: Application() {
    enum class SCOPE {
        ELEMENT,  STRUCTURAL_DOMAIN, BRANCH
    }
    val mediator: Mediator
    lateinit var stage: Stage
    private var scrollCounter = 0
    private val statusBar: FlowPane
    val allStructuresAvailable: MenuButton
    val clearAll2DsItem:MenuItem
    val clearAll2DsExceptCurrentItem:MenuItem
    val saveProject: Button
    val focus3D:Button
    val reload3D:Button
    val paintSelectionin3D:Button
    val paintSelectionAsCartoon:Button
    val paintSelectionAsStick:Button
    val showRibbon:Button
    val hideRibbon:Button
    private val root:BorderPane
    var centerDisplayOnSelection = false

    init {
        load()
        this.mediator = Mediator(this)
        this.root = BorderPane()

        //++++++ top Toolbar
        val toolbar = ToolBar()
        toolbar.padding = Insets(5.0, 5.0, 5.0, 5.0)

        val loadFiles = GridPane()
        loadFiles.vgap = 5.0
        loadFiles.hgap = 5.0

        var l = Label("Load")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0, 2, 1)
        loadFiles.children.add(l)

        val loadData = Button(null, FontIcon("fas-sign-in-alt:15"))
        loadData.onMouseClicked = EventHandler {
            val fileChooser = FileChooser()
            val files = fileChooser.showOpenMultipleDialog(stage)
            if (files != null) {
                for (f in files) {
                    fileChooser.initialDirectory = f.parentFile
                    val loadData =
                        object : Task<Pair<Pair<List<SecondaryStructureDrawing>, File>?, Exception?>?>() {
                            override fun call(): Pair<Pair<List<SecondaryStructureDrawing>, File>?, Exception?>? {
                                var ss: SecondaryStructure?
                                val secondaryStructureDrawings: MutableList<SecondaryStructureDrawing> = ArrayList()
                                try {
                                    val source = "file:" + f.absolutePath
                                    if (f.name.endsWith(".json")) {
                                        val drawing = parseJSON(FileReader(f))
                                        if (drawing != null) {
                                            drawing.secondaryStructure.rna.source = source
                                            drawing.secondaryStructure.source = source
                                            secondaryStructureDrawings.add(drawing)
                                        }
                                    }
                                    if (f.name.endsWith(".ct")) {
                                        val drawing = rnartist {
                                            ss {
                                                ct {
                                                    file = f.absolutePath
                                                }
                                            }
                                        }
                                        drawing?.let {
                                            it.secondaryStructure.source = source
                                            it.secondaryStructure.rna.source = source
                                            secondaryStructureDrawings.add(drawing)
                                        }
                                    } else if (f.name.endsWith(".bpseq")) {
                                        val drawing = rnartist {
                                            ss {
                                                bpseq {
                                                    file = f.absolutePath
                                                }
                                            }
                                        }
                                        drawing?.let {
                                            it.secondaryStructure.source = source
                                            it.secondaryStructure.rna.source = source
                                            secondaryStructureDrawings.add(drawing)
                                        }
                                    } else if (f.name.endsWith(".fasta") || f.name.endsWith(".fas") || f.name.endsWith(
                                            ".fa") || f.name.endsWith(".vienna")
                                    ) {
                                        val drawing = rnartist {
                                            ss {
                                                vienna {
                                                    file = f.absolutePath
                                                }
                                            }
                                        }
                                        drawing?.let {
                                            it.secondaryStructure.source = source
                                            it.secondaryStructure.rna.source = source
                                            secondaryStructureDrawings.add(drawing)
                                        }
                                    } else if (f.name.endsWith(".xml") || f.name.endsWith(".rnaml")) {
                                        for (structure in parseRnaml(f)) {
                                            if (!structure.helices.isEmpty()) {
                                                structure.rna.source = source
                                                structure.source = source
                                                secondaryStructureDrawings.add(SecondaryStructureDrawing(structure,
                                                    WorkingSession()))
                                            }
                                        }
                                    } else if (f.name.matches(Regex(".+\\.pdb[0-9]?"))) {
                                        if (!(isDockerInstalled() && isAssemble2DockerImageInstalled())) {
                                            throw Exception("You cannot use PDB loadFiles, it seems that RNArtist cannot find the RNAVIEW algorithm on your computer.\n Possible causes:\n- the tool Docker is not installed\n- the tool Docker is not running\n- the docker image fjossinet/assemble2 is not installed")
                                        }
                                        for (structure in Rnaview().annotate(f)) {
                                            if (!structure.helices.isEmpty()) {
                                                structure.rna.source = source
                                                secondaryStructureDrawings.add(SecondaryStructureDrawing(structure,
                                                    WorkingSession()))
                                            }
                                        }
                                    } else if (f.name.endsWith(".stk") || f.name.endsWith(".stockholm")) {
                                        for (structure in parseStockholm(FileReader(f), false)) {
                                            structure.rna.source = source
                                            structure.source = source
                                            secondaryStructureDrawings.add(SecondaryStructureDrawing(structure,
                                                WorkingSession()))
                                        }
                                    }
                                } catch (e: Exception) {
                                    return Pair.of(Pair.of(secondaryStructureDrawings, f), e)
                                }
                                return Pair.of(Pair.of(secondaryStructureDrawings, f), null)
                            }
                        }
                    loadData.onSucceeded = EventHandler {
                        try {
                            loadData.get()?.right?.let {  exception ->
                                val alert = Alert(Alert.AlertType.ERROR)
                                alert.title = "File Parsing error"
                                alert.headerText = exception.message
                                alert.contentText =
                                    "If this problem persists, you can send the exception stacktrace below to fjossinet@gmail.com"
                                val sw = StringWriter()
                                val pw = PrintWriter(sw)
                                exception.printStackTrace(pw)
                                val exceptionText = sw.toString()
                                val label = Label("The exception stacktrace was:")
                                val textArea = TextArea(exceptionText)
                                textArea.isEditable = false
                                textArea.isWrapText = true
                                textArea.maxWidth = Double.MAX_VALUE
                                textArea.maxHeight = Double.MAX_VALUE
                                GridPane.setVgrow(textArea, Priority.ALWAYS)
                                GridPane.setHgrow(textArea, Priority.ALWAYS)
                                val expContent = GridPane()
                                expContent.maxWidth = Double.MAX_VALUE
                                expContent.add(label, 0, 0)
                                expContent.add(textArea, 0, 1)
                                alert.dialogPane.expandableContent = expContent
                                alert.showAndWait()
                            }
                            loadData.get()?.left?.let {  result ->
                                for (drawing in result.left) mediator.drawingsLoaded.add(
                                    DrawingLoadedFromFile(mediator,
                                        drawing, result.right))
                                //we load and fit (only if not a drawing from a JSON file) on the last 2D loaded
                                mediator.drawingDisplayed.set(mediator.drawingsLoaded[mediator.drawingsLoaded.size - 1])
                                if (mediator.viewX == 0.0 && mediator.viewY == 0.0 && mediator.zoomLevel == 1.0) //this test allows to detect JSON loadFiles exported from RNArtist with a focus on a region
                                    mediator.canvas2D.fitStructure(null)
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        }
                    }
                    Thread(loadData).start()
                }
            }
        }
        loadData.tooltip = Tooltip("Load 2D from file")
        GridPane.setConstraints(loadData, 0, 1)
        loadFiles.children.add(loadData)

        val loadProject = Button(null, FontIcon("fas-grip-horizontal:15"))
        loadProject.onMouseClicked = EventHandler {
            mediator.projectsPanel.stage.show()
            mediator.projectsPanel.stage.toFront()
        }
        loadProject.tooltip = Tooltip("Load Project")
        GridPane.setConstraints(loadProject, 1, 1)
        loadFiles.children.add(loadProject)

        val saveFiles = GridPane()
        saveFiles.vgap = 5.0
        saveFiles.hgap = 5.0

        l = Label("Save")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0, 3, 1)
        saveFiles.children.add(l)

        val saveProjectAs = Button(null, FontIcon("fas-database:15"))
        saveProjectAs.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        saveProjectAs.onMouseClicked = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                drawing.workingSession.is_screen_capture = true
                drawing.workingSession.screen_capture_area =
                    Rectangle2D.Double(mediator.canvas2D.getBounds().getCenterX() - 200,
                        mediator.canvas2D.getBounds().getCenterY() - 100,
                        400.0,
                        200.0)
                mediator.canvas2D.repaint()
                val dialog = TextInputDialog("My Project")
                dialog.initModality(Modality.NONE)
                dialog.title = "Project Saving"
                dialog.headerText =
                    "Keep right mouse button pressed and drag the rectangle to define your project icon."
                dialog.contentText = "Project name:"
                val projectName = dialog.showAndWait()
                if (projectName.isPresent) {
                    try {
                        mediator.projectsPanel.saveProjectAs(projectName.get().trim { it <= ' ' },
                            mediator.canvas2D.screenCapture()!!)?.let { id ->
                            drawing.workingSession.is_screen_capture = false
                            drawing.workingSession.screen_capture_area = null
                            mediator.embeddedDB.getProject(id)
                                ?.let { newDrawing -> //we reload it from the DB to have a copy of the drawing and not the same object
                                    mediator.drawingsLoaded.add(0,
                                        DrawingLoadedFromRNArtistDB(mediator,
                                            newDrawing,
                                            id,
                                            projectName.get().trim { it <= ' ' }))
                                    mediator.drawingDisplayed.set(mediator.drawingsLoaded[0])
                                }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    drawing.workingSession.is_screen_capture = false
                    drawing.workingSession.screen_capture_area = null
                    mediator.canvas2D.repaint()
                }
            }
        }
        saveProjectAs.tooltip = Tooltip("Save Project As...")
        GridPane.setConstraints(saveProjectAs, 0, 1)
        saveFiles.children.add(saveProjectAs)

        this.saveProject = Button(null, FontIcon("fas-sync:15"))
        this.saveProject.setDisable(true)
        this.saveProject.setOnMouseClicked(EventHandler<MouseEvent?> {
            try {
                mediator.projectsPanel.saveProject()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        this.saveProject.setTooltip(Tooltip("Update Project in DB"))
        GridPane.setConstraints(saveProject, 1, 1)
        saveFiles.children.add(saveProject)

        val export2D = Button(null, FontIcon("fas-sign-out-alt:15"))
        export2D.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        export2D.onMouseClicked = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                val fileChooser = FileChooser()
                fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("SVG Files", "*.svg"))
                fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("JSON Files", "*.json"))
                val file = fileChooser.showSaveDialog(stage)
                if (file != null) {
                    if (file.name.endsWith(".svg")) {
                        fileChooser.initialDirectory = file.parentFile
                        val writer: PrintWriter
                        try {
                            writer = PrintWriter(file)
                            writer.println(toSVG(drawing,
                                mediator.canvas2D.getBounds().width.toDouble(),
                                mediator.canvas2D.getBounds().height.toDouble()))
                            writer.close()
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                    } else if (file.name.endsWith(".json")) {
                        fileChooser.initialDirectory = file.parentFile
                        val writer: PrintWriter
                        try {
                            writer = PrintWriter(file)
                            writer.println(toJSON(drawing))
                            writer.close()
                            mediator.chimeraDriver.saveSession(File(file.parentFile,
                                file.name.split(".".toRegex()).toTypedArray()[0] + ".py"),
                                File(file.parentFile, file.name.split(".".toRegex()).toTypedArray()[0] + ".pdb"))
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        export2D.tooltip = Tooltip("Export 2D to file")
        GridPane.setConstraints(export2D, 2, 1)
        saveFiles.children.add(export2D)

        val tertiaryStructureButtons = GridPane()
        tertiaryStructureButtons.vgap = 5.0
        tertiaryStructureButtons.hgap = 5.0

        val selectionSize = GridPane()
        selectionSize.vgap = 5.0
        selectionSize.hgap = 5.0

        l = Label("Selection Width (px)")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        selectionSize.children.add(l)

        val sliderSize = Slider(1.0, 3.0, selectionWidth.toDouble())
        sliderSize.isShowTickLabels = true
        sliderSize.isShowTickMarks = true
        sliderSize.isSnapToTicks = true
        sliderSize.majorTickUnit = 1.0
        sliderSize.minorTickCount = 0

        sliderSize.onMouseReleased = EventHandler {
            selectionWidth = sliderSize.value.toInt()
            mediator.canvas2D.repaint()
        }
        GridPane.setHalignment(sliderSize, HPos.CENTER)
        GridPane.setConstraints(sliderSize, 0, 1)
        selectionSize.children.add(sliderSize)

        val selectionColor = GridPane()
        selectionColor.vgap = 5.0
        selectionColor.hgap = 5.0

        l = Label("Selection Color")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        selectionColor.children.add(l)

        val colorPicker = ColorPicker()
        colorPicker.value = awtColorToJavaFX(RnartistConfig.selectionColor)
        colorPicker.valueProperty().addListener { observableValue, color, newValue ->
            val c = javaFXToAwt(colorPicker.value)
            RnartistConfig.selectionColor = c
            mediator.canvas2D.repaint()
        }
        colorPicker.maxWidth = Double.MAX_VALUE
        colorPicker.minWidth = Control.USE_PREF_SIZE
        GridPane.setConstraints(colorPicker, 0, 1)
        selectionColor.children.add(colorPicker)

        val structureSelection = GridPane()
        structureSelection.vgap = 5.0
        structureSelection.hgap = 5.0

        l = Label("2Ds Available")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        structureSelection.children.add(l)

        this.allStructuresAvailable = MenuButton("Choose a 2D")

        this.clearAll2DsItem = MenuItem("Clear All")
        this.clearAll2DsItem.setDisable(true)
        clearAll2DsItem.setOnAction(EventHandler<ActionEvent?> {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Confirmation Dialog"
            alert.headerText = null
            alert.contentText = "Are you Sure to Remove all the 2Ds from your Project?"
            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                mediator.drawingsLoaded.clear()
                stage.title = "RNArtist"
            }
        })

        this.clearAll2DsExceptCurrentItem = MenuItem("Clear All Except Current")
        this.clearAll2DsExceptCurrentItem.setDisable(true)
        clearAll2DsExceptCurrentItem.setOnAction(EventHandler<ActionEvent?> {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Confirmation Dialog"
            alert.headerText = null
            alert.contentText = "Are you Sure to Remove all the Remaining 2Ds from your Project?"
            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                val toDelete: MutableList<DrawingLoaded> = ArrayList()
                for (drawingLoaded in mediator.drawingsLoaded) if (drawingLoaded.drawing != mediator.drawingDisplayed.get()?.drawing) toDelete.add(
                    drawingLoaded)
                mediator.drawingsLoaded.removeAll(toDelete)
            }
        })

        allStructuresAvailable.getItems().addAll(SeparatorMenuItem(), clearAll2DsItem, clearAll2DsExceptCurrentItem)

        GridPane.setHalignment(allStructuresAvailable, HPos.CENTER)
        GridPane.setConstraints(allStructuresAvailable, 0, 1)
        structureSelection.children.add(allStructuresAvailable)

        val s1 = Separator()
        s1.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s2 = Separator()
        s2.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s3 = Separator()
        s2.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s4 = Separator()
        s2.padding = Insets(0.0, 5.0, 0.0, 5.0)

        toolbar.items.addAll(loadFiles,
            s1,
            saveFiles,
            s2,
            structureSelection,
            s3,
            tertiaryStructureButtons,
            s4,
            selectionSize,
            selectionColor)

        root.top = toolbar

        //++++++ left Toolbar
        val leftToolBar = GridPane()
        leftToolBar.alignment = Pos.TOP_CENTER
        leftToolBar.padding = Insets(5.0)
        leftToolBar.vgap = 5.0
        leftToolBar.hgap = 5.0

        var row = 0

        var s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        l = Label("2D")
        leftToolBar.add(l, 0, row++, 2, 1)
        GridPane.setHalignment(l, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val center2D = Button(null, FontIcon("fas-crosshairs:15"))
        center2D.maxWidth = Double.MAX_VALUE
        center2D.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        center2D.onMouseClicked = EventHandler { mouseEvent ->
            if (mouseEvent.isShiftDown) {
                centerDisplayOnSelection = !centerDisplayOnSelection
                if (centerDisplayOnSelection) center2D.graphic = FontIcon("fas-lock:15") else center2D.graphic =
                    FontIcon("fas-crosshairs:15")
            } else {
                val selectionFrame: Rectangle2D? = mediator.canvas2D.getSelectionFrame()
                mediator.drawingDisplayed.get()?.drawing?.let {
                    mediator.canvas2D.centerDisplayOn(selectionFrame ?: it.getFrame())
                }
            }
        }
        center2D.tooltip = Tooltip("Focus 2D on Selection")

        val fit2D = Button(null, FontIcon("fas-expand-arrows-alt:15"))
        fit2D.maxWidth = Double.MAX_VALUE
        fit2D.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        fit2D.onMouseClicked = EventHandler { mediator.canvas2D.fitStructure(mediator.canvas2D.getSelectionFrame()) }
        fit2D.tooltip = Tooltip("Fit 2D")

        leftToolBar.add(center2D, 0, row)
        GridPane.setHalignment(center2D, HPos.CENTER)
        leftToolBar.add(fit2D, 1, row++)
        GridPane.setHalignment(fit2D, HPos.CENTER)

        val showTertiaries = Button(null, FontIcon("fas-eye:15"))
        showTertiaries.maxWidth = Double.MAX_VALUE
        showTertiaries.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        showTertiaries.onMouseClicked = EventHandler {
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "true")
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                for (selectedItem in mediator.explorer.treeTableView.selectionModel.selectedItems) {
                    if (ResidueDrawing::class.java.isInstance(selectedItem.value.drawingElement)) starts.add(
                        selectedItem.parent) //if the user has selected single residues, this allows to display its tertiary interactions, since a tertiary can be a parent of a residue in the explorer
                    starts.add(selectedItem)
                }
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        showTertiaries.tooltip = Tooltip("Show Tertiaries")

        val hideTertiaries = Button(null, FontIcon("fas-eye-slash:15"))
        hideTertiaries.maxWidth = Double.MAX_VALUE
        hideTertiaries.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        hideTertiaries.onMouseClicked = EventHandler {
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "false")
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        hideTertiaries.tooltip = Tooltip("Hide Tertiaries")

        leftToolBar.add(showTertiaries, 0, row)
        GridPane.setHalignment(showTertiaries, HPos.CENTER)
        leftToolBar.add(hideTertiaries, 1, row++)
        GridPane.setHalignment(hideTertiaries, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val levelDetails1 = Button("1")
        levelDetails1.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails1.maxWidth = Double.MAX_VALUE
        levelDetails1.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "false")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand,
                DrawingConfigurationParameter.fulldetails,
                "false")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.fulldetails,
                "false")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.fulldetails,
                "false")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        val levelDetails2 = Button("2")
        levelDetails2.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails2.maxWidth = Double.MAX_VALUE
        levelDetails2.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.fulldetails,
                "false")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        leftToolBar.add(levelDetails1, 0, row)
        GridPane.setHalignment(levelDetails1, HPos.CENTER)
        leftToolBar.add(levelDetails2, 1, row++)
        GridPane.setHalignment(levelDetails2, HPos.CENTER)

        val levelDetails3 = Button("3")
        levelDetails3.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails3.maxWidth = Double.MAX_VALUE
        levelDetails3.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "false")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.fulldetails,
                "false")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        val levelDetails4 = Button("4")
        levelDetails4.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails4.maxWidth = Double.MAX_VALUE
        levelDetails4.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.fulldetails,
                "false")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        leftToolBar.add(levelDetails3, 0, row)
        GridPane.setHalignment(levelDetails1, HPos.CENTER)
        leftToolBar.add(levelDetails4, 1, row++)
        GridPane.setHalignment(levelDetails2, HPos.CENTER)

        val levelDetails5 = Button("5")
        levelDetails5.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails5.maxWidth = Double.MAX_VALUE
        levelDetails5.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.fulldetails,
                "true")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "true")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.fulldetails,
                "true")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        leftToolBar.add(levelDetails5, 0, row++)
        GridPane.setHalignment(levelDetails5, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val syncColors = Button(null, FontIcon("fas-unlock:12"))

        val AColorPicker = ColorPicker()
        AColorPicker.maxWidth = Double.MAX_VALUE
        val UColorPicker = ColorPicker()
        UColorPicker.maxWidth = Double.MAX_VALUE
        val GColorPicker = ColorPicker()
        GColorPicker.maxWidth = Double.MAX_VALUE
        val CColorPicker = ColorPicker()
        CColorPicker.maxWidth = Double.MAX_VALUE

        val ALabel = Button("A")
        ALabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        ALabel.maxWidth = Double.MAX_VALUE
        ALabel.userData = "white"
        ALabel.textFill = Color.WHITE
        val ULabel = Button("U")
        ULabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        ULabel.maxWidth = Double.MAX_VALUE
        ULabel.userData = "white"
        ULabel.textFill = Color.WHITE
        val GLabel = Button("G")
        GLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        GLabel.userData = "white"
        GLabel.textFill = Color.WHITE
        GLabel.maxWidth = Double.MAX_VALUE
        val CLabel = Button("C")
        CLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        CLabel.maxWidth = Double.MAX_VALUE
        CLabel.userData = "white"
        CLabel.textFill = Color.WHITE

        val pickColorScheme = Button(null, FontIcon("fas-swatchbook:15"))
        pickColorScheme.maxWidth = Double.MAX_VALUE
        pickColorScheme.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))

        val paintResidues = Button(null, FontIcon("fas-fill:15"))
        paintResidues.maxWidth = Double.MAX_VALUE
        paintResidues.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        paintResidues.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.A,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.AShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(AColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.U,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.UShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(UColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.G,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.GShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(GColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.C,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.CShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        leftToolBar.add(pickColorScheme, 0, row)
        GridPane.setHalignment(pickColorScheme, HPos.CENTER)
        leftToolBar.add(paintResidues, 1, row++)
        GridPane.setHalignment(paintResidues, HPos.CENTER)

        ALabel.onAction = EventHandler {
            if (ALabel.userData == "black") {
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
                if ("lock" == syncColors.userData) {
                    ULabel.userData = "white"
                    ULabel.textFill = Color.WHITE
                    GLabel.userData = "white"
                    GLabel.textFill = Color.WHITE
                    CLabel.userData = "white"
                    CLabel.textFill = Color.WHITE
                }
            } else {
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
                if ("lock" == syncColors.userData) {
                    ULabel.userData = "black"
                    ULabel.textFill = Color.BLACK
                    GLabel.userData = "black"
                    GLabel.textFill = Color.BLACK
                    CLabel.userData = "black"
                    CLabel.textFill = Color.BLACK
                }
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.A,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.AShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(AColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.U,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.UShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(UColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.G,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.GShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(GColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.C,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.CShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        leftToolBar.add(ALabel, 0, row)
        GridPane.setHalignment(ALabel, HPos.CENTER)
        leftToolBar.add(AColorPicker, 1, row++)
        GridPane.setHalignment(AColorPicker, HPos.CENTER)

        AColorPicker.styleClass.add("button")
        AColorPicker.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        AColorPicker.style = "-fx-color-label-visible: false ;"
        AColorPicker.onAction = EventHandler {
            ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(AColorPicker.value))
            if ("lock" == syncColors.userData) {
                GColorPicker.value = AColorPicker.value
                GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(AColorPicker.value))
                UColorPicker.value = AColorPicker.value
                ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(AColorPicker.value))
                CColorPicker.value = AColorPicker.value
                CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(AColorPicker.value))
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.A,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.AShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(AColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.G,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.GShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(GColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.U,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.UShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(UColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.C,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.CShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        ULabel.onAction = EventHandler {
            if (ULabel.userData == "black") {
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
                if ("lock" == syncColors.userData) {
                    ALabel.userData = "white"
                    ALabel.textFill = Color.WHITE
                    GLabel.userData = "white"
                    GLabel.textFill = Color.WHITE
                    CLabel.userData = "white"
                    CLabel.textFill = Color.WHITE
                }
            } else {
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
                if ("lock" == syncColors.userData) {
                    ALabel.userData = "black"
                    ALabel.textFill = Color.BLACK
                    GLabel.userData = "black"
                    GLabel.textFill = Color.BLACK
                    CLabel.userData = "black"
                    CLabel.textFill = Color.BLACK
                }
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.U,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.UShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(UColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.A,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.AShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(AColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.G,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.GShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(GColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.C,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.CShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        leftToolBar.add(ULabel, 0, row)
        GridPane.setHalignment(ULabel, HPos.CENTER)
        leftToolBar.add(UColorPicker, 1, row++)
        GridPane.setHalignment(UColorPicker, HPos.CENTER)

        UColorPicker.styleClass.add("button")
        UColorPicker.style = "-fx-color-label-visible: false ;"
        UColorPicker.onAction = EventHandler {
            ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(UColorPicker.value))
            if ("lock" == syncColors.userData) {
                AColorPicker.value = UColorPicker.value
                ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(UColorPicker.value))
                GColorPicker.value = UColorPicker.value
                GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(UColorPicker.value))
                CColorPicker.value = UColorPicker.value
                CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(UColorPicker.value))
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.U,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.UShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(UColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.A,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.AShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(AColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.G,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.GShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(GColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.C,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.CShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        GLabel.onAction = EventHandler {
            if (GLabel.userData == "black") {
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
                if ("lock" == syncColors.userData) {
                    ULabel.userData = "white"
                    ULabel.textFill = Color.WHITE
                    ALabel.userData = "white"
                    ALabel.textFill = Color.WHITE
                    CLabel.userData = "white"
                    CLabel.textFill = Color.WHITE
                }
            } else {
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
                if ("lock" == syncColors.userData) {
                    ULabel.userData = "black"
                    ULabel.textFill = Color.BLACK
                    ALabel.userData = "black"
                    ALabel.textFill = Color.BLACK
                    CLabel.userData = "black"
                    CLabel.textFill = Color.BLACK
                }
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.G,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.GShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(GColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.U,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.UShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(UColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.A,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.AShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(AColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.C,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.CShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        leftToolBar.add(GLabel, 0, row)
        GridPane.setHalignment(GLabel, HPos.CENTER)
        leftToolBar.add(GColorPicker, 1, row++)
        GridPane.setHalignment(GColorPicker, HPos.CENTER)

        GColorPicker.styleClass.add("button")
        GColorPicker.style = "-fx-color-label-visible: false ;"
        GColorPicker.onAction = EventHandler {
            GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(GColorPicker.value))
            if ("lock" == syncColors.userData) {
                AColorPicker.value = GColorPicker.value
                ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(GColorPicker.value))
                UColorPicker.value = GColorPicker.value
                ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(GColorPicker.value))
                CColorPicker.value = GColorPicker.value
                CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(GColorPicker.value))
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.G,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.GShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(GColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.A,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.AShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(AColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.U,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.UShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(UColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.C,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.CShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        CLabel.onAction = EventHandler {
            if (CLabel.userData == "black") {
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
                if ("lock" == syncColors.userData) {
                    ULabel.userData = "white"
                    ULabel.textFill = Color.WHITE
                    GLabel.userData = "white"
                    GLabel.textFill = Color.WHITE
                    ALabel.userData = "white"
                    ALabel.textFill = Color.WHITE
                }
            } else {
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
                if ("lock" == syncColors.userData) {
                    ULabel.userData = "black"
                    ULabel.textFill = Color.BLACK
                    GLabel.userData = "black"
                    GLabel.textFill = Color.BLACK
                    ALabel.userData = "black"
                    ALabel.textFill = Color.BLACK
                }
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.C,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.CShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.U,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.UShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(UColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.G,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.GShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(GColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.A,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.AShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(AColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        leftToolBar.add(CLabel, 0, row)
        GridPane.setHalignment(CLabel, HPos.CENTER)
        leftToolBar.add(CColorPicker, 1, row++)
        GridPane.setHalignment(CColorPicker, HPos.CENTER)

        CColorPicker.styleClass.add("button")
        CColorPicker.style = "-fx-color-label-visible: false ;"
        CColorPicker.onAction = EventHandler {
            CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(CColorPicker.value))
            if ("lock" == syncColors.userData) {
                AColorPicker.value = CColorPicker.value
                ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(CColorPicker.value))
                UColorPicker.value = CColorPicker.value
                ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(CColorPicker.value))
                GColorPicker.value = CColorPicker.value
                GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(CColorPicker.value))
            }
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.C,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE)))
            t.setConfigurationFor(SecondaryStructureType.CShape,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            if ("lock" == syncColors.userData) {
                t.setConfigurationFor(SecondaryStructureType.A,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.AShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(AColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.U,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.UShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(UColorPicker.value)))
                t.setConfigurationFor(SecondaryStructureType.G,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE)))
                t.setConfigurationFor(SecondaryStructureType.GShape,
                    DrawingConfigurationParameter.color,
                    getHTMLColorString(javaFXToAwt(GColorPicker.value)))
            }
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        //++++++++++ we init the color buttons with a random scheme
        val scheme = RnartistConfig.colorSchemes.values.stream()
            .toArray()[Random().nextInt(RnartistConfig.colorSchemes.size)] as Map<String, Map<String, String>>

        AColorPicker.value =
            awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.AShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                255))
        ALabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.AShape.toString()]!![DrawingConfigurationParameter.color.toString()]
        if (scheme[SecondaryStructureType.A.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE)
        ) {
            ALabel.userData = "white"
            ALabel.textFill = Color.WHITE
        } else {
            ALabel.userData = "black"
            ALabel.textFill = Color.BLACK
        }

        UColorPicker.value =
            awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.UShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                255))
        ULabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.UShape.toString()]!![DrawingConfigurationParameter.color.toString()]
        if (scheme[SecondaryStructureType.U.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE)
        ) {
            ULabel.userData = "white"
            ULabel.textFill = Color.WHITE
        } else {
            ULabel.userData = "black"
            ULabel.textFill = Color.BLACK
        }

        GColorPicker.value =
            awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.GShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                255))
        GLabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.GShape.toString()]!![DrawingConfigurationParameter.color.toString()]
        if (scheme[SecondaryStructureType.G.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE)
        ) {
            GLabel.userData = "white"
            GLabel.textFill = Color.WHITE
        } else {
            GLabel.userData = "black"
            GLabel.textFill = Color.BLACK
        }

        CColorPicker.value =
            awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.CShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                255))
        CLabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.CShape.toString()]!![DrawingConfigurationParameter.color.toString()]
        if (scheme[SecondaryStructureType.C.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE)
        ) {
            CLabel.userData = "white"
            CLabel.textFill = Color.WHITE
        } else {
            CLabel.userData = "black"
            CLabel.textFill = Color.BLACK
        }

        pickColorScheme.onAction = EventHandler {
            val scheme = RnartistConfig.colorSchemes.values.stream()
                .toArray()[Random().nextInt(RnartistConfig.colorSchemes.size)] as Map<String, Map<String, String>>
            AColorPicker.value =
                awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.AShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                    255))
            ALabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.AShape.toString()]!![DrawingConfigurationParameter.color.toString()]
            if (scheme[SecondaryStructureType.A.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE)
            ) {
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
            } else {
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
            }
            UColorPicker.value =
                awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.UShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                    255))
            ULabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.UShape.toString()]!![DrawingConfigurationParameter.color.toString()]
            if (scheme[SecondaryStructureType.U.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE)
            ) {
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
            } else {
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
            }
            GColorPicker.value =
                awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.GShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                    255))
            GLabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.GShape.toString()]!![DrawingConfigurationParameter.color.toString()]
            if (scheme[SecondaryStructureType.G.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE)
            ) {
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
            } else {
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
            }
            CColorPicker.value =
                awtColorToJavaFX(getAWTColor(scheme[SecondaryStructureType.CShape.toString()]!![DrawingConfigurationParameter.color.toString()]!!,
                    255))
            CLabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.CShape.toString()]!![DrawingConfigurationParameter.color.toString()]
            if (scheme[SecondaryStructureType.C.toString()]!![DrawingConfigurationParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE)
            ) {
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }

        //syncColors.setMaxWidth(Double.MAX_VALUE);
        syncColors.userData = "unlock"
        syncColors.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        syncColors.onAction = EventHandler {
            if ("unlock" == syncColors.userData) {
                syncColors.graphic = FontIcon("fas-lock:12")
                syncColors.userData = "lock"
            } else {
                syncColors.graphic = FontIcon("fas-unlock:12")
                syncColors.userData = "unlock"
            }
        }

        leftToolBar.add(syncColors, 0, row++, 2, 1)
        GridPane.setHalignment(syncColors, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val lineWidth1 = Button(null, null)
        lineWidth1.maxWidth = Double.MAX_VALUE
        var line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 0.25
        lineWidth1.graphic = line
        lineWidth1.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth1.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "0.25")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "0.25")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.linewidth,
                "0.25")
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "0.25")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.linewidth,
                "0.25")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        val lineWidth2 = Button(null, null)
        lineWidth2.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 0.5
        lineWidth2.graphic = line
        lineWidth2.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth2.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "0.5")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "0.5")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.linewidth,
                "0.5")
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "0.5")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.linewidth,
                "0.5")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        leftToolBar.add(lineWidth1, 0, row)
        GridPane.setHalignment(lineWidth1, HPos.CENTER)
        leftToolBar.add(lineWidth2, 1, row++)
        GridPane.setHalignment(lineWidth2, HPos.CENTER)

        val lineWidth3 = Button(null, null)
        lineWidth3.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 0.75
        lineWidth3.graphic = line
        lineWidth3.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth3.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "0.75")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "0.75")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.linewidth,
                "0.75")
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "0.75")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.linewidth,
                "0.75")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        val lineWidth4 = Button(null, null)
        lineWidth4.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 1.0
        lineWidth4.graphic = line
        lineWidth4.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth4.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "1.0")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "1.0")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.linewidth,
                "1.0")
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "1.0")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.linewidth,
                "1.0")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        leftToolBar.add(lineWidth3, 0, row)
        GridPane.setHalignment(lineWidth3, HPos.CENTER)
        leftToolBar.add(lineWidth4, 1, row++)
        GridPane.setHalignment(lineWidth4, HPos.CENTER)

        val lineWidth5 = Button(null, null)
        lineWidth5.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 2.0
        lineWidth5.graphic = line
        lineWidth5.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth5.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "2.0")
            t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "2.0")
            t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "2.0")
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "2.0")
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.linewidth,
                "2.0")
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.linewidth,
                "2.0")
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.linewidth,
                "2.0")
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }

        val lineColorPicker = ColorPicker(Color.BLACK)
        lineColorPicker.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineColorPicker.maxWidth = Double.MAX_VALUE
        lineColorPicker.styleClass.add("button")
        lineColorPicker.style = "-fx-color-label-visible: false ;"
        lineColorPicker.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope= RNArtist.SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            val t = Theme()
            t.setConfigurationFor(SecondaryStructureType.Helix,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(lineColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.Junction,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(lineColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.SingleStrand,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(lineColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(lineColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(lineColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(lineColorPicker.value)))
            t.setConfigurationFor(SecondaryStructureType.InteractionSymbol,
                DrawingConfigurationParameter.color,
                getHTMLColorString(javaFXToAwt(lineColorPicker.value)))
            for (start in starts) mediator.explorer.applyTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        leftToolBar.add(lineWidth5, 0, row)
        GridPane.setHalignment(lineWidth5, HPos.CENTER)
        leftToolBar.add(lineColorPicker, 1, row++)
        GridPane.setHalignment(lineColorPicker, HPos.CENTER)

        s = Separator()
        s.padding = Insets(10.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        l = Label("3D")
        leftToolBar.add(l, 0, row++, 2, 1)
        GridPane.setHalignment(l, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        this.reload3D = Button(null, FontIcon("fas-redo:15"))
        this.reload3D.setDisable(true)
        this.reload3D.setOnMouseClicked(EventHandler<MouseEvent?> { mediator.chimeraDriver.reloadTertiaryStructure() })
        this.reload3D.setTooltip(Tooltip("Reload 3D"))

        this.focus3D = Button(null, FontIcon("fas-crosshairs:15"))
        this.focus3D.setDisable(true)
        this.focus3D.setOnMouseClicked(EventHandler<MouseEvent?> { mediator.focusInChimera() })
        this.focus3D.setTooltip(Tooltip("Focus 3D on Selection"))

        leftToolBar.add(this.reload3D, 0, row)
        GridPane.setHalignment(this.reload3D, HPos.CENTER)
        leftToolBar.add(this.focus3D, 1, row++)
        GridPane.setHalignment(this.focus3D, HPos.CENTER)

        this.paintSelectionin3D = Button(null, FontIcon("fas-fill:15"))
        this.paintSelectionin3D.setDisable(true)
        this.paintSelectionin3D.setOnMouseClicked(EventHandler<MouseEvent?> { mediator.chimeraDriver.color3D(mediator.canvas2D.getSelectedResidues()) })
        this.paintSelectionin3D.setTooltip(Tooltip("Paint 3D selection"))

        leftToolBar.add(this.paintSelectionin3D, 0, row++)
        GridPane.setHalignment(this.paintSelectionin3D, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        this.paintSelectionAsCartoon = Button("SC", null)
        this.paintSelectionAsCartoon.setDisable(true)
        this.paintSelectionAsCartoon.setOnMouseClicked(EventHandler<MouseEvent?> {
            mediator.chimeraDriver.represent(ChimeraDriver.CARTOON,
                mediator.canvas2D.getSelectedPositions())
        })
        this.paintSelectionAsCartoon.setTooltip(Tooltip("Selection as Cartoon"))

        this.paintSelectionAsStick = Button("SS", null)
        this.paintSelectionAsStick.setDisable(true)
        this.paintSelectionAsStick.setOnMouseClicked(EventHandler<MouseEvent?> {
            mediator.chimeraDriver.represent(ChimeraDriver.STICK,
                mediator.canvas2D.getSelectedPositions())
        })
        this.paintSelectionAsStick.setTooltip(Tooltip("Selection as Stick"))

        leftToolBar.add(this.paintSelectionAsCartoon, 0, row)
        GridPane.setHalignment(this.paintSelectionAsCartoon, HPos.CENTER)
        leftToolBar.add(this.paintSelectionAsStick, 1, row++)
        GridPane.setHalignment(this.paintSelectionAsStick, HPos.CENTER)

        this.showRibbon = Button("SR", null)
        this.showRibbon.setDisable(true)
        this.showRibbon.setOnMouseClicked(EventHandler<MouseEvent?> {
            mediator.chimeraDriver.represent(ChimeraDriver.RIBBON_SHOW,
                mediator.canvas2D.getSelectedPositions())
        })
        this.showRibbon.setTooltip(Tooltip("Show Ribbon"))

        this.hideRibbon = Button("HR", null)
        this.hideRibbon.setDisable(true)
        this.hideRibbon.setOnMouseClicked(EventHandler<MouseEvent?> {
            mediator.chimeraDriver.represent(ChimeraDriver.RIBBON_HIDE,
                mediator.canvas2D.getSelectedPositions())
        })
        this.hideRibbon.setTooltip(Tooltip("Hide Ribbon"))

        leftToolBar.add(this.showRibbon, 0, row)
        GridPane.setHalignment(this.showRibbon, HPos.CENTER)
        leftToolBar.add(this.hideRibbon, 1, row++)
        GridPane.setHalignment(this.hideRibbon, HPos.CENTER)

        root.left = leftToolBar

        //++++++ Canvas2D
        val swingNode = SwingNode()
        swingNode.onMouseMoved = EventHandler { mouseEvent: MouseEvent? ->
            mediator.drawingDisplayed.get()?.drawing?.let {
                it.quickDraw = false //a trick if after the scroll event the quickdraw is still true
            }
        }
        swingNode.onMouseClicked = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.PRIMARY) {
                mediator.drawingDisplayed.get()?.let mouseClicked@{ drawingLoaded ->
                    if (!drawingLoaded.drawing.workingSession.is_screen_capture) {
                        val at = AffineTransform()
                        at.translate(drawingLoaded.drawing.workingSession.viewX, drawingLoaded.drawing.workingSession.viewY)
                        at.scale(drawingLoaded.drawing.workingSession.zoomLevel, drawingLoaded.drawing.workingSession.zoomLevel)
                        for (knob in drawingLoaded.knobs)
                            if (knob.contains(mouseEvent.x, mouseEvent.y, at))
                                return@mouseClicked
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
                                                r.parent!!.parent)
                                        ) {
                                            mediator.canvas2D.addToSelection(r)
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent) && !mediator.canvas2D.isSelected(
                                                r.parent!!.parent)
                                        ) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent!!.parent)) {
                                            mediator.canvas2D.removeFromSelection(r.parent)
                                            mediator.canvas2D.addToSelection(r.parent!!.parent)
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
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
                                                interaction.parent)
                                        ) {
                                            mediator.canvas2D.addToSelection(interaction)
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(interaction.parent)) {
                                            mediator.canvas2D.removeFromSelection(interaction)
                                            mediator.canvas2D.addToSelection(interaction.parent)
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        }
                                    }
                                }
                                if (!mediator.canvas2D.isSelected(h)) {
                                    mediator.canvas2D.addToSelection(h)
                                    mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                        override fun isOK(el: DrawingElement?): Boolean {
                                            return mediator.canvas2D.isSelected(el)
                                        }
                                    },
                                        Arrays.asList(mediator.explorer.treeTableView.root),
                                        false,
                                        RNArtist.SCOPE.BRANCH)
                                    return@mouseClicked
                                } else {
                                    var p = h.parent
                                    while (p != null && mediator.canvas2D.isSelected(p)) {
                                        p = p.parent
                                    }
                                    if (p == null) {
                                        mediator.canvas2D.addToSelection(h)
                                        mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                            override fun isOK(el: DrawingElement?): Boolean {
                                                return mediator.canvas2D.isSelected(el)
                                            }
                                        },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH)
                                    } else {
                                        mediator.canvas2D.addToSelection(p)
                                        mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                            override fun isOK(el: DrawingElement?): Boolean {
                                                return mediator.canvas2D.isSelected(el)
                                            }
                                        },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH)
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
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        }
                                    }
                                }
                                if (!mediator.canvas2D.isSelected(j)) {
                                    mediator.canvas2D.addToSelection(j)
                                    mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                        override fun isOK(el: DrawingElement?): Boolean {
                                            return mediator.canvas2D.isSelected(el)
                                        }
                                    },
                                        Arrays.asList(mediator.explorer.treeTableView.root),
                                        false,
                                        RNArtist.SCOPE.BRANCH)
                                    return@mouseClicked
                                } else {
                                    var p = j.parent
                                    while (p != null && mediator.canvas2D.isSelected(p)) {
                                        p = p.parent
                                    }
                                    if (p == null) {
                                        mediator.canvas2D.addToSelection(j)
                                        mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                            override fun isOK(el: DrawingElement?): Boolean {
                                                return mediator.canvas2D.isSelected(el)
                                            }
                                        },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH)
                                    } else {
                                        mediator.canvas2D.addToSelection(p)
                                        mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                            override fun isOK(el: DrawingElement?): Boolean {
                                                return mediator.canvas2D.isSelected(el)
                                            }
                                        },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH)
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
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            mediator.explorer.selectAllTreeViewItems(object : DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH)
                                            return@mouseClicked
                                        }
                                    }
                                }
                            }
                        }
                        if (mouseEvent.clickCount == 2) {
                            //no selection
                            mediator.canvas2D.clearSelection()
                            mediator.explorer.clearSelection()
                        }
                    }
                }
            }
        }
        swingNode.onMouseDragged = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.SECONDARY) {
                mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                    if (drawingLoaded.drawing.workingSession.is_screen_capture) {
                        val transX: Double =
                            mouseEvent.x - drawingLoaded.drawing.workingSession.screen_capture_transX
                        val transY: Double =
                            mouseEvent.y - drawingLoaded.drawing.workingSession.screen_capture_transY
                        drawingLoaded.drawing.workingSession.screen_capture_transX = mouseEvent.x
                        drawingLoaded.drawing.workingSession.screen_capture_transY = mouseEvent.y
                        val at = AffineTransform()
                        at.translate(transX, transY)
                        drawingLoaded.drawing.workingSession.screen_capture_area =
                            at.createTransformedShape(drawingLoaded.drawing.workingSession.screen_capture_area)
                                .bounds
                    } else {
                        drawingLoaded.drawing.quickDraw = true
                        val transX: Double = mouseEvent.x - mediator.canvas2D.translateX
                        val transY: Double = mouseEvent.y - mediator.canvas2D.translateY
                        drawingLoaded.drawing.workingSession.moveView(transX, transY)
                        mediator.canvas2D.translateX = mouseEvent.x
                        mediator.canvas2D.translateY = mouseEvent.y
                    }
                    mediator.canvas2D.repaint()
                }
            }
        }
        swingNode.onMouseReleased = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.SECONDARY) {
                mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                    if (drawingLoaded.drawing.workingSession.is_screen_capture) {
                        drawingLoaded.drawing.workingSession.screen_capture_transX = 0.0
                        drawingLoaded.drawing.workingSession.screen_capture_transY = 0.0
                    } else {
                        drawingLoaded.drawing.quickDraw = false
                        mediator.canvas2D.translateX = 0.0
                        mediator.canvas2D.translateY = 0.0
                    }
                    mediator.canvas2D.repaint()
                }
            }
        }
        swingNode.onMousePressed = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.SECONDARY) {
                mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                    if (drawingLoaded.drawing.workingSession.is_screen_capture) {
                        drawingLoaded.drawing.workingSession.screen_capture_transX = mouseEvent.x
                        drawingLoaded.drawing.workingSession.screen_capture_transY = mouseEvent.y
                    } else {
                        mediator.canvas2D.translateX = mouseEvent.x
                        mediator.canvas2D.translateY = mouseEvent.y
                    }
                }
            }
        }
        swingNode.onScroll = EventHandler { scrollEvent: ScrollEvent ->
            mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                if (!drawingLoaded.drawing.workingSession.is_screen_capture) {
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
                        Point2D.Double((scrollEvent.x - drawingLoaded.drawing.workingSession.viewX) / drawingLoaded.drawing.workingSession.zoomLevel,
                            (scrollEvent.y - drawingLoaded.drawing.workingSession.viewY) / drawingLoaded.drawing.workingSession.zoomLevel)
                    val notches = scrollEvent.deltaY
                    if (notches < 0) drawingLoaded.drawing.workingSession.setZoom(1.25)
                    if (notches > 0) drawingLoaded.drawing.workingSession.setZoom(1.0 / 1.25)
                    val newRealMouse =
                        Point2D.Double((scrollEvent.x - drawingLoaded.drawing.workingSession.viewX) / drawingLoaded.drawing.workingSession.zoomLevel,
                            (scrollEvent.y - drawingLoaded.drawing.workingSession.viewY) / drawingLoaded.drawing.workingSession.zoomLevel)
                    drawingLoaded.drawing.workingSession.moveView((newRealMouse.getX() - realMouse.getX()) * drawingLoaded.drawing.workingSession.zoomLevel,
                        (newRealMouse.getY() - realMouse.getY()) * drawingLoaded.drawing.workingSession.zoomLevel)
                    mediator.canvas2D.repaint()
                }
            }
        }
        createSwingContent(swingNode)
        root.center = swingNode

        //### Status Bar
        this.statusBar = FlowPane()
        statusBar.setAlignment(Pos.CENTER_RIGHT)
        statusBar.setPadding(Insets(5.0, 10.0, 5.0, 10.0))
        statusBar.setHgap(20.0)

        val release = Label(getRnartistRelease())
        statusBar.getChildren().add(release)

        val status = isDockerInstalled() && isAssemble2DockerImageInstalled()
        val dockerStatus =
            Label(null, if (status) FontIcon("fas-check-circle:15") else FontIcon("fas-exclamation-circle:15"))
        dockerStatus.tooltip =
            Tooltip(if (status) "I found RNAVIEW! You can open PDB files" else "RNAVIEW not found! You cannot open PDB files!")
        if (status) (dockerStatus.graphic as FontIcon).fill = Color.GREEN else (dockerStatus.graphic as FontIcon).fill =
            Color.RED
        val checkDockerStatus = Timeline(KeyFrame(Duration.seconds(30.0),
            {
                val status = isDockerInstalled() && isAssemble2DockerImageInstalled()
                dockerStatus.graphic =
                    if (status) FontIcon("fas-check-circle:15") else FontIcon("fas-exclamation-circle:15")
                dockerStatus.tooltip =
                    Tooltip(if (status) "I found RNAVIEW! You can open PDB files" else "RNAVIEW not found! You cannot open PDB files!")
                if (status) (dockerStatus.graphic as FontIcon).fill =
                    Color.GREEN else (dockerStatus.graphic as FontIcon).fill =
                    Color.RED
            }))
        checkDockerStatus.cycleCount = Timeline.INDEFINITE
        checkDockerStatus.play()

        statusBar.getChildren().add(dockerStatus)

        val shutdown = Button(null, FontIcon("fas-power-off:15"))
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

        val settings = Button(null, FontIcon("fas-cog:15"))
        settings.onAction = EventHandler { actionEvent: ActionEvent? ->
            mediator.settings.stage.show()
            mediator.settings.stage.toFront()
        }
        windowsBar.children.add(settings)

        val explorer = Button(null, FontIcon("fas-th-list:15"))
        explorer.onAction = EventHandler { actionEvent: ActionEvent? ->
            mediator.explorer.stage.show()
            mediator.explorer.stage.toFront()
        }
        windowsBar.children.add(explorer)

        val browser = Button(null, FontIcon("fas-globe-europe:15"))
        browser.onAction = EventHandler { actionEvent: ActionEvent? ->
            mediator.webBrowser.stage.show()
            mediator.webBrowser.stage.toFront()
        }
        windowsBar.children.add(browser)

        val bar = GridPane()
        val cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        bar.columnConstraints.addAll(cc, ColumnConstraints())

        bar.add(windowsBar, 0, 0)
        GridPane.setFillWidth(windowsBar, true)
        GridPane.setHalignment(windowsBar, HPos.LEFT)
        bar.add(this.statusBar, 1, 0)
        GridPane.setHalignment(this.statusBar, HPos.RIGHT)
        root.bottom = bar
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

        val screen = Screen.getPrimary()

        val scene = Scene(this.root, screen.bounds.width, screen.bounds.height)
        stage.scene = scene
        stage.title = "RNArtist"

        val screenSize = Screen.getPrimary().bounds
        val width = (screenSize.width * 4.0 / 10.0).toInt()
        scene.window.width = screenSize.width - width
        scene.window.height = screenSize.height
        scene.window.x = 0.0
        scene.window.y = 0.0

        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")

        SplashWindow(this.mediator)
    }

    private fun createSwingContent(swingNode: SwingNode) {
        SwingUtilities.invokeLater {
            val canvas = Canvas2D(mediator)
            swingNode.content = canvas
        }
    }

    fun main() {
        launch();
    }

}