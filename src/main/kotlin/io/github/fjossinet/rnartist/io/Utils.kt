package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.gui.editor.ScriptEditor
import io.github.fjossinet.rnartist.model.editor.*
import javafx.scene.paint.Color
import okio.ByteString.Companion.encode
import java.awt.Image
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO


@Throws(java.lang.Exception::class)
fun parseScript(reader: Reader, editor:ScriptEditor): DSLElement {
    val text = reader.readText().replace("\n","")
    class Element(var name:String, content:String, val start:Int, val end:Int, val children:MutableList<Element> = mutableListOf()) {

        var attributes = mutableListOf<String>()

        init {
            var _content = content.removePrefix(name) //the same is in the content. its '{' will be a pb for the newt step.
            name = name.replace("{", "").trim()
            //we will remote all the children element to keep only the attributes at this level
            var regex = Regex("[a-z]+\\s*\\{.+\\}")
            var hit = regex.find(_content)
            while (hit != null) {
                _content =  _content.removeRange(hit.range).trim()
                hit = regex.find(_content)
            }
            //we split the attributes. First we match the attributes with a value inside quotes (since we can have spaces inside the quotes). Then we match attributes whose value is without any spaces (then we match until we meet a space)
            regex = Regex("(\\S+\\s*(to|=)\\s*\".+?\"|\\S+\\s*(to|=)\\s*\\S+)")
            regex.findAll(_content).forEach {
                attributes.add(it.value)
            }
        }

        fun print(indent:String) {
            println(indent+""+this.name+": "+attributes.joinToString(separator = "|"))
            children.forEach {
                it.print(indent+"    ")
            }
        }

        //function to check of a direct child can be a sub-child, or sub-sub-child...
        fun cleanChildren() {
            val toRemove = mutableListOf<Element>()
            children.forEach { c ->
                children.forEach {
                    if (it.contains(c))
                        toRemove.add(c)
                }
            }
            children.removeAll(toRemove)
        }

        fun contains(el:Element):Boolean {
            if (this.children.contains(el))
                return true
            else
                return this.children.map { it.children.contains(el) }.contains(true)
            return false
        }

    }
    var elements = mutableListOf<Element>()
    var regex = Regex("[a-z]+\\s?\\{") //this regexp matches "keyword {"
    val openBrackets = regex.findAll(text)
    regex = Regex("\\}")
    val closedBrackets = regex.findAll(text)
    val allMatches = mutableListOf<MatchResult>()
    allMatches.addAll(openBrackets)
    allMatches.addAll(closedBrackets)
    allMatches.sortBy { it.range.start } //we order the matches according to their position in the text.
    var lastOpenBrackets = mutableListOf<MatchResult>()
    allMatches.forEach {
        if (it.value.trim().startsWith("}")) { //like a dot-bracket notation. a closing bracket match the last open bracket.
            var last = lastOpenBrackets.removeLast()
            elements.add(Element(last.value, text.substring(last.range.start, it.range.start), last.range.start, it.range.start))
        }
        if (it.value.trim().endsWith("{")) {
            lastOpenBrackets.add(it)
        }
    }

    //now we want to know which element is inside another one according to their positions in the text
    elements.forEach { element ->
        elements.forEach {
            if (it.start > element.start && it.end < element.end) {
                element.children.add(it)
            }
        }
    }

    //a child can have several parent according to the positions in the text, we clean that.
    elements.forEach {
        it.cleanChildren()
    }

    val root = RNArtistKw(editor)

    elements.forEach { element ->
        when (element.name) {
            "ss" ->  {
                val secondaryStructureKw = root.searchFirst { it is SecondaryStructureKw} as SecondaryStructureKw
                element.children.forEach { elementChild ->
                    when (elementChild.name) {
                        "bn" -> {
                            val bnKw = (secondaryStructureKw.searchFirst { themeChild -> themeChild is BracketNotationKw && !themeChild.inFinalScript } as BracketNotationKw)
                            bnKw.addToFinalScript(true)
                            elementChild.attributes.forEach { attribute ->
                                val tokens = attribute.split("=")
                                if ("value".equals(tokens.first().trim())) {
                                    val parameter = (bnKw.searchFirst { it is DSLParameter && "value".equals(it.key.text.text) } as DSLParameter)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                                if ("seq".equals(tokens.first().trim())) {
                                    val parameter = (bnKw.searchFirst { it is SequenceBnParameter} as SequenceBnParameter)
                                    parameter.value.text.text = tokens.last().trim()
                                    parameter.addToFinalScript(true)
                                }
                            }
                        }
                    }
                }
            }
            "theme" -> {
                val themeKw = root.searchFirst { it is ThemeKw} as ThemeKw
                themeKw.addToFinalScript(true)
                element.children.forEach { elementChild ->
                    when(elementChild.name) {
                        "color" -> {
                            val colorKw =
                                themeKw.searchFirst { themeChild -> themeChild is ColorKw && !themeChild.inFinalScript } as ColorKw
                            colorKw.addToFinalScript(true)
                            elementChild.attributes.forEach { attribute ->
                                val tokens = attribute.split("=")
                                if ("value".equals(tokens.first().trim())) {
                                    val parameter =
                                        (colorKw.searchFirst { it is DSLParameter && "value".equals(it.key.text.text) } as DSLParameter)
                                    parameter.value.text.text = tokens.last().trim()
                                    parameter.value.text.fill = Color.web(tokens.last().trim().replace("\"", ""))
                                }
                                if ("type".equals(tokens.first().trim())) {
                                    val parameter =
                                        (colorKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                    parameter.addToFinalScript(true)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                            }
                            elementChild.children.forEach { elementChildChild ->
                                when (elementChildChild.name) {
                                    "location" -> {
                                        val locationKw =
                                            colorKw.searchFirst { colorChild -> colorChild is LocationKw && !colorChild.inFinalScript } as LocationKw
                                        locationKw.addToFinalScript(true)
                                        elementChildChild.attributes.forEach { attribute ->
                                            val tokens = attribute.split("to")
                                            val parameter =
                                                locationKw.searchFirst { it is OptionalDSLParameter && !it.inFinalScript } as OptionalDSLParameter
                                            parameter.addToFinalScript(true)
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }
                            }
                        }
                        "line" -> {
                            val lineKw =
                                themeKw.searchFirst { themeChild -> themeChild is LineKw && !themeChild.inFinalScript } as LineKw
                            lineKw.addToFinalScript(true)
                            elementChild.attributes.forEach { attribute ->
                                val tokens = attribute.split("=")
                                if ("value".equals(tokens.first().trim())) {
                                    val parameter =
                                        (lineKw.searchFirst { it is DSLParameter && "value".equals(it.key.text.text) } as DSLParameter)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                                if ("type".equals(tokens.first().trim())) {
                                    val parameter =
                                        (lineKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                    parameter.addToFinalScript(true)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                            }
                            elementChild.children.forEach { elementChildChild ->
                                when (elementChildChild.name) {
                                    "location" -> {
                                        val locationKw =
                                            lineKw.searchFirst { lineChild -> lineChild is LocationKw && !lineChild.inFinalScript } as LocationKw
                                        locationKw.addToFinalScript(true)
                                        elementChildChild.attributes.forEach { attribute ->
                                            val tokens = attribute.split("to")
                                            val parameter =
                                                locationKw.searchFirst { it is OptionalDSLParameter && !it.inFinalScript } as OptionalDSLParameter
                                            parameter.addToFinalScript(true)
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }
                            }
                        }
                        "show" -> {
                            val showKw =
                                themeKw.searchFirst { themeChild -> themeChild is ShowKw && !themeChild.inFinalScript } as ShowKw
                            showKw.addToFinalScript(true)
                            elementChild.attributes.forEach { attribute ->
                                val tokens = attribute.split("=")
                                if ("type".equals(tokens.first().trim())) {
                                    val parameter =
                                        (showKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                    parameter.addToFinalScript(true)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                            }
                            elementChild.children.forEach { elementChildChild ->
                                when (elementChildChild.name) {
                                    "location" -> {
                                        val locationKw =
                                            showKw.searchFirst { showChild -> showChild is LocationKw && !showChild.inFinalScript } as LocationKw
                                        locationKw.addToFinalScript(true)
                                        elementChildChild.attributes.forEach { attribute ->
                                            val tokens = attribute.split("to")
                                            val parameter =
                                                locationKw.searchFirst { it is OptionalDSLParameter && !it.inFinalScript } as OptionalDSLParameter
                                            parameter.addToFinalScript(true)
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }
                            }
                        }
                        "hide" -> {
                            val hideKw =
                                themeKw.searchFirst { themeChild -> themeChild is HideKw && !themeChild.inFinalScript } as HideKw
                            hideKw.addToFinalScript(true)
                            elementChild.attributes.forEach { attribute ->
                                val tokens = attribute.split("=")
                                if ("type".equals(tokens.first().trim())) {
                                    val parameter =
                                        (hideKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                    parameter.addToFinalScript(true)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                            }
                            elementChild.children.forEach { elementChildChild ->
                                when (elementChildChild.name) {
                                    "location" -> {
                                        val locationKw =
                                            hideKw.searchFirst { hideChild -> hideChild is LocationKw && !hideChild.inFinalScript } as LocationKw
                                        locationKw.addToFinalScript(true)
                                        elementChildChild.attributes.forEach { attribute ->
                                            val tokens = attribute.split("to")
                                            val parameter =
                                                locationKw.searchFirst { it is OptionalDSLParameter && !it.inFinalScript } as OptionalDSLParameter
                                            parameter.addToFinalScript(true)
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "layout" -> {
                val layoutKw = root.searchFirst { it is LayoutKw} as LayoutKw
                layoutKw.addToFinalScript(true)
                element.children.forEach { elementChild ->
                    when(elementChild.name) {
                        "junction" -> {
                            val junctionKw = layoutKw.searchFirst { layoutChild -> layoutChild is JunctionKw && !layoutChild.inFinalScript } as JunctionKw
                            junctionKw.addToFinalScript(true)
                            elementChild.attributes.forEach { attribute ->
                                val tokens = attribute.split("=")
                                if ("out_ids".equals(tokens.first().trim())) {
                                    val parameter =
                                        (junctionKw.searchFirst { it is OptionalDSLParameter && "out_ids".equals(it.key.text.text) } as OptionalDSLParameter)
                                    parameter.addToFinalScript(true)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                                if ("type".equals(tokens.first().trim())) {
                                    val parameter =
                                        (junctionKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                    parameter.addToFinalScript(true)
                                    parameter.value.text.text = tokens.last().trim()
                                }
                            }
                            elementChild.children.forEach { elementChildChild ->
                                when (elementChildChild.name) {
                                    "location" -> {
                                        val locationKw =
                                            junctionKw.searchFirst { junctionChild -> junctionChild is LocationKw && !junctionChild.inFinalScript } as LocationKw
                                        locationKw.addToFinalScript(true)
                                        elementChildChild.attributes.forEach { attribute ->
                                            val tokens = attribute.split("to")
                                            val parameter =
                                                locationKw.searchFirst { it is OptionalDSLParameter && !it.inFinalScript } as OptionalDSLParameter
                                            parameter.addToFinalScript(true)
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    return root
}


fun getImage(imageName: String?): Image? {
    if (imageName == null) {
        return null
    }
    var image: Image? = null
    try {
        image = ImageIO.read({}.javaClass.getResource("images/$imageName"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return image
}

fun javaFXToAwt(c: Color): java.awt.Color {
    return java.awt.Color(c.red.toFloat(),
            c.green.toFloat(),
            c.blue.toFloat(),
            c.opacity.toFloat())
}

fun awtColorToJavaFX(c: java.awt.Color): Color {
    return Color.rgb(c.red, c.green, c.blue, c.alpha / 255.0)
}

fun sendFile(out: OutputStream, name: String, `in`: InputStream, fileName: String) {
    val o = """
        Content-Disposition: form-data; name="${
        URLEncoder.encode(
            name,
            "UTF-8"
        )
    }"; filename="${URLEncoder.encode(fileName, "UTF-8")}"
        
        
        """.trimIndent()
    out.write(o.encode(StandardCharsets.UTF_8).toByteArray())
    val buffer = ByteArray(2048)
    var n = 0
    while (n >= 0) {
        out.write(buffer, 0, n)
        n = `in`.read(buffer)
    }
    out.write("\r\n".encode(StandardCharsets.UTF_8).toByteArray())
}

fun sendField(out: OutputStream, name: String, field: String) {
    val o = """
        Content-Disposition: form-data; name="${URLEncoder.encode(name, "UTF-8")}"
        
        
        """.trimIndent()
    out.write(o.encode(StandardCharsets.UTF_8).toByteArray())
    out.write(URLEncoder.encode(field, "UTF-8").encode(StandardCharsets.UTF_8).toByteArray())
    out.write("\r\n".encode(StandardCharsets.UTF_8).toByteArray())
}