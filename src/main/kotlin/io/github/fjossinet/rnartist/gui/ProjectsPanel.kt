package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.model.DrawingLoadedFromRNArtistDB
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.apache.commons.lang3.tuple.Pair
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import org.dizitart.no2.NitriteId
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ExecutionException
import javax.imageio.ImageIO

class ProjectsPanel(val mediator:Mediator) {
    private val projectPanels: ObservableList<ProjectPanel>
    private val gridview: GridView<ProjectPanel>
    val stage: Stage

    init {
        stage = Stage()
        stage.title = "Saved Projects"
        stage.onCloseRequest = EventHandler { windowEvent: WindowEvent? ->
            if (mediator.drawingDisplayed.isNotNull
                    .get()
            ) { //the user has decided to cancel its idea to open another project
                mediator.rnartist.stage.show()
                mediator.rnartist.stage.toFront()
            }
        }
        projectPanels = FXCollections.observableArrayList()
        gridview = GridView(projectPanels)
        gridview.padding = Insets(20.0, 20.0, 20.0, 20.0)
        gridview.horizontalCellSpacing = 20.0
        gridview.verticalCellSpacing = 20.0
        gridview.cellWidth = 400.0
        gridview.cellHeight = 200.0
        gridview.style = "-fx-background-color: lightgray;"
        gridview.setCellFactory { ProjectCell() }
        val scene = Scene(gridview)
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        stage.width = 1360.0
        stage.height = 2 * screenSize.height / 3
        val newX = (screenSize.width - stage.width) / 2
        val newY = (screenSize.height - stage.height) / 2
        stage.x = newX
        stage.y = newY
    }

    fun loadProjects() {
        projectPanels.clear()
        for (project in this.mediator.embeddedDB.getProjects().find()) {
            projectPanels.add(ProjectPanel(project.id, project["name"] as String))
        }
    }

