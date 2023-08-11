package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.gui.RNArtistTaskDialog
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Task

abstract class RNArtistTask(val mediator: Mediator) : Task<Pair<Any?, Exception?>>() {
    lateinit var rnartistTaskDialog: RNArtistTaskDialog
    var stepProperty = SimpleObjectProperty<Pair<Int,Int>?>()
}

/*class ApplyThemeAndLayout(mediator: Mediator) : RNArtistTask(mediator) {

    init {
        setOnSucceeded { event ->
            this.rnartistTaskWindow?.stage?.hide()
            val result = get()
            result.first?.let { scriptResult ->
            }
            result.second?.let {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.dialogPane.minWidth = Region.USE_PREF_SIZE
                alert.dialogPane.minHeight = Region.USE_PREF_SIZE
                alert.title = "I got a problem"
                alert.headerText = "RNArtist got a problem"
                alert.contentText =
                    "You can send the exception stacktrace below to fjossinet@gmail.com"
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                it.printStackTrace(pw)
                val exceptionText = sw.toString()

                val label = Label("The exception stacktrace was:")

                val textArea = TextArea(exceptionText)
                textArea.isEditable = false
                textArea.isWrapText = true

                textArea.maxWidth = Double.MAX_VALUE
                textArea.maxHeight = Double.MAX_VALUE
                GridPane.setVgrow(textArea, Priority.ALWAYS)
                GridPane.setHgrow(textArea, Priority.ALWAYS)

                val expContent = GridPane()
                expContent.maxWidth = Double.MAX_VALUE
                expContent.add(label, 0, 0)
                expContent.add(textArea, 0, 1)
                alert.dialogPane.expandableContent = expContent
                alert.showAndWait()
            }
            //we repaint the current 2D (if the layout and theme have been defined directly from the script, we the current 2D needs to be updated to fit its thumbnail
            mediator.canvas2D.repaint()
        }
    }

    override fun call(): Pair<Any?, Exception?> {
        try {
            Platform.runLater {
                updateMessage("Applying theme & layout..")
            }
            Thread.sleep(100)
            val theme = mediator.scriptEditor.engine.eval(mediator.scriptEditor.getThemeAsText(useAbsolutePath= true)) as? Theme
            val layout = mediator.scriptEditor.engine.eval(mediator.scriptEditor.getLayoutAsText(useAbsolutePath= true)) as? Layout
            //println(scriptContent)
            Thread.sleep(100)
            var totalProgress = mediator.databaseExplorer.count().toDouble()
            var progressStep = 0.0
            var loaded = 0
            mediator.databaseExplorer.drawingsLoaded().forEach { drawingLoaded ->
                Platform.runLater {
                    updateProgress(++progressStep, totalProgress)
                    updateMessage("Applying theme & layout to 2D ${++loaded}/${mediator.databaseExplorer.count()}")
                    drawingLoaded.drawing.clearTheme()
                    theme?.let {
                        drawingLoaded.drawing.applyTheme(it)
                    }
                    layout?.let {
                        drawingLoaded.drawing.applyLayout(it)
                    }
                    drawingLoaded.layoutAndThemeUpdated.value = !drawingLoaded.layoutAndThemeUpdated.value
                }
                Thread.sleep(100)
            }
            return Pair(null, null)
        } catch (e: Exception) {
            return Pair(null, e)
        }
    }

}*/

