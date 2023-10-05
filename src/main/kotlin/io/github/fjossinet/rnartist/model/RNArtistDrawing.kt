package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.DrawingElement
import io.github.fjossinet.rnartist.core.model.RNArtistEl
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import javafx.collections.FXCollections

/**
 * An RNArtistDrawing is a SecondaryStructureDrawing from rnartistcore linked to additional data (the tree of DSLElement to synchronize the user modifications with the dsl script in the database
 */
class RNArtistDrawing(
    val mediator: Mediator,
    val secondaryStructureDrawing: SecondaryStructureDrawing,
    val dslScriptInvariantSeparatorsPath: String,
    val rnArtistEl:RNArtistEl
) {

    val selectedDrawings = FXCollections.observableArrayList<DrawingElement>()

}