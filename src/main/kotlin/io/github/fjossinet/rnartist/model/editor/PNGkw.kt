package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class PNGKw (parent:RNArtistKw, script: Script, indentLevel:Int): OptionalDSLKeyword(parent, script,  "png", indentLevel) {

    init {
        this.children.add(
            DSLParameter(this,
                script,
                StringWithoutQuotes(this, script, "path"),
                Operator(this, script, "="),
                DirectoryField(this, script),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(this, script, "width"),
                Operator(this, script, "="),
                FloatField(this, script, "800.0"),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(this, script, "height"),
                Operator(this, script, "="),
                FloatField(this, script, "800.0"),
                this.indentLevel + 1
            )
        )
        addButton.mouseReleased = {
            val p = this.searchFirst { it is DSLParameter } as DSLParameter
            val l = script.mediator.scriptEditor?.currentScriptLocation
            p.value.text.text = if (l == null)
                ""
            else
                "\"${l.absolutePath.replace("\\", "/")}\""
            this.inFinalScript = true
            script.initScript()
        }

        removeButton.mouseReleased =  {
            this.inFinalScript = false
            val index = this.parent.children.indexOf(this)
            this.parent.children.remove(this)
            this.parent.children.add(index, PNGKw(this.parent as RNArtistKw, script, this.indentLevel))
            script.initScript()
        }
    }

}