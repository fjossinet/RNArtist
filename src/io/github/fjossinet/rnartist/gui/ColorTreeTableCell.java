package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.model.ExplorerItem;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public final class ColorTreeTableCell<T> extends TreeTableCell<T, String> {

    public ColorTreeTableCell() {
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
            javafx.scene.paint.Color color = javafx.scene.paint.Color.web(value);
            final int red = (int) (color.getRed() * 255);
            final int green = (int) (color.getGreen() * 255);
            final int blue = (int) (color.getBlue() * 255);
            String style = String.format("-fx-background-color: rgb(%d, %d, %d) ; -fx-border-color: black", red, green, blue);
            Region graphic = new Region();
            graphic.setStyle(style);
            setGraphic(graphic);
            setAlignment(Pos.CENTER);
        }

    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn() {
        return (TreeTableColumn<T, String> tableColumn) -> new ColorTreeTableCell<>();
    }
}
