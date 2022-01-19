package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.JunctionType
import io.github.fjossinet.rnartist.gui.editor.RNArtistScript
import io.github.fjossinet.rnartist.model.editor.*
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileFilter
import java.io.PrintWriter
import java.io.StringWriter

class SplashWindow(val mediator: Mediator) {

    val stage = Stage()

    init {
        stage.initStyle(StageStyle.UNDECORATED)

        val form = GridPane()
        val constraints = ColumnConstraints()
        constraints.hgrow = Priority.ALWAYS
        form.getColumnConstraints().addAll(constraints)

        form.hgap = 10.0
        form.vgap = 10.0
        form.padding = Insets(10.0, 10.0, 10.0, 10.0)

        stage.setScene(Scene(form));

        val im = ImageView()
        im.image = Image("/io/github/fjossinet/rnartist/io/images/logo.png")
        form.add(im, 0, 0, 3, 1)
        if (RnartistConfig.projectsFolder == null || !File(RnartistConfig.projectsFolder).exists()) {
            var projectsFolder = File(System.getProperty("user.home"), "RNArtistProjects")
            val textField = TextField(projectsFolder.absolutePath)
            textField.isEditable = false
            GridPane.setHgrow(textField, Priority.ALWAYS)
            form.add(textField, 0, 1)
            val chooseFolder = Button("Choose Folder")
            GridPane.setHgrow(chooseFolder, Priority.NEVER)
            form.add(chooseFolder, 1, 1)
            val ok = Button("Launch RNArtist")
            GridPane.setHgrow(ok, Priority.NEVER)
            val statusLabel = Label("Please choose the folder to store your RNArtist projects")
            statusLabel.prefWidthProperty().bind(form.widthProperty().subtract(20))
            form.add(statusLabel, 0, 2, 3, 1)
            chooseFolder.onAction = EventHandler { e ->
                val dir = DirectoryChooser().showDialog(stage)
                dir?.let {
                    projectsFolder = File(dir, "RNArtistProjects")
                }
            }
            ok.onAction = EventHandler { e ->
                RnartistConfig.projectsFolder = projectsFolder.absolutePath
                if (!projectsFolder.exists())
                    projectsFolder.mkdir()
                form.children.removeAll(textField, chooseFolder, ok)
                val progressBar = ProgressBar(0.0)
                progressBar.prefWidthProperty().bind(form.widthProperty().subtract(20))
                form.add(progressBar, 0, 1, 3, 1)
                form.layout()
                val task = WarmUp(mediator, stage)
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(task.progressProperty());
                statusLabel.textProperty().unbind();
                statusLabel.textProperty().bind(task.messageProperty());
                Thread(task).start();
            }
            GridPane.setHgrow(ok, Priority.NEVER)
            form.add(ok, 2, 1)
            stage.showAndWait()
        } else {
            val progressBar = ProgressBar(0.0)
            progressBar.prefWidthProperty().bind(form.widthProperty().subtract(20))
            form.add(progressBar, 0, 1, 3, 1)
            val statusLabel = Label("")
            statusLabel.prefWidthProperty().bind(form.widthProperty().subtract(20))
            form.add(statusLabel, 0, 2, 3, 1)
            form.layout()
            val task = WarmUp(mediator, stage)
            stage.show()
            progressBar.progressProperty().unbind()
            progressBar.progressProperty().bind(task.progressProperty())
            statusLabel.textProperty().unbind()
            statusLabel.textProperty().bind(task.messageProperty())
            Thread(task).start()
        }

    }

    class WarmUp(val mediator: Mediator, val stage: Stage) : Task<Pair<Map<String, List<String>>?, Exception?>>() {

        init {
            setOnSucceeded { event ->
                val result = get()
                result.first?.let {
                    stage.hide()
                    mediator.rnartist.stage.show()
                    mediator.rnartist.stage.toFront()
                    mediator.sideWindow.stage.show()
                }
                result.second?.let {
                    stage.hide()
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "I got a problem"
                    alert.headerText = "RNartist got a problem during stratup ."
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
                    mediator.rnartist.stage.show()
                    mediator.rnartist.stage.toFront()
                    mediator.sideWindow.stage.show()
                    mediator.sideWindow.stage.toFront()
                }
            }
        }

        override fun call(): Pair<Map<String, List<String>>?, Exception?> {
            try {
                if (mediator.embeddedDB.getProjects().size() != 0L && File(RnartistConfig.projectsFolder).listFiles(
                        FileFilter { it.isDirectory }).isEmpty()
                ) {
                    updateMessage("Migrating old database..")
                    for (project in this.mediator.embeddedDB.getProjects().find()) {
                        updateMessage("Migrating project ${project.get("name")}..")
                        val drawing = mediator.embeddedDB.getProject(project.id)
                        with(drawing) {
                            val layoutScript = RNArtistScript(mediator)
                            layoutScript.allowScriptInit = false
                            with(layoutScript.getScriptRoot().getLayoutKw()) {
                                addButton.fire()
                                updateMessage("Migrating layout for project ${project.get("name")}..")
                                allJunctions.forEach {
                                    if (it.junctionType != JunctionType.ApicalLoop) {
                                        with(this.getJunctionLayoutKw()) {
                                            if (it.junctionType != JunctionType.ApicalLoop) { //not possible to modify layout for apical loops
                                                setOutIds(
                                                    "\"${
                                                        it.currentLayout.map { it.toString() }
                                                            .joinToString(separator = " ")
                                                    }\"")
                                                setLocation(it.location)
                                        }
                                    }
                                }
                                }
                            }
                            updateMessage("Migrating theme for project ${project.get("name")}..")
                            val themeDoc = mediator.embeddedDB.getThemeAsJSON(project.id)
                            with(layoutScript.getScriptRoot().getThemeKw()) {
                                addButton.fire()
                                val detailsKw = searchFirst { it is DetailsKw && !it.inFinalScript } as DetailsKw
                                detailsKw.setlevel("5")
                            }
                            updateMessage("Migrating structure for project ${project.get("name")}..")
                            with(layoutScript.getScriptRoot().getSecondaryStructureKw()) {
                                addButton.fire()
                                with(getRawSSKw()) {
                                    setSecondaryStructure(secondaryStructure)
                                }
                            }
                            layoutScript.allowScriptInit = true
                            layoutScript.initScript()

                            var projectDir = File(File(RnartistConfig.projectsFolder), project.get("name") as String)
                            var i = 1
                            while (projectDir.exists()) {
                                projectDir =
                                    File(File(RnartistConfig.projectsFolder), "${project.get(" name ") as String}_$i")
                                i++
                            }
                            projectDir.mkdir()
                            var scriptFile = File(projectDir, "rnartist.kts")
                            scriptFile.createNewFile()
                            val scriptContent = StringBuilder()
                            layoutScript.getScriptRoot().dumpText(scriptContent)
                            scriptFile.writeText("import io.github.fjossinet.rnartist.core.*${System.lineSeparator()}${System.lineSeparator()} ${scriptContent.toString()}")
                            drawing.asPNG(
                                Rectangle2D.Double(0.0, 0.0, 200.0, 200.0),
                                null,
                                File(projectDir, "preview.png")
                            )
                        }
                    }
                }
                updateMessage("Checking configuration..")
                Thread.sleep(2000)
                val projects = mutableMapOf<String, MutableList<String>>()
                var step = 0.0
                do {
                    updateProgress(step++, 100.0)
                    Thread.sleep(5)
                } while (step < 100.0)
                return Pair(projects, null)
            } catch (e: Exception) {
                return Pair(null, e)
            }
        }
    }
}