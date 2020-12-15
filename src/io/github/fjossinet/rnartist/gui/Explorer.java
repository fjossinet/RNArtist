package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.model.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Explorer {
    private Stage stage;
    private Mediator mediator;
    private TreeTableView<ExplorerItem> treeTableView;

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
        this.treeTableView.setEditable(true);

        this.treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.treeTableView.getSelectionModel().setCellSelectionEnabled(true);
        this.treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        TreeTableColumn<ExplorerItem, String> nameColumn = new TreeTableColumn<ExplorerItem, String>("Name");
        nameColumn.setContextMenu(new CMenu());
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
        lineWidthColumn.setUserData(DrawingConfigurationParameter.LineWidth);
        lineWidthColumn.setEditable(true);
        lineWidthColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("lineWidth"));
        lineWidthColumn.setCellFactory(LineWidthTableTreeCell.forTreeTableColumn(mediator));
        lineWidthColumn.setMinWidth(80);
        lineWidthColumn.setMaxWidth(80);
        lineWidthColumn.setSortable(false);

        TreeTableColumn<ExplorerItem, String> lineShiftColumn = new TreeTableColumn<ExplorerItem, String>("Shift");
        lineShiftColumn.setUserData(DrawingConfigurationParameter.LineShift);
        lineShiftColumn.setEditable(true);
        lineShiftColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("lineShift"));
        lineShiftColumn.setCellFactory(LineShiftTableTreeCell.forTreeTableColumn(mediator));
        lineShiftColumn.setMinWidth(60);
        lineShiftColumn.setMaxWidth(60);
        lineShiftColumn.setSortable(false);

        TreeTableColumn<ExplorerItem, String> opacityColumn = new TreeTableColumn<ExplorerItem, String>("Opacity");
        opacityColumn.setUserData(DrawingConfigurationParameter.Opacity);
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
                if (mouseEvent.getButton() != MouseButton.SECONDARY) {
                    if (!treeTableView.getSelectionModel().getSelectedItems().isEmpty()) {
                        mediator.getCanvas2D().clearSelection();
                        for (TreeItem<ExplorerItem> item : treeTableView.getSelectionModel().getSelectedItems()) {
                            if (SecondaryStructureItem.class.isInstance((item.getValue())))
                                continue;
                            else if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                                for (TreeItem<ExplorerItem> c: item.getChildren())
                                    mediator.getCanvas2D().addToSelection(c.getValue().getDrawingElement());
                            } else
                                mediator.getCanvas2D().addToSelection(item.getValue().getDrawingElement());
                        }
                        if (RnartistConfig.getFitDisplayOnSelection()) {
                            mediator.canvas2D.fitDisplayOn(mediator.getCurrent2DDrawing().getSelectionBounds());
                        } else if (RnartistConfig.getCenterDisplayOnSelection()) {
                            mediator.canvas2D.centerDisplayOn(mediator.getCurrent2DDrawing().getSelectionBounds());
                        }
                        mediator.canvas2D.repaint();
                    } else if (mediator.getCurrent2DDrawing() != null) {
                        //no selection
                        mediator.getCanvas2D().clearSelection();
                    }
                }
            }
        });
        treeTableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
                    for (TreeTablePosition<ExplorerItem, ?> cell : treeTableView.getSelectionModel().getSelectedCells()) {
                        ExplorerItem item = cell.getTreeItem().getValue();
                        if (item != null) {
                            if (cell.getTableColumn() == colorColumn)
                                item.setColor(null);
                            else if (cell.getTableColumn() == lineWidthColumn)
                                item.setLineWidth(null);
                            else if (cell.getTableColumn() == lineShiftColumn)
                                item.setLineShift(null);
                            else if (cell.getTableColumn() == opacityColumn)
                                item.setOpacity(null);
                            else if (cell.getTableColumn() == fullDetailsColumn)
                                item.setFullDetails(null);
                            treeTableView.refresh();
                        }
                    }
                    mediator.canvas2D.repaint();
                }  else if (keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.DOWN)) {
                    if (!treeTableView.getSelectionModel().getSelectedItems().isEmpty()) {
                        mediator.getCanvas2D().clearSelection();
                        for (TreeItem<ExplorerItem> item : treeTableView.getSelectionModel().getSelectedItems()) {
                            if (SecondaryStructureItem.class.isInstance((item.getValue())))
                                continue;
                            else if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                                for (TreeItem<ExplorerItem> c: item.getChildren())
                                    mediator.getCanvas2D().addToSelection(c.getValue().getDrawingElement());
                            } else
                                mediator.getCanvas2D().addToSelection(item.getValue().getDrawingElement());
                        }
                        if (RnartistConfig.getFitDisplayOnSelection()) {
                            mediator.canvas2D.fitDisplayOn(mediator.getCurrent2DDrawing().getSelectionBounds());
                        } else if (RnartistConfig.getCenterDisplayOnSelection()) {
                            mediator.canvas2D.centerDisplayOn(mediator.getCurrent2DDrawing().getSelectionBounds());
                        }
                        mediator.canvas2D.repaint();
                    } else if (mediator.getCurrent2DDrawing() != null) {
                        //no selection
                        mediator.getCanvas2D().clearSelection();
                    }
                }
            }
        });

        javafx.scene.control.ScrollPane sp = new ScrollPane(treeTableView);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        Scene scene = new Scene(sp);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("io/github/fjossinet/rnartist/gui/css/main.css").toExternalForm());

        /*JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);*/

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        int width = (int)(screenSize.getWidth()*4.0/10.0);
        scene.getWindow().setWidth(width);
        scene.getWindow().setHeight(screenSize.getHeight());
        scene.getWindow().setX(screenSize.getWidth()-width);
        scene.getWindow().setY(0);
    }

    public void applyTheme(Map<String, Map<String, String>> theme) {
        this.applyTheme(this.treeTableView.getRoot(), theme);
    }

    public void applyTheme(TreeItem<ExplorerItem> item, Map<String, Map<String, String>> theme) {
        item.getValue().applyTheme(theme);
        for (TreeItem<ExplorerItem> c:item.getChildren())
            applyTheme(c, theme);
    }

    public TreeItem<ExplorerItem> getTreeViewItemFor(TreeItem<ExplorerItem> item, Object o) {
        if (SecondaryStructureDrawing.class.isInstance(o)) //its the root element
            return this.treeTableView.getRoot();

        if (item != null && item.getValue().getDrawingElement() == o)
            return item;

        for (TreeItem<ExplorerItem> child : item.getChildren()) {
            TreeItem<ExplorerItem> _item = getTreeViewItemFor(child, o);
            if (_item != null)
                return _item;
        }
        return null;
    }

    public List<TreeItem<ExplorerItem>> getAllTreeViewItemsFrom(TreeItem<ExplorerItem> item) {
        List<TreeItem<ExplorerItem>> hits = new ArrayList<TreeItem<ExplorerItem>>();
        hits.add(item);
        for (TreeItem<ExplorerItem> c: item.getChildren())
            hits.addAll(getAllTreeViewItemsFrom(c));
        return hits;
    }

    public void selectAllTreeViewItems(DrawingElementFilter filter, List<TreeItem<ExplorerItem>> starts, boolean updateCanvas) {
        List<TreeItem<ExplorerItem>> hits = new ArrayList<TreeItem<ExplorerItem>>();
        clearSelection(); //we don't want in the selection the element we used at first to launch the selection (we selected an Helix to select all Residues -> we don't need/want the helix anymore in the selection)
        for (TreeItem<ExplorerItem> start:starts)
            getAllTreeViewItems(hits, start, filter);
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

    public List<TreeItem<ExplorerItem>> getAllTreeViewItems(List<TreeItem<ExplorerItem>> hits, TreeItem<ExplorerItem> start, DrawingElementFilter filter) {
        if (filter.isOK(start.getValue().getDrawingElement()))
            hits.add(start);

        for (TreeItem<ExplorerItem> child : start.getChildren())
            getAllTreeViewItems(hits, child, filter);

        return hits;
    }


    public void clearSelection() {
        this.treeTableView.getSelectionModel().clearSelection();
    }

    //++++++++++ methods to construct the treetable

    public void load(SecondaryStructureDrawing drawing) {
        TreeItem<ExplorerItem> root = new TreeItem<ExplorerItem>(new SecondaryStructureItem(drawing));
        root.setExpanded(true);

        TreeItem<ExplorerItem> allPknots = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Pseudoknots"));
        for (PKnotDrawing pknot : drawing.getPknots())
            allPknots.getChildren().addAll(this.load(pknot));
        root.getChildren().add(allPknots);

        TreeItem<ExplorerItem> allBranches = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Branches"));
        for (JunctionDrawing j: drawing.getBranches())
            allBranches.getChildren().addAll(this.load(j));
        root.getChildren().add(allBranches);

        TreeItem<ExplorerItem> allSingleStrands = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("SingleStrands"));
        for (SingleStrandDrawing ss : drawing.getSingleStrands())
            allSingleStrands.getChildren().addAll(this.load(ss));
        root.getChildren().add(allSingleStrands);

        TreeItem<ExplorerItem> allTertiaries = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Tertiaries"));
        for (TertiaryInteractionDrawing interaction : drawing.getTertiaryInteractions())
            if (interaction.getParent() == null) // if parent not null, this tertiary interaction is in a pknot and will be a child of this pknot
                allTertiaries.getChildren().addAll(this.load(interaction, true));

        root.getChildren().add(allTertiaries);
        this.treeTableView.setRoot(root);
    }

    private TreeItem<ExplorerItem> load(PKnotDrawing pknot) {
        TreeItem<ExplorerItem> pknotItem = new TreeItem<ExplorerItem>(new PknotItem(pknot));
        pknotItem.getChildren().add(this.load(pknot.helix));
        TreeItem<ExplorerItem> allTertiaries = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Tertiaries"));
        for (TertiaryInteractionDrawing interaction : pknot.getTertiaryInteractions())
            allTertiaries.getChildren().addAll(this.load(interaction, true));
        pknotItem.getChildren().add(allTertiaries);
        return pknotItem;
    }

    private TreeItem<ExplorerItem> load(HelixDrawing h) {
        TreeItem<ExplorerItem> helixItem = new TreeItem<ExplorerItem>(new HelixItem(h));

        TreeItem<ExplorerItem> secondaries = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Secondaries"));
        helixItem.getChildren().add(secondaries);
        for (SecondaryInteractionDrawing interaction : h.getSecondaryInteractions())
            secondaries.getChildren().addAll(this.load(interaction, false));

        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Phosphodiester Bonds"));
        helixItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: h.getPhosphoBonds())
            allPhosphos.getChildren().add(this.load(p));
        return helixItem;
    }

    private TreeItem<ExplorerItem> load(JunctionDrawing jc) {

        TreeItem<ExplorerItem> junctionItem = new TreeItem<ExplorerItem>(new JunctionItem(jc));

        TreeItem<ExplorerItem> residues = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Residues"));
        junctionItem.getChildren().add(residues);
        for (ResidueDrawing r : jc.getResidues())
            if (r.getParent() == jc)
                residues.getChildren().addAll(load(r));

        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Phosphodiester Bonds"));
        junctionItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: jc.getPhosphoBonds())
            allPhosphos.getChildren().add(this.load(p));

        for (JunctionDrawing connectedJC: jc.getConnectedJunctions().values())
            junctionItem.getChildren().add(this.load(connectedJC));

        TreeItem<ExplorerItem> helixItem = load((HelixDrawing)jc.getParent());
        helixItem.getChildren().add(junctionItem);
        return helixItem;
    }

    private TreeItem<ExplorerItem> load(SingleStrandDrawing ss) {
        TreeItem<ExplorerItem> ssItem = new TreeItem<ExplorerItem>(new SingleStrandItem(ss));

        TreeItem<ExplorerItem> residues = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Residues"));
        ssItem.getChildren().add(residues);
        for (ResidueDrawing r : ss.getResidues()) {
            if (r.getParent() == ss)
                residues.getChildren().addAll(load(r));
        }

        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Phosphodiester Bonds"));
        ssItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: ss.getPhosphoBonds())
            allPhosphos.getChildren().add(this.load(p));
        return ssItem;
    }

    private TreeItem<ExplorerItem> load(BaseBaseInteractionDrawing interaction, boolean isTertiary) {
        TreeItem<ExplorerItem> interactionItem =
                isTertiary ? new TreeItem<ExplorerItem>(new TertiaryInteractionItem((TertiaryInteractionDrawing) interaction)) : new TreeItem<ExplorerItem>(new SecondaryInteractionItem((SecondaryInteractionDrawing) interaction));

        TreeItem<ExplorerItem> interactionSymbolItem = new TreeItem<ExplorerItem>(new InteractionSymbolItem(interaction.getInteractionSymbol()));
        interactionItem.getChildren().add(interactionSymbolItem);
        if (interaction.getResidue().getParent() == interaction)
            interactionItem.getChildren().add(load(interaction.getResidue()));
        if (interaction.getPairedResidue().getParent() == interaction)
            interactionItem.getChildren().add(load(interaction.getPairedResidue()));
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

    private class CMenu extends ContextMenu {
        CMenu() {
            MenuItem clearSelection = new MenuItem("Clear Selection");
            this.getItems().add(clearSelection);
            clearSelection.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    mediator.getExplorer().clearSelection();
                    mediator.getCanvas2D().clearSelection();
                }
            });

            Menu selectMenu = new Menu("Select...");
            this.getItems().add(selectMenu);

            MenuItem selectionMode = new MenuItem("Helices");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> HelixDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("SingleStrands");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> SingleStrandDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("Junctions");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> JunctionDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("Apical Loops");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> JunctionDrawing.class.isInstance(drawingElement) && ((JunctionDrawing) drawingElement).getJunction().getType() == JunctionType.ApicalLoop, starts, true);
                }
            });

            selectionMode = new MenuItem("Inner Loops");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> JunctionDrawing.class.isInstance(drawingElement) && ((JunctionDrawing) drawingElement).getJunction().getType() == JunctionType.InnerLoop, starts, true);
                }
            });

            selectionMode = new MenuItem("PseudoKnots");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> PKnotDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("Secondary Interactions");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> SecondaryInteractionDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("Tertiary Interactions");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> TertiaryInteractionDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("Interaction Symbols");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> InteractionSymbolDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("PhosphoDiester Bonds");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> PhosphodiesterBondDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("Residues");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("Residue Letters");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueLetterDrawing.class.isInstance(drawingElement), starts, true);
                }
            });

            selectionMode = new MenuItem("As");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueDrawing.class.isInstance(drawingElement) && drawingElement.getType() == SecondaryStructureType.AShape, starts, true);
                }
            });

            selectionMode = new MenuItem("Us");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueDrawing.class.isInstance(drawingElement) && drawingElement.getType() == SecondaryStructureType.UShape, starts, true);
                }
            });

            selectionMode = new MenuItem("Gs");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueDrawing.class.isInstance(drawingElement) && drawingElement.getType() == SecondaryStructureType.GShape, starts, true);
                }
            });

            selectionMode = new MenuItem("Cs");
            selectMenu.getItems().add(selectionMode);
            selectionMode.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                    if (getTreeTableView().getSelectionModel().getSelectedItems().isEmpty())
                        starts.add(getTreeTableView().getRoot());
                    else
                        starts.addAll(getTreeTableView().getSelectionModel().getSelectedItems());
                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> ResidueDrawing.class.isInstance(drawingElement) && drawingElement.getType() == SecondaryStructureType.CShape, starts, true);
                }
            });

        }
    }

}
