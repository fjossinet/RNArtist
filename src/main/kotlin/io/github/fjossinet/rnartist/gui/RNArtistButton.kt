package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.effect.InnerShadow
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import org.kordamp.ikonli.javafx.FontIcon

open class RNArtistButton(icon: String, toolTip: String, clickable: Boolean = true, iconColor: Color = Color.WHITE, buttonRadius:Double = 12.0, isClickedColor: Color? = null, onActionEventHandler: EventHandler<ActionEvent>? = null): Group() {
    var isClicked: Boolean = false
    val button = Button(null, FontIcon(icon))
    init {
        with(this.button) {
            this.background = null
            this.tooltip = Tooltip(toolTip)
            (this.graphic as FontIcon).iconColor = iconColor
            val c = Circle(0.0, 0.0, buttonRadius)
            c.fill = Color.TRANSPARENT
            c.strokeWidth = 0.5
            c.stroke = if (this.isDisable) Color.DARKGRAY else Color.WHITE
            this.disableProperty().addListener { _, oldValue, newValue ->
                c.stroke = if (newValue) Color.DARKGRAY else Color.WHITE
            }
            this@RNArtistButton.children.add(c)
            this.setShape(c)
            button.layoutX = - buttonRadius
            button.layoutY = - buttonRadius
            this.setMinSize(2 * buttonRadius, 2 * buttonRadius)
            this.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
            if (clickable) {
                this.onMouseEntered = EventHandler {
                    this.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (this.graphic as FontIcon).iconColor = Color.BLACK
                }
                this.onMouseExited = EventHandler {
                    (this.graphic as FontIcon).iconColor = iconColor
                    isClickedColor?.let {
                        if (isClicked) {
                            this.background =
                                Background(BackgroundFill(isClickedColor, CornerRadii.EMPTY, Insets.EMPTY))
                            val dropShadow = InnerShadow()
                            dropShadow.offsetX = 0.0
                            dropShadow.offsetY = 0.0
                            dropShadow.color = Color.LIGHTGRAY
                            this.effect = dropShadow
                        } else {
                            this.background = null
                            this.effect = null
                        }
                    } ?: run {
                        this.background = null
                    }
                }
                this.onMousePressed = EventHandler {
                    this.background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (this.graphic as FontIcon).iconColor = Color.BLACK
                }
                this.onMouseReleased = EventHandler {
                    this.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
                    (this.graphic as FontIcon).iconColor = Color.BLACK
                }
                this.onMouseClicked = EventHandler {
                    isClicked = !isClicked
                    isClickedColor?.let {
                        when (isClicked) {
                            true -> {
                                (this.graphic as FontIcon).iconColor = isClickedColor
                            }

                            false -> {
                                (this.graphic as FontIcon).iconColor = iconColor
                            }
                        }
                    }
                }
                onActionEventHandler?.let {
                    this.onAction = it
                }

            }
        }
        this.children.add(this.button)
    }
}