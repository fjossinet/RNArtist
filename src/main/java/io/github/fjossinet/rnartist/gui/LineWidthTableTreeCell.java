package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.DrawingConfigurationParameter;
import io.github.fjossinet.rnartist.core.model.RnartistConfig;
import io.github.fjossinet.rnartist.core.model.SecondaryStructureType;
import io.github.fjossinet.rnartist.model.ExplorerItem;
import io.github.fjossinet.rnartist.model.GroupOfStructuralElements;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;

public class LineWidthTableTreeCell<T> extends TreeTableCell<T, String> {

    private Mediator mediator;

    public LineWidthTableTreeCell(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        setGraphic(null);
        if (!empty && value != null && !value.isEmpty()) {
            ExplorerItem item = (ExplorerItem)getTreeTableRow().getItem();
            if (item != null) {
                setText(value);
                setGraphic(LineWidthTableTreeCell.this.getShape(item.getLineWidth()));
                setAlignment(Pos.CENTER);
            }
        }

    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn(Mediator mediator) {
        return (TreeTableColumn<T, String> tableColumn) -> new LineWidthTableTreeCell<>(mediator);
    }

    private Node getShape(String lineWidth) {
        Node node = null;

        switch (lineWidth.toLowerCase()) {
            case "0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(0);
                break;
            case "0.25":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(0.25);
                break;
            case "0.5":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(0.5);
                break;
            case "0.75":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(0.75);
                break;
            case "1.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(1);
                break;
            case "1.25":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(1.25);
                break;
            case "1.5":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(1.5);
                break;
            case "1.75":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(1.75);
                break;
            case "2.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(2);
                break;
            case "2.5":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(2.5);
                break;
            case "3.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(3);
                break;
            case "3.5":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(3.5);
                break;
            case "4.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(4);
                break;
            case "5.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(5);
                break;
            case "6.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(6);
                break;
            case "7.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(7);
                break;
            case "8.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(8);
                break;
            case "9.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(9);
                break;
            case "10.0":
                node = new Line(0, 10, 20, 10);
                ((Line)node).setStrokeWidth(10);
                break;
            default:
                node = null;
        }
        return node;
    }
}
