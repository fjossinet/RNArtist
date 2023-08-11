package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.io.parseDSLScript
import io.github.fjossinet.rnartist.core.model.JunctionDrawing
import io.github.fjossinet.rnartist.core.model.RNArtistEl
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.gui.SelectionShape
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import java.io.FileReader

/**
 * An RNArtistDrawing is a SecondaryStructureDrawing from rnartistcore linked to additional data (selection shapes, the tree of DSLElement to synchronize the user modifications with the dsl script in the database
 */
class RNArtistDrawing(val mediator: Mediator, val drawing: SecondaryStructureDrawing, val dslScriptAbsolutePath:String) {

    val selectionShapes = FXCollections.observableArrayList<SelectionShape>()
    var rnArtistEl:RNArtistEl? = null

    init {
        parseDSLScript(FileReader(dslScriptAbsolutePath)).let {
            this.rnArtistEl = it
        }

        this.selectionShapes.addListener( ListChangeListener {
            val junctionsSelected = this.selectionShapes.filter { it.element is JunctionDrawing }
            if (junctionsSelected.isEmpty() || junctionsSelected.size > 1)
                mediator.rnartist.junctionSelectionKnob.selectedJunction = null
            else //only on junction has to be selected to change its layout
                mediator.rnartist.junctionSelectionKnob.selectedJunction = junctionsSelected.first().element as JunctionDrawing
        })
    }

}