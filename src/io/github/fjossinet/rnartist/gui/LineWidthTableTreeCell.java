package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.model.ExplorerItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.awt.*;

public class LineWidthTableTreeCell<T> extends TreeTableCell<T, String> {

    public LineWidthTableTreeCell() {
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
            ExplorerItem item = (ExplorerItem)getTreeTableRow().getItem();
            if (item != null) {
                setText(value);
                javafx.scene.shape.Shape shape = this.getShape(value);
                setGraphic(shape);
                setAlignment(Pos.CENTER);
            }
        }

    }

    public javafx.scene.shape.Shape getShape(String shapeType) {
        javafx.scene.shape.Shape shape = null;

        switch (shapeType.toLowerCase()) {
            case "0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(0);
                break;
            case "0.25":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(0.25);
                break;
            case "0.5":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(0.5);
                break;
            case "0.75":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(0.75);
                break;
            case "1.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(1);
                break;
            case "1.25":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(1.25);
                break;
            case "1.5":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(1.5);
                break;
            case "1.75":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(1.75);
                break;
            case "2.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(2);
                break;
            case "3.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(3);
                break;
            case "4.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(4);
                break;
            case "5.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(5);
                break;
            case "6.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(6);
                break;
            case "7.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(7);
                break;
            case "8.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(8);
                break;
            case "9.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(9);
                break;
            case "10.0":
                shape = new Line(0, 10, 20, 10);
                shape.setStrokeWidth(10);
                break;
            default:
                shape = null;
        }
        return shape;
    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn() {
        return (TreeTableColumn<T, String> tableColumn) -> new LineWidthTableTreeCell<>();
    }
}
