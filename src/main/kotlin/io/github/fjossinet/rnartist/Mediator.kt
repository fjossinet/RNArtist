package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.theme
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.*
import io.github.fjossinet.rnartist.model.RNArtistDrawing
import io.github.fjossinet.rnartist.model.RNArtistTask
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Worker
import javafx.scene.web.WebView
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.EventTarget
import java.awt.Color
import java.io.File
import javax.script.ScriptEngineManager
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString

class Mediator(val rnartist: RNArtist) {

    var currentDrawing: SimpleObjectProperty<RNArtistDrawing?> = SimpleObjectProperty<RNArtistDrawing?>(null)

    //var chimeraDriver = ChimeraXDriver(this)
    val scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
    val webView = WebView()
    val drawingHighlighted = SimpleObjectProperty<DrawingElement?>()
    var helpMode = SimpleBooleanProperty(false)
    var helpModeOn: Boolean = this.helpMode.get()
        get() = this.helpMode.get()

    lateinit var canvas2D: Canvas2D

    val currentDB: RNArtistDB?
        get() = rnartist.currentDB.get()

    //++++++ some shortcuts
    private val secondaryStructure: SecondaryStructure?
        get() {
            return this.currentDrawing.get()?.secondaryStructureDrawing?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val workingSession: WorkingSession?
        get() {
            return this.currentDrawing.get()?.secondaryStructureDrawing?.workingSession
        }

    val viewX: Double?
        get() {
            return this.workingSession?.viewX
        }

    val viewY: Double?
        get() {
            return this.workingSession?.viewY
        }

    val zoomLevel: Double?
        get() {
            return this.workingSession?.zoomLevel
        }

    init {
        this.currentDrawing.addListener { _, _, _ ->
            canvas2D.repaint()
        }
        this.webView.getEngine().getLoadWorker().stateProperty().addListener { o, old, state ->
            if (state == Worker.State.SUCCEEDED) {

                val doc = webView.getEngine().getDocument()
                val links = doc.getElementsByTagName("a")
                for (i in 0 until links.length) {
                    val link = links.item(i)
                    link.attributes.getNamedItem("class")?.let {
                        when (it.nodeValue) {
                            "linkedToUI" -> {
                                link.attributes.getNamedItem("id")?.let {
                                    if (it.nodeValue.startsWith("tutorial")) {
                                        when (it.nodeValue) {
                                            "tutorial_part_1" -> {
                                                (link as EventTarget).addEventListener("click", { event ->
                                                    this.rnartist.lastSelectedFolderAbsPathInDB?.let {
                                                        if (it.equals(this.currentDB?.rootInvariantSeparatorsPath)) {
                                                            HelpDialog(
                                                                this,
                                                                "You cannot select the root folder. You need to select a subfolder to pursue the tutorial"
                                                            )
                                                        } else {
                                                            val w = TaskDialog(this)
                                                            w.task = AddStructureFromURL(
                                                                this,
                                                                "https://rnacentral.org/rna/URS000014FF3E/9606",
                                                                it
                                                            )
                                                        }
                                                    } ?: run {
                                                        HelpDialog(
                                                            this,
                                                            "You need to select a subfolder to pursue the tutorial"
                                                        )
                                                    }
                                                }, false)
                                            }

                                            "tutorial_compute_prerequisites_for_part_2" -> {
                                                (link as EventTarget).addEventListener("click", { event ->
                                                    currentDB?.let { rnartistDB ->
                                                        TaskDialog(this).task =
                                                            createPipelineToComputeTutorialPrerequisites(rnartistDB, 1)
                                                    } ?: run {
                                                        HelpDialog(
                                                            this,
                                                            "You need to open a database (even an empty one)"
                                                        )
                                                    }
                                                }, false)
                                            }

                                            "tutorial_compute_prerequisites_for_part_3" -> {
                                                (link as EventTarget).addEventListener("click", { event ->
                                                    currentDB?.let { rnartistDB ->
                                                        TaskDialog(this).task =
                                                            createPipelineToComputeTutorialPrerequisites(rnartistDB, 1)
                                                    } ?: run {
                                                        HelpDialog(
                                                            this,
                                                            "You need to open a database (even an empty one)"
                                                        )
                                                    }
                                                }, false)
                                            }

                                            "tutorial_compute_prerequisites_for_part_4" -> {
                                                (link as EventTarget).addEventListener("click", { event ->
                                                    currentDB?.let { rnartistDB ->
                                                        TaskDialog(this).task =
                                                            createPipelineToComputeTutorialPrerequisites(rnartistDB, 3)
                                                    } ?: run {
                                                        HelpDialog(
                                                            this,
                                                            "You need to open a database (even an empty one)"
                                                        )
                                                    }
                                                }, false)
                                            }

                                            "tutorial_part_3" -> {
                                                (link as EventTarget).addEventListener("click", { event ->
                                                    currentDrawing.get()?.let {
                                                        var t = Theme()
                                                        t.addConfiguration(
                                                            selector = { true },
                                                            ThemeProperty.color,
                                                            { getHTMLColorString(Color.BLUE) })
                                                        it.secondaryStructureDrawing.applyTheme(t)
                                                        t = theme {
                                                            details {
                                                                value = 3
                                                            }
                                                        }
                                                        it.secondaryStructureDrawing.applyTheme(t)
                                                        canvas2D.repaint()
                                                    } ?: run {
                                                        HelpDialog(this, "You need to load an RNA 2D in the canvas.")
                                                    }
                                                }, false)
                                            }

                                            "tutorial_part_8" -> {
                                                (link as EventTarget).addEventListener("click", { event ->
                                                    currentDB?.let { rnartistDB ->
                                                        val tasks = mutableListOf<RNArtistTask>()
                                                        var dataDirPath = Path(rnartistDB.rootInvariantSeparatorsPath, "tutorial")
                                                        if (!dataDirPath.exists())
                                                            tasks.add(CreateDBFolder(this, dataDirPath.invariantSeparatorsPathString))

                                                        dataDirPath = Path(rnartistDB.rootInvariantSeparatorsPath, "tutorial", "small_cajal")
                                                        if (!dataDirPath.exists())
                                                            tasks.add(CreateDBFolder(this, dataDirPath.invariantSeparatorsPathString))

                                                        var url = "https://rnacentral.org/rna/URS000026BDF0/9606"
                                                        var entryID = "URS000026BDF0"
                                                        var dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")

                                                        if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                                                            tasks.add(
                                                                AddStructureFromURL(
                                                                    this,
                                                                    url,
                                                                    dataDirPath.invariantSeparatorsPathString
                                                                )
                                                            )
                                                        tasks.add(
                                                            LoadStructure(
                                                                this,
                                                                dslScriptPath.invariantSeparatorsPathString
                                                            )
                                                        )

                                                        url = "https://rnacentral.org/rna/URS000014FF3E/9606"
                                                        entryID = "URS000014FF3E"
                                                        dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")

                                                        if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                                                            tasks.add(
                                                                AddStructureFromURL(
                                                                    this,
                                                                    url,
                                                                    dataDirPath.invariantSeparatorsPathString
                                                                )
                                                            )
                                                        tasks.add(
                                                            LoadStructure(
                                                                this,
                                                                dslScriptPath.invariantSeparatorsPathString
                                                            )
                                                        )

                                                        url = "https://rnacentral.org/rna/URS00005F3006/9606"
                                                        entryID = "URS00005F3006"
                                                        dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")

                                                        if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                                                            tasks.add(
                                                                AddStructureFromURL(
                                                                    this,
                                                                    url,
                                                                    dataDirPath.invariantSeparatorsPathString
                                                                )
                                                            )
                                                        tasks.add(
                                                            LoadStructure(
                                                                this,
                                                                dslScriptPath.invariantSeparatorsPathString
                                                            )
                                                        )

                                                        dataDirPath = Path(rnartistDB.rootInvariantSeparatorsPath, "tutorial", "small_nucleolar_RNA")
                                                        if (!dataDirPath.exists())
                                                            tasks.add(CreateDBFolder(this, dataDirPath.invariantSeparatorsPathString))

                                                        url = "https://rnacentral.org/rna/URS00008B2C89/9606"
                                                        entryID = "URS00008B2C89"
                                                        dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")

                                                        if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                                                            tasks.add(
                                                                AddStructureFromURL(
                                                                    this,
                                                                    url,
                                                                    dataDirPath.invariantSeparatorsPathString
                                                                )
                                                            )
                                                        tasks.add(
                                                            LoadStructure(
                                                                this,
                                                                dslScriptPath.invariantSeparatorsPathString
                                                            )
                                                        )

                                                        url = "https://rnacentral.org/rna/URS00003F1BD0/9606"
                                                        entryID = "URS00003F1BD0"
                                                        dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")

                                                        if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                                                            tasks.add(
                                                                AddStructureFromURL(
                                                                    this,
                                                                    url,
                                                                    dataDirPath.invariantSeparatorsPathString
                                                                )
                                                            )
                                                        tasks.add(
                                                            LoadStructure(
                                                                this,
                                                                dslScriptPath.invariantSeparatorsPathString
                                                            )
                                                        )

                                                        dataDirPath = Path(rnartistDB.rootInvariantSeparatorsPath, "tutorial", "RNAseP")
                                                        if (!dataDirPath.exists())
                                                            tasks.add(CreateDBFolder(this, dataDirPath.invariantSeparatorsPathString))

                                                        url = "https://rnacentral.org/rna/URS00004FBCB7/224308"
                                                        entryID = "URS00004FBCB7"
                                                        dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")

                                                        if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                                                            tasks.add(
                                                                AddStructureFromURL(
                                                                    this,
                                                                    url,
                                                                    dataDirPath.invariantSeparatorsPathString
                                                                )
                                                            )
                                                        tasks.add(
                                                            LoadStructure(
                                                                this,
                                                                dslScriptPath.invariantSeparatorsPathString
                                                            )
                                                        )

                                                        url = "https://rnacentral.org/rna/URS000013F331/9606"
                                                        entryID = "URS000013F331"
                                                        dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")

                                                        if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                                                            tasks.add(
                                                                AddStructureFromURL(
                                                                    this,
                                                                    url,
                                                                    dataDirPath.invariantSeparatorsPathString
                                                                )
                                                            )
                                                        tasks.add(
                                                            LoadStructure(
                                                                this,
                                                                dslScriptPath.invariantSeparatorsPathString
                                                            )
                                                        )

                                                        constructPipeline(*tasks.toTypedArray())
                                                        TaskDialog(this).task = tasks.first()
                                                    } ?: run {
                                                        HelpDialog(
                                                            this,
                                                            "You need to open a database (even an empty one)"
                                                        )
                                                    }
                                                }, false)
                                            }

                                            else -> {}
                                        }
                                    } else
                                        (link as EventTarget).addEventListener("click", EventListener { event ->
                                            rnartist.blinkUINode(it.nodeValue)
                                        }, false)
                                }
                            }

                            else -> {}
                        }
                    }
                    link.attributes.getNamedItem("href")?.let {
                        if (it.nodeValue.startsWith("http")) {
                            (link as EventTarget).addEventListener("click", EventListener { event ->
                                rnartist.getHostServices().showDocument(it.nodeValue)
                            }, false)
                        }

                    }

                }

            }
        }
    }

    /**
     * Return the first task of the pipeline. When this task is linked to a TaskDialog, the pipeline starts.
     * @param lastPart the part of the tutorial until which we need to compute the prerequisites (for example a value of 3 means all the prerequisites from part 1 to part 3)
     */
    private fun createPipelineToComputeTutorialPrerequisites(rnartistDB: RNArtistDB, lastPart: Int): RNArtistTask {
        val tasks = mutableListOf<RNArtistTask>()
        val dataDirPath = Path(rnartistDB.rootInvariantSeparatorsPath, "tutorial")
        val url = "https://rnacentral.org/rna/URS000014FF3E/9606"
        val tokens = url.split("/")
        val entryID = tokens[tokens.size - 2]
        val dslScriptPath = Path(dataDirPath.invariantSeparatorsPathString, "${entryID}.kts")
        (1..lastPart).forEach { part ->
            when (part) {
                1 -> {
                    if (!dataDirPath.exists())
                        tasks.add(CreateDBFolder(this, dataDirPath.invariantSeparatorsPathString))
                    if (!File(dslScriptPath.invariantSeparatorsPathString).exists())
                        tasks.add(
                            AddStructureFromURL(
                                this,
                                url,
                                dataDirPath.invariantSeparatorsPathString
                            )
                        )
                    tasks.add(
                        LoadStructure(
                            this,
                            dslScriptPath.invariantSeparatorsPathString
                        )
                    )
                }

                2 -> {

                }

                3 -> {
                    tasks.add(object: RNArtistTask(this) {
                        init {
                            setOnSucceeded { _ ->
                                this.resultNow().second?.let { exception ->
                                    this.rnartistDialog.displayException(exception)
                                } ?: run {
                                    this.rnartistDialog.stage.close()
                                }
                            }
                            setOnCancelled {
                                this.rnartistDialog.stage.close()
                            }
                        }

                        override fun call(): Pair<Any?, Exception?> {
                            try {
                                Platform.runLater {
                                    updateMessage(
                                        "Theming RNA 2D..."
                                    )
                                }
                                Thread.sleep(1000)

                                mediator.currentDrawing.get()?.let { currentDrawing ->
                                    val AUPairs =currentDrawing.secondaryStructureDrawing.allSecondaryInteractions.filter { it.residue is AShapeDrawing && it.pairedResidue is UShapeDrawing || it.residue is UShapeDrawing && it.pairedResidue is AShapeDrawing }
                                    var l = Location()
                                    AUPairs.forEach {
                                        l = l.addLocation(it.location)
                                    }
                                    val theme = currentDrawing.rnArtistEl.addTheme()
                                    with (theme.addDetails()) {
                                        this.setValue(3)
                                        this.setStep(1)
                                    }
                                    with (theme.addColor()) {
                                        this.setValue("blue")
                                        this.setStep(1)
                                    }
                                    with (theme.addColor()) {
                                        this.setValue("fuchsia")
                                        this.setType("junction N@junction phosphodiester_bond@junction")
                                        this.setStep(2)
                                    }
                                    with (theme.addColor()) {
                                        this.setValue("green")
                                        this.setType("apical_loop N@apical_loop phosphodiester_bond@apical_loop")
                                        this.setStep(3)
                                    }
                                    with (theme.addColor()) {
                                        this.setValue("orange")
                                        this.addLocation().setLocation(l)
                                        this.setStep(4)
                                    }
                                    currentDrawing.secondaryStructureDrawing.applyTheme(theme.toTheme())
                                    mediator.canvas2D.repaint()
                                }
                                return Pair(null, null)
                            } catch (e: Exception) {
                                return Pair(null, e)
                            }
                        }

                    })
                }
            }
        }
        constructPipeline(*tasks.toTypedArray())
        return tasks.first()
    }

    fun rollbackToFirstThemeInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            themeEl.getFirstThemeInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.clearTheme()
                currentDrawing.secondaryStructureDrawing.applyTheme(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun rollbackToPreviousThemeInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            themeEl.getFormerThemeInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.clearTheme()
                currentDrawing.secondaryStructureDrawing.applyTheme(it)
                this.canvas2D.repaint()
            }

        }
    }

    fun applyNextThemeInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            themeEl.getNextThemeInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.applyTheme(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun applyLastThemeInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val themeEl = currentDrawing.rnArtistEl.getThemeOrNew()
            themeEl.getLastThemeInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.applyTheme(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun rollbackToPreviousJunctionLayoutInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            layoutEl.getFormerLayoutInHistory().forEach {
                currentDrawing.secondaryStructureDrawing.applyLayout(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun rollbackLayoutHistoryToStart() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            if (layoutEl.undoRedoCursor != 0) {
                layoutEl.undoRedoCursor = 0
                currentDrawing.secondaryStructureDrawing.clearLayout()
                this.canvas2D.repaint()
            }
        }
    }

    fun applyNextLayoutInHistory() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            layoutEl.getNextLayoutInHistory()?.let {
                currentDrawing.secondaryStructureDrawing.applyLayout(it)
                this.canvas2D.repaint()
            }
        }
    }

    fun applyLayoutsInHistoryFromNextToEnd() {
        this.currentDrawing.get()?.let { currentDrawing ->
            val layoutEl = currentDrawing.rnArtistEl.getLayoutOrNew()
            layoutEl.getLayoutInHistoryFromNextToEnd()?.let {
                currentDrawing.secondaryStructureDrawing.applyLayout(it)
                this.canvas2D.repaint()
            }
        }
    }

    /*
    fun focusInChimera() {
        this.currentDrawing.get()?.let { drawingDisplayed ->
            chimeraDriver.setFocus(
                canvas2D.getSelectedPositions()
            )
        }
    }

    fun pivotInChimera() {
        this.currentDrawing.get()?.let { drawingDisplayed->
            chimeraDriver.setPivot(
                canvas2D.getSelectedPositions()
            )
        }
    }*/

}