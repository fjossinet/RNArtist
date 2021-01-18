package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.getImage
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import java.awt.image.RenderedImage
import java.io.File
import javax.imageio.ImageIO

class Mediator(val rnartist: RNArtist) {

    val _2DDrawingsLoaded = FXCollections.observableArrayList<SecondaryStructureDrawing>()
    var secondaryStructureDrawingProperty: SimpleObjectProperty<SecondaryStructureDrawing?> = SimpleObjectProperty<SecondaryStructureDrawing?>(null)

    var scope = RNArtist.BRANCH_SCOPE

    val embeddedDB = EmbeddedDB()
    var webBrowser = WebBrowser(this)
    var chimeraDriver = ChimeraDriver(this)
    val settings = Settings(this)
    val explorer = Explorer(this)
    val embeddedDBGUI = EmbeddedDBGUI(this)
    val projectsPanel = ProjectsPanel(this)

    lateinit var canvas2D: Canvas2D
    var rnaGallery:Map<String, List<String>>? = null

    //++++++ shortcuts
    private val secondaryStructure: SecondaryStructure?
        get() {
            return this.secondaryStructureDrawingProperty.get()?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val workingSession: WorkingSession?
        get() {
            return this.secondaryStructureDrawingProperty.get()?.workingSession
        }

    val viewX: Double?
        get() {
            return this.workingSession?.viewX
        }

    val viewY: Double?
        get() {
            return this.workingSession?.viewY
        }

    val zoomLevel: Double?
        get() {
            return this.workingSession?.zoomLevel
        }


    init {
        val img = File(File(File(embeddedDB.rootDir, "images"), "user"), "New Project.png");
        if (!img.exists())
            ImageIO.write(getImage("New Project.png") as RenderedImage, "png", img)

        this._2DDrawingsLoaded.addListener(ListChangeListener { change ->
            while (change.next()) {
                if (change.wasAdded()) {
                    for (drawing in change.addedSubList) {
                        val item = MenuItem("RNA ${drawing.secondaryStructure.rna.name} ${drawing.secondaryStructure.rna.length}nts")
                        item.setMnemonicParsing(false)
                        item.userData = drawing
                        item.setOnAction {
                            canvas2D.load2D(item.userData as SecondaryStructureDrawing)
                            if ((item.userData as SecondaryStructureDrawing).viewX == 0.0 && (item.userData as SecondaryStructureDrawing).viewY == 0.0 && (item.userData as SecondaryStructureDrawing).zoomLevel == 1.0) {
                                //it seems it is a first opening, then we fit to the display
                                canvas2D.fitStructure()
                            }
                        }
                        var found = false
                        rnartist.allStructuresAvailableMenu.items.forEach { fileName ->
                            if (drawing.secondaryStructure.rna.source.split("/") .last().equals(fileName.text))  {
                                (fileName as Menu).items.add(0,item)
                                found = true
                            }
                        }
                        if (!found) {
                            val menu = Menu(drawing.secondaryStructure.rna.source.split("/") .last())
                            menu.setMnemonicParsing(false)
                            menu.items.add(item)
                            rnartist.allStructuresAvailableMenu.items.add(0, menu)
                        }
                    }
                } else if (change.wasRemoved()) {
                    for (fileName in rnartist.allStructuresAvailableMenu.items.toList()) {
                        val toDelete = mutableListOf<MenuItem>()
                        (fileName as? Menu)?.let { menu ->
                            for (structureMenuItem in menu.items) {
                                for (drawing in change.removed) {
                                    if (structureMenuItem.userData == drawing) {
                                        toDelete.add(structureMenuItem)
                                    }
                                }
                            }
                            toDelete.forEach {
                                menu.items.remove(it)
                            }
                            if (menu.items.isEmpty())
                                rnartist.allStructuresAvailableMenu.items.remove(menu)
                        }
                    }
                }
            }
            if (!this._2DDrawingsLoaded.isEmpty()) {
                rnartist.clearAll2DsItem.isDisable = false
                rnartist.clearAll2DsExceptCurrentItem.isDisable = false
            }
            else {
                this.secondaryStructureDrawingProperty.set(null)
                rnartist.clearAll2DsItem.isDisable = true
                rnartist.clearAll2DsExceptCurrentItem.isDisable = true
                canvas2D.repaint()
            }
        })

    }

    public fun focusInChimera() {
        this.secondaryStructureDrawingProperty.get()?.let { drawing ->
            chimeraDriver.setFocus(
                canvas2D.getSelectionAbsPositions(),
                drawing.secondaryStructure.rna.name
            )
        }
    }

    public fun pivotInChimera() {
        this.secondaryStructureDrawingProperty.get()?.let { drawing ->
            chimeraDriver.setPivot(
                canvas2D.getSelectionAbsPositions(),
                drawing.secondaryStructure.rna.name
            )
        }
    }

}