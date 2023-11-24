package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.io.createTemporaryFile
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.model.RNArtistDrawing
import io.github.fjossinet.rnartist.model.RNArtistTask
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileFilter
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.writeText

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
                    mediator.rnartist.currentDBDBThumbnail.set(
                        mediator.rnartist.addDBThumbnail(
                            File(
                                mediator.currentDB!!.getDrawingsDirForDataDir(File(mediator.rnartist.currentDBFolderAbsPath.get())),
                                "${scriptFile.name.split(".kts").first()}.png"
                            ),
                            scriptFile.invariantSeparatorsPath
                        )
                    )
                } ?: run {
                    //it is an update of the current 2D in the current folder
                    mediator.rnartist.dbThumbnails.items.forEach {
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
                if (!File(currentDrawing.dslScriptInvariantSeparatorsPath).parentFile.invariantSeparatorsPath.equals(
                        mediator.rnartist.currentDBFolderAbsPath.get()
                    )
                ) {
                    val scriptFile = mediator.currentDB!!.addNewStructure(
                        File(mediator.rnartist.currentDBFolderAbsPath.get()),
                        currentDrawing.secondaryStructureDrawing.secondaryStructure,
                        currentDrawing.rnArtistEl
                    )
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
                    mediator.rnartist.currentDBDBThumbnail.set(
                        mediator.rnartist.addDBThumbnail(
                            File(
                                mediator.currentDB!!.getDrawingsDirForDataDir(File(mediator.rnartist.currentDBFolderAbsPath.get())),
                                "${scriptFile.name.split(".kts").first()}.png"
                            ),
                            scriptFile.invariantSeparatorsPath
                        )
                    )
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
            var scriptFile: File? = null
            mediator.currentDrawing.get()?.let { currentDrawing ->
                val selectedLoc = Location(currentDrawing.getSelectedPositions().toIntArray())
                val dataDir = File(mediator.rnartist.currentDBFolderAbsPath.get())
                val current2DFileName = currentDrawing.secondaryStructureDrawing.name
                val domainsAlreadySaved = dataDir.listFiles()
                    .count { it.name.endsWith(".kts") && it.name.startsWith("${current2DFileName}_domain") }
                val fileName = "${current2DFileName}_domain_${domainsAlreadySaved + 1}"
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

    var entryID: String? = null

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
                            mediator.rnartist.addDBThumbnail(
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
                        return Pair(scriptFile, null)
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

class LoadDB(mediator: Mediator, rootDbInvariantSeparatorsPath: String) : RNArtistTask<Any?>(mediator) {

    val reload: Boolean
    var importDB: Boolean = false

    init {
        mediator.rnartist.currentDBFolderAbsPath.set(null)
        this.reload =
            (mediator.currentDB?.rootInvariantSeparatorsPath?.equals(rootDbInvariantSeparatorsPath) ?: false) && File(
                kotlin.io.path.Path(rootDbInvariantSeparatorsPath, ".rnartist_db.index").invariantSeparatorsPathString
            ).exists() /*we could open a new database with the same abspath whose former version has been removed*/
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
                    "Reloading database..."
                else
                    "Loading database..."
            )

            mediator.currentDB?.let { currentDB ->
                val dbIndex = currentDB.indexFile
                dbIndex.readLines().forEach {
                    if (!it.startsWith(currentDB.rootInvariantSeparatorsPath)) {//this means that it is a database coming from another location/computer. We set the importMode on
                        importDB = true
                        return@forEach // no need to pursue, we will regenerate the index file as a first load
                    } else
                        mediator.rnartist.addFolderToTreeView(it)
                }

                updateMessage("Searching for non-indexed folders...")
                val nonIndexedDirs = currentDB.indexDatabase(importDB)
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
                this.resultNow().first?.let { pictures ->

                    var i = 0
                    updateProgress(i.toDouble(), pictures.size.toDouble())

                    mediator.rnartist.lowerPanel.dbExplorerPanel.dbExplorerSubPanel.dbTreeView.selectionModel.selectedItem?.let { selectedDBFolder ->
                        val dataDir = File(selectedDBFolder.value.absPath)
                        pictures.forEach {
                            i++
                            updateProgress(i.toDouble(), pictures.size.toDouble())
                            mediator.rnartist.addDBThumbnail(
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
                    val script = rootDB.getScriptForDataDir(dataDir)

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
                        updateMessage("Computing $structures2Compute 2Ds for ${dataDir.name}...")
                        mediator.scriptEngine.eval(scriptContent)
                    }

                    updateMessage("Load thumbnails...")
                    Thread.sleep(1000)

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

            val script = File(dslScriptInvariantSeparatorsPath)

            //we remove the instruction to produce the png/svg file (if any), otherwise a new png/svg file will be generated with the current drawing parameters (for exemple a different drawing algorithm)
            //one advantage is to keep as a preview the former result and to see the nex result in the canvas once clicked. Then the user decide to save (and then to update the preview) or not
            var scriptContent = script.readLines().toMutableList()
            var pngEl: PNGEl? = null
            var svgEl: SVGEl? = null
            scriptContent.find { it.matches(Regex("^.*png.*$")) }?.let {
                val pngIndex = scriptContent.indexOf(it)
                pngEl = PNGEl()
                do {
                    if (scriptContent.get(pngIndex).matches(Regex("^.*path.*$")))
                        pngEl!!.setPath(
                            scriptContent.get(pngIndex).split("=").last().trim().removePrefix("\"").removeSuffix("\"")
                        )
                    else if (scriptContent.get(pngIndex).matches(Regex("^.*width.*$")))
                        pngEl!!.setWidth(scriptContent.get(pngIndex).split("=").last().trim().toDouble())
                    else if (scriptContent.get(pngIndex).matches(Regex("^.*height.*$")))
                        pngEl!!.setHeight(scriptContent.get(pngIndex).split("=").last().trim().toDouble())
                    scriptContent.removeAt(pngIndex)
                } while ((!scriptContent.get(pngIndex).matches(Regex("^.*}.*$"))))
                scriptContent.removeAt(pngIndex)
            }
            scriptContent.find { it.matches(Regex("^.*svg.*$")) }?.let {
                val svgIndex = scriptContent.indexOf(it)
                svgEl = SVGEl()
                do {
                    if (scriptContent.get(svgIndex).matches(Regex("^.*path.*$")))
                        svgEl!!.setPath(
                            scriptContent.get(svgIndex).split("=").last().trim().removePrefix("\"").removeSuffix("\"")
                        )
                    else if (scriptContent.get(svgIndex).matches(Regex("^.*width.*$")))
                        svgEl!!.setWidth(scriptContent.get(svgIndex).split("=").last().trim().toDouble())
                    else if (scriptContent.get(svgIndex).matches(Regex("^.*height.*$")))
                        svgEl!!.setHeight(scriptContent.get(svgIndex).split("=").last().trim().toDouble())
                    scriptContent.removeAt(svgIndex)
                } while ((!scriptContent.get(svgIndex).matches(Regex("^.*}.*$"))))
                scriptContent.removeAt(svgIndex)
            }
            val result =
                mediator.scriptEngine.eval(scriptContent.joinToString("\n")) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>
            result?.let {
                //we reinject the png/svg exports if we found and saved ones before to run the script
                pngEl?.let { pngEl ->
                    it.second.addPNG(pngEl)
                }
                svgEl?.let { svgEl ->
                    it.second.addSVG(svgEl)
                }
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
            updateMessage(
                "Theming the RNA 2D..."
            )

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

class ApplyLayout(mediator: Mediator, val layoutResult: RNArtist.LayoutThumbnail) : RNArtistTask<Any?>(mediator) {
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
                "Applying layout to current 2D..."
            )

            mediator.currentDrawing.get()?.let { currentDrawing ->
                (mediator.scriptEngine.eval(File(layoutResult.dslScriptInvariantSeparatorsPath).readText()) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.let {
                    with(it.second.getLayoutOrNew()) {
                        currentDrawing.rnArtistEl.addLayout(this)
                        currentDrawing.secondaryStructureDrawing.applyLayout(this.toLayout())
                    }
                    with(it.second.getThemeOrNew()) {
                        currentDrawing.rnArtistEl.addTheme(this)
                        currentDrawing.secondaryStructureDrawing.applyTheme(this.toTheme())
                    }
                    mediator.canvas2D.fitStructure(null)
                    mediator.canvas2D.repaint()
                }


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
                mediator.rnartist.dbThumbnails.items.forEach { item ->
                    if (item != mediator.rnartist.currentDBDBThumbnail.get()) {
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
                mediator.rnartist.dbThumbnails.items.forEach { item ->
                    if (item != mediator.rnartist.currentDBDBThumbnail.get()) {
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

class LayoutResult(
    val overlappingScore: Int,
    val pngFile: File,
    val dslScriptInvariantSeparatorsPath: String,
    val drawing: SecondaryStructureDrawing? = null
)

class ComputeLayouts(
    mediator: Mediator,
    val junctionSizeDeltas: List<Float> = listOf(0f)
) : RNArtistTask<List<LayoutResult>?>(mediator) {
    init {
        setOnSucceeded { _ ->
            this.resultNow().second?.let {
                this.rnartistDialog.displayException(it)
            } ?: run {
                this.resultNow().first?.let {
                    it.sortedBy { it.overlappingScore }.forEach {
                        mediator.rnartist.addLayoutThumbnail(
                            it.pngFile,
                            it.dslScriptInvariantSeparatorsPath,
                            it.overlappingScore
                        )
                    }
                    this.rnartistDialog.stage.close()
                }
            }
        }
        setOnCancelled {
            this.rnartistDialog.stage.close()
        }
    }

    private fun <T> allPermutations(list: List<T>): Set<List<T>> {
        if (list.isEmpty()) return emptySet()

        fun <T> _allPermutations(list: List<T>): List<List<T>> {
            if (list.isEmpty()) return listOf(emptyList())

            val result: MutableList<List<T>> = mutableListOf()
            for (i in list.indices) {
                _allPermutations(list - list[i]).forEach { item ->
                    result.add(item + list[i])
                }
            }
            return result
        }

        return _allPermutations(list).toSet()
    }

    override fun call(): Pair<List<LayoutResult>?, Exception?> {
        try {
            updateMessage("Setting layouts parameters...")

            val results = mutableListOf<LayoutResult>()
            mediator.currentDrawing.get()?.let { drawing ->

                var ss = drawing.secondaryStructureDrawing.secondaryStructure
                var junctionsToImproveBefore = drawing.secondaryStructureDrawing.junctionsToImprove()

                listOf(
                    ConnectorId.n,
                    ConnectorId.nne,
                    ConnectorId.nnw,
                    ConnectorId.ne,
                    ConnectorId.nw,
                    ConnectorId.ene,
                    ConnectorId.wnw,
                    ConnectorId.e,
                    ConnectorId.w,
                    ConnectorId.ese,
                    ConnectorId.wsw,
                    ConnectorId.se,
                    ConnectorId.sw,
                    ConnectorId.sse,
                    ConnectorId.ssw
                ).forEach { outIdForLongest -> //that's what a script is testing at basically. Now we"re targeting specifically the overlapped junctions and try to change their size and to try a relative helix placement
                    if (junctionsToImproveBefore.isNotEmpty()) {
                        junctionSizeDeltas.forEach { sizeDelta ->
                            updateMessage("Computing 2D layout ${results.size + 1}...")
                            var drawing = SecondaryStructureDrawing(
                                ss,
                                junctionSizeDelta = Pair(sizeDelta, junctionsToImproveBefore),
                                outIdForLongest = { outIdForLongest }
                            )

                            var junctionsToImproveAfter = drawing.junctionsToImprove()

                            var themeEl = ThemeEl()
                            themeEl.addDetails().setValue(1)
                            themeEl.addLine().setValue(5.0)
                            //to highlight the junctions to improve in the preview
                            junctionsToImproveBefore.forEach {
                                with(themeEl.addLine()) {
                                    this.setValue(10.0)
                                    this.addLocation().setLocation(it)
                                }
                                with(themeEl.addColor()) {
                                    this.setValue("red")
                                    this.addLocation().setLocation(it)
                                }
                            }

                            junctionsToImproveBefore.filter { !junctionsToImproveAfter.contains(it) }.forEach {
                                with(themeEl.addLine()) {
                                    this.setValue(10.0)
                                    this.addLocation().setLocation(it)
                                }
                                with(themeEl.addColor()) {
                                    this.setValue("green")
                                    this.addLocation().setLocation(it)
                                }
                            }

                            junctionsToImproveAfter.forEach {
                                with(themeEl.addLine()) {
                                    this.setValue(10.0)
                                    this.addLocation().setLocation(it)
                                }
                                with(themeEl.addColor()) {
                                    this.setValue("red")
                                    this.addLocation().setLocation(it)
                                }
                            }
                            drawing.applyTheme(themeEl.toTheme())

                            var pngFile = createTemporaryFile("layout")
                            drawing.asPNG(frame = Rectangle2D.Double(0.0, 0.0, 350.0, 350.0), outputFile = pngFile)
                            var rnartistEl = initScript()
                            rnartistEl.addSS(drawing.secondaryStructure.getSSEl())
                            rnartistEl.addLayout(drawing.getLayoutEl())
                            rnartistEl.addTheme(themeEl)
                            var scriptFile = kotlin.io.path.createTempFile(prefix = "script", suffix = "kts")
                            scriptFile.writeText(rnartistEl.dump(indent = "", StringBuffer()).toString())
                            results.add(
                                LayoutResult(
                                    drawing.overlappingScore,
                                    pngFile,
                                    scriptFile.invariantSeparatorsPathString,
                                    drawing
                                )
                            )

                            updateMessage("Computing 2D layout ${results.size + 1}...")
                            drawing = SecondaryStructureDrawing(
                                ss,
                                useRelativeHelixPlacement = junctionsToImproveBefore,
                                junctionSizeDelta = Pair(sizeDelta, junctionsToImproveBefore),
                                outIdForLongest = { outIdForLongest }
                            )
                            junctionsToImproveAfter = drawing.junctionsToImprove()

                            themeEl = ThemeEl()
                            themeEl.addDetails().setValue(1)
                            themeEl.addLine().setValue(5.0)
                            //to highlight the junctions to improve in the preview
                            junctionsToImproveBefore.forEach {
                                with(themeEl.addLine()) {
                                    this.setValue(10.0)
                                    this.addLocation().setLocation(it)
                                }
                                with(themeEl.addColor()) {
                                    this.setValue("red")
                                    this.addLocation().setLocation(it)
                                }
                            }

                            junctionsToImproveBefore.filter { !junctionsToImproveAfter.contains(it) }.forEach {
                                with(themeEl.addLine()) {
                                    this.setValue(10.0)
                                    this.addLocation().setLocation(it)
                                }
                                with(themeEl.addColor()) {
                                    this.setValue("green")
                                    this.addLocation().setLocation(it)
                                }
                            }

                            junctionsToImproveAfter.forEach {
                                with(themeEl.addLine()) {
                                    this.setValue(10.0)
                                    this.addLocation().setLocation(it)
                                }
                                with(themeEl.addColor()) {
                                    this.setValue("red")
                                    this.addLocation().setLocation(it)
                                }
                            }
                            drawing.applyTheme(themeEl.toTheme())

                            pngFile = createTemporaryFile("layout")
                            drawing.asPNG(frame = Rectangle2D.Double(0.0, 0.0, 350.0, 350.0), outputFile = pngFile)
                            rnartistEl = initScript()
                            rnartistEl.addSS(drawing.secondaryStructure.getSSEl())
                            rnartistEl.addLayout(drawing.getLayoutEl())
                            rnartistEl.addTheme(themeEl)
                            scriptFile = kotlin.io.path.createTempFile(prefix = "script", suffix = "kts")
                            scriptFile.writeText(rnartistEl.dump(indent = "", StringBuffer()).toString())
                            results.add(
                                LayoutResult(
                                    drawing.overlappingScore,
                                    pngFile,
                                    scriptFile.invariantSeparatorsPathString,
                                    drawing
                                )
                            )
                        }
                    }
                }
                updateMessage("All 2D layouts computed. Loading ${results.size} thumbnails...")
            }
            return Pair(results, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }
}


/**
 * Redefine the setOnSucceeded for each task to run the next one (if any) and to use the same TaskDialog.
 * To launch the pipeline, instanciate a TaskDialog and set its task with the first one of the pipeline.
 */
fun constructPipeline(vararg tasks: RNArtistTask<*>, atTheEnd: (() -> Any?)? = null) {
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