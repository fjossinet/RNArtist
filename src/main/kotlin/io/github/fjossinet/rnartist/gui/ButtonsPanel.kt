package io.github.fjossinet.rnartist.gui
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.model.rotatePoint
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Arc
import javafx.scene.shape.Circle
import java.awt.geom.Point2D
import kotlin.math.min

open class ButtonsPanel(val mediator: Mediator, val panelRadius:Double = 50.0, val buttonRadius:Double = 15.0, val buttomsPerRow:Int = 5):  VBox() {

    var panelCenterX = 50.0
    var panelCenterY = 50.0
    var startAngle = 90.0
    val group = Group()
    private val buttons = mutableListOf<Button>()

    init {
        this.padding = Insets(10.0, 10.0, 20.0, 10.0)
        this.alignment =  Pos.CENTER_LEFT
        this.spacing = 7.5
        this.children.add(this.group)
    }

    fun addButton(button:Button) {
        buttons.add(button)
        this.group.children.clear()
        var a = Arc(this.panelCenterX, this.panelCenterY + this.panelRadius, this.panelRadius, this.panelRadius, 0.0, 180.0)
        a.fill = Color.TRANSPARENT
        a.strokeWidth = 2.0
        a.stroke = Color.LIGHTGRAY
        a.layoutY = -this.panelRadius
        this.group.children.add(a)
        var row = buttons.subList(0, min(buttons.size, buttomsPerRow))
        for (i in 0 until row.size) {
            val p = rotatePoint(
                Point2D.Double(this.panelCenterX, this.panelCenterY + this.panelRadius),
                Point2D.Double(this.panelCenterX, this.panelCenterY),
                i * 180.0/(row.size.toDouble()-1.0) + startAngle
            )
            var c = Circle(p.x, p.y, buttonRadius+5)
            c.fill = RNArtist.RNArtistGUIColor
            c.strokeWidth = 1.0
            c.stroke = Color.LIGHTGRAY
            this.group.children.add(c)
            c = Circle(p.x, p.y, buttonRadius)
            row[i].setShape(c)
            row[i].layoutX = p.x-buttonRadius
            row[i].layoutY = p.y-buttonRadius
            row[i].setMinSize(2 * buttonRadius, 2 * buttonRadius)
            row[i].setMaxSize(2 * buttonRadius, 2 * buttonRadius)
            this.group.children.add(row[i])
        }

        if (row.size < buttons.size) {
            a = Arc(this.panelCenterX, this.panelCenterY + this.panelRadius/2.5, this.panelRadius/2.5, this.panelRadius/2.5, 0.0, 180.0)
            a.fill = Color.TRANSPARENT
            a.strokeWidth = 2.0
            a.stroke = Color.LIGHTGRAY
            a.layoutY = -this.panelRadius/2.5
            this.group.children.add(a)
            row = buttons.subList(buttomsPerRow, min(buttons.size, buttomsPerRow*2-1))
            for (i in 0 until row.size) {
                val p = rotatePoint(
                    Point2D.Double(this.panelCenterX, this.panelCenterY + this.panelRadius/2.5),
                    Point2D.Double(this.panelCenterX, this.panelCenterY),
                    i * 180.0/(row.size.toDouble()-1.0) + startAngle
                )
                var c = Circle(p.x, p.y, buttonRadius+5)
                c.fill = RNArtist.RNArtistGUIColor
                c.strokeWidth = 1.0
                c.stroke = Color.LIGHTGRAY
                this.group.children.add(c)
                c = Circle(p.x, p.y, buttonRadius)
                row[i].setShape(c)
                row[i].layoutX = p.x-buttonRadius
                row[i].layoutY = p.y-buttonRadius
                row[i].setMinSize(2 * buttonRadius, 2 * buttonRadius)
                row[i].setMaxSize(2 * buttonRadius, 2 * buttonRadius)
                this.group.children.add(row[i])
            }
        }

    }

}