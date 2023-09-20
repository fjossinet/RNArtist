package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

class AlertDialog(exception: Exception): Alert(AlertType.ERROR) {

    init {
        this.title = "Oups!!"
        this.headerText = "RNartist got a problem."
        this.contentText =
            "You can send the exception stacktrace below to fjossinet@gmail.com"
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
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
        this.dialogPane.expandableContent = expContent
        this.showAndWait()
    }
}