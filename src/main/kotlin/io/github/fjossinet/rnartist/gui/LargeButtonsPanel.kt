package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.RNArtistButton
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.Point2D

class LargeButtonsPanel(
    orientation:Orientation = Orientation.HORIZONTAL,
    alignment:Pos = Pos.CENTER_LEFT,
    padding:Insets = Insets(0.0, 0.0, 15.0, 0.0),
    val buttonRadius: Double = 15.0,
    val buttomsPerRow: Int = 5
) : Pane() {

    val content: Pane
    val buttons
        get() = this.content.children.filterIsInstance<RNArtistButton>()

    init {
        if (orientation == Orientation.HORIZONTAL) {
            content = HBox()
            content.spacing = 10.0
            content.alignment = alignment
        } else {
            content = VBox()
            content.spacing = 10.0
            content.alignment = alignment
        }
        content.padding = padding
        this.children.add(content)
        this.minHeightProperty().bind(content.heightProperty())
        this.minWidthProperty().bind(content.widthProperty())
    }

    fun addButton(icon: String, toolTip: String, disabled:Boolean = true, onActionEventHandler: EventHandler<ActionEvent>? = null): RNArtistButton {
        val rnArtistButton = RNArtistButton(icon, toolTip, buttonRadius =  buttonRadius, onActionEventHandler = onActionEventHandler)
        rnArtistButton.isDisable = disabled
        content.children.add(rnArtistButton)
        return rnArtistButton
    }

    fun add(node:Node) = content.children.add(node)


}