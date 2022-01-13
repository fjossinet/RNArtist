package io.github.fjossinet.rnartist.gui.editor

import com.google.gson.JsonParser
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.gui.LoadGist
import io.github.fjossinet.rnartist.gui.LoadScript
import io.github.fjossinet.rnartist.gui.RunScript
import io.github.fjossinet.rnartist.gui.SaveProject
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.RNArtistTaskWindow
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.model.editor.*
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.Desktop
import java.awt.geom.Rectangle2D
import java.io.*
import java.lang.IllegalStateException
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

class ThemeAndLayoutScript(mediator: Mediator) : Script(mediator) {

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
                junctionLayoutKw.setLocation(junctionLocation)
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
                junctionLayoutKw.setLocation(junctionLocation)
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
                selection?.let {
                    detailsKw.setLocation(it)
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
                selection?.let {
                    colorKw.setLocation(it)
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
                selection?.let {
                    lineKw.setLocation(it)
                }
            }
            allowScriptInit = true
            initScript()
        }
    }
}

class ScriptEditor(val mediator: Mediator) {

    var currentScriptLocation: File? = null
    val themeAndLayoutScript = ThemeAndLayoutScript(mediator)
    val engine: ScriptEngine
    val stage = Stage()
    private val run = Button(null, FontIcon("fas-play:15"))

    init {
        stage.title = "Script Editor"
        val manager = ScriptEngineManager()
        this.engine = manager.getEngineByExtension("kts")
        createScene(stage)
    }

