package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.model.ExplorerItem;
import io.github.fjossinet.rnartist.model.GroupOfStructuralElements;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;

public final class ColorTreeTableCell<T> extends TreeTableCell<T, String> {

    private Mediator mediator;
    private final Region graphic;

    public ColorTreeTableCell(Mediator mediator) {
        this.mediator = mediator;
        setContextMenu(new CMenu());
        this.graphic = new Region();
        this.graphic.setMaxHeight(15);
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        setGraphic(null);
        if (!empty && value != null && !value.isEmpty()) {
            setText(null);
            String style = null;
            if (value != null && !empty) {
                style = String.format("-fx-background-color: %s ; -fx-border-width: 1; -fx-border-color: dimgrey;", value);
                graphic.setStyle(style);
                setGraphic(graphic);
                setAlignment(Pos.CENTER);
            }
        }

    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn(Mediator mediator) {
        return (TreeTableColumn<T, String> tableColumn) -> new ColorTreeTableCell<>(mediator);
    }

    private class CMenu extends ContextMenu {
        CMenu() {
            final Menu colors = new Menu("Set Colors...");
            colors.getStyleClass().add("no-highlight");
            this.getItems().add(colors);
            final ColorPicker colorsPicker = new ColorPicker();
            final MenuItem setColor = new MenuItem(null,colorsPicker);
            colors.getItems().add(setColor);
            setColor.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: ColorTreeTableCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                ((ExplorerItem)((TreeItem)child).getValue()).setColor(getHTMLColorString(javaFXToAwt(colorsPicker.getValue())));
                        }
                        else
                            ((ExplorerItem)item.getValue()).setColor(getHTMLColorString(javaFXToAwt(colorsPicker.getValue())));
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
            final Menu clearColors = new Menu("Clear Colors...");
            this.getItems().add(clearColors);
            final MenuItem here = new MenuItem("Here");
            clearColors.getItems().add(here);
            here.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: ColorTreeTableCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                ((ExplorerItem)((TreeItem)child).getValue()).setColor(null);
                        }
                        else
                            ((ExplorerItem)item.getValue()).setColor(null);
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
            final MenuItem fromHere = new MenuItem("From Here");
            clearColors.getItems().add(fromHere);
            fromHere.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: ColorTreeTableCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setColor(null);
                        }
                        else
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setColor(null);
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
        }
    }
}
