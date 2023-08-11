package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.gui.RNArtistColorPicker
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.animation.RotateTransition
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.util.Duration

class SelectionColorPicker(mediator: Mediator) : RNArtistColorPicker(mediator) {

    init {
        this.colorWheel.onMouseClicked = EventHandler { event ->
            colorsFromWheel.forEach {
                val color = Color.hsb(it.value[0], it.value[1], it.value[2])
                if (event.x >= it.key.x && event.x <= it.key.x + 1.0 && event.y >= it.key.y && event.y <= it.key.y + 1.0) {
                    if (applyOnFont)
                        lastFontColor = color
                    else
                        lastNonFontColor = color
                    val t = Theme()
                    t.addConfiguration(selection = getDrawingElementsSelector(), ThemeParameter.color) {
                        getHTMLColorString(
                            javaFXToAwt(color)
                        )
                    }
                    //we update the last colors for all the color pickers
                    mediator.colorsPickers.forEach { colorPicker ->
                        colorPicker.addLastColor(it.value)
                    }
                    this.repaintBrightness(it.value)
                    this.repaintSaturation(it.value)
                    mediator.canvas2D.getSelection().map {it.applyTheme(t)}
                    mediator.canvas2D.repaint()
                    return@EventHandler
                }
            }
        }

        this.colorWheelGroup.onMouseClicked = EventHandler {
            var i = 0
            while (i <= 6) {
                var rotatedPoint = rotatePoint(
                    java.awt.geom.Point2D.Double(90.0, 18.0),
                    java.awt.geom.Point2D.Double(90.0, 85.0),
                    currentAngle - 30.0 * i
                )
                var c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                if (c.contains(Point2D(it.x, it.y))) {
                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                    rt.byAngle = - 30.0 * i
                    currentAngle -= 30.0 * i
                    currentAngle = currentAngle%360
                    rt.play()
                    val t = Theme()
                    t.addConfiguration(getDrawingElementsSelector(), ThemeParameter.linewidth) {
                        when (currentAngle) {
                            0.0 -> "0.0"
                            30.0, -330.0 -> "0.5"
                            60.0, -300.0 -> "1.0"
                            90.0, -270.0 -> "2.0"
                            120.0, -240.0 -> "3.0"
                            150.0, -210.0 -> "4.0"
                            180.0, -180.0 -> "5.0"
                            210.0, -150.0 -> "6.0"
                            240.0, -120.0 -> "7.0"
                            270.0, -90.0 -> "8.0"
                            300.0, -60.0 -> "9.0"
                            330.0, -30.0 -> "10.0"
                            else -> "1.0"
                        }
                    }
                    mediator.canvas2D.getSelection().map {it.applyTheme(t)}
                    mediator.canvas2D.repaint()
                    break
                }
                rotatedPoint = rotatePoint(
                    java.awt.geom.Point2D.Double(90.0, 18.0),
                    java.awt.geom.Point2D.Double(90.0, 85.0),
                    currentAngle + 30.0 * i
                )
                c = Circle(rotatedPoint.x, rotatedPoint.y, 7.0)
                if (c.contains(Point2D(it.x, it.y))) {
                    val rt = RotateTransition(Duration.millis(100.0), this.lineWidthKnobGroup)
                    rt.byAngle = 30.0 * i
                    currentAngle += 30.0 * i
                    currentAngle = currentAngle%360
                    rt.play()
                    val t = Theme()
                    t.addConfiguration(selection = getDrawingElementsSelector(), ThemeParameter.linewidth) {
                        when (currentAngle) {
                            0.0 -> "0.0"
                            30.0, -330.0 -> "0.5"
                            60.0, -300.0 -> "1.0"
                            90.0, -270.0 -> "2.0"
                            120.0, -240.0 -> "3.0"
                            150.0, -210.0 -> "4.0"
                            180.0, -180.0 -> "5.0"
                            210.0, -150.0 -> "6.0"
                            240.0, -120.0 -> "7.0"
                            270.0, -90.0 -> "8.0"
                            300.0, -60.0 -> "9.0"
                            330.0, -30.0 -> "10.0"
                            else -> "1.0"
                        }
                    }
                    mediator.canvas2D.getSelection().map {it.applyTheme(t)}
                    mediator.canvas2D.repaint()
                    break
                }
                i++
            }

        }
    }

