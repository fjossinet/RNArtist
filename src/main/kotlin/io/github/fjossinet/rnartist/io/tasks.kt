package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.gui.HelpDialog
import io.github.fjossinet.rnartist.model.RNArtistDrawing
import io.github.fjossinet.rnartist.model.RNArtistTask
import javafx.application.Platform
import java.io.File
import java.io.FileFilter
import kotlin.io.path.invariantSeparatorsPathString

/**
 * Save the current 2D in the current db folder.
 * If the 2D has been loaded from this folder, it is an update. Otherwise we save the new working copy in the current folder
 */
class Save2D(mediator: Mediator) : RNArtistTask<File?>(mediator) {
    init {
        setOnSucceeded { _ ->
            this.resultNow().second?.let { exception ->
                this.rnartistDialog.displayException(exception)
            } ?: run {
                this.resultNow().first?.let { scriptFile ->
                    //it is a copy of the current 2D in the current folder
                    mediator.rnartist.currentThumbnail.set(mediator.rnartist.addThumbnail(File(mediator.currentDB!!.getDrawingsDirForDataDir(File(mediator.rnartist.currentDBFolderAbsPath.get())), "${scriptFile.name.split(".kts").first()}.png"),
                        scriptFile.invariantSeparatorsPath))
                } ?: run {
                    //it is an update of the current 2D in the current folder
                    mediator.rnartist.thumbnails.items.forEach {
                        if (it.dslScriptInvariantSeparatorsPath.equals(mediator.currentDrawing.get()?.dslScriptInvariantSeparatorsPath)) {
                            it.layoutAndThemeUpdated.value = !it.layoutAndThemeUpdated.value
                        }
                    }
                }
                this.rnartistDialog.stage.close()
            }
        }
    }

