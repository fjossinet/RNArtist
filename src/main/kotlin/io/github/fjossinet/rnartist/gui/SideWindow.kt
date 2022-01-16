package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.SaveProject
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import org.kordamp.ikonli.javafx.FontIcon
import java.io.File
import java.io.FileFilter

class SideWindow(val mediator: Mediator) {

    val stage = Stage()
    val root = BorderPane()
    val tabPane = TabPane()

    init {
        stage.title = "RNArtist Tools"
        createScene(stage)
    }

    private fun createScene(stage: Stage) {
        val toolbar = ToolBar()
        toolbar.padding = Insets(10.0, 10.0, 10.0, 10.0)

        toolbar.items.add(Label("Projects"))

        val loadProject = Button(null, FontIcon("fas-grip-horizontal:15"))
        loadProject.onMouseClicked = EventHandler {
            mediator.projectsPanel.stage.show()
            mediator.projectsPanel.stage.toFront()
            mediator.projectsPanel.loadProjects()
        }
        loadProject.tooltip = Tooltip("Load Project")
        toolbar.items.add(loadProject)

        val saveProjectAs = Button(null, FontIcon("fas-database:15"))
        saveProjectAs.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        saveProjectAs.onMouseClicked = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                val dialog = TextInputDialog("Project ${File(RnartistConfig.projectsFolder).listFiles(FileFilter { it.isDirectory }).size+1}")
                dialog.initModality(Modality.NONE)
                dialog.title = "Save Project"
                dialog.headerText = null
                dialog.contentText = "Project name:"
                var projectName = dialog.showAndWait()
                while (projectName.isPresent && !projectName.isEmpty && File(File(RnartistConfig.projectsFolder), projectName.get().trim()).exists()) {
                    if (File(File(RnartistConfig.projectsFolder), projectName.get().trim()).exists())
                        dialog.headerText = "This project already exists"
                    projectName = dialog.showAndWait()
                }
                if (projectName.isPresent && !projectName.isEmpty)
                    RNArtistTaskWindow(mediator).task = SaveProject(mediator, File(File(RnartistConfig.projectsFolder), projectName.get()))
            }
        }
        saveProjectAs.tooltip = Tooltip("Save Project As...")
        toolbar.items.add(saveProjectAs)

        val saveProject = Button(null, FontIcon("fas-sync:15"))

        saveProject.setOnMouseClicked {
            mediator.scriptEditor.currentScriptLocation?.let { projectDir ->
                RnartistConfig.projectsFolder?.let {
                    if (projectDir.absolutePath.startsWith(it))
                        RNArtistTaskWindow(mediator).task = SaveProject(mediator, projectDir)
                }
            }
        }

        saveProject.setTooltip(Tooltip("Save Project"))
        saveProject.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))

        toolbar.items.add(saveProject)

        /*val submitProject = Button(null, FontIcon("fas-database:15"))
        submitProject.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        submitProject.onMouseClicked = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
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
                    mediator.canvas2D.repaint()
                }
            }
        }
        submitProject.tooltip = Tooltip("Submit project to RNArtist Gallery")
        GridPane.setConstraints(submitProject, 3, 1)
        saveFiles.children.add(submitProject)*/

        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabPane.tabs.add(Tab("Script Editor", this.mediator.scriptEditor))
        tabPane.tabs.add(Tab("2Ds loaded", this.mediator.drawingsLoadedPanel))
        tabPane.tabs.add(Tab("Preferences", this.mediator.settings))

        //root.top = toolbar
        root.center = tabPane

        val scene = Scene(root)
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        val width = (screenSize.width * 0.5).toInt()
        scene.window.width = width.toDouble()
        scene.window.height = screenSize.height
        scene.window.x = screenSize.width - width
        scene.window.y = 0.0
    }


}