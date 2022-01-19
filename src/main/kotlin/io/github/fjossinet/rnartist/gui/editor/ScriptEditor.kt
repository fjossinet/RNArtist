package io.github.fjossinet.rnartist.gui.editor

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.model.editor.*
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.TextFlow
import org.kordamp.ikonli.javafx.FontIcon
import java.io.*
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

abstract class Script(var mediator: Mediator) : TextFlow() {

    abstract protected var root: DSLElement

    //this is to avoid to call script init too much time (for example when several elements are added, and then addButtons are fired several times)
    var allowScriptInit = true

    open fun getScriptRoot(): DSLElement = this.root

    fun setScriptRoot(root: DSLElement) {
        this.root = root
        this.initScript()
    }

    abstract fun initScript()

}

class RNArtistScript(mediator: Mediator) : Script(mediator) {

    override var root: DSLElement = RNArtistKw(this)

    init {
        this.prefWidth = Region.USE_COMPUTED_SIZE
        this.initScript()
    }

    override fun getScriptRoot(): RNArtistKw = this.root as RNArtistKw

    override fun initScript() {
        if (this.allowScriptInit) {
            this.getScriptRoot()?.let {
                //currentJunctionBehaviors.clear() //we clear the current junction behaviors that can have been tweaked by the user
                //currentJunctionBehaviors.putAll(defaultJunctionBehaviors)
                this.children.clear()
                var nodes = mutableListOf<Node>()
                it.dumpNodes(nodes)
                this.children.addAll(nodes)
                this.layout()
            }
        }
    }

    fun setJunctionLayout(outIds: String, type: String, junctionLocation: Location) {
        with(getScriptRoot().getLayoutKw()) {
            allowScriptInit = false
            addButton.fire()
            val junctionLayoutKw =
                searchFirst { it is JunctionLayoutKw && it.inFinalScript && junctionLocation.equals(it.getLocation()) } as JunctionLayoutKw?
            //We have found a junctionKw with the same location, we update it
            junctionLayoutKw?.let {
                it.setOutIds(outIds) //We just need to change the outIds (type and location should be the same)
            } ?: run { //we create a new one
                val junctionLayoutKw = searchFirst { it is JunctionLayoutKw && !it.inFinalScript } as JunctionLayoutKw
                junctionLayoutKw.addButton.fire()
                junctionLayoutKw.setOutIds(outIds)
                junctionLayoutKw.setType(type)
                mediator.drawingDisplayed.get()?.let {
                    if (it.drawing.secondaryStructure.rna.useNumberingSystem)
                        junctionLayoutKw.setLocation(it.drawing.secondaryStructure.rna.mapLocation(junctionLocation))
                    else
                        junctionLayoutKw.setLocation(junctionLocation)
                }
            }
            allowScriptInit = true
            initScript()
        }
    }

    fun setJunctionRadius(radius: Double, type: String, junctionLocation: Location) {
        with(this.getScriptRoot().getLayoutKw()) {
            allowScriptInit = false
            addButton.fire()
            val junctionLayoutKw =
                searchFirst { it is JunctionLayoutKw && it.inFinalScript && junctionLocation.equals(it.getLocation()) } as JunctionLayoutKw?
            //We have found a junctionKw with the same location, we update it
            junctionLayoutKw?.let {
                it.setRadius(radius) //We just need to change the outIds (type and location should be the same)
            } ?: run { //we create a new one
                val junctionLayoutKw = searchFirst { it is JunctionLayoutKw && !it.inFinalScript } as JunctionLayoutKw
                junctionLayoutKw.addButton.fire()
                junctionLayoutKw.setRadius(radius)
                junctionLayoutKw.setType(type)
                mediator.drawingDisplayed.get()?.let {
                    if (it.drawing.secondaryStructure.rna.useNumberingSystem)
                        junctionLayoutKw.setLocation(it.drawing.secondaryStructure.rna.mapLocation(junctionLocation))
                    else
                        junctionLayoutKw.setLocation(junctionLocation)
                }
            }
            allowScriptInit = true
            initScript()
        }
    }

