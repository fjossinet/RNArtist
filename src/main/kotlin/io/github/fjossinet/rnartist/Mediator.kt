package io.github.fjossinet.rnartist

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.EmbeddedDB
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.getImage
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
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
    val projectManager = ProjectManager(this)
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
    val secondaryStructure: SecondaryStructure?
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
                        val item = MenuItem(drawing.name)
                        item.userData = drawing
                        item.setOnAction { actionEvent ->
                            canvas2D.load2D(item.userData as SecondaryStructureDrawing)
                            canvas2D.fitDisplayOn(current2DDrawing!!.getBounds())
                        }
                        rnartist.load2DForMenu.items.add(0,item)
                    }
                } else if (change.wasRemoved()) {
                    val toDelete = mutableListOf<MenuItem>()
                    for (drawing in change.removed) {
                        for (item in rnartist.load2DForMenu.items) {
                            if (item.userData == drawing) {
                                toDelete.add(item)
                            }
                        }
                    }
                    rnartist.load2DForMenu.items.removeAll(toDelete)
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
            (this.tertiaryStructure as? TertiaryStructure)?.let { tertiaryStructure ->
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
            (this.tertiaryStructure as? TertiaryStructure)?.let { tertiaryStructure ->
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