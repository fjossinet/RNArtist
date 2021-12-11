package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor.*
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.model.DrawingLoadedFromEditor
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import javafx.stage.Screen
import javafx.stage.Stage
import org.kordamp.ikonli.javafx.FontIcon
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import javax.script.ScriptEngineManager

class ScriptEditor(val mediator: Mediator) {

    var selectedParameter: DSLElement? = null
    val editorPane = TextFlow()
    var scriptRoot:RNArtistKw? = null
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
        val samplesMenu = Menu("Samples")

        var menuItem = MenuItem("Load Script")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            val fileChooser = FileChooser()
            val file = fileChooser.showOpenDialog(stage)
            file?.let {
                //convert script to editor model
            }

        })

        loadScript.getItems().addAll(menuItem, samplesMenu)

        menuItem = MenuItem("Load 2D from scratch")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            scriptRoot = RNArtistKw(this,0)
            val bnKeyWord = (scriptRoot as DSLElement).searchFirst { it is BracketNotationKw } as BracketNotationKw
            bnKeyWord.addToFinalScript(true)
            ((scriptRoot as DSLElement).searchFirst { it is ThemeKw } as ThemeKw).addToFinalScript(true)
            (bnKeyWord.searchFirst {it is OptionalDSLParameter && it.key.text.text.equals("seq")} as? OptionalDSLParameter)?.let { parameter ->
                parameter.addToFinalScript(true)
                parameter.value.text.text = "\"AUGAUCAAGGAAUUGAUGU\""
            }
            (bnKeyWord.searchFirst {it is DSLParameter && it.key.text.text.equals("value")} as? DSLParameter)?.let { parameter ->
                parameter.value.text.text = "\"(((..(((....))).)))\""
            }
            initScript()
        })

        samplesMenu.items.add(menuItem)

        val fromLocalFilesMenu = Menu("Load 2D from Local Files")
        val fromDatabasesMenu = Menu("Load 2D from Databases")
        samplesMenu.items.addAll(fromLocalFilesMenu, fromDatabasesMenu)

        menuItem = MenuItem("Vienna Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            scriptRoot = RNArtistKw(this,0)
            ((scriptRoot as DSLElement).searchFirst { it is ViennaKw } as ViennaKw).addToFinalScript(true)
            ((scriptRoot as DSLElement).searchFirst { it is ThemeKw } as ThemeKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("CT Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            scriptRoot = RNArtistKw(this,0)
            ((scriptRoot as DSLElement).searchFirst { it is CtKw } as CtKw).addToFinalScript(true)
            ((scriptRoot as DSLElement).searchFirst { it is ThemeKw } as ThemeKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("BPSeq Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            scriptRoot = RNArtistKw(this,0)
            ((scriptRoot as DSLElement).searchFirst { it is BpseqKw } as BpseqKw).addToFinalScript(true)
            ((scriptRoot as DSLElement).searchFirst { it is ThemeKw } as ThemeKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Stockholm Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            scriptRoot = RNArtistKw(this,0)
            ((scriptRoot as DSLElement).searchFirst { it is StockholmKw } as StockholmKw).addToFinalScript(true)
            ((scriptRoot as DSLElement).searchFirst { it is ThemeKw } as ThemeKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Rfam DB")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            scriptRoot = RNArtistKw(this,0)
            val rfamkw = (scriptRoot as RNArtistKw).searchFirst { it is RfamKw }
            (rfamkw as RfamKw).addToFinalScript(true)
            ((scriptRoot as DSLElement).searchFirst { it is ThemeKw } as ThemeKw).addToFinalScript(true)
            initScript()
        })
        fromDatabasesMenu.items.add(menuItem)

        GridPane.setConstraints(loadScript, 0, 1)
        loadScriptPane.children.add(loadScript)

        val saveScriptPane = GridPane()
        saveScriptPane.vgap = 5.0
        saveScriptPane.hgap = 5.0

        l = Label("Save")
        GridPane.setHalignment(l, HPos.CENTER)
        GridPane.setConstraints(l, 0, 0)
        saveScriptPane.children.add(l)

        val saveScript = Button(null, FontIcon("fas-sign-out-alt:15"))
        saveScript.onMouseClicked = EventHandler {
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
        }

        GridPane.setConstraints(saveScript, 0, 1)
        saveScriptPane.children.add(saveScript)

        val decreaseTab = Button(null, FontIcon("fas-outdent:15"))
        decreaseTab.onAction = EventHandler {
            if (editorPane.tabSize > 1) {
                editorPane.tabSize --
            }
            editorPane.layout()
        }

        val increaseTab = Button(null, FontIcon("fas-indent:15"))
        increaseTab.onAction = EventHandler {
            editorPane.tabSize ++
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
                (it as DSLElement).fontName = fontChooser.value
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
                (it as DSLElement).fontSize = sizeFont.value
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
            Platform.runLater(Runnable() {
                try {
                    var scriptAsText = ( editorPane.children.filterIsInstance<Text>().map {
                        it.text
                    }).joinToString(separator = "")
                    scriptAsText = scriptAsText.split("\n").filter { !it.matches(Regex("^\\s*$")) }.joinToString(separator = "\n")
                    println(scriptAsText)

                    val result = engine.eval("import io.github.fjossinet.rnartist.core.*\n\n ${scriptAsText}")
                    when (result) {
                        is List<*> -> {

                            (result as? List<SecondaryStructureDrawing>)?.forEach {
                                mediator.drawingsLoaded.add(
                                    DrawingLoadedFromEditor(mediator,
                                        it, File("script.kts")))
                                mediator.drawingDisplayed.set(mediator.drawingsLoaded[mediator.drawingsLoaded.size - 1])
                                mediator.canvas2D.fitStructure(null)
                            }

                        }

                        is AdvancedTheme -> {
                            mediator.explorer.applyAdvancedTheme(mediator.explorer.treeTableView.root, result, RNArtist.SCOPE.BRANCH)
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
            });
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
        val spacer = Region()
        spacer.prefHeight = 20.0
        leftToolbar.children.addAll(increaseTab, decreaseTab, spacer, Label("Bg"), bgColor, Label("Kw"), kwColor, Label("{ }"), bracesColor, Label("Key"), keyParamColor, Label("Op"), operatorParamColor, Label("Val"), valueParamColor)

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

    fun keywordAddedToScript(element:OptionalDSLKeyword) {
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

    fun parameterAddedToScript(parameter:OptionalDSLParameter) {
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

    fun keywordRemovedFromScript(parameter:OptionalDSLKeyword, nodesToRemove:Int) {
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

    fun parameterRemovedFromScript(parameter:OptionalDSLParameter, nodesToRemove:Int) {
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

    fun pasteDrawing(drawing:SecondaryStructureDrawing) {

        val script = """rnartist {
    ss {
        rna {
            sequence =      "${drawing.secondaryStructure.rna.seq}"
        }
        
        bracket_notation =  "${drawing.secondaryStructure.toBracketNotation()}"
        
        layout {
            ${drawing.allJunctions.map( { junction ->
            if (junction.junctionType != JunctionType.ApicalLoop) {
                """
            junction {
               name ="${junction.name}"
               out_ids = "${junction.currentLayout.joinToString(separator = " ")}"
            }    
            """
            } else
                """"""
        }).joinToString(separator = "\n").trim()}
        }
    }
}""".trimIndent()

    }
}