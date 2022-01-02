package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Helix
import io.github.fjossinet.rnartist.gui.editor.Script

class HelixKw(script: Script, indentLevel: Int, var helix:Helix?=null) : OptionalDSLKeyword(script, " helix", indentLevel) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            this.helix?.let { helix ->
                val l = HelixLocationKw(script, this.indentLevel + 1, l = helix.location)
                l.addToFinalScript(true)
                this.children.add(1, l)
                this.children.add(
                    1,
                    OptionalDSLParameter(
                        script,
                        null,
                        StringWithoutQuotes(script, "name"),
                        Operator(script, "="),
                        StringValueWithQuotes(script, helix.name, editable = true),
                        this.indentLevel + 1,
                        inFinalScript = true
                    )
                )
                this.children.add(HelixKw(script, this.indentLevel))
            } ?: run {
                this.children.add(1, InteractionKw(script, this.indentLevel + 1))
                this.children.add(1, HelixLocationKw(script, this.indentLevel + 1))
                this.children.add(
                    1,
                    OptionalDSLParameter(
                        script,
                        null,
                        StringWithoutQuotes(script, "name"),
                        Operator(script, "="),
                        StringValueWithQuotes(script, "my helix", editable = true),
                        this.indentLevel + 1
                    )
                )
                this.children.add(HelixKw(script, this.indentLevel))
            }
        }
    }

}