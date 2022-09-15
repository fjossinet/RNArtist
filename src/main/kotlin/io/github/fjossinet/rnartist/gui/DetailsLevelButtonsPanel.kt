package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label

class DetailsLevelButtonsPanel(mediator: Mediator) : ButtonsPanel(mediator = mediator, panelRadius = 60.0) {

    var targetsComboBox: ComboBox<String> = ComboBox<String>()

    init {
        with(this.targetsComboBox) {
            minWidth = 150.0
            maxWidth = 150.0
            items.addAll(
                "Any",
                "Selection",
                "Helices",
                "Junctions",
                "Strands",
                "Apical Loops",
                "Inner Loops",
                "3-Way",
                "4-Way"
            )
            value = items.first()
        }

        val levelDetails1 = Button("1")
        levelDetails1.setStyle("-fx-font-weight: bold")
        levelDetails1.onAction = EventHandler {
            val t = Theme()
            when (this.targetsComboBox.value) {
                "Any" -> {
                    t.setConfigurationFor(
                        { el -> el.type != SecondaryStructureType.TertiaryInteraction }, //visibility of tertiaries are managed from the "2D actions" Panel
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Selection" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.inside(
                                Location(
                                    mediator.canvas2D.getSelection()
                                        .flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
                            )
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Helices" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Junctions" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Strands" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Apical Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Inner Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "3-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "4-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                else -> {

                }
            }
            mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("1")

        }
        this.addButton(levelDetails1)

        val levelDetails2 = Button("2")
        levelDetails2.setStyle("-fx-font-weight: bold")
        levelDetails2.onAction = EventHandler {
            val t = Theme()
            when (this.targetsComboBox.value) {
                "Any" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )

                }
                "Selection" -> {
                    val selectedLocation = Location(
                        mediator.canvas2D.getSelection()
                            .flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Helices" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Junctions" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Strands" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Apical Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Inner Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "3-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "4-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                else -> {

                }
            }
            mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("2")
        }
        this.addButton(levelDetails2)

        val levelDetails3 = Button("3")
        levelDetails3.setStyle("-fx-font-weight: bold")
        levelDetails3.onAction = EventHandler {
            val t = Theme()
            when (this.targetsComboBox.value) {
                "Any" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.X
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Selection" -> {
                    val selectedLocation = Location(
                        mediator.canvas2D.getSelection()
                            .flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.X && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Helices" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Junctions" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Strands" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Apical Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "Inner Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "3-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                "4-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "false" })
                }
                else -> {
                }
            }
            mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("3")
        }
        this.addButton(levelDetails3)

        val levelDetails4 = Button("4")
        levelDetails4.setStyle("-fx-font-weight: bold")
        levelDetails4.onAction = EventHandler {
            val t = Theme()
            when (this.targetsComboBox.value) {
                "Any" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Selection" -> {
                    val selectedLocation = Location(
                        mediator.canvas2D.getSelection()
                            .flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Helices" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "false" }
                    )
                }
                "Junctions" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "Strands" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "Apical Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "Inner Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "3-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "4-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                else -> {

                }
            }
            mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("4")
        }
        this.addButton(levelDetails4)

        val levelDetails5 = Button("5")
        levelDetails5.setStyle("-fx-font-weight: bold")
        levelDetails5.onAction = EventHandler {
            val t = Theme()
            when (this.targetsComboBox.value) {
                "Any" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                }
                "Selection" -> {
                    val selectedLocation = Location(
                        mediator.canvas2D.getSelection()
                            .flatMap { if (it is JunctionDrawing) it.junction.locationWithoutSecondaries.blocks else it.location.blocks })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.inside(selectedLocation)
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.inside(selectedLocation)
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                }
                "Helices" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Helix
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SecondaryInteraction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.InteractionSymbol && el.parent?.type == SecondaryStructureType.SecondaryInteraction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                }
                "Junctions" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "Strands" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.SingleStrand
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.SingleStrand
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "Apical Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ApicalLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "Inner Loops" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.InnerLoop
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.InnerLoop
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "3-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.ThreeWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                "4-Way" -> {
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.Junction && (el as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.PhosphodiesterBond && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.AShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.A && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.UShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.U && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.GShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.G && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.CShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.C && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                    t.setConfigurationFor(
                        { el ->
                            el.type == SecondaryStructureType.XShape && el.parent?.type == SecondaryStructureType.Junction && (el.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                        },
                        ThemeParameter.fulldetails,
                        { el -> "true" }
                    )
                    t.setConfigurationFor({ el ->
                        el.type == SecondaryStructureType.X && el.parent?.parent?.type == SecondaryStructureType.Junction && (el.parent?.parent as JunctionDrawing).junctionType == JunctionType.FourWay
                    }, ThemeParameter.fulldetails, { el -> "true" })
                }
                else -> {
                }
            }
            mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("5")
        }
        this.addButton(levelDetails5)

        this.children.add(Label("Target"))
        this.children.add(targetsComboBox)
    }

}