package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.getImage
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import java.awt.image.RenderedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class Mediator(val rnartist: RNArtist) {

    val _2DDrawingsLoaded = FXCollections.observableArrayList<SecondaryStructureDrawing>()

    val embeddedDB = EmbeddedDB()
    val settings = Settings(this)
    val explorer = Explorer(this)
    val embeddedDBGUI = EmbeddedDBGUI(this)
    val projectManager = ProjectsPanel(this)
    var webBrowser: WebBrowser? =
            WebBrowser(this)
    lateinit var canvas2D: Canvas2D
    var chimeraDriver: ChimeraDriver? = null

    //++++++ shortcuts
    var current2DDrawing: SecondaryStructureDrawing?
        set(value) {
            this.canvas2D.secondaryStructureDrawing = value
        }
        get() {
            return this.canvas2D.secondaryStructureDrawing
        }
    var tertiaryStructure: TertiaryStructure? = null
        get() {
            return this.secondaryStructure?.tertiaryStructure
        }
    private val secondaryStructure: SecondaryStructure?
        get() {
            return this.current2DDrawing?.secondaryStructure
        }
    val rna: RNA?
        get() {
            return this.secondaryStructure?.rna
        }
    val theme: Theme
        get() {
            return this.current2DDrawing?.let {
                Theme() //TODO export the current theme
            } ?: Theme()
        }
    val workingSession: WorkingSession?
        get() {
            return this.current2DDrawing?.workingSession
        }

    val viewX: Double?
        get() {
            return this.workingSession?.viewX
        }

    val viewY: Double?
        get() {
            return this.workingSession?.viewY
        }

    val finalZoomLevel: Double?
        get() {
            return this.workingSession?.finalZoomLevel
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
                        item.userData = drawing
                        item.setOnAction {
                            canvas2D.load2D(item.userData as SecondaryStructureDrawing)
                            if ((item.userData as SecondaryStructureDrawing).workingSession.viewX == 0.0 && (item.userData as SecondaryStructureDrawing).workingSession.viewY == 0.0 && (item.userData as SecondaryStructureDrawing).workingSession.finalZoomLevel == 1.0) {
                                //it seems it is a first opening, then we fit to the display
                                canvas2D.fitDisplayOn(current2DDrawing!!.getBounds())
                            }
                        }
                        var found = false
                        rnartist.allStructuresAvailableMenu.items.forEach { fileName ->
                            if (drawing.secondaryStructure.rna.source.equals(fileName.text))  {
                                (fileName as Menu).items.add(0,item)
                                found = true
                            }
                        }
                        if (!found) {
                            val menu = Menu(drawing.secondaryStructure.rna.source)
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
                current2DDrawing = null
                rnartist.clearAll2DsItem.isDisable = true
                rnartist.clearAll2DsExceptCurrentItem.isDisable = true
                canvas2D.repaint()
            }
        })

    }

    public fun focusInChimera() {
        this.chimeraDriver?.let { chimeraDriver ->
            val positions: MutableList<String> = ArrayList(1)
            this.tertiaryStructure?.let { tertiaryStructure ->
                for (absPos in workingSession!!.selectedAbsPositions) {
                    (tertiaryStructure.getResidue3DAt(absPos) as Residue3D)?.let {
                        positions.add(if (it.label != null) it.label!! else "" + (absPos + 1))
                    }
                }
                chimeraDriver.selectResidues(positions, this.secondaryStructure?.rna?.name)
                chimeraDriver.setFocus(positions, this.secondaryStructure?.rna?.name)
            }
        }
    }

    public fun pivotInChimera() {
        this.chimeraDriver?.let { chimeraDriver ->
            val positions: MutableList<String> = ArrayList(1)
            this.tertiaryStructure?.let { tertiaryStructure ->
                for (absPos in workingSession!!.selectedAbsPositions) {
                    (tertiaryStructure.getResidue3DAt(absPos) as Residue3D)?.let {
                        positions.add(if (it.label != null) it.label!! else "" + (absPos + 1))
                    }
                }
                chimeraDriver.selectResidues(positions, this.secondaryStructure?.rna?.name)
                chimeraDriver.setPivot(positions, this.secondaryStructure?.rna?.name)
            }
        }
    }

}