    private fun createScene(stage: Stage) {
        val root = BorderPane()
        themeAndLayoutScript.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.backgroundEditorColor)}"
        themeAndLayoutScript.padding = Insets(10.0, 10.0, 10.0, 10.0)
        themeAndLayoutScript.lineSpacing = 10.0
        themeAndLayoutScript.tabSize = 6
        themeAndLayoutScript.layout()

        val topToolbar = ToolBar()
        topToolbar.padding = Insets(5.0, 5.0, 5.0, 5.0)

        val loadScriptPane = GridPane()
        loadScriptPane.vgap = 5.0
        loadScriptPane.hgap = 5.0

        var l = Label("Load")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        loadScriptPane.children.add(l)

        val loadScript = MenuButton(null, FontIcon("fas-sign-in-alt:15"))
        val scriptsLibraryMenu = Menu("Scripts Templates")

        val newScript = MenuItem("New Script")
        newScript.onAction = EventHandler {
            //we erase the 2D displayed
            mediator.drawingDisplayed.set(null)
            mediator.canvas2D.repaint()
            currentScriptLocation = null
            //we erase the previous scripts
            themeAndLayoutScript.setScriptRoot(RNArtistKw(themeAndLayoutScript))
        }

        val loadFile = MenuItem("Load Script..")
        loadFile.onAction = EventHandler {

            val fileChooser = FileChooser()
            fileChooser.initialDirectory = File(mediator.rnartist.getInstallDir(), "samples")
            val file = fileChooser.showOpenDialog(stage)
            file?.let {
                currentScriptLocation = file.parentFile
                RNArtistTaskWindow(mediator).task = LoadScript(mediator, script = FileReader(file), true)
            }
        }

        val loadGist = MenuItem("Load Gist..")
        loadGist.onAction = EventHandler {
            currentScriptLocation = null
            val gistInput = TextInputDialog()
            gistInput.title = "Enter your Gist ID"
            gistInput.graphic = null
            gistInput.headerText = null
            gistInput.contentText = "Gist ID"
            gistInput.editor.text = "Paste your ID"
            var gistID = gistInput.showAndWait()
            if (gistID.isPresent && !gistID.isEmpty) {
                RNArtistTaskWindow(mediator).task = LoadGist(mediator, gistID.get())
            }
        }

        loadScript.getItems().addAll(newScript, loadFile, loadGist, scriptsLibraryMenu)

        val load2D = Menu("Load 2D..")

        scriptsLibraryMenu.items.add(load2D)

        var menuItem = MenuItem("...from bracket notation")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_bn.kts"))
            )
        }

        load2D.items.add(menuItem)

        menuItem = MenuItem("...from bracket notation with data")
        menuItem.setOnAction {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_bn_with_data.kts"))
            )
        }

        load2D.items.add(menuItem)

        val fromLocalFilesMenu = Menu("...from Local Files")
        val fromDatabasesMenu = Menu("...from Databases")
        load2D.items.addAll(fromLocalFilesMenu, fromDatabasesMenu)

        menuItem = MenuItem("Vienna Format")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_vienna_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("CT Format")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_ct_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("BPSeq Format")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_bpseq_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Stockholm Format")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_stockholm_file.kts"))
            )
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Rfam DB")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_rfam.kts"))
            )
        }
        fromDatabasesMenu.items.add(menuItem)

        menuItem = MenuItem("PDB")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_pdb.kts"))
            )
        }
        fromDatabasesMenu.items.add(menuItem)

        menuItem = MenuItem("RNACentral")
        menuItem.onAction = EventHandler {
            currentScriptLocation = null
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_rnacentral.kts"))
            )
        }
        fromDatabasesMenu.items.add(menuItem)


        val themes = Menu("Create Theme..")
        //scriptsLibraryMenu.items.add(themes)

        val layout = Menu("Create Layout..")
        //scriptsLibraryMenu.items.add(layout)

        GridPane.setConstraints(loadScript, 0, 1)
        loadScriptPane.children.add(loadScript)

        val exportScriptPane = GridPane()
        exportScriptPane.vgap = 5.0
        exportScriptPane.hgap = 5.0

        l = Label("Export")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        exportScriptPane.children.add(l)

        val saveScript = MenuButton(null, FontIcon("fas-sign-out-alt:15"))

        val saveAsFile = MenuItem("Export in File..")
        saveAsFile.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        saveAsFile.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                val dir = DirectoryChooser().showDialog(stage)
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
                    val publish = ButtonType("Publish", ButtonData.OK_DONE)
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
                                getScriptAsText().replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")
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
                                            .getAsJsonObject()
                                    val alert = Alert(Alert.AlertType.INFORMATION)
                                    alert.headerText = "Script published Successfully."
                                    alert.graphic = FontIcon("fab-github:15")
                                    alert.buttonTypes.clear()
                                    alert.buttonTypes.add(ButtonType.OK)
                                    alert.buttonTypes.add(ButtonType("Show me", ButtonData.HELP))
                                    var result = alert.showAndWait()
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

        saveScript.getItems().addAll(saveAsFile, saveAsGist)

        GridPane.setConstraints(saveScript, 0, 1)
        exportScriptPane.children.add(saveScript)

        val fontPane = GridPane()
        fontPane.vgap = 5.0
        fontPane.hgap = 5.0

        l = Label("Font")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0, 2, 1)
        fontPane.children.add(l)

        val fontFamilies = Font.getFamilies()

        val fontChooser = ComboBox<String>()
        fontChooser.items.addAll(fontFamilies)
        fontChooser.value = RnartistConfig.editorFontName
        GridPane.setConstraints(fontChooser, 0, 1)

        fontChooser.onAction = EventHandler {
            RnartistConfig.editorFontName = fontChooser.value
            val hits = mutableListOf<DSLElementInt>()
            themeAndLayoutScript.getScriptRoot().searchAll(hits) { it is DSLElement }
            hits.forEach {
                it.fontName = fontChooser.value
                (it as? OptionalDSLKeyword)?.addButton?.setFontName(fontChooser.value)
                (it as? OptionalDSLParameter)?.addButton?.setFontName(fontChooser.value)
            }
            themeAndLayoutScript.initScript()
        }

        fontPane.children.add(fontChooser)

        val sizeFont = Spinner<Int>(5, 40, RnartistConfig.editorFontSize)
        sizeFont.isEditable = true
        sizeFont.prefWidth = 75.0
        sizeFont.onMouseClicked = EventHandler {
            RnartistConfig.editorFontSize = sizeFont.value
            val hits = mutableListOf<DSLElementInt>()
            themeAndLayoutScript.getScriptRoot().searchAll(hits) { it is DSLElement }
            hits.forEach {
                it.fontSize = sizeFont.value
                (it as? OptionalDSLKeyword)?.addButton?.setFontSize(sizeFont.value)
                (it as? OptionalDSLParameter)?.addButton?.setFontSize(sizeFont.value)
            }
            themeAndLayoutScript.initScript()
        }

        GridPane.setConstraints(sizeFont, 1, 1)
        fontPane.children.add(sizeFont)

        val runPane = GridPane()
        runPane.vgap = 5.0
        runPane.hgap = 5.0

        l = Label("Run")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        runPane.children.add(l)

        run.onAction = EventHandler {
            Platform.runLater {
                RNArtistTaskWindow(mediator).task = RunScript(mediator)
            }

        }

        GridPane.setConstraints(run, 0, 1)
        runPane.children.add(run)

        val s1 = Separator()
        s1.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s2 = Separator()
        s2.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s3 = Separator()
        s3.padding = Insets(0.0, 5.0, 0.0, 5.0)

        topToolbar.items.addAll(loadScriptPane, s1, exportScriptPane, s2, fontPane, s3, runPane)

        val leftToolbar = VBox()
        leftToolbar.alignment = Pos.TOP_CENTER
        leftToolbar.spacing = 5.0
        leftToolbar.padding = Insets(10.0, 5.0, 10.0, 5.0)

        val decreaseTab = Button(null, FontIcon("fas-outdent:15"))
        decreaseTab.onAction = EventHandler {
            if (themeAndLayoutScript.tabSize > 1) {
                themeAndLayoutScript.tabSize--
            }
        }

        val increaseTab = Button(null, FontIcon("fas-indent:15"))
        increaseTab.onAction = EventHandler {
            themeAndLayoutScript.tabSize++
        }

        val decreaseLineSpacing = Button(null, FontIcon("fas-compress-alt:15"))
        decreaseLineSpacing.onAction = EventHandler {
            themeAndLayoutScript.lineSpacing--
        }

        val increaseLineSpacing = Button(null, FontIcon("fas-expand-alt:15"))
        increaseLineSpacing.onAction = EventHandler {
            themeAndLayoutScript.lineSpacing++
        }

        val expandAll = Button(null, FontIcon("fas-plus:15"))
        expandAll.onAction = EventHandler {
            themeAndLayoutScript.allowScriptInit = false
            themeAndLayoutScript.children.filterIsInstance<DSLKeyword.KeywordNode>().map {
                if (it.children.isNotEmpty())
                    (it.children.get(it.children.size - 2) as? Collapse)?.let {
                        if (it.collapsed)
                            it.fire()
                    }
            }
            themeAndLayoutScript.allowScriptInit = true
            themeAndLayoutScript.initScript()
        }

        val collapseAll = Button(null, FontIcon("fas-minus:15"))
        collapseAll.onAction = EventHandler {
            themeAndLayoutScript.allowScriptInit = false
            themeAndLayoutScript.children.filterIsInstance<DSLKeyword.KeywordNode>().map {
                if (it.children.isNotEmpty())
                    (it.children.get(it.children.size - 2) as? Collapse)?.let {
                        if (!it.collapsed)
                            it.fire()
                    }
            }
            themeAndLayoutScript.allowScriptInit = true
            themeAndLayoutScript.initScript()
        }

        val bgColor = ColorPicker()
        bgColor.value = awtColorToJavaFX(RnartistConfig.backgroundEditorColor)
        bgColor.styleClass.add("button")
        bgColor.style = "-fx-color-label-visible: false ;"
        bgColor.onAction = EventHandler {
            themeAndLayoutScript.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(bgColor.value))}"
            RnartistConfig.backgroundEditorColor = javaFXToAwt(bgColor.value)
        }

        val kwColor = ColorPicker()
        kwColor.value = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        kwColor.styleClass.add("button")
        kwColor.style = "-fx-color-label-visible: false ;"
        kwColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            themeAndLayoutScript.getScriptRoot()?.searchAll(hits) { it is DSLKeyword }
            hits.forEach {
                it.color = kwColor.value
                (it as DSLKeyword).collapseButton.setColor(kwColor.value)
                (it as? OptionalDSLKeyword)?.addButton?.setColor(kwColor.value)
            }
            RnartistConfig.keywordEditorColor = javaFXToAwt(kwColor.value)
            themeAndLayoutScript.initScript()
        }

        val bracesColor = ColorPicker()
        bracesColor.value = awtColorToJavaFX(RnartistConfig.bracesEditorColor)
        bracesColor.styleClass.add("button")
        bracesColor.style = "-fx-color-label-visible: false ;"
        bracesColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            themeAndLayoutScript.getScriptRoot()?.searchAll(hits) { it is OpenedCurly || it is ClosedCurly }
            hits.forEach {
                it.color = bracesColor.value
            }
            RnartistConfig.bracesEditorColor = javaFXToAwt(bracesColor.value)
            themeAndLayoutScript.initScript()
        }

        val keyParamColor = ColorPicker()
        keyParamColor.value = awtColorToJavaFX(RnartistConfig.keyParamEditorColor)
        keyParamColor.styleClass.add("button")
        keyParamColor.style = "-fx-color-label-visible: false ;"
        keyParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            themeAndLayoutScript.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).key.color = keyParamColor.value
                (it as? OptionalDSLParameter)?.addButton?.setColor(keyParamColor.value)
            }
            RnartistConfig.keyParamEditorColor = javaFXToAwt(keyParamColor.value)
            themeAndLayoutScript.initScript()
        }

        val operatorParamColor = ColorPicker()
        operatorParamColor.value = awtColorToJavaFX(RnartistConfig.operatorParamEditorColor)
        operatorParamColor.styleClass.add("button")
        operatorParamColor.style = "-fx-color-label-visible: false ;"
        operatorParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            themeAndLayoutScript.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).operator.color = operatorParamColor.value
            }
            RnartistConfig.operatorParamEditorColor = javaFXToAwt(operatorParamColor.value)
            themeAndLayoutScript.initScript()
        }

        val valueParamColor = ColorPicker()
        valueParamColor.value = awtColorToJavaFX(RnartistConfig.valueParamEditorColor)
        valueParamColor.styleClass.add("button")
        valueParamColor.style = "-fx-color-label-visible: false ;"
        valueParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElementInt>()
            themeAndLayoutScript.getScriptRoot()?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).value.color = valueParamColor.value
            }
            RnartistConfig.valueParamEditorColor = javaFXToAwt(valueParamColor.value)
            themeAndLayoutScript.initScript()
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

        root.top = topToolbar
        root.left = leftToolbar

        var scrollpane = ScrollPane(themeAndLayoutScript)
        scrollpane.isFitToHeight = true
        themeAndLayoutScript.minWidthProperty().bind(scrollpane.widthProperty())
        root.center = scrollpane

        val scene = Scene(root)
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        val width = (screenSize.width * 0.5).toInt()
        scene.window.width = width.toDouble()
        scene.window.height = screenSize.height
        scene.window.x = screenSize.width - width
        scene.window.y = 0.0
    }

    fun getScriptAsText(): String {
        val scriptContent = StringBuilder()
        themeAndLayoutScript.getScriptRoot().dumpText(scriptContent)

        //scriptContent.split("\n").filter { !it.matches(Regex("^\\s*$")) }.joinToString(separator = "\n")
        //println(scriptContent)
        return "import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent}"
    }

    fun getInputFileFields():List<InputFileKw> {
        val hits = mutableListOf<DSLElementInt>()
        themeAndLayoutScript.getScriptRoot().searchAll(hits, {it is InputFileKw})
        return hits.map { it  as InputFileKw }
    }

}
