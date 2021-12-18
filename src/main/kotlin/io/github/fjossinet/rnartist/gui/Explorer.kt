package io.github.fjossinet.rnartist.gui
import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.RNArtist
import io.github.fjossinet.rnartist.RNArtist.SCOPE
import io.github.fjossinet.rnartist.core.RnartistConfig
import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.gui.*
import io.github.fjossinet.rnartist.io.awtColorToJavaFX
import io.github.fjossinet.rnartist.io.javaFXToAwt
import io.github.fjossinet.rnartist.model.*
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.TreeItemPropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.util.Callback
import org.kordamp.ikonli.javafx.FontIcon
import java.util.*

class Explorer(val mediator:Mediator) {

    val stage = Stage()
    val treeTableView = TreeTableView<ExplorerItem>()
    private var lastColorClicked: LastColor? = null

    init {
        stage.title = "Explorer"
        createScene(stage)
    }

    fun refresh() {
        treeTableView.refresh()
    }

    private fun createScene(stage: Stage) {
        //this.treeTableView.setEditable(true);
        treeTableView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        //this.treeTableView.getSelectionModel().setCellSelectionEnabled(true);
        treeTableView.columnResizePolicy = TreeTableView.UNCONSTRAINED_RESIZE_POLICY
        val nameColumn = TreeTableColumn<ExplorerItem, String>(null)
        nameColumn.setCellValueFactory(TreeItemPropertyValueFactory("name"))
        nameColumn.setCellFactory(NameTreeTableCell.forTreeTableColumn(mediator))
        nameColumn.isSortable = false
        val fullDetailsColumn = TreeTableColumn<ExplorerItem, String>("Full Details")
        fullDetailsColumn.setCellValueFactory(TreeItemPropertyValueFactory("fullDetails"))
        fullDetailsColumn.setCellFactory(FullDetailsTreeTableCell.forTreeTableColumn(mediator))
        fullDetailsColumn.minWidth = 80.0
        fullDetailsColumn.maxWidth = 80.0
        fullDetailsColumn.isSortable = false
        val colorColumn = TreeTableColumn<ExplorerItem, String>("Color")
        colorColumn.isEditable = true
        colorColumn.setCellValueFactory(TreeItemPropertyValueFactory("color"))
        colorColumn.setCellFactory(ColorTreeTableCell.forTreeTableColumn(mediator))
        colorColumn.minWidth = 80.0
        colorColumn.maxWidth = 80.0
        colorColumn.isSortable = false
        val lineWidthColumn = TreeTableColumn<ExplorerItem, String>("Width")
        lineWidthColumn.userData = ThemeParameter.linewidth
        lineWidthColumn.isEditable = true
        lineWidthColumn.setCellValueFactory(TreeItemPropertyValueFactory("lineWidth"))
        lineWidthColumn.setCellFactory(LineWidthTableTreeCell.forTreeTableColumn(mediator))
        lineWidthColumn.minWidth = 80.0
        lineWidthColumn.maxWidth = 80.0
        lineWidthColumn.isSortable = false
        val lineShiftColumn = TreeTableColumn<ExplorerItem, String>("Shift")
        lineShiftColumn.userData = ThemeParameter.lineshift
        lineShiftColumn.isEditable = true
        lineShiftColumn.setCellValueFactory(TreeItemPropertyValueFactory("lineShift"))
        lineShiftColumn.setCellFactory(LineShiftTableTreeCell.forTreeTableColumn(mediator))
        lineShiftColumn.minWidth = 60.0
        lineShiftColumn.maxWidth = 60.0
        lineShiftColumn.isSortable = false
        val opacityColumn = TreeTableColumn<ExplorerItem, String>("Opacity")
        opacityColumn.userData = ThemeParameter.opacity
        opacityColumn.isEditable = true
        opacityColumn.setCellValueFactory(TreeItemPropertyValueFactory("opacity"))
        opacityColumn.setCellFactory(OpacityTableTreeCell.forTreeTableColumn(mediator))
        opacityColumn.minWidth = 80.0
        opacityColumn.maxWidth = 80.0
        opacityColumn.isSortable = false
        treeTableView.columns.addAll(colorColumn,
            lineWidthColumn,
            lineShiftColumn,
            opacityColumn,
            fullDetailsColumn,
            nameColumn)
        treeTableView.setTreeColumn(nameColumn)
        treeTableView.onMouseClicked = EventHandler {
            if (!treeTableView.selectionModel.selectedCells.isEmpty()) {
                val selectedElements: MutableList<DrawingElement?> = ArrayList()
                for (item in treeTableView.selectionModel.selectedCells) {
                    if (SecondaryStructureItem::class.java.isInstance(item.treeItem.value)) continue else if (GroupOfStructuralElements::class.java.isInstance(
                            item.treeItem.value)
                    ) for (c in item.treeItem.children) selectedElements.add(c.value.drawingElement) else selectedElements.add(
                        item.treeItem.value.drawingElement)
                }
                if (!selectedElements.isEmpty()) {
                    mediator.canvas2D.clearSelection()
                    for (e in selectedElements) mediator.canvas2D.addToSelection(e)
                    if (mediator.rnartist.centerDisplayOnSelection)
                        mediator.canvas2D.getSelectionFrame()?.let {
                            mediator.canvas2D.centerDisplayOn(it)
                        }
                    mediator.canvas2D.repaint()
                } else {
                    mediator.canvas2D.clearSelection()
                    mediator.canvas2D.repaint()
                }
            }
        }
        treeTableView.onKeyPressed = EventHandler { keyEvent ->
            if (keyEvent.code == KeyCode.UP || keyEvent.code == KeyCode.DOWN) {
                if (!treeTableView.selectionModel.selectedItems.isEmpty()) {
                    mediator.canvas2D.clearSelection()
                    for (item in treeTableView.selectionModel.selectedItems) {
                        if (SecondaryStructureItem::class.java.isInstance(item.value)) continue else if (GroupOfStructuralElements::class.java.isInstance(
                                item.value)
                        ) for (c in item.children) mediator.canvas2D.addToSelection(c.value.drawingElement) else mediator.canvas2D.addToSelection(
                            item.value.drawingElement)
                    }
                    if (mediator.rnartist.centerDisplayOnSelection)
                        mediator.canvas2D.getSelectionFrame()?.let {
                            mediator.canvas2D.centerDisplayOn(it)
                        }
                    mediator.canvas2D.repaint()
                } else if (mediator.drawingDisplayed.isNotNull.get()) //no selection
                    mediator.canvas2D.clearSelection()
            }
        }
        val sp = ScrollPane(treeTableView)
        sp.isFitToWidth = true
        sp.isFitToHeight = true
        val stackedTitledPanes = VBox()

        //SCOPE TITLEDPANE
        var pane = GridPane()
        pane.padding = Insets(10.0)
        pane.vgap = 10.0
        pane.hgap = 5.0
        var cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        pane.columnConstraints.addAll(ColumnConstraints(), cc)
        val scopeButtons = HBox()
        scopeButtons.alignment = Pos.CENTER
        scopeButtons.spacing = 5.0
        val currentElementScope = ToggleButton(null, FontIcon("fas-plus:15"))
        val up2NextDomainsScope = ToggleButton(null, FontIcon("fas-compress:15"))
        val branchesScope = ToggleButton(null, FontIcon("fas-expand:15"))
        currentElementScope.onMouseClicked = EventHandler {
            currentElementScope.isSelected = true
            mediator.scope = SCOPE.ELEMENT
            up2NextDomainsScope.isSelected = false
            branchesScope.isSelected = false
        }
        currentElementScope.tooltip = Tooltip("Restricted to Selected Element ")
        scopeButtons.children.add(currentElementScope)
        up2NextDomainsScope.onMouseClicked = EventHandler {
            up2NextDomainsScope.isSelected = true
            mediator.scope = SCOPE.STRUCTURAL_DOMAIN
            currentElementScope.isSelected = false
            branchesScope.isSelected = false
        }
        up2NextDomainsScope.tooltip = Tooltip("Restricted to Structural Domains")
        scopeButtons.children.add(up2NextDomainsScope)
        branchesScope.isSelected = true
        branchesScope.onMouseClicked = EventHandler {
            branchesScope.isSelected = true
            mediator.scope = SCOPE.BRANCH
            currentElementScope.isSelected = false
            up2NextDomainsScope.isSelected = false
        }
        branchesScope.tooltip = Tooltip("Along branches")
        scopeButtons.children.add(branchesScope)
        GridPane.setHalignment(scopeButtons, HPos.CENTER)
        pane.add(scopeButtons, 0, 0, 2, 1)
        val ignoreTertiaries = CheckBox("Ignore Tertiaries")
        ignoreTertiaries.isSelected = false
        ignoreTertiaries.onAction =
            EventHandler { mediator.ignoreTertiaries = ignoreTertiaries.isSelected }
        ignoreTertiaries.alignment = Pos.CENTER
        pane.add(ignoreTertiaries, 0, 1, 2, 1)
        val scope = TitledPane("Scope", pane)
        scope.isExpanded = true

        // COLORS TITLEDPANE
        pane = GridPane()
        pane.padding = Insets(10.0)
        pane.vgap = 5.0
        pane.hgap = 5.0
        val cc1 = ColumnConstraints()
        cc1.percentWidth = 50.0
        val cc2 = ColumnConstraints()
        cc2.percentWidth = 50.0
        pane.columnConstraints.addAll(cc1, cc2)
        val lastColorsUsed = FlowPane()
        lastColorsUsed.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        val picker = ColorPicker()
        picker.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        picker.onAction = EventHandler {
            for (item in treeTableView.selectionModel.selectedItems) mediator.explorer.setColorFrom(item,
                getHTMLColorString(javaFXToAwt(picker.value)),
                mediator.scope)
            val r = LastColor(picker.value)
            if (lastColorsUsed.children.size == 30) lastColorsUsed.children.removeAt(29)
            lastColorsUsed.children.add(0, r)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        pane.add(picker, 0, 0)
        val colorSchemesMenu = MenuButton("Schemes")
        colorSchemesMenu.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        val branchesColors = Menu("Branches")
        colorSchemesMenu.items.add(branchesColors)
        val randomColors = MenuItem("Random")
        branchesColors.items.add(randomColors)
        randomColors.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                drawing.branches.forEach { branch ->
                    val rand = Random()
                    val r = rand.nextFloat()
                    val g = rand.nextFloat()
                    val b = rand.nextFloat()
                    val c = java.awt.Color(r, g, b)
                    val configuration: MutableMap<String, Map<String, String>> = HashMap()
                    val colorConfig: MutableMap<String, String> = HashMap()
                    colorConfig[ThemeParameter.color.toString()] = getHTMLColorString(c)
                    val letterColorConfig: MutableMap<String, String> = HashMap()
                    letterColorConfig[ThemeParameter.color.toString()] =
                        getHTMLColorString(java.awt.Color.BLACK)
                    configuration[SecondaryStructureType.Helix.toString()] = colorConfig
                    configuration[SecondaryStructureType.SecondaryInteraction.toString()] = colorConfig
                    configuration[SecondaryStructureType.Junction.toString()] = colorConfig
                    configuration[SecondaryStructureType.AShape.toString()] = colorConfig
                    configuration[SecondaryStructureType.A.toString()] = letterColorConfig
                    configuration[SecondaryStructureType.UShape.toString()] = colorConfig
                    configuration[SecondaryStructureType.U.toString()] = letterColorConfig
                    configuration[SecondaryStructureType.GShape.toString()] = colorConfig
                    configuration[SecondaryStructureType.G.toString()] = letterColorConfig
                    configuration[SecondaryStructureType.CShape.toString()] = colorConfig
                    configuration[SecondaryStructureType.C.toString()] = letterColorConfig
                    configuration[SecondaryStructureType.XShape.toString()] = colorConfig
                    configuration[SecondaryStructureType.X.toString()] = letterColorConfig
                    val theme = Theme(configuration)
                    for (item in mediator.explorer.getTreeViewItemsFor(mediator.explorer.treeTableView.root,
                        branch.parent!!)) mediator.explorer.applyTheme(item, theme, SCOPE.BRANCH)
                }
                mediator.canvas2D.repaint()
                mediator.explorer.refresh()
            }
        }
        val distanceColors = MenuItem("Distance")
        branchesColors.items.add(distanceColors)
        distanceColors.onAction = EventHandler {
            mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
                drawing.branches.forEach { branch ->
                    val c = branch.parent!!.getColor()
                    val junctions = branch.junctionsFromBranch()
                    for (junction in junctions) {
                        val path = junction.pathToRoot()
                        var interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate(Color.LIGHTGRAY,
                            (path.size - 1).toDouble() / branch.maxBranchLength.toDouble()))
                        junction.drawingConfiguration.params[ThemeParameter.color.toString()] =
                            getHTMLColorString(
                                interpolatedColor)
                        for (r in junction.residues) r.drawingConfiguration.params[ThemeParameter.color.toString()] =
                            getHTMLColorString(
                                interpolatedColor)
                        interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate(Color.LIGHTGRAY,
                            (path.size - 2).toDouble() / branch.maxBranchLength.toDouble()))
                        junction.parent!!.drawingConfiguration.params[ThemeParameter.color.toString()] =
                            getHTMLColorString(interpolatedColor)
                        for (r in junction.parent!!.residues) r.drawingConfiguration.params[ThemeParameter.color.toString()] =
                            getHTMLColorString(interpolatedColor)
                    }
                }
                drawing.singleStrands.forEach { ss ->
                    ss.drawingConfiguration.params[ThemeParameter.color.toString()] =
                        getHTMLColorString(java.awt.Color.BLACK)
                    for (r in ss.residues) r.drawingConfiguration.params[ThemeParameter.color.toString()] =
                        getHTMLColorString(java.awt.Color.BLACK)
                }
                mediator.canvas2D.repaint()
                mediator.explorer.refresh()
            }
        }
        val colorSchemes = Menu("Residues...")
        colorSchemesMenu.items.add(colorSchemes)
        RnartistConfig.colorSchemes.forEach { themeName, configurations ->
            val residueThemeItem = MenuItem(themeName)
            residueThemeItem.onAction = EventHandler {
                val selection = mediator.explorer.treeTableView.selectionModel.selectedItems
                val t = Theme(configurations)
                if (!selection.isEmpty()) {
                    for (item in selection) mediator.explorer.applyTheme(item, t, mediator.scope)
                } else mediator.explorer.applyTheme(t, mediator.scope)
                mediator.canvas2D.repaint()
                mediator.explorer.refresh()
            }
            colorSchemes.items.add(residueThemeItem)
        }
        pane.add(colorSchemesMenu, 1, 0)
        pane.add(Label("Last Colors"), 0, 1, 2, 1)
        lastColorsUsed.hgap = 1.0
        lastColorsUsed.vgap = 1.0
        val firstColors: MutableSet<String> = HashSet()
        for (v in RnartistConfig.colorSchemes.values) {
            for (_v in v.values) firstColors.addAll(_v.values)
        }
        for (c in firstColors) {
            val r = LastColor(Color.web(c))
            if (lastColorsUsed.children.size == 30) lastColorsUsed.children.removeAt(29)
            lastColorsUsed.children.add(0, r)
        }
        GridPane.setHalignment(lastColorsUsed, HPos.CENTER)
        pane.add(lastColorsUsed, 0, 2, 2, 1)
        val colors = TitledPane("Colors", pane)
        colors.isExpanded = true

        // LINE WIDTH TITLEDPANE
        pane = GridPane()
        pane.padding = Insets(10.0)
        pane.vgap = 5.0
        pane.hgap = 5.0
        cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        pane.columnConstraints.addAll(ColumnConstraints(), cc)
        val lineWidth = ListView<String>()
        lineWidth.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        lineWidth.maxHeight = 200.0
        lineWidth.items.addAll("0",
            "0.25",
            "0.5",
            "0.75",
            "1.0",
            "1.25",
            "1.5",
            "1.75",
            "2.0",
            "2.5",
            "3.0",
            "3.5",
            "4.0",
            "5.0",
            "6.0",
            "7.0",
            "8.0",
            "9.0",
            "10.0")
        lineWidth.setCellFactory(ShapeCellFactory())
        lineWidth.selectionModel.selectedItemProperty().addListener { observableValue, old_val, new_val ->
            for (item in treeTableView.selectionModel.selectedItems) mediator.explorer.setLineWidthFrom(
                item,
                lineWidth.selectionModel.selectedItem,
                mediator.scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        pane.add(lineWidth, 0, 0, 2, 1)
        GridPane.setHalignment(lineWidth, HPos.CENTER)
        val linesWidth = TitledPane("Lines Width", pane)
        linesWidth.isExpanded = true

        // LINE SHIFT TITLEDPANE
        pane = GridPane()
        pane.padding = Insets(10.0)
        pane.vgap = 5.0
        pane.hgap = 5.0
        cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        pane.columnConstraints.addAll(ColumnConstraints(), cc)
        val lineShiftSlider = Slider(0.0, 10.0, 0.0)
        lineShiftSlider.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        lineShiftSlider.isShowTickLabels = true
        lineShiftSlider.isShowTickMarks = true
        lineShiftSlider.majorTickUnit = 5.0
        lineShiftSlider.minorTickCount = 1
        lineShiftSlider.isShowTickMarks = true
        lineShiftSlider.onMouseReleased = EventHandler {
            for (item in treeTableView.selectionModel.selectedItems) mediator.explorer.setLineShiftFrom(item,
                Integer.toString(
                    lineShiftSlider.value.toInt()),
                mediator.scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        pane.add(lineShiftSlider, 0, 0, 2, 1)
        GridPane.setHalignment(lineShiftSlider, HPos.CENTER)
        val linesShift = TitledPane("Lines Shift", pane)
        linesShift.isExpanded = false

        // OPACITY TITLEDPANE
        pane = GridPane()
        pane.padding = Insets(10.0)
        pane.vgap = 5.0
        pane.hgap = 5.0
        cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        pane.columnConstraints.addAll(ColumnConstraints(), cc)
        val opacitySlider = Slider(0.0, 100.0, 100.0)
        opacitySlider.disableProperty()
            .bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        opacitySlider.isShowTickLabels = true
        opacitySlider.isShowTickMarks = true
        opacitySlider.majorTickUnit = 25.0
        opacitySlider.minorTickCount = 5
        opacitySlider.isShowTickMarks = true
        opacitySlider.onMouseReleased = EventHandler {
            val opacity = (opacitySlider.value / 100.0 * 255.0).toInt()
            for (item in treeTableView.selectionModel.selectedItems) mediator.explorer.setOpacityFrom(item,
                Integer.toString(opacity),
                mediator.scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        pane.add(opacitySlider, 0, 0, 2, 1)
        GridPane.setHalignment(opacitySlider, HPos.CENTER)
        val opacity = TitledPane("Opacity", pane)
        opacity.isExpanded = false

        // DETAILS TITLEDPANE
        pane = GridPane()
        pane.padding = Insets(10.0)
        pane.vgap = 5.0
        pane.hgap = 5.0
        cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        pane.columnConstraints.addAll(ColumnConstraints(), cc)
        val hbox = HBox()
        hbox.spacing = 5.0
        var b = Button(null, FontIcon("fas-eye:15"))
        b.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        b.onAction = EventHandler {
            for (item in treeTableView.selectionModel.selectedItems) mediator.explorer.setFullDetailsFrom(item,
                "true",
                mediator.scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        hbox.children.add(b)
        b = Button(null, FontIcon("fas-eye-slash:15"))
        b.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        b.onAction = EventHandler {
            for (item in treeTableView.selectionModel.selectedItems) mediator.explorer.setFullDetailsFrom(item,
                "false",
                mediator.scope)
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        hbox.children.add(b)
        hbox.alignment = Pos.CENTER
        pane.add(hbox, 0, 0, 2, 1)
        GridPane.setHalignment(hbox, HPos.CENTER)
        val details = TitledPane("Full Details", pane)
        details.isExpanded = true

        // SELECTION TITLEDPANE
        pane = GridPane()
        pane.padding = Insets(10.0)
        pane.vgap = 5.0
        pane.hgap = 5.0
        cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        pane.columnConstraints.addAll(ColumnConstraints(), cc)
        val selection = TitledPane("Selection", pane)
        selection.isExpanded = true
        val elementsToSelect = ListView<String>()
        elementsToSelect.maxHeight = 200.0
        elementsToSelect.items.addAll("Helices",
            "SingleStrands",
            "Junctions",
            "Apical Loops",
            "Inner Loops",
            "PseudoKnots",
            "Secondary Interactions",
            "Tertiary Interactions",
            "Interaction Symbols",
            "Phosphodiester Bonds",
            "Residues",
            "As",
            "Us",
            "Gs",
            "Cs",
            "Xs",
            "Residue Letters")
        pane.add(elementsToSelect, 0, 0, 2, 1)
        GridPane.setHalignment(elementsToSelect, HPos.CENTER)
        b = Button(null, FontIcon("fas-search:15"))
        b.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        b.onAction = EventHandler {
            val starts: MutableList<TreeItem<ExplorerItem>> = ArrayList()
            if (treeTableView.selectionModel.isEmpty) starts.add(mediator.explorer.treeTableView.root) else starts.addAll(
                treeTableView.selectionModel.selectedItems)
            when (elementsToSelect.selectionModel.selectedItem) {
                "Helices" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return  HelixDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "SingleStrands" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return  SingleStrandDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Junctions" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return  JunctionDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Apical Loops" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return JunctionDrawing::class.java.isInstance(el) && (el as? JunctionDrawing)?.junctionType === JunctionType.ApicalLoop
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Inner Loops" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return JunctionDrawing::class.java.isInstance(el) && (el as? JunctionDrawing)?.junctionType === JunctionType.InnerLoop
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "PseudoKnots" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return PKnotDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Secondary Interactions" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return SecondaryInteractionDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Tertiary Interactions" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return TertiaryInteractionDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Interaction Symbols" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return InteractionSymbolDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Phosphodiester Bonds" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return PhosphodiesterBondDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Residues" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return ResidueDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "As" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return AShapeDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Us" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return UShapeDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Gs" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return GShapeDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Cs" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return CShapeDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
                "Residue Letters" -> mediator.explorer.selectAllTreeViewItems( object : DrawingElementFilter {
                    override fun isOK(el: DrawingElement?): Boolean {
                        return ResidueLetterDrawing::class.java.isInstance(el)
                    }
                }, starts, true, RNArtist.SCOPE.BRANCH)
            }
            mediator.explorer.refresh()
            mediator.canvas2D.repaint()
        }
        pane.add(b, 0, 1)
        GridPane.setHalignment(b, HPos.LEFT)
        val l = Label("Search from Selection")
        l.disableProperty().bind(Bindings.`when`(mediator.drawingDisplayed.isNull).then(true).otherwise(false))
        pane.add(l, 1, 1)
        stackedTitledPanes.children.add(selection)
        stackedTitledPanes.children.add(scope)
        stackedTitledPanes.children.add(details)
        stackedTitledPanes.children.add(colors)
        stackedTitledPanes.children.add(linesWidth)
        stackedTitledPanes.children.add(linesShift)
        stackedTitledPanes.children.add(opacity)
        val splitPane = SplitPane()
        splitPane.setDividerPosition(0, 0.25)
        splitPane.padding = Insets(5.0)
        val sp2 = ScrollPane(stackedTitledPanes)
        SplitPane.setResizableWithParent(sp2, false)
        sp2.isFitToWidth = true
        splitPane.items.addAll(sp2, sp)
        val scene = Scene(splitPane)
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        val width = (screenSize.width * 4.0 / 10.0).toInt()
        scene.window.width = width.toDouble()
        scene.window.height = screenSize.height
        scene.window.x = screenSize.width - width
        scene.window.y = 0.0
    }

    private fun applyTheme(theme: Theme, scope: SCOPE) {
        this.applyTheme(treeTableView.root, theme, scope)
    }

    fun applyTheme(item: TreeItem<ExplorerItem>, theme: Theme, scope: SCOPE) {
        if (mediator.ignoreTertiaries && item.value.name == "Tertiaries") return
        item.value.applyTheme(theme)
        if (scope === SCOPE.ELEMENT) return
        for (c in item.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(item.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    c.value.drawingElement)
            ) continue
            applyTheme(c, theme, scope)
        }
    }

    fun applyAdvancedTheme(item: TreeItem<ExplorerItem>, theme: AdvancedTheme, scope: SCOPE) {
        if (mediator.ignoreTertiaries && item.value.name == "Tertiaries") return
        item.value.applyAdvancedTheme(theme)
        if (scope === SCOPE.ELEMENT) return
        for (c in item.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(item.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    c.value.drawingElement)
            ) continue
            applyAdvancedTheme(c, theme, scope)
        }
    }

    /**
     * Return a list of TreeItem containing a DrawingElement. Can return several objects since a DrawingElement can be encapsulated at different places
     * (like an helix and its children in a pknot or residues in tertiary interactions
     * @param item
     * @param drawingElement
     * @return
     */
    private fun getTreeViewItemsFor(
        item: TreeItem<ExplorerItem>,
        drawingElement: DrawingElement,
    ): List<TreeItem<ExplorerItem>> {
        val items: MutableList<TreeItem<ExplorerItem>> = ArrayList()
        if (SecondaryStructureDrawing::class.java.isInstance(drawingElement)) { //its the root element
            items.add(treeTableView.root)
            return items
        }
        if (item.value.drawingElement === drawingElement) {
            items.add(item)
            return items
        }
        for (child in item.children) items.addAll(getTreeViewItemsFor(child, drawingElement))
        return items
    }

    private fun getAllTreeViewItemsFrom(item: TreeItem<ExplorerItem>): List<TreeItem<ExplorerItem>> {
        val hits: MutableList<TreeItem<ExplorerItem>> = ArrayList()
        hits.add(item)
        for (c in item.children) hits.addAll(getAllTreeViewItemsFrom(c))
        return hits
    }

    fun selectAllTreeViewItems(
        filter: DrawingElementFilter,
        starts: List<TreeItem<ExplorerItem>>,
        updateCanvas: Boolean,
        scope: SCOPE,
    ) {
        val hits: MutableList<TreeItem<ExplorerItem>> = ArrayList()
        clearSelection() //we don't want in the selection the element we used at first to launch the selection (we selected an Helix to select all Residues -> we don't need/want the helix anymore in the selection)
        for (start in starts) getAllTreeViewItems(hits, start, filter, scope)
        for (hit in hits) {
            var p: TreeItem<*>? = hit.parent
            while (p != null) {
                p.isExpanded = true
                p = p.parent
            }
        }
        for (hit in hits) treeTableView.selectionModel.select(hit)
        if (updateCanvas) {
            mediator.canvas2D.clearSelection()
            for (hit in hits) mediator.canvas2D.addToSelection(hit.value.drawingElement)
        }
    }

    private fun getAllTreeViewItems(
        hits: MutableList<TreeItem<ExplorerItem>>?,
        start: TreeItem<ExplorerItem>,
        filter: DrawingElementFilter,
        scope: SCOPE,
    ): List<TreeItem<ExplorerItem>> {
        var hits = hits
        if (hits == null) hits = ArrayList()
        if (mediator.ignoreTertiaries && start.value.name == "Tertiaries") return hits
        if (filter.isOK(start.value.drawingElement)) hits.add(start)
        if (scope === SCOPE.ELEMENT) return hits
        for (child in start.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(start.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    child.value.drawingElement)
            ) continue
            getAllTreeViewItems(hits, child, filter, scope)
        }
        return hits
    }

    fun clearSelection() {
        treeTableView.selectionModel.clearSelection()
    }

    private fun setFullDetailsFrom(from: TreeItem<ExplorerItem>, fullDetails: String, scope: SCOPE) {
        if (mediator.ignoreTertiaries && from.value.name == "Tertiaries") return
        from.value.fullDetails = fullDetails
        if (scope === SCOPE.ELEMENT) return
        for (child in from.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(from.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    child.value.drawingElement)
            ) continue
            setFullDetailsFrom(child, fullDetails, scope)
        }
    }

    fun setColorFrom(from: TreeItem<ExplorerItem>, color: String, scope: SCOPE) {
        if (mediator.ignoreTertiaries && from.value.name == "Tertiaries") return
        from.value.color = color
        if (scope === SCOPE.ELEMENT) return
        for (child in from.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(from.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    child.value.drawingElement)
            ) continue
            setColorFrom(child, color, scope)
        }
    }

    private fun setLineWidthFrom(from: TreeItem<ExplorerItem>, width: String, scope: SCOPE) {
        if (mediator.ignoreTertiaries && from.value.name == "Tertiaries") return
        from.value.lineWidth = width
        if (scope === SCOPE.ELEMENT) return
        for (child in from.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(from.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    child.value.drawingElement)
            ) continue
            setLineWidthFrom(child, width, scope)
        }
    }

    private fun setLineShiftFrom(from: TreeItem<ExplorerItem>, shift: String, scope: SCOPE) {
        if (mediator.ignoreTertiaries && from.value.name == "Tertiaries") return
        from.value.lineShift = shift
        if (scope === SCOPE.ELEMENT) return
        for (child in from.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(from.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    child.value.drawingElement)
            ) continue
            setLineShiftFrom(child, shift, scope)
        }
    }

    private fun setOpacityFrom(from: TreeItem<ExplorerItem>, opacity: String, scope: SCOPE) {
        if (mediator.ignoreTertiaries && from.value.name == "Tertiaries") return
        from.value.opacity = opacity
        if (scope === SCOPE.ELEMENT) return
        for (child in from.children) {
            if (!GroupOfStructuralElements::class.java.isInstance(from.value) && scope === SCOPE.STRUCTURAL_DOMAIN && StructuralDomainDrawing::class.java.isInstance(
                    child.value.drawingElement)
            ) continue
            setOpacityFrom(child, opacity, scope)
        }
    }

    //++++++++++ methods to construct the treetable

    //++++++++++ methods to construct the treetable
    private val helicesAlreadyInPknots: MutableList<TreeItem<ExplorerItem>> = ArrayList()
    private val residuesAlreadyInTertiaries: MutableList<TreeItem<ExplorerItem>> = ArrayList()
    private val alreadyTertiaries: MutableList<TreeItem<ExplorerItem>> = ArrayList()

    fun load(drawing: SecondaryStructureDrawing) {
        //we fit the width of the Name column to have enough space to expand the nodes
        var max = 0
        for (b in drawing.branches) {
            if (b.maxBranchLength > max) max = b.maxBranchLength
        }
        treeTableView.columns[treeTableView.columns.size - 1].minWidth = (50 * max).toDouble()
        treeTableView.columns[treeTableView.columns.size - 1].maxWidth = (60 * max).toDouble()
        helicesAlreadyInPknots.clear()
        residuesAlreadyInTertiaries.clear()
        val root = TreeItem<ExplorerItem>(SecondaryStructureItem(drawing))
        root.isExpanded = true
        val pknotsGroup = GroupOfStructuralElements("Pseudoknots")
        val pknotsTreeItem = TreeItem<ExplorerItem>(pknotsGroup)
        root.children.add(pknotsTreeItem)
        for (pknot in drawing.pknots) {
            val pknotTreeItem = this.load(pknot)
            pknotsTreeItem.children.add(pknotTreeItem)
            pknotsGroup.children.add(pknotTreeItem.value)
        }
        val branchesGroup = GroupOfStructuralElements("Branches")
        val allBranches = TreeItem<ExplorerItem>(branchesGroup)
        root.children.add(allBranches)
        for (j in drawing.branches) {
            val branchTreeItem = this.load(j)
            allBranches.children.addAll(branchTreeItem)
            branchesGroup.children.add(branchTreeItem.value)
        }
        val singleStrandsGroup = GroupOfStructuralElements("SingleStrands")
        val allSingleStrands = TreeItem<ExplorerItem>(singleStrandsGroup)
        root.children.add(allSingleStrands)
        for (ss in drawing.singleStrands) {
            val singleStrandTreeItem = this.load(ss)
            allSingleStrands.children.addAll(singleStrandTreeItem)
            singleStrandsGroup.children.add(singleStrandTreeItem.value)
        }
        val phosphoGroup = GroupOfStructuralElements("Phosphodiester Bonds")
        val allphosphos = TreeItem<ExplorerItem>(phosphoGroup)
        root.children.add(allphosphos)
        for (p in drawing.phosphoBonds) {
            val phosphoTreeItem = this.load(p)
            allphosphos.children.addAll(phosphoTreeItem)
            phosphoGroup.children.add(phosphoTreeItem.value)
        }
        treeTableView.root = root

        //cleaning
        helicesAlreadyInPknots.clear()
        residuesAlreadyInTertiaries.clear()
    }


    //make a tree of TreeItems containing the same ExplorerItems. This allows to synchronize two different tree items targeting the same drawing elements (Residues betwenn secondaries and tertiaries, helices between junctions and pknots)
    private fun copy(toCopy: TreeItem<ExplorerItem>): TreeItem<ExplorerItem> {
        val copy = TreeItem(toCopy.value)
        for (c in toCopy.children) copy.children.add(copy(c))
        return copy
    }

    private fun load(pknot: PKnotDrawing): TreeItem<ExplorerItem> {
        val pknotItem = TreeItem<ExplorerItem>(PknotItem(pknot))
        pknotItem.children.add(this.load(pknot.helix))
        helicesAlreadyInPknots.add(copy(pknotItem.children[0])) //a copy for the helix in the branches
        val tertiariesGroup = GroupOfStructuralElements("Tertiaries")
        val allTertiaries = TreeItem<ExplorerItem>(tertiariesGroup)
        for (interaction in pknot.tertiaryInteractions) {
            val tertiaryTreeItem = this.load(interaction, true)
            allTertiaries.children.addAll(tertiaryTreeItem)
            tertiariesGroup.children.add(tertiaryTreeItem.value)
        }
        pknotItem.children.add(allTertiaries)
        return pknotItem
    }

    private fun load(h: HelixDrawing): TreeItem<ExplorerItem> {
        val helixItem = TreeItem<ExplorerItem>(HelixItem(h))
        val secondariesGroup = GroupOfStructuralElements("Secondaries")
        val secondaries = TreeItem<ExplorerItem>(secondariesGroup)
        helixItem.children.add(secondaries)
        for (interaction in h.secondaryInteractions) {
            val secondaryTreeItem = this.load(interaction, false)
            secondaries.children.addAll(secondaryTreeItem)
            secondariesGroup.children.add(secondaryTreeItem.value)
        }
        val tertiariesGroup = GroupOfStructuralElements("Tertiaries")
        val tertiariesTreeItem = TreeItem<ExplorerItem>(tertiariesGroup)
        helixItem.children.add(tertiariesTreeItem)
        for (interaction in h.ssDrawing.allTertiaryInteractions) if (h.residues.contains(interaction.residue) || h.residues.contains(
                interaction.pairedResidue)
        ) {
            val tertiaryTreeItem = this.load(interaction, true)
            tertiariesTreeItem.children.addAll(tertiaryTreeItem)
            tertiariesGroup.children.add(tertiaryTreeItem.value)
        }
        val phosphoGroup = GroupOfStructuralElements("Phosphodiester Bonds")
        val allPhosphos = TreeItem<ExplorerItem>(phosphoGroup)
        helixItem.children.add(allPhosphos)
        for (p in h.phosphoBonds) {
            val phosphoTreeItem = this.load(p)
            allPhosphos.children.add(phosphoTreeItem)
            phosphoGroup.children.add(phosphoTreeItem.value)
        }
        return helixItem
    }

    private fun load(jc: JunctionDrawing): TreeItem<ExplorerItem> {
        val junctionItem = TreeItem<ExplorerItem>(JunctionItem(jc))
        val residuesGroup = GroupOfStructuralElements("Residues")
        val residues = TreeItem<ExplorerItem>(residuesGroup)
        junctionItem.children.add(residues)
        for (r in jc.residues) if (r.parent === jc) {
            var found = false
            for (_r in residuesAlreadyInTertiaries) {
                if (r == _r.value.drawingElement) {
                    residues.children.addAll(_r)
                    residuesGroup.children.add(_r.value)
                    found = true
                    break
                }
            }
            if (!found) {
                val residueTreeItem = load(r)
                residues.children.addAll(residueTreeItem)
                residuesGroup.children.add(residueTreeItem.value)
            }
        }
        val tertiariesGroup = GroupOfStructuralElements("Tertiaries")
        val tertiariesTreeItem = TreeItem<ExplorerItem>(tertiariesGroup)
        junctionItem.children.add(tertiariesTreeItem)
        for (interaction in jc.ssDrawing.allTertiaryInteractions) if (jc.residues.contains(interaction.residue) || jc.residues.contains(
                interaction.pairedResidue)
        ) {
            val tertiaryTreeItem = this.load(interaction, true)
            tertiariesTreeItem.children.addAll(tertiaryTreeItem)
            tertiariesGroup.children.add(tertiaryTreeItem.value)
        }
        val phosphoGroup = GroupOfStructuralElements("Phosphodiester Bonds")
        val allPhosphos = TreeItem<ExplorerItem>(phosphoGroup)
        junctionItem.children.add(allPhosphos)
        for (p in jc.phosphoBonds) {
            val phosphoTreeItem = load(p)
            allPhosphos.children.add(phosphoTreeItem)
            phosphoGroup.children.add(phosphoTreeItem.value)
        }
        for (connectedJC in jc.connectedJunctions.values) junctionItem.children.add(this.load(connectedJC))
        for (h in helicesAlreadyInPknots) {
            if (h.value.drawingElement == jc.parent) {
                h.children.add(junctionItem)
                return h
            }
        }
        val helixItem = load(jc.parent as HelixDrawing)
        helixItem.children.add(junctionItem)
        return helixItem
    }

    private fun load(ss: SingleStrandDrawing): TreeItem<ExplorerItem> {
        val ssItem = TreeItem<ExplorerItem>(SingleStrandItem(ss))
        val residuesGroup = GroupOfStructuralElements("Residues")
        val residues = TreeItem<ExplorerItem>(residuesGroup)
        ssItem.children.add(residues)
        for (r in ss.residues) {
            if (r.parent === ss) {
                var found = false
                for (_r in residuesAlreadyInTertiaries) {
                    if (r == _r.value.drawingElement) {
                        residues.children.add(_r)
                        residuesGroup.children.add(_r.value)
                        found = true
                        break
                    }
                }
                if (!found) {
                    val residueTreeItem = load(r)
                    residues.children.add(residueTreeItem)
                    residuesGroup.children.add(residueTreeItem.value)
                }
            }
        }
        val tertiariesGroup = GroupOfStructuralElements("Tertiaries")
        val tertiariesTreeItem = TreeItem<ExplorerItem>(tertiariesGroup)
        ssItem.children.add(tertiariesTreeItem)
        for (interaction in ss.ssDrawing.allTertiaryInteractions) if (ss.residues.contains(interaction.residue) || ss.residues.contains(
                interaction.pairedResidue)
        ) {
            val tertiaryTreeItem = this.load(interaction, true)
            tertiariesTreeItem.children.addAll(tertiaryTreeItem)
            tertiariesGroup.children.add(tertiaryTreeItem.value)
        }
        val phosphoGroup = GroupOfStructuralElements("Phosphodiester Bonds")
        val allPhosphos = TreeItem<ExplorerItem>(phosphoGroup)
        ssItem.children.add(allPhosphos)
        for (p in ss.phosphoBonds) {
            val phosphoTreeItem = this.load(p)
            allPhosphos.children.add(phosphoTreeItem)
            phosphoGroup.children.add(phosphoTreeItem.value)
        }
        return ssItem
    }

    private fun load(interaction: BaseBaseInteractionDrawing, isTertiary: Boolean): TreeItem<ExplorerItem> {
        if (isTertiary) for (already in alreadyTertiaries) {
            if (interaction == already.value.drawingElement) {
                return copy(already)
            }
        }
        val interactionItem =
            if (isTertiary) TreeItem<ExplorerItem>(TertiaryInteractionItem((interaction as TertiaryInteractionDrawing))) else TreeItem<ExplorerItem>(
                SecondaryInteractionItem(
                    (interaction as SecondaryInteractionDrawing)))
        val interactionSymbolItem = TreeItem<ExplorerItem>(InteractionSymbolItem(interaction.interactionSymbol))
        interactionItem.children.add(interactionSymbolItem)
        if (isTertiary) {
            var found = false
            for (r in residuesAlreadyInTertiaries) {
                if (interaction.residue == r.value.drawingElement) {
                    interactionItem.children.add(r)
                    residuesAlreadyInTertiaries.add(copy(r)) //we add a new copy if needed for an other interaction
                    found = true
                    break
                }
            }
            if (!found) {
                val r = load(interaction.residue)
                residuesAlreadyInTertiaries.add(copy(r))
                interactionItem.children.add(r)
            }
            found = false
            for (r in residuesAlreadyInTertiaries) {
                if (interaction.pairedResidue == r.value.drawingElement) {
                    interactionItem.children.add(r)
                    residuesAlreadyInTertiaries.add(copy(r)) //we add a new copy if needed for an other interaction
                    found = true
                    break
                }
            }
            if (!found) {
                val r = load(interaction.pairedResidue)
                residuesAlreadyInTertiaries.add(copy(r))
                interactionItem.children.add(r)
            }
        } else {
            var foundR = false
            var foundPaired = false
            for (r in residuesAlreadyInTertiaries) {
                if (interaction.residue == r.value.drawingElement) {
                    interactionItem.children.add(r)
                    foundR = true
                } else if (interaction.pairedResidue == r.value.drawingElement) {
                    interactionItem.children.add(r)
                    foundPaired = true
                }
                if (foundR && foundPaired) break
            }
            if (!foundR) {
                val r = load(interaction.residue)
                interactionItem.children.add(r)
            }
            if (!foundPaired) {
                val r = load(interaction.pairedResidue)
                interactionItem.children.add(r)
            }
        }
        if (isTertiary) alreadyTertiaries.add(interactionItem)
        return interactionItem
    }

    private fun load(r: ResidueDrawing): TreeItem<ExplorerItem> {
        val residueItem = TreeItem<ExplorerItem>(ResidueItem(r))
        val residueLetterItem = TreeItem<ExplorerItem>(ResidueLetterItem(r.residueLetter))
        residueItem.children.add(residueLetterItem)
        return residueItem
    }

    private fun load(phospho: PhosphodiesterBondDrawing): TreeItem<ExplorerItem> {
        return TreeItem(PhosphodiesterItem(phospho))
    }

    interface DrawingElementFilter {
        fun isOK(el: DrawingElement?): Boolean
    }

    private fun getLineShape(lineWidth: String): Node? {
        var node: Node?
        when (lineWidth.toLowerCase()) {
            "0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 0.0
            }
            "0.25" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 0.25
            }
            "0.5" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 0.5
            }
            "0.75" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 0.75
            }
            "1.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 1.0
            }
            "1.25" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 1.25
            }
            "1.5" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 1.5
            }
            "1.75" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 1.75
            }
            "2.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 2.0
            }
            "2.5" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 2.5
            }
            "3.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 3.0
            }
            "3.5" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 3.5
            }
            "4.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 4.0
            }
            "5.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 5.0
            }
            "6.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 6.0
            }
            "7.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 7.0
            }
            "8.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 8.0
            }
            "9.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 9.0
            }
            "10.0" -> {
                node = Line(0.0, 10.0, 100.0, 10.0)
                node.strokeWidth = 10.0
            }
            else -> node = null
        }
        return node
    }

    private inner class LastColor (val color: Color) :
        Rectangle(20.0, 20.0, color) {
        init {
            style = "-fx-stroke: " + getHTMLColorString(javaFXToAwt(color)) + " ;-fx-stroke-width: 2;"
            this.onMouseClicked = EventHandler {
                lastColorClicked?.let { lastColorClicked ->
                    lastColorClicked.setStyle("-fx-stroke: " + getHTMLColorString(javaFXToAwt(
                        lastColorClicked.color)) + " ;-fx-stroke-width: 2;")
                }

                lastColorClicked = this@LastColor
                lastColorClicked!!.setStyle("-fx-stroke: black; -fx-stroke-width: 2;")
                for (item in treeTableView.getSelectionModel().getSelectedItems()) mediator.explorer.setColorFrom(item,
                    getHTMLColorString(javaFXToAwt(
                        color)),
                    mediator.scope)
                mediator.explorer.refresh()
                mediator.canvas2D.repaint()
            }
        }
    }

    private inner class ShapeCellFactory :
        Callback<ListView<String?>?, ListCell<String>> {
        override fun call(listview: ListView<String?>?): ListCell<String> {
            return ShapeCell()
        }
    }

    private inner class ShapeCell : ListCell<String>() {
        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty) {
                text = null
                graphic = null
            } else {
                text = item
                graphic = getLineShape(item!!)
            }
        }
    }
}

