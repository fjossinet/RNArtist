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

}
