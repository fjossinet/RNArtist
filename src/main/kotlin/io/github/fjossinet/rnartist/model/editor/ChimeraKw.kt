package io.github.fjossinet.rnartist.io.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script
import io.github.fjossinet.rnartist.model.editor.*

class ChimeraKw (editor: Script, indentLevel:Int): OptionalDSLKeyword(editor,  " chimera ", indentLevel) {

    init {
        this.children.add(1, DSLParameter(script, StringWithoutQuotes(script,"path"), Operator(script,"="), DirectoryField(script), this.indentLevel+1))
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