package io.github.fjossinet.rnartist.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class TextTreeTableCell<T> extends TreeTableCell<T, String> {

    public TextTreeTableCell() {
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        setGraphic(null);
        if (!empty && value == null) {
            Glyph g = new Glyph("FontAwesome", FontAwesome.Glyph.TIMES_CIRCLE);
            g.setFontSize(20);
            Label l = new Label(null, g);
            setGraphic(l);
            setAlignment(Pos.CENTER);
        }
        else if (!empty && !value.isEmpty()) {
           setText(value);
            setAlignment(Pos.CENTER);
        }

    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn() {
        return (TreeTableColumn<T, String> tableColumn) -> new TextTreeTableCell<T>();
    }
}
