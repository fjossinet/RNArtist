package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import java.awt.geom.Point2D

open class LinearButtonsPanel(val mediator: Mediator, val buttonRadius:Double = 15.0, val buttomsPerRow:Int = 5):  VBox() {

    val group = Group()
    private val buttons = mutableListOf<Button>()
    private val spacingBetweenButtons = 5.0

    init {
        this.padding = Insets(10.0, 10.0, 10.0, 10.0)
        this.spacing = 10.0
        this.alignment =  Pos.CENTER
        this.children.add(this.group)
    }

    fun addButton(button: Button) {
        buttons.add(button)
        val p = if (buttons.size == 1)
                Point2D.Double(buttonRadius+5.0, buttonRadius)
            else
                Point2D.Double(buttons[buttons.size-2].layoutX+3*buttonRadius+10.0+spacingBetweenButtons, buttonRadius)
        var c = Circle(p.x, p.y, buttonRadius+5)
        c.fill = RNArtist.RNArtistGUIColor
        c.strokeWidth = 1.0
        c.stroke = Color.LIGHTGRAY
        this.group.children.add(c)
        c = Circle(p.x, p.y, buttonRadius)
        button.setShape(c)
        button.layoutX = p.x-buttonRadius
        button.layoutY = p.y-buttonRadius
        button.setMinSize(2 * buttonRadius, 2 * buttonRadius)
        button.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
        this.group.children.add(button)

    }

}