    override fun addLastColor(c:DoubleArray) {
        val r = Circle(0.0, 0.0, 10.0)
        val color = Color.hsb(c[0], c[1], c[2])
        r.onMouseClicked = EventHandler { event ->
            if (applyOnFont)
                lastFontColor = color
            else
                lastNonFontColor = color
            val t = Theme()
            t.addConfiguration(selection = getDrawingElementsSelector(), ThemeParameter.color) {
                getHTMLColorString(
                    javaFXToAwt(color)
                )
            }
            this.repaintBrightness(c)
            this.repaintSaturation(c)
            mediator.currentDrawing.get()?.let { currentDrawing ->
                mediator.canvas2D.getSelection().map {it.applyTheme(t)}
                mediator.canvas2D.repaint()
            }
        }
        r.fill = color
        r.stroke = Color.BLACK
        r.strokeWidth = 1.0
        if (this.lastColors.children.size >= 6) {
            val children = mutableListOf<Node>()
            this.lastColors.children.forEach {
                children.add(it)
            }
            children.removeAt(children.size-1)
            children.add(0, r)
            this.lastColors.children.clear()
            this.lastColors.children.addAll(*children.toTypedArray())
        }
        else
            this.lastColors.children.add(r)
    }

    override fun repaintBrightness(c:DoubleArray) {
        this.brightnessBar.children.clear()
        (0..5).reversed().forEach {
            val r = Circle(0.0, 0.0, 10.0)
            val color = Color.hsb(c[0], c[1], 0.2*it)
            r.onMouseClicked = EventHandler { event ->
                if (applyOnFont)
                    lastFontColor = color
                else
                    lastNonFontColor = color
                val t = Theme()
                t.addConfiguration(selection = getDrawingElementsSelector(), ThemeParameter.color) {
                    getHTMLColorString(
                        javaFXToAwt(color)
                    )
                }
                mediator.currentDrawing.get()?.let { currentDrawing ->
                    mediator.canvas2D.getSelection().map {it.applyTheme(t)}
                    mediator.canvas2D.repaint()
                }
            }
            r.fill = color
            r.stroke = Color.BLACK
            r.strokeWidth = 1.0
            this.brightnessBar.children.add(r)
        }
    }

    override fun repaintSaturation(c:DoubleArray) {
        this.saturationBar.children.clear()
        (0..5).reversed().forEach {
            val r = Circle(0.0, 0.0, 10.0)
            val color = Color.hsb(c[0], 0.2*it, c[2])
            r.onMouseClicked = EventHandler { event ->
                if (applyOnFont)
                    lastFontColor = color
                else
                    lastNonFontColor = color
                val t = Theme()
                t.addConfiguration(selection = getDrawingElementsSelector(), ThemeParameter.color) {
                    getHTMLColorString(
                        javaFXToAwt(color)
                    )
                }
                mediator.currentDrawing.get()?.let { currentDrawing ->
                    mediator.canvas2D.getSelection().map {it.applyTheme(t)}
                    mediator.canvas2D.repaint()
                }
            }
            r.fill = color
            r.stroke = Color.BLACK
            r.strokeWidth = 1.0
            this.saturationBar.children.add(r)
        }
    }

    override fun updateDSLScript() {
        mediator.currentDrawing.get()?.let { currentDrawing ->
            currentDrawing.rnArtistEl?.let { rnArtistEl ->
                val colorElements = rnArtistEl.getThemeEl().getColorEl()
                this.lastNonFontColor?.let { color ->
                    //we remove the color elements with just a value to use the new one instead
                    colorElements.map { colorEl ->
                        if (colorEl.properties.size == 1 && colorEl.properties.containsKey("value"))
                            rnArtistEl.getThemeEl().removeChild(colorEl)
                    }
                    val colorEl = rnArtistEl.getThemeEl().addColorEl()
                    colorEl.setValue(getHTMLColorString(javaFXToAwt(color)))
                }
                this.lastFontColor?.let { color ->
                    //we remove the color elements with a value and a type "n" to use the new one instead
                    colorElements.map { colorEl ->
                        if (colorEl.properties.size == 2 && colorEl.properties.containsKey("value") && colorEl.properties.containsKey("type") && colorEl.properties["type"] == "n")
                            rnArtistEl.getThemeEl().removeChild(colorEl)
                    }
                    val colorEl = rnArtistEl.getThemeEl().addColorEl()
                    colorEl.setValue(getHTMLColorString(javaFXToAwt(color)))
                    colorEl.setType("n")
                }
                val lineElements = rnArtistEl.getThemeEl().getLineEl()
                this.lastLineWidth?.let { lineWidth ->
                    //we remove the line elements with just a value to use the new one instead
                    lineElements.map { lineEl ->
                        if (lineEl.properties.size == 1 && lineEl.properties.containsKey("value"))
                            rnArtistEl.getThemeEl().removeChild(lineEl)
                    }
                    val lineEl = rnArtistEl.getThemeEl().addLineEl()
                    lineEl.setValue(lineWidth)
                }
            }
        }
    }

}