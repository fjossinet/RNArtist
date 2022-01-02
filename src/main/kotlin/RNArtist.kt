package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.RnartistConfig.getRnartistRelease
import io.github.fjossinet.rnartist.core.RnartistConfig.isDockerImageInstalled
import io.github.fjossinet.rnartist.core.RnartistConfig.isDockerInstalled
import io.github.fjossinet.rnartist.core.RnartistConfig.load
import io.github.fjossinet.rnartist.core.RnartistConfig.save
import io.github.fjossinet.rnartist.core.RnartistConfig.selectionWidth
import io.github.fjossinet.rnartist.core.io.createTemporaryFile
import io.github.fjossinet.rnartist.core.io.toJSON
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.theme
import io.github.fjossinet.rnartist.gui.Canvas2D
import io.github.fjossinet.rnartist.gui.Explorer
import io.github.fjossinet.rnartist.gui.SplashWindow
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.io.sendField
import io.github.fjossinet.rnartist.io.sendFile
import io.github.fjossinet.rnartist.model.DrawingLoaded
import io.github.fjossinet.rnartist.model.DrawingLoadedFromRNArtistDB
import io.github.fjossinet.rnartist.model.ExplorerItem
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.CacheHint
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
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

class RNArtist : Application() {
    enum class SCOPE {
        ELEMENT, STRUCTURAL_DOMAIN, BRANCH
    }

    val mediator: Mediator
    lateinit var stage: Stage
    private var scrollCounter = 0
    private val statusBar: FlowPane
    val allStructuresAvailable = MenuButton("Choose a 2D")
    val clearAll2DsItem: MenuItem
    val clearAll2DsExceptCurrentItem: MenuItem
    val updateProject: Button
    val focus3D: Button
    val reload3D: Button
    val paintSelectionin3D: Button
    private val root: BorderPane
    var centerDisplayOnSelection = false