internal class FullDetailsTreeTableCell<T>(private val mediator: Mediator) : TreeTableCell<T, String>() {
    protected override fun updateItem(value: String?, empty: Boolean) {
        super.updateItem(value, empty)
        text = null
        graphic = null
        if (!empty) {
            var icon: FontIcon? = null
            if (value === "true") icon = FontIcon("fas-eye:15") else if (value === "false") icon =
                FontIcon("fas-eye-slash:15")
            if (icon != null) {
                val l = Label(null, icon)
                graphic = l
                alignment = Pos.CENTER
            }
        }
    }

    companion object {
        fun <T> forTreeTableColumn(mediator: Mediator): Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {
            return Callback { tableColumn: TreeTableColumn<T, String> ->
                FullDetailsTreeTableCell<T>(mediator)
            }
        }
    }
}

class ColorTreeTableCell<T>(private val mediator: Mediator) : TreeTableCell<T, String>() {
    private val graphic: Region
    protected override fun updateItem(value: String?, empty: Boolean) {
        super.updateItem(value, empty)
        text = null
        setGraphic(null)
        if (!empty && value != null && !value.isEmpty()) {
            text = null
            var style: String? = null
            if (value != null && !empty) {
                style =
                    String.format("-fx-background-color: %s ; -fx-border-width: 1; -fx-border-color: dimgrey;", value)
                graphic.style = style
                setGraphic(graphic)
                alignment = Pos.CENTER
            }
        }
    }

