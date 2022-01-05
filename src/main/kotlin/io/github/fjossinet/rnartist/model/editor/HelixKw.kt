package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Helix
import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.Script
import javafx.event.EventHandler

class HelixKw(var parent: SecondaryStructureKw, script: Script, indentLevel: Int) :
    OptionalDSLKeyword(script, " helix", indentLevel) {

    init {
        this.children.add(1, InteractionKw(this, script, this.indentLevel + 1))
        this.children.add(1, HelixLocationKw(script, this.indentLevel + 1))
        this.children.add(
            1,
            OptionalDSLParameter(
                this,
                script,
                null,
                StringWithoutQuotes(script, "name"),
                Operator(script, "="),
                StringValueWithQuotes(script, "my helix", editable = true),
                this.indentLevel + 1
            )
        )

        addButton.onAction = EventHandler {
            this.inFinalScript = true
            if (this.parent.children.get(this.parent.children.indexOf(this)+1) !is HelixKw)
                this.parent.children.add(this.parent.children.indexOf(this) + 1, HelixKw(parent, script, indentLevel))
            script.initScript()
        }

        removeButton.onAction = EventHandler {
            this.inFinalScript = false
            val childAfter = this.parent.children.get(this.parent.children.indexOf(this) + 1)
            if (childAfter is HelixKw)
                this.parent.children.remove(this)
            else {
                val childBefore = this.parent.children.get(this.parent.children.indexOf(this) - 1)
                if (childBefore is HelixKw && !childBefore.inFinalScript)
                    this.parent.children.remove(this)
            }
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