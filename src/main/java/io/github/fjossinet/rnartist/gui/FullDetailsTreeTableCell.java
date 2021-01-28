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
import org.kordamp.ikonli.javafx.FontIcon;

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
            FontIcon icon = null;
            if (value == "true")
                icon =  new FontIcon("fas-eye:15");
            else if (value == "false")
                icon =  new FontIcon("fas-eye-slash:15");
            if (icon != null) {
                Label l = new Label(null, icon);
                setGraphic(l);
                setAlignment(Pos.CENTER);
            }
        }
    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn(Mediator mediator) {
        return (TreeTableColumn<T, String> tableColumn) -> new FullDetailsTreeTableCell<>(mediator);
    }

}
