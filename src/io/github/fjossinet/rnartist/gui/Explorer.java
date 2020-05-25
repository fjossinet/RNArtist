package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.model.*;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.*;

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

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {
        this.treeTableView = new TreeTableView<ExplorerItem>();
        this.treeTableView.setEditable(true);
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        TreeTableColumn<ExplorerItem, String> nameCol = new TreeTableColumn<ExplorerItem, String>("Name");
        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("name"));
        TreeTableColumn<ExplorerItem, String> locationCol = new TreeTableColumn<ExplorerItem, String>("Details");
        locationCol.setCellValueFactory(new TreeItemPropertyValueFactory<ExplorerItem, String>("value"));
        treeTableView.getColumns().addAll(nameCol, locationCol);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!treeTableView.getSelectionModel().getSelectedItems().isEmpty()) {
                    int i = 0;
                    for (TreeItem<ExplorerItem> item : treeTableView.getSelectionModel().getSelectedItems()) {
                        if (item.getValue().getSecondaryStructureElement() != null) {
                            mediator.addToSelection(Mediator.SelectionEmitter.EXPLORER, i == 0, item.getValue().getSecondaryStructureElement()); //if the first element added to the selection, we clear the previous selection
                            i++;
                        } else if (item.getValue().getName().equals("Full 2D")) {
                            mediator.addToSelection(null, true, null); //if the full 2D (the root of the explorer) is in the selection, we do nothing else
                            if (RnartistConfig.getFitDisplayOnSelection()) { //fit first since fit will center too. we center on the full 2D since the selection has been cleared above, so nothing to center on
                                mediator.canvas2D.fitDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                            } else if (RnartistConfig.getCenterDisplayOnSelection()) {
                                mediator.canvas2D.centerDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                            }
                            break;
                        }
                    }
                    mediator.canvas2D.repaint();
                } else {
                    //no selection
                    mediator.addToSelection(Mediator.SelectionEmitter.EXPLORER, true,null);
                    mediator.canvas2D.repaint();
                }
            }
        });
        treeTableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals( KeyCode.BACK_SPACE )) {
                    List<TreeItem<ExplorerItem>> toDelete = new ArrayList<TreeItem<ExplorerItem>>();
                    for (TreeItem<ExplorerItem> item: treeTableView.getSelectionModel().getSelectedItems()) {
                        if (ThemeParameterItem.class.isInstance(item.getValue())) {
                                ExplorerItem p  = item.getParent().getParent().getValue();
                                if (!SecondaryStructureItem.class.isInstance(p)) { //the user cannot delete theme params from the full 2D
                                    p.getSecondaryStructureElement().getTheme().getParams().remove(item.getValue().getName());
                                    toDelete.add(item);
                                }
                        }
                    }
                    for (TreeItem<ExplorerItem> item:toDelete)
                        item.getParent().getChildren().remove(item);
                    mediator.canvas2D.repaint();
                }
            }
        });
        Scene scene = new Scene(treeTableView);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("io/github/fjossinet/rnartist/gui/css/explorer.css").toExternalForm());

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(450);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(screenSize.getWidth()-450);
        this.stage.setY(0);
    }

    public void select(SecondaryStructureElement sse) {
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

    private TreeItem getTreeViewItemFor(TreeItem<ExplorerItem> item , SecondaryStructureElement sse) {
        if (item != null && item.getValue().getSecondaryStructureElement() == sse)
            return item;

        for (TreeItem<ExplorerItem> child : item.getChildren()){
            TreeItem<String> _item = getTreeViewItemFor(child, sse);
            if( _item != null)
                return _item;
        }
        return null;
    }

    public void load(SecondaryStructureDrawing drawing) {
        TreeItem<ExplorerItem> root = new TreeItem<ExplorerItem>(new SecondaryStructureItem(drawing));
        root.setExpanded(true);
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        root.getChildren().add(themeItem);
        SortedSet<String> keys = new TreeSet<>(drawing.getTheme().getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, drawing.getTheme().getParams().get(key))));
        TreeItem<ExplorerItem> allHelices = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Helices"));

        for (HelixLine h: drawing.getAllHelices())
            allHelices.getChildren().addAll(this.load(drawing, h));
        root.getChildren().add(allHelices);

        TreeItem<ExplorerItem> allJunctions = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Junctions"));
        for (JunctionCircle jc:drawing.getAllJunctions())
            allJunctions.getChildren().addAll(this.load(drawing, jc));
        root.getChildren().add(allJunctions);

        TreeItem<ExplorerItem> allSingleStrands = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("SingleStrands"));
        for (SingleStrandLine ss:drawing.getSingleStrands())
            allSingleStrands.getChildren().addAll(this.load(drawing, ss));
        root.getChildren().add(allSingleStrands);

        TreeItem<ExplorerItem> allTertiaries = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Tertiaries"));
        for (TertiaryInteractionLine interaction:drawing.getTertiaryInteractions())
            allTertiaries.getChildren().addAll(this.load(drawing,interaction, true));

        root.getChildren().add(allTertiaries);
        this.treeTableView.setRoot(root);
    }


    private TreeItem<ExplorerItem> load(SecondaryStructureDrawing drawing, HelixLine h) {
        TreeItem<ExplorerItem> helixItem = new TreeItem<ExplorerItem>(new HelixItem(h));
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        helixItem.getChildren().add(themeItem);
        SortedSet<String> keys = new TreeSet<>(h.getTheme().getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, h.getTheme().getParams().get(key))));
        for (SecondaryInteractionLine interaction: h.getSecondaryInteractions())
            helixItem.getChildren().addAll(this.load(drawing,interaction, false));
        return helixItem;
    }

    private TreeItem<ExplorerItem> load(SecondaryStructureDrawing drawing, JunctionCircle jc) {
        TreeItem<ExplorerItem> junctionItem = new TreeItem<ExplorerItem>(new JunctionItem(jc));
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        junctionItem.getChildren().add(themeItem);
        SortedSet<String> keys = new TreeSet<>(jc.getTheme().getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, jc.getTheme().getParams().get(key))));
        for (ResidueCircle r: drawing.getResidues())
            if (r.getParent() == jc)
                junctionItem.getChildren().addAll(load(drawing,r));
        return junctionItem;
    }

    private TreeItem<ExplorerItem> load(SecondaryStructureDrawing drawing, SingleStrandLine ss) {
        TreeItem<ExplorerItem> ssItem = new TreeItem<ExplorerItem>(new SingleStrandItem(ss));
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        ssItem.getChildren().add(themeItem);
        SortedSet<String> keys = new TreeSet<>(ss.getTheme().getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, ss.getTheme().getParams().get(key))));
        for (ResidueCircle r: drawing.getResidues())
            if (r.getParent() == ss)
                ssItem.getChildren().add(load(drawing,r));
        return ssItem;
    }

    private TreeItem<ExplorerItem> load(SecondaryStructureDrawing drawing, BaseBaseInteraction interaction, boolean isTertiary) {
        TreeItem<ExplorerItem> interactionItem =
                isTertiary ? new TreeItem<ExplorerItem>(new TertiaryInteractionItem((TertiaryInteractionLine)interaction)) : new TreeItem<ExplorerItem>(new SecondaryInteractionItem((SecondaryInteractionLine)interaction));
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        interactionItem.getChildren().add(themeItem);
        SortedSet<String> keys = new TreeSet<>(interaction.getTheme().getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, interaction.getTheme().getParams().get(key))));

        TreeItem<ExplorerItem> regularSymbols = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Regular Symbols"));
        interactionItem.getChildren().add(regularSymbols);

        for (LWSymbol s: interaction.getRegularSymbols())
            regularSymbols.getChildren().add(load(drawing,s));

        TreeItem<ExplorerItem> lwSymbols = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("LW Symbols"));
        interactionItem.getChildren().add(lwSymbols);

        for (LWSymbol s: interaction.getLwSymbols())
            lwSymbols.getChildren().add(load(drawing,s));

        for (ResidueCircle r: drawing.getResidues())
            if (r.getParent() == interaction)
                interactionItem.getChildren().add(load(drawing,r));
        return interactionItem;
    }

    private TreeItem<ExplorerItem> load(SecondaryStructureDrawing drawing, ResidueCircle r) {
        TreeItem<ExplorerItem> residueItem = new TreeItem<ExplorerItem>(new ResidueItem(r));
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        residueItem.getChildren().add(themeItem);
        SortedSet<String> keys = new TreeSet<>(r.getTheme().getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, r.getTheme().getParams().get(key))));
        return residueItem;
    }

    private TreeItem<ExplorerItem> load(SecondaryStructureDrawing drawing, LWSymbol s) {
        TreeItem<ExplorerItem> symbolItem = new TreeItem<ExplorerItem>(new LWSymbolItem(s));
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        symbolItem.getChildren().add(themeItem);
        SortedSet<String> keys = new TreeSet<>(s.getTheme().getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, s.getTheme().getParams().get(key))));
        return symbolItem;
    }

    public void clearSelection() {
        this.treeTableView.getSelectionModel().clearSelection();
    }

    public void setThemeParameter(SecondaryStructureElement element, String key, String value) {
        TreeItem<ExplorerItem> item = this.getTreeViewItemFor(this.treeTableView.getRoot(), element);
        if (item != null) {
            TreeItem<ExplorerItem> theme = item.getChildren().get(0);
            for (TreeItem<ExplorerItem> param:theme.getChildren()) {
                if (param.getValue().getName().equals(key)) {
                    param.getValue().setValue(value);
                    treeTableView.refresh();
                    return;
                }
            }
            //this parameter was not set so far
            theme.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, value)));
        }
    }

    public void setThemeParameter(SecondaryStructureDrawing element, String key, String value) {
        TreeItem<ExplorerItem> theme = this.treeTableView.getRoot().getChildren().get(0);
        for (TreeItem<ExplorerItem> param : theme.getChildren()) {
            if (param.getValue().getName().equals(key)) {
                param.getValue().setValue(value);
                treeTableView.refresh();
                return;
            }
        }
    }
}
