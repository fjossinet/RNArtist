package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.*
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ComboBox

class Full2DDetailsLevelButtonsPanel(mediator: Mediator) : LinearButtonsPanel(mediator = mediator) {

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
        this.targetsComboBox.onAction = EventHandler {
        }

        val levelDetails1 = Button("1")
        levelDetails1.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            levelDetails1.isDisable = newValue == null
        }
        levelDetails1.setStyle("-fx-font-weight: bold")
        levelDetails1.onAction = EventHandler {
            mediator.currentDrawing.get()?.let { currentDrawing->
                var t = Theme()
                t.addConfiguration(
                    {  true },
                    ThemeParameter.fulldetails,
                    { el -> "false"}
                )
                currentDrawing.drawing.applyTheme(t)
                mediator.canvas2D.repaint()
                currentDrawing.rnArtistEl?.let { rnArtistEl->
                    val detailsEl = rnArtistEl.getThemeEl().getDetailsEl()
                    detailsEl.setValue(1)
                }
            }

        }
        this.addButton(levelDetails1)

        val levelDetails2 = Button("2")
        levelDetails2.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            levelDetails2.isDisable = newValue == null
        }
        levelDetails2.setStyle("-fx-font-weight: bold")
        levelDetails2.onAction = EventHandler {
            val t = Theme()
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Helix
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SecondaryInteraction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Junction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SingleStrand
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.PhosphodiesterBond
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.AShape
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.A
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.UShape
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.U
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.GShape
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.G
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.CShape
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.C
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.XShape
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.X
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.InteractionSymbol
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            mediator.currentDrawing.get()?.let { currentDrawing->
                currentDrawing.drawing.applyTheme(t)
                mediator.canvas2D.repaint()
                currentDrawing.rnArtistEl?.let { rnArtistEl->
                    val detailsEl = rnArtistEl.getThemeEl().getDetailsEl()
                    detailsEl.setValue(2)
                }
            }
        }
        this.addButton(levelDetails2)

        val levelDetails3 = Button("3")
        levelDetails3.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            levelDetails3.isDisable = newValue == null
        }
        levelDetails3.setStyle("-fx-font-weight: bold")
        levelDetails3.onAction = EventHandler {
            val t = Theme()
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Helix
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SecondaryInteraction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Junction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SingleStrand
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.PhosphodiesterBond
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.AShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.A
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.UShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.U
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.GShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.G
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.CShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.C
            }, ThemeParameter.fulldetails,{ el -> "false"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.XShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.X
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.InteractionSymbol
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            mediator.currentDrawing.get()?.let { currentDrawing->
                currentDrawing.drawing.applyTheme(t)
                mediator.canvas2D.repaint()
                currentDrawing.rnArtistEl?.let { rnArtistEl->
                    val detailsEl = rnArtistEl.getThemeEl().getDetailsEl()
                    detailsEl.setValue(3)
                }
            }
        }
        this.addButton(levelDetails3)

        val levelDetails4 = Button("4")
        levelDetails4.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            levelDetails4.isDisable = newValue == null
        }
        levelDetails4.setStyle("-fx-font-weight: bold")
        levelDetails4.onAction = EventHandler {
            val t = Theme()
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Helix
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SecondaryInteraction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Junction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SingleStrand
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.PhosphodiesterBond
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.AShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.A
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.UShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.U
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.GShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.G
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.CShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.C
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.XShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.X
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.InteractionSymbol
                },
                ThemeParameter.fulldetails,
                { el -> "false"}
            )
            mediator.currentDrawing.get()?.let { currentDrawing->
                currentDrawing.drawing.applyTheme(t)
                mediator.canvas2D.repaint()
                currentDrawing.rnArtistEl?.let { rnArtistEl->
                    val detailsEl = rnArtistEl.getThemeEl().getDetailsEl()
                    detailsEl.setValue(4)
                }
            }
        }
        this.addButton(levelDetails4)

        val levelDetails5 = Button("5")
        levelDetails5.isDisable = true
        mediator.currentDrawing.addListener {
                observableValue, oldValue, newValue ->
            levelDetails5.isDisable = newValue == null
        }
        levelDetails5.setStyle("-fx-font-weight: bold")
        levelDetails5.onAction = EventHandler {
            val t = Theme()
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Helix
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SecondaryInteraction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.Junction
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.SingleStrand
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.PhosphodiesterBond
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.AShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.A
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.UShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.U
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.GShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.G
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.CShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.C
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.XShape
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            t.addConfiguration({ el ->
                el.type == SecondaryStructureType.X
            }, ThemeParameter.fulldetails,{ el -> "true"})
            t.addConfiguration(
                { el ->
                    el.type == SecondaryStructureType.InteractionSymbol
                },
                ThemeParameter.fulldetails,
                { el -> "true"}
            )
            mediator.currentDrawing.get()?.let { currentDrawing->
                currentDrawing.drawing.applyTheme(t)
                mediator.canvas2D.repaint()
                currentDrawing.rnArtistEl?.let { rnArtistEl->
                    val detailsEl = rnArtistEl.getThemeEl().getDetailsEl()
                    detailsEl.setValue(5)
                }
            }
        }
        this.addButton(levelDetails5)
    }

}