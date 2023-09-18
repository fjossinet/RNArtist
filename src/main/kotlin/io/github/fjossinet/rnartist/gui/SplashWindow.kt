package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File
import java.io.FileFilter
import java.io.PrintWriter
import java.io.StringWriter

class SplashWindow(val mediator: Mediator) {

    val stage = Stage()

    init {
        stage.initStyle(StageStyle.UNDECORATED)

        val form = GridPane()
        val constraints = ColumnConstraints()
        constraints.hgrow = Priority.ALWAYS
        form.getColumnConstraints().addAll(constraints)

        form.hgap = 10.0
        form.vgap = 10.0
        form.padding = Insets(10.0, 10.0, 10.0, 10.0)

        stage.setScene(Scene(form));

        val im = ImageView()
        im.image = Image("/io/github/fjossinet/rnartist/io/images/logo.png")
        form.add(im, 0, 0, 3, 1)
        val progressBar = ProgressBar(0.0)
        progressBar.prefWidthProperty().bind(form.widthProperty().subtract(20))
        form.add(progressBar, 0, 1, 3, 1)
        val statusLabel = Label("")
        statusLabel.prefWidthProperty().bind(form.widthProperty().subtract(20))
        form.add(statusLabel, 0, 2, 3, 1)
        form.layout()
        val task = WarmUp(mediator, stage)
        stage.show()
        progressBar.progressProperty().unbind()
        progressBar.progressProperty().bind(task.progressProperty())
        statusLabel.textProperty().unbind()
        statusLabel.textProperty().bind(task.messageProperty())
        Thread(task).start()

    }

    class WarmUp(val mediator: Mediator, val stage: Stage) : Task<Exception?>() {

        init {
            setOnSucceeded { event ->
                val result = get()
                result?.let {
                    stage.hide()
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "I got a problem"
                    alert.headerText = "RNartist got a problem during startup ."
                    alert.contentText =
                        "You can send the exception stacktrace below to fjossinet@gmail.com"
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
                }
                stage.hide()

                mediator.rnartist.stage.show()
                mediator.rnartist.stage.toFront()
            }
        }

        override fun call(): Exception? {
            try {
                updateMessage("Checking configuration...")
                Thread.sleep(2000)
                updateMessage("Warm up scripting engine...")
                (mediator.scriptEngine.eval("1") as? Int)?.let {
                    updateMessage("Scripting engine ready...")
                    Thread.sleep(2000)
                }
                return null
            } catch (e: Exception) {
                return e
            }
        }
    }
}