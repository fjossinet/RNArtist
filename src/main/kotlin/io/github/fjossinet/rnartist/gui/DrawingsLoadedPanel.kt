package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.io.createTemporaryFile
import io.github.fjossinet.rnartist.model.DrawingLoaded
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.geom.Rectangle2D
import java.io.File

class DrawingsLoadedPanel(val mediator: Mediator): GridView<DrawingLoadedPanel>() {

    init {
        this.padding = Insets(20.0, 20.0, 20.0, 20.0)
        this.horizontalCellSpacing = 20.0
        this.verticalCellSpacing = 20.0
        this.cellWidth = 200.0
        this.cellHeight = 200.0
        this.style = "-fx-background-color: lightgray;"
        this.setCellFactory { DrawingLoadedCell() }
    }

    fun addItem(drawingLoaded:DrawingLoaded) {
        this.items.add(DrawingLoadedPanel(drawingLoaded))
    }

    fun removeItem(drawingLoaded:DrawingLoaded) {
        val index = this.items.indexOfFirst { it.drawingLoaded == drawingLoaded }
        val lastPreviews = this.items.get(index).lastPreviews
        this.items.removeAt(index)
        /*lastPreviews.forEach {
            //TODO doesn't want to delete file, probably a lock on them. How to improve that?
            File(it).delete()
        }*/
    }

    private inner class DrawingLoadedCell : GridCell<DrawingLoadedPanel>() {
        private val icon = ImageView()
        private val drawingName = Label()
        private val content: VBox
        private val border: HBox
        private val titleBar: HBox

        override fun updateItem(drawingLoadedPanel: DrawingLoadedPanel?, empty: Boolean) {
            super.updateItem(drawingLoadedPanel, empty)
            graphic = null
            text = null
            if (!empty && drawingLoadedPanel != null) {
                drawingName.text = drawingLoadedPanel.drawingLoaded.drawing.name
                icon.image = drawingLoadedPanel.image
                graphic = content
            }
        }

        init {
            content = VBox()
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
            (deleteProject.graphic as FontIcon).fill = Color.BLACK
            titleBar.children.add(deleteProject)
            content.children.add(titleBar)
            drawingName.textFill = Color.BLACK
            drawingName.style = "-fx-font-weight: bold"
            deleteProject.onMouseClicked = EventHandler { event ->
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.initModality(Modality.WINDOW_MODAL)
                alert.title = "Confirm Deletion"
                alert.headerText = null
                alert.contentText = "Are you sure to delete this project?"
                val alerttStage = alert.dialogPane.scene.window as Stage
                alerttStage.isAlwaysOnTop = true
                alerttStage.toFront()
                val result = alert.showAndWait()
                if (result.get() == ButtonType.OK) {
                    removeItem(item.drawingLoaded)
                } else {
                    event.consume()
                }
            }
            icon.onMouseClicked = EventHandler {
                mediator.drawingDisplayed.set(item.drawingLoaded)
                if (item.drawingLoaded.drawing.viewX == 0.0 && item.drawingLoaded.drawing.viewY == 0.0 && item.drawingLoaded.drawing.zoomLevel == 1.0) {
                    //it seems it is a first opening, then we fit to the display
                    mediator.canvas2D.fitStructure(null)
                }
            }
            this.onMouseEntered = EventHandler { border.style = "-fx-border-color: darkgray; -fx-border-width: 4px;" }
            this.onMouseExited = EventHandler { border.style = "-fx-border-color: lightgray; -fx-border-width: 4px;" }
        }
    }


}

class DrawingLoadedPanel(val drawingLoaded: DrawingLoaded) {

    var lastPreviews = mutableListOf<String>()

    val image: Image?
        get() {
            try {
                val previewFile = createTemporaryFile("preview.png")
                drawingLoaded.drawing.asPNG(Rectangle2D.Double(0.0,0.0,200.0,200.0), null,previewFile)
                lastPreviews.add(previewFile.toURI().toURL().toString())
                return Image(lastPreviews.last())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
}
