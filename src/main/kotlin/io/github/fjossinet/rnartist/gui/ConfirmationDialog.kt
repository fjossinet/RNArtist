package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.RNArtist.Companion.RNArtistGUIColor
import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.gui.TaskDialog
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistTask
import io.github.fjossinet.rnartist.io.javaFXToAwt
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

class ConfirmationDialog(val mediator: Mediator, message:String, helpDocName: String? = null) {

    val stage = Stage()
    val statusLabel = Label("")
    val rootContent = VBox()
    val content = VBox()
    val messageBox = HBox()
    var isConfirmed = false

    init {
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.initModality(Modality.APPLICATION_MODAL)

        rootContent.background = Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(10.0), Insets(10.0)))
        rootContent.alignment = Pos.TOP_CENTER

        stage.setScene(Scene(rootContent))
        stage.scene.fill = Color.TRANSPARENT
        stage.scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/dialog.css")

        content.background =
            Background(BackgroundFill(RNArtistGUIColor, CornerRadii(10.0), Insets(0.0, 10.0, 10.0, 10.0)))
        content.padding = Insets(10.0,10.0,20.0,10.0)

        this.statusLabel.text = message
        messageBox.padding = Insets(10.0)
        messageBox.spacing = 10.0
        messageBox.children.add(this.statusLabel)
        content.children.add(messageBox)

        rootContent.children.add(content)

        val buttonsPanel = LargeButtonsPanel()
        buttonsPanel.padding = Insets(10.0)
        buttonsPanel.alignment = Pos.CENTER_RIGHT

        content.children.add(buttonsPanel)

        var button = buttonsPanel.addButton("fas-times:15")
        button.isDisable = false
        button.onMouseClicked = EventHandler {
            isConfirmed = false
            stage.close()
        }

        button = buttonsPanel.addButton("fas-check:15")
        button.isDisable = false
        button.onMouseClicked = EventHandler {
            isConfirmed = true
            stage.close()
        }

        helpDocName?.let {
            mediator.webView.engine.load({}.javaClass.getResource("doc/${helpDocName}").toURI().toString())

            val expContent = ScrollPane()
            expContent.maxWidth = 800.0
            expContent.content = mediator.webView
            val tp = TitledPane("More details", expContent)
            tp.isExpanded = false
            tp.heightProperty().addListener { _, _, _ ->
                stage.sizeToScene()
            }
            this.content.children.add(tp)
            stage.sizeToScene()
        }

        stage.showAndWait()
    }


}
