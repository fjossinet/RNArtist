package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.gui.LinearButtonsPanel
import javafx.geometry.Pos

class DBExplorerButtonsPanel(mediator: Mediator): LinearButtonsPanel(mediator = mediator) {

    init {
        this.alignment = Pos.TOP_LEFT
    }

}