    companion object {
        fun <T> forTreeTableColumn(mediator: Mediator): Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {
            return Callback { tableColumn: TreeTableColumn<T, String> ->
                ColorTreeTableCell<T>(mediator)
            }
        }
    }

    init {
        this.graphic = Region()
        this.graphic.maxHeight = 15.0
    }
}

class LineShiftTableTreeCell<T>(private val mediator: Mediator) : TreeTableCell<T, String>() {
    private val slider = Slider()

    protected override fun updateItem(value: String?, empty: Boolean) {
        super.updateItem(value, empty)
        text = null
        graphic = null
        if (!empty && value != null && !value.isEmpty()) {
            text = value
            alignment = Pos.CENTER
        }
    }

    companion object {
        fun <T> forTreeTableColumn(mediator: Mediator): Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {
            return Callback { tableColumn: TreeTableColumn<T, String> ->
                LineShiftTableTreeCell<T>(mediator)
            }
        }
    }

    init {
        slider.min = 0.0
        slider.max = 255.0
    }
}

class LineWidthTableTreeCell<T>(private val mediator: Mediator) : TreeTableCell<T, String>() {
    protected override fun updateItem(value: String?, empty: Boolean) {
        super.updateItem(value, empty)
        text = null
        graphic = null
        if (!empty && value != null && !value.isEmpty()) {
            val item = treeTableRow.item as ExplorerItem
            if (item != null) {
                text = value
                graphic = this@LineWidthTableTreeCell.getShape(item.lineWidth)
                alignment = Pos.CENTER
            }
        }
    }

