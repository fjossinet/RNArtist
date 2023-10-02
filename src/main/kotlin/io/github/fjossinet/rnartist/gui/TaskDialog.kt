package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.model.RNArtistTask
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.kordamp.ikonli.javafx.FontIcon
import java.io.PrintWriter
import java.io.StringWriter

/**
 * A dialog displayed during a running task and following its progress
 */
class TaskDialog(val mediator: Mediator) {

    val stage = Stage()
    val progressBar = ProgressBar(0.0)
    val statusLabel = Label("")
    val rootContent = VBox()
    val content = VBox()
    val messageBox = HBox()
    var thread:Thread? = null
    var task: RNArtistTask? = null
        set(value) {
            field = value
            field?.rnartistDialog = this
            progressBar.progressProperty().unbind()
            progressBar.progressProperty().bind(field?.progressProperty())
            statusLabel.textProperty().unbind()
            statusLabel.textProperty().bind(field?.messageProperty())
            thread = Thread(field)
            thread?.start()
        }

    init {
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.initModality(Modality.APPLICATION_MODAL)

        rootContent.background = Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(10.0), Insets(10.0)))
        rootContent.alignment = Pos.TOP_CENTER

        stage.setScene(Scene(rootContent))
        stage.scene.fill = Color.TRANSPARENT
        stage.scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/dialog.css")

        content.background =  Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii(10.0), Insets(0.0, 10.0, 10.0, 10.0)))
        content.padding = Insets(10.0)

        val titleBar = HBox()
        titleBar.background =  Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        titleBar.alignment = Pos.CENTER_RIGHT
        titleBar.prefWidth = 800.0
        titleBar.padding = Insets(0.0, 10.0, 0.0, 10.0)
        val icon = FontIcon("fas-times:15")
        icon.iconColor = Color.WHITE
        titleBar.children.add(icon)

        icon.onMouseClicked = EventHandler {
            task?.cancel()
            stage.close()
        }

        content.children.add(titleBar)

        messageBox.padding = Insets(10.0)
        messageBox.spacing = 10.0
        messageBox.children.add(this.statusLabel)
        content.children.add(messageBox)

        this.progressBar.padding = Insets(10.0)
        this.progressBar.prefWidth = 800.0
        content.children.add(this.progressBar)
        rootContent.children.add(content)

        stage.show()
    }

    fun displayException(e:Exception) {
        this.task?.let {
            content.children.remove(this.progressBar)
        }
        val message = Label("Oups! RNArtist was not able to complete this task.")
        message.textFill = Color.WHITE
        messageBox.children.clear()
        messageBox.children.add(message)
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        val exceptionText = sw.toString()

        val textArea = TextArea(exceptionText)
        textArea.isEditable = false
        textArea.isWrapText = true
        textArea.maxWidth = 800.0
        textArea.maxHeight = Double.MAX_VALUE

        val tp = TitledPane("More details", textArea)
        tp.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        tp.padding = Insets(10.0)
        tp.isExpanded = false
        tp.heightProperty().addListener { _,_,_ ->
            stage.sizeToScene()
        }
        this.content.children.add(tp)
        stage.sizeToScene()
    }

}