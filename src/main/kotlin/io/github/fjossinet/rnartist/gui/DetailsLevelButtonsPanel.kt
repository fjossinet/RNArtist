package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui.Targetable
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label

class DetailsLevelButtonsPanel(mediator: Mediator) : ButtonsPanel(mediator = mediator, panelRadius = 60.0), Targetable {

    var targetsComboBox: ComboBox<String> = ComboBox<String>()

    init {
        with(this.targetsComboBox) {
            minWidth = 150.0
            maxWidth = 150.0
            items.addAll(
                "Any",
                "Helices",
                "Junctions",
                "Strands",
                "Apical Loops",
                "Inner Loops",
                "3-Ways",
                "4-Ways"
            )
            value = items.first()

        }
        mediator.targetables.add(this)
        this.targetsComboBox.onAction = EventHandler {
            //synchronization with the other targetable widgets
            mediator.targetables.forEach { it.setTarget(this.targetsComboBox.value) }
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
                "3-Ways" -> {
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
                "4-Ways" -> {
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
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.getSelection().forEach {
                    it.applyTheme(t)
                }
            } else {
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            }
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
                "3-Ways" -> {
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
                "4-Ways" -> {
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
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.getSelection().forEach {
                    it.applyTheme(t)
                }
            } else {
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            }
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
                "3-Ways" -> {
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
                "4-Ways" -> {
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
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.getSelection().forEach {
                    it.applyTheme(t)
                }
            } else {
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            }
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
                "3-Ways" -> {
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
                "4-Ways" -> {
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
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.getSelection().forEach {
                    it.applyTheme(t)
                }
            } else {
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            }
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
                "3-Ways" -> {
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
                "4-Ways" -> {
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
            if (mediator.canvas2D.getSelection().isNotEmpty()) {
                mediator.canvas2D.getSelection().forEach {
                    it.applyTheme(t)
                }
            } else {
                mediator.drawingDisplayed.get()?.drawing?.applyTheme(t)
            }
            mediator.canvas2D.repaint()
            mediator.scriptEditor.script.setDetailsLevel("5")
        }
        this.addButton(levelDetails5)

        this.children.add(Label("Target"))
        this.children.add(targetsComboBox)
    }

    override fun setTarget(target: String) {
        if (this.targetsComboBox.items.contains(target))
            this.targetsComboBox.value = target
    }

}