package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.io.parseDSLScript
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistTask
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import org.kordamp.ikonli.javafx.FontIcon
import java.io.File
import java.io.FileReader
import javax.script.ScriptEngineManager

class Full2DButtonsPanel(mediator:Mediator): LinearButtonsPanel(mediator = mediator) {

    init {
        val center2D = Button(null, FontIcon("fas-crosshairs:15"))
        center2D.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            center2D.isDisable = newValue == null
        }
        center2D.onMouseClicked = EventHandler { mouseEvent ->
            mediator.currentDrawing.get()?.drawing?.let {
                mediator.canvas2D.centerDisplayOn(it.getFrame())
            }
        }
        center2D.tooltip = Tooltip("Center View on 2D")
        this.addButton(center2D)

        val fit2D = Button(null, FontIcon("fas-expand-arrows-alt:15"))
        fit2D.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            fit2D.isDisable = newValue == null
        }
        fit2D.onMouseClicked = EventHandler {
            mediator.canvas2D.fitStructure(null)
        }
        fit2D.tooltip = Tooltip("Fit 2D to View")
        this.addButton(fit2D)

        val saveCurrentDrawing = Button(null, FontIcon("fas-save:15"))
        saveCurrentDrawing.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            saveCurrentDrawing.isDisable = newValue == null
        }
        saveCurrentDrawing.onMouseClicked = EventHandler { _ ->
            class LoadThumbnailsTask(mediator: Mediator) : RNArtistTask(mediator) {
                init {
                    setOnSucceeded { _ ->
                        this.rnartistTaskDialog.stage.hide()
                    }
                }

                override fun call(): Pair<Any?, Exception?> {
                    try {
                        mediator.currentDrawing.get()?.let { currentDrawing ->
                            val manager = ScriptEngineManager()
                            val engine = manager.getEngineByExtension("kts")
                            //we replace the dsl script in the file of the currentDrawing with the dsl script in memory
                            val b = StringBuffer()
                            currentDrawing.rnArtistEl?.dump(buffer = b)
                            File(currentDrawing.dslScriptAbsolutePath).writeText(b.toString())
                            engine.eval(
                                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                    System.getProperty(
                                        "line.separator"
                                    )
                                } ${File(currentDrawing.dslScriptAbsolutePath).readText()}"
                            )
                            mediator.DBExplorer.lastThumbnailCellClicked?.let {
                                Platform.runLater {
                                    it.item.layoutAndThemeUpdated.value = !it.item.layoutAndThemeUpdated.value
                                }
                                Thread.sleep(100)
                            }

                        }
                        return Pair(null, null)
                    } catch (e: Exception) {
                        return Pair(null, e)
                    }
                }

            }

            val w = RNArtistTaskDialog(mediator)
            w.task = LoadThumbnailsTask(mediator)

        }
        this.addButton(saveCurrentDrawing)

        val applyCurrentDrawing = Button(null, FontIcon("fas-th:15"))
        applyCurrentDrawing.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            applyCurrentDrawing.isDisable = newValue == null
        }
        applyCurrentDrawing.onMouseClicked = EventHandler { _ ->
            class LoadThumbnailsTask(mediator: Mediator) : RNArtistTask(mediator) {
                init {
                    setOnSucceeded { _ ->
                        this.rnartistTaskDialog.stage.hide()
                    }
                }

                override fun call(): Pair<Any?, Exception?> {
                    try {
                        mediator.currentDrawing.get()?.let { currentDrawing ->
                            val manager = ScriptEngineManager()
                            val engine = manager.getEngineByExtension("kts")
                            //first the thumbnail of the current drawing
                            //we replace the dsl script in the file of the currentDrawing with the dsl script in memory
                            val b = StringBuffer()
                            currentDrawing.rnArtistEl?.dump(buffer = b)
                            File(currentDrawing.dslScriptAbsolutePath).writeText(b.toString())
                            engine.eval(
                                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                    System.getProperty(
                                        "line.separator"
                                    )
                                } ${File(currentDrawing.dslScriptAbsolutePath).readText()}"
                            )

                            mediator.DBExplorer.lastThumbnailCellClicked?.let {
                                Platform.runLater {
                                    it.item.layoutAndThemeUpdated.value = !it.item.layoutAndThemeUpdated.value
                                }
                                Thread.sleep(100)
                            }

                            //then the other thumbnails
                            mediator.DBExplorer.thumbnails.items.forEach { item ->
                                if (item != mediator.DBExplorer.lastThumbnailCellClicked?.item) {
                                    val rnartistEl = parseDSLScript(FileReader(item.dslScriptAbsolutePath))
                                    rnartistEl?.let { rnartistEl ->
                                        //we replace the theme and layout elements in the dsl script of this item with the ones from the current drawing
                                        rnartistEl.addThemeEl(currentDrawing.rnArtistEl?.getThemeEl())
                                        rnartistEl.addLayoutEl(currentDrawing.rnArtistEl?.getLayoutEl())
                                        val b = StringBuffer()
                                        rnartistEl.dump(buffer = b)
                                        File(item.dslScriptAbsolutePath).writeText(b.toString())
                                        engine.eval(
                                            "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                                System.getProperty(
                                                    "line.separator"
                                                )
                                            } ${File(item.dslScriptAbsolutePath).readText()}"
                                        )
                                        Platform.runLater {
                                            item.layoutAndThemeUpdated.value = !item.layoutAndThemeUpdated.value
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

            val w = RNArtistTaskDialog(mediator)
            w.task = LoadThumbnailsTask(mediator)

        }
        this.addButton(applyCurrentDrawing)
    }

}