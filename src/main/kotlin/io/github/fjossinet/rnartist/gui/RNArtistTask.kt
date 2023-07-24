package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.io.ScriptElement
import io.github.fjossinet.rnartist.core.io.copyFile
import io.github.fjossinet.rnartist.core.io.parseDSLScript
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.model.editor.*
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import java.awt.Desktop
import java.awt.geom.Rectangle2D
import java.io.*
import java.lang.StringBuilder
import java.net.URL
import java.nio.file.Paths

abstract class RNArtistTask(val mediator: Mediator) : Task<Pair<Any?, Exception?>>() {
    var rnartistTaskWindow: RNArtistTaskWindow? = null
}

class ApplyExportInScript(mediator: Mediator) : RNArtistTask(mediator) {

    init {
        setOnSucceeded { event ->
            this.rnartistTaskWindow?.stage?.hide()
            val result = get()
            result.first?.let { scriptResult ->
            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            }
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                updateMessage("Export data..")
            }
            Thread.sleep(100)
            val svgKw = mediator.scriptEditor.script.getScriptRoot().getSVGKw()
            if (svgKw.inFinalScript) {
                svgKw.path?.let { path ->
                    mediator.databaseExplorer.drawingsLoaded().forEach {
                        svgKw.name?.let { chainName ->
                            if (chainName.equals(it.drawing.secondaryStructure.rna.name)) {
                                Platform.runLater {
                                    it.drawing.asSVG(
                                        Rectangle2D.Double(0.0, 0.0, svgKw.width, svgKw.height),
                                        null,
                                        Paths.get(path).resolve("${it.drawing.name}.svg").toFile()
                                    )
                                }
                                Thread.sleep(100)
                            }
                        } ?: run {
                            Platform.runLater {
                                it.drawing.asSVG(
                                    Rectangle2D.Double(0.0, 0.0, svgKw.width, svgKw.height),
                                    null,
                                    Paths.get(path).resolve("${it.drawing.name}.svg").toFile()
                                )
                            }
                            Thread.sleep(100)
                        }
                    }
                }
            }
            val pngKw = mediator.scriptEditor.script.getScriptRoot().getPNGKw()
            if (pngKw.inFinalScript) {
                pngKw.path?.let { path ->
                    mediator.databaseExplorer.drawingsLoaded().forEach {
                        pngKw.name?.let { chainName ->
                            if (chainName.equals(it.drawing.secondaryStructure.rna.name)) {
                                Platform.runLater {
                                    it.drawing.asPNG(
                                        Rectangle2D.Double(0.0, 0.0, svgKw.width, svgKw.height),
                                        null,
                                        Paths.get(path).resolve("${it.drawing.name}.png").toFile()
                                    )
                                }
                                Thread.sleep(100)
                            }
                        } ?: run {
                            Platform.runLater {
                                it.drawing.asPNG(
                                    Rectangle2D.Double(0.0, 0.0, svgKw.width, svgKw.height),
                                    null,
                                    Paths.get(path).resolve("${it.drawing.name}.png").toFile()
                                )
                            }
                            Thread.sleep(100)
                        }
                    }
                }
            }
            val chimeraKw = mediator.scriptEditor.script.getScriptRoot().getChimeraKw()
            if (chimeraKw.inFinalScript) {
                chimeraKw.path?.let { path ->
                    mediator.databaseExplorer.drawingsLoaded().forEach {
                        chimeraKw.name?.let { chainName ->
                            if (chainName.equals(it.drawing.secondaryStructure.rna.name)) {
                                Platform.runLater {
                                    val outputFile = Paths.get(path).resolve("${it.drawing.name}.cxc").toFile()
                                    it.drawing.asChimeraScript(
                                        outputFile
                                    )
                                    mediator.chimeraDriver.loadChimeraScript(outputFile)
                                }
                                Thread.sleep(100)
                            }
                        } ?: run {
                            Platform.runLater {
                                val outputFile = Paths.get(path).resolve("${it.drawing.name}.cxc").toFile()
                                it.drawing.asChimeraScript(
                                    outputFile
                                )
                                mediator.chimeraDriver.loadChimeraScript(outputFile)
                            }
                            Thread.sleep(100)
                        }
                    }
                }
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class ApplyThemeAndLayout(mediator: Mediator) : RNArtistTask(mediator) {

    init {
        setOnSucceeded { event ->
            this.rnartistTaskWindow?.stage?.hide()
            val result = get()
            result.first?.let { scriptResult ->
            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            }
            //we repaint the current 2D (if the layout and theme have been defined directly from the script, we the current 2D needs to be updated to fit its thumbnail
            mediator.canvas2D.repaint()
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                updateMessage("Applying theme & layout..")
            }
            Thread.sleep(100)
            val theme = mediator.scriptEditor.engine.eval(mediator.scriptEditor.getThemeAsText(useAbsolutePath= true)) as? Theme
            val layout = mediator.scriptEditor.engine.eval(mediator.scriptEditor.getLayoutAsText(useAbsolutePath= true)) as? Layout
            //println(scriptContent)
            Thread.sleep(100)
            var totalProgress = mediator.databaseExplorer.count().toDouble()
            var progressStep = 0.0
            var loaded = 0
            mediator.databaseExplorer.drawingsLoaded().forEach { drawingLoaded ->
                Platform.runLater {
                    updateProgress(++progressStep, totalProgress)
                    updateMessage("Applying theme & layout to 2D ${++loaded}/${mediator.databaseExplorer.count()}")
                    drawingLoaded.drawing.clearTheme()
                    theme?.let {
                        drawingLoaded.drawing.applyTheme(it)
                    }
                    //TODO clear layout
                    layout?.let {
                        drawingLoaded.drawing.applyLayout(it)
                    }
                    drawingLoaded.layoutAndThemeUpdated.value = !drawingLoaded.layoutAndThemeUpdated.value
                }
                Thread.sleep(100)
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class RunEntireScript(mediator: Mediator) : RNArtistTask(mediator) {

    init {
        setOnSucceeded { event ->
            this.rnartistTaskWindow?.stage?.hide()
            val result = get()
            result.first?.let { scriptResult ->

            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            }
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                mediator.databaseExplorer.drawingsLoaded().clear()
            }
            Thread.sleep(100)
            Platform.runLater {
                updateMessage("Running script..")
            }
            Thread.sleep(100)
            val scriptContent = mediator.scriptEditor.getEntireScriptAsText(useAbsolutePath= true)
            //println(scriptContent)
            val result = mediator.scriptEditor.engine.eval(scriptContent)
            Platform.runLater {
                updateMessage("Loading new 2Ds...")
            }
            Thread.sleep(100)
            val structuresToBeLoaded = (result as? List<SecondaryStructureDrawing>)?.sortedBy { it.secondaryStructure.name }?.reversed()
            val totalProgress = structuresToBeLoaded!!.size.toDouble()
            var progressStep = 0.0
            var loaded = 0
            structuresToBeLoaded?.forEach {
                Platform.runLater {
                    updateProgress(++progressStep, totalProgress)
                    updateMessage("Loading 2D ${++loaded}/${structuresToBeLoaded.size}")
                    mediator.databaseExplorer.addItem(
                        DrawingLoaded(
                            mediator,
                            it, mediator.scriptEditor.script.getScriptRoot().id
                        )
                    )
                }
                Thread.sleep(100)
            }
            return Pair(result, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class LoadScript(mediator: Mediator, val script: Reader, val runScript:Boolean = false) : RNArtistTask(mediator) {

    init {
        mediator.drawingDisplayed.set(null)
        mediator.canvas2D.repaint()
        mediator.scriptEditor.script.setScriptRoot(RNArtistKw(mediator.scriptEditor.script))
        //to avoid doing initScript() for each addbutton fired
        mediator.scriptEditor.script.allowScriptInit = false
        setOnSucceeded { event ->
            val result = get()
            this.rnartistTaskWindow?.stage?.hide()
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            } ?: run {
                if (runScript)
                    RNArtistTaskWindow(mediator).task = RunEntireScript(mediator)
            }
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                mediator.databaseExplorer.drawingsLoaded().clear()
            }
            Thread.sleep(100)
            Platform.runLater {
                updateMessage("Loading script..")
            }
            Thread.sleep(100)
            val scriptContent = script.readText()
            val result = parseDSLScript(StringReader(scriptContent))
            Platform.runLater {
                updateMessage("Found ${result.second.size} issues in the script...")
            }
            Thread.sleep(100)
            var (elements, issues) = result

            var progressStep = 0.0
            Platform.runLater {
                updateMessage("Loading script in script editor..")
            }
            Thread.sleep(100)

            val allElements = mutableListOf<ScriptElement>()
            elements.first().getAllElements(allElements)
            var totalProgress = allElements.size.toDouble() + 1
            elements.first().children.forEach { element ->
                when (element.name) {
                    "ss" -> {
                        progressStep = (allElements.indexOf(element) + 1).toDouble()
                        Platform.runLater {
                            updateProgress(++progressStep, totalProgress)
                            updateMessage("Importing script element ${element.name}")
                        }
                        Thread.sleep(5)
                        val secondaryStructureInputKw =
                            mediator.scriptEditor.script.getScriptRoot()
                                .searchFirst { it is SecondaryStructureInputKw } as SecondaryStructureInputKw
                        element.children.forEach { elementChild ->
                            when (elementChild.name) {
                                "parts" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val partsKw =
                                        secondaryStructureInputKw.searchFirst { child -> child is PartsKw && !child.inFinalScript } as PartsKw
                                    partsKw.addButton.fire()
                                    elementChild.children.forEach { elementChildChild ->
                                        when (elementChildChild.name) {
                                            "rna" -> {
                                                progressStep = (allElements.indexOf(elementChildChild) + 1).toDouble()
                                                Platform.runLater {
                                                    updateProgress(++progressStep, totalProgress)
                                                    updateMessage("Importing script element ${elementChildChild.name}")
                                                }
                                                Thread.sleep(5)
                                                if (!partsKw.inFinalScript)
                                                    partsKw.addButton.fire()
                                                val rnaKw =
                                                    (partsKw.searchFirst { child -> child is RnaKw } as RnaKw)
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("=")
                                                    if ("seq".equals(tokens.first().trim())) {
                                                        val parameter =
                                                            rnaKw.searchFirst {
                                                                it is OptionalDSLParameter && "seq".equals(
                                                                    it.key.text.text
                                                                )
                                                            } as OptionalDSLParameter
                                                        parameter.addButton.fire()
                                                        parameter.value.text.text = tokens.last().trim()
                                                    }
                                                    if ("length".equals(tokens.first().trim())) {
                                                        val parameter =
                                                            rnaKw.searchFirst {
                                                                it is OptionalDSLParameter && "length".equals(
                                                                    it.key.text.text
                                                                )
                                                            } as OptionalDSLParameter
                                                        parameter.addButton.fire()
                                                        parameter.value.text.text = tokens.last().trim()
                                                    }
                                                    if ("name".equals(tokens.first().trim())) {
                                                        val parameter =
                                                            (rnaKw.searchFirst {
                                                                it is OptionalDSLParameter && "name".equals(
                                                                    it.key.text.text
                                                                )
                                                            } as OptionalDSLParameter)
                                                        parameter.addButton.fire()
                                                        parameter.value.text.text = tokens.last().trim()
                                                    }
                                                }
                                            }
                                            "helix" -> {
                                                if (!partsKw.inFinalScript)
                                                    partsKw.addButton.fire()
                                                val helixKw =
                                                    partsKw.searchFirst { child -> child is HelixKw && !child.inFinalScript } as HelixKw
                                                helixKw.collapseButton.collapsed = true
                                                helixKw.addButton.fire()
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("=")
                                                    if ("name".equals(tokens.first().trim())) {
                                                        progressStep =
                                                            (allElements.indexOf(elementChildChild) + 1).toDouble()
                                                        Platform.runLater {
                                                            updateProgress(++progressStep, totalProgress)
                                                            updateMessage(
                                                                "Importing script element ${elementChildChild.name} ${
                                                                    tokens.last().trim()
                                                                }"
                                                            )
                                                        }
                                                        Thread.sleep(5)

                                                        val parameter =
                                                            helixKw.searchFirst {
                                                                it is OptionalDSLParameter && "name".equals(
                                                                    it.key.text.text
                                                                )
                                                            } as OptionalDSLParameter
                                                        parameter.addButton.fire()
                                                        parameter.value.text.text = tokens.last().trim()
                                                    }
                                                }
                                                elementChildChild.children.forEach { elementChildChildChild ->
                                                    when (elementChildChildChild.name) {
                                                        "location" -> {
                                                            val blocks = mutableListOf<Block>()
                                                            elementChildChildChild.attributes.forEach { attribute ->
                                                                val tokens = attribute.split("to")
                                                                blocks.add(
                                                                    Block(
                                                                        tokens.first().trim().toInt(),
                                                                        tokens.last().trim().toInt()
                                                                    )
                                                                )
                                                            }
                                                            helixKw.setLocation(Location(blocks))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("source".equals(tokens.first().trim())) {
                                            val tokens = attribute.split("=")
                                            val p = partsKw.searchFirst {
                                                it is SourceParameter
                                            } as SourceParameter
                                            p.value.text.text = tokens.last().trim()
                                            p.addButton.fire()
                                        }
                                    }
                                }

                                "bn" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val bnKw =
                                        secondaryStructureInputKw.searchFirst { child -> child is BracketNotationKw && !child.inFinalScript } as BracketNotationKw
                                    bnKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("value".equals(tokens.first().trim())) {
                                            val parameter =
                                                (bnKw.searchFirst { it is DSLParameter && "value".equals(it.key.text.text) } as DSLParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("seq".equals(tokens.first().trim())) {
                                            val parameter =
                                                (bnKw.searchFirst { it is SequenceBnParameter } as SequenceBnParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                            parameter.addButton.fire()
                                        }
                                    }
                                }

                                "vienna" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val viennaKw =
                                        (secondaryStructureInputKw.searchFirst { themeChild -> themeChild is ViennaKw && !themeChild.inFinalScript } as ViennaKw)
                                    viennaKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("file".equals(tokens.first().trim())) {
                                            val parameter =
                                                (viennaKw.searchFirst { it is DSLParameter && "file".equals(it.key.text.text) } as DSLParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }

                                "bpseq" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val bpseqKw =
                                        (secondaryStructureInputKw.searchFirst { themeChild -> themeChild is BpseqKw && !themeChild.inFinalScript } as BpseqKw)
                                    bpseqKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("file".equals(tokens.first().trim())) {
                                            val parameter =
                                                (bpseqKw.searchFirst { it is DSLParameter && "file".equals(it.key.text.text) } as DSLParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }

                                "ct" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val ctKw =
                                        (secondaryStructureInputKw.searchFirst { themeChild -> themeChild is CtKw && !themeChild.inFinalScript } as CtKw)
                                    ctKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("file".equals(tokens.first().trim())) {
                                            val parameter =
                                                (ctKw.searchFirst { it is DSLParameter && "file".equals(it.key.text.text) } as DSLParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }

                                "stockholm" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val stockholmKw =
                                        (secondaryStructureInputKw.searchFirst { themeChild -> themeChild is StockholmKw && !themeChild.inFinalScript } as StockholmKw)
                                    stockholmKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        if (attribute.startsWith("use alignment numbering")) {
                                            val parameter =
                                                stockholmKw.searchFirst {
                                                    it is OptionalDSLParameter && "use".equals(
                                                        it.key.text.text
                                                    ) && "alignment".equals(it.operator.text.text.trim()) && "numbering".equals(
                                                        it.value.text.text
                                                    )
                                                } as OptionalDSLParameter
                                            parameter.addButton.fire()
                                        }
                                        val tokens = attribute.split("=")
                                        if ("file".equals(tokens.first().trim())) {
                                            val parameter =
                                                (stockholmKw.searchFirst {
                                                    it is DSLParameter && "file".equals(
                                                        it.key.text.text
                                                    )
                                                } as DSLParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }

                                "rfam" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val rfamKw =
                                        (secondaryStructureInputKw.searchFirst { themeChild -> themeChild is RfamKw && !themeChild.inFinalScript } as RfamKw)
                                    rfamKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        if (attribute.startsWith("use alignment numbering")) {
                                            val parameter =
                                                rfamKw.searchFirst {
                                                    it is OptionalDSLParameter && "use".equals(
                                                        it.key.text.text
                                                    ) && "alignment".equals(it.operator.text.text.trim()) && "numbering".equals(
                                                        it.value.text.text
                                                    )
                                                } as OptionalDSLParameter
                                            parameter.addButton.fire()
                                        }
                                        val tokens = attribute.split("=")
                                        if ("id".equals(tokens.first().trim())) {
                                            val parameter =
                                                rfamKw.searchFirst { it is DSLParameter && "id".equals(it.key.text.text) } as DSLParameter
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("name".equals(tokens.first().trim())) {
                                            val parameter =
                                                rfamKw.searchFirst {
                                                    it is OptionalDSLParameter && "name".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }

                                "rnacentral" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val rnaCentralKw =
                                        (secondaryStructureInputKw.searchFirst { themeChild -> themeChild is RNACentralKw && !themeChild.inFinalScript } as RNACentralKw)
                                    rnaCentralKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("id".equals(tokens.first().trim())) {
                                            val parameter =
                                                rnaCentralKw.searchFirst { it is DSLParameter && "id".equals(it.key.text.text) } as DSLParameter
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }

                                "pdb" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    if (!secondaryStructureInputKw.inFinalScript)
                                        secondaryStructureInputKw.addButton.fire()
                                    val pdbKW =
                                        (secondaryStructureInputKw.searchFirst { themeChild -> themeChild is PDBKw && !themeChild.inFinalScript } as PDBKw)
                                    pdbKW.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("file".equals(tokens.first().trim())) {
                                            val parameter =
                                                pdbKW.searchFirst {
                                                    it is OptionalDSLParameter && "file".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("id".equals(tokens.first().trim())) {
                                            val parameter =
                                                pdbKW.searchFirst { it is OptionalDSLParameter && "id".equals(it.key.text.text) } as OptionalDSLParameter
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("name".equals(tokens.first().trim())) {
                                            val parameter =
                                                pdbKW.searchFirst {
                                                    it is OptionalDSLParameter && "name".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "theme" -> {
                        progressStep = (allElements.indexOf(element) + 1).toDouble()
                        Platform.runLater {
                            updateProgress(++progressStep, totalProgress)
                            updateMessage("Importing script element ${element.name}")
                        }
                        Thread.sleep(5)
                        val themeKw = mediator.scriptEditor.script.getScriptRoot()
                            .searchFirst { it is ThemeKw } as ThemeKw
                        themeKw.addButton.fire()
                        element.children.forEach { elementChild ->
                            when (elementChild.name) {
                                "details" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    val detailsLevelKw =
                                        themeKw.searchFirst { themeChild -> themeChild is DetailsKw && !themeChild.inFinalScript } as DetailsKw
                                    detailsLevelKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        var tokens = attribute.split("=")
                                        if ("value".equals(tokens.first().trim())) {
                                            val parameter =
                                                (detailsLevelKw.searchFirst {
                                                    it is DSLParameter && "value".equals(
                                                        it.key.text.text
                                                    )
                                                } as DSLParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("type".equals(tokens.first().trim())) {
                                            val parameter =
                                                (detailsLevelKw.searchFirst {
                                                    it is OptionalDSLParameter && "type".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                    elementChild.children.forEach { elementChildChild ->
                                        when (elementChildChild.name) {
                                            "location" -> {
                                                val blocks = mutableListOf<Block>()
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("to")
                                                    blocks.add(
                                                        Block(
                                                            tokens.first().trim().toInt(),
                                                            tokens.last().trim().toInt()
                                                        )
                                                    )
                                                }
                                                detailsLevelKw.setLocation(Location(blocks))
                                            }
                                        }
                                    }
                                }
                                "color" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    val colorKw =
                                        themeKw.searchFirst { themeChild -> themeChild is ColorKw && !themeChild.inFinalScript } as ColorKw
                                    colorKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        var tokens = attribute.split("=")
                                        if ("value".equals(tokens.first().trim())) {
                                            val parameter =
                                                (colorKw.searchFirst { it is OptionalDSLParameter && "value".equals(it.key.text.text) } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                            parameter.value.text.fill =
                                                Color.web(tokens.last().trim().replace("\"", ""))
                                        }
                                        if ("scheme".equals(tokens.first().trim())) {
                                            val parameter =
                                                (colorKw.searchFirst { it is OptionalDSLParameter && "scheme".equals(it.key.text.text) } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("type".equals(tokens.first().trim())) {
                                            val parameter =
                                                (colorKw.searchFirst {
                                                    it is OptionalDSLParameter && "type".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("to".equals(tokens.first().trim())) {
                                            val parameter =
                                                (colorKw.searchFirst {
                                                    it is OptionalDSLParameter && "to".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                            parameter.value.text.fill =
                                                Color.web(tokens.last().trim().replace("\"", ""))
                                        }
                                        if (attribute.trim().startsWith("data")) {
                                            tokens = attribute.trim().split(" ")
                                            val parameter =
                                                (colorKw.searchFirst {
                                                    it is OptionalDSLParameter && "data".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.operator.text.text = " ${tokens[1].trim()} "
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                    elementChild.children.forEach { elementChildChild ->
                                        when (elementChildChild.name) {
                                            "location" -> {
                                                val blocks = mutableListOf<Block>()
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("to")
                                                    blocks.add(
                                                        Block(
                                                            tokens.first().trim().toInt(),
                                                            tokens.last().trim().toInt()
                                                        )
                                                    )
                                                }
                                                colorKw.setLocation(Location(blocks))
                                            }
                                        }
                                    }
                                }
                                "line" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    val lineKw =
                                        themeKw.searchFirst { themeChild -> themeChild is LineKw && !themeChild.inFinalScript } as LineKw
                                    lineKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        var tokens = attribute.split("=")
                                        if ("value".equals(tokens.first().trim())) {
                                            val parameter =
                                                (lineKw.searchFirst { it is DSLParameter && "value".equals(it.key.text.text) } as DSLParameter)
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("type".equals(tokens.first().trim())) {
                                            val parameter =
                                                (lineKw.searchFirst {
                                                    it is OptionalDSLParameter && "type".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if (attribute.startsWith("data")) {
                                            tokens = attribute.split(" ")
                                            val parameter =
                                                (lineKw.searchFirst {
                                                    it is OptionalDSLParameter && "data".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.operator.text.text = " ${tokens[1].trim()} "
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                    elementChild.children.forEach { elementChildChild ->
                                        when (elementChildChild.name) {
                                            "location" -> {
                                                val blocks = mutableListOf<Block>()
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("to")
                                                    blocks.add(
                                                        Block(
                                                            tokens.first().trim().toInt(),
                                                            tokens.last().trim().toInt()
                                                        )
                                                    )
                                                }
                                                lineKw.setLocation(Location(blocks))
                                            }
                                        }
                                    }
                                }
                                "show" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    val showKw =
                                        themeKw.searchFirst { themeChild -> themeChild is ShowKw && !themeChild.inFinalScript } as ShowKw
                                    showKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        var tokens = attribute.split("=")
                                        if ("type".equals(tokens.first().trim())) {
                                            val parameter =
                                                (showKw.searchFirst {
                                                    it is OptionalDSLParameter && "type".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                            if ("\"tertiary_interaction\"".equals(tokens.last().trim()))
                                                mediator.actions2DButtonsPanel.showTertiaries.value = true
                                        }
                                        if (attribute.startsWith("data")) {
                                            tokens = attribute.split(" ")
                                            val parameter =
                                                (showKw.searchFirst {
                                                    it is OptionalDSLParameter && "data".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.operator.text.text = " ${tokens[1].trim()} "
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                    elementChild.children.forEach { elementChildChild ->
                                        when (elementChildChild.name) {
                                            "location" -> {
                                                val blocks = mutableListOf<Block>()
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("to")
                                                    blocks.add(
                                                        Block(
                                                            tokens.first().trim().toInt(),
                                                            tokens.last().trim().toInt()
                                                        )
                                                    )
                                                }
                                                showKw.setLocation(Location(blocks))
                                            }
                                        }
                                    }
                                }
                                "hide" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name}")
                                    }
                                    Thread.sleep(5)
                                    val hideKw =
                                        themeKw.searchFirst { themeChild -> themeChild is HideKw && !themeChild.inFinalScript } as HideKw
                                    hideKw.addButton.fire()
                                    elementChild.attributes.forEach { attribute ->
                                        var tokens = attribute.split("=")
                                        if ("type".equals(tokens.first().trim())) {
                                            val parameter =
                                                (hideKw.searchFirst {
                                                    it is OptionalDSLParameter && "type".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                            if ("\"tertiary_interaction\"".equals(tokens.last().trim()))
                                                mediator.actions2DButtonsPanel.showTertiaries.value = false
                                        }
                                        if (attribute.startsWith("data")) {
                                            tokens = attribute.split(" ")
                                            val parameter =
                                                (hideKw.searchFirst {
                                                    it is OptionalDSLParameter && "data".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.key.text.text = tokens.first().trim()
                                            parameter.operator.text.text = " ${tokens[1].trim()} "
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                    elementChild.children.forEach { elementChildChild ->
                                        when (elementChildChild.name) {
                                            "location" -> {
                                                val blocks = mutableListOf<Block>()
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("to")
                                                    blocks.add(
                                                        Block(
                                                            tokens.first().trim().toInt(),
                                                            tokens.last().trim().toInt()
                                                        )
                                                    )
                                                }
                                                hideKw.setLocation(Location(blocks))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "layout" -> {
                        progressStep = (allElements.indexOf(element) + 1).toDouble()
                        Platform.runLater {
                            updateProgress(++progressStep, totalProgress)
                            updateMessage("Importing script element ${element.name}")
                        }
                        Thread.sleep(5)
                        val layoutKw = mediator.scriptEditor.script.getScriptRoot()
                            .searchFirst { it is LayoutKw } as LayoutKw
                        layoutKw.addButton.fire()
                        element.children.forEach { elementChild ->
                            when (elementChild.name) {
                                "junction" -> {
                                    progressStep = (allElements.indexOf(elementChild) + 1).toDouble()
                                    Platform.runLater {
                                        updateProgress(++progressStep, totalProgress)
                                        updateMessage("Importing script element ${elementChild.name} ")
                                    }
                                    Thread.sleep(5)
                                    val junctionLayoutKw =
                                        layoutKw.searchFirst { layoutChild -> layoutChild is JunctionLayoutKw && !layoutChild.inFinalScript } as JunctionLayoutKw
                                    elementChild.attributes.forEach { attribute ->
                                        val tokens = attribute.split("=")
                                        if ("out_ids".equals(tokens.first().trim())) {
                                            val parameter =
                                                (junctionLayoutKw.searchFirst {
                                                    it is OptionalDSLParameter && "out_ids".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("radius".equals(tokens.first().trim())) {
                                            val parameter =
                                                (junctionLayoutKw.searchFirst {
                                                    it is OptionalDSLParameter && "radius".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                        if ("type".equals(tokens.first().trim())) {
                                            val parameter =
                                                (junctionLayoutKw.searchFirst {
                                                    it is OptionalDSLParameter && "type".equals(
                                                        it.key.text.text
                                                    )
                                                } as OptionalDSLParameter)
                                            parameter.addButton.fire()
                                            parameter.value.text.text = tokens.last().trim()
                                        }
                                    }
                                    elementChild.children.forEach { elementChildChild ->
                                        when (elementChildChild.name) {
                                            "location" -> {
                                                val blocks = mutableListOf<Block>()
                                                elementChildChild.attributes.forEach { attribute ->
                                                    val tokens = attribute.split("to")
                                                    blocks.add(
                                                        Block(
                                                            tokens.first().trim().toInt(),
                                                            tokens.last().trim().toInt()
                                                        )
                                                    )
                                                }
                                                junctionLayoutKw.setLocation(Location(blocks))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "data" -> {
                        progressStep = (allElements.indexOf(element) + 1).toDouble()
                        Platform.runLater {
                            updateProgress(++progressStep, totalProgress)
                            updateMessage("Importing script element ${element.name} ")
                        }
                        Thread.sleep(5)
                        val dataKw = mediator.scriptEditor.script.getScriptRoot()
                            .searchFirst { it is DataKw } as DataKw
                        dataKw.addButton.fire()
                        element.attributes.forEach { attribute ->
                            val tokens = attribute.split(" ")
                            val parameter =
                                (dataKw.searchFirst { it is OptionalDSLParameter && !it.inFinalScript } as OptionalDSLParameter)
                            parameter.addButton.fire()
                            parameter.key.text.text = tokens.first().trim()
                            parameter.operator.text.text = " ${tokens[1].trim()} "
                            parameter.value.text.text = tokens.last().trim()
                        }
                    }
                }
            }
            Platform.runLater {
                updateMessage("Finalizing script model..")
            }
            Thread.sleep(100)
            Platform.runLater {
                mediator.scriptEditor.script.allowScriptInit = true
                mediator.scriptEditor.script.initScript()
                if (issues.isNotEmpty()) {
                    val alert = Alert(Alert.AlertType.WARNING)
                    alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                    alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                    alert.headerText = "I fixed issues in your script."
                    alert.contentText = issues.joinToString(separator = "\n")
                    alert.buttonTypes.clear()
                    alert.buttonTypes.add(ButtonType.OK)
                    alert.buttonTypes.add(ButtonType("Go to Documentation", ButtonBar.ButtonData.HELP))
                    var result = alert.showAndWait()
                    if (result.isPresent && result.get() != ButtonType.OK) { //show documentation
                        Desktop.getDesktop()
                            .browse(URL("https://github.com/fjossinet/RNArtistCore/blob/master/Changelog.md").toURI())
                    }
                }
                updateProgress(++progressStep, totalProgress)
            }
            Thread.sleep(5)
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class LoadGist(mediator: Mediator, val gistID:String) : RNArtistTask(mediator) {

    init {
        mediator.drawingDisplayed.set(null)
        mediator.canvas2D.repaint()
        mediator.scriptEditor.script.setScriptRoot(RNArtistKw(mediator.scriptEditor.script))
        //to avoid doing initScript() for each addbutton fired
        mediator.scriptEditor.script.allowScriptInit = false
        setOnSucceeded { event ->
            val result = get()
            this.rnartistTaskWindow?.stage?.hide()
            result.first?.let { scriptContent ->
                RNArtistTaskWindow(mediator).task = LoadScript(mediator, StringReader(scriptContent as String), true)
            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            }
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                updateMessage("Downloading Gist data..")
            }
            Thread.sleep(100)
            val gistContent = URL("https://api.github.com/gists/${gistID}").readText()
            val regex = Regex("\"content\":\"(import io.+?)\"},\"rnartist\\.svg\":\\{")
            val match = regex.find(gistContent)
            val scriptContent = match?.groupValues?.get(1)?.
            replace("\\n",System.lineSeparator())?.
            replace("\\t", " ")?.
            replace("\\\"","\"")

            return Pair(scriptContent, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class SaveProject(mediator: Mediator, val projectDir: File) : RNArtistTask(mediator) {

    init {
        setOnSucceeded { event ->
            this.rnartistTaskWindow?.stage?.hide()
            val result = get()
            result.first?.let {
            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            }
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                updateMessage("Saving project..")
            }
            Thread.sleep(100)
            projectDir.mkdir()

            Platform.runLater {
                updateMessage("Copying input files (if any) and updating their path..")
            }
            Thread.sleep(100)
            mediator.scriptEditor.getInputFileFields().forEach { inputFileField ->
                val inputPath = StringBuilder()
                inputFileField.getFileField().dumpText(inputPath, useAbsolutePath = true)
                if (inputPath.isNotEmpty()) {
                    val inputFile = File(inputPath.toString().replace("\"",""))
                    val outputFile = File(projectDir, inputFile.name)
                    if (!outputFile.exists()) { //if we copy a file over itself, it becomes empty
                        copyFile(inputFile, outputFile)
                        inputFileField.getFileField().text.text = "\"${outputFile.name}\""
                    }
                }
            }

            with(mediator.scriptEditor.script.getScriptRoot().getSecondaryStructureKw().getPartsKw()) {
                if (inFinalScript) {
                    val source = searchFirst { it is SourceParameter} as SourceParameter
                    val sourceValue = source.value.text.text.replace("\"","")
                    if (sourceValue.startsWith("local:file:")) {
                        val inputFile = File(sourceValue.split("local:file:").last())
                        val outputFile = File(projectDir, inputFile.name)
                        if (!outputFile.exists()) { //if we copy a file over itself, it becomes empty
                            copyFile(inputFile, outputFile)
                            source.value.text.text = "\"local:file:${outputFile.name}\""
                        }
                    }
                }
            }

            var scriptFile = File(projectDir, "rnartist.kts")
            scriptFile.createNewFile()
            Platform.runLater {
                updateMessage("Saving script..")
                var writer = PrintWriter(scriptFile)
                writer.println(mediator.scriptEditor.getEntireScriptAsText())
                writer.close()
                mediator.scriptEditor.currentScriptLocation = projectDir
            }
            Thread.sleep(100)
            Platform.runLater {
                updateMessage("Saving preview..")
            }
            Thread.sleep(100)
            //and we create a preview as a png file...
            mediator.drawingDisplayed.get()?.let {
                if (it.id.equals( mediator.scriptEditor.script.getScriptRoot().id)) {
                    it.drawing.asPNG(Rectangle2D.Double(0.0,0.0,200.0,200.0), null, File(projectDir, "preview.png"))
                }
            }
            Platform.runLater {
                mediator.projectsPanel.getProject(projectDir)?.let {
                    it.projectUpdated.value = true
                    mediator.drawingDisplayed.get()?.layoutAndThemeUpdated?.value = true
                } ?: run {
                    mediator.projectsPanel.addProject(projectDir)
                    mediator.drawingDisplayed.get()?.layoutAndThemeUpdated?.value = true
                }
            }
            Thread.sleep(100)
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class DeleteProject(mediator: Mediator, val projectDir: File) : RNArtistTask(mediator) {

    init {
        setOnSucceeded { event ->
            this.rnartistTaskWindow?.stage?.hide()
            val result = get()
            result.first?.let {
            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            }
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                updateMessage("Deleting project..")
                projectDir.deleteRecursively()
                mediator.projectsPanel.removeProjectPanel(projectDir)
            }
            Thread.sleep(100)
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}