package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
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

public class LineShiftTableTreeCell<T> extends TreeTableCell<T, String> {

    private Slider slider = new Slider();
    private Mediator mediator;

    public LineShiftTableTreeCell(Mediator mediator) {
        slider.setMin(0);
        slider.setMax(255);
        this.setContextMenu(new CMenu());
        this.mediator = mediator;
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        setGraphic(null);
        if (!empty && value != null && !value.isEmpty()) {
            setText(value);
            setAlignment(Pos.CENTER);
        }
    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn(Mediator mediator) {
        return (TreeTableColumn<T, String> tableColumn) -> new LineShiftTableTreeCell<>(mediator);
    }

    private class CMenu extends ContextMenu {
        CMenu() {
            final Menu shift = new Menu("Set Shift");
            shift.getStyleClass().add("no-highlight");
            this.getItems().add(shift);
            Slider slider = new Slider(0, 10,0);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit(5);
            slider.setMinorTickCount(1);
            slider.setShowTickMarks(true);
            slider.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    for (TreeItem item: LineShiftTableTreeCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                ((ExplorerItem)((TreeItem)child).getValue()).setLineShift(Integer.toString((int)slider.getValue()));
                        }
                        else
                            ((ExplorerItem)item.getValue()).setLineShift(Integer.toString((int)slider.getValue()));
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();

                }
            });
            shift.getItems().add(new MenuItem(null,slider));
            final Menu clear = new Menu("Clear Shift...");
            this.getItems().add(clear);
            final MenuItem here = new MenuItem("Here");
            clear.getItems().add(here);
            here.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    for (TreeItem item: LineShiftTableTreeCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                ((ExplorerItem)((TreeItem)child).getValue()).setLineShift(null);
                        }
                        else
                            ((ExplorerItem)item.getValue()).setLineShift(null);
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
                    for (TreeItem item: LineShiftTableTreeCell.this.getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (GroupOfStructuralElements.class.isInstance(item.getValue())) {
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setLineShift(null);
                        }
                        else
                            for (Object child:item.getChildren())
                                for (Object i: mediator.getExplorer().getAllTreeViewItemsFrom((TreeItem)child))
                                    ((TreeItem<ExplorerItem>)i).getValue().setLineShift(null);
                    }
                    mediator.getExplorer().refresh();
                    mediator.getCanvas2D().repaint();
                }
            });
        }
    }
}