    @Throws(IOException::class)
    fun saveProjectAs(name: String, image: BufferedImage): NitriteId? {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val id = mediator.embeddedDB.saveProjectAs(name, drawing)
            val pngFile = File(File(File(mediator.embeddedDB.rootDir, "images"), "user"), "$id.png")
            ImageIO.write(image, "PNG", pngFile)
            val chimera_sessions = File(mediator.embeddedDB.rootDir, "chimera_sessions")
            if (!chimera_sessions.exists()) chimera_sessions.mkdir()
            mediator.chimeraDriver.saveSession(File(chimera_sessions, id.toString()), File(chimera_sessions, "$id.pdb"))
            return id
        }
        return null
    }

    @Throws(IOException::class)
    fun updateProject(name: String, image: BufferedImage) {
        mediator.drawingDisplayed.get()?.let { drawingDisplayed ->
            (drawingDisplayed as? DrawingLoadedFromRNArtistDB)?.let { drawingDisplayed ->
                mediator.embeddedDB.updateProject(name, drawingDisplayed.id,
                    drawingDisplayed.drawing)
                val pngFile = File(File(File(mediator.embeddedDB.rootDir, "images"), "user"), "${drawingDisplayed.id}.png")
                ImageIO.write(image, "PNG", pngFile)
                val chimera_sessions = File(mediator.embeddedDB.rootDir, "chimera_sessions")
                mediator.chimeraDriver.saveSession(File(chimera_sessions,
                    drawingDisplayed.id.toString()),
                    File(chimera_sessions,
                        drawingDisplayed.id.toString() + ".pdb"))
            }
        }
    }

    private inner class ProjectCell : GridCell<ProjectPanel>() {
        private val icon = ImageView()
        private val projectName = Label()
        private val content: VBox
        private val border: HBox
        private val titleBar: HBox
        protected override fun updateItem(projectPanel: ProjectPanel?, empty: Boolean) {
            super.updateItem(projectPanel, empty)
            graphic = null
            text = null
            if (!empty && projectPanel != null) {
                projectName.text = projectPanel.name
                icon.image = projectPanel.image
                graphic = content
            }
        }

        init {
            content = VBox()
            content.spacing = 5.0
            content.alignment = Pos.CENTER
            this.border = HBox()
            this.border.children.add(icon)
            this.border.style = "-fx-border-color: lightgray; -fx-border-width: 4px;"
            content.children.add(border)
            titleBar = HBox()
            titleBar.spacing = 5.0
            titleBar.alignment = Pos.CENTER
            titleBar.children.add(projectName)
            val deleteProject = Label(null, FontIcon("fas-trash:15"))
            (deleteProject.graphic as FontIcon).fill = Color.BLACK
            titleBar.children.add(deleteProject)
            content.children.add(titleBar)
            projectName.textFill = Color.BLACK
            projectName.style = "-fx-font-weight: bold"
            deleteProject.onMouseClicked = EventHandler { event ->
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.initOwner(this@ProjectsPanel.stage)
                alert.initModality(Modality.WINDOW_MODAL)
                alert.title = "Confirm Deletion"
                alert.headerText = null
                alert.contentText = "Are you sure to delete this project?"
                val alerttStage = alert.dialogPane.scene.window as Stage
                alerttStage.isAlwaysOnTop = true
                alerttStage.toFront()
                val result = alert.showAndWait()
                if (result.get() == ButtonType.OK) {
                    val deleteProject: Task<Exception?> = object : Task<Exception?>() {
                        override fun call(): Exception? {
                            return try {
                                mediator.embeddedDB.removeProject(item!!.id!!)
                                null
                            } catch (e: Exception) {
                                e
                            }
                        }
                    }
                    deleteProject.onSucceeded = EventHandler {
                        try {
                            if (deleteProject.get() != null) {
                                val alert = Alert(Alert.AlertType.ERROR)
                                alert.title = "Project deletion error"
                                alert.headerText = deleteProject.get()!!.message
                                alert.contentText =
                                    "If this problem persists, you can send the exception stacktrace below to fjossinet@gmail.com"
                                val sw = StringWriter()
                                val pw = PrintWriter(sw)
                                deleteProject.get()!!.printStackTrace(pw)
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
                            } else {
                                loadProjects()
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        }
                    }
                    Thread(deleteProject).start()
                } else {
                    event.consume()
                }
            }
            icon.onMouseClicked = EventHandler {
                val loadData: Task<Pair<SecondaryStructureDrawing?, Exception?>> =
                    object : Task<Pair<SecondaryStructureDrawing?, Exception?>>() {
                        override fun call(): Pair<SecondaryStructureDrawing?, Exception?> {
                            return try {
                                Pair.of(mediator.embeddedDB.getProject(
                                    item!!.id!!), null)
                            } catch (e: Exception) {
                                Pair.of(null, e)
                            }
                        }
                    }
                loadData.onSucceeded = EventHandler {
                    try {
                        loadData.get().right?.let { exception ->
                            val alert = Alert(Alert.AlertType.ERROR)
                            alert.title = "Project loading error"
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
                        loadData.get().left?.let { result ->
                            stage.hide()
                            mediator.editor.stage.show()
                            mediator.rnartist.stage.show()
                            mediator.rnartist.stage.toFront()
                            mediator.drawingsLoaded.add(DrawingLoadedFromRNArtistDB(mediator, result,
                                item!!.id!!, item!!.name!!))
                            mediator.drawingDisplayed.set(mediator.drawingsLoaded[mediator.drawingsLoaded.size - 1])
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Thread(loadData).start()
            }
            this.onMouseEntered = EventHandler { border.style = "-fx-border-color: darkgray; -fx-border-width: 4px;" }
            this.onMouseExited = EventHandler { border.style = "-fx-border-color: lightgray; -fx-border-width: 4px;" }
        }
    }

    inner private class ProjectPanel {
        var id: NitriteId? = null
        var name: String? = null

        constructor(id: NitriteId?, name: String?) {
            this.id = id
            this.name = name
        }

        val image: Image?
            get() {
                try {
                    return Image(File(File(File(mediator.embeddedDB.rootDir, "images"), "user"),
                        id.toString() + ".png").toURI().toURL().toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
    }
}