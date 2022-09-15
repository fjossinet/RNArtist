package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.core.model.FileSource
import io.github.fjossinet.rnartist.core.model.JunctionDrawing
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.gui.SelectionShape
import javafx.beans.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.Rectangle2D
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class DrawingsLoadedPanel(val mediator: Mediator): BorderPane() {

    val gridview = GridView<DrawingLoaded>()

    init {
        this.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        gridview.background = Background(BackgroundFill(RNArtist.RNArtistGUIColor, CornerRadii.EMPTY, Insets.EMPTY))
        gridview.padding = Insets(20.0, 20.0, 20.0, 20.0)
        gridview.horizontalCellSpacing = 20.0
        gridview.verticalCellSpacing = 20.0
        gridview.cellWidth = 200.0
        gridview.cellHeight = 200.0
        this.gridview.style = "-fx-background-color: #333333;"
        this.gridview.setCellFactory { DrawingLoadedCell() }
        this.center = this.gridview

        this.gridview.items.addListener(ListChangeListener { change ->
            if (gridview.items.isEmpty()) {
                mediator.drawingDisplayed.set(null)
                mediator.canvas2D.repaint()
            }
        })
    }

    fun drawingsLoaded() = this.gridview.items

    fun count() = this.gridview.items.size

    fun addItem(drawingLoaded:DrawingLoaded) {
        this.gridview.items.add(drawingLoaded)
    }

    fun removeItem(drawingLoaded:DrawingLoaded) {
        val index = this.gridview.items.indexOfFirst { it == drawingLoaded }
        this.gridview.items.removeAt(index)
    }

    private inner class DrawingLoadedCell : GridCell<DrawingLoaded>() {
        private val icon = ImageView()
        private val drawingName = Label()
        private val content: VBox = VBox()
        private val border: HBox
        private val titleBar: HBox

        override fun updateItem(drawingLoaded: DrawingLoaded?, empty: Boolean) {
            super.updateItem(drawingLoaded, empty)
            graphic = null
            text = null
            if (!empty && drawingLoaded != null) {
                drawingName.text = "${drawingLoaded.drawing.name}${if (drawingLoaded.drawing.secondaryStructure.source != null) "(${if (drawingLoaded.drawing.secondaryStructure.source is FileSource) drawingLoaded.drawing.secondaryStructure.source?.getId()?.split(Regex("[\\/]"))?.last() else drawingLoaded.drawing.secondaryStructure.source?.getId()})" else ""}"
                icon.image = drawingLoaded.thumbnail
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
                mediator.drawingDisplayed.set(item)
                if (item.drawing.viewX == 0.0 && item.drawing.viewY == 0.0 && item.drawing.zoomLevel == 1.0) {
                    //it seems it is a first opening, then we fit to the display
                    mediator.canvas2D.fitStructure(null)
                }
            }
            this.onMouseEntered = EventHandler { border.style = "-fx-border-color: darkgray; -fx-border-width: 4px;" }
            this.onMouseExited = EventHandler { border.style = "-fx-border-color: lightgray; -fx-border-width: 4px;" }
        }
    }

}


class DrawingLoaded(val mediator: Mediator, val drawing: SecondaryStructureDrawing, val id:String) {

    val selectionShapes = FXCollections.observableArrayList<SelectionShape>()
    var tmpChimeraSession: Pair<File, File>? = null
    val layoutAndThemeUpdated = SimpleBooleanProperty()
    var thumbnail:Image? = null

    companion object {
        fun extractor(): Callback<DrawingLoaded, Array<Observable>> {
            return Callback<DrawingLoaded, Array<Observable>> {
                arrayOf(it.layoutAndThemeUpdated)
            }
        }
    }

    init {
        try {
            val previewFile =  Files.createTempFile("preview", "png")
            this.drawing.asPNG(Rectangle2D.Double(0.0,0.0,200.0,200.0), null, previewFile.toFile())
            thumbnail = Image(previewFile.toUri().toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        this.layoutAndThemeUpdated.addListener { _, _, _ ->
            var previous:Path? = null
            this.thumbnail?.let {
                previous = Paths.get(URI(it.url))
            }
            val previewFile = Files.createTempFile("preview", "png")
            this.drawing.asPNG(Rectangle2D.Double(0.0,0.0,200.0,200.0), null, previewFile.toFile())
            thumbnail = Image(previewFile.toUri().toString())
            previous?.let {
                Files.delete(it)
            }
        }

        this.selectionShapes.addListener( ListChangeListener {
            val junctionsSelected = this.selectionShapes.filter { it.element is JunctionDrawing }
            if (junctionsSelected.isEmpty() || junctionsSelected.size > 1)
                mediator.rnartist.junctionSelectionKnob.selectedJunction = null
            else //only on junction has to be selected to change its layout
                mediator.rnartist.junctionSelectionKnob.selectedJunction = junctionsSelected.first().element as JunctionDrawing
        })
    }

}