    override fun call(): Pair<File?, Exception?> {
        try {
            updateMessage(
                "Saving 2D for ${
                    mediator.currentDrawing.get()!!.dslScriptInvariantSeparatorsPath.removeSuffix(".kts").split(
                        System.getProperty(
                            "file.separator"
                        )
                    ).last()
                }..."
            )
            mediator.currentDrawing.get()?.let { currentDrawing ->
                //if the dslScriptInvariantSeparatorsPath for this 2D doesn't start with the current DB folder, this means that the user wants to add a 2D coming from elsewhere
                if (!File(currentDrawing.dslScriptInvariantSeparatorsPath).parentFile.invariantSeparatorsPath.equals(mediator.rnartist.currentDBFolderAbsPath.get())) {
                    val scriptFile = mediator.currentDB!!.addNewStructure(File(mediator.rnartist.currentDBFolderAbsPath.get()), currentDrawing.secondaryStructureDrawing.secondaryStructure, currentDrawing.rnArtistEl)
                    mediator.scriptEngine.eval(scriptFile.readText())
                    currentDrawing.dslScriptInvariantSeparatorsPath = scriptFile.invariantSeparatorsPath

                    return Pair(scriptFile, null)
                } else { //otherwise it is an update of a 2D already in the current DB folder
                    //we replace the dsl script in the file of the currentDrawing with the dsl script in memory
                    with(File(currentDrawing.dslScriptInvariantSeparatorsPath)) {
                        currentDrawing.rnArtistEl.getThemeOrNew().cleanHistory()
                        currentDrawing.rnArtistEl.getLayoutOrNew().cleanHistory()
                        val content = currentDrawing.rnArtistEl.dump().toString()
                        this.writeText(content)
                        mediator.scriptEngine.eval(content)
                    }
                    return Pair(null, null)
                }
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

/**
 * Save the current selection in the 2D displayed as a new 2D in the current DB folder
 */
class Save2DSelection(mediator: Mediator) : RNArtistTask<File?>(mediator) {
    init {
        setOnSucceeded { _ ->
            this.resultNow().second?.let { exception ->
                this.rnartistDialog.displayException(exception)
            } ?: run {
                this.resultNow().first?.let { scriptFile ->
                    mediator.rnartist.currentThumbnail.set(mediator.rnartist.addThumbnail(
                        File(
                            mediator.currentDB!!.getDrawingsDirForDataDir(File(mediator.rnartist.currentDBFolderAbsPath.get())),
                            "${scriptFile.name.split(".kts").first()}.png"
                        ),
                        scriptFile.invariantSeparatorsPath
                    ))
                }
                this.rnartistDialog.stage.close()
            }
        }
    }

    override fun call(): Pair<File?, Exception?> {
        return try {
            updateMessage(
                "Saving 2D Selection ..."
            )
            var scriptFile:File? = null
            mediator.currentDrawing.get()?.let { currentDrawing ->
                val selectedLoc = Location(currentDrawing.getSelectedPositions().toIntArray())
                val dataDir = File(mediator.rnartist.currentDBFolderAbsPath.get())
                val current2DFileName = currentDrawing.secondaryStructureDrawing.name
                val domainsAlreadySaved = dataDir.listFiles().count {it.name.endsWith(".kts") && it.name.startsWith("${current2DFileName}_domain")}
                val fileName = "${current2DFileName}_domain_${domainsAlreadySaved+1}"
                val ss = SecondaryStructure(
                    RNA(
                        fileName,
                        currentDrawing.secondaryStructureDrawing.secondaryStructure.rna.subSequence(selectedLoc)
                    ), currentDrawing.secondaryStructureDrawing.secondaryStructure.toBracketNotation(selectedLoc)
                )
                scriptFile = mediator.currentDB?.addNewStructure(
                    dataDir,
                    ss

                )
                scriptFile?.let {
                    mediator.scriptEngine.eval(it.readText())
                }
            }
            Pair(scriptFile, null)
        } catch (e: Exception) {
            Pair(null, e)
        }
    }

}

class AddStructureFromURL(
    mediator: Mediator,
    val url: String,
    val dataDir: String
) : RNArtistTask<File?>(mediator) {

    var entryID:String? = null
    init {
        if (url.startsWith("https://rnacentral.org")) {
            val tokens = url.split("/")
            this.entryID = tokens[tokens.size - 2]
        }
        setOnSucceeded { _ ->
            this.resultNow().second?.let { exception ->
                this.rnartistDialog.displayException(exception)
            } ?: run {
                this.resultNow().first?.let { scriptFile ->
                    if (mediator.rnartist.currentDBFolderAbsPath.equals(scriptFile.invariantSeparatorsPath)) { //we add the thumbnail only if the target dir is the current DB folder
                        mediator.currentDB?.let { rootDB ->
                            mediator.rnartist.addThumbnail(
                                File(rootDB.getDrawingsDirForDataDir(File(dataDir)), "${entryID}.png"),
                                scriptFile.invariantSeparatorsPath
                            )
                        }
                    }
                }
                this.rnartistDialog.stage.close()
            }
        }
        setOnCancelled {
            this.rnartistDialog.stage.close()
        }

    }

    override fun call(): Pair<File?, Exception?> {
        try {
            mediator.currentDB?.let { rootDB ->
                this.entryID?.let { entryID ->
                    updateMessage("Downloading data....")
                    RNACentral().fetch(entryID)?.let { ss ->
                        updateMessage("Saving data....")
                        val scriptFile = rootDB.addNewStructure(File(dataDir), ss)
                        updateMessage("Computing 2D....")
                        mediator.scriptEngine.eval(scriptFile.readText())
                        return Pair(scriptFile,null)
                    } ?: run {
                        return Pair(null, RuntimeException("No 2D found in RNACentral entry $entryID"))
                    }
                } ?: run {
                    return Pair(null, RuntimeException("Not an RNACentral URL"))
                }
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

class LoadDB(mediator: Mediator, rootDbInvariantSeparatorsPath:String) : RNArtistTask<Any?>(mediator) {

    val reload:Boolean

    init {
        mediator.rnartist.currentDBFolderAbsPath.set(null)
        this.reload = (mediator.currentDB?.rootInvariantSeparatorsPath?.equals(rootDbInvariantSeparatorsPath) ?: false) && File(kotlin.io.path.Path(rootDbInvariantSeparatorsPath, ".rnartist_db.index").invariantSeparatorsPathString).exists() /*we could open a new database with the same abspath whose former version has been removed*/
        if (!this.reload) {
            mediator.rnartist.currentDB.set(RNArtistDB(rootDbInvariantSeparatorsPath))
        }

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
        return try {
            updateMessage(
                if (this.reload)
                    "Reloading database, please wait..."
                else
                    "Loading database, please wait..."
            )

            mediator.currentDB?.let { currentDB ->
                val dbIndex = currentDB.indexFile
                dbIndex.readLines().forEach {
                    mediator.rnartist.addFolderToTreeView(it)
                }

                updateMessage("Searching for non-indexed folders, please wait...")
                val nonIndexedDirs = currentDB.indexDatabase()
                if (nonIndexedDirs.isNotEmpty()) {
                    updateMessage("Found ${nonIndexedDirs.size} folders!")
                    nonIndexedDirs.forEach {
                        if (this.isCancelled)
                            return Pair(null, null)
                        mediator.rnartist.addFolderToTreeView(it.invariantSeparatorsPath)
                    }
                    updateMessage("Indexing done!")
                } else {
                    updateMessage("No new folders found!")
                }
            }
            Pair(null, null)
        } catch (e: Exception) {
            Pair(null, e)
        }
    }
}

class CreateDBFolder(mediator: Mediator, val absPathFolder: String) : RNArtistTask<Any?>(mediator) {
    init {
        setOnSucceeded { _ ->
            this.resultNow().second?.let { exception ->
                this.rnartistDialog.displayException(exception)
            } ?: run {
                mediator.currentDB?.let { rootDB ->
                    rootDB.createNewFolder(absPathFolder)?.let { newFolder ->
                        mediator.rnartist.addFolderToTreeView(newFolder.invariantSeparatorsPath)?.let {
                            mediator.rnartist.expandTreeView(it)
                        }
                    }
                }
                this.rnartistDialog.stage.close()
            }
        }
        setOnCancelled {
            this.rnartistDialog.stage.close()
        }
    }

    override fun call(): Pair<Any?, Exception?> {
            return Pair(null, null)
    }

}

class LoadDBFolder(mediator: Mediator) : RNArtistTask<List<File>?>(mediator) {
    init {
        setOnSucceeded { _ ->
            this.resultNow().second?.let {
                this.rnartistDialog.displayException(it)
            } ?: run {
                this.resultNow().first?.let {
                    mediator.rnartist.lowerPanel.dbExplorerPanel.dbExplorerSubPanel.dbTreeView.selectionModel.selectedItem?.let { selectedDBFolder ->
                        val dataDir = File(selectedDBFolder.value.absPath)
                        it.forEach {
                            mediator.rnartist.addThumbnail(
                                it,
                                File(
                                    dataDir,
                                    "${it.name.split(".png").first()}.kts"
                                ).invariantSeparatorsPath
                            )
                        }
                    }
                    this.rnartistDialog.stage.close()
                }

            }
        }
        setOnCancelled {
            this.rnartistDialog.stage.close()
        }
    }

    override fun call(): Pair<List<File>?, Exception?> {
        try {
            var fullTotalStructures = 0

            val pictures = mutableListOf<File>()

            mediator.currentDB?.let { rootDB ->

                mediator.rnartist.lowerPanel.dbExplorerPanel.dbExplorerSubPanel.dbTreeView.selectionModel.selectedItem?.let { selectedDBFolder ->

                    if (this.isCancelled)
                        return Pair(null, null)

                    val dataDir = File(selectedDBFolder.value.absPath)
                    val drawingsDir = rootDB.getDrawingsDirForDataDir(dataDir)
                    val script = rootDB.getScriptFileForDataDir(dataDir)

                    var scriptContent = script.readText()
                    val totalStructures = dataDir.listFiles(FileFilter {
                        it.name.endsWith(".ct") || it.name.endsWith(".vienna") || it.name.endsWith(
                            ".bpseq"
                        ) || it.name.endsWith(".pdb")
                    })?.size ?: 0

                    fullTotalStructures += totalStructures

                    var totalPNGFiles =
                        drawingsDir.listFiles(FileFilter { it.name.endsWith(".png") })?.size
                            ?: 0

                    val structures2Compute = totalStructures - totalPNGFiles

                    if (structures2Compute > 0) {
                        updateMessage("Computing $structures2Compute 2Ds for ${dataDir.name}, please wait...")
                        mediator.scriptEngine.eval(scriptContent)
                    }

                    totalPNGFiles =
                        drawingsDir.listFiles(FileFilter { it.name.endsWith(".png") })?.size
                            ?: 0

                    var i = 0
                    updateProgress(i.toDouble(), totalPNGFiles.toDouble())

                    drawingsDir.listFiles(FileFilter { it.name.endsWith(".png") })
                        ?.forEach {
                            if (this.isCancelled)
                                return Pair(null, null)
                            pictures.add(it)
                        }
                }
            }
            return Pair(pictures, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}

/**
 * Load a structure and display it in the canvas as described in the script whose absolute path is given as argument
 */
class LoadStructure(mediator: Mediator, val dslScriptInvariantSeparatorsPath: String) : RNArtistTask<Any?>(mediator) {
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
            updateMessage(
                "Loading 2D for ${
                    dslScriptInvariantSeparatorsPath.removeSuffix(".kts").split(
                        System.getProperty(
                            "file.separator"
                        )
                    ).last()
                }..."
            )

            val result =
                mediator.scriptEngine.eval(File(dslScriptInvariantSeparatorsPath).readText()) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>
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

class ApplyTheme(mediator: Mediator, val theme: Theme) : RNArtistTask<Any?>(mediator) {
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

/*
* Apply theme and layout to all the 2Ds in the current DB folder (except the current 2D)
* */
class UpdateAll2Ds(mediator: Mediator) : RNArtistTask<Any?>(mediator) {
    init {
        setOnSucceeded { _ ->
            this.resultNow().second?.let { exception ->
                this.rnartistDialog.displayException(exception)
            } ?: run {
                mediator.rnartist.thumbnails.items.forEach { item ->
                    if (item != mediator.rnartist.currentThumbnail.get()) {
                        item.layoutAndThemeUpdated.value = !item.layoutAndThemeUpdated.value
                    }
                }
            }
            this.rnartistDialog.stage.close()
        }
        setOnCancelled {
            this.rnartistDialog.stage.close()
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            mediator.currentDrawing.get()?.let { currentDrawing ->
                //we replace the dsl script in the file of the currentDrawing with the dsl script in memory

                //then the other thumbnails
                mediator.rnartist.thumbnails.items.forEach { item ->
                    if (item !=  mediator.rnartist.currentThumbnail.get()) {
                        updateMessage(
                            "Updating 2D for ${
                                item.dslScriptInvariantSeparatorsPath.removeSuffix(".kts").split(
                                    System.getProperty(
                                        "file.separator"
                                    )
                                ).last()
                            }..."
                        )
                        val rnartistEl =
                            (mediator.scriptEngine.eval(File(item.dslScriptInvariantSeparatorsPath).readText()) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.second

                        rnartistEl?.let {
                            //we replace the theme and layout elements in the dsl script for this thumbnail with the ones from the current drawing
                            rnartistEl.addTheme(currentDrawing.rnArtistEl.getThemeOrNew())
                            rnartistEl.addLayout(currentDrawing.rnArtistEl.getLayoutOrNew())

                            with(File(item.dslScriptInvariantSeparatorsPath)) {
                                val content = rnartistEl.dump().toString()
                                this.writeText(content)
                                mediator.scriptEngine.eval(content)
                            }
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


/**
 * Redefine the setOnSucceeded for each task to run the next one (if any) and to use the same TaskDialog.
 * To launch the pipeline, instanciate a TaskDialog and set its task with the first one of the pipeline.
 */
fun constructPipeline(vararg tasks: RNArtistTask<*>, atTheEnd:(()->Any?)?= null) {
    tasks.forEachIndexed { i, task ->
        tasks[i].setOnSucceeded {
            task.resultNow().second?.let { exception ->
                task.rnartistDialog.displayException(exception)
            } ?: run {
                if (i < tasks.size - 1) {
                    task.rnartistDialog.task = tasks[i + 1]
                } else
                    atTheEnd?.invoke()
                    task.rnartistDialog.stage.close()
            }
        }
    }
}