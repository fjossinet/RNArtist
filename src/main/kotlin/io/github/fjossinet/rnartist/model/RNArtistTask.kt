package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.gui.TaskDialog
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Task

abstract class RNArtistTask<T>(val mediator: Mediator) : Task<Pair<T?, Exception?>>() {
    lateinit var rnartistDialog: TaskDialog
    var stepProperty = SimpleObjectProperty<Pair<Int,Int>?>()
}

