package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.core.model.Block
import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import javafx.scene.Node

class LocationKw(editor: ScriptEditor, indentLevel:Int, inFinalScript:Boolean = false): OptionalDSLKeyword(editor, " location", indentLevel, inFinalScript) {

    var location:Location?
        get() {
            var blocks = mutableListOf<Block>()
            this.children.filter {it is OptionalDSLParameter && it.inFinalScript && "to".equals(it.operator.text.text.trim()) }.forEach { range ->
                (range as? OptionalDSLParameter)?.let {
                    blocks.add(Block(it.key.text.text.toInt(), it.value.text.text.toInt()))
                }
            }
            if (blocks.isEmpty())
                return null
            return Location(blocks)
        }
        private set(value) {
            //remove the previous Parameters making the current location
            this.children.removeIf { it is OptionalDSLParameter && "to".equals(it.operator.text.text.trim())  }
            //adding the new one
            value?.blocks?.forEachIndexed() { index, block ->
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
                        canBeMultiple = index == value.blocks.size-1
                    )
                )
            }
        }

    override fun addToFinalScript(add: Boolean) {
        super.addToFinalScript(add)
        if (add) {
            if (editor.mediator.canvas2D.getSelectedPositions().isNotEmpty())
                this.location =  Location(editor.mediator.canvas2D.getSelectedPositions().toIntArray())
            else
                this.children.add(
                    this.children.size-1,
                    OptionalDSLParameter(
                        editor,
                        "range",
                        IntegerField(editor,1),
                        Operator(editor, "to"),
                        IntegerField(editor, 10),
                        this.indentLevel + 1,
                        inFinalScript = false,
                        canBeMultiple = true
                    )
                )
        }
    }
}