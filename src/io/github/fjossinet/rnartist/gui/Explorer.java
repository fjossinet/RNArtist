package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.model.*;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

    public void refresh() {
        this.treeTableView.refresh();
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {
        this.treeTableView = new TreeTableView<ExplorerItem>();
        this.treeTableView.setEditable(true);
        this.treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        this.treeTableView.getSelectionModel().setCellSelectionEnabled(true);
        TreeTableColumn<ExplorerItem, String> nameCol = new TreeTableColumn<ExplorerItem, String>("Name");
        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("name"));
        nameCol.setPrefWidth(200);

        TreeTableColumn<ExplorerItem, String> locationColumn = new TreeTableColumn<ExplorerItem, String>("Location");
        locationColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("location"));
        locationColumn.setCellFactory(TextTreeTableCell.forTreeTableColumn());

        TreeTableColumn<ExplorerItem, String> colorColumn = new TreeTableColumn<ExplorerItem, String>("Shape Color");
        colorColumn.setUserData(DrawingConfigurationParameter.Color);
        colorColumn.setEditable(true);
        colorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("color"));
        colorColumn.setCellFactory(ColorTreeTableCell.forTreeTableColumn());

        TreeTableColumn<ExplorerItem, String> charColorColumn = new TreeTableColumn<ExplorerItem, String>("Character Color");
        charColorColumn.setUserData(DrawingConfigurationParameter.CharColor);
        charColorColumn.setEditable(true);
        charColorColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("charColor"));
        charColorColumn.setCellFactory(ColorTreeTableCell.forTreeTableColumn());

        TreeTableColumn<ExplorerItem, String> lineWidthColumn = new TreeTableColumn<ExplorerItem, String>("Line Width");
        lineWidthColumn.setUserData(DrawingConfigurationParameter.LineWidth);
        lineWidthColumn.setEditable(true);
        lineWidthColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("lineWidth"));
        lineWidthColumn.setCellFactory(LineWidthTableTreeCell.forTreeTableColumn());

        TreeTableColumn<ExplorerItem, String> lineShiftColumn = new TreeTableColumn<ExplorerItem, String>("Line Shift");
        lineShiftColumn.setUserData(DrawingConfigurationParameter.LineShift);
        lineShiftColumn.setEditable(true);
        lineShiftColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("lineShift"));
        lineShiftColumn.setCellFactory(TextTreeTableCell.forTreeTableColumn());

        TreeTableColumn<ExplorerItem, String> opacityColumn = new TreeTableColumn<ExplorerItem, String>("Opacity");
        opacityColumn.setUserData(DrawingConfigurationParameter.Opacity);
        opacityColumn.setEditable(true);
        opacityColumn.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("opacity"));
        opacityColumn.setCellFactory(TextTreeTableCell.forTreeTableColumn());

        treeTableView.getColumns().addAll(nameCol, locationColumn, colorColumn, charColorColumn, lineWidthColumn, lineShiftColumn, opacityColumn);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!treeTableView.getSelectionModel().getSelectedItems().isEmpty()) {
                    int i = 0;
                    for (TreeItem<ExplorerItem> item : treeTableView.getSelectionModel().getSelectedItems()) {
                        if (item.getValue().getDrawingElement() != null) {
                            mediator.addToSelection(Mediator.SelectionEmitter.EXPLORER, i == 0, item.getValue().getDrawingElement()); //if the first element added to the selection, we clear the previous selection
                            i++;
                        } else if (item.getValue().getName().equals("Full 2D")) {
                            mediator.addToSelection(Mediator.SelectionEmitter.EXPLORER, true, null); //if the full 2D (the root of the explorer) is in the selection, we do nothing else
                            if (RnartistConfig.getFitDisplayOnSelection()) { //fit first since fit will center too. we center on the full 2D since the selection has been cleared above, so nothing to center on
                                mediator.canvas2D.fitDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                            } else if (RnartistConfig.getCenterDisplayOnSelection()) {
                                mediator.canvas2D.centerDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                            }
                            break;
                        }
                    }
                    mediator.canvas2D.repaint();
                } else if (mediator.getCurrent2DDrawing() != null) {
                    //no selection
                    mediator.addToSelection(Mediator.SelectionEmitter.EXPLORER, true, null);
                    mediator.canvas2D.repaint();
                }
            }
        });
        treeTableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
                    for (TreeTablePosition<ExplorerItem, ?> cell : treeTableView.getSelectionModel().getSelectedCells()) {
                        DrawingElement element = cell.getTreeItem().getValue().getDrawingElement();
                        if (element != null) {
                            cell.getTreeItem().getValue().setDrawingConfigurationParameter(((DrawingConfigurationParameter) cell.getTableColumn().getUserData()).toString(), null);
                            treeTableView.refresh();
                        }
                    }
                    mediator.canvas2D.repaint();
                }
            }
        });
        javafx.scene.control.ScrollPane sp = new ScrollPane(treeTableView);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        Scene scene = new Scene(sp);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("io/github/fjossinet/rnartist/gui/css/explorer.css").toExternalForm());

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        scene.getWindow().setWidth(screenSize.getWidth() - 400);
        scene.getWindow().setHeight(200);
        scene.getWindow().setX(400);
        scene.getWindow().setY(screenSize.getHeight() - 200);
    }

    public void select(DrawingElement sse) {
        TreeItem item = this.getTreeViewItemFor(this.treeTableView.getRoot(), sse);
        if (item != null) {
            item.setExpanded(true);
            TreeItem parent = item.getParent();
            while (parent != null) {
                parent.setExpanded(true);
                parent = parent.getParent();
            }
            treeTableView.scrollTo(treeTableView.getRow(item));
            this.treeTableView.getSelectionModel().select(item);
        }
    }

    private TreeItem getTreeViewItemFor(TreeItem<ExplorerItem> item, Object sse) {
        if (SecondaryStructureDrawing.class.isInstance(sse)) //its the root element
            return this.treeTableView.getRoot();

        if (item != null && item.getValue().getDrawingElement() == sse)
            return item;

        for (TreeItem<ExplorerItem> child : item.getChildren()) {
            TreeItem<String> _item = getTreeViewItemFor(child, sse);
            if (_item != null)
                return _item;
        }
        return null;
    }

    public void load(SecondaryStructureDrawing drawing) {
        TreeItem<ExplorerItem> root = new TreeItem<ExplorerItem>(new SecondaryStructureItem(drawing));
        root.setExpanded(true);

        TreeItem<ExplorerItem> allPknots = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Pseudoknots"));
        for (PKnotDrawing pknot : drawing.getPknots())
            allPknots.getChildren().addAll(this.load(pknot));
        root.getChildren().add(allPknots);

        TreeItem<ExplorerItem> allHelices = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Helices"));
        for (HelixDrawing h : drawing.getAllHelices())
            if (h.getParent() == null) // if parent not null, this helix is in a pknot and will be a child of this pknot
                allHelices.getChildren().addAll(this.load(h));
        root.getChildren().add(allHelices);

        TreeItem<ExplorerItem> allJunctions = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Junctions"));
        for (JunctionDrawing jc : drawing.getAllJunctions())
            allJunctions.getChildren().addAll(this.load(jc));
        root.getChildren().add(allJunctions);

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
        for (TertiaryInteractionDrawing interaction : pknot.getTertiaryInteractions())
            pknotItem.getChildren().addAll(this.load(interaction, true));
        return pknotItem;
    }

    private TreeItem<ExplorerItem> load(HelixDrawing h) {
        TreeItem<ExplorerItem> helixItem = new TreeItem<ExplorerItem>(new HelixItem(h));
        for (SecondaryInteractionDrawing interaction : h.getSecondaryInteractions())
            helixItem.getChildren().addAll(this.load(interaction, false));
        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Phosphodiester Bonds"));
        helixItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: h.getPhosphoBonds())
            allPhosphos.getChildren().add(this.load(p));
        return helixItem;
    }

    private TreeItem<ExplorerItem> load(JunctionDrawing jc) {
        TreeItem<ExplorerItem> junctionItem = new TreeItem<ExplorerItem>(new JunctionItem(jc));
        for (ResidueDrawing r : jc.getResidues())
            junctionItem.getChildren().addAll(load(r));
        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Phosphodiester Bonds"));
        junctionItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: jc.getPhosphoBonds())
            allPhosphos.getChildren().add(this.load(p));
        return junctionItem;
    }

    private TreeItem<ExplorerItem> load(SingleStrandDrawing ss) {
        TreeItem<ExplorerItem> ssItem = new TreeItem<ExplorerItem>(new SingleStrandItem(ss));
        for (ResidueDrawing r : ss.getResidues())
            ssItem.getChildren().add(load(r));
        TreeItem<ExplorerItem> allPhosphos = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Phosphodiester Bonds"));
        ssItem.getChildren().add(allPhosphos);
        for (PhosphodiesterBondDrawing p: ss.getPhosphoBonds())
            allPhosphos.getChildren().add(this.load(p));
        return ssItem;
    }

    private TreeItem<ExplorerItem> load(BaseBaseInteractionDrawing interaction, boolean isTertiary) {
        TreeItem<ExplorerItem> interactionItem =
                isTertiary ? new TreeItem<ExplorerItem>(new TertiaryInteractionItem((TertiaryInteractionDrawing) interaction)) : new TreeItem<ExplorerItem>(new SecondaryInteractionItem((SecondaryInteractionDrawing) interaction));
        TreeItem<ExplorerItem> regularSymbols = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Regular Symbols"));
        interactionItem.getChildren().add(regularSymbols);

        for (LWSymbolDrawing s : interaction.getRegularSymbols())
            regularSymbols.getChildren().add(load(s));

        TreeItem<ExplorerItem> lwSymbols = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("LW Symbols"));
        interactionItem.getChildren().add(lwSymbols);

        for (LWSymbolDrawing s : interaction.getLwSymbols())
            lwSymbols.getChildren().add(load(s));

        interactionItem.getChildren().add(load(interaction.getResidue()));
        interactionItem.getChildren().add(load(interaction.getPairedResidue()));
        return interactionItem;
    }

    private TreeItem<ExplorerItem> load(ResidueDrawing r) {
        TreeItem<ExplorerItem> residueItem = new TreeItem<ExplorerItem>(new ResidueItem(r));
        return residueItem;
    }

    private TreeItem<ExplorerItem> load(PhosphodiesterBondDrawing phospho) {
        TreeItem<ExplorerItem> phosphoItem = new TreeItem<ExplorerItem>(new PhosphodiesterItem(phospho));
        return phosphoItem;
    }

    private TreeItem<ExplorerItem> load(LWSymbolDrawing s) {
        TreeItem<ExplorerItem> symbolItem = new TreeItem<ExplorerItem>(new LWSymbolItem(s));
        return symbolItem;
    }

    public void clearSelection() {
        this.treeTableView.getSelectionModel().clearSelection();
    }

}
