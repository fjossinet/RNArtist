package io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import java.awt.geom.Rectangle2D

/**
 * An RNArtistDrawing is a SecondaryStructureDrawing from RNArtistCore linked to additional features:
 * - the tree of DSLElement to sync between the drawing and a DSL script
 * - the drawing elements selected in the Canvas 2D
 */
class RNArtistDrawing(
    val mediator: Mediator,
    val secondaryStructureDrawing: SecondaryStructureDrawing,
    var dslScriptInvariantSeparatorsPath: String,
    val rnArtistEl: RNArtistEl
) {

    val selectedDrawings = FXCollections.observableArrayList<DrawingElement>()
    private val addedSteps = FXCollections.observableArrayList<Int>()

    init {
        selectedDrawings.addListener(ListChangeListener {
            mediator.canvas2D.repaint()
            it.next()
            if (it.wasAdded()) {
                addedSteps.add(it.addedSize)
            }
        })
        addedSteps.addListener(ListChangeListener {
            it.next()
            if (it.wasRemoved()) {
                selectedDrawings.remove(selectedDrawings.size - it.removed.get(0), selectedDrawings.size)
            }
        })
    }

    fun extendSelection() {
        if (this.selectedDrawings.isNotEmpty())
            this.addToSelection(this.selectedDrawings.first()) //we did as if the user clicked on more time on the first selected element
    }

    fun reduceSelection() {
        if (selectedDrawings.size > 1) //we dont want to remove the first selected element to be able to re-extend the selection
            addedSteps.removeAt(addedSteps.lastIndex)
    }

    private fun nexUnselectedStructuralDomains(froms:List<StructuralDomainDrawing>, unselectedStructuralDomains:MutableSet<StructuralDomainDrawing> = mutableSetOf(), alreadyProcessedStructuralDomains:MutableSet<StructuralDomainDrawing> = mutableSetOf()):Set<StructuralDomainDrawing> {
        val nextStructuralDomains = mutableListOf<StructuralDomainDrawing>()
        froms.forEach { from ->
            when (from) {
                is JunctionDrawing -> {
                    from.outHelices.forEach {
                        if (!alreadyProcessedStructuralDomains.contains(it)) {
                            nextStructuralDomains.add(it)
                            alreadyProcessedStructuralDomains.add(it)
                        }
                    }
                    secondaryStructureDrawing.allHelices.filter { it.helix == from.inHelix }.firstOrNull()?.let {
                        if (!alreadyProcessedStructuralDomains.contains(it)) {
                            nextStructuralDomains.add(it)
                            alreadyProcessedStructuralDomains.add(it)
                        }
                    }
                }

                is HelixDrawing -> {
                    secondaryStructureDrawing.allJunctions.filter { from.helix.junctionsLinked.first == it.junction || from.helix.junctionsLinked.second == it.junction }
                        .forEach {
                            if (!alreadyProcessedStructuralDomains.contains(it)) {
                                nextStructuralDomains.add(it)
                                alreadyProcessedStructuralDomains.add(it)
                            }
                        }
                }
            }
        }
        unselectedStructuralDomains.addAll(nextStructuralDomains.filter { !selectedDrawings.contains(it) })
        if (unselectedStructuralDomains.isEmpty() && nextStructuralDomains.isNotEmpty()) { //we dit not catch any unselected structural domains. We need to go one step further
            return nexUnselectedStructuralDomains(nextStructuralDomains, unselectedStructuralDomains, alreadyProcessedStructuralDomains)
        }
        else
            return unselectedStructuralDomains
    }

    fun addToSelection(element: DrawingElement?) {
        element?.let {
            when (element) {
                //###### Structural domains selection #################
                /**
                 * If a structural domain is already selected, it is conserved and the selection is extended to the structural doamins connected
                 */
                is JunctionDrawing -> {
                    if (!selectedDrawings.contains(element)) {
                        selectedDrawings.add(element)
                    } else {
                        this.selectedDrawings.addAll(nexUnselectedStructuralDomains(mutableListOf(element)).toList())
                    }
                }

                is HelixDrawing -> {
                    if (!selectedDrawings.contains(element)) {
                        selectedDrawings.add(element)
                    } else {
                        this.selectedDrawings.addAll(nexUnselectedStructuralDomains(mutableListOf(element)).toList())
                    }
                }

                is SingleStrandDrawing -> {
                    if (!selectedDrawings.contains(element)) {
                        selectedDrawings.add(element)
                    } else {

                    }
                }

                //###### Non structural domains selection #################
                /**
                 * If a non structural domain is already selected, it is removed from the selection to select one of its parents
                 */

                is PhosphodiesterBondDrawing -> {
                    if (element.pathToStructuralDomain().none { selectedDrawings.contains(it) }) {
                        selectedDrawings.add(element)
                    } else {
                        selectedDrawings.remove(element)
                        this.addToSelection(element.parent)
                    }
                }

                is ResidueDrawing -> {
                    if (element.pathToStructuralDomain().none { selectedDrawings.contains(it) }) {
                        selectedDrawings.add(element)
                    } else {
                        selectedDrawings.remove(element)
                        this.addToSelection(element.parent)
                    }
                }

                is InteractionSymbolDrawing -> {
                    if (element.pathToStructuralDomain().none { selectedDrawings.contains(it) }) {
                        selectedDrawings.add(element)
                    } else {
                        selectedDrawings.remove(element)
                        this.addToSelection(element.parent)
                    }
                }

                is SecondaryInteractionDrawing -> {
                    if (element.pathToStructuralDomain().none { selectedDrawings.contains(it) }) {
                        selectedDrawings.add(element)
                    } else {
                        selectedDrawings.remove(element)
                        this.addToSelection(element.parent)
                    }
                }

                else -> {

                }
            }
        }

    }

    fun clearSelection() {
        this.selectedDrawings.clear()
    }

    fun getSelection(): List<DrawingElement> = this.selectedDrawings.map { it }

    fun getSelectedPositions(): List<Int> {
        val absPositions = mutableSetOf<Int>()
        this.selectedDrawings.map { it }.forEach {
            absPositions.addAll(it.location.positions)
        }
        return absPositions.toList()
    }

    fun getSelectedResidues(): List<ResidueDrawing> =
        this.secondaryStructureDrawing.getResiduesFromAbsPositions(*getSelectedPositions().toIntArray())

    fun getSelectionFrame(): Rectangle2D? {
        val firstShape = this.selectedDrawings.firstOrNull()?.selectionShape
        firstShape?.let { firstShape ->
            var frame = firstShape.bounds2D
            this.selectedDrawings.forEach {
                frame = frame.createUnion(it.selectionShape.bounds2D)
            }
            return frame
        }
        return null
    }

    fun isSelected(el: DrawingElement?) = this.selectedDrawings.any { it == el }

    fun removeFromSelection(el: DrawingElement?) = this.selectedDrawings.removeIf { it == el }

    fun structuralDomainsSelected(): List<StructuralDomainDrawing> {
        val domains = mutableListOf<StructuralDomainDrawing>()
        domains.addAll(this.selectedDrawings.filter { it is StructuralDomainDrawing }
            .map { it as StructuralDomainDrawing })
        return domains
    }

}