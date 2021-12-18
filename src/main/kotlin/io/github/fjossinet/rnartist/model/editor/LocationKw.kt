package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.ScriptEditor

class LocationKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " location", indentLevel, inFinalScript) {

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            if (editor.mediator.canvas2D.getSelectedPositions().isNotEmpty()) {
                val blocks = Location(editor.mediator.canvas2D.getSelectedPositions().toIntArray()).blocks
                blocks.forEachIndexed() { index, block ->
                    this.children.add(
                        this.children.size-1,
                        OptionalDSLParameter(
                            editor,
                            "range",
                            IntegerField(editor, block.start),
                            Operator(editor, "to"),
                            IntegerField(editor, block.end),
                            this.indentLevel + 1,
                            inFinalScript = true,
                            canBeMultiple = index == blocks.size-1
                        )
                    )
                }
            }
            else
                this.children.add(
                    1,
                    OptionalDSLParameter(
                        editor,
                        "range",
                        IntegerField(editor, 1),
                        Operator(editor, "to"),
                        IntegerField(editor, 10,),
                        this.indentLevel + 1,
                        canBeMultiple = true
                    )
                )
        }
    }
}