    fun setDetailsLevel(level: String) {
        with(this.getScriptRoot().getThemeKw()) {
            allowScriptInit = false
            addButton.fire()
            val selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelectedPositions().toIntArray())
            val toUpdates = mutableListOf<DSLElementInt>()
            searchAll(toUpdates) { it is DetailsKw && it.getLocation() == selection }
            //If we found at least one DetailsKw with the same location if any, we update it
            if (toUpdates.isNotEmpty()) {
                with(toUpdates.first()) {
                    (this as DetailsKw).setlevel(level)
                }

                if (toUpdates.size > 1) {
                    toUpdates.subList(1, toUpdates.size).forEach {
                        (it as DetailsKw).removeButton.fire()
                    }
                }

            } else { //nothing found we add a new DetailsKw element
                val detailsKw = searchFirst { it is DetailsKw && !it.inFinalScript } as DetailsKw
                detailsKw.setlevel(level)
                selection?.let { l ->
                    mediator.drawingDisplayed.get()?.let {
                        if (it.drawing.secondaryStructure.rna.useNumberingSystem)
                            detailsKw.setLocation(it.drawing.secondaryStructure.rna.mapLocation(l))
                        else
                            detailsKw.setLocation(l)
                    }
                }
            }
            allowScriptInit = true
            initScript()
        }
    }

    fun setColor(types: String, color: String) {
        with(getScriptRoot().getThemeKw()) {
            allowScriptInit = false
            addButton.fire()
            val selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelectedPositions().toIntArray())
            val toUpdates = mutableListOf<DSLElementInt>()
            searchAll(toUpdates) { it is ColorKw && types.equals(it.getTypes()) && it.getLocation() == selection }
            //If we found at least one colorKW with the same types (and location if any), we update it
            if (toUpdates.isNotEmpty()) {

                with(toUpdates.first()) {
                    (this as ColorKw).setColor(color)
                }

                if (toUpdates.size > 1) {
                    toUpdates.subList(1, toUpdates.size).forEach {
                        (it as ColorKw).removeButton.fire()
                    }
                }

            } else { //nothing found we add a new ColorKW element
                val colorKw = searchFirst { it is ColorKw && !it.inFinalScript } as ColorKw
                colorKw.setColor(color)
                colorKw.setTypes(types)
                selection?.let { l ->
                    mediator.drawingDisplayed.get()?.let {
                        if (it.drawing.secondaryStructure.rna.useNumberingSystem)
                            colorKw.setLocation(it.drawing.secondaryStructure.rna.mapLocation(l))
                        else
                            colorKw.setLocation(l)
                    }
                }
            }
            allowScriptInit = true
            initScript()
        }
    }

    fun setLineWidth(types: String, width: String) {
        with(getScriptRoot().getThemeKw()) {
            allowScriptInit = false
            addButton.fire()
            val selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelectedPositions().toIntArray())
            val toUpdates = mutableListOf<DSLElementInt>()
            searchAll(toUpdates) { it is LineKw && types.equals(it.getTypes()) && it.getLocation() == selection }
            //If we found at least one lineKW with the same types (and location if any), we update it
            if (toUpdates.isNotEmpty()) {

                with(toUpdates.first()) {
                    (this as LineKw).setWidth(width)
                }

                if (toUpdates.size > 1) {
                    toUpdates.subList(1, toUpdates.size).forEach {
                        (it as LineKw).removeButton.fire()
                    }
                }

            } else { //nothing found we add a new LineKW element
                val lineKw = searchFirst { it is LineKw && !it.inFinalScript } as LineKw
                lineKw.setWidth(width)
                lineKw.setTypes(types)
                selection?.let { l ->
                    mediator.drawingDisplayed.get()?.let {
                        if (it.drawing.secondaryStructure.rna.useNumberingSystem)
                            lineKw.setLocation(it.drawing.secondaryStructure.rna.mapLocation(l))
                        else
                            lineKw.setLocation(l)
                    }
                }
            }
            allowScriptInit = true
            initScript()
        }
    }
}

class ScriptEditor(val mediator: Mediator):BorderPane() {

    var currentScriptLocation: File? = null
    val script:RNArtistScript
    val engine: ScriptEngine

