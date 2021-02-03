package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.RNArtist;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.model.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.*;

import static io.github.fjossinet.rnartist.RNArtist.*;
import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static io.github.fjossinet.rnartist.io.UtilsKt.awtColorToJavaFX;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;

public class Explorer {
    private Stage stage;
    private Mediator mediator;
    private TreeTableView<ExplorerItem> treeTableView;
    private LastColor lastColorClicked;

    public Explorer(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("Explorer");
        this.createScene(stage);
    }

    public TreeTableView<ExplorerItem> getTreeTableView() {
        return this.treeTableView;
    }

    public void refresh() {
        this.treeTableView.refresh();
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {
        this.treeTableView = new TreeTableView<ExplorerItem>();
        //this.treeTableView.setEditable(true);

        this.treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //this.treeTableView.getSelectionModel().setCellSelectionEnabled(true);
        this.treeTableView.setColumnResizePolicy(TreeTableView.UNCONSTRAINED_RESIZE_POLICY);

        TreeTableColumn<ExplorerItem, String> nameColumn = new TreeTableColumn<ExplorerItem, String>(null);
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("name"));
        nameColumn.setCellFactory(NameTreeTableCell.forTreeTableColumn(mediator));
        nameColumn.setSortable(false);

        TreeTableColumn<ExplorerItem, String> fullDetailsColumn = new TreeTableColumn<ExplorerItem, String>("Full Details");
        fullDetailsColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("fullDetails"));
        fullDetailsColumn.setCellFactory(FullDetailsTreeTableCell.forTreeTableColumn(mediator));
        fullDetailsColumn.setMinWidth(80);
        fullDetailsColumn.setMaxWidth(80);
        fullDetailsColumn.setSortable(false);

