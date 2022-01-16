package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig.chimeraHost
import io.github.fjossinet.rnartist.core.RnartistConfig.chimeraPort
import io.github.fjossinet.rnartist.core.RnartistConfig.exportSVGWithBrowserCompatibility
import io.github.fjossinet.rnartist.core.RnartistConfig.isChimeraX
import io.github.fjossinet.rnartist.core.RnartistConfig.save
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.ChimeraXDriver
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.stage.*
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import java.util.stream.Collectors

class Settings(mediator: Mediator):VBox() {

    init {
        this.padding = Insets(10.0, 10.0, 10.0, 10.0)

        //---- Chimera
        var title = Label("UCSF Chimera")
        title.style = "-fx-font-size: 20"
        this.children.add(VBox(title, Separator(Orientation.HORIZONTAL)))
        val chimeraPane = GridPane()
        for (i in 0..5) {
            val constraints = ColumnConstraints()
            if (i == 3) constraints.hgrow = Priority.ALWAYS
            chimeraPane.columnConstraints.add(constraints)
        }
        chimeraPane.padding = Insets(10.0, 5.0, 15.0, 5.0)
        chimeraPane.hgap = 5.0
        chimeraPane.vgap = 5.0
        this.children.add(chimeraPane)
        var hostLabel = Label("Host")
        chimeraPane.children.add(hostLabel)
        GridPane.setConstraints(hostLabel, 0, 0)
        var hostValue = TextField(chimeraHost)
        chimeraPane.children.add(hostValue)
        GridPane.setConstraints(hostValue, 1, 0)
        var portLabel = Label("Port")
        chimeraPane.children.add(portLabel)
        GridPane.setConstraints(portLabel, 2, 0)
        var portValue = TextField("" + chimeraPort)
        chimeraPane.children.add(portValue)
        GridPane.setConstraints(portValue, 3, 0)
        var isX = CheckBox("Chimera X")
        isX.isSelected = isChimeraX
        chimeraPane.children.add(isX)
        GridPane.setConstraints(isX, 4, 0)
        var connect2ChimeraRest = Button("Connect")
        connect2ChimeraRest.maxWidth = Double.MAX_VALUE
        chimeraPane.children.add(connect2ChimeraRest)
        GridPane.setConstraints(connect2ChimeraRest, 5, 0)
        connect2ChimeraRest.onMouseClicked = EventHandler {
            try {
                chimeraHost = hostValue.text.trim { it <= ' ' }
                chimeraPort = portValue.text.trim { it <= ' ' }.toInt()
                isChimeraX = isX.isSelected
                val structures = mediator.chimeraDriver.tertiaryStructures
                val pdbFile = mediator.chimeraDriver.pdbFile
                val sessionFile = mediator.chimeraDriver.sessionFile
                if (isChimeraX)
                    mediator.chimeraDriver = ChimeraXDriver(mediator)
                else
                    mediator.chimeraDriver = ChimeraDriver(mediator)
                mediator.chimeraDriver.tertiaryStructures = structures
                mediator.chimeraDriver.pdbFile = pdbFile
                mediator.chimeraDriver.sessionFile = sessionFile
                mediator.chimeraDriver.connectToRestServer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //++++++ pane for the fonts


        //---- Bunch of options
        title = Label("Misc Settings")
        title.style = "-fx-font-size: 20"
        this.children.add(VBox(title, Separator(Orientation.HORIZONTAL)))
        val optionsPane = GridPane()
        var cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        optionsPane.columnConstraints.addAll(ColumnConstraints(), cc)
        this.children.add(optionsPane)
        optionsPane.padding = Insets(10.0, 5.0, 15.0, 5.0)
        optionsPane.hgap = 5.0
        optionsPane.vgap = 5.0
        var row = 0
        val svgBrowserFix = CheckBox()
        svgBrowserFix.isSelected = exportSVGWithBrowserCompatibility()
        svgBrowserFix.onAction = EventHandler { actionEvent: ActionEvent? ->
            exportSVGWithBrowserCompatibility(svgBrowserFix.isSelected)
        }
        optionsPane.children.add(svgBrowserFix)
        GridPane.setConstraints(svgBrowserFix, 0, row)
        var l = Label("Set Browser Compatibility for SVG Export")
        optionsPane.children.add(l)
        GridPane.setConstraints(l, 1, row++)
    }

}