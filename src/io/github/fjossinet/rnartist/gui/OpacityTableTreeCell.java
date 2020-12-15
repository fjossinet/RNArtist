package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.DrawingConfigurationParameter;
import io.github.fjossinet.rnartist.core.model.RnartistConfig;
import io.github.fjossinet.rnartist.core.model.SecondaryStructureType;
import io.github.fjossinet.rnartist.model.ExplorerItem;
import io.github.fjossinet.rnartist.model.GroupOfStructuralElements;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class OpacityTableTreeCell<T> extends TreeTableCell<T, String> {

    private Mediator mediator;

    public OpacityTableTreeCell(Mediator mediator) {
        this.setContextMenu(new CMenu());
        this.mediator = mediator;
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        setGraphic(null);
        if (!empty && value != null && !value.isEmpty()) {
            int opacity = (int) ((double) (Integer.parseInt(value) / 255.0 * 100.0));
            setText(Integer.toString(opacity)+"%");
            setAlignment(Pos.CENTER);
        }
    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn(Mediator mediator) {
        return (TreeTableColumn<T, String> tableColumn) -> new OpacityTableTreeCell<>(mediator);
    }

    private class CMenu extends ContextMenu {
        CMenu() {
            final Menu opacity = new Menu("Set Opacity");
            opacity.getStyleClass().add("no-highlight");
            this.getItems().add(opacity);
            Slider slider = new Slider(0, 100,100);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit(25);
            slider.setMinorTickCount(5);
            slider.setShowTickMarks(true);
            slider.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    int opacity = (int) ((double) (slider.getValue()) / 100.0 * 255.0);
                    for (TreeItem item: OpacityTableTreeCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                ((ExplorerItem)((TreeItem)child).getValue()).setOpacity(Integer.toString(opacity));
                        }
                        else
                            ((ExplorerItem)item.getValue()).setOpacity(Integer.toString(opacity));
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();

                }
            });
            opacity.getItems().add(new MenuItem(null,slider));
            final Menu clear = new Menu("Clear Opacity...");
            this.getItems().add(clear);
            final MenuItem here = new MenuItem("Here");
            clear.getItems().add(here);
            here.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: OpacityTableTreeCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                ((ExplorerItem)((TreeItem)child).getValue()).setOpacity(null);
                        }
                        else
                            ((ExplorerItem)item.getValue()).setOpacity(null);
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
            final MenuItem fromHere = new MenuItem("From Here");
            clear.getItems().add(fromHere);
            fromHere.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: OpacityTableTreeCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setOpacity(null);
                        }
                        else
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setOpacity(null);
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
        }
    }
}
