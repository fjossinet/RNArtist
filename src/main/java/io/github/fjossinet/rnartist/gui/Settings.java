package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.io.Backend;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;

public class Settings {

    private Stage stage;
    private Mediator mediator;
    private ComboBox<String> fontNames, tertiaryInteractionStyle;
    private Spinner<Integer> deltaXRes, deltaYRes, deltaFontSize;

    private ObservableList<ThemeFromWebsite> themesList;

    public Settings(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("Settings");
        this.createScene(stage);
        //new Thread(new LoadThemesFromWebsite()).run();
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {

        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //this.createAllThemesPanel(root);
        this.createJunctionsPanel(root);
        this.createGLobalSettingsPanel(root);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        scene.getWindow().setWidth(400);
        scene.getWindow().setHeight(screenSize.getHeight());
        scene.getWindow().setX(0);
        scene.getWindow().setY(0);
    }

    private void createJunctionsPanel(TabPane root) {

        VBox parent = new VBox();

        GridPane targetForm = new GridPane();
        targetForm.setHgap(5);
        targetForm.setVgap(5);
        targetForm.setPadding(new Insets(10, 10, 10, 10));
        targetForm.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        targetForm.getColumnConstraints().addAll(cc);

        parent.getChildren().add(targetForm);

        tertiaryInteractionStyle = new ComboBox<>();
        tertiaryInteractionStyle.getItems().addAll("solid", "dashed");
        tertiaryInteractionStyle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
            }
        });
        tertiaryInteractionStyle.setMaxWidth(Double.MAX_VALUE);

        /*applyTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.TertiaryInteractionStyle, tertiaryInteractionStyle.getValue());
                setMuted(true);
            }
        });*/

        Button clearTertiariesStyle = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));

        clearTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            }
        });

        Button recoverTertiariesStyle = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        recoverTertiariesStyle.setMinWidth(Control.USE_PREF_SIZE);

        recoverTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                }
        });
        recoverTertiariesStyle.setDisable(true);

        Tab theme = new Tab("Junctions Settings", parent);
        root.getTabs().add(theme);
    }

    private void createAllThemesPanel(TabPane root) {
        VBox vbox = new VBox();
        vbox.setFillWidth(true);

        themesList = FXCollections.observableArrayList();

        GridView<ThemeFromWebsite> gridView = new GridView<ThemeFromWebsite>(themesList);
        gridView.setHorizontalCellSpacing(5);
        gridView.setVerticalCellSpacing(5);
        gridView.setCellWidth(400.0);
        gridView.setCellHeight(300.0);
        gridView.setCellFactory(new Callback<GridView<ThemeFromWebsite>, GridCell<ThemeFromWebsite>>() {
            @Override
            public GridCell<ThemeFromWebsite> call(GridView<ThemeFromWebsite> lv) {
                return new ThemeCell();
            }
        });

        GridPane reloadForm = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        reloadForm.getColumnConstraints().addAll(new ColumnConstraints(), cc);
        reloadForm.setHgap(5);
        reloadForm.setVgap(5);
        reloadForm.setPadding(new Insets(10, 10, 10, 10));

        Button reload = new Button("Reload");
        reloadForm.add(reload, 0, 0, 2, 1);
        GridPane.setHalignment(reload, HPos.CENTER);

        reload.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e) {
                new Thread(new LoadThemesFromWebsite()).run();
            }
        });

        vbox.getChildren().add(gridView);
        vbox.getChildren().add(reloadForm);
        VBox.setVgrow(gridView, Priority.ALWAYS);

        Tab themes = new Tab("All Themes", vbox);
        root.getTabs().add(themes);
    }

    private void createGLobalSettingsPanel(TabPane root) {
        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(10, 10, 10, 10));

        Tab settings = new Tab("Global Settings", vbox);
        root.getTabs().add(settings);

        //---- Chimera

        Label title = new Label("UCSF Chimera");
        title.setStyle("-fx-font-size: 20");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane chimeraPane = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        chimeraPane.getColumnConstraints().addAll(cc, new ColumnConstraints());
        chimeraPane.setPadding(new Insets(0, 5, 20, 5));
        chimeraPane.setHgap(5);
        chimeraPane.setVgap(5);

        vbox.getChildren().add(chimeraPane);

        TextField chimeraPath = new TextField();
        chimeraPath.setEditable(false);
        chimeraPath.setText(RnartistConfig.getChimeraPath());
        chimeraPane.getChildren().add(chimeraPath);
        GridPane.setConstraints(chimeraPath, 0, 1);

        Button chimeraSearch = new Button("Browse");
        chimeraPane.getChildren().add(chimeraSearch);
        GridPane.setConstraints(chimeraSearch, 1, 1);
        chimeraSearch.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FileChooser fileChooser = new FileChooser();
                File f = fileChooser.showOpenDialog(null);
                if (f != null) {
                    if (f.getName().endsWith(".app")) //MacOSX
                        chimeraPath.setText(f.getAbsolutePath() + "/Contents/MacOS/chimera");
                    else
                        chimeraPath.setText(f.getAbsolutePath());
                    RnartistConfig.setChimeraPath(chimeraPath.getText());
                }
            }
        });

        //++++++ pane for the fonts
        title = new Label("Font");
        title.setStyle("-fx-font-size: 20");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane fontsPane = new GridPane();
        fontsPane.setPadding(new Insets(0, 5, 20, 5));
        fontsPane.setMaxWidth(Double.MAX_VALUE);
        fontsPane.setHgap(5);
        fontsPane.setVgap(5);
        vbox.getChildren().add(fontsPane);

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        fontsPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), cc, new ColumnConstraints());

        fontNames = new ComboBox<>(
                observableList(Font.getFamilies().stream().distinct().collect(toList())));

        EventHandler eventHandler = (event) -> {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    return null;
                }
            }.execute();
        };

        fontNames.setOnAction(eventHandler);
        fontNames.setValue(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.FontName.toString()));
        fontNames.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(fontNames, 0, 0, 6, 1);
        fontsPane.getChildren().add(fontNames);

        Button applyFontName = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyFontName, 6, 0, 1, 1);
        fontsPane.getChildren().add(applyFontName);

        applyFontName.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            }
        });

        deltaXRes = new Spinner<Integer>();
        deltaXRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.DeltaXRes.toString()))));
        deltaXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            }
        });

        deltaYRes = new Spinner<Integer>();
        deltaYRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.DeltaYRes.toString()))));
        deltaYRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            }
        });

        deltaFontSize = new Spinner<Integer>();
        deltaFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 5, Integer.parseInt(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.DeltaFontSize.toString()))));
        deltaFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            }
        });

        Label l = new Label("x");
        GridPane.setConstraints(l, 0, 1, 1, 1);
        GridPane.setHalignment(l, HPos.LEFT);
        fontsPane.getChildren().add(l);
        GridPane.setConstraints(deltaXRes, 1, 1, 1, 1);
        GridPane.setHalignment(deltaXRes, HPos.LEFT);
        fontsPane.getChildren().add(deltaXRes);

        l = new Label("y");
        GridPane.setConstraints(l, 2, 1, 1, 1);
        GridPane.setHalignment(l, HPos.LEFT);
        fontsPane.getChildren().add(l);
        GridPane.setConstraints(deltaYRes, 3, 1, 1, 1);
        GridPane.setHalignment(deltaYRes, HPos.LEFT);
        fontsPane.getChildren().add(deltaYRes);

        l = new Label("s");
        GridPane.setConstraints(l, 4, 1, 1, 1);
        fontsPane.getChildren().add(l);
        GridPane.setHalignment(l, HPos.LEFT);
        GridPane.setConstraints(deltaFontSize, 5, 1, 1, 1);
        fontsPane.getChildren().add(deltaFontSize);
        GridPane.setHalignment(deltaFontSize, HPos.LEFT);

        Button applyDeltas = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        applyDeltas.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(applyDeltas, 6, 1, 1, 1);
        fontsPane.getChildren().add(applyDeltas);

        applyDeltas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            }
        });

        //---- User ID
        title = new Label("User ID");
        title.setStyle("-fx-font-size: 20");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane userIDPane = new GridPane();
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        userIDPane.getColumnConstraints().addAll(cc, new ColumnConstraints());
        userIDPane.setPadding(new Insets(0, 5, 20, 5));
        userIDPane.setHgap(5);
        userIDPane.setVgap(5);
        vbox.getChildren().add(userIDPane);

        TextField userID = new TextField();
        userID.setEditable(false);
        userID.setPromptText("Click on register to get a User ID");
        if (RnartistConfig.getUserID() != null)
            userID.setText(RnartistConfig.getUserID());
        userIDPane.getChildren().add(userID);
        GridPane.setConstraints(userID, 0, 1);

        Button register = new Button("Register");
        if (RnartistConfig.getUserID() != null)
            register.setDisable(true);
        userIDPane.getChildren().add(register);
        GridPane.setConstraints(register, 1, 1);
        register.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new RegisterDialog(mediator.getRnartist());
                if (RnartistConfig.getUserID() != null) {
                    userID.setText(RnartistConfig.getUserID());
                    register.setDisable(true);
                }
            }
        });

        //---- Bunch of options
        title = new Label("Misc Settings");
        title.setStyle("-fx-font-size: 20");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane optionsPane = new GridPane();
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        optionsPane.getColumnConstraints().addAll(new ColumnConstraints(), cc);
        vbox.getChildren().add(optionsPane);
        optionsPane.setPadding(new Insets(0, 5, 20, 5));
        optionsPane.setHgap(5);
        optionsPane.setVgap(5);

        int row = 0;

        CheckBox svgBrowserFix = new CheckBox();
        svgBrowserFix.setSelected(RnartistConfig.exportSVGWithBrowserCompatibility());
        svgBrowserFix.setOnAction(actionEvent -> {
            RnartistConfig.exportSVGWithBrowserCompatibility(svgBrowserFix.isSelected());
        });
        optionsPane.getChildren().add(svgBrowserFix);
        GridPane.setConstraints(svgBrowserFix, 0, row);

        l = new Label("Set Browser Compatibility for SVG Export");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1, row++);
    }

    class ShapeCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                javafx.scene.shape.Shape shape = this.getShape(item);
                setGraphic(shape);
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
                case "2.5":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(2.5);
                    break;
                case "3.0":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(3);
                    break;
                case "3.5":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(3.5);
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
    }

    public class ShapeCellFactory implements Callback<ListView<String>, ListCell<String>> {
        @Override
        public ListCell<String> call(ListView<String> listview) {
            return new ShapeCell();
        }
    }

    public class ThemeCell extends GridCell<ThemeFromWebsite> {

        private final ImageView themeCapture = new ImageView();
        private final AnchorPane content = new AnchorPane();

        public ThemeCell() {
            content.getChildren().add(themeCapture);
        }

        @Override
        protected void updateItem(ThemeFromWebsite item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(null);
            setText(null);
            setContentDisplay(ContentDisplay.CENTER);
            if (!empty && item != null) {
                themeCapture.setImage(item.getImage());
                setGraphic(content);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }

    }

    public class ThemeFromWebsite {

        private String picture;
        private String name;

        public ThemeFromWebsite(String picture, String name) {
            this.picture = picture;
            this.name = name;
        }

        public Image getImage() {
            return new Image(RnartistConfig.getWebsite() + "/captures/" + this.picture);
        }
    }

    private class LoadThemesFromWebsite extends Task<Exception> {

        @Override
        protected Exception call() throws Exception {
            themesList.clear();
            try {
                for (Map.Entry<String, String> t : Backend.getAllThemes().entrySet()) {
                    themesList.add(new ThemeFromWebsite(t.getKey(), t.getValue()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}