        TreeTableColumn<ExplorerItem, String> colorColumn = new TreeTableColumn<ExplorerItem, String>("Color");
        colorColumn.setEditable(true);
        colorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("color"));
        colorColumn.setCellFactory(ColorTreeTableCell.forTreeTableColumn(mediator));
        colorColumn.setMinWidth(80);
        colorColumn.setMaxWidth(80);
        colorColumn.setSortable(false);

        TreeTableColumn<ExplorerItem, String> lineWidthColumn = new TreeTableColumn<ExplorerItem, String>("Width");
        lineWidthColumn.setUserData(DrawingConfigurationParameter.linewidth);
        lineWidthColumn.setEditable(true);
        lineWidthColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("lineWidth"));
        lineWidthColumn.setCellFactory(LineWidthTableTreeCell.forTreeTableColumn(mediator));
        lineWidthColumn.setMinWidth(80);
        lineWidthColumn.setMaxWidth(80);
        lineWidthColumn.setSortable(false);

        TreeTableColumn<ExplorerItem, String> lineShiftColumn = new TreeTableColumn<ExplorerItem, String>("Shift");
        lineShiftColumn.setUserData(DrawingConfigurationParameter.lineshift);
        lineShiftColumn.setEditable(true);
        lineShiftColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("lineShift"));
        lineShiftColumn.setCellFactory(LineShiftTableTreeCell.forTreeTableColumn(mediator));
        lineShiftColumn.setMinWidth(60);
        lineShiftColumn.setMaxWidth(60);
        lineShiftColumn.setSortable(false);

        TreeTableColumn<ExplorerItem, String> opacityColumn = new TreeTableColumn<ExplorerItem, String>("Opacity");
        opacityColumn.setUserData(DrawingConfigurationParameter.opacity);
        opacityColumn.setEditable(true);
        opacityColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("opacity"));
        opacityColumn.setCellFactory(OpacityTableTreeCell.forTreeTableColumn(mediator));
        opacityColumn.setMinWidth(80);
        opacityColumn.setMaxWidth(80);
        opacityColumn.setSortable(false);

        treeTableView.getColumns().addAll(colorColumn, lineWidthColumn, lineShiftColumn, opacityColumn, fullDetailsColumn, nameColumn);
        treeTableView.setTreeColumn(nameColumn);

        treeTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!treeTableView.getSelectionModel().getSelectedCells().isEmpty()) {
                    List<DrawingElement> selectedElements = new ArrayList<DrawingElement>();
                    for (TreeTablePosition<ExplorerItem,?> item : treeTableView.getSelectionModel().getSelectedCells()) {
                        if (SecondaryStructureItem.class.isInstance((item.getTreeItem().getValue())))
                            continue;
                        else if (GroupOfStructuralElements.class.isInstance(item.getTreeItem().getValue()))
                            for (TreeItem<ExplorerItem> c : item.getTreeItem().getChildren())
                                selectedElements.add(c.getValue().getDrawingElement());
                        else
                            selectedElements.add(item.getTreeItem().getValue().getDrawingElement());
                    }
                    if (!selectedElements.isEmpty()) {
                        mediator.canvas2D.clearSelection();
                        for (DrawingElement e : selectedElements)
                            mediator.getCanvas2D().addToSelection(e);
                        if (mediator.getRnartist().isCenterDisplayOnSelection())
                            mediator.canvas2D.centerDisplayOn(mediator.getCanvas2D().getSelectionFrame());
                        mediator.canvas2D.repaint();
                    } else {
                        mediator.getCanvas2D().clearSelection();
                        mediator.canvas2D.repaint();
                    }
                }
            }
        });
        treeTableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.DOWN)) {
                    if (!treeTableView.getSelectionModel().getSelectedItems().isEmpty()) {
                        mediator.getCanvas2D().clearSelection();
                        for (TreeItem<ExplorerItem> item : treeTableView.getSelectionModel().getSelectedItems()) {
                            if (SecondaryStructureItem.class.isInstance((item.getValue())))
                                continue;
                            else if (GroupOfStructuralElements.class.isInstance(item.getValue()))
                                for (TreeItem<ExplorerItem> c: item.getChildren())
                                    mediator.getCanvas2D().addToSelection(c.getValue().getDrawingElement());
                            else
                                mediator.getCanvas2D().addToSelection(item.getValue().getDrawingElement());
                        }
                        if (mediator.getRnartist().isCenterDisplayOnSelection())
                            mediator.canvas2D.centerDisplayOn(mediator.getCanvas2D().getSelectionFrame());
                        mediator.canvas2D.repaint();
                    } else if (mediator.getDrawingDisplayed().isNotNull().get())
                        //no selection
                        mediator.getCanvas2D().clearSelection();
                }
            }
        });

        javafx.scene.control.ScrollPane sp = new ScrollPane(treeTableView);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        VBox stackedTitledPanes = new VBox();

        //SCOPE TITLEDPANE
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(10);
        pane.setHgap(5);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        HBox scopeButtons = new HBox();
        scopeButtons.setAlignment(Pos.CENTER);
        scopeButtons.setSpacing(5);

        ToggleButton currentElementScope = new ToggleButton(null, new FontIcon("fas-plus:15"));
        ToggleButton up2NextDomainsScope = new ToggleButton(null, new FontIcon("fas-compress:15"));
        ToggleButton branchesScope = new ToggleButton(null, new FontIcon("fas-expand:15"));

        currentElementScope.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                currentElementScope.setSelected(true);
                mediator.setScope(ELEMENT_SCOPE);
                up2NextDomainsScope.setSelected(false);
                branchesScope.setSelected(false);
            }
        });
        currentElementScope.setTooltip(new Tooltip("Restricted to Selected Element "));
        scopeButtons.getChildren().add(currentElementScope);

        up2NextDomainsScope.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                up2NextDomainsScope.setSelected(true);
                mediator.setScope(STRUCTURAL_DOMAIN_SCOPE);
                currentElementScope.setSelected(false);
                branchesScope.setSelected(false);
            }
        });
        up2NextDomainsScope.setTooltip(new Tooltip("Restricted to Structural Domains"));
        scopeButtons.getChildren().add(up2NextDomainsScope);

        branchesScope.setSelected(true);
        branchesScope.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                branchesScope.setSelected(true);
                mediator.setScope(BRANCH_SCOPE);
                currentElementScope.setSelected(false);
                up2NextDomainsScope.setSelected(false);
            }
        });
        branchesScope.setTooltip(new Tooltip("Along branches"));
        scopeButtons.getChildren().add(branchesScope);

        GridPane.setHalignment(scopeButtons, HPos.CENTER);
        pane.add(scopeButtons, 0,0, 2, 1);

        CheckBox ignoreTertiaries = new CheckBox("Ignore Tertiaries");
        ignoreTertiaries.setSelected(false);
        ignoreTertiaries.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.setIgnoreTertiaries(ignoreTertiaries.isSelected());
            }
        });
        ignoreTertiaries.setAlignment(Pos.CENTER);
        pane.add(ignoreTertiaries, 0,1, 2, 1);

        TitledPane scope = new TitledPane("Scope" , pane);
        scope.setExpanded(true);

        // COLORS TITLEDPANE
        pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(5);
        pane.setHgap(5);
        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setPercentWidth(50);
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setPercentWidth(50);
        pane.getColumnConstraints().addAll(cc1,cc2);

        FlowPane lastColorsUsed = new FlowPane();
        lastColorsUsed.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));

        ColorPicker picker = new ColorPicker();
        picker.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        picker.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                for (TreeItem item: treeTableView.getSelectionModel().getSelectedItems())
                    mediator.getExplorer().setColorFrom(item, getHTMLColorString(javaFXToAwt(picker.getValue())), mediator.getScope());
                LastColor r = new LastColor(picker.getValue());
                if (lastColorsUsed.getChildren().size() == 30)
                    lastColorsUsed.getChildren().remove(29);
                lastColorsUsed.getChildren().add(0, r);
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        pane.add(picker, 0,0);

        MenuButton colorSchemesMenu = new MenuButton("Schemes");
        colorSchemesMenu.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));

        Menu branchesColors = new Menu("Branches");
        colorSchemesMenu.getItems().add(branchesColors);

        MenuItem randomColors = new MenuItem("Random");
        branchesColors.getItems().add(randomColors);

        randomColors.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (Branch branch: mediator.getDrawingDisplayed().get().getDrawing().getBranches()) {
                    Random rand = new Random();
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();
                    java.awt.Color c = new java.awt.Color(r, g, b);
                    Map<String, Map<String,String>> configuration = new HashMap<>();
                    Map<String,String> colorConfig = new HashMap<>();
                    colorConfig.put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(c));
                    Map<String,String> letterColorConfig = new HashMap<>();
                    letterColorConfig.put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                    configuration.put(SecondaryStructureType.Helix.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.SecondaryInteraction.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.Junction.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.AShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.A.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.UShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.U.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.GShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.G.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.CShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.C.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.XShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.X.toString(), letterColorConfig);
                    Theme theme = new Theme(configuration);
                    for (TreeItem<ExplorerItem> item: mediator.getExplorer().getTreeViewItemsFor(mediator.getExplorer().getTreeTableView().getRoot(),branch.getParent()))
                        mediator.getExplorer().applyTheme( item, theme ,BRANCH_SCOPE);
                }

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        MenuItem distanceColors = new MenuItem("Distance");
        branchesColors.getItems().add(distanceColors);

        distanceColors.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (Branch branch: mediator.getDrawingDisplayed().get().getDrawing().getBranches()) {
                    java.awt.Color c = branch.getParent().getColor();
                    List<JunctionDrawing> junctions = branch.junctionsFromBranch();
                    for (JunctionDrawing junction:junctions) {
                        List<DrawingElement> path = junction.pathToRoot();
                        java.awt.Color interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate( javafx.scene.paint.Color.LIGHTGRAY, (double)(path.size()-1)/(double)branch.getBranchLength()));
                        junction.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                        for (ResidueDrawing r:junction.getResidues())
                            r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                        interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate( javafx.scene.paint.Color.LIGHTGRAY, (double)(path.size()-2)/(double)branch.getBranchLength()));
                        junction.getParent().getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                        for (ResidueDrawing r:junction.getParent().getResidues())
                            r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                    }
                }
                for (SingleStrandDrawing ss: mediator.getDrawingDisplayed().get().getDrawing().getSingleStrands()) {
                    ss.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                    for (ResidueDrawing r: ss.getResidues())
                        r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                }
                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu colorSchemes = new Menu("Residues...");

        colorSchemesMenu.getItems().add(colorSchemes);

        for (String themeName : RnartistConfig.colorSchemes.keySet()) {
            MenuItem residueThemeItem = new MenuItem(themeName);
            residueThemeItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                    Theme t = new Theme(RnartistConfig.colorSchemes.get(themeName));

                    if (!selection.isEmpty()) {
                        for (TreeItem<ExplorerItem> item:selection)
                            mediator.getExplorer().applyTheme(item, t, mediator.getScope());
                    } else
                        mediator.getExplorer().applyTheme(t, mediator.getScope());

                    mediator.getCanvas2D().repaint();
                    mediator.getExplorer().refresh();
                }
            });
            colorSchemes.getItems().add(residueThemeItem);
        }

        pane.add(colorSchemesMenu, 1,0);

        pane.add(new Label("Last Colors"), 0,1, 2, 1);

        lastColorsUsed.setHgap(1.0);
        lastColorsUsed.setVgap(1.0);
        Set<String> firstColors = new HashSet<String>();
        for (Map<String, Map<String, String>> v:RnartistConfig.colorSchemes.values()) {
            for (Map<String, String>_v: v.values())
                firstColors.addAll(_v.values());
        }
        for (String c: firstColors) {
            LastColor r = new LastColor(Color.web(c));
            if (lastColorsUsed.getChildren().size() == 30)
                lastColorsUsed.getChildren().remove(29);
            lastColorsUsed.getChildren().add(0, r);
        }
        GridPane.setHalignment(lastColorsUsed, HPos.CENTER);
        pane.add(lastColorsUsed, 0,2, 2,1);

        TitledPane colors = new TitledPane("Colors" , pane);
        colors.setExpanded(true);

        // LINE WIDTH TITLEDPANE
        pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(5);
        pane.setHgap(5);
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        ListView<String> lineWidth = new ListView<String>();
        lineWidth.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineWidth.setMaxHeight(200);
        lineWidth.getItems().addAll("0", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0", "3.5", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0");
        lineWidth.setCellFactory(new ShapeCellFactory());
        lineWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                for (TreeItem item: treeTableView.getSelectionModel().getSelectedItems())
                    mediator.getExplorer().setLineWidthFrom(item, lineWidth.getSelectionModel().getSelectedItem(), mediator.getScope());
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        pane.add(lineWidth, 0,0, 2, 1);
        GridPane.setHalignment(lineWidth, HPos.CENTER);

        TitledPane linesWidth = new TitledPane("Lines Width" ,pane);
        linesWidth.setExpanded(true);

        // LINE SHIFT TITLEDPANE
        pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(5);
        pane.setHgap(5);
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        final Slider lineShiftSlider = new Slider(0, 10,0);
        lineShiftSlider.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineShiftSlider.setShowTickLabels(true);
        lineShiftSlider.setShowTickMarks(true);
        lineShiftSlider.setMajorTickUnit(5);
        lineShiftSlider.setMinorTickCount(1);
        lineShiftSlider.setShowTickMarks(true);
        lineShiftSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                for (TreeItem item: treeTableView.getSelectionModel().getSelectedItems())
                    mediator.getExplorer().setLineShiftFrom(item, Integer.toString((int)lineShiftSlider.getValue()), mediator.getScope());
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();

            }
        });
        pane.add(lineShiftSlider, 0,0, 2, 1);
        GridPane.setHalignment(lineShiftSlider, HPos.CENTER);

        TitledPane linesShift = new TitledPane("Lines Shift", pane);
        linesShift.setExpanded(false);

        // OPACITY TITLEDPANE
        pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(5);
        pane.setHgap(5);
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        final Slider opacitySlider = new Slider(0, 100,100);
        opacitySlider.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        opacitySlider.setShowTickLabels(true);
        opacitySlider.setShowTickMarks(true);
        opacitySlider.setMajorTickUnit(25);
        opacitySlider.setMinorTickCount(5);
        opacitySlider.setShowTickMarks(true);
        opacitySlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                int opacity = (int) ((double) (opacitySlider.getValue()) / 100.0 * 255.0);
                for (TreeItem item: treeTableView.getSelectionModel().getSelectedItems())
                    mediator.getExplorer().setOpacityFrom(item, Integer.toString(opacity), mediator.getScope());
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();

            }
        });
        pane.add(opacitySlider, 0,0, 2, 1);
        GridPane.setHalignment(opacitySlider, HPos.CENTER);

        TitledPane opacity = new TitledPane("Opacity", pane);
        opacity.setExpanded(false);

        // DETAILS TITLEDPANE
        pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(5);
        pane.setHgap(5);
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        HBox hbox = new HBox();
        hbox.setSpacing(5);
        Button b = new Button(null, new FontIcon("fas-eye:15"));
        b.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TreeItem item : treeTableView.getSelectionModel().getSelectedItems())
                    mediator.getExplorer().setFullDetailsFrom(item, "true", mediator.getScope());
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        hbox.getChildren().add(b);
        b = new Button(null, new FontIcon("fas-eye-slash:15"));
        b.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (TreeItem item : treeTableView.getSelectionModel().getSelectedItems())
                    mediator.getExplorer().setFullDetailsFrom(item, "false", mediator.getScope());
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        hbox.getChildren().add(b);
        hbox.setAlignment(Pos.CENTER);
        pane.add(hbox, 0,0, 2, 1);
        GridPane.setHalignment(hbox, HPos.CENTER);

        TitledPane details = new TitledPane("Full Details", pane);
        details.setExpanded(true);

        // SELECTION TITLEDPANE
        pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setVgap(5);
        pane.setHgap(5);
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        TitledPane selection = new TitledPane("Selection", pane);
        selection.setExpanded(true);

        ListView<String> elementsToSelect = new ListView<String>();
        elementsToSelect.setMaxHeight(200);
        elementsToSelect.getItems().addAll("Helices", "SingleStrands", "Junctions", "Apical Loops", "Inner Loops", "PseudoKnots", "Secondary Interactions", "Tertiary Interactions", "Interaction Symbols", "Phosphodiester Bonds", "Residues", "As", "Us", "Gs", "Cs", "Xs", "Residue Letters");
        pane.add(elementsToSelect, 0,0, 2, 1);
        GridPane.setHalignment(elementsToSelect, HPos.CENTER);

        b = new Button(null, new FontIcon("fas-search:15"));
        b.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        b.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                if (getTreeTableView().getSelectionModel().isEmpty())
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                else
                    starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                switch (elementsToSelect.getSelectionModel().getSelectedItem()) {
                    case "Helices":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> HelixDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "SingleStrands":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> SingleStrandDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Junctions":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> JunctionDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Apical Loops":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> JunctionDrawing.class.isInstance(drawingElement) && ((JunctionDrawing)drawingElement).getJunctionType() == JunctionType.ApicalLoop, starts, true,  mediator.getScope());
                        break;
                    case "Inner Loops":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> JunctionDrawing.class.isInstance(drawingElement) && ((JunctionDrawing)drawingElement).getJunctionType() == JunctionType.InnerLoop, starts, true,  mediator.getScope());
                        break;
                    case "PseudoKnots":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> PKnotDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Secondary Interactions":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> SecondaryInteractionDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Tertiary Interactions":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> TertiaryInteractionDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Interaction Symbols":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> InteractionSymbolDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Phosphodiester Bonds":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> PhosphodiesterBondDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Residues":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "As":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> AShapeDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Us":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> UShapeDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Gs":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> GShapeDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Cs":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> CShapeDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                    case "Residue Letters":
                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueLetterDrawing.class.isInstance(drawingElement), starts, true,  mediator.getScope());
                        break;
                }
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        pane.add(b, 0,1);
        GridPane.setHalignment(b, HPos.LEFT);
        Label l = new Label("Search from Selection");
        l.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        pane.add(l, 1,1);

        stackedTitledPanes.getChildren().add(scope);
        stackedTitledPanes.getChildren().add(selection);
        stackedTitledPanes.getChildren().add(details);
        stackedTitledPanes.getChildren().add(colors);
        stackedTitledPanes.getChildren().add(linesWidth);
        stackedTitledPanes.getChildren().add(linesShift);
        stackedTitledPanes.getChildren().add(opacity);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPosition(0, 0.25);
        splitPane.setPadding(new Insets(5));
        ScrollPane sp2 = new ScrollPane(stackedTitledPanes);
        splitPane.setResizableWithParent(sp2, false);
        sp2.setFitToWidth(true);
        splitPane.getItems().addAll(sp2, sp);

        Scene scene = new Scene(splitPane);
        scene.getStylesheets().add("io/github/fjossinet/rnartist/gui/css/main.css");
        stage.setScene(scene);

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        int width = (int)(screenSize.getWidth()*4.0/10.0);
        scene.getWindow().setWidth(width);
        scene.getWindow().setHeight(screenSize.getHeight());
        scene.getWindow().setX(screenSize.getWidth()-width);
        scene.getWindow().setY(0);
    }

    public void applyTheme(Theme theme, byte scope) {
        this.applyTheme(this.treeTableView.getRoot(), theme, scope);
    }

    public void applyTheme(TreeItem<ExplorerItem> item, Theme theme, byte scope) {
        if (mediator.getIgnoreTertiaries() && item.getValue().getName().equals("Tertiaries"))
            return;
        item.getValue().applyTheme(theme);
        if (scope == RNArtist.ELEMENT_SCOPE)
            return;
        for (TreeItem<ExplorerItem> c:item.getChildren()) {
            if (!GroupOfStructuralElements.class.isInstance(item.getValue()) && scope == STRUCTURAL_DOMAIN_SCOPE && StructuralDomainDrawing.class.isInstance(c.getValue().getDrawingElement()))
                continue;
            applyTheme(c, theme, scope);
        }
    }

    /**
     * Return a list of TreeItem containing a DrawingElement. Can return several objects since a DrawingElement can be encapsulated at different places
     * (like an helix and its children in a pknot or residues in tertiary interactions
     * @param item
     * @param drawingElement
     * @return
     */
    public List<TreeItem<ExplorerItem>> getTreeViewItemsFor(TreeItem<ExplorerItem> item, DrawingElement drawingElement) {
        List<TreeItem<ExplorerItem>> items = new ArrayList<>();
        if (SecondaryStructureDrawing.class.isInstance(drawingElement)) {//its the root element
            items.add(this.treeTableView.getRoot());
            return items;
        }

        if (item != null && item.getValue().getDrawingElement() == drawingElement) {
            items.add(item);
            return items;
        }

        for (TreeItem<ExplorerItem> child : item.getChildren())
            items.addAll(getTreeViewItemsFor(child, drawingElement));

        return items;
    }

    public List<TreeItem<ExplorerItem>> getAllTreeViewItemsFrom(TreeItem<ExplorerItem> item) {
        List<TreeItem<ExplorerItem>> hits = new ArrayList<TreeItem<ExplorerItem>>();
        hits.add(item);
        for (TreeItem<ExplorerItem> c: item.getChildren())
            hits.addAll(getAllTreeViewItemsFrom(c));
        return hits;
    }

    public void selectAllTreeViewItems(DrawingElementFilter filter, List<TreeItem<ExplorerItem>> starts, boolean updateCanvas, byte scope) {
        List<TreeItem<ExplorerItem>> hits = new ArrayList<TreeItem<ExplorerItem>>();
        clearSelection(); //we don't want in the selection the element we used at first to launch the selection (we selected an Helix to select all Residues -> we don't need/want the helix anymore in the selection)
        for (TreeItem<ExplorerItem> start:starts)
            getAllTreeViewItems(hits, start, filter, scope);
        for (TreeItem<ExplorerItem> hit:hits) {
            TreeItem p = hit.getParent();
            while (p!= null) {
                p.setExpanded(true);
                p = p.getParent();
            }
        }
        for (TreeItem<ExplorerItem> hit:hits)
            treeTableView.getSelectionModel().select(hit);
        if (updateCanvas) {
            mediator.getCanvas2D().clearSelection();
            for (TreeItem<ExplorerItem> hit : hits)
                mediator.getCanvas2D().addToSelection(hit.getValue().getDrawingElement());
        }
    }

    public List<TreeItem<ExplorerItem>> getAllTreeViewItems(List<TreeItem<ExplorerItem>> hits, TreeItem<ExplorerItem> start, DrawingElementFilter filter, byte scope) {
        if (hits == null)
            hits = new ArrayList<TreeItem<ExplorerItem>>();

        if (mediator.getIgnoreTertiaries() && start.getValue().getName().equals("Tertiaries"))
            return hits;

        if (filter.isOK(start.getValue().getDrawingElement()))
            hits.add(start);

        if (scope == RNArtist.ELEMENT_SCOPE)
            return hits;

        for (TreeItem<ExplorerItem> child : start.getChildren()) {
            if (!GroupOfStructuralElements.class.isInstance(start.getValue()) && scope == STRUCTURAL_DOMAIN_SCOPE && StructuralDomainDrawing.class.isInstance(child.getValue().getDrawingElement()))
                continue;
            getAllTreeViewItems(hits, child, filter, scope);
        }

        return hits;
    }

    public void clearSelection() {
        this.treeTableView.getSelectionModel().clearSelection();
    }

    public ObservableList<TreeItem<ExplorerItem>> getSelectedTreeViewItems() {
        return this.treeTableView.getSelectionModel().getSelectedItems();
    }

    public void setFullDetailsFrom(TreeItem<ExplorerItem> from, String fullDetails, byte scope) {
        if (mediator.getIgnoreTertiaries() &&  from.getValue().getName().equals("Tertiaries"))
            return;
        from.getValue().setFullDetails(fullDetails);
        if (scope == RNArtist.ELEMENT_SCOPE)
            return;
        for (TreeItem<ExplorerItem> child:from.getChildren()) {
            if (!GroupOfStructuralElements.class.isInstance(from.getValue()) && scope == STRUCTURAL_DOMAIN_SCOPE && StructuralDomainDrawing.class.isInstance(child.getValue().getDrawingElement()))
                continue;
            setFullDetailsFrom(child, fullDetails, scope);
        }
    }

    public void setColorFrom(TreeItem<ExplorerItem> from, String color, byte scope) {
        if (mediator.getIgnoreTertiaries() && from.getValue().getName().equals("Tertiaries"))
            return;
        from.getValue().setColor(color);
        if (scope == RNArtist.ELEMENT_SCOPE)
            return;
        for (TreeItem<ExplorerItem> child:from.getChildren()) {
            if (!GroupOfStructuralElements.class.isInstance(from.getValue()) && scope == STRUCTURAL_DOMAIN_SCOPE && StructuralDomainDrawing.class.isInstance(child.getValue().getDrawingElement()))
                continue;
            setColorFrom(child, color, scope);
        }
    }

    public void setLineWidthFrom(TreeItem<ExplorerItem> from, String width, byte scope) {
        if (mediator.getIgnoreTertiaries() && from.getValue().getName().equals("Tertiaries"))
            return;
        from.getValue().setLineWidth(width);
        if (scope == RNArtist.ELEMENT_SCOPE)
            return;
        for (TreeItem<ExplorerItem> child:from.getChildren()) {
            if (!GroupOfStructuralElements.class.isInstance(from.getValue()) && scope == STRUCTURAL_DOMAIN_SCOPE && StructuralDomainDrawing.class.isInstance(child.getValue().getDrawingElement()))
                continue;
            setLineWidthFrom(child, width, scope);
        }
    }

    public void setLineShiftFrom(TreeItem<ExplorerItem> from, String shift, byte scope) {
        if (mediator.getIgnoreTertiaries() && from.getValue().getName().equals("Tertiaries"))
            return;
        from.getValue().setLineShift(shift);
        if (scope == RNArtist.ELEMENT_SCOPE)
            return;
        for (TreeItem<ExplorerItem> child:from.getChildren()) {
            if (!GroupOfStructuralElements.class.isInstance(from.getValue()) && scope == STRUCTURAL_DOMAIN_SCOPE && StructuralDomainDrawing.class.isInstance(child.getValue().getDrawingElement()))
                continue;
            setLineShiftFrom(child, shift, scope);
        }
    }

    public void setOpacityFrom(TreeItem<ExplorerItem> from, String opacity, byte scope) {
        if (mediator.getIgnoreTertiaries() && from.getValue().getName().equals("Tertiaries"))
            return;
        from.getValue().setOpacity(opacity);
        if (scope == RNArtist.ELEMENT_SCOPE)
            return;
        for (TreeItem<ExplorerItem> child:from.getChildren()) {
            if (!GroupOfStructuralElements.class.isInstance(from.getValue()) && scope == STRUCTURAL_DOMAIN_SCOPE && StructuralDomainDrawing.class.isInstance(child.getValue().getDrawingElement()))
                continue;
            setOpacityFrom(child, opacity, scope);
        }
    }

    //++++++++++ methods to construct the treetable

    private List<TreeItem<ExplorerItem>> helicesAlreadyInPknots = new ArrayList<>();
    private List<TreeItem<ExplorerItem>> residuesAlreadyInTertiaries = new ArrayList<>();
    private List<TreeItem<ExplorerItem>> alreadyTertiaries = new ArrayList<>();

    public void load(SecondaryStructureDrawing drawing) {
        //we fit the width of the Name column to have enough space to expand the nodes
        int max = 0;
        for (Branch b: drawing.getBranches()) {
            if (b.getBranchLength() > max)
                max = b.getBranchLength();
        }
        treeTableView.getColumns().get(treeTableView.getColumns().size()-1).setMinWidth(50*max);
        treeTableView.getColumns().get(treeTableView.getColumns().size()-1).setMaxWidth(60*max);

        this.helicesAlreadyInPknots.clear();
        this.residuesAlreadyInTertiaries.clear();

        TreeItem<ExplorerItem> root = new TreeItem<ExplorerItem>(new SecondaryStructureItem(drawing));
        root.setExpanded(true);

        GroupOfStructuralElements pknotsGroup = new GroupOfStructuralElements("Pseudoknots");
        TreeItem<ExplorerItem> pknotsTreeItem = new TreeItem<ExplorerItem>(pknotsGroup);
        root.getChildren().add(pknotsTreeItem);
        for (PKnotDrawing pknot : drawing.getPknots()) {
            TreeItem<ExplorerItem> pknotTreeItem = this.load(pknot);
            pknotsTreeItem.getChildren().add(pknotTreeItem);
            pknotsGroup.getChildren().add(pknotTreeItem.getValue());
        }

        GroupOfStructuralElements branchesGroup = new GroupOfStructuralElements("Branches");
        TreeItem<ExplorerItem> allBranches = new TreeItem<ExplorerItem>(branchesGroup);
        root.getChildren().add(allBranches);
        for (JunctionDrawing j: drawing.getBranches()) {
            TreeItem<ExplorerItem> branchTreeItem = this.load(j);
            allBranches.getChildren().addAll(branchTreeItem);
            branchesGroup.getChildren().add(branchTreeItem.getValue());
        }

        GroupOfStructuralElements singleStrandsGroup = new GroupOfStructuralElements("SingleStrands");
        TreeItem<ExplorerItem> allSingleStrands = new TreeItem<ExplorerItem>(singleStrandsGroup);
        root.getChildren().add(allSingleStrands);
        for (SingleStrandDrawing ss : drawing.getSingleStrands()) {
            TreeItem<ExplorerItem> singleStrandTreeItem = this.load(ss);
            allSingleStrands.getChildren().addAll(singleStrandTreeItem);
            singleStrandsGroup.getChildren().add(singleStrandTreeItem.getValue());
        }

        GroupOfStructuralElements phosphoGroup = new GroupOfStructuralElements("Phosphodiester Bonds");
        TreeItem<ExplorerItem> allphosphos = new TreeItem<ExplorerItem>(phosphoGroup);
        root.getChildren().add(allphosphos);
        for (PhosphodiesterBondDrawing p : drawing.getPhosphoBonds()) {
            TreeItem<ExplorerItem> phosphoTreeItem = this.load(p);
            allphosphos.getChildren().addAll(phosphoTreeItem);
            phosphoGroup.getChildren().add(phosphoTreeItem.getValue());
        }

        this.treeTableView.setRoot(root);

        //cleaning
        this.helicesAlreadyInPknots.clear();
        this.residuesAlreadyInTertiaries.clear();
    }


    //make a tree of TreeItems containing the same ExplorerItems. This allows to synchronize two different tree items targeting the same drawing elements (Residues betwenn secondaries and tertiaries, helices between junctions and pknots)
    private TreeItem<ExplorerItem> copy(TreeItem<ExplorerItem> toCopy) {
        TreeItem<ExplorerItem> copy = new TreeItem<ExplorerItem>(toCopy.getValue());
        for (TreeItem<ExplorerItem> c : toCopy.getChildren())
            copy.getChildren().add(copy(c));
        return copy;
    }

    private TreeItem<ExplorerItem> load(PKnotDrawing pknot) {
        TreeItem<ExplorerItem> pknotItem = new TreeItem<ExplorerItem>(new PknotItem(pknot));
        pknotItem.getChildren().add(this.load(pknot.helix));
        helicesAlreadyInPknots.add(this.copy(pknotItem.getChildren().get(0))) ; //a copy for the helix in the branches
        GroupOfStructuralElements tertiariesGroup = new GroupOfStructuralElements("Tertiaries");
        TreeItem<ExplorerItem> allTertiaries = new TreeItem<ExplorerItem>(tertiariesGroup);
        for (TertiaryInteractionDrawing interaction : pknot.getTertiaryInteractions()) {
            TreeItem<ExplorerItem> tertiaryTreeItem  = this.load(interaction, true);
            allTertiaries.getChildren().addAll(tertiaryTreeItem);
            tertiariesGroup.getChildren().add(tertiaryTreeItem.getValue());
        }
        pknotItem.getChildren().add(allTertiaries);
        return pknotItem;
    }

    private TreeItem<ExplorerItem> load(HelixDrawing h) {
        TreeItem<ExplorerItem> helixItem = new TreeItem<ExplorerItem>(new HelixItem(h));

        GroupOfStructuralElements secondariesGroup = new GroupOfStructuralElements("Secondaries");
        TreeItem<ExplorerItem> secondaries = new TreeItem<ExplorerItem>(secondariesGroup);
        helixItem.getChildren().add(secondaries);
        for (SecondaryInteractionDrawing interaction : h.getSecondaryInteractions()) {
            TreeItem<ExplorerItem> secondaryTreeItem = this.load(interaction, false);
            secondaries.getChildren().addAll(secondaryTreeItem);
            secondariesGroup.getChildren().add(secondaryTreeItem.getValue());
        }

        GroupOfStructuralElements tertiariesGroup = new GroupOfStructuralElements("Tertiaries");
        TreeItem<ExplorerItem> tertiariesTreeItem = new TreeItem<ExplorerItem>(tertiariesGroup);
        helixItem.getChildren().add(tertiariesTreeItem);
        for (TertiaryInteractionDrawing interaction : h.getSsDrawing().getAllTertiaryInteractions())
            if (h.getResidues().contains(interaction.getResidue()) ||  h.getResidues().contains(interaction.getPairedResidue())) {
                TreeItem<ExplorerItem> tertiaryTreeItem = this.load(interaction, true);
                tertiariesTreeItem.getChildren().addAll(tertiaryTreeItem);
                tertiariesGroup.getChildren().add(tertiaryTreeItem.getValue());
            }

        GroupOfStructuralElements phosphoGroup = new GroupOfStructuralElements("Phosphodiester Bonds");
        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(phosphoGroup);
        helixItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: h.getPhosphoBonds()) {
            TreeItem<ExplorerItem> phosphoTreeItem = this.load(p);
            allPhosphos.getChildren().add(phosphoTreeItem);
            phosphoGroup.getChildren().add(phosphoTreeItem.getValue());
        }
        return helixItem;
    }

    private TreeItem<ExplorerItem> load(JunctionDrawing jc) {

        TreeItem<ExplorerItem> junctionItem = new TreeItem<ExplorerItem>(new JunctionItem(jc));

        GroupOfStructuralElements residuesGroup = new GroupOfStructuralElements("Residues");
        TreeItem<ExplorerItem> residues = new TreeItem<ExplorerItem>(residuesGroup);
        junctionItem.getChildren().add(residues);
        for (ResidueDrawing r : jc.getResidues())
            if (r.getParent() == jc) {
                boolean found = false;
                for (TreeItem<ExplorerItem> _r: this.residuesAlreadyInTertiaries) {
                    if (r.equals(_r.getValue().getDrawingElement())) {
                        residues.getChildren().addAll(_r);
                        residuesGroup.getChildren().add(_r.getValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    TreeItem<ExplorerItem> residueTreeItem = load(r);
                    residues.getChildren().addAll(residueTreeItem);
                    residuesGroup.getChildren().add(residueTreeItem.getValue());
                }
            }

        GroupOfStructuralElements tertiariesGroup = new GroupOfStructuralElements("Tertiaries");
        TreeItem<ExplorerItem> tertiariesTreeItem = new TreeItem<ExplorerItem>(tertiariesGroup);
        junctionItem.getChildren().add(tertiariesTreeItem);
        for (TertiaryInteractionDrawing interaction : jc.getSsDrawing().getAllTertiaryInteractions())
            if (jc.getResidues().contains(interaction.getResidue()) ||  jc.getResidues().contains(interaction.getPairedResidue())) {
                TreeItem<ExplorerItem> tertiaryTreeItem = this.load(interaction, true);
                tertiariesTreeItem.getChildren().addAll(tertiaryTreeItem);
                tertiariesGroup.getChildren().add(tertiaryTreeItem.getValue());
            }

        GroupOfStructuralElements phosphoGroup = new GroupOfStructuralElements("Phosphodiester Bonds");
        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(phosphoGroup);
        junctionItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: jc.getPhosphoBonds()) {
            TreeItem<ExplorerItem> phosphoTreeItem = load(p);
            allPhosphos.getChildren().add(phosphoTreeItem);
            phosphoGroup.getChildren().add(phosphoTreeItem.getValue());
        }

        for (JunctionDrawing connectedJC: jc.getConnectedJunctions().values())
            junctionItem.getChildren().add(this.load(connectedJC));

        for (TreeItem<ExplorerItem> h: this.helicesAlreadyInPknots) {
            if (h.getValue().getDrawingElement().equals(jc.getParent())) {
                TreeItem<ExplorerItem> helixItem = h;
                helixItem.getChildren().add(junctionItem);
                return helixItem;
            }
        }
        TreeItem<ExplorerItem> helixItem = load((HelixDrawing)jc.getParent());
        helixItem.getChildren().add(junctionItem);
        return helixItem;
    }

    private TreeItem<ExplorerItem> load(SingleStrandDrawing ss) {
        TreeItem<ExplorerItem> ssItem = new TreeItem<ExplorerItem>(new SingleStrandItem(ss));

        GroupOfStructuralElements residuesGroup = new GroupOfStructuralElements("Residues");
        TreeItem<ExplorerItem> residues = new TreeItem<ExplorerItem>(residuesGroup);
        ssItem.getChildren().add(residues);
        for (ResidueDrawing r : ss.getResidues()) {
            if (r.getParent() == ss) {
                boolean found = false;
                for (TreeItem<ExplorerItem> _r: this.residuesAlreadyInTertiaries) {
                    if (r.equals(_r.getValue().getDrawingElement())) {
                        residues.getChildren().add(_r);
                        residuesGroup.getChildren().add(_r.getValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    TreeItem<ExplorerItem> residueTreeItem = load(r);
                    residues.getChildren().add(residueTreeItem);
                    residuesGroup.getChildren().add(residueTreeItem.getValue());
                }
            }
        }

        GroupOfStructuralElements tertiariesGroup = new GroupOfStructuralElements("Tertiaries");
        TreeItem<ExplorerItem> tertiariesTreeItem = new TreeItem<ExplorerItem>(tertiariesGroup);
        ssItem.getChildren().add(tertiariesTreeItem);
        for (TertiaryInteractionDrawing interaction : ss.getSsDrawing().getAllTertiaryInteractions())
            if (ss.getResidues().contains(interaction.getResidue()) ||  ss.getResidues().contains(interaction.getPairedResidue())) {
                TreeItem<ExplorerItem> tertiaryTreeItem = this.load(interaction, true);
                tertiariesTreeItem.getChildren().addAll(tertiaryTreeItem);
                tertiariesGroup.getChildren().add(tertiaryTreeItem.getValue());
            }

        GroupOfStructuralElements phosphoGroup = new GroupOfStructuralElements("Phosphodiester Bonds");
        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(phosphoGroup);
        ssItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: ss.getPhosphoBonds()) {
            TreeItem<ExplorerItem> phosphoTreeItem = this.load(p);
            allPhosphos.getChildren().add(phosphoTreeItem);
            phosphoGroup.getChildren().add(phosphoTreeItem.getValue());
        }
        return ssItem;
    }

    private TreeItem<ExplorerItem> load(BaseBaseInteractionDrawing interaction, boolean isTertiary) {
        if (isTertiary)
            for (TreeItem<ExplorerItem> already: this.alreadyTertiaries) {
                if (interaction.equals(already.getValue().getDrawingElement())) {
                    return copy(already);
                }
            }

        TreeItem<ExplorerItem> interactionItem =
                isTertiary ? new TreeItem<ExplorerItem>(new TertiaryInteractionItem((TertiaryInteractionDrawing) interaction)) : new TreeItem<ExplorerItem>(new SecondaryInteractionItem((SecondaryInteractionDrawing) interaction));

        TreeItem<ExplorerItem> interactionSymbolItem = new TreeItem<ExplorerItem>(new InteractionSymbolItem(interaction.getInteractionSymbol()));
        interactionItem.getChildren().add(interactionSymbolItem);
        if (isTertiary) {
            boolean found = false;
            for (TreeItem<ExplorerItem> r: this.residuesAlreadyInTertiaries) {
                if (interaction.getResidue().equals(r.getValue().getDrawingElement())) {
                    interactionItem.getChildren().add(r);
                    this.residuesAlreadyInTertiaries.add(copy(r)); //we add a new copy if needed for an other interaction
                    found = true;
                    break;
                }
            }
            if (!found) {
                TreeItem<ExplorerItem> r = load(interaction.getResidue());
                this.residuesAlreadyInTertiaries.add(copy(r));
                interactionItem.getChildren().add(r);
            }
            found = false;
            for (TreeItem<ExplorerItem> r: this.residuesAlreadyInTertiaries) {
                if (interaction.getPairedResidue().equals(r.getValue().getDrawingElement())) {
                    interactionItem.getChildren().add(r);
                    this.residuesAlreadyInTertiaries.add(copy(r)); //we add a new copy if needed for an other interaction
                    found = true;
                    break;
                }
            }
            if (!found) {
                TreeItem<ExplorerItem> r = load(interaction.getPairedResidue());
                this.residuesAlreadyInTertiaries.add(copy(r));
                interactionItem.getChildren().add(r);
            }
        } else {
            boolean foundR = false, foundPaired = false;
            for (TreeItem<ExplorerItem> r: this.residuesAlreadyInTertiaries) {
                if (interaction.getResidue().equals(r.getValue().getDrawingElement())) {
                    interactionItem.getChildren().add(r);
                    foundR = true;
                } else if (interaction.getPairedResidue().equals(r.getValue().getDrawingElement())) {
                    interactionItem.getChildren().add(r);
                    foundPaired = true;
                }
                if (foundR && foundPaired)
                    break;
            }
            if (!foundR) {
                TreeItem<ExplorerItem> r = load(interaction.getResidue());
                interactionItem.getChildren().add(r);
            }
            if (!foundPaired) {
                TreeItem<ExplorerItem> r = load(interaction.getPairedResidue());
                interactionItem.getChildren().add(r);
            }
        }
        if (isTertiary)
            alreadyTertiaries.add(interactionItem);
        return interactionItem;
    }

    private TreeItem<ExplorerItem> load(ResidueDrawing r) {
        TreeItem<ExplorerItem> residueItem = new TreeItem<ExplorerItem>(new ResidueItem(r));
        TreeItem<ExplorerItem> residueLetterItem = new TreeItem<ExplorerItem>(new ResidueLetterItem(r.getResidueLetter()));
        residueItem.getChildren().add(residueLetterItem);
        return residueItem;
    }

    private TreeItem<ExplorerItem> load(PhosphodiesterBondDrawing phospho) {
        TreeItem<ExplorerItem> phosphoItem = new TreeItem<ExplorerItem>(new PhosphodiesterItem(phospho));
        return phosphoItem;
    }

    public interface DrawingElementFilter {
        public boolean isOK(DrawingElement el);
    }

    private class LastColor extends Rectangle {

        private Color color;

        private LastColor(Color color) {
            super(20,20, color);
            this.color = color;
            this.setStyle("-fx-stroke: "+getHTMLColorString(javaFXToAwt(this.color))+" ;-fx-stroke-width: 2;");
            this.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (lastColorClicked != null)
                        lastColorClicked.setStyle("-fx-stroke: "+getHTMLColorString(javaFXToAwt(lastColorClicked.color))+" ;-fx-stroke-width: 2;");
                    lastColorClicked = LastColor.this;
                    lastColorClicked.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
                    for (TreeItem item: treeTableView.getSelectionModel().getSelectedItems())
                        mediator.getExplorer().setColorFrom(item, getHTMLColorString(javaFXToAwt(color)), mediator.getScope());
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
        }

    }

    private class ShapeCellFactory implements Callback<ListView<String>, ListCell<String>> {
        @Override
        public ListCell<String> call(ListView<String> listview) {
            return new ShapeCell();
        }
    }

    private class ShapeCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(Explorer.this.getShape(item));
            }
        }
    }

    private Node getShape(String lineWidth) {
        Node node = null;

        switch (lineWidth.toLowerCase()) {
            case "0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(0);
                break;
            case "0.25":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(0.25);
                break;
            case "0.5":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(0.5);
                break;
            case "0.75":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(0.75);
                break;
            case "1.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(1);
                break;
            case "1.25":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(1.25);
                break;
            case "1.5":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(1.5);
                break;
            case "1.75":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(1.75);
                break;
            case "2.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(2);
                break;
            case "2.5":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(2.5);
                break;
            case "3.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(3);
                break;
            case "3.5":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(3.5);
                break;
            case "4.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(4);
                break;
            case "5.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(5);
                break;
            case "6.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(6);
                break;
            case "7.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(7);
                break;
            case "8.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(8);
                break;
            case "9.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(9);
                break;
            case "10.0":
                node = new Line(0, 10, 100, 10);
                ((Line)node).setStrokeWidth(10);
                break;
            default:
                node = null;
        }
        return node;
    }

}
