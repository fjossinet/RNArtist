package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class SVGKw(parent: RNArtistKw, editor: Script, indentLevel: Int) :
    OptionalDSLKeyword(parent, editor, "svg", indentLevel) {

    var path: String? = null
        get() = (this.children.get(0) as DSLParameter).value.text.text.replace("\"", "")

    var width: Double = 800.0
        get() = if ((this.children.get(1) as OptionalDSLParameter).inFinalScript)
            (this.children.get(1) as OptionalDSLParameter).value.text.text.toDouble()
        else
            800.0

    var height: Double = 800.0
        get() = if ((this.children.get(2) as OptionalDSLParameter).inFinalScript)
            (this.children.get(2) as OptionalDSLParameter).value.text.text.toDouble()
        else
            800.0

    var name: String? = null
        get() =
            if ((this.children.get(3) as OptionalDSLParameter).inFinalScript)
                (this.children.get(3) as OptionalDSLParameter).value.text.text.replace("\"", "")
            else
                null

    init {
        this.children.add(
            DSLParameter(
                this,
                script,
                StringWithoutQuotes(this, script, "path"),
                Operator(this, script, "="),
                DirectoryField(this, script),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(
                this,
                script,
                null,
                StringWithoutQuotes(this, script, "width"),
                Operator(this, script, "="),
                FloatField(this, script, "800.0"),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(
                this,
                script,
                null,
                StringWithoutQuotes(this, script, "height"),
                Operator(this, script, "="),
                FloatField(this, script, "800.0"),
                this.indentLevel + 1
            )
        )
        this.children.add(
            OptionalDSLParameter(
                this,
                script,
                null,
                StringWithoutQuotes(this, script, "name"),
                Operator(this, script, "="),
                StringValueWithQuotes(this, script, editable = true),
                this.indentLevel + 1
            )
        )

        addButton.mouseReleased = {
            val p = this.searchFirst { it is DSLParameter } as DSLParameter
            val l = script.mediator.scriptEditor.currentScriptLocation
            p.value.text.text = if (l == null)
                ""
            else
                "\"${l.absolutePath.replace("\\", "/")}\""
            this.inFinalScript = true
            script.initScript()
        }
    }

    fun setChainName(chainName: String) {
        val p = this.searchFirst { it is OptionalDSLParameter && "name".equals(it.key.text.text) } as OptionalDSLParameter
        p.value.text.text = "${chainName}"
        this.addButton.fire()
        p.addButton.fire()
    }

}