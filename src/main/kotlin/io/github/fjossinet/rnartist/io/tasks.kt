package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.RNACentral
import io.github.fjossinet.rnartist.core.model.RNArtistEl
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.core.model.Theme
import io.github.fjossinet.rnartist.model.RNArtistDrawing
import io.github.fjossinet.rnartist.model.RNArtistTask
import javafx.application.Platform
import java.io.File

class AddStructureFromURL(
        mediator: Mediator,
        val url: String,
        val dataDir: String
) : RNArtistTask(mediator) {
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
            mediator.currentDB?.let { rootDB ->
                if (url.startsWith("https://rnacentral.org")) {
                    val tokens = url.split("/")
                    val entryID = tokens[tokens.size - 2]
                    Platform.runLater {
                        updateMessage("Downloading data....")
                    }
                    Thread.sleep(100)
                    RNACentral().fetch(entryID)?.let { ss ->
                        Platform.runLater {
                            updateMessage("Saving data....")
                        }
                        Thread.sleep(100)
                        val scriptFile = rootDB.addAndPlot2D(entryID, File(dataDir), ss)
                        Platform.runLater {
                            updateMessage("Computing 2D....")
                        }
                        Thread.sleep(100)
                        mediator.scriptEngine.eval(scriptFile.readText())

                        Platform.runLater {
                            mediator.rnartist.addThumbnail(File(rootDB.getDrawingsDirForDataDir(File(dataDir)), "${entryID}.png"),
                                scriptFile.invariantSeparatorsPath)
                        }
                        Thread.sleep(200)
                    } ?: run {
                        return Pair(null, RuntimeException("No 2D found in RNACentral entry $entryID"))
                    }
                }
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class CreateDBFolder(mediator: Mediator, val absPathFolder: String) : RNArtistTask(mediator) {
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
            mediator.currentDB?.let { rootDB ->
                Platform.runLater {
                    mediator.rnartist.clearThumbnails()
                }
                Thread.sleep(100)
                rootDB.createNewFolder(absPathFolder)?.let { newFolder ->
                    mediator.rnartist.addFolderToTreeView(newFolder.invariantSeparatorsPath)?.let {
                        mediator.rnartist.expandTreeView(it)
                        mediator.rnartist.selectInTreeView(it)
                    }
                }
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

/**
 * Load a structure and display it in the canvas as described in the script whose absolute path is given as argument
 */
class LoadStructure(mediator: Mediator, val dslScriptInvariantSeparatorsPath:String) : RNArtistTask(mediator) {
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
                    "Loading 2D for ${
                        dslScriptInvariantSeparatorsPath.removeSuffix(".kts").split(
                            System.getProperty(
                                "file.separator"
                            )
                        ).last()
                    }..."
                )
            }
            Thread.sleep(1000)

            val result = mediator.scriptEngine.eval(File(dslScriptInvariantSeparatorsPath).readText()) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>
            result?.let {
                val drawing =
                    RNArtistDrawing(
                        mediator,
                        it.first.first(),
                        dslScriptInvariantSeparatorsPath,
                        it.second
                    )
                mediator.currentDrawing.set(drawing)
                if (drawing.secondaryStructureDrawing.viewX == 0.0 && drawing.secondaryStructureDrawing.viewY == 0.0 && drawing.secondaryStructureDrawing.zoomLevel == 1.0) {
                    //it seems it is a first opening, then we fit to the display
                    mediator.canvas2D.fitStructure(null)
                }
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class ApplyTheme(mediator: Mediator, val theme: Theme) : RNArtistTask(mediator) {
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
                    "Theming the RNA 2D..."
                )
            }
            Thread.sleep(100)

            mediator.currentDrawing.get()?.let { currentDrawing ->
                currentDrawing.secondaryStructureDrawing.applyTheme(theme)
                mediator.canvas2D.repaint()
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}



/**
 * Redefine the setOnSucceeded for each task to run the next one (if any) and to use the same TaskDialog.
 * To launch the pipeline, instanciate a TaskDialog and set its task with the first one of the pipeline.
 */
fun constructPipeline(vararg tasks:RNArtistTask) {
    tasks.forEachIndexed { i, task ->
        tasks[i].setOnSucceeded {
            task.resultNow().second?.let { exception ->
                task.rnartistDialog.displayException(exception)
            } ?: run {
                if (i < tasks.size-1) {
                    task.rnartistDialog.task = tasks[i + 1]
                }
                else
                    task.rnartistDialog.stage.close()
            }
        }
    }
}