    init {
        script = RNArtistScript(mediator)
        val manager = ScriptEngineManager()
        this.engine = manager.getEngineByExtension("kts")
        script.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.backgroundEditorColor)}"
        script.padding = Insets(10.0, 10.0, 10.0, 10.0)
        script.lineSpacing = 10.0
        script.tabSize = 6
        script.layout()

        val leftToolbar = VBox()
        leftToolbar.alignment = Pos.TOP_CENTER
        leftToolbar.spacing = 5.0
        leftToolbar.padding = Insets(10.0, 5.0, 10.0, 5.0)

        val decreaseTab = Button(null, FontIcon("fas-outdent:15"))
        decreaseTab.onAction = EventHandler {
            if (script.tabSize > 1) {
                script.tabSize--
            }
        }

        val increaseTab = Button(null, FontIcon("fas-indent:15"))
        increaseTab.onAction = EventHandler {
            script.tabSize++
        }

        val decreaseLineSpacing = Button(null, FontIcon("fas-compress-alt:15"))
        decreaseLineSpacing.onAction = EventHandler {
            script.lineSpacing--
        }

        val increaseLineSpacing = Button(null, FontIcon("fas-expand-alt:15"))
        increaseLineSpacing.onAction = EventHandler {
            script.lineSpacing++
        }

        val expandAll = Button(null, FontIcon("fas-plus:15"))
        expandAll.onAction = EventHandler {
            script.allowScriptInit = false
            script.children.filterIsInstance<DSLKeyword.KeywordNode>().map {
                if (it.children.isNotEmpty())
                    (it.children.get(it.children.size - 2) as? Collapse)?.let {
                        if (it.collapsed)
                            it.fire()
                    }
            }
            script.allowScriptInit = true
            script.initScript()
        }

        val collapseAll = Button(null, FontIcon("fas-minus:15"))
        collapseAll.onAction = EventHandler {
            script.allowScriptInit = false
            script.children.filterIsInstance<DSLKeyword.KeywordNode>().map {
                if (it.children.isNotEmpty())
                    (it.children.get(it.children.size - 2) as? Collapse)?.let {
                        if (!it.collapsed)
                            it.fire()
                    }
            }
            script.allowScriptInit = true
            script.initScript()
        }

        val bgColor = ColorPicker()
        bgColor.value = awtColorToJavaFX(RnartistConfig.backgroundEditorColor)
        bgColor.styleClass.add("button")
        bgColor.style = "-fx-color-label-visible: false ;"
        bgColor.onAction = EventHandler {
            script.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(bgColor.value))}"
            RnartistConfig.backgroundEditorColor = javaFXToAwt(bgColor.value)
        }

        val kwColor = ColorPicker()
        kwColor.value = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        kwColor.styleClass.add("button")
        kwColor.style = "-fx-color-label-visible: false ;"
        kwColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            script.getScriptRoot()?.searchAll(hits) { it is DSLKeyword }
            hits.forEach {
                it.color = kwColor.value
                (it as DSLKeyword).collapseButton.setColor(kwColor.value)
                (it as? OptionalDSLKeyword)?.addButton?.setColor(kwColor.value)
            }
            RnartistConfig.keywordEditorColor = javaFXToAwt(kwColor.value)
            script.initScript()
        }

        val bracesColor = ColorPicker()
        bracesColor.value = awtColorToJavaFX(RnartistConfig.bracesEditorColor)
        bracesColor.styleClass.add("button")
        bracesColor.style = "-fx-color-label-visible: false ;"
        bracesColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            script.getScriptRoot()?.searchAll(hits) { it is OpenedCurly || it is ClosedCurly }
            hits.forEach {
                it.color = bracesColor.value
            }
            RnartistConfig.bracesEditorColor = javaFXToAwt(bracesColor.value)
            script.initScript()
        }

        val keyParamColor = ColorPicker()
        keyParamColor.value = awtColorToJavaFX(RnartistConfig.keyParamEditorColor)
        keyParamColor.styleClass.add("button")
        keyParamColor.style = "-fx-color-label-visible: false ;"
        keyParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).key.color = keyParamColor.value
                (it as? OptionalDSLParameter)?.addButton?.setColor(keyParamColor.value)
            }
            RnartistConfig.keyParamEditorColor = javaFXToAwt(keyParamColor.value)
            script.initScript()
        }

        val operatorParamColor = ColorPicker()
        operatorParamColor.value = awtColorToJavaFX(RnartistConfig.operatorParamEditorColor)
        operatorParamColor.styleClass.add("button")
        operatorParamColor.style = "-fx-color-label-visible: false ;"
        operatorParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).operator.color = operatorParamColor.value
            }
            RnartistConfig.operatorParamEditorColor = javaFXToAwt(operatorParamColor.value)
            script.initScript()
        }

        val valueParamColor = ColorPicker()
        valueParamColor.value = awtColorToJavaFX(RnartistConfig.valueParamEditorColor)
        valueParamColor.styleClass.add("button")
        valueParamColor.style = "-fx-color-label-visible: false ;"
        valueParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            script.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).value.color = valueParamColor.value
            }
            RnartistConfig.valueParamEditorColor = javaFXToAwt(valueParamColor.value)
            script.initScript()
        }

        val spacer = Region()
        spacer.prefHeight = 20.0
        leftToolbar.children.addAll(
            expandAll,
            collapseAll,
            increaseLineSpacing,
            decreaseLineSpacing,
            increaseTab,
            decreaseTab,
            spacer,
            Label("Bg"),
            bgColor,
            Label("Kw"),
            kwColor,
            Label("{ }"),
            bracesColor,
            Label("Key"),
            keyParamColor,
            Label("Op"),
            operatorParamColor,
            Label("Val"),
            valueParamColor
        )

        this.left = leftToolbar

        var scrollpane = ScrollPane(script)
        scrollpane.isFitToHeight = true
        script.minWidthProperty().bind(scrollpane.widthProperty())
        this.center = scrollpane
    }

    fun getEntireScriptAsText(): String {
        val scriptContent = StringBuilder()
        script.getScriptRoot().dumpText(scriptContent)
        return "import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent}"
    }

    fun getLayoutAsText(): String {
        val scriptContent = StringBuilder()
        val layoutKw = script.getScriptRoot().getLayoutKw()
        layoutKw.dumpText(scriptContent)
        return "import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent}"
    }

    fun getThemeAsText(): String {
        val scriptContent = StringBuilder()
        val themeKw = script.getScriptRoot().getThemeKw()
        themeKw.dumpText(scriptContent)
        return "import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent}"
    }

    fun getInputFileFields():List<InputFileKw> {
        val hits = mutableListOf<DSLElementInt>()
        script.getScriptRoot().searchAll(hits, {it is InputFileKw})
        return hits.map { it  as InputFileKw }
    }

}
