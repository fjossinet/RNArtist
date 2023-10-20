package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.RNArtistButton
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.Point2D

class LargeButtonsPanel(
    val buttonRadius: Double = 15.0,
    val buttomsPerRow: Int = 5
) : HBox() {

    val buttons
        get() = this.children.filterIsInstance<RNArtistButton>()

    init {
        this.padding = Insets(0.0, 0.0, 15.0, 0.0)
        this.spacing = 10.0
        this.alignment = Pos.CENTER_LEFT
    }

    fun addButton(icon: String, toolTip: String, onActionEventHandler: EventHandler<ActionEvent>? = null): RNArtistButton {
        val rnArtistButton = RNArtistButton(icon, toolTip, buttonRadius =  buttonRadius, onActionEventHandler = onActionEventHandler)
        rnArtistButton.isDisable = true
        this.children.add(rnArtistButton)
        return rnArtistButton
    }


}