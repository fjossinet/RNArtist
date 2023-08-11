package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.model.RNArtistEl
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.DBExplorerButtonsPanel
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistDrawing
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistTask
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Stage
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import org.kordamp.ikonli.javafx.FontIcon
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.script.ScriptEngineManager
import kotlin.io.path.absolutePathString
import kotlin.io.path.name


class DBExplorer(val mediator: Mediator) : VBox() {

    private val drawingsDirName = "drawings"
    private val loadDB = Button(null, FontIcon("fas-folder-open:15"))
    private val reloadDB = Button(null, FontIcon("fas-sync:15"))
    private val viewRNAClass = Button(null, FontIcon("fas-eye:15"))
    var lastThumbnailCellClicked:ThumbnailCell? = null
    //we explicitly create the observable list of thumbnails to link it to an extractor that will automatically update the picture when the layout and/or theme of the current drawing is saved back in the DSL script.
    private val thumbnailsList: ObservableList<Thumbnail> =
        FXCollections.observableArrayList { thumbnail: Thumbnail ->
            arrayOf(
                thumbnail.layoutAndThemeUpdated
            )
        }

    class StructuralClass(var name: String, var absPath: String) {
        override fun toString(): String {
            return this.name
        }
    }

    val thumbnails = GridView<Thumbnail>(thumbnailsList)
    private val treeView = TreeView<StructuralClass>()
    var rootDB: String? = null
        set(value) {
            field = value
            rootDB?.let {
                this.treeView.root = TreeItem(StructuralClass(Path.of(it).name, it))
                dirsWithStructuralDataAlreadyIndexed.clear()
            }

        }

    val dirsWithStructuralDataAlreadyIndexed = mutableListOf<String>()

