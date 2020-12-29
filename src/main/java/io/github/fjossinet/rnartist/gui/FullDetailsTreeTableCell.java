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

}
