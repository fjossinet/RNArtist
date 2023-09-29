package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.effect.InnerShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.Point2D

class HelpDialog(val mediator: Mediator, message:String, docPage: String? = null) {

    val stage = Stage()
    val rootContent = VBox()
    val content = VBox()
    var initX = 0.0
    var initY = 0.0

    init {
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.isResizable = false
        stage.initModality(Modality.APPLICATION_MODAL)

        rootContent.background = Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(10.0), Insets(10.0)))
        rootContent.alignment = Pos.TOP_CENTER

        stage.setScene(Scene(rootContent))
        stage.scene.fill = Color.TRANSPARENT
        stage.scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/dialog.css")

        content.background =  Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii(10.0), Insets(0.0, 10.0, 10.0, 10.0)))
        content.padding = Insets(10.0)
        content.prefHeight = 100.0

        val titleBar = HBox()
        titleBar.prefWidth = 800.0
        titleBar.background =  Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii(10.0), Insets.EMPTY))
        titleBar.alignment = Pos.CENTER_RIGHT
        titleBar.padding = Insets(0.0, 10.0, 0.0, 10.0)
        val closeIcon = createButton("fas-times:15")
        titleBar.children.add(closeIcon)

        titleBar.onMousePressed = EventHandler {
            this.initX = it.x
            this.initY = it.y
        }

        titleBar.onMouseReleased = EventHandler {
            this.initX = 0.0
            this.initY = 0.0
        }

        titleBar.onMouseDragged = EventHandler {
            this.stage.x += it.x - this.initX
            this.stage.y += it.y - this.initY
        }

        closeIcon.onMouseClicked = EventHandler {
            stage.close()
        }

        content.children.add(titleBar)

        var label = Label(message)
        label.textFill = Color.WHITE
        content.children.add(label)
        label.padding = Insets(10.0, 10.0, 10.0, 10.0)

        docPage?.let {
            val moreDetailsBar = HBox()
            moreDetailsBar.spacing = 5.0
            moreDetailsBar.background =
                Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii(10.0), Insets.EMPTY))
            moreDetailsBar.alignment = Pos.CENTER_LEFT
            moreDetailsBar.padding = Insets(10.0, 10.0, 10.0, 10.0)
            label = Label("More details")
            label.textFill = Color.WHITE
            moreDetailsBar.children.add(label)
            val documentationIcon = createButton("fas-book:15")
            documentationIcon.onMouseClicked = EventHandler {
                stage.close()
                mediator.rnartist.displayDocPage(docPage)
            }

            moreDetailsBar.children.add(documentationIcon)
            content.children.add(moreDetailsBar)
        }

        rootContent.children.add(content)
        stage.showAndWait()
    }

    fun createButton(icon: String): Button {
        val button = Button(null, FontIcon(icon))
        button.background = null
        (button.graphic as FontIcon).iconColor = Color.WHITE
        button.onMouseEntered = EventHandler {
                button.background =
                    Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                (button.graphic as FontIcon).iconColor = Color.BLACK
        }
        button.onMouseExited = EventHandler {
                button.background =
                    Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
                (button.graphic as FontIcon).iconColor = Color.WHITE
        }
        val c = Circle(0.0, 0.0, 15.0)
        button.setShape(c)
        button.setMinSize(2 * 15.0, 2 * 15.0)
        button.setMaxSize(2 * 15.0, 2 * 15.0)
        return button
    }

}