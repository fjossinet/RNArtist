package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import javafx.concurrent.Task
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
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

        stage.setScene(Scene(form))
        //stage.scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")

        val im = ImageView()
        im.image = Image("/io/github/fjossinet/rnartist/io/images/logo.png")
        form.add(im, 0, 0, 3, 1)
        val progressBar = ProgressBar(0.0)
        progressBar.prefWidthProperty().bind(form.widthProperty().subtract(20))
        form.add(progressBar, 0, 1, 3, 1)
        val statusLabel = Label("")
        statusLabel.textFill = Color.BLACK
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
                stage.hide()
                result?.let { exception ->
                    ExceptionDialog(mediator, exception)
                }
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

class ExceptionDialog(val mediator: Mediator, exception: Exception): Alert(AlertType.ERROR) {

    init {
        this.initModality(Modality.WINDOW_MODAL)
        this.initStyle(StageStyle.TRANSPARENT)
        this.initOwner(mediator.rnartist.stage)
        this.dialogPane.scene.fill = Color.TRANSPARENT

        this.dialogPane.padding = Insets(10.0)
        this.dialogPane.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii(10.0), Insets(10.0)))

        val content = VBox()
        content.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii(10.0), Insets(10.0)))
        content.padding = Insets(10.0)
        val l = Label("RNartist got a problem.")
        l.textFill = Color.WHITE
        content.children.add(l)

        this.dialogPane.header = content

        this.contentText =
            "You can send the exception stacktrace below to fjossinet@gmail.com"

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val exceptionText = sw.toString()

        val textArea = TextArea(exceptionText)
        textArea.isEditable = false
        textArea.isWrapText = true

        textArea.maxWidth = 800.0
        textArea.maxHeight = Double.MAX_VALUE
        GridPane.setVgrow(textArea, Priority.ALWAYS)
        GridPane.setHgrow(textArea, Priority.ALWAYS)

        val expContent = GridPane()
        expContent.maxWidth = 800.0
        expContent.add(textArea, 0, 0)
        this.dialogPane.expandableContent = expContent
        this.showAndWait()
    }
}