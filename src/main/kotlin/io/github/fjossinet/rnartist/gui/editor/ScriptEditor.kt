package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.editor

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.editor.*
import io.github.fjossinet.rnartist.model.DrawingLoadedFromEditor
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
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
    var rnartistElement:RNArtistKw? = null
    val stage = Stage()
    val scrollpane = ScrollPane(editorPane)

    init {
        stage.title = "Script Editor"
        createScene(stage)
    }

    private fun createScene(stage: Stage) {
        val root = VBox()
        root.spacing = 10.0
        root.padding = Insets(10.0, 10.0, 10.0, 10.0)
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")
        editorPane.style = "-fx-background-color: black"
        editorPane.padding = Insets(10.0, 10.0, 10.0, 10.0)
        editorPane.lineSpacing = 15.0
        editorPane.tabSize = 5

        val buttons = HBox()
        buttons.alignment = Pos.CENTER_LEFT
        buttons.spacing = 10.0

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
            rnartistElement = RNArtistKw(this,0)
            ((rnartistElement as DSLElement).search { it is BracketNotationKw } as BracketNotationKw).addToFinalScript(true)
            initScript()
        })

        samplesMenu.items.add(menuItem)

        val fromLocalFilesMenu = Menu("Load 2D from Local Files")
        val fromDatabasesMenu = Menu("Load 2D from Databases")
        samplesMenu.items.addAll(fromLocalFilesMenu, fromDatabasesMenu)

        menuItem = MenuItem("Vienna Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            rnartistElement = RNArtistKw(this,0)
            ((rnartistElement as DSLElement).search { it is ViennaKw } as ViennaKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("CT Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            rnartistElement = RNArtistKw(this,0)
            ((rnartistElement as DSLElement).search { it is CtKw } as CtKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("BPSeq Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            rnartistElement = RNArtistKw(this,0)
            ((rnartistElement as DSLElement).search { it is BpseqKw } as BpseqKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Stockholm Format")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            rnartistElement = RNArtistKw(this,0)
            ((rnartistElement as DSLElement).search { it is StockholmKw } as StockholmKw).addToFinalScript(true)
            initScript()
        })
        fromLocalFilesMenu.items.add(menuItem)

        menuItem = MenuItem("Rfam DB")
        menuItem.setOnAction(EventHandler<ActionEvent?> {
            rnartistElement = RNArtistKw(this,0)
            val rfamkw = (rnartistElement as RNArtistKw).search { it is RfamKw }
            (rfamkw as RfamKw).addToFinalScript(true)
            initScript()
        })
        fromDatabasesMenu.items.add(menuItem)

        buttons.children.add(loadScript)

        val saveScript = Button(null, FontIcon("fas-sign-out-alt:15"))
        buttons.children.add(saveScript)
        saveScript.onMouseClicked = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("SVG Files", "*.kts"))
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

        val decreaseTab = Button(null, FontIcon("fas-outdent:15"))
        decreaseTab.onAction = EventHandler {
            if (editorPane.tabSize > 1) {
                editorPane.tabSize --
            }
            editorPane.layout()
        }
        buttons.children.add(decreaseTab)

        val increaseTab = Button(null, FontIcon("fas-indent:15"))
        increaseTab.onAction = EventHandler {
            editorPane.tabSize ++
            editorPane.layout()
        }
        buttons.children.add(increaseTab)

        val run = Button(null, FontIcon("fas-play:15"))
        run.onAction = EventHandler {
            Platform.runLater(Runnable() {
                try {
                    var scriptAsText = ( editorPane.children.filterIsInstance<Text>().map { it.text }).joinToString(separator = "")
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
        buttons.children.add(run)
        scrollpane.setFitToHeight(true);
        scrollpane.setFitToWidth(true);

        root.children.addAll(buttons, scrollpane)
        VBox.setVgrow(scrollpane, Priority.ALWAYS)
        val scene = Scene(root)
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        val width = (screenSize.width * 4.0 / 10.0).toInt()
        scene.window.width = width.toDouble()
        scene.window.height = screenSize.height
        scene.window.x = screenSize.width - width
        scene.window.y = 0.0
    }

    fun initScript() {
        rnartistElement?.let {
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
            println(nodesToRemove)
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