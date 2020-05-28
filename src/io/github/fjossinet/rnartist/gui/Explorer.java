package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.RNArtist;
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

import java.awt.*;
import java.util.*;
import java.util.List;

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;

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

    private TreeItem getTreeViewItemFor(TreeItem<ExplorerItem> item , Object sse) {
        if (SecondaryStructureDrawing.class.isInstance(sse)) //its the root element
            return this.treeTableView.getRoot();

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
        root.getChildren().add(load(drawing.getTheme()));

        TreeItem<ExplorerItem> allHelices = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Helices"));

        for (HelixLine h: drawing.getAllHelices())
            allHelices.getChildren().addAll(this.load(h));
        root.getChildren().add(allHelices);

        TreeItem<ExplorerItem> allJunctions = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Junctions"));
        for (JunctionCircle jc:drawing.getAllJunctions())
            allJunctions.getChildren().addAll(this.load(jc));
        root.getChildren().add(allJunctions);

        TreeItem<ExplorerItem> allSingleStrands = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("SingleStrands"));
        for (SingleStrandLine ss:drawing.getSingleStrands())
            allSingleStrands.getChildren().addAll(this.load(ss));
        root.getChildren().add(allSingleStrands);

        TreeItem<ExplorerItem> allTertiaries = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Tertiaries"));
        for (TertiaryInteractionLine interaction:drawing.getTertiaryInteractions())
            allTertiaries.getChildren().addAll(this.load(interaction, true));

        root.getChildren().add(allTertiaries);
        this.treeTableView.setRoot(root);
    }

    private TreeItem<ExplorerItem> load(HelixLine h) {
        TreeItem<ExplorerItem> helixItem = new TreeItem<ExplorerItem>(new HelixItem(h));

        helixItem.getChildren().add(load(h.getTheme()));

        for (SecondaryInteractionLine interaction: h.getSecondaryInteractions())
            helixItem.getChildren().addAll(this.load(interaction, false));
        return helixItem;
    }

    private TreeItem<ExplorerItem> load(JunctionCircle jc) {
        TreeItem<ExplorerItem> junctionItem = new TreeItem<ExplorerItem>(new JunctionItem(jc));

        junctionItem.getChildren().add(load(jc.getTheme()));

        for (ResidueCircle r: jc.getResidues())
            if (r.getParent() == jc)
                junctionItem.getChildren().addAll(load(r));
        return junctionItem;
    }

    private TreeItem<ExplorerItem> load(SingleStrandLine ss) {
        TreeItem<ExplorerItem> ssItem = new TreeItem<ExplorerItem>(new SingleStrandItem(ss));

        ssItem.getChildren().add(load(ss.getTheme()));

        for (ResidueCircle r: ss.getResidues())
            if (r.getParent() == ss)
                ssItem.getChildren().add(load(r));
        return ssItem;
    }

    private TreeItem<ExplorerItem> load(BaseBaseInteraction interaction, boolean isTertiary) {
        TreeItem<ExplorerItem> interactionItem =
                isTertiary ? new TreeItem<ExplorerItem>(new TertiaryInteractionItem((TertiaryInteractionLine)interaction)) : new TreeItem<ExplorerItem>(new SecondaryInteractionItem((SecondaryInteractionLine)interaction));

        interactionItem.getChildren().add(load(interaction.getTheme()));

        TreeItem<ExplorerItem> regularSymbols = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("Regular Symbols"));
        interactionItem.getChildren().add(regularSymbols);

        for (LWSymbol s: interaction.getRegularSymbols())
            regularSymbols.getChildren().add(load(s));

        TreeItem<ExplorerItem> lwSymbols = new TreeItem<ExplorerItem>(new GroupOfStructuralElements("LW Symbols"));
        interactionItem.getChildren().add(lwSymbols);

        for (LWSymbol s: interaction.getLwSymbols())
            lwSymbols.getChildren().add(load(s));

        interactionItem.getChildren().add(load(interaction.getResidue()));
        interactionItem.getChildren().add(load(interaction.getPairedResidue()));
        return interactionItem;
    }

    private TreeItem<ExplorerItem> load(ResidueCircle r) {
        TreeItem<ExplorerItem> residueItem = new TreeItem<ExplorerItem>(new ResidueItem(r));

        residueItem.getChildren().add(load(r.getTheme()));

        return residueItem;
    }

    private TreeItem<ExplorerItem> load(LWSymbol s) {
        TreeItem<ExplorerItem> symbolItem = new TreeItem<ExplorerItem>(new LWSymbolItem(s));

        symbolItem.getChildren().add(load(s.getTheme()));

        return symbolItem;
    }

    private TreeItem<ExplorerItem> load(Theme theme) {
        TreeItem<ExplorerItem> themeItem = new TreeItem<ExplorerItem>(new ThemeItem());
        SortedSet<String> keys = new TreeSet<>(theme.getParams().keySet());
        for (String key : keys)
            themeItem.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(key, theme.getParams().get(key))));
        return themeItem;
    }

    public void clearSelection() {
        this.treeTableView.getSelectionModel().clearSelection();
    }

    /**
     *
     * @param listener
     * @param param
     * @param value can be null. If null, the theme parameter is removed from the theme.
     */
    public void setThemeParameter(ThemeConfiguratorListener listener, ThemeParameter param, String value) {
        TreeItem<ExplorerItem> item = this.getTreeViewItemFor(this.treeTableView.getRoot(), listener);
        if (item != null) {
            TreeItem<ExplorerItem> theme = item.getChildren().get(0);
            TreeItem<ExplorerItem> toRemove = null;
            for (TreeItem<ExplorerItem> _param:theme.getChildren()) {
                if (_param.getValue().getName().equals(param.toString())) {
                    if (value != null) {
                        _param.getValue().setValue(value);
                        treeTableView.refresh();
                        return;
                    }
                    else {
                        toRemove = _param;
                        break;
                    }
                }
            }
            if (toRemove != null) {
                theme.getChildren().remove(toRemove);
                return;
            }
            if (value != null)
                //this parameter was not set so far
                theme.getChildren().add(new TreeItem<ExplorerItem>(new ThemeParameterItem(param.toString(), value)));
        }
    }

    public void setThemeParameter(ThemeConfiguratorListener listener, ThemeParameter param, int value) {
        this.setThemeParameter(listener, param, ""+value);
    }

    public void setThemeParameter(ThemeConfiguratorListener listener, ThemeParameter param, double value) {
        this.setThemeParameter(listener, param, ""+value);
    }

    public void setThemeParameter(ThemeConfiguratorListener listener, ThemeParameter param, Color value) {
        this.setThemeParameter(listener, param, getHTMLColorString(value));
    }

    public void clearThemeParameter(ThemeConfiguratorListener listener, ThemeParameter param) {
        if (SecondaryStructureDrawing.class.isInstance(listener)) //if we cleared the theme param of the full 2D, we restore the default value.
            this.setThemeParameter(listener, param, RnartistConfig.defaultThemeParams.get(param.toString()));
        else //for all the other elements, we delete the entry in its theme
            this.setThemeParameter(listener, param, (String)null);
        treeTableView.refresh();
    }

    public void applyTheme(ThemeConfiguratorListener listener, Theme theme) {
        TreeItem<ExplorerItem> item = this.getTreeViewItemFor(this.treeTableView.getRoot(), listener);
        if (item != null) {
            item.getChildren().remove(0); //the first children is always the Theme for an Explorer item
            item.getChildren().add(0, load(theme));
        }
    }

    public void clearTheme(ThemeConfiguratorListener listener) {
        TreeItem<ExplorerItem> item = this.getTreeViewItemFor(this.treeTableView.getRoot(), listener);
        if (item != null)
            this.clearTheme(item);
        if (SecondaryStructureDrawing.class.isInstance(listener)) //if we cleared the theme of the full 2D, we restore the default one.
            applyTheme(listener, new Theme());
        treeTableView.refresh();
    }

    private void clearTheme(TreeItem<ExplorerItem> item) {
        for (TreeItem<ExplorerItem> _item: item.getChildren())
            if (ThemeItem.class.isInstance(_item.getValue()))
                _item.getChildren().clear();
            else if (ThemeConfiguratorListener.class.isInstance(_item.getValue().getSecondaryStructureElement()))
                clearTheme(_item);
            else if (_item.getValue().getSecondaryStructureElement() == null) {//SecondaryStructureDrawing
                clearTheme(_item);
            }
    }
}
