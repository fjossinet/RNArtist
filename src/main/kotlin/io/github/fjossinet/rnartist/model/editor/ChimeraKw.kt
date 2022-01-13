package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.model.editor.*

class ChimeraKw (parent:RNArtistKw, editor: Script, indentLevel:Int): OptionalDSLKeyword(parent, editor,  "chimera", indentLevel) {

    init {
        this.children.add(DSLParameter(this, script, StringWithoutQuotes(this, script,"path"), Operator(this, script,"="), DirectoryField(this, script), this.indentLevel+1))
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
    }
}