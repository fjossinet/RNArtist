package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.gui.JunctionKnob
import io.github.fjossinet.rnartist.gui.SelectionShape
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import org.dizitart.no2.NitriteId
import java.io.File

interface DrawingLoaded {

    val drawing: SecondaryStructureDrawing

    val selectionShapes: ObservableList<SelectionShape>
    val knobs : MutableList<JunctionKnob>
    var tmpChimeraSession: Pair<File, File>?
}

abstract class AbstractDrawingLoaded(protected val mediator: Mediator, override val drawing: SecondaryStructureDrawing):DrawingLoaded {

    override val selectionShapes = FXCollections.observableArrayList<SelectionShape>()
    override val knobs = mutableListOf<JunctionKnob>()
    override var tmpChimeraSession: Pair<File, File>? = null

    init {
        this.selectionShapes.addListener(ListChangeListener { change ->
            if (!this.selectionShapes.isEmpty() && mediator.chimeraDriver.pdbFile != null) {
                mediator.rnartist.paintSelectionin3D.isDisable = false
            } else {
                mediator.rnartist.paintSelectionin3D.isDisable = true
            }
        })
    }

}

class DrawingLoadedFromFile( mediator: Mediator,drawing: SecondaryStructureDrawing, val file: File):AbstractDrawingLoaded(mediator, drawing) {

    override fun toString(): String {
        return "RNA ${drawing.secondaryStructure.rna.name} ${drawing.secondaryStructure.rna.length}nts from file ${file.name}"
    }
}

class DrawingLoadedFromScriptEditor(mediator: Mediator, drawing: SecondaryStructureDrawing, val id:String):AbstractDrawingLoaded(mediator, drawing) {

    override fun toString(): String {
        return "RNA ${drawing.secondaryStructure.rna.name} ${drawing.secondaryStructure.rna.length}nts loaded from script editor"
    }
}

class DrawingLoadedFromRNArtistDB( mediator: Mediator,drawing: SecondaryStructureDrawing, val id: NitriteId, var projectName:String):AbstractDrawingLoaded(mediator, drawing) {

    fun getChimeraSession(): File? {
        val f = File(File(mediator.embeddedDB.rootDir, "chimera_sessions"), "$id.py")
        return when(f.exists()) {
            true -> f
            false -> null
        }
    }

    fun getPdbFile(): File? {
        val f = File(File(mediator.embeddedDB.rootDir, "chimera_sessions"), "$id.pdb")
        return when(f.exists()) {
            true -> f
            false -> null
        }
    }

    override fun toString(): String {
        return "RNA ${drawing.secondaryStructure.rna.name} ${drawing.secondaryStructure.rna.length}nts from project ${projectName}"
    }
}