package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class PNGKw (script: Script, indentLevel:Int): OptionalDSLKeyword(script,  " png ", indentLevel) {

    init {
        this.children.add(
            1,
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(script, "width"),
                Operator(script, "="),
                FloatField(script, "800.0"),
                this.indentLevel + 1
            )
        )
        this.children.add(
            1,
            OptionalDSLParameter(this,
                script,
                null,
                StringWithoutQuotes(script, "height"),
                Operator(script, "="),
                FloatField(script, "800.0"),
                this.indentLevel + 1
            )
        )
        this.children.add(
            1,
            DSLParameter(
                script,
                StringWithoutQuotes(script, "path"),
                Operator(script, "="),
                DirectoryField(script),
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
    }

}