package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import com.google.gson.JsonParser
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.model.editor.DSLElement
import io.github.fjossinet.rnartist.model.editor.DSLElementInt
import io.github.fjossinet.rnartist.model.editor.OptionalDSLKeyword
import io.github.fjossinet.rnartist.model.editor.OptionalDSLParameter
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.text.Font
import javafx.stage.*
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
import java.net.URL

class SideWindow(val mediator: Mediator) {

    val stage = Stage()
    val root = BorderPane()
    val tabPane = TabPane()

    init {
        stage.title = "RNArtist Tools"
        createScene(stage)
    }

    private fun createScene(stage: Stage) {
        val toolbar = ToolBar()
        toolbar.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val loadScriptPane = GridPane()
        loadScriptPane.vgap = 5.0
        loadScriptPane.hgap = 5.0

        var l = Label("Open")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
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
            RNArtistTaskWindow(mediator).task = LoadScript(
                mediator,
                script = FileReader(File(mediator.rnartist.getInstallDir(), "/samples/scripts/from_rfam.kts"))
            )
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
                RNArtistTaskWindow(mediator).task = LoadScript(mediator, script = FileReader(file), true)
            }
        }

        val openProject = MenuItem("Open Project..")
        openProject.onAction = EventHandler {
            mediator.projectsPanel.stage.show()
            mediator.projectsPanel.stage.toFront()
            mediator.projectsPanel.loadProjects()
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

        loadScript.items.addAll(newScript, openProject, openFile, openGist)

        GridPane.setConstraints(loadScript, 0, 1)
        GridPane.setHalignment(loadScript, HPos.CENTER)
        loadScriptPane.children.add(loadScript)

        val exportScriptPane = GridPane()
        exportScriptPane.vgap = 5.0
        exportScriptPane.hgap = 5.0

        l = Label("Save/Export")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
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

        GridPane.setHalignment(saveScript, HPos.CENTER)
        GridPane.setConstraints(saveScript, 0, 1)
        exportScriptPane.children.add(saveScript)

        val fontPane = GridPane()
        fontPane.vgap = 5.0
        fontPane.hgap = 5.0

        l = Label("Script Font")
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

        GridPane.setConstraints(sizeFont, 1, 1)
        fontPane.children.add(sizeFont)

        val runPane = GridPane()
        runPane.vgap = 5.0
        runPane.hgap = 5.0

        l = Label("Apply Script")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0, 2, 1)
        runPane.children.add(l)

        val runThemeAndLayout = Button(null, FontIcon("fas-magic:15"))

        runThemeAndLayout.onAction = EventHandler {
            Platform.runLater {
                RNArtistTaskWindow(mediator).task = ApplyThemeAndLayout(mediator)
            }
        }

        GridPane.setConstraints(runThemeAndLayout, 0, 1, 1, 1)
        runPane.children.add(runThemeAndLayout)

        val runEntireScript = Button(null, FontIcon("fas-play:15"))

        runEntireScript.onAction = EventHandler {
            Platform.runLater {
                RNArtistTaskWindow(mediator).task = RunEntireScript(mediator)
            }
        }

        GridPane.setConstraints(runEntireScript, 1, 1, 1, 1)
        runPane.children.add(runEntireScript)

        val s1 = Separator()
        s1.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s2 = Separator()
        s2.padding = Insets(0.0, 5.0, 0.0, 5.0)

        val s3 = Separator()
        s3.padding = Insets(0.0, 5.0, 0.0, 5.0)

        toolbar.items.addAll(loadScriptPane, s1, exportScriptPane, s2, fontPane, s3, runPane)

        /*val submitProject = Button(null, FontIcon("fas-database:15"))
        submitProject.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull()).then(true).otherwise(false))
        submitProject.onMouseClicked = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                mediator.canvas2D.repaint()
                val dialog = TextInputDialog("My Project")
                dialog.initModality(Modality.NONE)
                dialog.title = "Project Saving"
                dialog.headerText =
                    "Keep right mouse button pressed and drag the rectangle to define your project miniature."
                dialog.contentText = "Project name:"
                val projectName = dialog.showAndWait()
                if (projectName.isPresent && !projectName.isEmpty) {
                    try {
                        val pictureFile = createTemporaryFile("test.svg")
                        //ImageIO.write(mediator.canvas2D.screenCapture()!!, "PNG", pictureFile)
                        drawing.asSVG(
                            frame = Rectangle2D.Double(
                                0.0,
                                0.0,
                                mediator.canvas2D.getBounds().width.toDouble(),
                                mediator.canvas2D.getBounds().height.toDouble()
                            ), outputFile = pictureFile
                        )

                        val url = URL("http://localhost:8080/api/submit")
                        val con = url.openConnection()
                        val http = con as HttpURLConnection
                        http.setRequestMethod("POST")
                        http.setDoOutput(true)
                        val boundary = UUID.randomUUID().toString()
                        val boundaryBytes = "--$boundary\r\n".toByteArray(StandardCharsets.UTF_8)
                        val finishBoundaryBytes = "--$boundary--".toByteArray(StandardCharsets.UTF_8)
                        http.setRequestProperty(
                            "Content-Type",
                            "multipart/form-data; charset=UTF-8; boundary=$boundary"
                        )

                        // Enable streaming mode with default settings
                        http.setChunkedStreamingMode(0)

                        // Send our fields:
                        http.outputStream.use { out ->
                            // Send our header (thx Algoman)
                            out.write(boundaryBytes)

                            // Send our first field
                            sendField(out, "script", mediator.scriptEditor.getScriptAsText())

                            // Send a separator
                            out.write(boundaryBytes)

                            // Send our second field
                            sendField(out, "password", "toor")

                            //Send another separator
                            out.write(boundaryBytes)
                            FileInputStream(pictureFile).use { file ->
                                sendFile(
                                    out,
                                    "capture",
                                    file,
                                    pictureFile.name
                                )
                            }

                            // Finish the request
                            out.write(finishBoundaryBytes)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    mediator.canvas2D.repaint()
                }
            }
        }
        submitProject.tooltip = Tooltip("Submit project to RNArtist Gallery")
        GridPane.setConstraints(submitProject, 3, 1)
        saveFiles.children.add(submitProject)*/

        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        tabPane.tabs.add(Tab("Script Editor", this.mediator.scriptEditor))
        tabPane.tabs.last().graphic = FontIcon("fas-align-left:15")
        tabPane.tabs.add(Tab("2Ds loaded", this.mediator.drawingsLoadedPanel))
        tabPane.tabs.last().graphic = FontIcon("fas-th:15")
        tabPane.tabs.add(Tab("Settings", this.mediator.settings))
        tabPane.tabs.last().graphic = FontIcon("fas-wrench:15")

        root.top = toolbar
        root.center = tabPane

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


}