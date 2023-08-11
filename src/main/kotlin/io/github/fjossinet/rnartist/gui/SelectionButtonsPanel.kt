package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.DrawingElement
import io.github.fjossinet.rnartist.core.model.JunctionDrawing
import io.github.fjossinet.rnartist.core.model.JunctionType
import io.github.fjossinet.rnartist.core.model.SecondaryStructureType
import io.github.fjossinet.rnartist.gui.LinearButtonsPanel
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon

class SelectionButtonsPanel(val mediator: Mediator) : VBox() {

    val typesComboBox = ComboBox<String>()

    init {
        this.spacing = 5.0

        val buttons = LinearButtonsPanel(mediator)
        val select = Button(null, FontIcon("fas-search:15"))
        select.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            select.isDisable = newValue == null
        }
        select.setStyle("-fx-font-weight: bold")
        buttons.addButton(select)
        select.onMouseClicked = EventHandler {
            mediator.currentDrawing.get()?.let { currentDrawing ->
                when(this.typesComboBox.value) {
                    "Residues" -> currentDrawing.drawing.let {
                        it.residues.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Adenines" -> currentDrawing.drawing.let {
                        it.residues.forEach {
                            if (it.type == SecondaryStructureType.AShape)
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Uracils" -> currentDrawing.drawing.let {
                        it.residues.forEach {
                            if (it.type == SecondaryStructureType.UShape)
                                mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Guanines" -> currentDrawing.drawing.let {
                        it.residues.forEach {
                            if (it.type == SecondaryStructureType.GShape)
                                mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Cytosines" -> currentDrawing.drawing.let {
                        it.residues.forEach {
                            if (it.type == SecondaryStructureType.CShape)
                                mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Helices" -> currentDrawing.drawing.let {
                        it.allHelices.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Secondary Interactions" -> currentDrawing.drawing.let {
                        it.allHelices.forEach {
                            it.secondaryInteractions.forEach {
                                mediator.canvas2D.addToSelection(it)
                            }
                        }
                    }

                    "Phosphodiester Bonds" -> currentDrawing.drawing.let {
                        it.allPhosphoBonds.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Junctions" -> currentDrawing.drawing.let {
                        it.allJunctions.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Apical Loops" -> currentDrawing.drawing.let {
                        it.allJunctions.forEach {
                            if (it.junctionType == JunctionType.ApicalLoop)
                                mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Inner Loops" -> currentDrawing.drawing.let {
                        it.allJunctions.forEach {
                            if (it.junctionType == JunctionType.InnerLoop)
                                mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "3-Ways" -> currentDrawing.drawing.let {
                        it.allJunctions.forEach {
                            if (it.junctionType == JunctionType.ThreeWay)
                                mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "4-Ways" -> currentDrawing.drawing.let {
                        it.allJunctions.forEach {
                            if (it.junctionType == JunctionType.FourWay)
                                mediator.canvas2D.addToSelection(it)
                        }
                    }
                }
            }
        }

        val selectInSelection = Button(null, FontIcon("fas-search-plus:15"))
        selectInSelection.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            selectInSelection.isDisable = newValue == null
        }
        selectInSelection.setStyle("-fx-font-weight: bold")
        buttons.addButton(selectInSelection)
        selectInSelection.onMouseClicked = EventHandler {
            mediator.currentDrawing.get()?.let { currentDrawing ->
                val selectedElements = mutableListOf<DrawingElement>()
                mediator.canvas2D.getSelection().map {
                    selectedElements.add(it)
                    selectedElements.addAll(it.allChildren)
                }
                when(this.typesComboBox.value) {
                    "Residues" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.AShape ||  it.type == SecondaryStructureType.UShape || it.type == SecondaryStructureType.GShape || it.type == SecondaryStructureType.CShape || it.type == SecondaryStructureType.XShape}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Adenines" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.AShape}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Uracils" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.UShape}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Guanines" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.GShape}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Cytosines" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.CShape}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Helices" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.Helix}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Secondary Interactions" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.SecondaryInteraction}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Phosphodiester Bonds" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.PhosphodiesterBond}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Junctions" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.Junction}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Apical Loops" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.Junction && (it as JunctionDrawing).junctionType == JunctionType.ApicalLoop}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "Inner Loops" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.Junction && (it as JunctionDrawing).junctionType == JunctionType.InnerLoop}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "3-Ways" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.Junction && (it as JunctionDrawing).junctionType == JunctionType.ThreeWay}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }

                    "4-Ways" -> currentDrawing.drawing.let {
                        mediator.canvas2D.clearSelection()
                        selectedElements.filter { it.type == SecondaryStructureType.Junction && (it as JunctionDrawing).junctionType == JunctionType.FourWay}.forEach {
                            mediator.canvas2D.addToSelection(it)
                        }
                    }
                }
            }
        }

        val center2D = Button(null, FontIcon("fas-crosshairs:15"))
        center2D.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            center2D.isDisable = newValue == null
        }
        center2D.onMouseClicked = EventHandler { mouseEvent ->
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.centerDisplayOn(mediator.canvas2D.getSelectionFrame()!!)
        }
        center2D.tooltip = Tooltip("Center View on Selection")
        buttons.addButton(center2D)

        val fit2D = Button(null, FontIcon("fas-expand-arrows-alt:15"))
        fit2D.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            fit2D.isDisable = newValue == null
        }
        fit2D.onMouseClicked = EventHandler {
            if (mediator.canvas2D.getSelection().isNotEmpty())
                mediator.canvas2D.fitStructure(mediator.canvas2D.getSelectionFrame(), 2.0)
        }
        fit2D.tooltip = Tooltip("Fit Selection to View")
        buttons.addButton(fit2D)

        this.children.add(buttons)

        val types = listOf(
            "Residues",
            "Adenines",
            "Uracils",
            "Guanines",
            "Cytosines",
            "Helices",
            "Secondary Interactions",
            "Phosphodiester Bonds",
            "Junctions",
            "Apical Loops",
            "Inner Loops",
            "3-Ways",
            "4-Ways"
        )
        this.typesComboBox.value = "Any"
        this.typesComboBox.items.addAll(types)
        this.typesComboBox.selectionModel.select(0)
        this.typesComboBox.minWidth = 150.0
        this.typesComboBox.maxWidth = 150.0
        this.typesComboBox.onAction = EventHandler {

        }
        var hbox = HBox()
        hbox.padding = Insets(5.0, 0.0, 0.0, 0.0)
        hbox.spacing = 5.0
        hbox.alignment = Pos.CENTER_LEFT
        hbox.children.add(Label("Type"))
        hbox.children.add(typesComboBox)
        this.children.add(hbox)

    }
}