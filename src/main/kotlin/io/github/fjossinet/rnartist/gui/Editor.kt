package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.model.DrawingLoadedFromEditor
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import org.kordamp.ikonli.javafx.FontIcon
import java.io.File
import javax.script.ScriptEngineManager

class Editor(val mediator: Mediator): VBox() {

    val webView = WebView()
    val webEngine = webView.engine

    init {
        this.spacing = 10.0
        this.padding = Insets(10.0, 10.0, 10.0, 10.0)
        this.webEngine.setJavaScriptEnabled(true);
        this.webView.setContextMenuEnabled(false);
        this.webEngine.getLoadWorker().stateProperty().addListener { observable, oldValue, newValue ->
            if (newValue == Worker.State.SUCCEEDED) {
                val doc = webEngine.getDocument()
                val editor = doc.getElementById("editor")
                webEngine.executeScript("initEditor()")
            }
        }
        this.webEngine.load(Editor::class.java.getResource("/io/github/fjossinet/rnartist/editor/editor.html").toExternalForm())
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")

        val buttons = HBox()
        buttons.alignment = Pos.CENTER_RIGHT
        buttons.spacing = 10.0
        val run = Button(null, FontIcon("fas-play:15"))
        run.onAction = EventHandler {
            Platform.runLater(Runnable() {
                try {
                    val script = webEngine.executeScript("getScript()")
                    val result = engine.eval("import io.github.fjossinet.rnartist.core.*\n\n ${(script as String)}")
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
        val scrollpane = ScrollPane(this.webView)
        scrollpane.setFitToHeight(true);
        scrollpane.setFitToWidth(true);
        this.children.addAll(scrollpane, buttons)
        VBox.setVgrow(scrollpane, Priority.ALWAYS)
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
        val window = this.webView.getEngine().executeScript("window")
        (window as JSObject).call("setScript", script)
    }
}