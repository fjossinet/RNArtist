package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.RnartistConfig.getRnartistRelease
import io.github.fjossinet.rnartist.core.RnartistConfig.load
import io.github.fjossinet.rnartist.core.RnartistConfig.save
import io.github.fjossinet.rnartist.core.io.randomColor
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.theme
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.embed.swing.SwingNode
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.stage.*
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.io.*
import java.util.*
import java.util.stream.Collectors

class RNArtist : Application() {

    val mediator: Mediator
    lateinit var stage: Stage
    private var scrollCounter = 0
    private val statusBar: FlowPane
    val swingNode = SwingNode()
    private val root: BorderPane
    var centerDisplayOnSelection = false

    fun getInstallDir(): String {
        return File(
            this::class.java.protectionDomain.codeSource.location
                .toURI()
        ).parentFile.parent
    }

    init {
        load()
        this.mediator = Mediator(this)
        this.root = BorderPane()

        val toolbar = ToolBar()
        toolbar.padding = Insets(10.0, 10.0, 10.0, 10.0)

        toolbar.items.add(Label("Selection"))

        var selectionWidth = Button(null, null)
        selectionWidth.maxWidth = Double.MAX_VALUE
        var selectionLine = Line(0.0, 10.0, 10.0, 10.0)
        selectionLine.strokeWidth = 1.0
        selectionWidth.graphic = selectionLine
        selectionWidth.onAction = EventHandler {
            RnartistConfig.selectionWidth = 1
            mediator.canvas2D.repaint()
        }
        toolbar.items.add(selectionWidth)

        selectionWidth = Button(null, null)
        selectionWidth.maxWidth = Double.MAX_VALUE
        selectionLine = Line(0.0, 10.0, 10.0, 10.0)
        selectionLine.strokeWidth = 2.0
        selectionWidth.graphic = selectionLine
        selectionWidth.onAction = EventHandler {
            RnartistConfig.selectionWidth = 2
            mediator.canvas2D.repaint()
        }
        toolbar.items.add(selectionWidth)

        selectionWidth = Button(null, null)
        selectionWidth.maxWidth = Double.MAX_VALUE
        selectionLine = Line(0.0, 10.0, 10.0, 10.0)
        selectionLine.strokeWidth = 4.0
        selectionWidth.graphic = selectionLine
        selectionWidth.onAction = EventHandler {
            RnartistConfig.selectionWidth = 4
            mediator.canvas2D.repaint()
        }
        toolbar.items.add(selectionWidth)

        selectionWidth = Button(null, null)
        selectionWidth.maxWidth = Double.MAX_VALUE
        selectionLine = Line(0.0, 10.0, 10.0, 10.0)
        selectionLine.strokeWidth = 8.0
        selectionWidth.graphic = selectionLine
        selectionWidth.onAction = EventHandler {
            RnartistConfig.selectionWidth = 8
            mediator.canvas2D.repaint()
        }
        toolbar.items.add(selectionWidth)

        val selectionColorPicker = ColorPicker(Color.RED)
        selectionColorPicker.maxWidth = Double.MAX_VALUE
        selectionColorPicker.styleClass.add("button")
        selectionColorPicker.style = "-fx-color-label-visible: false ;"
        selectionColorPicker.onAction = EventHandler {
            RnartistConfig.selectionColor = javaFXToAwt(selectionColorPicker.value)
            mediator.canvas2D.repaint()
        }
        toolbar.items.add(selectionColorPicker)

        toolbar.items.add(Separator(Orientation.VERTICAL))

        toolbar.items.add(Label("Font"))

        val fontNames = ComboBox(
            FXCollections.observableList(Font.getFamilies().stream().distinct().collect(Collectors.toList())))
        fontNames.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                drawingLoaded.drawing.workingSession.fontName = fontNames.value
                mediator.canvas2D.repaint()
            }
        }
        fontNames.maxWidth = Double.MAX_VALUE

        toolbar.items.add(fontNames)

        val deltaXRes = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, 0)
        deltaXRes.prefWidth = 50.0
        deltaXRes.valueProperty().addListener { observable, oldValue, newValue ->
            mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                drawingLoaded.drawing.workingSession.deltafontx = deltaXRes.value
                mediator.canvas2D.repaint()
            }
        }
        toolbar.items.add(Label("x"))
        toolbar.items.add(deltaXRes)

        val deltaYRes = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, 0)
        deltaYRes.prefWidth = 50.0
        deltaYRes.valueProperty().addListener { observable, oldValue, newValue ->
            mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                drawingLoaded.drawing.workingSession.deltafonty = deltaYRes.value
                mediator.canvas2D.repaint()
            }
        }
        toolbar.items.add(Label("y"))
        toolbar.items.add(deltaYRes)

        val deltaFontSize = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, 0)
        deltaFontSize.prefWidth = 50.0
        deltaFontSize.valueProperty().addListener { observable, oldValue, newValue ->
            mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                drawingLoaded.drawing.workingSession.deltafontsize = deltaFontSize.value
                mediator.canvas2D.repaint()
            }
        }
        toolbar.items.add(Label("s"))
        toolbar.items.add(deltaFontSize)

        root.top = toolbar

        //++++++ left Toolbar
        val leftToolBar = GridPane()
        leftToolBar.alignment = Pos.TOP_CENTER
        leftToolBar.padding = Insets(5.0)
        leftToolBar.vgap = 5.0
        leftToolBar.hgap = 5.0

        var row = 0

        var s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        var l = Label("2D")
        leftToolBar.add(l, 0, row++, 2, 1)
        GridPane.setHalignment(l, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val center2D = Button(null, FontIcon("fas-crosshairs:15"))
        center2D.maxWidth = Double.MAX_VALUE
        center2D.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        center2D.onMouseClicked = EventHandler { mouseEvent ->
            if (mouseEvent.isShiftDown) {
                centerDisplayOnSelection = !centerDisplayOnSelection
                if (centerDisplayOnSelection) center2D.graphic = FontIcon("fas-lock:15") else center2D.graphic =
                    FontIcon("fas-crosshairs:15")
            } else {
                val selectionFrame: Rectangle2D? = mediator.canvas2D.getSelectionFrame()
                mediator.drawingDisplayed.get()?.drawing?.let {
                    mediator.canvas2D.centerDisplayOn(selectionFrame ?: it.getFrame())
                }
            }
        }
        center2D.tooltip = Tooltip("Focus 2D on Selection")

        val fit2D = Button(null, FontIcon("fas-expand-arrows-alt:15"))
        fit2D.maxWidth = Double.MAX_VALUE
        fit2D.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        fit2D.onMouseClicked = EventHandler {
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.fitStructure(mediator.canvas2D.getSelectionFrame(), 2.0)
            } else
                mediator.canvas2D.fitStructure(null)
        }
        fit2D.tooltip = Tooltip("Fit 2D")

        leftToolBar.add(center2D, 0, row)
        GridPane.setHalignment(center2D, HPos.CENTER)
        leftToolBar.add(fit2D, 1, row++)
        GridPane.setHalignment(fit2D, HPos.CENTER)

        val showTertiaries = Button(null, FontIcon("fas-eye:15"))
        showTertiaries.maxWidth = Double.MAX_VALUE
        showTertiaries.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        showTertiaries.onMouseClicked = EventHandler {
            val t = theme {
                show {
                    type = "tertiary_interaction"
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
        }
        showTertiaries.tooltip = Tooltip("Show Tertiaries")

        val hideTertiaries = Button(null, FontIcon("fas-eye-slash:15"))
        hideTertiaries.maxWidth = Double.MAX_VALUE
        hideTertiaries.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        hideTertiaries.onMouseClicked = EventHandler {
            val t = theme {
                hide {
                    type = "tertiary_interaction"
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
        }

        hideTertiaries.tooltip = Tooltip("Hide Tertiaries")

        leftToolBar.add(showTertiaries, 0, row)
        GridPane.setHalignment(showTertiaries, HPos.CENTER)
        leftToolBar.add(hideTertiaries, 1, row++)
        GridPane.setHalignment(hideTertiaries, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val levelDetails1 = Button("1")
        levelDetails1.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails1.maxWidth = Double.MAX_VALUE
        levelDetails1.onAction = EventHandler {
            val t = theme {
                details {
                    value = 1
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("1")
        }

        val levelDetails2 = Button("2")
        levelDetails2.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails2.maxWidth = Double.MAX_VALUE
        levelDetails2.onAction = EventHandler {
            val t = theme {
                details {
                    value = 2
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("2")
        }

        leftToolBar.add(levelDetails1, 0, row)
        GridPane.setHalignment(levelDetails1, HPos.CENTER)
        leftToolBar.add(levelDetails2, 1, row++)
        GridPane.setHalignment(levelDetails2, HPos.CENTER)

        val levelDetails3 = Button("3")
        levelDetails3.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails3.maxWidth = Double.MAX_VALUE
        levelDetails3.onAction = EventHandler {
            val t = theme {
                details {
                    value = 3
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("3")
        }

        val levelDetails4 = Button("4")
        levelDetails4.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails4.maxWidth = Double.MAX_VALUE
        levelDetails4.onAction = EventHandler {
            val t = theme {
                details {
                    value = 4
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("4")
        }
        leftToolBar.add(levelDetails3, 0, row)
        GridPane.setHalignment(levelDetails1, HPos.CENTER)
        leftToolBar.add(levelDetails4, 1, row++)
        GridPane.setHalignment(levelDetails2, HPos.CENTER)

        val levelDetails5 = Button("5")
        levelDetails5.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        levelDetails5.maxWidth = Double.MAX_VALUE
        levelDetails5.onAction = EventHandler {
            val t = theme {
                details {
                    value = 5
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("5")
        }

        leftToolBar.add(levelDetails5, 0, row++)
        GridPane.setHalignment(levelDetails5, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val AColorPicker = ColorPicker()
        AColorPicker.maxWidth = Double.MAX_VALUE
        val UColorPicker = ColorPicker()
        UColorPicker.maxWidth = Double.MAX_VALUE
        val GColorPicker = ColorPicker()
        GColorPicker.maxWidth = Double.MAX_VALUE
        val CColorPicker = ColorPicker()
        CColorPicker.maxWidth = Double.MAX_VALUE
        val RColorPicker = ColorPicker()
        RColorPicker.value = Color.BLACK
        RColorPicker.maxWidth = Double.MAX_VALUE
        val YColorPicker = ColorPicker()
        YColorPicker.value = Color.BLACK
        YColorPicker.maxWidth = Double.MAX_VALUE
        val NColorPicker = ColorPicker()
        NColorPicker.value = Color.BLACK
        NColorPicker.maxWidth = Double.MAX_VALUE

        val ALabel = Button("A")
        ALabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        ALabel.maxWidth = Double.MAX_VALUE
        ALabel.userData = "white"
        ALabel.textFill = Color.WHITE
        val ULabel = Button("U")
        ULabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        ULabel.maxWidth = Double.MAX_VALUE
        ULabel.userData = "white"
        ULabel.textFill = Color.WHITE
        val GLabel = Button("G")
        GLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        GLabel.userData = "white"
        GLabel.textFill = Color.WHITE
        GLabel.maxWidth = Double.MAX_VALUE
        val CLabel = Button("C")
        CLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        CLabel.maxWidth = Double.MAX_VALUE
        CLabel.userData = "white"
        CLabel.textFill = Color.WHITE
        val RLabel = Button("R")
        RLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        RLabel.maxWidth = Double.MAX_VALUE
        RLabel.userData = "white"
        RLabel.textFill = Color.WHITE
        RLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(Color.BLACK))
        val YLabel = Button("Y")
        YLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        YLabel.maxWidth = Double.MAX_VALUE
        YLabel.userData = "white"
        YLabel.textFill = Color.WHITE
        YLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(Color.BLACK))
        val NLabel = Button("N")
        NLabel.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        NLabel.maxWidth = Double.MAX_VALUE
        NLabel.userData = "white"
        NLabel.textFill = Color.WHITE
        NLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(Color.BLACK))

        val clearTheme = Button(null, FontIcon("fas-undo:15"))
        clearTheme.tooltip = Tooltip("Clear Theme")
        clearTheme.maxWidth = Double.MAX_VALUE
        clearTheme.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        clearTheme.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.let {
                it.drawing.clearTheme()
                mediator.canvas2D.repaint()
                mediator.scriptEditor.script.getScriptRoot().getThemeKw().removeButton.fire()
            }
        }

        val clearLayout = Button(null, FontIcon("fas-undo-alt:15"))
        clearLayout.tooltip = Tooltip("Clear Layout")
        clearLayout.maxWidth = Double.MAX_VALUE
        clearLayout.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        clearLayout.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.let {
                mediator.scriptEditor.script.getScriptRoot().getLayoutKw().removeButton.fire()
            }
        }

        leftToolBar.add(clearTheme, 0, row)
        GridPane.setHalignment(clearTheme, HPos.CENTER)
        leftToolBar.add(clearLayout, 1, row++)
        GridPane.setHalignment(clearLayout, HPos.CENTER)

        val pickColorScheme = Button(null, FontIcon("fas-dice-three:15"))
        pickColorScheme.maxWidth = Double.MAX_VALUE
        pickColorScheme.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))

        val paintResidues = Button(null, FontIcon("fas-fill:15"))
        paintResidues.maxWidth = Double.MAX_VALUE
        paintResidues.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        paintResidues.onAction = EventHandler {
            val t = theme {
                color {
                    type = "A"
                    value = getHTMLColorString(javaFXToAwt(AColorPicker.value))
                }
                color {
                    type = "a"
                    value =
                        getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE))
                }
                color {
                    type = "U"
                    value = getHTMLColorString(javaFXToAwt(UColorPicker.value))
                }
                color {
                    type = "u"
                    value =
                        getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE))
                }
                color {
                    type = "G"
                    value = getHTMLColorString(javaFXToAwt(GColorPicker.value))
                }
                color {
                    type = "g"
                    value =
                        getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE))
                }
                color {
                    type = "C"
                    value = getHTMLColorString(javaFXToAwt(CColorPicker.value))
                }
                color {
                    type = "c"
                    value =
                        getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE))
                }
            }

            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setColor(
                "A",
                getHTMLColorString(javaFXToAwt(AColorPicker.value))
            )
            mediator.scriptEditor.script.setColor(
                "a",
                getHTMLColorString(javaFXToAwt(if (ALabel.userData == "black") Color.BLACK else Color.WHITE))
            )
            mediator.scriptEditor.script.setColor(
                "U",
                getHTMLColorString(javaFXToAwt(UColorPicker.value))
            )
            mediator.scriptEditor.script.setColor(
                "u",
                getHTMLColorString(javaFXToAwt(if (ULabel.userData == "black") Color.BLACK else Color.WHITE))
            )
            mediator.scriptEditor.script.setColor(
                "G",
                getHTMLColorString(javaFXToAwt(GColorPicker.value))
            )
            mediator.scriptEditor.script.setColor(
                "g",
                getHTMLColorString(javaFXToAwt(if (GLabel.userData == "black") Color.BLACK else Color.WHITE))
            )
            mediator.scriptEditor.script.setColor(
                "C",
                getHTMLColorString(javaFXToAwt(CColorPicker.value))
            )
            mediator.scriptEditor.script.setColor(
                "c",
                getHTMLColorString(javaFXToAwt(if (CLabel.userData == "black") Color.BLACK else Color.WHITE))
            )
        }

        leftToolBar.add(pickColorScheme, 0, row)
        GridPane.setHalignment(pickColorScheme, HPos.CENTER)
        leftToolBar.add(paintResidues, 1, row++)
        GridPane.setHalignment(paintResidues, HPos.CENTER)

        ALabel.onAction = EventHandler {
            if (ALabel.userData == "black") {
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
            } else {
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
            }
        }

        leftToolBar.add(ALabel, 0, row)
        GridPane.setHalignment(ALabel, HPos.CENTER)
        leftToolBar.add(AColorPicker, 1, row++)
        GridPane.setHalignment(AColorPicker, HPos.CENTER)

        AColorPicker.styleClass.add("button")
        AColorPicker.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        AColorPicker.style = "-fx-color-label-visible: false ;"
        AColorPicker.onAction = EventHandler {
            ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(AColorPicker.value))
        }

        ULabel.onAction = EventHandler {
            if (ULabel.userData == "black") {
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
            } else {
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
            }
        }

        leftToolBar.add(ULabel, 0, row)
        GridPane.setHalignment(ULabel, HPos.CENTER)
        leftToolBar.add(UColorPicker, 1, row++)
        GridPane.setHalignment(UColorPicker, HPos.CENTER)

        UColorPicker.styleClass.add("button")
        UColorPicker.style = "-fx-color-label-visible: false ;"
        UColorPicker.onAction = EventHandler {
            ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(UColorPicker.value))
        }

        GLabel.onAction = EventHandler {
            if (GLabel.userData == "black") {
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
            } else {
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(GLabel, 0, row)
        GridPane.setHalignment(GLabel, HPos.CENTER)
        leftToolBar.add(GColorPicker, 1, row++)
        GridPane.setHalignment(GColorPicker, HPos.CENTER)

        GColorPicker.styleClass.add("button")
        GColorPicker.style = "-fx-color-label-visible: false ;"
        GColorPicker.onAction = EventHandler {
            GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(GColorPicker.value))
        }

        CLabel.onAction = EventHandler {
            if (CLabel.userData == "black") {
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(CLabel, 0, row)
        GridPane.setHalignment(CLabel, HPos.CENTER)
        leftToolBar.add(CColorPicker, 1, row++)
        GridPane.setHalignment(CColorPicker, HPos.CENTER)

        CColorPicker.styleClass.add("button")
        CColorPicker.style = "-fx-color-label-visible: false ;"
        CColorPicker.onAction = EventHandler {
            CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(CColorPicker.value))
        }

        RLabel.onAction = EventHandler {
            if (RLabel.userData == "black") {
                RLabel.userData = "white"
                RLabel.textFill = Color.WHITE
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
            } else {
                RLabel.userData = "black"
                RLabel.textFill = Color.BLACK
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(RLabel, 0, row)
        GridPane.setHalignment(RLabel, HPos.CENTER)
        leftToolBar.add(RColorPicker, 1, row++)
        GridPane.setHalignment(RColorPicker, HPos.CENTER)

        RColorPicker.styleClass.add("button")
        RColorPicker.style = "-fx-color-label-visible: false ;"
        RColorPicker.onAction = EventHandler {
            RLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(RColorPicker.value))
            AColorPicker.value = RColorPicker.value
            ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(RColorPicker.value))
            GColorPicker.value = RColorPicker.value
            GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(RColorPicker.value))
        }

        YLabel.onAction = EventHandler {
            if (YLabel.userData == "black") {
                YLabel.userData = "white"
                YLabel.textFill = Color.WHITE
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                YLabel.userData = "black"
                YLabel.textFill = Color.BLACK
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(YLabel, 0, row)
        GridPane.setHalignment(YLabel, HPos.CENTER)
        leftToolBar.add(YColorPicker, 1, row++)
        GridPane.setHalignment(YColorPicker, HPos.CENTER)

        YColorPicker.styleClass.add("button")
        YColorPicker.style = "-fx-color-label-visible: false ;"
        YColorPicker.onAction = EventHandler {
            YLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(YColorPicker.value))
            UColorPicker.value = YColorPicker.value
            ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(YColorPicker.value))
            CColorPicker.value = YColorPicker.value
            CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(YColorPicker.value))
        }

        NLabel.onAction = EventHandler {
            if (NLabel.userData == "black") {
                NLabel.userData = "white"
                NLabel.textFill = Color.WHITE
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                NLabel.userData = "black"
                NLabel.textFill = Color.BLACK
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }
        leftToolBar.add(NLabel, 0, row)
        GridPane.setHalignment(NLabel, HPos.CENTER)
        leftToolBar.add(NColorPicker, 1, row++)
        GridPane.setHalignment(NColorPicker, HPos.CENTER)

        NColorPicker.styleClass.add("button")
        NColorPicker.style = "-fx-color-label-visible: false ;"
        NColorPicker.onAction = EventHandler {
            NLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            AColorPicker.value = NColorPicker.value
            ALabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            GColorPicker.value = NColorPicker.value
            GLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            UColorPicker.value = NColorPicker.value
            ULabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
            CColorPicker.value = NColorPicker.value
            CLabel.style = "-fx-background-color: " + getHTMLColorString(javaFXToAwt(NColorPicker.value))
        }

        //++++++++++ we init the color buttons with a random scheme
        val random = Random()
        var color = randomColor()
        AColorPicker.value =
            awtColorToJavaFX(
                color
            )
        ALabel.style =
            "-fx-background-color: " + getHTMLColorString(color)
        if (random.nextInt(2) == 0) {
            ALabel.userData = "white"
            ALabel.textFill = Color.WHITE
        } else {
            ALabel.userData = "black"
            ALabel.textFill = Color.BLACK
        }

        color = randomColor()
        UColorPicker.value =
            awtColorToJavaFX(
                color
            )
        ULabel.style =
            "-fx-background-color: " + getHTMLColorString(color)
        if (random.nextInt(2) == 0) {
            ULabel.userData = "white"
            ULabel.textFill = Color.WHITE
        } else {
            ULabel.userData = "black"
            ULabel.textFill = Color.BLACK
        }

        color = randomColor()
        GColorPicker.value =
            awtColorToJavaFX(
                color
            )
        GLabel.style =
            "-fx-background-color: " + getHTMLColorString(color)

        if (random.nextInt(2) == 0) {
            GLabel.userData = "white"
            GLabel.textFill = Color.WHITE
        } else {
            GLabel.userData = "black"
            GLabel.textFill = Color.BLACK
        }

        color = randomColor()
        CColorPicker.value =
            awtColorToJavaFX(
                color
            )

        CLabel.style =
            "-fx-background-color: " + getHTMLColorString(color)

        if (random.nextInt(2) == 0) {
            CLabel.userData = "white"
            CLabel.textFill = Color.WHITE
        } else {
            CLabel.userData = "black"
            CLabel.textFill = Color.BLACK
        }

        pickColorScheme.onAction = EventHandler {

            val random = Random()
            var color = randomColor()
            AColorPicker.value =
                awtColorToJavaFX(
                    color
                )
            ALabel.style =
                "-fx-background-color: " + getHTMLColorString(color)
            if (random.nextInt(2) == 0) {
                ALabel.userData = "white"
                ALabel.textFill = Color.WHITE
            } else {
                ALabel.userData = "black"
                ALabel.textFill = Color.BLACK
            }

            color = randomColor()
            UColorPicker.value =
                awtColorToJavaFX(
                    color
                )
            ULabel.style =
                "-fx-background-color: " + getHTMLColorString(color)
            if (random.nextInt(2) == 0) {
                ULabel.userData = "white"
                ULabel.textFill = Color.WHITE
            } else {
                ULabel.userData = "black"
                ULabel.textFill = Color.BLACK
            }

            color = randomColor()
            GColorPicker.value =
                awtColorToJavaFX(
                    color
                )
            GLabel.style =
                "-fx-background-color: " + getHTMLColorString(color)

            if (random.nextInt(2) == 0) {
                GLabel.userData = "white"
                GLabel.textFill = Color.WHITE
            } else {
                GLabel.userData = "black"
                GLabel.textFill = Color.BLACK
            }

            color = randomColor()
            CColorPicker.value =
                awtColorToJavaFX(
                    color
                )

            CLabel.style =
                "-fx-background-color: " + getHTMLColorString(color)

            if (random.nextInt(2) == 0) {
                CLabel.userData = "white"
                CLabel.textFill = Color.WHITE
            } else {
                CLabel.userData = "black"
                CLabel.textFill = Color.BLACK
            }
        }

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val applyLineWidth = { lineWidth: Double ->
            val t = theme {
                line {
                    type =
                        "helix junction single_strand N secondary_interaction phosphodiester_bond tertiary_interaction interaction_symbol"
                    value = lineWidth
                }
            }
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setLineWidth(
                "helix junction single_strand N secondary_interaction phosphodiester_bond tertiary_interaction interaction_symbol",
                "${lineWidth}"
            )
        }

        val lineWidth1 = Button(null, null)
        lineWidth1.maxWidth = Double.MAX_VALUE
        var line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 0.25
        lineWidth1.graphic = line
        lineWidth1.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth1.onAction = EventHandler {
            applyLineWidth(0.25)
        }

        val lineWidth2 = Button(null, null)
        lineWidth2.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 0.5
        lineWidth2.graphic = line
        lineWidth2.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth2.onAction = EventHandler {
            applyLineWidth(0.5)
        }
        leftToolBar.add(lineWidth1, 0, row)
        GridPane.setHalignment(lineWidth1, HPos.CENTER)
        leftToolBar.add(lineWidth2, 1, row++)
        GridPane.setHalignment(lineWidth2, HPos.CENTER)

        val lineWidth3 = Button(null, null)
        lineWidth3.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 1.0
        lineWidth3.graphic = line
        lineWidth3.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth3.onAction = EventHandler {
            applyLineWidth(1.0)
        }

        val lineWidth4 = Button(null, null)
        lineWidth4.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 3.0
        lineWidth4.graphic = line
        lineWidth4.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth4.onAction = EventHandler {
            applyLineWidth(3.0)
        }
        leftToolBar.add(lineWidth3, 0, row)
        GridPane.setHalignment(lineWidth3, HPos.CENTER)
        leftToolBar.add(lineWidth4, 1, row++)
        GridPane.setHalignment(lineWidth4, HPos.CENTER)

        val lineWidth5 = Button(null, null)
        lineWidth5.maxWidth = Double.MAX_VALUE
        line = Line(0.0, 10.0, 10.0, 10.0)
        line.strokeWidth = 5.0
        lineWidth5.graphic = line
        lineWidth5.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineWidth5.onAction = EventHandler {
            applyLineWidth(5.0)
        }

        val lineColorPicker = ColorPicker(Color.BLACK)
        lineColorPicker.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        lineColorPicker.maxWidth = Double.MAX_VALUE
        lineColorPicker.styleClass.add("button")
        lineColorPicker.style = "-fx-color-label-visible: false ;"
        lineColorPicker.onAction = EventHandler {
            val t = theme {
                color {
                    type =
                        "helix junction single_strand secondary_interaction phosphodiester_bond interaction_symbol tertiary_interaction"
                    value = getHTMLColorString(javaFXToAwt(lineColorPicker.value))
                }
            }

            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.getSelection().map { it.applyTheme(t)}
            else
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setColor(
                "helix junction single_strand secondary_interaction phosphodiester_bond interaction_symbol tertiary_interaction",
                getHTMLColorString(javaFXToAwt(lineColorPicker.value))
            )

        }
        leftToolBar.add(lineWidth5, 0, row)
        GridPane.setHalignment(lineWidth5, HPos.CENTER)
        leftToolBar.add(lineColorPicker, 1, row++)
        GridPane.setHalignment(lineColorPicker, HPos.CENTER)

        s = Separator()
        s.padding = Insets(10.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        l = Label("3D")
        leftToolBar.add(l, 0, row++, 2, 1)
        GridPane.setHalignment(l, HPos.CENTER)

        s = Separator()
        s.padding = Insets(5.0, 0.0, 5.0, 0.0)
        s.styleClass.add("thick-separator")
        leftToolBar.add(s, 0, row++, 2, 1)
        GridPane.setHalignment(s, HPos.CENTER)

        val reload3D = Button(null, FontIcon("fas-redo:15"))
        reload3D.onMouseClicked = EventHandler { mediator.chimeraDriver.displayCurrent3D() }
        reload3D.setTooltip(Tooltip("Reload 3D"))

        val focus3D = Button(null, FontIcon("fas-crosshairs:15"))
        focus3D.onMouseClicked = EventHandler { mediator.focusInChimera() }
        focus3D.setTooltip(Tooltip("Focus 3D on Selection"))

        leftToolBar.add(reload3D, 0, row)
        GridPane.setHalignment(reload3D, HPos.CENTER)
        leftToolBar.add(focus3D, 1, row++)
        GridPane.setHalignment(focus3D, HPos.CENTER)

        val paintSelectionin3D = Button(null, FontIcon("fas-fill:15"))
        paintSelectionin3D.setOnMouseClicked( { mediator.chimeraDriver.color3D(if (mediator.canvas2D.getSelectedResidues().isNotEmpty()) mediator.canvas2D.getSelectedResidues() else mediator.drawingDisplayed.get()!!.drawing.residues) })
        paintSelectionin3D.setTooltip(Tooltip("Paint 3D selection"))

        leftToolBar.add(paintSelectionin3D, 0, row++)
        GridPane.setHalignment(paintSelectionin3D, HPos.CENTER)

        root.left = leftToolBar

        //++++++ Canvas2D
        swingNode.onMouseMoved = EventHandler { mouseEvent: MouseEvent? ->
            mediator.drawingDisplayed.get()?.drawing?.let {
                it.quickDraw = false //a trick if after the scroll event the quickdraw is still true
            }
        }
        swingNode.onMouseClicked = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.PRIMARY) {
                mediator.drawingDisplayed.get()?.let mouseClicked@{ drawingLoaded ->
                    val at = AffineTransform()
                    at.translate(
                        drawingLoaded.drawing.workingSession.viewX,
                        drawingLoaded.drawing.workingSession.viewY
                    )
                    at.scale(
                        drawingLoaded.drawing.workingSession.zoomLevel,
                        drawingLoaded.drawing.workingSession.zoomLevel
                    )
                    for (knob in drawingLoaded.knobs)
                        if (knob.contains(mouseEvent.x, mouseEvent.y))
                            return@mouseClicked
                    for (h in drawingLoaded.drawing.workingSession.helicesDrawn) {
                        var shape = h.selectionFrame
                        if (shape != null && at.createTransformedShape(shape)
                                .contains(mouseEvent.x, mouseEvent.y)
                        ) {
                            for (r in h.residues) {
                                shape = r.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
                                        .contains(mouseEvent.x, mouseEvent.y)
                                ) {
                                    if (!mediator.canvas2D.isSelected(r) && !mediator.canvas2D.isSelected(r.parent) && !mediator.canvas2D.isSelected(
                                            r.parent!!.parent
                                        )
                                    ) {
                                        mediator.canvas2D.addToSelection(r)
                                        return@mouseClicked
                                    } else if (!mediator.canvas2D.isSelected(r.parent) && !mediator.canvas2D.isSelected(
                                            r.parent!!.parent
                                        )
                                    ) {
                                        mediator.canvas2D.removeFromSelection(r)
                                        mediator.canvas2D.addToSelection(r.parent)
                                        return@mouseClicked
                                    } else if (!mediator.canvas2D.isSelected(r.parent!!.parent)) {
                                        mediator.canvas2D.removeFromSelection(r.parent)
                                        mediator.canvas2D.addToSelection(r.parent!!.parent)
                                        return@mouseClicked
                                    }
                                }
                            }
                            for (interaction in h.secondaryInteractions) {
                                shape = interaction.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
                                        .contains(mouseEvent.x, mouseEvent.y)
                                ) {
                                    if (!mediator.canvas2D.isSelected(interaction) && !mediator.canvas2D.isSelected(
                                            interaction.parent
                                        )
                                    ) {
                                        mediator.canvas2D.addToSelection(interaction)
                                        return@mouseClicked
                                    } else if (!mediator.canvas2D.isSelected(interaction.parent)) {
                                        mediator.canvas2D.removeFromSelection(interaction)
                                        mediator.canvas2D.addToSelection(interaction.parent)
                                        return@mouseClicked
                                    }
                                }
                            }
                            if (!mediator.canvas2D.isSelected(h)) {
                                mediator.canvas2D.addToSelection(h)
                                return@mouseClicked
                            } else {
                                var p = h.parent
                                while (p != null && mediator.canvas2D.isSelected(p)) {
                                    p = p.parent
                                }
                                if (p == null) {
                                    mediator.canvas2D.addToSelection(h)
                                } else {
                                    mediator.canvas2D.addToSelection(p)
                                }
                                return@mouseClicked
                            }
                        }
                    }
                    for (j in drawingLoaded.drawing.workingSession.junctionsDrawn) {
                        var shape = j.selectionFrame
                        if (shape != null && at.createTransformedShape(shape)
                                .contains(mouseEvent.x, mouseEvent.y)
                        ) {
                            for (r in j.residues) {
                                shape = r.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
                                        .contains(mouseEvent.x, mouseEvent.y)
                                ) {
                                    if (!mediator.canvas2D.isSelected(r) && !mediator.canvas2D.isSelected(r.parent)) {
                                        mediator.canvas2D.addToSelection(r)
                                        return@mouseClicked
                                    } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                        mediator.canvas2D.removeFromSelection(r)
                                        mediator.canvas2D.addToSelection(r.parent)
                                        return@mouseClicked
                                    }
                                }
                            }
                            if (!mediator.canvas2D.isSelected(j)) {
                                mediator.canvas2D.addToSelection(j)
                                return@mouseClicked
                            } else {
                                var p = j.parent
                                while (p != null && mediator.canvas2D.isSelected(p)) {
                                    p = p.parent
                                }
                                if (p == null) {
                                    mediator.canvas2D.addToSelection(j)
                                } else {
                                    mediator.canvas2D.addToSelection(p)
                                }
                                return@mouseClicked
                            }
                        }
                    }
                    for (ss in drawingLoaded.drawing.workingSession.singleStrandsDrawn) {
                        var shape = ss.selectionFrame
                        if (shape != null && at.createTransformedShape(shape)
                                .contains(mouseEvent.x, mouseEvent.y)
                        ) {
                            for (r in ss.residues) {
                                shape = r.selectionFrame
                                if (shape != null && at.createTransformedShape(shape)
                                        .contains(mouseEvent.x, mouseEvent.y)
                                ) {
                                    if (!mediator.canvas2D.isSelected(r) && !mediator.canvas2D.isSelected(r.parent)) {
                                        mediator.canvas2D.addToSelection(r)
                                        return@mouseClicked
                                    } else if (!mediator.canvas2D.isSelected(r.parent)) {
                                        mediator.canvas2D.removeFromSelection(r)
                                        mediator.canvas2D.addToSelection(r.parent)
                                        return@mouseClicked
                                    }
                                }
                            }
                        }
                    }
                    if (mouseEvent.clickCount == 2) {
                        //no selection
                        mediator.canvas2D.clearSelection()
                    }
                }
            }
        }
        swingNode.onMouseDragged = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.SECONDARY) {
                mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                    drawingLoaded.drawing.quickDraw = true
                    val transX: Double = mouseEvent.x - mediator.canvas2D.translateX
                    val transY: Double = mouseEvent.y - mediator.canvas2D.translateY
                    drawingLoaded.drawing.workingSession.moveView(transX, transY)
                    mediator.canvas2D.translateX = mouseEvent.x
                    mediator.canvas2D.translateY = mouseEvent.y
                    mediator.canvas2D.repaint()
                }
            }
        }
        swingNode.onMouseReleased = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.SECONDARY) {
                mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                    drawingLoaded.drawing.quickDraw = false
                    mediator.canvas2D.translateX = 0.0
                    mediator.canvas2D.translateY = 0.0
                    mediator.canvas2D.repaint()
                }
            }
        }
        swingNode.onMousePressed = EventHandler { mouseEvent: MouseEvent ->
            if (mouseEvent.button == MouseButton.SECONDARY) {
                mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                    mediator.canvas2D.translateX = mouseEvent.x
                    mediator.canvas2D.translateY = mouseEvent.y
                }
            }
        }
        swingNode.onScroll = EventHandler { scrollEvent: ScrollEvent ->
            mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                drawingLoaded.drawing.quickDraw = true
                scrollCounter++
                val th = Thread {
                    try {
                        Thread.sleep(100)
                        if (scrollCounter == 1) {
                            drawingLoaded.drawing.quickDraw = false
                            mediator.canvas2D.repaint()
                        }
                        scrollCounter--
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                }
                th.isDaemon = true
                th.start()
                val realMouse =
                    Point2D.Double(
                        (scrollEvent.x - drawingLoaded.drawing.workingSession.viewX) / drawingLoaded.drawing.workingSession.zoomLevel,
                        (scrollEvent.y - drawingLoaded.drawing.workingSession.viewY) / drawingLoaded.drawing.workingSession.zoomLevel
                    )
                val notches = scrollEvent.deltaY
                if (notches < 0) drawingLoaded.drawing.workingSession.setZoom(1.25)
                if (notches > 0) drawingLoaded.drawing.workingSession.setZoom(1.0 / 1.25)
                val newRealMouse =
                    Point2D.Double(
                        (scrollEvent.x - drawingLoaded.drawing.workingSession.viewX) / drawingLoaded.drawing.workingSession.zoomLevel,
                        (scrollEvent.y - drawingLoaded.drawing.workingSession.viewY) / drawingLoaded.drawing.workingSession.zoomLevel
                    )
                drawingLoaded.drawing.workingSession.moveView(
                    (newRealMouse.getX() - realMouse.getX()) * drawingLoaded.drawing.workingSession.zoomLevel,
                    (newRealMouse.getY() - realMouse.getY()) * drawingLoaded.drawing.workingSession.zoomLevel
                )
                mediator.canvas2D.repaint()
            }
        }
        createSwingContent(swingNode)

        root.center = swingNode

        //### Status Bar
        this.statusBar = FlowPane()
        statusBar.setAlignment(Pos.CENTER_RIGHT)
        statusBar.setPadding(Insets(5.0, 10.0, 5.0, 10.0))
        statusBar.setHgap(20.0)

        val release = Label(getRnartistRelease())
        statusBar.getChildren().add(release)

        val shutdown = Button(null, FontIcon("fas-power-off:15"))
        shutdown.tooltip = Tooltip("Exit RNArtist")
        shutdown.onAction = EventHandler { actionEvent: ActionEvent ->
            val alert =
                Alert(Alert.AlertType.CONFIRMATION)
            alert.initOwner(stage)
            alert.initModality(Modality.WINDOW_MODAL)
            alert.title = "Confirm Exit"
            alert.headerText = null
            alert.contentText = "Are you sure to exit RNArtist?"
            val alerttStage = alert.dialogPane.scene.window as Stage
            alerttStage.isAlwaysOnTop = true
            alerttStage.toFront()
            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                try {
                    Platform.exit()
                    save()
                    System.exit(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                actionEvent.consume()
            }
        }
        statusBar.getChildren().add(shutdown)

        val windowsBar = FlowPane()
        windowsBar.alignment = Pos.CENTER_LEFT
        windowsBar.padding = Insets(5.0, 10.0, 5.0, 10.0)
        windowsBar.hgap = 10.0

        val showTools = Button(null, FontIcon("fas-tools:15"))
        showTools.tooltip = Tooltip("Show Tools")
        showTools.onAction = EventHandler { actionEvent: ActionEvent? ->
            mediator.sideWindow.stage.show()
            mediator.sideWindow.stage.toFront()
        }
        windowsBar.children.add(showTools)

        val bar = GridPane()
        val cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        bar.columnConstraints.addAll(cc, ColumnConstraints())

        bar.add(windowsBar, 0, 0)
        GridPane.setFillWidth(windowsBar, true)
        GridPane.setHalignment(windowsBar, HPos.LEFT)
        bar.add(this.statusBar, 1, 0)
        GridPane.setHalignment(this.statusBar, HPos.RIGHT)
        //root.bottom = bar

        //root.right = RNAGallery(mediator)
    }

    override fun start(stage: Stage) {
        this.stage = stage
        this.stage.setOnCloseRequest(EventHandler { windowEvent: WindowEvent ->
            val alert =
                Alert(Alert.AlertType.CONFIRMATION)
            alert.initOwner(stage)
            alert.initModality(Modality.WINDOW_MODAL)
            alert.title = "Confirm Exit"
            alert.headerText = null
            alert.contentText = "Are you sure to exit RNArtist?"
            val alerttStage = alert.dialogPane.scene.window as Stage
            alerttStage.isAlwaysOnTop = true
            alerttStage.toFront()
            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                try {
                    Platform.exit()
                    save()
                    System.exit(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                windowEvent.consume()
            }
        })

        this.stage.widthProperty().addListener { obs: ObservableValue<out Number?>?, oldVal: Number?, newVal: Number? ->
            //mediator.canvas2D.updateKnobs()
            mediator.canvas2D.repaint()
        }

        this.stage.heightProperty()
            .addListener { obs: ObservableValue<out Number?>?, oldVal: Number?, newVal: Number? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
            }

        this.stage.fullScreenProperty()
            .addListener { obs: ObservableValue<out Boolean?>?, oldVal: Boolean?, newVal: Boolean? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
            }

        this.stage.maximizedProperty()
            .addListener { obs: ObservableValue<out Boolean?>?, oldVal: Boolean?, newVal: Boolean? ->
                //mediator.canvas2D.updateKnobs()
                mediator.canvas2D.repaint()
            }

        val screen = Screen.getPrimary()

        val scene = Scene(this.root, screen.bounds.width, screen.bounds.height)
        stage.scene = scene
        stage.title = "RNArtist"

        val screenSize = Screen.getPrimary().bounds
        val width = (screenSize.width * 0.5).toInt()
        scene.window.width = screenSize.width - width
        scene.window.height = screenSize.height
        scene.window.x = 0.0
        scene.window.y = 0.0

        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")

        SplashWindow(this.mediator)
    }

    private fun createSwingContent(swingNode: SwingNode) {
        Platform.runLater {
            val canvas = Canvas2D(mediator)
            swingNode.content = canvas
            swingNode.isCache = true
            swingNode.cacheHint = CacheHint.SPEED
        }
    }

    fun main() {
        launch();
    }


}
