package io.github.fjossinet.rnartist.gui.editor

import com.google.gson.JsonParser
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.model.editor.*
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Font
import javafx.scene.text.TextFlow
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Modality
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.Desktop
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileFilter
import java.io.FileReader
import java.io.StringReader
import java.net.URL
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
            var selection = junctionLocation
            mediator.drawingDisplayed.get()?.let {
                if (it.drawing.secondaryStructure.rna.useAlignmentNumberingSystem)
                    selection = it.drawing.secondaryStructure.rna.mapLocation(selection)
            }
            val junctionLayoutKw =
                searchFirst { it is JunctionLayoutKw && it.inFinalScript && it.getLocation() == selection } as JunctionLayoutKw?
            //We have found a junctionKw with the same location, we update it
            junctionLayoutKw?.let {
                it.setOutIds(outIds) //We just need to change the outIds (type and location should be the same)
            } ?: run { //we create a new one
                val junctionLayoutKw = searchFirst { it is JunctionLayoutKw && !it.inFinalScript } as JunctionLayoutKw
                junctionLayoutKw.addButton.fire()
                junctionLayoutKw.setOutIds(outIds)
                junctionLayoutKw.setType(type)
                mediator.drawingDisplayed.get()?.let {
                   junctionLayoutKw.setLocation(selection)
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
            var selection = junctionLocation
            mediator.drawingDisplayed.get()?.let {
                if (it.drawing.secondaryStructure.rna.useAlignmentNumberingSystem)
                    selection = it.drawing.secondaryStructure.rna.mapLocation(selection)
            }
            val junctionLayoutKw =
                searchFirst { it is JunctionLayoutKw && it.inFinalScript && it.getLocation() == selection } as JunctionLayoutKw?
            //We have found a junctionKw with the same location, we update it
            junctionLayoutKw?.let {
                it.setRadius(radius) //We just need to change the outIds (type and location should be the same)
            } ?: run { //we create a new one
                val junctionLayoutKw = searchFirst { it is JunctionLayoutKw && !it.inFinalScript } as JunctionLayoutKw
                junctionLayoutKw.addButton.fire()
                junctionLayoutKw.setRadius(radius)
                junctionLayoutKw.setType(type)
                mediator.drawingDisplayed.get()?.let {
                    junctionLayoutKw.setLocation(selection)
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
            var selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelection().flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
            selection?.let { sel  ->
                mediator.drawingDisplayed.get()?.let {
                    if (it.drawing.secondaryStructure.rna.useAlignmentNumberingSystem)
                        selection = it.drawing.secondaryStructure.rna.mapLocation(sel)
                }
            }
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
                   detailsKw.setLocation(l)
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
            var selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelection().flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
            selection?.let { sel  ->
                mediator.drawingDisplayed.get()?.let {
                    if (it.drawing.secondaryStructure.rna.useAlignmentNumberingSystem)
                        selection = it.drawing.secondaryStructure.rna.mapLocation(sel)
                }
            }
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
                    colorKw.setLocation(l)
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
            var selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelection().flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
            selection?.let { sel  ->
                mediator.drawingDisplayed.get()?.let {
                    if (it.drawing.secondaryStructure.rna.useAlignmentNumberingSystem)
                        selection = it.drawing.secondaryStructure.rna.mapLocation(sel)
                }
            }
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
                    lineKw.setLocation(l)
                }
            }
            allowScriptInit = true
            initScript()
        }
    }
}

class ScriptEditor(val mediator: Mediator): GridPane() {

    var currentScriptLocation: File? = null
    val script:RNArtistScript
    val engine: ScriptEngine
    var buttonRadius = 15.0

