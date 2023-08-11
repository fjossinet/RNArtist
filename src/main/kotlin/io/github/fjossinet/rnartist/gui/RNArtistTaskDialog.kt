package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.model.RNArtistTask
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 * A dialog displayed during a running task and displaying the progress
 */
class RNArtistTaskDialog(val mediator: Mediator) {

    val stage = Stage()
    val progressBar = ProgressBar(0.0)
    val statusLabel = Label("")
    val progressLabel = Label("")
    var task: RNArtistTask? = null
        set(value) {
            field = value
            field?.rnartistTaskDialog = this
            progressBar.progressProperty().unbind()
            progressBar.progressProperty().bind(field?.progressProperty())
            statusLabel.textProperty().unbind()
            statusLabel.textProperty().bind(field?.messageProperty())
            field?.stepProperty?.unbind()
            field?.stepProperty?.addListener { v, oldV, newV ->
                newV?.let {
                    progressLabel.text = "${it.first}/${it.second}"
                } ?: run {
                    progressLabel.text = ""
                }

            }
            Thread(field).start()
        }

    init {
        stage.initStyle(StageStyle.UNDECORATED)
        stage.initModality(Modality.APPLICATION_MODAL)
        val form = GridPane()
        val constraints = ColumnConstraints()
        constraints.hgrow = Priority.ALWAYS
        form.getColumnConstraints().addAll(constraints)

        form.hgap = 10.0
        form.vgap = 10.0
        form.padding = Insets(10.0, 10.0, 10.0, 10.0)

        stage.setScene(Scene(form));

        val im = ImageView()
        im.image = Image("/io/github/fjossinet/rnartist/io/images/taskbanner.png")
        form.add(im, 0, 0, 2, 1)
        this.progressBar.prefWidthProperty().bind(form.widthProperty().subtract(20))
        form.add(this.progressBar, 0, 1, 2, 1)

        this.statusLabel.prefWidthProperty().bind(form.widthProperty().subtract(20))
        form.add(this.statusLabel, 0, 2, 1, 1)
        form.add(this.progressLabel, 1, 2, 1, 1)
        form.layout()
        stage.show()
    }

}