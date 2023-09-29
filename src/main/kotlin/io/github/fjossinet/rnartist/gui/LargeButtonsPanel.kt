package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.RNArtist
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.Point2D

class LargeButtonsPanel(
    val buttonRadius: Double = 15.0,
    val buttomsPerRow: Int = 5
) : VBox() {

    val group = Group()
    val buttons = mutableListOf<Button>()

    init {
        this.padding = Insets.EMPTY
        this.spacing = 10.0
        this.alignment = Pos.CENTER_LEFT
        this.children.add(this.group)
    }

    fun addSeparator() {
        val separator = Separator()
        buttons.add(separator)
        val p = Point2D.Double(buttons[buttons.size - 2].layoutX + 2.75 * buttonRadius, buttonRadius)
        val c = Circle(p.x, p.y, buttonRadius)
        separator.setShape(c)
        separator.layoutX = p.x - buttonRadius
        separator.layoutY = p.y - buttonRadius
        separator.setMinSize(2 * buttonRadius, 2 * buttonRadius)
        separator.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
        this.group.children.add(separator)
    }

    fun addButton(icon: String, toolTip: String? = null): Button {
        val button = Button(null, FontIcon(icon))
        toolTip?.let {
            button.tooltip = Tooltip(it)
        }
        button.background = null
        button.isDisable = true
        (button.graphic as FontIcon).iconColor = Color.WHITE
        buttons.add(button)
        val p = if (buttons.size == 1)
            Point2D.Double(buttonRadius + 2.75 * buttonRadius, buttonRadius)
        else {
            (buttons[buttons.size - 2] as? Separator)?.let {
                Point2D.Double(buttons[buttons.size - 2].layoutX + 2.75 * buttonRadius, buttonRadius)
            } ?: run {
                Point2D.Double(buttons[buttons.size - 2].layoutX + 3.75 * buttonRadius, buttonRadius)
            }
        }
        val c = Circle(p.x, p.y, buttonRadius)
        c.fill = Color.TRANSPARENT
        c.strokeWidth = 1.0
        c.stroke = if (button.isDisable) Color.DARKGRAY else Color.WHITE
        button.disableProperty().addListener { _, oldValue, newValue ->
            c.stroke = if (newValue) Color.DARKGRAY else Color.WHITE
        }
        this.group.children.add(c)
        val buttonShape = Circle(p.x, p.y, buttonRadius)
        button.setShape(buttonShape)
        button.layoutX = p.x - buttonRadius
        button.layoutY = p.y - buttonRadius
        button.setMinSize(2 * buttonRadius, 2 * buttonRadius)
        button.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
        this.group.children.add(button)

        button.onMouseEntered = EventHandler {
            button.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
            (button.graphic as FontIcon).iconColor = Color.BLACK
        }
        button.onMouseExited = EventHandler {
            button.background = null
            (button.graphic as FontIcon).iconColor = Color.WHITE
        }
        button.onMousePressed = EventHandler {
            button.background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
            (button.graphic as FontIcon).iconColor = Color.BLACK
        }
        button.onMouseReleased = EventHandler {
            button.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY))
            (button.graphic as FontIcon).iconColor = Color.BLACK
        }

        return button
    }

    fun addButtonWithPin(icon: String, pinIcon: String, toolTip: String? = null): Button {
        val button = this.addButton(icon, toolTip)
        val pin = Button(null, FontIcon(pinIcon))
        pin.background = null
        (pin.graphic as FontIcon).iconColor = if (button.isDisable) Color.DARKGRAY else Color.WHITE
        button.disableProperty().addListener { _, oldValue, newValue ->
            (pin.graphic as FontIcon).iconColor = if (button.isDisable) Color.DARKGRAY else Color.WHITE
        }
        val c = Circle(
            button.layoutX - buttonRadius / 2.0 + buttonRadius / 2.0 + 0.5,
            button.layoutY - buttonRadius / 2.0 + buttonRadius / 2.0 + 0.5,
            buttonRadius / 2.0 + 3.0
        )
        c.fill = RNArtist.RNArtistGUIColor
        c.strokeWidth = 1.0
        c.stroke = if (button.isDisable) Color.DARKGRAY else Color.WHITE
        button.disableProperty().addListener { _, oldValue, newValue ->
            c.stroke = if (button.isDisable) Color.DARKGRAY else Color.WHITE
        }
        group.children.add(c)
        val pinShape = Circle(0.0, 0.0, buttonRadius / 2.0)
        pin.setShape(pinShape)
        pin.layoutX = button.layoutX - buttonRadius / 2.0
        pin.layoutY = button.layoutY - buttonRadius / 2.0
        pin.setMinSize(2 * buttonRadius / 2.0, 2 * buttonRadius / 2.0)
        pin.setMaxSize(2 * buttonRadius / 2.0, 2 * buttonRadius / 2.0)
        group.children.add(pin)
        return button
    }

    private inner class Separator : Button(null, FontIcon("fas-ellipsis-v:15")) {
        init {
            this.background = null
            (this.graphic as FontIcon).iconColor = Color.WHITE
        }
    }

}