    init {
        val rConstraints1 = RowConstraints()
        rConstraints1.vgrow = Priority.NEVER
        val rConstraints2 = RowConstraints()
        rConstraints2.vgrow = Priority.ALWAYS
        this.rowConstraints.addAll(rConstraints1, rConstraints2)

        val cConstraints1 = ColumnConstraints()
        cConstraints1.hgrow = Priority.ALWAYS
        val cConstraints2 = ColumnConstraints()
        cConstraints2.hgrow = Priority.NEVER
        this.columnConstraints.addAll(cConstraints1, cConstraints2)

        script = RNArtistScript(mediator)
        val manager = ScriptEngineManager()
        this.engine = manager.getEngineByExtension("kts")
        script.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.backgroundEditorColor)}"
        script.padding = Insets(10.0, 10.0, 10.0, 10.0)
        script.lineSpacing = 10.0
        script.tabSize = 6
        script.layout()

        val topToolBar = ToolBar()
        topToolBar.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        topToolBar.padding = Insets(10.0, 5.0, 10.0, 5.0)

        val loadScriptPane = GridPane()
        loadScriptPane.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        loadScriptPane.vgap = 5.0
        loadScriptPane.hgap = 5.0

        var l = Label("Load Script")
        setHalignment(l, HPos.CENTER)
        setConstraints(l, 0, 0)
        loadScriptPane.children.add(l)

        val loadScript = MenuButton(null, FontIcon("fas-sign-in-alt:15"))

        val newScript = Menu("New Script..")

        val emptyScript = MenuItem("Empty Script")
        emptyScript.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/empty.kts"))
            )
        }
        newScript.items.add(emptyScript)

        var menuItem = MenuItem("2D from bracket notation")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_bn.kts"))
            )
        }

        newScript.items.add(menuItem)

        val fromLocalFilesMenu = Menu("2D from Local Files")
        val fromDatabasesMenu = Menu("2D from Databases")
        newScript.items.addAll(fromLocalFilesMenu, fromDatabasesMenu)

        menuItem = MenuItem("Vienna Format")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_vienna_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("CT Format")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_ct_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("BPSeq Format")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_bpseq_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("PDB Format")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_pdb_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Stockholm Format")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_stockholm_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Rfam DB")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            val inputdialog = TextInputDialog("RF01072")
            inputdialog.contentText = "Rfam ID: "
            inputdialog.headerText = "Enter your Rfam ID"

            val rfamID = inputdialog.showAndWait()

            if (!rfamID.isEmpty) {
                RNArtistTaskWindow(mediator).task = LoadScript(
                    mediator,
                    script = StringReader(URL("https://raw.githubusercontent.com/fjossinet/Rfam-for-RNArtist/main/data/${rfamID.get()}/rnartist.kts").readText())
                )
            }


        }
        fromDatabasesMenu.items.add(menuItem)

        menuItem = MenuItem("PDB")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_pdb.kts"))
            )
        }
        fromDatabasesMenu.items.add(menuItem)

        menuItem = MenuItem("RNACentral")
        menuItem.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_rnacentral.kts"))
            )
        }
        fromDatabasesMenu.items.add(menuItem)

        val openFile = MenuItem("Open Script..")
        openFile.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.initialDirectory = File(mediator.rnartist.getInstallDir(), "samples")
            val file = fileChooser.showOpenDialog(mediator.rnartist.stage)
            file?.let {
                mediator.scriptEditor.currentScriptLocation = file.parentFile
                RNArtistTaskWindow(mediator).task = LoadScript(mediator, script = FileReader(file))
            }
        }

        val openGist = MenuItem("Open Gist..")
        openGist.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation = null
            val gistInput = TextInputDialog()
            gistInput.title = "Enter your Gist ID"
            gistInput.graphic = null
            gistInput.headerText = null
            gistInput.contentText = "Gist ID"
            gistInput.editor.text = "Paste your ID"
            val gistID = gistInput.showAndWait()
            if (gistID.isPresent && !gistID.isEmpty) {
                RNArtistTaskWindow(mediator).task = LoadGist(mediator, gistID.get())
            }
        }

        loadScript.items.addAll(newScript, openFile, openGist)

        setConstraints(loadScript, 0, 1)
        setHalignment(loadScript, HPos.CENTER)
        loadScriptPane.children.add(loadScript)

        val exportScriptPane = GridPane()
        exportScriptPane.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        exportScriptPane.vgap = 5.0
        exportScriptPane.hgap = 5.0

        l = Label("Save/Export Script")
        setHalignment(l, HPos.CENTER)
        setConstraints(l, 0, 0)
        exportScriptPane.children.add(l)

        val saveScript = MenuButton(null, FontIcon("fas-sign-out-alt:15"))
        saveScript.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))

        val saveProjectAs = MenuItem("Save Project as..")
        saveProjectAs.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                val dialog = TextInputDialog("Project ${File(RnartistConfig.projectsFolder).listFiles(FileFilter { it.isDirectory }).size+1}")
                dialog.initModality(Modality.NONE)
                dialog.title = "Save Project"
                dialog.headerText = null
                dialog.contentText = "Project name:"
                var projectName = dialog.showAndWait()
                while (projectName.isPresent && !projectName.isEmpty && File(File(RnartistConfig.projectsFolder), projectName.get().trim()).exists()) {
                    if (File(File(RnartistConfig.projectsFolder), projectName.get().trim()).exists())
                        dialog.headerText = "This project already exists"
                    projectName = dialog.showAndWait()
                }
                if (projectName.isPresent && !projectName.isEmpty)
                    RNArtistTaskWindow(mediator).task = SaveProject(mediator, File(File(RnartistConfig.projectsFolder), projectName.get()))
            }
        }

        val updateProject = MenuItem("Update Project")
        updateProject.onAction = EventHandler {
            mediator.scriptEditor.currentScriptLocation?.let { projectDir ->
                RnartistConfig.projectsFolder?.let {
                    if (projectDir.absolutePath.startsWith(it))
                        RNArtistTaskWindow(mediator).task = SaveProject(mediator, projectDir)
                }
            }
        }

        val saveAsFile = MenuItem("Export in File..")
        saveAsFile.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                val dir = DirectoryChooser().showDialog(mediator.rnartist.stage)
                dir?.let {
                    val dialog = TextInputDialog()
                    dialog.initModality(Modality.NONE)
                    dialog.title = "Export Project"
                    dialog.headerText = null
                    dialog.contentText = "Project name:"
                    var projectName = dialog.showAndWait()
                    while (projectName.isPresent && !projectName.isEmpty && File(dir, projectName.get().trim()).exists()) {
                        if (File(dir, projectName.get().trim()).exists())
                            dialog.headerText = "This project already exists in ${dir.name}"
                        projectName = dialog.showAndWait()
                    }
                    if (projectName.isPresent && !projectName.isEmpty)
                        RNArtistTaskWindow(mediator).task = SaveProject(mediator, File(dir, projectName.get().trim()))
                }

            }
        }

        val saveAsGist = MenuItem("Publish as GitHub Gist..")
        saveAsGist.onAction = EventHandler {
            val token = ""
            if (token.trim().isNotEmpty()) {
                mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                    val dialog = Dialog<String>()
                    dialog.headerText = "Description of your Plot"
                    dialog.initModality(Modality.NONE)
                    dialog.title = "Publish Plot as a GitHub Gist"
                    dialog.contentText = null
                    dialog.dialogPane.content = TextArea()
                    val publish = ButtonType("Publish", ButtonBar.ButtonData.OK_DONE)
                    dialog.dialogPane.buttonTypes.add(publish)
                    dialog.setResultConverter { b ->
                        if (b == publish) {
                            (dialog.dialogPane.content as TextArea).text.trim()
                        } else null
                    }
                    val description = dialog.showAndWait()
                    if (description.isPresent && !description.isEmpty) {
                        try {
                            val client = OkHttpClient()
                            val body = """{"description":"${
                                description.get().trim()
                            }", "files":{"rnartist.kts":{"content":"${
                                mediator.scriptEditor.getEntireScriptAsText().replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")
                            }"},
                        "rnartist.svg":{"content":"${
                                drawing.asSVG(Rectangle2D.Double(0.0, 0.0, 800.0, 800.0)).replace("\"", "\\\"")
                            }"}}, "public":true}"""
                            println(body)
                            val request = Request.Builder()
                                .url("https://api.github.com/gists")
                                .header("User-Agent", "OkHttp Headers.java")
                                .addHeader("Authorization", "bearer $token")
                                .post(
                                    body.toRequestBody("application/json".toMediaTypeOrNull())
                                )
                                .build()

                            client.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) {
                                    println("Problem!")
                                    println(response.body?.charStream()?.readText())
                                } else {
                                    val root =
                                        JsonParser.parseString(response.body?.charStream()?.readText())
                                            .asJsonObject
                                    val alert = Alert(Alert.AlertType.INFORMATION)
                                    alert.headerText = "Script published Successfully."
                                    alert.graphic = FontIcon("fab-github:15")
                                    alert.buttonTypes.clear()
                                    alert.buttonTypes.add(ButtonType.OK)
                                    alert.buttonTypes.add(ButtonType("Show me", ButtonBar.ButtonData.HELP))
                                    val result = alert.showAndWait()
                                    if (result.isPresent && result.get() != ButtonType.OK) { //show me
                                        Desktop.getDesktop().browse(URL(root.get("html_url").asString).toURI())
                                    }
                                }

                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
            } else {
                val alert = Alert(Alert.AlertType.WARNING)
                alert.headerText = "Gist publication not available."
                alert.contentText = "This feature will be activated soon"
                alert.show()
            }
        }

        saveScript.getItems().addAll(saveProjectAs, updateProject, saveAsFile, saveAsGist)

        setHalignment(saveScript, HPos.CENTER)
        setConstraints(saveScript, 0, 1)
        exportScriptPane.children.add(saveScript)

        val fontPane = GridPane()
        fontPane.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        fontPane.vgap = 5.0
        fontPane.hgap = 5.0

        l = Label("Script Font")
        setHalignment(l, HPos.CENTER)
        setConstraints(l, 0, 0, 2, 1)
        fontPane.children.add(l)

        val fontFamilies = Font.getFamilies()

        val fontChooser = ComboBox<String>()
        fontChooser.items.addAll(fontFamilies)
        fontChooser.value = RnartistConfig.editorFontName
        setConstraints(fontChooser, 0, 1)

        fontChooser.onAction = EventHandler {
            RnartistConfig.editorFontName = fontChooser.value
            val hits = mutableListOf<DSLElementInt>()
            mediator.scriptEditor.script.getScriptRoot().searchAll(hits) { it is DSLElement }
            hits.forEach {
                it.fontName = fontChooser.value
                (it as? OptionalDSLKeyword)?.addButton?.setFontName(fontChooser.value)
                (it as? OptionalDSLParameter)?.addButton?.setFontName(fontChooser.value)
            }
            mediator.scriptEditor.script.initScript()
        }

        fontPane.children.add(fontChooser)

        val sizeFont = Spinner<Int>(5, 40, RnartistConfig.editorFontSize)
        sizeFont.isEditable = true
        sizeFont.prefWidth = 75.0
        sizeFont.onMouseClicked = EventHandler {
            RnartistConfig.editorFontSize = sizeFont.value
            val hits = mutableListOf<DSLElementInt>()
            mediator.scriptEditor.script.getScriptRoot().searchAll(hits) { it is DSLElement }
            hits.forEach {
                it.fontSize = sizeFont.value
                (it as? OptionalDSLKeyword)?.addButton?.setFontSize(sizeFont.value)
                (it as? OptionalDSLParameter)?.addButton?.setFontSize(sizeFont.value)
            }
            mediator.scriptEditor.script.initScript()
        }

        setConstraints(sizeFont, 1, 1)
        fontPane.children.add(sizeFont)

        val runPane = GridPane()
        runPane.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        runPane.vgap = 5.0
        runPane.hgap = 5.0

        l = Label("Run Script")
        setHalignment(l, HPos.CENTER)
        setConstraints(l, 0, 0, 3, 1)
        runPane.children.add(l)
        var c = Circle(buttonRadius/2.0, buttonRadius/2.0, buttonRadius)

        val exportInFiles = Button(null, FontIcon("fas-file:15"))
        exportInFiles.setShape(c)
        exportInFiles.setMinSize(2 * buttonRadius, 2 * buttonRadius)
        exportInFiles.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
        exportInFiles.onAction = EventHandler {
            Platform.runLater {
                RNArtistTaskWindow(mediator).task = ApplyExportInScript(mediator)
            }
        }

        setConstraints(exportInFiles, 0, 1)
        runPane.children.add(exportInFiles)

        val runThemeAndLayout = Button(null, FontIcon("fas-magic:15"))
        runThemeAndLayout.setShape(c)
        runThemeAndLayout.setMinSize(2 * buttonRadius, 2 * buttonRadius)
        runThemeAndLayout.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
        runThemeAndLayout.onAction = EventHandler {
            Platform.runLater {
                RNArtistTaskWindow(mediator).task = ApplyThemeAndLayout(mediator)
            }
        }

        setConstraints(runThemeAndLayout, 1, 1)
        runPane.children.add(runThemeAndLayout)

        val runEntireScript = Button(null, FontIcon("fas-play:15"))
        runEntireScript.setShape(c)
        runEntireScript.setMinSize(2 * buttonRadius, 2 * buttonRadius)
        runEntireScript.setMaxSize(2 * buttonRadius, 2 * buttonRadius)
        runEntireScript.onAction = EventHandler {
            Platform.runLater {
                RNArtistTaskWindow(mediator).task = RunEntireScript(mediator)
            }
        }

        setConstraints(runEntireScript, 2, 1)
        runPane.children.add(runEntireScript)

        val s1 = Separator()
        s1.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s2 = Separator()
        s2.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s3 = Separator()
        s3.padding = Insets(0.0, 5.0, 0.0, 5.0)

        topToolBar.items.addAll(loadScriptPane, s1, exportScriptPane, s2, fontPane, s3, runPane)
        this.add(topToolBar, 0, 0)

        var scrollpane = ScrollPane(script)
        scrollpane.isFitToHeight = true
        script.minWidthProperty().bind(scrollpane.widthProperty())
        this.add(scrollpane, 0, 1)

        val configurationPanel = VBox()
        configurationPanel.minWidth = 180.0
        configurationPanel.prefWidth = 180.0
        configurationPanel.maxWidth = 180.0
        configurationPanel.padding = Insets(10.0, 10.0, 10.0, 10.0)
        configurationPanel.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        configurationPanel.alignment = Pos.TOP_CENTER
        scrollpane = ScrollPane(configurationPanel)
        scrollpane.isFitToWidth = true
        scrollpane.isFitToHeight = true
        scrollpane.vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        this.add(scrollpane, 1, 0, 1, 2)

        l = Label("Script Layout")
        l.textFill = Color.WHITE
        l.maxWidth = 150.0
        configurationPanel.children.add(l)
        var s = Separator()
        s.maxWidth = 150.0
        configurationPanel.children.add(s)

        val buttonsPanel = ButtonsPanel(mediator, panelRadius = 60.0, buttonRadius = 12.0, buttomsPerRow = 6)

        val increaseTab = Button(null, FontIcon("fas-indent:12"))
        increaseTab.onAction = EventHandler {
            script.tabSize++
        }
        buttonsPanel.addButton(increaseTab)

        val decreaseTab = Button(null, FontIcon("fas-outdent:12"))
        decreaseTab.onAction = EventHandler {
            if (script.tabSize > 1) {
                script.tabSize--
            }
        }
        buttonsPanel.addButton(decreaseTab)

        val decreaseLineSpacing = Button(null, FontIcon("fas-compress-alt:12"))
        decreaseLineSpacing.onAction = EventHandler {
            script.lineSpacing--
        }
        buttonsPanel.addButton(decreaseLineSpacing)

        val increaseLineSpacing = Button(null, FontIcon("fas-expand-alt:12"))
        increaseLineSpacing.onAction = EventHandler {
            script.lineSpacing++
        }
        buttonsPanel.addButton(increaseLineSpacing)

        val expandAll = Button(null, FontIcon("fas-plus:12"))
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
        buttonsPanel.addButton(expandAll)

        val collapseAll = Button(null, FontIcon("fas-minus:12"))
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
        buttonsPanel.addButton(collapseAll)

        configurationPanel.children.add(buttonsPanel)

        l = Label("Script Theme")
        l.textFill = Color.WHITE
        l.maxWidth = 150.0
        configurationPanel.children.add(l)
        s = Separator()
        s.maxWidth = 150.0
        configurationPanel.children.add(s)

        configurationPanel.children.add(ScriptEditorColorPicker(mediator))

    }

    fun getEntireScriptAsText(useAbsolutePath: Boolean = false): String {
        val scriptContent = StringBuilder()
        script.getScriptRoot().dumpText(scriptContent, useAbsolutePath)
        return "import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent}"
    }

    fun getLayoutAsText(useAbsolutePath:Boolean= false): String {
        val scriptContent = StringBuilder()
        val layoutKw = script.getScriptRoot().getLayoutKw()
        layoutKw.dumpText(scriptContent, useAbsolutePath)
        return "import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent}"
    }

    fun getThemeAsText(useAbsolutePath:Boolean= false): String {
        val scriptContent = StringBuilder()
        val themeKw = script.getScriptRoot().getThemeKw()
        themeKw.dumpText(scriptContent, useAbsolutePath)
        return "import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent}"
    }

    fun getInputFileFields():List<InputFileKw> {
        val hits = mutableListOf<DSLElementInt>()
        script.getScriptRoot().searchAll(hits, {it is InputFileKw})
        return hits.map { it  as InputFileKw }
    }

}