    init {
        this.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        this.reloadDB.isDisable = true
        this.viewRNAClass.isDisable = true

        val toolBar = DBExplorerButtonsPanel(mediator)
        this.loadDB.onMouseClicked = EventHandler { _ ->
            val directoryChooser = DirectoryChooser()
            directoryChooser.showDialog(null)?.let {
                clearItems()
                rootDB = it.absolutePath
                val w = RNArtistTaskDialog(mediator)
                w.task = LoadDB(mediator)
            }
        }
        this.loadDB.tooltip = Tooltip("Load database")
        toolBar.addButton(this.loadDB)

        this.reloadDB.onMouseClicked = EventHandler { _ ->
            val w = RNArtistTaskDialog(mediator)
            w.task = LoadDB(mediator)
        }
        this.reloadDB.tooltip = Tooltip("ReLoad database")
        toolBar.addButton(this.reloadDB)

        this.viewRNAClass.onMouseClicked = EventHandler { _ ->
            this.treeView.selectionModel.selectedItem?.let { selectedItem ->
                class LoadThumbnailsTask(mediator: Mediator) : RNArtistTask(mediator) {
                    init {
                        setOnSucceeded { _ ->
                            this.rnartistTaskDialog.stage.hide()
                            loadDB.isDisable = false
                            reloadDB.isDisable = false
                            viewRNAClass.isDisable = false
                        }
                    }

                    override fun call(): Pair<Any?, Exception?> {
                        try {
                            rootDB?.let { rootDB ->

                                Platform.runLater {
                                    clearItems()
                                    loadDB.isDisable = true
                                    reloadDB.isDisable = true
                                    viewRNAClass.isDisable = true
                                }
                                Thread.sleep(100)

                                //we will process the folder of the item selected and all the indexed folders
                                val folders2View = mutableListOf(selectedItem.value.absPath)

                                folders2View.addAll(dirsWithStructuralDataAlreadyIndexed.filter {
                                    it.startsWith(
                                        selectedItem.value.absPath
                                    ) && it != selectedItem.value.absPath /*to not add it twice if indexed*/
                                })

                                folders2View.forEach { absPath ->
                                    Platform.runLater {
                                        updateProgress(-1.toDouble(), Double.MAX_VALUE)
                                        stepProperty.value = null
                                    }
                                    Thread.sleep(100)
                                    val path = Paths.get(
                                        rootDB,
                                        drawingsDirName,
                                        *absPath.split(rootDB).last().removePrefix("/")
                                            .removeSuffix("/").split("/").toTypedArray()
                                    )
                                    val drawingsDir = File(path.toUri())
                                    val dataDir = File(absPath)

                                    val script = File(File(absPath).parent, "${dataDir.name}.kts")
                                    val containsStructuralData = containsStructuralData(dataDir.toPath())
                                    if (containsStructuralData && !script.exists()) {
                                        script.createNewFile()

                                        val rnartistEl = RNArtistEl()

                                        val pngEl = rnartistEl.addPNGEl()

                                        pngEl.setPath(path.absolutePathString())

                                        pngEl.setWidth(250.0)
                                        pngEl.setHeight(250.0)

                                        val ssEl = rnartistEl.addSSEl()

                                        var totalStructures = 0

                                        dataDir.listFiles(FileFilter { it.name.endsWith(".vienna") })?.let {
                                            if (it.isNotEmpty()) {
                                                val viennaEl = ssEl.addViennaEl()
                                                viennaEl.setPath(dataDir.absolutePath)
                                                totalStructures += it.size
                                            }
                                        }

                                        dataDir.listFiles(FileFilter { it.name.endsWith(".bpseq") })?.let {
                                            if (it.isNotEmpty()) {
                                                val bpSeqEl = ssEl.addBPSeqEl()
                                                bpSeqEl.setPath(dataDir.absolutePath)
                                                totalStructures += it.size
                                            }
                                        }

                                        dataDir.listFiles(FileFilter { it.name.endsWith(".ct") })?.let {
                                            if (it.isNotEmpty()) {
                                                val ctEl = ssEl.addCTEl()
                                                ctEl.setPath(dataDir.absolutePath)
                                                totalStructures += it.size
                                            }
                                        }

                                        Platform.runLater {
                                            updateMessage("Computing $totalStructures drawings for ${dataDir.name}, please wait...")
                                        }
                                        Thread.sleep(2000)

                                        val themeEl = rnartistEl.addThemeEl()

                                        val detailsEl = themeEl.addDetailsEl()
                                        detailsEl.setValue(4)

                                        var colorEl = themeEl.addColorEl()
                                        colorEl.setType("helix")
                                        colorEl.setValue("darkorange")

                                        colorEl = themeEl.addColorEl()
                                        colorEl.setType("junction")
                                        colorEl.setValue("lightgreen")

                                        val lineEl = themeEl.addLineEl()
                                        lineEl.setValue(3.0)

                                        val scriptBuffer = StringBuffer()
                                        rnartistEl.dump("", scriptBuffer)
                                        val scriptContent = scriptBuffer.toString()
                                        script.writeText(scriptContent)

                                        val manager = ScriptEngineManager()
                                        val engine = manager.getEngineByExtension("kts")
                                        engine.eval(
                                            "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                                System.getProperty(
                                                    "line.separator"
                                                )
                                            } $scriptContent"
                                        )

                                        Platform.runLater {
                                            updateMessage("Computing done!")
                                        }
                                        Thread.sleep(2000)

                                    }

                                    val total =
                                        drawingsDir.listFiles(FileFilter { it.name.endsWith(".png") })?.size ?: 0

                                    var i = 0
                                    Platform.runLater {
                                        updateProgress(i.toDouble(), total.toDouble())
                                    }
                                    Thread.sleep(100)
                                    drawingsDir.listFiles(FileFilter { it.name.endsWith(".png") })?.forEach {
                                        Platform.runLater {
                                            updateMessage("Loading ${it.name.split(".png").first()} in ${dataDir.name}")
                                            val t = Thumbnail(
                                                this.mediator,
                                                it,
                                                File(
                                                    drawingsDir,
                                                    "${it.name.split(".png").first()}.kts"
                                                ).absolutePath
                                            )
                                            thumbnails.items.add(t)
                                            updateProgress((++i).toDouble(), total.toDouble())
                                            stepProperty.value = Pair(i, total)
                                        }
                                        Thread.sleep(100)
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
        }
        this.viewRNAClass.tooltip = Tooltip("View RNA class")
        toolBar.addButton(this.viewRNAClass)

        this.children.add(toolBar)

        this.thumbnails.background =
            Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        this.thumbnails.padding = Insets(0.0, 5.0, 5.0, 5.0)
        this.thumbnails.horizontalCellSpacing = 10.0
        this.thumbnails.verticalCellSpacing = 10.0
        this.thumbnails.cellWidth = 250.0
        this.thumbnails.cellHeight = 250.0
        this.thumbnails.items.addListener(ListChangeListener { _ ->

        })
        this.thumbnails.setCellFactory { ThumbnailCell() }

        this.treeView.padding = Insets(0.0, 5.0, 5.0, 5.0)
        this.treeView.background =
            Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        this.treeView.isEditable = true
        this.treeView.root = TreeItem(StructuralClass("No database selected", ""))
        this.treeView.onMouseClicked = EventHandler {
        }

        val splitPane = SplitPane()
        splitPane.orientation = Orientation.HORIZONTAL
        val s = ScrollPane(this.treeView)
        s.isFitToHeight = true
        s.isFitToWidth = true
        splitPane.items.add(s)
        splitPane.items.add(this.thumbnails)
        splitPane.setDividerPositions(0.10)
        setVgrow(splitPane, Priority.ALWAYS)
        this.children.add(splitPane)

    }

    private fun containsStructuralData(path: Path): Boolean {
        var containsStructuralData = false
        path.toFile().listFiles(FileFilter {
            it.name.endsWith(".vienna") || it.name.endsWith(".bpseq") || it.name.endsWith(
                ".ct"
            )
        })?.let {
            containsStructuralData = it.isNotEmpty()
        }
        return containsStructuralData
    }


    private fun getNonIndexedDirsWithStructuralData(dirs: MutableList<File>, dir: Path) {
        try {
            Files.newDirectoryStream(dir).use { stream ->
                for (path in stream) {
                    if (path.toFile().isDirectory() && path.name != drawingsDirName) {
                        val containsStructuralData = containsStructuralData(path)
                        if (containsStructuralData && !dirsWithStructuralDataAlreadyIndexed.contains(path.absolutePathString()))
                            dirs.add(path.toFile())
                        getNonIndexedDirsWithStructuralData(dirs, path)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun findIntermediateDirs(absolutePath2StructuralFiles: String) =
        absolutePath2StructuralFiles.split(rootDB!!).last().removePrefix("/").removeSuffix("/").split("/")

    private fun addNewStructuralClassToTreeView(absolutePath2StructuralFiles: String) {
        rootDB?.let { _ ->
            val intermediateDirs = findIntermediateDirs(absolutePath2StructuralFiles)
            var currentParent = this.treeView.root
            for (i in 0 until intermediateDirs.size) {
                if (i == intermediateDirs.size - 1) {
                    currentParent.children.find { intermediateDirs[i] == it.value.name }?.let {
                        //this child already exists in the treeview
                    } ?: run {
                        currentParent.children.add(
                            TreeItem(
                                StructuralClass(
                                    intermediateDirs[i],
                                    absolutePath2StructuralFiles
                                )
                            )
                        )
                    }
                } else {
                    val item = currentParent.children.find { intermediateDirs[i] == it.value.name }
                    item?.let {
                        currentParent = item
                    } ?: run {
                        val treeItem = TreeItem(
                            StructuralClass(
                                intermediateDirs[i],
                                Path.of(
                                    absolutePath2StructuralFiles.split(intermediateDirs[i]).first(),
                                    intermediateDirs[i]
                                ).absolutePathString()
                            )
                        )
                        currentParent.children.add(treeItem)
                        currentParent = treeItem
                    }
                }


            }

        }
    }

    fun count() = this.thumbnails.items.size

    fun removeItem(thumbnail: Thumbnail) {
        val index = this.thumbnails.items.indexOfFirst { it == thumbnail }
        this.thumbnails.items.removeAt(index)
    }

    fun clearItems() {
        this.thumbnails.items.clear()
    }

    inner class ThumbnailCell : GridCell<Thumbnail>() {
        private val icon = ImageView()
        private val drawingName = Label()
        private val content: VBox = VBox()
        private val border: HBox
        private val titleBar: HBox

        override fun updateItem(thumbnail: Thumbnail?, empty: Boolean) {
            super.updateItem(thumbnail, empty)
            graphic = null
            text = null
            if (!empty && thumbnail != null) {
                icon.image = thumbnail.image
                graphic = content
            }
        }

        init {
            content.spacing = 5.0
            content.alignment = Pos.CENTER
            this.border = HBox()
            this.border.children.add(icon)
            this.border.style = "-fx-border-color: lightgray; -fx-border-width: 4px;"
            content.children.add(border)
            titleBar = HBox()
            titleBar.spacing = 5.0
            titleBar.alignment = Pos.CENTER
            titleBar.children.add(drawingName)
            val deleteProject = Label(null, FontIcon("fas-trash:15"))
            (deleteProject.graphic as FontIcon).fill = Color.WHITE
            titleBar.children.add(deleteProject)
            content.children.add(titleBar)
            drawingName.textFill = Color.WHITE
            drawingName.style = "-fx-font-weight: bold"
            deleteProject.onMouseClicked = EventHandler { event ->
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.initModality(Modality.WINDOW_MODAL)
                alert.title = "Confirm Deletion"
                alert.headerText = null
                alert.contentText = "Are you sure to delete this 2D?"
                val alerttStage = alert.dialogPane.scene.window as Stage
                alerttStage.isAlwaysOnTop = true
                alerttStage.toFront()
                val result = alert.showAndWait()
                if (result.get() == ButtonType.OK) {
                    removeItem(item)
                } else {
                    event.consume()
                }
            }
            icon.onMouseClicked = EventHandler {
                lastThumbnailCellClicked = this
                val scriptContent = File(item.dslScriptAbsolutePath).readLines()
                //we remove the png output in the script to avoid to override the current preview
                val scriptContentWithoutOutput = mutableListOf<String>()
                var inOutput = false
                for (i in 0 until scriptContent.size) {
                    if (scriptContent[i].contains("png")) {
                        inOutput = true
                    }
                    if (inOutput && scriptContent[i].contains("}")) {
                        inOutput = false
                        continue
                    }
                    if (!inOutput)
                        scriptContentWithoutOutput.add(scriptContent[i])
                }
                val manager = ScriptEngineManager()
                val engine = manager.getEngineByExtension("kts")
                val drawings = engine.eval(
                    "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                        System.getProperty(
                            "line.separator"
                        )
                    } ${scriptContentWithoutOutput.joinToString("\n")}"
                ) as List<SecondaryStructureDrawing>
                val drawing = RNArtistDrawing(mediator, drawings.first(), item.dslScriptAbsolutePath)
                mediator.currentDrawing.set(drawing)
                if (drawing.drawing.viewX == 0.0 && drawing.drawing.viewY == 0.0 && drawing.drawing.zoomLevel == 1.0) {
                    //it seems it is a first opening, then we fit to the display
                    mediator.canvas2D.fitStructure(null)
                }
            }
            this.onMouseEntered = EventHandler { border.style = "-fx-border-color: darkgray; -fx-border-width: 4px;" }
            this.onMouseExited = EventHandler { border.style = "-fx-border-color: lightgray; -fx-border-width: 4px;" }
        }
    }

    private inner class LoadDB(mediator: Mediator) : RNArtistTask(mediator) {
        init {
            setOnSucceeded { _ ->
                this.rnartistTaskDialog.stage.hide()
                loadDB.isDisable = false
                reloadDB.isDisable = false
                viewRNAClass.isDisable = false
            }
        }

        override fun call(): Pair<Any?, Exception?> {
            return try {
                Platform.runLater {
                    loadDB.isDisable = true
                    viewRNAClass.isDisable = true
                    updateMessage("Loading database, please wait...")
                }
                Thread.sleep(100)
                rootDB?.let {
                    val dbIndex = File(File(it), ".rnartist_db_index")
                    val pw: PrintWriter
                    if (dbIndex.exists()) {
                        Platform.runLater {
                            updateMessage("Found database index, please wait...")
                        }
                        Thread.sleep(2000)
                        dbIndex.readLines().forEach {
                            if (!dirsWithStructuralDataAlreadyIndexed.contains(it)) { //otherwise the reload button which call this task will add one more time the dirs stored in the index file
                                addNewStructuralClassToTreeView(it)
                                dirsWithStructuralDataAlreadyIndexed.add(it)
                            }
                        }
                        pw = PrintWriter(FileWriter(dbIndex, true))
                    } else {
                        dbIndex.createNewFile()
                        pw = PrintWriter(dbIndex)
                    }

                    Platform.runLater {
                        updateMessage("Searching for structural data in non-indexed folders, please wait...")
                    }
                    Thread.sleep(2000)
                    val dirsWithStructuralData = mutableListOf<File>()
                    getNonIndexedDirsWithStructuralData(
                        dirsWithStructuralData,
                        File(it).toPath()
                    )
                    if (dirsWithStructuralData.isNotEmpty()) {
                        Platform.runLater {
                            updateMessage("Found ${dirsWithStructuralData.size} non-indexed folders with structural data!")
                        }
                        Thread.sleep(2000)
                        var i = 1
                        dirsWithStructuralData.forEach {
                            pw.println(it.absolutePath)
                            addNewStructuralClassToTreeView(it.absolutePath)
                            dirsWithStructuralDataAlreadyIndexed.add(it.absolutePath)
                            i++
                        }
                        Platform.runLater {
                            updateMessage("Indexing done!")
                        }
                        Thread.sleep(2000)
                    }
                    pw.close()
                }
                Pair(null, null)
            } catch (e: Exception) {
                Pair(null, e)
            }
        }
    }

}

class Thumbnail(val mediator: Mediator, pngFile: File, val dslScriptAbsolutePath: String) {

    var image:Image? = null
    val layoutAndThemeUpdated = SimpleBooleanProperty()

    init {
        image = Image(pngFile.toPath().toUri().toString())
        this.layoutAndThemeUpdated.addListener { _, _, _ ->
            image = Image(pngFile.toPath().toUri().toString())
        }
    }

}


