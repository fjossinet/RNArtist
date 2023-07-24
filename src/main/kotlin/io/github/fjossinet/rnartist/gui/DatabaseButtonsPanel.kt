package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.DSLElement
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.stage.DirectoryChooser
import org.kordamp.ikonli.javafx.FontIcon
import java.io.File

class DatabaseButtonsPanel(mediator:Mediator): ButtonsPanel(mediator = mediator, panelRadius = 60.0) {

    var structuralClasses: ComboBox<String> = ComboBox<String>()

    init {

        mediator.databaseButtonsPanel = this

        with(this.structuralClasses) {
            minWidth = 150.0
            maxWidth = 150.0
        }
        val chooseDatabase = Button(null, FontIcon("fas-folder-open:15"))
        chooseDatabase.onMouseClicked = EventHandler { mouseEvent ->
            val directoryChooser = DirectoryChooser()
            directoryChooser.setTitle("Select the root directory")
            directoryChooser.showDialog(null)?.let {
                mediator.databaseExplorer.currentRootDB = it.absolutePath
                structuralClasses.items.clear()
                File(mediator.databaseExplorer.currentRootDB).list { dir, name ->
                    name.endsWith(".kts")
                }.forEach {
                    structuralClasses.items.add(it.split(".").first())
                }
            }
        }
        chooseDatabase.tooltip = Tooltip("Choose database folder")
        this.addButton(chooseDatabase)

        val addRNAClass = Button(null, FontIcon("fas-plus-circle:15"))
        addRNAClass.onMouseClicked = EventHandler { mouseEvent ->
            mediator.databaseExplorer.currentRootDB?.let { currentRootDB ->
                val directoryChooser = DirectoryChooser()
                directoryChooser.setTitle("Select the a directory containing Vienna files")
                directoryChooser.initialDirectory = File(currentRootDB)
                directoryChooser.showDialog(null)?.let { dir ->
                    val script = File(File(currentRootDB), "${dir.name}.kts")
                    if (!script.exists())
                        script.createNewFile()
                    val rnartistEl = DSLElement("rnartist")
                    val pngEl = DSLElement("png")
                    pngEl.properties["width"] = "600"
                    pngEl.properties["height"] = "600"
                    rnartistEl.children.add(pngEl)
                    val buf = StringBuffer()
                    rnartistEl.dump("", buf)
                    script.writeText(buf.toString())
                    structuralClasses.items.add(dir.name)
                }
            }

        }
        addRNAClass.tooltip = Tooltip("Add a new RNA class")
        this.addButton(addRNAClass)

        this.children.add(Label("RNA Classes"))
        this.children.add(structuralClasses)


    }

}