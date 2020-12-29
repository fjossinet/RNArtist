package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.model.ExplorerItem;
import io.github.fjossinet.rnartist.model.GroupOfStructuralElements;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;

public final class ColorTreeTableCell<T> extends TreeTableCell<T, String> {

    private Mediator mediator;
    private final Region graphic;

    public ColorTreeTableCell(Mediator mediator) {
        this.mediator = mediator;
        this.graphic = new Region();
        this.graphic.setMaxHeight(15);
    }

    @Override
    protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        setGraphic(null);
        if (!empty && value != null && !value.isEmpty()) {
            setText(null);
            String style = null;
            if (value != null && !empty) {
                style = String.format("-fx-background-color: %s ; -fx-border-width: 1; -fx-border-color: dimgrey;", value);
                graphic.setStyle(style);
                setGraphic(graphic);
                setAlignment(Pos.CENTER);
            }
        }

    }

    public static <T> Callback<TreeTableColumn<T, String>, TreeTableCell<T, String>> forTreeTableColumn(Mediator mediator) {
        return (TreeTableColumn<T, String> tableColumn) -> new ColorTreeTableCell<>(mediator);
    }
}
