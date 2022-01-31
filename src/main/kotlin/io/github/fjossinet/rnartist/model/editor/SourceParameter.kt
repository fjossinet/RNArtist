package io.github.fjossinet.rnartist.model.editor

import io.github.fjossinet.rnartist.gui.editor.Script

class SourceParameter(parent:DSLElement, script: Script, indentLevel:Int):OptionalDSLParameter(parent,script,null, StringWithoutQuotes(parent, script,"source"), Operator(parent, script, "="), StringValueWithQuotes(parent, script, "",editable = true), indentLevel ) {

    override fun dumpText(text: StringBuilder, useAbsolutePath: Boolean) {
        if (inFinalScript) {
            var inputPath = this.value.text.text.replace("\"", "")
            if (inputPath.startsWith("local:file:")) {
                inputPath = inputPath.split("local:file:").last()
                if (useAbsolutePath && !inputPath.startsWith("/") /*unix*/ && !inputPath.matches(Regex("^[A-Z]:/.+")) /*windows*/ && script.mediator.scriptEditor.currentScriptLocation != null) {
                    inputPath =
                        "${
                            script.mediator.scriptEditor.currentScriptLocation?.absolutePath?.replace(
                                "\\",
                                "/"
                            )
                        }/$inputPath"
                }
                inputPath = "local:file:$inputPath"
            }
            (0 until indentLevel).forEach {
                text.append(" ")
            }
            text.append("source = \"$inputPath\"")
            text.append(System.lineSeparator())
        }

    }
}