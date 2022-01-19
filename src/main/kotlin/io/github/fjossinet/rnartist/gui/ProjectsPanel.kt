package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.RNArtistTaskWindow
import javafx.application.Platform
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
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import org.kordamp.ikonli.javafx.FontIcon
import java.io.*

class ProjectsPanel(val mediator: Mediator) {
    private val projectPanels: ObservableList<ProjectPanel>
    private val gridview: GridView<ProjectPanel>
    val stage: Stage
    val progressBar = ProgressBar(0.0)

    init {
        val root = BorderPane()
        stage = Stage()
        stage.title = "Your Projects"
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
        gridview.cellWidth = 200.0
        gridview.cellHeight = 200.0
        gridview.style = "-fx-background-color: lightgray;"
        gridview.setCellFactory { ProjectCell() }
        root.center = gridview

        progressBar.prefWidth = Double.MAX_VALUE
        root.bottom = progressBar

        val scene = Scene(root)
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

    fun clearProjects() {
        this.projectPanels.clear()
    }

    fun removeProjectPanel(projectDir: File) {
        this.projectPanels.remove(this.projectPanels.find { it.projectDir.equals(projectDir) })
    }

    fun loadProjects() {
        class ListingProjectsTask : Task<Any?>() {

            init {
                progressBar.progressProperty().unbind()
                progressBar.progressProperty().bind(this.progressProperty())
            }

            override fun call(): Any? {
                val projects = File(RnartistConfig.projectsFolder).listFiles(FileFilter { it.isDirectory })
                var i = 0.0
                for (project in projects) {
                    Platform.runLater {
                        projectPanels.add(ProjectPanel(project))
                        updateProgress(++i, projects.size.toDouble())
                    }
                    Thread.sleep(50)
                }
                return null
            }
        }
        Thread(ListingProjectsTask()).start()

    }

    private inner class ProjectCell : GridCell<ProjectPanel>() {
        private val icon = ImageView()
        private val projectName = Label()
        private val content: VBox
        private val border: HBox
        private val titleBar: HBox

        override fun updateItem(projectPanel: ProjectPanel?, empty: Boolean) {
            super.updateItem(projectPanel, empty)
            graphic = null
            text = null
            if (!empty && projectPanel != null) {
                projectName.text = projectPanel.projectDir.name
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
                    RNArtistTaskWindow(mediator).task = DeleteProject(mediator, item!!.projectDir)
                } else {
                    event.consume()
                }
            }
            icon.onMouseClicked = EventHandler {
                mediator.scriptEditor.currentScriptLocation = item!!.projectDir
                this@ProjectsPanel.stage.hide()
                RNArtistTaskWindow(mediator).task = LoadScript(
                    mediator, script = FileReader(
                        File(
                            item!!.projectDir,
                            "rnartist.kts"
                        )
                    ), runScript = true
                )
            }
            this.onMouseEntered = EventHandler { border.style = "-fx-border-color: darkgray; -fx-border-width: 4px;" }
            this.onMouseExited = EventHandler { border.style = "-fx-border-color: lightgray; -fx-border-width: 4px;" }
        }
    }

    inner private class ProjectPanel(val projectDir: File) {

        val image: Image?
            get() {
                try {
                    return Image(
                        File(
                            projectDir,
                            "preview.png"
                        ).toURI().toURL().toString()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
    }
}