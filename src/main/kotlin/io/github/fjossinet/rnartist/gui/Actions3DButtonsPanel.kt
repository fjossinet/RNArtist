package io.github.fjossinet.rnartist.gui
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.io.ChimeraXDriver
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon

class Actions3DButtonsPanel(mediator: Mediator):ButtonsPanel(mediator = mediator, panelRadius = 60.0) {

    val linkChimeraX = Button(null, FontIcon("fas-link:15"))
    val waitingForConnection = Timeline()

    init {
        mediator.tertiaryStructureButtonsPanel = this
        waitingForConnection.cycleCount = Timeline.INDEFINITE;
        waitingForConnection.keyFrames.add(KeyFrame(Duration.seconds(0.5), KeyValue(linkChimeraX.styleProperty(), "-fx-base: grey")))
        waitingForConnection.keyFrames.add(KeyFrame(Duration.seconds(1.0), KeyValue(linkChimeraX.styleProperty(), "-fx-base: red")))
        val chimeraRemoteRest = TextField("${RnartistConfig.chimeraHost}:${RnartistConfig.chimeraPort}")
        chimeraRemoteRest.minWidth = 150.0
        chimeraRemoteRest.maxWidth = 150.0
        this.linkChimeraX.maxWidth = Double.MAX_VALUE
        this.linkChimeraX.style = "-fx-base: red"
        (this.linkChimeraX.graphic as FontIcon).fill = Color.WHITE
        this.linkChimeraX.onMouseClicked = EventHandler {
            try {
                this.linkChimeraX.style = "-fx-base: red"
                waitingForConnection.play()
                RnartistConfig.chimeraHost = chimeraRemoteRest.text.split(":").first().trim { it <= ' ' }
                RnartistConfig.chimeraPort = chimeraRemoteRest.text.split(":").last().trim { it <= ' ' }.toInt()
                val sessionFile = mediator.chimeraDriver.sessionFile
                mediator.chimeraDriver = ChimeraXDriver(mediator)
                mediator.chimeraDriver.sessionFile = sessionFile
                mediator.chimeraDriver.connectToRestServer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        this.linkChimeraX.tooltip = Tooltip("Link ChimeraX")
        this.addButton(this.linkChimeraX)

        val focus3D = Button(null, FontIcon("fas-crosshairs:15"))
        focus3D.onMouseClicked = EventHandler { mediator.focusInChimera() }
        focus3D.tooltip = Tooltip("Focus 3D on Selection")
        this.addButton(focus3D)

        val paintSelectionin3D = Button(null, FontIcon("fas-fill:15"))
        paintSelectionin3D.setOnMouseClicked {
            mediator.chimeraDriver.color3D(
                if (mediator.canvas2D.getSelectedResidues()
                        .isNotEmpty()
                ) mediator.canvas2D.getSelectedResidues() else mediator.drawingDisplayed.get()!!.drawing.residues
            )
        }
        paintSelectionin3D.tooltip = Tooltip("Paint 3D selection")
        this.addButton(paintSelectionin3D)

        val reload3D = Button(null, FontIcon("fas-redo:15"))
        reload3D.onMouseClicked = EventHandler { mediator.chimeraDriver.displayCurrent3D() }
        reload3D.tooltip = Tooltip("Reload 3D")
        this.addButton(reload3D)

        val clearTheme = Button(null, FontIcon("fas-undo:15"))
        clearTheme.tooltip = Tooltip("Clear Theme")
        clearTheme.maxWidth = Double.MAX_VALUE
        clearTheme.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.let {
                it.drawing.clearTheme()
                mediator.canvas2D.repaint()
                mediator.scriptEditor.script.getScriptRoot().getThemeKw().removeButton.fire()
            }
        }
        this.addButton(clearTheme)

        this.children.add(Label("ChimeraX Remote Rest"))
        this.children.add(chimeraRemoteRest)
    }

    fun chimeraConnected(connected:Boolean) {
        waitingForConnection.stop()
        this.linkChimeraX.style =
            if (connected) "-fx-base: green" else "-fx-base: red"
        if (connected) {
            mediator.chimeraDriver.displayCurrent3D()
        }
    }
}