package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Block
import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

open class LocationKw(parent:DSLElement, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script, "location", indentLevel) {

    init {
        this.text
        val p = OptionalDSLParameter(
            this,
            script,
            "range",
            StringWithoutQuotes(this, script, editable = true),
            Operator(this, script, "to"),
            StringWithoutQuotes(this, script, editable = true),
            this.indentLevel + 1,
            canBeMultiple = true
        )
        this.children.add(
            p
        )

        this.text.onMouseReleased = EventHandler {
            script.mediator.canvas2D.clearSelection()
            this.getLocation()?.let { l ->
                (this.parent as? JunctionLayoutKw)?.let {
                    script.mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                        drawingLoaded.drawing.allJunctions.forEach { j ->
                            if (j.inside(l))
                                script.mediator.canvas2D.addToSelection(j)
                        }
                        script.mediator.canvas2D.fitStructure(script.mediator.canvas2D.getSelectionFrame(), ratio = 3.0)
                    }
                } ?: run {
                    script.mediator.drawingDisplayed.get()?.let { drawingLoaded ->
                        drawingLoaded.drawing.residues.forEach { r ->
                            if (r.inside(l))
                                script.mediator.canvas2D.addToSelection(r)
                        }
                        script.mediator.canvas2D.fitStructure(script.mediator.canvas2D.getSelectionFrame(), ratio = 3.0)
                    }
                }
            }
        }

        addButton.mouseReleased = {
            if (getLocation() == null && script.mediator.canvas2D.getSelectedPositions().isNotEmpty()) {
                val location = Location(script.mediator.canvas2D.getSelectedPositions().toIntArray())
                location.blocks.forEachIndexed() { index, block ->
                    val p = this.searchFirst { it is OptionalDSLParameter && !it.inFinalScript && "to".equals(it.operator.text.text.trim()) } as OptionalDSLParameter
                    (p.key as StringWithoutQuotes).setText(block.start.toString())
                    (p.value as StringWithoutQuotes).setText(block.end.toString())
                    p.addButton.fire()
                }
            }
            this.inFinalScript = true
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val index = this.parent.children.indexOf(this)
            this.parent.children.remove(this)
            this.parent.children.add(index, LocationKw(this.parent as DSLElement, script, this.indentLevel))
            script.initScript()
        }
    }

    fun setLocation(location: Location) {
        location.blocks.forEachIndexed() { index, block ->
            val p = this.searchFirst { it is OptionalDSLParameter && !it.inFinalScript && "to".equals(it.operator.text.text.trim()) } as OptionalDSLParameter
            (p.key as StringWithoutQuotes).setText(block.start.toString())
            (p.value as StringWithoutQuotes).setText(block.end.toString())
            p.addButton.fire()
        }
        this.addButton.fire()
    }

    fun getLocation(): Location? {
        var blocks = mutableListOf<Block>()
        this.children.filter { it is OptionalDSLParameter && it.inFinalScript && "to".equals(it.operator.text.text.trim()) }
            .forEach { range ->
                (range as? OptionalDSLParameter)?.let {
                    blocks.add(Block(it.key.text.text.toInt(), it.value.text.text.toInt()))
                }
            }
        if (blocks.isEmpty())
            return null
        return Location(blocks)
    }
}