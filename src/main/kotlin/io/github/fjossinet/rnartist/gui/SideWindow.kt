package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import com.google.gson.JsonParser
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.model.editor.DSLElement
import io.github.fjossinet.rnartist.model.editor.DSLElementInt
import io.github.fjossinet.rnartist.model.editor.OptionalDSLKeyword
import io.github.fjossinet.rnartist.model.editor.OptionalDSLParameter
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.Desktop
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileFilter
import java.io.FileReader
import java.net.URL

class SideWindow(val mediator: Mediator) {

    val stage = Stage()
    val root = BorderPane()
    val tabPane = TabPane()

    init {
        stage.title = "RNArtist Tools"
        createScene(stage)
    }

    private fun createScene(stage: Stage) {

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

        val splitPane = SplitPane(this.mediator.drawingsLoadedPanel, this.mediator.scriptEditor)
        splitPane.orientation = Orientation.VERTICAL
        tabPane.tabs.add(Tab("Working Session", splitPane))
        tabPane.tabs.add(Tab("Projects", this.mediator.projectsPanel))
        tabPane.tabs.add(Tab("Settings", this.mediator.settings))

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