    private fun getShape(lineWidth: String): Node? {
        var node: Node? = null
        when (lineWidth.toLowerCase()) {
            "0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 0.0
            }
            "0.25" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 0.25
            }
            "0.5" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 0.5
            }
            "0.75" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 0.75
            }
            "1.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 1.0
            }
            "1.25" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 1.25
            }
            "1.5" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 1.5
            }
            "1.75" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 1.75
            }
            "2.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 2.0
            }
            "2.5" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 2.5
            }
            "3.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 3.0
            }
            "3.5" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 3.5
            }
            "4.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 4.0
            }
            "5.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 5.0
            }
            "6.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 6.0
            }
            "7.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 7.0
            }
            "8.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 8.0
            }
            "9.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 9.0
            }
            "10.0" -> {
                node = Line(0.0, 10.0, 20.0, 10.0)
                node.strokeWidth = 10.0
            }
            else -> node = null
        }
        return node
    }

    companion object {
        fun <T> forTreeTableColumn(mediator: Mediator): Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {
            return Callback { tableColumn: TreeTableColumn<T, String> ->
                LineWidthTableTreeCell<T>(mediator)
            }
        }
    }
}

class NameTreeTableCell<T>(private val mediator: Mediator) : TreeTableCell<T, String>() {
    protected override fun updateItem(value: String?, empty: Boolean) {
        super.updateItem(value, empty)
        text = null
        graphic = null
        if (!empty) {
            text = value
        }
    }

    companion object {
        fun <T> forTreeTableColumn(mediator: Mediator): Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {
            return Callback { tableColumn: TreeTableColumn<T, String> ->
                NameTreeTableCell<T>(mediator)
            }
        }
    }
}

class OpacityTableTreeCell<T>(private val mediator: Mediator) : TreeTableCell<T, String>() {
    protected override fun updateItem(value: String?, empty: Boolean) {
        super.updateItem(value, empty)
        text = null
        graphic = null
        if (!empty && value != null && !value.isEmpty()) {
            val opacity = (value.toInt() / 255.0 * 100.0).toInt()
            text = Integer.toString(opacity) + "%"
            alignment = Pos.CENTER
        }
    }

    companion object {
        fun <T> forTreeTableColumn(mediator: Mediator): Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> {
            return Callback { tableColumn: TreeTableColumn<T, String> ->
                OpacityTableTreeCell<T>(mediator)
            }
        }
    }
}





