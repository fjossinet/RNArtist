package io.github.fjossinet.rnartist.gui.editor

import com.google.gson.JsonParser
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.io.parseDSLScript
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.model.DrawingLoadedFromScriptEditor
import io.github.fjossinet.rnartist.model.editor.*
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.Desktop
import java.awt.geom.Rectangle2D
import java.io.*
import java.net.URL
import javax.script.ScriptEngineManager

class ScriptEditor(val mediator: Mediator) {

    val editorPane = TextFlow()
    var scriptRoot: RNArtistKw? = null
    val stage = Stage()
    val scrollpane = ScrollPane(editorPane)

    init {
        stage.title = "Script Editor"
        createScene(stage)
    }

    private fun createScene(stage: Stage) {
        val root = BorderPane()
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")
        editorPane.style = "-fx-background-color: ${getHTMLColorString(RnartistConfig.backgroundEditorColor)}"
        editorPane.padding = Insets(10.0, 10.0, 10.0, 10.0)
        editorPane.lineSpacing = 10.0
        editorPane.tabSize = 6

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
        val scriptsLibraryMenu = Menu("Scripts Library")

        val loadFile = MenuItem("Load Script..")
        loadFile.setOnAction {
            val fileChooser = FileChooser()
            fileChooser.initialDirectory = File(mediator.rnartist.getInstallDir(), "samples")
            val file = fileChooser.showOpenDialog(stage)
            file?.let {

                val (root, issues) = parseScript(FileReader(file), this)
                when (root) {
                    is RNArtistKw -> {
                        scriptRoot = root
                        initScript()
                    }
                }
                if (issues.isNotEmpty()) {
                    val alert = Alert(Alert.AlertType.WARNING)
                    alert.headerText = "I fixed issues in your script."
                    alert.contentText = issues.joinToString(separator = "\n")
                    alert.buttonTypes.clear()
                    alert.buttonTypes.add(ButtonType.OK)
                    alert.buttonTypes.add(ButtonType("Go to Documentation", ButtonData.HELP))
                    var result = alert.showAndWait()
                    if (result.isPresent && result.get() != ButtonType.OK) { //show documentation
                        Desktop.getDesktop()
                            .browse(URL("https://github.com/fjossinet/RNArtistCore/blob/master/Changelog.md").toURI())
                    }
                }
            }

        }

        val loadGist = MenuItem("Load Gist..")
        loadGist.setOnAction {
            val gistInput = TextInputDialog()
            gistInput.title = "Enter your Gist ID"
            gistInput.graphic = null
            gistInput.headerText = null
            gistInput.contentText = "Gist ID"
            gistInput.editor.text = "Paste your ID"
            var gistID = gistInput.showAndWait()
            if (gistID.isPresent && !gistID.isEmpty) {
                val jsonAnswer = URL("https://api.github.com/gists/${gistID.get()}").readText()
                val regex = Regex("\"content\":\"(.+?)\"}},\"public\":")
                val match = regex.find(jsonAnswer)
                val scriptContent =
                    "${match?.groupValues?.get(1)}".replace("\\n", "").replace("\\t", "").replace("\\", "")

                val (root, issues) = parseScript(StringReader(scriptContent), this)
                when (root) {
                    is RNArtistKw -> {
                        scriptRoot = root
                        initScript()
                    }
                }
                if (issues.isNotEmpty()) {
                    val alert = Alert(Alert.AlertType.WARNING)
                    alert.headerText = "I fixed issues in your script."
                    alert.contentText = issues.joinToString(separator = "\n")
                    alert.buttonTypes.clear()
                    alert.buttonTypes.add(ButtonType.OK)
                    alert.buttonTypes.add(ButtonType("Go to Documentation", ButtonData.HELP))
                    var result = alert.showAndWait()
                    if (result.isPresent && result.get() != ButtonType.OK) { //show documentation
                        Desktop.getDesktop()
                            .browse(URL("https://github.com/fjossinet/RNArtistCore/blob/master/Changelog.md").toURI())
                    }
                }
            }
        }

        loadScript.getItems().addAll(loadFile, loadGist, scriptsLibraryMenu)

        val load2D = Menu("Load 2D..")

        scriptsLibraryMenu.items.add(load2D)

        var menuItem = MenuItem("...from scratch")
        menuItem.setOnAction {
            val file = File(mediator.rnartist.getInstallDir(), "/samples/scripts/scratch.kts")
            val root = parseScript(FileReader(file), this).first
            when (root) {
                is RNArtistKw -> {
                    scriptRoot = root
                    initScript()
                }
            }
        }

        load2D.items.add(menuItem)

        menuItem = MenuItem("...from scratch with data")
        menuItem.setOnAction {
            val file = File(mediator.rnartist.getInstallDir(), "/samples/scripts/scratch_with_data.kts")
            val root = parseScript(FileReader(file), this).first
            when (root) {
                is RNArtistKw -> {
                    scriptRoot = root
                    initScript()
                }
            }
        }

        load2D.items.add(menuItem)

        val fromLocalFilesMenu = Menu("...from Local Files")
        val fromDatabasesMenu = Menu("...from Databases")
        load2D.items.addAll(fromLocalFilesMenu, fromDatabasesMenu)

        menuItem = MenuItem("Vienna Format")
        menuItem.setOnAction {
            val file = File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_vienna_file.kts")
            val root = parseScript(FileReader(file), this).first
            when (root) {
                is RNArtistKw -> {
                    scriptRoot = root
                    initScript()
                }
            }
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("CT Format")
        menuItem.setOnAction {
            val file = File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_ct_file.kts")
            val root = parseScript(FileReader(file), this).first
            when (root) {
                is RNArtistKw -> {
                    scriptRoot = root
                    initScript()
                }
            }
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("BPSeq Format")
        menuItem.setOnAction {
            val file = File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_bpseq_file.kts")
            val root = parseScript(FileReader(file), this).first
            when (root) {
                is RNArtistKw -> {
                    scriptRoot = root
                    initScript()
                }
            }
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Stockholm Format")
        menuItem.setOnAction {
            val file = File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_stockholm_file.kts")
            val root = parseScript(FileReader(file), this).first
            when (root) {
                is RNArtistKw -> {
                    scriptRoot = root
                    initScript()
                }
            }
        }
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Rfam DB")
        menuItem.setOnAction {
            val file = File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_rfam_entry.kts")
            val root = parseScript(FileReader(file), this).first
            when (root) {
                is RNArtistKw -> {
                    scriptRoot = root
                    initScript()
                }
            }
        }
        fromDatabasesMenu.items.add(menuItem)

        val themes = Menu("Create Theme..")
        scriptsLibraryMenu.items.add(themes)

        val layout = Menu("Create Layout..")
        scriptsLibraryMenu.items.add(layout)

        GridPane.setConstraints(loadScript, 0, 1)
        loadScriptPane.children.add(loadScript)

        val saveScriptPane = GridPane()
        saveScriptPane.vgap = 5.0
        saveScriptPane.hgap = 5.0

        l = Label("Save")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        saveScriptPane.children.add(l)

        val saveScript = MenuButton(null, FontIcon("fas-sign-out-alt:15"))

        val saveAsFile = MenuItem("Save as Local File..")
        saveAsFile.setOnAction(EventHandler<ActionEvent?> {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("RNArtist Scripts", "*.kts"))
            val file = fileChooser.showSaveDialog(stage)
            if (file != null) {
                if (file.name.endsWith(".kts")) {
                    fileChooser.initialDirectory = file.parentFile
                    val writer: PrintWriter
                    try {
                        writer = PrintWriter(file)
                        var scriptAsText =
                            (editorPane.children.filterIsInstance<Text>().map { it.text }).joinToString(separator = "")
                        scriptAsText = scriptAsText.split("\n").filter { !it.matches(Regex("^\\s*$")) }
                            .joinToString(separator = "\n")
                        writer.println("import io.github.fjossinet.rnartist.core.*\n\n ${scriptAsText}")
                        writer.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

        })

        val saveAsGist = MenuItem("Publish as GitHub Gist..")
        saveAsGist.setOnAction(EventHandler<ActionEvent?> {
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
                        val token = ""
                        val client = OkHttpClient()
                        val body = """{"description":"${
                            description.get().trim()
                        }", "files":{"rnartist.kts":{"content":"${
                            getScriptAsText().replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")
                        }"},
                        "rnartist.svg":{"content":"${drawing.asSVG(Rectangle2D.Double(0.0, 0.0, 800.0, 800.0)).replace("\"", "\\\"")}"}}, "public":true}"""
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
                                    JsonParser.parseString(response.body?.charStream()?.readText()).getAsJsonObject()
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
        })

        saveScript.getItems().addAll(saveAsFile, saveAsGist)

        GridPane.setConstraints(saveScript, 0, 1)
        saveScriptPane.children.add(saveScript)

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
            val hits = mutableListOf<DSLElement>()
            scriptRoot?.searchAll(hits) { it is DSLElement }
            hits.forEach {
                it.fontName = fontChooser.value
            }
            initScript()
        }

        fontPane.children.add(fontChooser)

        val sizeFont = Spinner<Int>(5, 40, RnartistConfig.editorFontSize)
        sizeFont.isEditable = true
        sizeFont.prefWidth = 75.0
        sizeFont.onMouseClicked = EventHandler {
            RnartistConfig.editorFontSize = sizeFont.value
            val hits = mutableListOf<DSLElement>()
            scriptRoot?.searchAll(hits) { it is DSLElement }
            hits.forEach {
                it.fontSize = sizeFont.value
            }
            initScript()
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

        val run = Button(null, FontIcon("fas-play:15"))
        run.onAction = EventHandler {
            Platform.runLater {
                try {
                    when (val result = engine.eval(getScriptAsText())) {
                        is List<*> -> {
                            //first we remove the drawings loaded with the same script id (if any)
                            mediator.drawingsLoaded.removeIf { it is DrawingLoadedFromScriptEditor && it.id.equals(scriptRoot!!.id) }
                            //then we add the new ones
                            (result as? List<SecondaryStructureDrawing>)?.forEach {
                                mediator.drawingsLoaded.add(
                                    DrawingLoadedFromScriptEditor(
                                        mediator,
                                        it, scriptRoot!!.id
                                    )
                                )
                                mediator.drawingDisplayed.set(mediator.drawingsLoaded[mediator.drawingsLoaded.size - 1])
                                mediator.canvas2D.fitStructure(null)
                            }

                        }

                        is AdvancedTheme -> {
                            mediator.explorer.applyAdvancedTheme(
                                mediator.explorer.treeTableView.root,
                                result,
                                RNArtist.SCOPE.BRANCH
                            )
                            mediator.explorer.refresh()
                            mediator.canvas2D.repaint()
                        }

                        is Layout -> {
                            mediator.drawingDisplayed.get()?.let {
                                it.drawing.applyLayout(result)
                                mediator.canvas2D.repaint()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            };
        }

        GridPane.setConstraints(run, 0, 1)
        runPane.children.add(run)

        val s1 = Separator()
        s1.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s2 = Separator()
        s2.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s3 = Separator()
        s3.padding = Insets(0.0, 5.0, 0.0, 5.0)

        topToolbar.items.addAll(loadScriptPane, s1, saveScriptPane, s2, fontPane, s3, runPane)

        val leftToolbar = VBox()
        leftToolbar.alignment = Pos.TOP_CENTER
        leftToolbar.spacing = 5.0
        leftToolbar.padding = Insets(10.0, 5.0, 10.0, 5.0)

        val decreaseTab = Button(null, FontIcon("fas-outdent:15"))
        decreaseTab.onAction = EventHandler {
            if (editorPane.tabSize > 1) {
                editorPane.tabSize--
            }
            editorPane.layout()
        }

        val increaseTab = Button(null, FontIcon("fas-indent:15"))
        increaseTab.onAction = EventHandler {
            editorPane.tabSize++
            editorPane.layout()
        }

        val decreaseLineSpacing = Button(null, FontIcon("fas-compress-alt:15"))
        decreaseLineSpacing.onAction = EventHandler {
            editorPane.lineSpacing--
            editorPane.layout()
        }

        val increaseLineSpacing = Button(null, FontIcon("fas-expand-alt:15"))
        increaseLineSpacing.onAction = EventHandler {
            editorPane.lineSpacing++
            editorPane.layout()
        }

        val bgColor = ColorPicker()
        bgColor.value = awtColorToJavaFX(RnartistConfig.backgroundEditorColor)
        bgColor.styleClass.add("button")
        bgColor.style = "-fx-color-label-visible: false ;"
        bgColor.onAction = EventHandler {
            editorPane.style = "-fx-background-color: ${getHTMLColorString(javaFXToAwt(bgColor.value))}"
            RnartistConfig.backgroundEditorColor = javaFXToAwt(bgColor.value)
        }

        val kwColor = ColorPicker()
        kwColor.value = awtColorToJavaFX(RnartistConfig.keywordEditorColor)
        kwColor.styleClass.add("button")
        kwColor.style = "-fx-color-label-visible: false ;"
        kwColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElement>()
            scriptRoot?.searchAll(hits) { it is DSLKeyword }
            hits.forEach {
                it.color = kwColor.value
            }
            RnartistConfig.keywordEditorColor = javaFXToAwt(kwColor.value)
            initScript()
        }

        val bracesColor = ColorPicker()
        bracesColor.value = awtColorToJavaFX(RnartistConfig.bracesEditorColor)
        bracesColor.styleClass.add("button")
        bracesColor.style = "-fx-color-label-visible: false ;"
        bracesColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElement>()
            scriptRoot?.searchAll(hits) { it is OpenedCurly || it is ClosedCurly }
            hits.forEach {
                it.color = bracesColor.value
            }
            RnartistConfig.bracesEditorColor = javaFXToAwt(bracesColor.value)
            initScript()
        }

        val keyParamColor = ColorPicker()
        keyParamColor.value = awtColorToJavaFX(RnartistConfig.keyParamEditorColor)
        keyParamColor.styleClass.add("button")
        keyParamColor.style = "-fx-color-label-visible: false ;"
        keyParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElement>()
            scriptRoot?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).key.color = keyParamColor.value
            }
            RnartistConfig.keyParamEditorColor = javaFXToAwt(keyParamColor.value)
            initScript()
        }

        val operatorParamColor = ColorPicker()
        operatorParamColor.value = awtColorToJavaFX(RnartistConfig.operatorParamEditorColor)
        operatorParamColor.styleClass.add("button")
        operatorParamColor.style = "-fx-color-label-visible: false ;"
        operatorParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElement>()
            scriptRoot?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).operator.color = operatorParamColor.value
            }
            RnartistConfig.operatorParamEditorColor = javaFXToAwt(operatorParamColor.value)
            initScript()
        }

        val valueParamColor = ColorPicker()
        valueParamColor.value = awtColorToJavaFX(RnartistConfig.valueParamEditorColor)
        valueParamColor.styleClass.add("button")
        valueParamColor.style = "-fx-color-label-visible: false ;"
        valueParamColor.onAction = EventHandler {
            val hits = mutableListOf<DSLElement>()
            scriptRoot?.searchAll(hits) { it is DSLParameter }
            hits.forEach {
                (it as DSLParameter).value.color = valueParamColor.value
            }
            RnartistConfig.valueParamEditorColor = javaFXToAwt(valueParamColor.value)
            initScript()
        }

        val spacer = Region()
        spacer.prefHeight = 20.0
        leftToolbar.children.addAll(
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

        scrollpane.setFitToHeight(true);
        scrollpane.setFitToWidth(true);

        root.top = topToolbar
        root.left = leftToolbar
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

    fun initScript() {
        scriptRoot?.let {
            editorPane.children.clear()
            var nodes = mutableListOf<Node>()
            it.dumpNodes(nodes)
            editorPane.children.addAll(nodes)
        }
    }

    fun setJunctionLayout(outIds: String, type: String, junctionLocation: Location) {
        scriptRoot?.getLayoutKw()?.let { layoutKw ->
            if (!layoutKw.inFinalScript)
                layoutKw.addButton.fire()
            val junctionKw =
                layoutKw.searchFirst { it is JunctionKw && it.getLocation()?.start == junctionLocation.start } as JunctionKw?
            //We have found a junctionKw with the same location, we update it
            junctionKw?.let {
                it.setOutIds(outIds) //We just need to change the outIds (type and location should be the same)
            } ?: run { //we create a new one
                val junctionKw = layoutKw.searchFirst { it is JunctionKw && !it.inFinalScript } as JunctionKw
                junctionKw.addButton.fire()
                junctionKw.setLocation(junctionLocation)
                junctionKw.setOutIds(outIds)
                junctionKw.setType(type)
            }
        }
    }

    fun setDetailsLevel(level: String) {
        scriptRoot?.getThemeKw()?.let { themeKw ->
            if (!themeKw.inFinalScript)
                themeKw.addButton.fire()
            val selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelectedPositions().toIntArray())
            val toUpdates = mutableListOf<DSLElement>()
            themeKw.searchAll(toUpdates) { it is DetailsKw && (if (selection != null) selection.start == it.getLocation()?.start else true) }
            //If we found at least one DetailsKw with the same location if any, we update it
            if (toUpdates.isNotEmpty()) {
                with(toUpdates.first()) {
                    (this as DetailsKw).setlevel(level)
                    selection?.let { s ->
                        setLocation(s)
                    } ?: run {
                        removeLocation()
                    }
                }

                if (toUpdates.size > 1) {
                    toUpdates.subList(1, toUpdates.size).forEach {
                        (it as DetailsKw).removeButton.fire()
                    }
                } else {

                }

            } else { //nothing found we add a new DetailsKw element
                val detailsKw = themeKw.searchFirst { it is DetailsKw && !it.inFinalScript } as DetailsKw
                detailsKw.addButton.fire()
                detailsKw.setlevel(level)
                selection?.let { s ->
                    detailsKw.setLocation(s)
                }
            }
        }
    }

    fun setColor(types: String, color: String) {
        scriptRoot?.getThemeKw()?.let { themeKw ->
            if (!themeKw.inFinalScript)
                themeKw.addButton.fire()
            val selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelectedPositions().toIntArray())
            val toUpdates = mutableListOf<DSLElement>()
            themeKw.searchAll(toUpdates) { it is ColorKw && types.equals(it.getTypes()) && (if (selection != null) selection.start == it.getLocation()?.start else true) }
            //If we found at least one colorKW with the same types (and location if any), we update it
            if (toUpdates.isNotEmpty()) {

                with(toUpdates.first()) {
                    (this as ColorKw).setColor(color)
                    setTypes(types)
                    selection?.let { s ->
                        setLocation(s)
                    } ?: run {
                        removeLocation()
                    }
                }

                if (toUpdates.size > 1) {
                    toUpdates.subList(1, toUpdates.size).forEach {
                        (it as ColorKw).removeButton.fire()
                    }
                } else {

                }

            } else { //nothing found we add a new ColorKW element
                val colorKw = themeKw.searchFirst { it is ColorKw && !it.inFinalScript } as ColorKw
                colorKw.addButton.fire()
                colorKw.setColor(color)
                colorKw.setTypes(types)
                selection?.let { s ->
                    colorKw.setLocation(s)
                }
            }

        }
    }

    fun setLineWidth(types: String, width: String) {
        scriptRoot?.getThemeKw()?.let { themeKw ->
            if (!themeKw.inFinalScript)
                themeKw.addButton.fire()
            val selection = if (mediator.canvas2D.getSelectedPositions()
                    .isEmpty()
            ) null else Location(mediator.canvas2D.getSelectedPositions().toIntArray())
            val toUpdates = mutableListOf<DSLElement>()
            themeKw.searchAll(toUpdates) { it is LineKw && types.equals(it.getTypes()) && (if (selection != null) selection.start == it.getLocation()?.start else true) }
            //If we found at least one lineKW with the same types (and location if any), we update it
            if (toUpdates.isNotEmpty()) {

                with(toUpdates.first()) {
                    (this as LineKw).setWidth(width)
                    setTypes(types)
                    selection?.let { s ->
                        setLocation(s)
                    } ?: run {
                        removeLocation()
                    }
                }

                if (toUpdates.size > 1) {
                    toUpdates.subList(1, toUpdates.size).forEach {
                        (it as LineKw).removeButton.fire()
                    }
                } else {

                }

            } else { //nothing found we add a new LineKW element
                val lineKw = themeKw.searchFirst { it is LineKw && !it.inFinalScript } as LineKw
                lineKw.addButton.fire()
                lineKw.setWidth(width)
                lineKw.setTypes(types)
                selection?.let { s ->
                    lineKw.setLocation(s)
                }
            }

        }
    }

    fun getScriptAsText(): String {
        var scriptAsText = (editorPane.children.filterIsInstance<Text>().map {
            it.text
        }).joinToString(separator = "")
        scriptAsText = scriptAsText.split("\n").filter { !it.matches(Regex("^\\s*$")) }.joinToString(separator = "\n")
        return "import io.github.fjossinet.rnartist.core.*\n\n ${scriptAsText}"
    }

    fun keywordAddedToScript(element: OptionalDSLKeyword) {
        var addButton: Button? = null
        for (child in editorPane.children) {
            if (element.addButton == child) {
                addButton = element.addButton
            }
        }
        addButton?.let {
            val index = editorPane.children.indexOf(addButton)
            editorPane.children.removeAt(index) //the button
            editorPane.children.removeAt(index) //its new line
            var nodes = mutableListOf<Node>()
            element.dumpNodes(nodes, withTabs = false)
            editorPane.children.addAll(index, nodes)
        }
    }

    fun parameterAddedToScript(parameter: OptionalDSLParameter) {
        var addButton: Button? = null
        for (child in editorPane.children) {
            if (parameter.addButton == child) {
                addButton = parameter.addButton
            }
        }
        addButton?.let {
            val index = editorPane.children.indexOf(addButton)
            editorPane.children.removeAt(index) //the button
            editorPane.children.removeAt(index) //its new line
            var nodes = mutableListOf<Node>()
            parameter.dumpNodes(nodes, withTabs = false)
            editorPane.children.addAll(index, nodes)
        }
    }

    fun keywordRemovedFromScript(parameter: OptionalDSLKeyword, nodesToRemove: Int) {
        var removeButton: Button? = null
        for (child in editorPane.children) {
            if (parameter.removeButton == child) {
                removeButton = parameter.removeButton
            }
        }
        removeButton?.let {
            val index = editorPane.children.indexOf(removeButton)
            (1..nodesToRemove).forEach {
                editorPane.children.removeAt(index)
            }
            var nodes = mutableListOf<Node>()
            parameter.dumpNodes(nodes, withTabs = false)
            editorPane.children.addAll(index, nodes)
        }
    }

    fun parameterRemovedFromScript(parameter: OptionalDSLParameter, nodesToRemove: Int) {
        var removeButton: Button? = null
        for (child in editorPane.children) {
            if (parameter.removeButton == child) {
                removeButton = parameter.removeButton
            }
        }
        removeButton?.let {
            val index = editorPane.children.indexOf(removeButton)
            (1..nodesToRemove).forEach {
                editorPane.children.removeAt(index)
            }
            var nodes = mutableListOf<Node>()
            parameter.dumpNodes(nodes, withTabs = false)
            editorPane.children.addAll(index, nodes)
        }
    }

    @Throws(java.lang.Exception::class)
    fun parseScript(reader: Reader, editor: ScriptEditor): Pair<DSLElement, List<String>> {
        var (elements, issues) = parseDSLScript(reader)

        val root = RNArtistKw(editor)

        elements.first().children.forEach { element ->
            println(element.name)
            when (element.name) {
                "ss" -> {
                    val secondaryStructureKw = root.searchFirst { it is SecondaryStructureKw } as SecondaryStructureKw
                    element.children.forEach { elementChild ->
                        when (elementChild.name) {
                            "bn" -> {
                                val bnKw =
                                    (secondaryStructureKw.searchFirst { themeChild -> themeChild is BracketNotationKw && !themeChild.inFinalScript } as BracketNotationKw)
                                bnKw.addToFinalScript(true)
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
                                        parameter.addToFinalScript(true)
                                    }
                                }
                            }

                            "vienna" -> {
                                val viennaKw =
                                    (secondaryStructureKw.searchFirst { themeChild -> themeChild is ViennaKw && !themeChild.inFinalScript } as ViennaKw)
                                viennaKw.addToFinalScript(true)
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
                                val bpseqKw =
                                    (secondaryStructureKw.searchFirst { themeChild -> themeChild is BpseqKw && !themeChild.inFinalScript } as BpseqKw)
                                bpseqKw.addToFinalScript(true)
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
                                val ctKw =
                                    (secondaryStructureKw.searchFirst { themeChild -> themeChild is CtKw && !themeChild.inFinalScript } as CtKw)
                                ctKw.addToFinalScript(true)
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
                                val stockholmKw =
                                    (secondaryStructureKw.searchFirst { themeChild -> themeChild is StockholmKw && !themeChild.inFinalScript } as StockholmKw)
                                stockholmKw.addToFinalScript(true)
                                elementChild.attributes.forEach { attribute ->
                                    val tokens = attribute.split("=")
                                    if ("file".equals(tokens.first().trim())) {
                                        val parameter =
                                            (stockholmKw.searchFirst { it is DSLParameter && "file".equals(it.key.text.text) } as DSLParameter)
                                        parameter.value.text.text = tokens.last().trim()
                                    }
                                }
                            }

                            "rfam" -> {
                                val stockholmKw =
                                    (secondaryStructureKw.searchFirst { themeChild -> themeChild is RfamKw && !themeChild.inFinalScript } as RfamKw)
                                stockholmKw.addToFinalScript(true)
                                elementChild.attributes.forEach { attribute ->
                                    val tokens = attribute.split("=")
                                    if ("id".equals(tokens.first().trim())) {
                                        val parameter =
                                            (stockholmKw.searchFirst { it is DSLParameter && "id".equals(it.key.text.text) } as DSLParameter)
                                        parameter.value.text.text = tokens.last().trim()
                                    }
                                    if ("name".equals(tokens.first().trim())) {
                                        val parameter =
                                            (stockholmKw.searchFirst { it is OptionalDSLParameter && "name".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.value.text.text = tokens.last().trim()
                                    }
                                }
                            }
                        }
                    }
                }
                "theme" -> {
                    val themeKw = root.searchFirst { it is ThemeKw } as ThemeKw
                    themeKw.addToFinalScript(true)
                    element.children.forEach { elementChild ->
                        when (elementChild.name) {
                            "details" -> {
                                val detailsLevelKw =
                                    themeKw.searchFirst { themeChild -> themeChild is DetailsKw && !themeChild.inFinalScript } as DetailsKw
                                detailsLevelKw.addToFinalScript(true)
                                elementChild.attributes.forEach { attribute ->
                                    var tokens = attribute.split("=")
                                    if ("value".equals(tokens.first().trim())) {
                                        val parameter =
                                            (detailsLevelKw.searchFirst { it is DSLParameter && "value".equals(it.key.text.text) } as DSLParameter)
                                        parameter.value.text.text = tokens.last().trim()
                                    }
                                    if ("type".equals(tokens.first().trim())) {
                                        val parameter =
                                            (detailsLevelKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.value.text.text = tokens.last().trim()
                                    }
                                }
                                elementChild.children.forEach { elementChildChild ->
                                    when (elementChildChild.name) {
                                        "location" -> {
                                            val locationKw =
                                                detailsLevelKw.searchFirst { colorChild -> colorChild is LocationKw && !colorChild.inFinalScript } as LocationKw
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
                            "color" -> {
                                val colorKw =
                                    themeKw.searchFirst { themeChild -> themeChild is ColorKw && !themeChild.inFinalScript } as ColorKw
                                colorKw.addToFinalScript(true)
                                elementChild.attributes.forEach { attribute ->
                                    var tokens = attribute.split("=")
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
                                    if ("to".equals(tokens.first().trim())) {
                                        val parameter =
                                            (colorKw.searchFirst { it is OptionalDSLParameter && "to".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.value.text.text = tokens.last().trim()
                                        parameter.value.text.fill = Color.web(tokens.last().trim().replace("\"", ""))
                                    }
                                    if (attribute.trim().startsWith("data")) {
                                        tokens = attribute.trim().split(" ")
                                        val parameter =
                                            (colorKw.searchFirst { it is OptionalDSLParameter && "data".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.key.text.text = tokens.first().trim()
                                        parameter.operator.text.text = " ${tokens[1].trim()} "
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
                                    var tokens = attribute.split("=")
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
                                    if (attribute.startsWith("data")) {
                                        tokens = attribute.split(" ")
                                        val parameter =
                                            (lineKw.searchFirst { it is OptionalDSLParameter && "data".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.key.text.text = tokens.first().trim()
                                        parameter.operator.text.text = " ${tokens[1].trim()} "
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
                                    var tokens = attribute.split("=")
                                    if ("type".equals(tokens.first().trim())) {
                                        val parameter =
                                            (showKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.value.text.text = tokens.last().trim()
                                    }
                                    if (attribute.startsWith("data")) {
                                        tokens = attribute.split(" ")
                                        val parameter =
                                            (showKw.searchFirst { it is OptionalDSLParameter && "data".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.key.text.text = tokens.first().trim()
                                        parameter.operator.text.text = " ${tokens[1].trim()} "
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
                                    var tokens = attribute.split("=")
                                    if ("type".equals(tokens.first().trim())) {
                                        val parameter =
                                            (hideKw.searchFirst { it is OptionalDSLParameter && "type".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.value.text.text = tokens.last().trim()
                                    }
                                    if (attribute.startsWith("data")) {
                                        tokens = attribute.split(" ")
                                        val parameter =
                                            (hideKw.searchFirst { it is OptionalDSLParameter && "data".equals(it.key.text.text) } as OptionalDSLParameter)
                                        parameter.addToFinalScript(true)
                                        parameter.key.text.text = tokens.first().trim()
                                        parameter.operator.text.text = " ${tokens[1].trim()} "
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
                    val layoutKw = root.searchFirst { it is LayoutKw } as LayoutKw
                    layoutKw.addToFinalScript(true)
                    element.children.forEach { elementChild ->
                        when (elementChild.name) {
                            "junction" -> {
                                val junctionKw =
                                    layoutKw.searchFirst { layoutChild -> layoutChild is JunctionKw && !layoutChild.inFinalScript } as JunctionKw
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

                "data" -> {
                    val dataKw = root.searchFirst { it is DataKw } as DataKw
                    dataKw.addToFinalScript(true)
                    element.attributes.forEach { attribute ->
                        val tokens = attribute.split(" ")
                        val parameter =
                            (dataKw.searchFirst { it is OptionalDSLParameter && !it.inFinalScript } as OptionalDSLParameter)
                        parameter.addToFinalScript(true)
                        parameter.key.text.text = tokens.first().trim()
                        parameter.operator.text.text = " ${tokens[1].trim()} "
                        parameter.value.text.text = tokens.last().trim()
                    }
                }
            }
        }

        return Pair(root, issues)
    }

    fun pasteDrawing(drawing: SecondaryStructureDrawing) {

        val script = """rnartist {
    ss {
        rna {
            sequence =      "${drawing.secondaryStructure.rna.seq}"
        }
        
        bracket_notation =  "${drawing.secondaryStructure.toBracketNotation()}"
        
        layout {
            ${
            drawing.allJunctions.map({ junction ->
                if (junction.junctionType != JunctionType.ApicalLoop) {
                    """
            junction {
               name ="${junction.name}"
               out_ids = "${junction.currentLayout.joinToString(separator = " ")}"
            }    
            """
                } else
                    """"""
            }).joinToString(separator = "\n").trim()
        }
        }
    }
}""".trimIndent()

    }
}