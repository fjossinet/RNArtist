package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.RnartistConfig
import javafx.concurrent.Task
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL

class SplashWindow(val mediator: Mediator) {

    val stage = Stage()

    init {
        stage.initStyle(StageStyle.UNDECORATED);
        val form = GridPane()
        val constraints = ColumnConstraints()
        constraints.hgrow = Priority.ALWAYS
        form.getColumnConstraints().addAll(constraints)

        form.hgap = 10.0
        form.vgap = 10.0
        form.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val im = ImageView()
        im.image = Image("/io/github/fjossinet/rnartist/io/images/logo.png")
        form.add(im, 0, 0)
        val progressBar = ProgressBar(0.0)
        progressBar.prefWidthProperty().bind(form.widthProperty().subtract(20))
        form.add(progressBar, 0, 1)
        val statusLabel = Label("")
        statusLabel.prefWidthProperty().bind(form.widthProperty().subtract(20))
        form.add(statusLabel, 0, 2)

        stage.setScene(Scene(form));
        stage.show()
        stage.toFront()

        val task = CheckRNAGallery()
        task.setOnSucceeded { event ->
            val result = task.get()
            result.first?.let {
                mediator.rnaGallery = it
                stage.hide()
                mediator.rnartist.stage.show()
                mediator.rnartist.stage.toFront()
                mediator.editor.stage.show()
            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Problem to connect to RNA Gallery"
                alert.headerText = "RNartist cannot reach the RNA Gallery website."
                alert.contentText =
                    "You can still use RNArtist. If this problem persists, you can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
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
                stage.hide()
                mediator.rnartist.stage.show()
                mediator.rnartist.stage.toFront()
                mediator.editor.stage.show()
            }
        }
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().unbind();
        statusLabel.textProperty().bind(task.messageProperty());
        Thread(task).start();
    }

    class CheckRNAGallery : Task<Pair<Map<String, List<String>>?, Exception?>>() {
        override fun call(): Pair<Map<String, List<String>>?, Exception?> {
            try {
                updateMessage("RNA Gallery Connection...")
                val projects = mutableMapOf<String, MutableList<String>>()
                    val text =
                        (if (RnartistConfig.useOnlineRNAGallery) URL("https://raw.githubusercontent.com/fjossinet/RNAGallery/main/PDB/status.md") else File(
                            "${RnartistConfig.rnaGalleryPath}/PDB/status.md"
                        ).toURI().toURL()).readText()
                    val lines = text.split(Regex("\\|\\[[^V]"))
                    var step = 0
                    updateProgress(step.toDouble(), lines.size.toDouble())
                    lines.forEach { line ->
                        val tokens = line.split('|')
                        if (tokens.size == 6) {
                            Thread.sleep(1)
                            step++
                            val pdbId = tokens.first().substring(tokens.first().length - 5, tokens.first().length - 1)
                                .toString()
                            if (projects.containsKey(pdbId))
                                projects[pdbId]!!.add(tokens[1])
                            else {
                                projects[pdbId] = mutableListOf(tokens[1])
                            }
                            updateMessage("RNA Gallery: found pre-computed drawing for chain ${tokens[1]} in entry ${pdbId} ")
                            updateProgress(step.toDouble(), lines.size.toDouble())
                        }
                    }
                    return Pair(projects, null)
                } catch (e: Exception) {
                    return Pair(null, e)
                }
            }
        }
}