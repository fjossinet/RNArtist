package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Helix
import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

class HelixKw(parent: PartsKw, script: Script, indentLevel: Int) :
    OptionalDSLKeyword(parent, script, "helix", indentLevel) {

    init {
        this.children.add(
            OptionalDSLParameter(
                this,
                script,
                null,
                StringWithoutQuotes(this, script, "name"),
                Operator(this, script, "="),
                StringValueWithQuotes(this, script, "my helix", editable = true),
                this.indentLevel + 1
            )
        )
        this.children.add(HelixLocationKw(this, script, this.indentLevel + 1))
        this.children.add(InteractionKw(this, script, this.indentLevel + 1))

        this.text.onMouseReleased = EventHandler{
            (this.searchFirst { it is HelixLocationKw } as HelixLocationKw).getLocation().let { l ->
                script.mediator.drawingDisplayed.get()?.let here@{ drawingLoaded ->
                    drawingLoaded.drawing.allHelices.forEach { h ->
                        if (h.location.equals(l)) {
                            script.mediator.canvas2D.clearSelection()
                            script.mediator.canvas2D.addToSelection(h)
                            script.mediator.canvas2D.fitStructure(script.mediator.canvas2D.getSelectionFrame(), ratio = 3.0)
                            return@here
                        }
                    }
                }
            }
        }

        addButton.mouseReleased = {
            this.inFinalScript = true
            if (this.parent.children.indexOf(this) == this.parent.children.size-1 || this.parent.children.get(this.parent.children.indexOf(this)+1) !is HelixKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, HelixKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            if (this.parent.children.indexOf(this) <this.parent.children.size-1 && this.parent.children.get(this.parent.children.indexOf(this) + 1) is HelixKw)
                this.parent.children.remove(this)
            else if (this.parent.children.indexOf(this) > 0 && this.parent.children.get(this.parent.children.indexOf(this) - 1) is HelixKw)
                this.parent.children.remove(this)
            script.initScript()
        }
    }

    fun setHelix(helix: Helix) {
        val helixLocationKw = this.searchFirst { it is HelixLocationKw && !it.inFinalScript } as HelixLocationKw
        helixLocationKw.setLocation(helix.location)
        val parameter = this.searchFirst { it is OptionalDSLParameter && "name".equals(it.key.text.text.trim()) } as OptionalDSLParameter
        (parameter.value as StringValueWithQuotes).setText(helix.name)
        parameter.addButton.fire()
        this.addButton.fire()
    }

    fun getLocation(): Location? = (this.searchFirst { it is HelixLocationKw } as HelixLocationKw?)?.getLocation()

    fun setLocation(location: Location) {
        (this.searchFirst { it is HelixLocationKw } as HelixLocationKw).setLocation(location)
        this.addButton.fire()
    }

}