    fun getInstallDir(): String {
        return File(
            this::class.java.getProtectionDomain().getCodeSource().getLocation()
                .toURI()
        ).parentFile.parent
    }

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
            fileChooser.initialDirectory = File(mediator.rnartist.getInstallDir(), "samples")
            val file = fileChooser.showOpenDialog(stage)
            file?.let { f ->
                if (f.name.endsWith(".ct")) {
                    Platform.runLater {
                        mediator.scriptEditor.loadScript(
                            StringReader(
                                """
rnartist {
    ss {
        ct {
            file = "${f.absolutePath.replace("\\", "/")}"
        }
    }
    
    theme {
       details {
           value = 1
       }
    }
}
"""
                            )
                        )
                        mediator.scriptEditor.runScript()
                    }
                } else if (f.name.endsWith(".bpseq")) {
                    Platform.runLater {
                        mediator.scriptEditor.loadScript(
                            StringReader(
                                """
rnartist {
    ss {
        bpseq {
            file = "${f.absolutePath.replace("\\", "/")}"
        }
    }    
    theme {
       details {
           value = 1
       }
    }
}
"""
                            )
                        )
                        mediator.scriptEditor.runScript()
                    }
                } else if (f.name.endsWith(".fasta") || f.name.endsWith(".fas") || f.name.endsWith(
                        ".fa"
                    ) || f.name.endsWith(".vienna")
                ) {
                    Platform.runLater {
                        mediator.scriptEditor.loadScript(
                            StringReader(
                                """
rnartist {
    ss {
        vienna {
            file = "${f.absolutePath.replace("\\", "/")}"
        }
    }    
    theme {
       details {
           value = 1
       }
    }
}
"""
                            )
                        )
                        mediator.scriptEditor.runScript()
                    }
                } else if (f.name.matches(Regex(".+\\.pdb[0-9]?"))) {
                    Platform.runLater {
                        mediator.scriptEditor.loadScript(
                            StringReader(
                                """
rnartist {
    ss {
        pdb {
            file = "${f.absolutePath.replace("\\", "/")}"
        }
    }    
    theme {
       details {
           value = 1
       }
    }
}
"""
                            )
                        )
                        mediator.scriptEditor.runScript()
                    }
                } else if (f.name.endsWith(".stk") || f.name.endsWith(".stockholm")) {
                    Platform.runLater {
                        mediator.scriptEditor.loadScript(
                            StringReader(
                                """
rnartist {
    ss {
        stockholm {
            file = "${f.absolutePath.replace("\\", "/")}"
        }
    }    
    theme {
       details {
           value = 1
       }
    }
}
"""
                            )
                        )
                        mediator.scriptEditor.runScript()
                    }
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
            mediator.projectsPanel.loadProjects()
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
                    Rectangle2D.Double(
                        mediator.canvas2D.getBounds().getCenterX() - 200,
                        mediator.canvas2D.getBounds().getCenterY() - 100,
                        400.0,
                        200.0
                    )
                mediator.canvas2D.repaint()
                val dialog = TextInputDialog("My Project")
                dialog.initModality(Modality.NONE)
                dialog.title = "Project Saving"
                dialog.headerText =
                    "Keep right mouse button pressed and drag the rectangle to define your project miniature."
                dialog.contentText = "Project name:"
                val projectName = dialog.showAndWait()
                if (projectName.isPresent && !projectName.isEmpty) {
                    try {
                        mediator.projectsPanel.saveProjectAs(
                            projectName.get().trim { it <= ' ' },
                            mediator.canvas2D.screenCapture()!!
                        )?.let { id ->
                            drawing.workingSession.is_screen_capture = false
                            drawing.workingSession.screen_capture_area = null
                            mediator.embeddedDB.getProject(id)
                                ?.let { newDrawing -> //we reload it from the DB to have a copy of the drawing and not the same object
                                    mediator.drawingsLoaded.add(
                                        0,
                                        DrawingLoadedFromRNArtistDB(mediator,
                                            newDrawing,
                                            id,
                                            projectName.get().trim { it <= ' ' })
                                    )
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

        this.updateProject = Button(null, FontIcon("fas-sync:15"))

        this.updateProject.setOnMouseClicked(EventHandler<MouseEvent?> {
            try {
                mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                    drawing.workingSession.is_screen_capture = true
                    drawing.workingSession.screen_capture_area =
                        Rectangle2D.Double(
                            mediator.canvas2D.getBounds().getCenterX() - 200,
                            mediator.canvas2D.getBounds().getCenterY() - 100,
                            400.0,
                            200.0
                        )
                    mediator.canvas2D.repaint()
                    val dialog =
                        TextInputDialog((mediator.drawingDisplayed.get() as? DrawingLoadedFromRNArtistDB)?.projectName)
                    dialog.initModality(Modality.NONE)
                    dialog.title = "Project Saving"
                    dialog.headerText =
                        "Keep right mouse button pressed and drag the rectangle to define your project miniature."
                    dialog.contentText = "Project name:"
                    val projectName = dialog.showAndWait()
                    if (projectName.isPresent) {
                        (mediator.drawingDisplayed.get() as? DrawingLoadedFromRNArtistDB)?.projectName =
                            projectName.get().trim { it <= ' ' }
                        mediator.projectsPanel.updateProject(
                            projectName.get().trim { it <= ' ' },
                            mediator.canvas2D.screenCapture()!!
                        )
                        allStructuresAvailable.items.forEach {
                            if (it.userData == mediator.drawingDisplayed.get()) {
                                it.text = it.userData.toString()
                            }
                        }
                        drawing.workingSession.is_screen_capture = false
                        drawing.workingSession.screen_capture_area = null
                        mediator.canvas2D.repaint()
                    } else {
                        drawing.workingSession.is_screen_capture = false
                        drawing.workingSession.screen_capture_area = null
                        mediator.canvas2D.repaint()
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        this.updateProject.setTooltip(Tooltip("Update Project in DB"))
        GridPane.setConstraints(updateProject, 1, 1)
        saveFiles.children.add(updateProject)

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
                        drawing.asSVG(
                            frame = Rectangle2D.Double(
                                0.0,
                                0.0,
                                mediator.canvas2D.getBounds().width.toDouble(),
                                mediator.canvas2D.getBounds().height.toDouble()
                            ), outputFile = file
                        )

                    } else if (file.name.endsWith(".json")) {
                        fileChooser.initialDirectory = file.parentFile
                        val writer: PrintWriter
                        try {
                            writer = PrintWriter(file)
                            writer.println(toJSON(drawing))
                            writer.close()
                            mediator.chimeraDriver.saveSession(
                                File(
                                    file.parentFile,
                                    file.name.split(".".toRegex()).toTypedArray()[0] + ".py"
                                ),
                                File(file.parentFile, file.name.split(".".toRegex()).toTypedArray()[0] + ".pdb")
                            )
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

        val submitProject = Button(null, FontIcon("fas-database:15"))
        submitProject.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        submitProject.onMouseClicked = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                drawing.workingSession.is_screen_capture = true
                drawing.workingSession.screen_capture_area =
                    Rectangle2D.Double(
                        mediator.canvas2D.getBounds().getCenterX() - 200,
                        mediator.canvas2D.getBounds().getCenterY() - 100,
                        400.0,
                        200.0
                    )
                mediator.canvas2D.repaint()
                val dialog = TextInputDialog("My Project")
                dialog.initModality(Modality.NONE)
                dialog.title = "Project Saving"
                dialog.headerText =
                    "Keep right mouse button pressed and drag the rectangle to define your project miniature."
                dialog.contentText = "Project name:"
                val projectName = dialog.showAndWait()
                if (projectName.isPresent && !projectName.isEmpty) {
                    try {
                        val pictureFile = createTemporaryFile("test.svg")
                        //ImageIO.write(mediator.canvas2D.screenCapture()!!, "PNG", pictureFile)
                        drawing.asSVG(
                            frame = Rectangle2D.Double(
                                0.0,
                                0.0,
                                mediator.canvas2D.getBounds().width.toDouble(),
                                mediator.canvas2D.getBounds().height.toDouble()
                            ), outputFile = pictureFile
                        )

                        val url = URL("http://localhost:8080/api/submit")
                        val con = url.openConnection()
                        val http = con as HttpURLConnection
                        http.setRequestMethod("POST")
                        http.setDoOutput(true)
                        val boundary = UUID.randomUUID().toString()
                        val boundaryBytes = "--$boundary\r\n".toByteArray(StandardCharsets.UTF_8)
                        val finishBoundaryBytes = "--$boundary--".toByteArray(StandardCharsets.UTF_8)
                        http.setRequestProperty(
                            "Content-Type",
                            "multipart/form-data; charset=UTF-8; boundary=$boundary"
                        )

                        // Enable streaming mode with default settings
                        http.setChunkedStreamingMode(0)

                        // Send our fields:
                        http.outputStream.use { out ->
                            // Send our header (thx Algoman)
                            out.write(boundaryBytes)

                            // Send our first field
                            sendField(out, "script", mediator.scriptEditor.getScriptAsText())

                            // Send a separator
                            out.write(boundaryBytes)

                            // Send our second field
                            sendField(out, "password", "toor")

                            //Send another separator
                            out.write(boundaryBytes)
                            FileInputStream(pictureFile).use { file ->
                                sendFile(
                                    out,
                                    "capture",
                                    file,
                                    pictureFile.name
                                )
                            }

                            // Finish the request
                            out.write(finishBoundaryBytes)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    drawing.workingSession.is_screen_capture = false
                    drawing.workingSession.screen_capture_area = null
                    mediator.canvas2D.repaint()
                } else {
                    drawing.workingSession.is_screen_capture = false
                    drawing.workingSession.screen_capture_area = null
                    mediator.canvas2D.repaint()
                }
            }
        }
        submitProject.tooltip = Tooltip("Submit project to RNArtist Gallery")
        GridPane.setConstraints(submitProject, 3, 1)
        //saveFiles.children.add(submitProject)

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

        l = Label("2Ds Loaded")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        structureSelection.children.add(l)

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
                    drawingLoaded
                )
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

        toolbar.items.addAll(
            loadFiles,
            s1,
            saveFiles,
            s2,
            structureSelection,
            s3,
            tertiaryStructureButtons,
            s4,
            selectionSize,
            selectionColor
        )

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
            val t = theme {
                show {
                    type = "tertiary_interaction"
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
                for (selectedItem in mediator.explorer.treeTableView.selectionModel.selectedItems) {
                    if (ResidueDrawing::class.java.isInstance(selectedItem.value.drawingElement)) starts.add(
                        selectedItem.parent
                    ) //if the user has selected single residues, this allows to display its tertiary interactions, since a tertiary can be a parent of a residue in the explorer
                    starts.add(selectedItem)
                }
                scope = RNArtist.SCOPE.STRUCTURAL_DOMAIN
            }
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        showTertiaries.tooltip = Tooltip("Show Tertiaries")

        val hideTertiaries = Button(null, FontIcon("fas-eye-slash:15"))
        hideTertiaries.maxWidth = Double.MAX_VALUE
        hideTertiaries.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        hideTertiaries.onMouseClicked = EventHandler {
            val t = theme {
                hide {
                    type = "tertiary_interaction"
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
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
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
            var scope = SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = SCOPE.STRUCTURAL_DOMAIN
            }
            val t = theme {
                details {
                    value = 1
                }
            }
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setDetailsLevel("1")
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
            val t = theme {
                details {
                    value = 2
                }
            }
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setDetailsLevel("2")
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
            var scope = SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = SCOPE.STRUCTURAL_DOMAIN
            }
            val t = theme {
                details {
                    value = 3
                }
            }
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setDetailsLevel("3")
        }

        val levelDetails4 = Button("4")
        levelDetails4.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails4.maxWidth = Double.MAX_VALUE
        levelDetails4.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = SCOPE.STRUCTURAL_DOMAIN
            }
            val t = theme {
                details {
                    value = 4
                }
            }
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setDetailsLevel("4")
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
            var scope = SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = SCOPE.STRUCTURAL_DOMAIN
            }
            val t = theme {
                details {
                    value = 5
                }
            }
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setDetailsLevel("5")
        }

        leftToolBar.add(levelDetails5, 0, row++)
        GridPane.setHalignment(levelDetails5, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val AColorPicker = ColorPicker()
        AColorPicker.maxWidth = Double.MAX_VALUE
        val UColorPicker = ColorPicker()
        UColorPicker.maxWidth = Double.MAX_VALUE
        val GColorPicker = ColorPicker()
        GColorPicker.maxWidth = Double.MAX_VALUE
        val CColorPicker = ColorPicker()
        CColorPicker.maxWidth = Double.MAX_VALUE
        val RColorPicker = ColorPicker()
        RColorPicker.value = Color.BLACK
        RColorPicker.maxWidth = Double.MAX_VALUE
        val YColorPicker = ColorPicker()
        YColorPicker.value = Color.BLACK
        YColorPicker.maxWidth = Double.MAX_VALUE
        val NColorPicker = ColorPicker()
        NColorPicker.value = Color.BLACK
        NColorPicker.maxWidth = Double.MAX_VALUE

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
        val RLabel = Button("R")
        RLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        RLabel.maxWidth = Double.MAX_VALUE
        RLabel.userData = "white"
        RLabel.textFill = Color.WHITE
        RLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(Color.BLACK))
        val YLabel = Button("Y")
        YLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        YLabel.maxWidth = Double.MAX_VALUE
        YLabel.userData = "white"
        YLabel.textFill = Color.WHITE
        YLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(Color.BLACK))
        val NLabel = Button("N")
        NLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        NLabel.maxWidth = Double.MAX_VALUE
        NLabel.userData = "white"
        NLabel.textFill = Color.WHITE
        NLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(Color.BLACK))

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
            var scope = SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = SCOPE.STRUCTURAL_DOMAIN
            }
            val t = theme {
                color {
                    type = "A"
                    value = getHTMLColorString(javaFXToAwt(AColorPicker.value))
                }
                color {
                    type = "a"
                    value =
                        getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE))
                }
                color {
                    type = "U"
                    value = getHTMLColorString(javaFXToAwt(UColorPicker.value))
                }
                color {
                    type = "u"
                    value =
                        getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE))
                }
                color {
                    type = "G"
                    value = getHTMLColorString(javaFXToAwt(GColorPicker.value))
                }
                color {
                    type = "g"
                    value =
                        getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE))
                }
                color {
                    type = "C"
                    value = getHTMLColorString(javaFXToAwt(CColorPicker.value))
                }
                color {
                    type = "c"
                    value =
                        getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE))
                }
            }

            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setColor("A", getHTMLColorString(javaFXToAwt(AColorPicker.value)))
            mediator.scriptEditor.themeAndLayoutScript.setColor(
                "a",
                getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE))
            )
            mediator.scriptEditor.themeAndLayoutScript.setColor("U", getHTMLColorString(javaFXToAwt(UColorPicker.value)))
            mediator.scriptEditor.themeAndLayoutScript.setColor(
                "u",
                getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE))
            )
            mediator.scriptEditor.themeAndLayoutScript.setColor("G", getHTMLColorString(javaFXToAwt(GColorPicker.value)))
            mediator.scriptEditor.themeAndLayoutScript.setColor(
                "g",
                getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE))
            )
            mediator.scriptEditor.themeAndLayoutScript.setColor("C", getHTMLColorString(javaFXToAwt(CColorPicker.value)))
            mediator.scriptEditor.themeAndLayoutScript.setColor(
                "c",
                getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE))
            )
        }

        leftToolBar.add(pickColorScheme, 0, row)
        GridPane.setHalignment(pickColorScheme, HPos.CENTER)
        leftToolBar.add(paintResidues, 1, row++)
        GridPane.setHalignment(paintResidues, HPos.CENTER)

        ALabel.onAction = EventHandler {
            if (ALabel.userData == "black") {
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
            } else {
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
            }
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
        }

        ULabel.onAction = EventHandler {
            if (ULabel.userData == "black") {
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
            } else {
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
            }
        }

        leftToolBar.add(ULabel, 0, row)
        GridPane.setHalignment(ULabel, HPos.CENTER)
        leftToolBar.add(UColorPicker, 1, row++)
        GridPane.setHalignment(UColorPicker, HPos.CENTER)

        UColorPicker.styleClass.add("button")
        UColorPicker.style = "-fx-color-label-visible: false ;"
        UColorPicker.onAction = EventHandler {
            ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(UColorPicker.value))
        }

        GLabel.onAction = EventHandler {
            if (GLabel.userData == "black") {
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
            } else {
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(GLabel, 0, row)
        GridPane.setHalignment(GLabel, HPos.CENTER)
        leftToolBar.add(GColorPicker, 1, row++)
        GridPane.setHalignment(GColorPicker, HPos.CENTER)

        GColorPicker.styleClass.add("button")
        GColorPicker.style = "-fx-color-label-visible: false ;"
        GColorPicker.onAction = EventHandler {
            GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(GColorPicker.value))
        }

        CLabel.onAction = EventHandler {
            if (CLabel.userData == "black") {
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(CLabel, 0, row)
        GridPane.setHalignment(CLabel, HPos.CENTER)
        leftToolBar.add(CColorPicker, 1, row++)
        GridPane.setHalignment(CColorPicker, HPos.CENTER)

        CColorPicker.styleClass.add("button")
        CColorPicker.style = "-fx-color-label-visible: false ;"
        CColorPicker.onAction = EventHandler {
            CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(CColorPicker.value))
        }

        RLabel.onAction = EventHandler {
            if (RLabel.userData == "black") {
                RLabel.userData = "white"
                RLabel.textFill = Color.WHITE
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
            } else {
                RLabel.userData = "black"
                RLabel.textFill = Color.BLACK
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(RLabel, 0, row)
        GridPane.setHalignment(RLabel, HPos.CENTER)
        leftToolBar.add(RColorPicker, 1, row++)
        GridPane.setHalignment(RColorPicker, HPos.CENTER)

        RColorPicker.styleClass.add("button")
        RColorPicker.style = "-fx-color-label-visible: false ;"
        RColorPicker.onAction = EventHandler {
            RLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(RColorPicker.value))
            AColorPicker.value = RColorPicker.value
            ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(RColorPicker.value))
            GColorPicker.value = RColorPicker.value
            GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(RColorPicker.value))
        }

        YLabel.onAction = EventHandler {
            if (YLabel.userData == "black") {
                YLabel.userData = "white"
                YLabel.textFill = Color.WHITE
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                YLabel.userData = "black"
                YLabel.textFill = Color.BLACK
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(YLabel, 0, row)
        GridPane.setHalignment(YLabel, HPos.CENTER)
        leftToolBar.add(YColorPicker, 1, row++)
        GridPane.setHalignment(YColorPicker, HPos.CENTER)

        YColorPicker.styleClass.add("button")
        YColorPicker.style = "-fx-color-label-visible: false ;"
        YColorPicker.onAction = EventHandler {
            YLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(YColorPicker.value))
            UColorPicker.value = YColorPicker.value
            ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(YColorPicker.value))
            CColorPicker.value = YColorPicker.value
            CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(YColorPicker.value))
        }

        NLabel.onAction = EventHandler {
            if (NLabel.userData == "black") {
                NLabel.userData = "white"
                NLabel.textFill = Color.WHITE
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                NLabel.userData = "black"
                NLabel.textFill = Color.BLACK
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(NLabel, 0, row)
        GridPane.setHalignment(NLabel, HPos.CENTER)
        leftToolBar.add(NColorPicker, 1, row++)
        GridPane.setHalignment(NColorPicker, HPos.CENTER)

        NColorPicker.styleClass.add("button")
        NColorPicker.style = "-fx-color-label-visible: false ;"
        NColorPicker.onAction = EventHandler {
            NLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            AColorPicker.value = NColorPicker.value
            ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            GColorPicker.value = NColorPicker.value
            GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            UColorPicker.value = NColorPicker.value
            ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            CColorPicker.value = NColorPicker.value
            CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
        }

        //++++++++++ we init the color buttons with a random scheme
        val scheme = RnartistConfig.colorSchemes.values.stream()
            .toArray()[Random().nextInt(RnartistConfig.colorSchemes.size)] as Map<String, Map<String, String>>

        AColorPicker.value =
            awtColorToJavaFX(
                getAWTColor(
                    scheme[SecondaryStructureType.AShape.toString()]!![ThemeParameter.color.toString()]!!,
                    255
                )
            )
        ALabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.AShape.toString()]!![ThemeParameter.color.toString()]
        if (scheme[SecondaryStructureType.A.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE
            )
        ) {
            ALabel.userData = "white"
            ALabel.textFill = Color.WHITE
        } else {
            ALabel.userData = "black"
            ALabel.textFill = Color.BLACK
        }

        UColorPicker.value =
            awtColorToJavaFX(
                getAWTColor(
                    scheme[SecondaryStructureType.UShape.toString()]!![ThemeParameter.color.toString()]!!,
                    255
                )
            )
        ULabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.UShape.toString()]!![ThemeParameter.color.toString()]
        if (scheme[SecondaryStructureType.U.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE
            )
        ) {
            ULabel.userData = "white"
            ULabel.textFill = Color.WHITE
        } else {
            ULabel.userData = "black"
            ULabel.textFill = Color.BLACK
        }

        GColorPicker.value =
            awtColorToJavaFX(
                getAWTColor(
                    scheme[SecondaryStructureType.GShape.toString()]!![ThemeParameter.color.toString()]!!,
                    255
                )
            )
        GLabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.GShape.toString()]!![ThemeParameter.color.toString()]
        if (scheme[SecondaryStructureType.G.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE
            )
        ) {
            GLabel.userData = "white"
            GLabel.textFill = Color.WHITE
        } else {
            GLabel.userData = "black"
            GLabel.textFill = Color.BLACK
        }

        CColorPicker.value =
            awtColorToJavaFX(
                getAWTColor(
                    scheme[SecondaryStructureType.CShape.toString()]!![ThemeParameter.color.toString()]!!,
                    255
                )
            )
        CLabel.style =
            "-fx-background-color: " + scheme[SecondaryStructureType.CShape.toString()]!![ThemeParameter.color.toString()]
        if (scheme[SecondaryStructureType.C.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                java.awt.Color.WHITE
            )
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
                awtColorToJavaFX(
                    getAWTColor(
                        scheme[SecondaryStructureType.AShape.toString()]!![ThemeParameter.color.toString()]!!,
                        255
                    )
                )
            ALabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.AShape.toString()]!![ThemeParameter.color.toString()]
            if (scheme[SecondaryStructureType.A.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE
                )
            ) {
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
            } else {
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
            }
            UColorPicker.value =
                awtColorToJavaFX(
                    getAWTColor(
                        scheme[SecondaryStructureType.UShape.toString()]!![ThemeParameter.color.toString()]!!,
                        255
                    )
                )
            ULabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.UShape.toString()]!![ThemeParameter.color.toString()]
            if (scheme[SecondaryStructureType.U.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE
                )
            ) {
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
            } else {
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
            }
            GColorPicker.value =
                awtColorToJavaFX(
                    getAWTColor(
                        scheme[SecondaryStructureType.GShape.toString()]!![ThemeParameter.color.toString()]!!,
                        255
                    )
                )
            GLabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.GShape.toString()]!![ThemeParameter.color.toString()]
            if (scheme[SecondaryStructureType.G.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE
                )
            ) {
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
            } else {
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
            }
            CColorPicker.value =
                awtColorToJavaFX(
                    getAWTColor(
                        scheme[SecondaryStructureType.CShape.toString()]!![ThemeParameter.color.toString()]!!,
                        255
                    )
                )
            CLabel.style =
                "-fx-background-color: " + scheme[SecondaryStructureType.CShape.toString()]!![ThemeParameter.color.toString()]
            if (scheme[SecondaryStructureType.C.toString()]!![ThemeParameter.color.toString()] == getHTMLColorString(
                    java.awt.Color.WHITE
                )
            ) {
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val applyLineWidth = { lineWidth: Double ->
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            var scope = SCOPE.BRANCH
            if (mediator.explorer.treeTableView.selectionModel
                    .isEmpty() || mediator.explorer.treeTableView.selectionModel
                    .getSelectedItems().size == 1 && mediator.explorer.treeTableView.selectionModel
                    .getSelectedItem() === mediator.explorer.treeTableView.root
            ) {
                starts.add(mediator.explorer.treeTableView.root)
            } else {
                starts.addAll(mediator.explorer.treeTableView.selectionModel.selectedItems)
                scope = SCOPE.STRUCTURAL_DOMAIN
            }
            val t = theme {
                line {
                    type =
                        "helix junction single_strand N secondary_interaction phosphodiester_bond tertiary_interaction interaction_symbol"
                    value = lineWidth
                }
            }
            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setLineWidth(
                "helix junction single_strand N secondary_interaction phosphodiester_bond tertiary_interaction interaction_symbol",
                "${lineWidth}"
            )
        }

        val lineWidth1 = Button(null, null)
        lineWidth1.maxWidth = Double.MAX_VALUE
        var line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 0.25
        lineWidth1.graphic = line
        lineWidth1.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth1.onAction = EventHandler {
            applyLineWidth(0.25)
        }

        val lineWidth2 = Button(null, null)
        lineWidth2.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 0.5
        lineWidth2.graphic = line
        lineWidth2.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth2.onAction = EventHandler {
            applyLineWidth(0.5)
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
            applyLineWidth(0.75)
        }

        val lineWidth4 = Button(null, null)
        lineWidth4.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 1.0
        lineWidth4.graphic = line
        lineWidth4.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth4.onAction = EventHandler {
            applyLineWidth(1.0)
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
            applyLineWidth(2.0)
        }

        val lineColorPicker = ColorPicker(Color.BLACK)
        lineColorPicker.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineColorPicker.maxWidth = Double.MAX_VALUE
        lineColorPicker.styleClass.add("button")
        lineColorPicker.style = "-fx-color-label-visible: false ;"
        lineColorPicker.onAction = EventHandler {
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
            val t = theme {
                color {
                    type =
                        "helix junction single_strand secondary_interaction phosphodiester_bond interaction_symbol tertiary_interaction"
                    value = getHTMLColorString(javaFXToAwt(lineColorPicker.value))
                }
            }

            for (start in starts) mediator.explorer.applyAdvancedTheme(start, t, scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
            mediator.scriptEditor.themeAndLayoutScript.setColor(
                "helix junction single_strand secondary_interaction phosphodiester_bond interaction_symbol tertiary_interaction",
                getHTMLColorString(javaFXToAwt(lineColorPicker.value))
            )

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
                        at.translate(
                            drawingLoaded.drawing.workingSession.viewX,
                            drawingLoaded.drawing.workingSession.viewY
                        )
                        at.scale(
                            drawingLoaded.drawing.workingSession.zoomLevel,
                            drawingLoaded.drawing.workingSession.zoomLevel
                        )
                        for (knob in drawingLoaded.knobs)
                            if (knob.contains(mouseEvent.x, mouseEvent.y))
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
                                                r.parent!!.parent
                                            )
                                        ) {
                                            mediator.canvas2D.addToSelection(r)
                                            mediator.explorer.selectAllTreeViewItems(
                                                object :
                                                    Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent) && !mediator.canvas2D.isSelected(
                                                r.parent!!.parent
                                            )
                                        ) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            mediator.explorer.selectAllTreeViewItems(
                                                object :
                                                    Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent!!.parent)) {
                                            mediator.canvas2D.removeFromSelection(r.parent)
                                            mediator.canvas2D.addToSelection(r.parent!!.parent)
                                            mediator.explorer.selectAllTreeViewItems(
                                                object : Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
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
                                            mediator.explorer.selectAllTreeViewItems(
                                                object :
                                                    Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(interaction.parent)) {
                                            mediator.canvas2D.removeFromSelection(interaction)
                                            mediator.canvas2D.addToSelection(interaction.parent)
                                            mediator.explorer.selectAllTreeViewItems(
                                                object : Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
                                            return@mouseClicked
                                        }
                                    }
                                }
                                if (!mediator.canvas2D.isSelected(h)) {
                                    mediator.canvas2D.addToSelection(h)
                                    mediator.explorer.selectAllTreeViewItems(
                                        object : Explorer.DrawingElementFilter {
                                            override fun isOK(el: DrawingElement?): Boolean {
                                                return mediator.canvas2D.isSelected(el)
                                            }
                                        },
                                        Arrays.asList(mediator.explorer.treeTableView.root),
                                        false,
                                        RNArtist.SCOPE.BRANCH
                                    )
                                    return@mouseClicked
                                } else {
                                    var p = h.parent
                                    while (p != null && mediator.canvas2D.isSelected(p)) {
                                        p = p.parent
                                    }
                                    if (p == null) {
                                        mediator.canvas2D.addToSelection(h)
                                        mediator.explorer.selectAllTreeViewItems(
                                            object : Explorer.DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH
                                        )
                                    } else {
                                        mediator.canvas2D.addToSelection(p)
                                        mediator.explorer.selectAllTreeViewItems(
                                            object : Explorer.DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH
                                        )
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
                                            mediator.explorer.selectAllTreeViewItems(
                                                object : Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            mediator.explorer.selectAllTreeViewItems(
                                                object :
                                                    Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
                                            return@mouseClicked
                                        }
                                    }
                                }
                                if (!mediator.canvas2D.isSelected(j)) {
                                    mediator.canvas2D.addToSelection(j)
                                    mediator.explorer.selectAllTreeViewItems(
                                        object : Explorer.DrawingElementFilter {
                                            override fun isOK(el: DrawingElement?): Boolean {
                                                return mediator.canvas2D.isSelected(el)
                                            }
                                        },
                                        Arrays.asList(mediator.explorer.treeTableView.root),
                                        false,
                                        RNArtist.SCOPE.BRANCH
                                    )
                                    return@mouseClicked
                                } else {
                                    var p = j.parent
                                    while (p != null && mediator.canvas2D.isSelected(p)) {
                                        p = p.parent
                                    }
                                    if (p == null) {
                                        mediator.canvas2D.addToSelection(j)
                                        mediator.explorer.selectAllTreeViewItems(
                                            object : Explorer.DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH
                                        )
                                    } else {
                                        mediator.canvas2D.addToSelection(p)
                                        mediator.explorer.selectAllTreeViewItems(
                                            object : Explorer.DrawingElementFilter {
                                                override fun isOK(el: DrawingElement?): Boolean {
                                                    return mediator.canvas2D.isSelected(el)
                                                }
                                            },
                                            Arrays.asList(mediator.explorer.treeTableView.root),
                                            false,
                                            RNArtist.SCOPE.BRANCH
                                        )
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
                                            mediator.explorer.selectAllTreeViewItems(
                                                object : Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
                                            return@mouseClicked
                                        } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                            mediator.canvas2D.removeFromSelection(r)
                                            mediator.canvas2D.addToSelection(r.parent)
                                            mediator.explorer.selectAllTreeViewItems(
                                                object : Explorer.DrawingElementFilter {
                                                    override fun isOK(el: DrawingElement?): Boolean {
                                                        return mediator.canvas2D.isSelected(el)
                                                    }
                                                },
                                                Arrays.asList(mediator.explorer.treeTableView.root),
                                                false,
                                                RNArtist.SCOPE.BRANCH
                                            )
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

        val status = isDockerInstalled() && isDockerImageInstalled()
        val dockerStatus =
            Label(null, if (status) FontIcon("fas-check-circle:15") else FontIcon("fas-exclamation-circle:15"))
        dockerStatus.tooltip =
            Tooltip(if (status) "I found RNAVIEW! You can open PDB files" else "RNAVIEW not found! You cannot open PDB files!")
        if (status) (dockerStatus.graphic as FontIcon).fill = Color.GREEN else (dockerStatus.graphic as FontIcon).fill =
            Color.RED
        val checkDockerStatus = Timeline(
            KeyFrame(Duration.seconds(30.0),
                {
                    val status = isDockerInstalled() && isDockerImageInstalled()
                    dockerStatus.graphic =
                        if (status) FontIcon("fas-check-circle:15") else FontIcon("fas-exclamation-circle:15")
                    dockerStatus.tooltip =
                        Tooltip(if (status) "I found RNAVIEW! You can open PDB files" else "RNAVIEW not found! You cannot open PDB files!")
                    if (status) (dockerStatus.graphic as FontIcon).fill =
                        Color.GREEN else (dockerStatus.graphic as FontIcon).fill =
                        Color.RED
                })
        )
        checkDockerStatus.cycleCount = Timeline.INDEFINITE
        checkDockerStatus.play()

        statusBar.getChildren().add(dockerStatus)

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

        val settings = Button(null, FontIcon("fas-cog:15"))
        settings.tooltip = Tooltip("Show RNArtist Settings")
        settings.onAction = EventHandler { actionEvent: ActionEvent? ->
            mediator.settings.stage.show()
            mediator.settings.stage.toFront()
        }
        windowsBar.children.add(settings)

        val explorer = Button(null, FontIcon("fas-th-list:15"))
        explorer.tooltip = Tooltip("Show Objects Explorer")
        explorer.onAction = EventHandler { actionEvent: ActionEvent? ->
            mediator.explorer.stage.show()
            mediator.explorer.stage.toFront()
        }
        windowsBar.children.add(explorer)

        val showEditor = Button(null, FontIcon("fas-play:15"))
        showEditor.tooltip = Tooltip("Show Script Editor")
        showEditor.onAction = EventHandler { actionEvent: ActionEvent? ->
            mediator.scriptEditor.stage.show()
            mediator.scriptEditor.stage.toFront()
        }
        windowsBar.children.add(showEditor)

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

        //root.right = RNAGallery(mediator)
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

        this.stage.widthProperty().addListener {
                obs: ObservableValue<out Number?>?, oldVal: Number?, newVal: Number? ->
            //mediator.canvas2D.updateKnobs()
            mediator.canvas2D.repaint()
        }

        this.stage.heightProperty().addListener {
                obs: ObservableValue<out Number?>?, oldVal: Number?, newVal: Number? ->
            //mediator.canvas2D.updateKnobs()
            mediator.canvas2D.repaint()
        }

        this.stage.fullScreenProperty().addListener { obs: ObservableValue<out Boolean?>?, oldVal: Boolean?, newVal: Boolean? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
        }

        this.stage.maximizedProperty().addListener{ obs: ObservableValue<out Boolean?>?, oldVal: Boolean?, newVal: Boolean? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
        }


        val screen = Screen.getPrimary()

        val scene = Scene(this.root, screen.bounds.width, screen.bounds.height)
        stage.scene = scene
        stage.title = "RNArtist"

        val screenSize = Screen.getPrimary().bounds
        val width = (screenSize.width * 0.5).toInt()
        scene.window.width = screenSize.width - width
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
