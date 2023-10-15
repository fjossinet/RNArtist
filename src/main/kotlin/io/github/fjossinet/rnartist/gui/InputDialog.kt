package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
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

class InputDialog(val mediator: Mediator, message:String, helpDocName: String? = null) {

    val stage = Stage()
    val statusLabel = Label("")
    val rootContent = VBox()
    val content = VBox()
    val messageBox = HBox()
    val input = TextField("")

    init {
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.initModality(Modality.APPLICATION_MODAL)

        rootContent.background = Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(10.0), Insets(10.0)))
        rootContent.alignment = Pos.TOP_CENTER

        stage.setScene(Scene(rootContent))
        stage.scene.fill = Color.TRANSPARENT
        stage.scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/dialog.css")

        content.alignment = Pos.CENTER_LEFT
        content.background =
            Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii(10.0), Insets(0.0, 10.0, 10.0, 10.0)))
        content.padding = Insets(10.0,10.0,20.0,10.0)

        this.statusLabel.text = message
        messageBox.alignment = Pos.CENTER_LEFT
        messageBox.padding = Insets(10.0)
        messageBox.spacing = 10.0
        messageBox.children.add(this.statusLabel)
        messageBox.children.add(input)
        HBox.setHgrow(this.statusLabel, Priority.ALWAYS)
        content.children.add(messageBox)

        rootContent.children.add(content)

        val buttonsPanel = LargeButtonsPanel()
        buttonsPanel.padding = Insets(10.0)
        buttonsPanel.alignment = Pos.CENTER_RIGHT

        content.children.add(buttonsPanel)

        var button = buttonsPanel.addButton("fas-times:15") {
            input.text = ""
            stage.close()
        }
        button.isDisable = false

        button = buttonsPanel.addButton("fas-check:15") {
            stage.close()
        }
        button.isDisable = false

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