package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.model.ExplorerItem;
import io.github.fjossinet.rnartist.model.GroupOfStructuralElements;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class FullDetailsTreeTableCell<T> extends TreeTableCell<T, String> {

    private Mediator mediator;

    public FullDetailsTreeTableCell(Mediator mediator) {
        this.mediator = mediator;
        setContextMenu(new FullDetailsTreeTableCell.CMenu());
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        setGraphic(null);
        if (!empty) {
            Glyph g = null;
            if (value == "true")
                g = new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE);
            else if (value == "false")
                g = new Glyph("FontAwesome", FontAwesome.Glyph.TIMES_CIRCLE);
            if (g != null) {
                g.setFontSize(20);
                Label l = new Label(null, g);
                setGraphic(l);
                setAlignment(Pos.CENTER);
            }
        }
    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn(Mediator mediator) {
        return (TreeTableColumn<T, String> tableColumn) -> new FullDetailsTreeTableCell<>(mediator);
    }

    private class CMenu extends ContextMenu {
        CMenu() {
            Menu setDetails = new Menu("Full Details...");
            this.getItems().add(setDetails);
            MenuItem fullDetails = new MenuItem("Yes");
            setDetails.getItems().add(fullDetails);
            fullDetails.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    for (TreeItem item : FullDetailsTreeTableCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child : item.getChildren()) {
                                ((ExplorerItem) ((TreeItem) child).getValue()).setFullDetails("true");
                            }
                        } else {
                            ((ExplorerItem) item.getValue()).setFullDetails("true");
                        }
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
            MenuItem lowDetails = new MenuItem("No");
            setDetails.getItems().add(lowDetails);
            lowDetails.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    for (TreeItem item : FullDetailsTreeTableCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child : item.getChildren()) {
                                ((ExplorerItem) ((TreeItem) child).getValue()).setFullDetails("false");
                            }
                        } else {
                            ((ExplorerItem) item.getValue()).setFullDetails("false");
                        }
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });

            final Menu clearDetails = new Menu("Clear Details...");
            this.getItems().add(clearDetails);
            final MenuItem here = new MenuItem("Here");
            clearDetails.getItems().add(here);
            here.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: FullDetailsTreeTableCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                ((ExplorerItem)((TreeItem)child).getValue()).setFullDetails(null);
                        }
                        else
                            ((ExplorerItem)item.getValue()).setFullDetails(null);
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
            final MenuItem fromHere = new MenuItem("From Here");
            clearDetails.getItems().add(fromHere);
            fromHere.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: FullDetailsTreeTableCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setFullDetails(null);
                        }
                        else
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setFullDetails(null);
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
        }
    }
}
