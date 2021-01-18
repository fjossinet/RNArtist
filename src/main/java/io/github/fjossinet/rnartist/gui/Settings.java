package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.io.Backend;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.io.File;
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
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {

        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.createGlobalSettingsPanel(root);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        scene.getWindow().setWidth(screenSize.getWidth()/2);
        scene.getWindow().setHeight(screenSize.getHeight()/2);
        scene.getWindow().setX(screenSize.getWidth()/2-screenSize.getWidth()/4);
        scene.getWindow().setY(screenSize.getHeight()/2-screenSize.getHeight()/4);
    }

    private void createGlobalSettingsPanel(TabPane root) {
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

        for (int i = 0; i < 6; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            if (i == 3)
                constraints.setHgrow(Priority.ALWAYS);
            chimeraPane.getColumnConstraints().add(constraints);
        }
        chimeraPane.setPadding(new Insets(10, 5, 15, 5));
        chimeraPane.setHgap(5);
        chimeraPane.setVgap(5);

        vbox.getChildren().add(chimeraPane);

        TextField chimeraPath = new TextField();
        chimeraPath.setEditable(false);
        chimeraPath.setText(RnartistConfig.getChimeraPath());
        chimeraPane.getChildren().add(chimeraPath);
        GridPane.setConstraints(chimeraPath, 0, 0, 4, 1);

        Button chimeraSearch = new Button("Browse");
        chimeraPane.getChildren().add(chimeraSearch);
        GridPane.setConstraints(chimeraSearch, 4, 0);
        chimeraSearch.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FileChooser fileChooser = new FileChooser();
                File f = fileChooser.showOpenDialog(mediator.getRnartist().getStage());
                if (f != null) {
                    if (f.getName().endsWith(".app")) //MacOSX
                        chimeraPath.setText(f.getAbsolutePath() + "/Contents/MacOS/chimera");
                    else
                        chimeraPath.setText(f.getAbsolutePath());
                    RnartistConfig.setChimeraPath(chimeraPath.getText());
                }
            }
        });

        Button chimeraRun = new Button("Run");
        chimeraPane.getChildren().add(chimeraRun);
        GridPane.setConstraints(chimeraRun, 5, 0);
        chimeraRun.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getChimeraDriver().connectToExecutable();
            }
        });

        Label hostLabel = new Label("Host");
        chimeraPane.getChildren().add(hostLabel);
        GridPane.setConstraints(hostLabel, 0, 1);

        TextField hostValue = new TextField(RnartistConfig.getChimeraHost());
        chimeraPane.getChildren().add(hostValue);
        GridPane.setConstraints(hostValue, 1, 1);

        Label portLabel = new Label("Port");
        chimeraPane.getChildren().add(portLabel);
        GridPane.setConstraints(portLabel, 2, 1);

        TextField portValue = new TextField(""+RnartistConfig.getChimeraPort());
        chimeraPane.getChildren().add(portValue);
        GridPane.setConstraints(portValue, 3, 1);

        Button connect2ChimeraRest = new Button("Connect");
        connect2ChimeraRest.setMaxWidth(Double.MAX_VALUE);
        chimeraPane.getChildren().add(connect2ChimeraRest);
        GridPane.setConstraints(connect2ChimeraRest, 4, 1, 2,1);
        connect2ChimeraRest.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    RnartistConfig.setChimeraHost(hostValue.getText().trim());
                    RnartistConfig.setChimeraPort(Integer.parseInt(portValue.getText().trim()));
                    mediator.getChimeraDriver().connectToRestServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //---- RNA GAllery
        title = new Label("RNA Gallery");
        title.setStyle("-fx-font-size: 20");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane rnaGalleryPane = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        rnaGalleryPane.getColumnConstraints().addAll(cc, new ColumnConstraints());
        rnaGalleryPane.setPadding(new Insets(10, 5, 15, 5));
        rnaGalleryPane.setHgap(5);
        rnaGalleryPane.setVgap(5);

        vbox.getChildren().add(rnaGalleryPane);

        TextField galleryPath = new TextField();
        galleryPath.setEditable(false);
        galleryPath.setText(RnartistConfig.getRnaGalleryPath());
        rnaGalleryPane.getChildren().add(galleryPath);
        GridPane.setConstraints(galleryPath, 0, 1);

        Button gallerySearch = new Button("Browse");
        rnaGalleryPane.getChildren().add(gallerySearch);
        GridPane.setConstraints(gallerySearch, 1, 1);
        gallerySearch.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File f = directoryChooser.showDialog(mediator.getRnartist().getStage());
                if (f != null) {
                    if (f.isDirectory() && new File(f,"PDB").exists()) {
                        galleryPath.setText(f.getAbsolutePath());
                        RnartistConfig.setRnaGalleryPath(galleryPath.getText());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Problem with your RNA Gallery");
                        alert.setHeaderText("The directory selected doesn't look like an RNA Gallery");
                        HBox box = new HBox();
                        box.setAlignment(Pos.CENTER);
                        box.setSpacing(10);
                        box.getChildren().add(new Label("You need to download and select a copy of the RNA Gallery project."));
                        Button button = new Button(null, new FontIcon("fas-download:15"));
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                mediator.getRnartist().getHostServices().showDocument("https://github.com/fjossinet/RNAGallery");
                            }
                        });
                        box.getChildren().add(button);
                        alert.getDialogPane().setContent(box);
                        alert.showAndWait();
                    }
                }
            }
        });

        CheckBox useOnlineGallery = new CheckBox("Use online gallery");
        useOnlineGallery.setSelected(RnartistConfig.getUseOnlineRNAGallery());
        galleryPath.setDisable(RnartistConfig.getUseOnlineRNAGallery());
        gallerySearch.setDisable(RnartistConfig.getUseOnlineRNAGallery());
        useOnlineGallery.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                RnartistConfig.setUseOnlineRNAGallery(useOnlineGallery.isSelected());
                galleryPath.setDisable(RnartistConfig.getUseOnlineRNAGallery());
                gallerySearch.setDisable(RnartistConfig.getUseOnlineRNAGallery());
            }
        });
        rnaGalleryPane.getChildren().add(useOnlineGallery);
        GridPane.setConstraints(useOnlineGallery, 0, 2, 2, 1);

        //++++++ pane for the fonts
        title = new Label("Font");
        title.setStyle("-fx-font-size: 20");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane fontsPane = new GridPane();
        fontsPane.setPadding(new Insets(10, 5, 15, 5));
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
        deltaXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            }
        });

        deltaYRes = new Spinner<Integer>();
        deltaYRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            }
        });

        deltaFontSize = new Spinner<Integer>();
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

        //---- Bunch of options
        title = new Label("Misc Settings");
        title.setStyle("-fx-font-size: 20");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane optionsPane = new GridPane();
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        optionsPane.getColumnConstraints().addAll(new ColumnConstraints(), cc);
        vbox.getChildren().add(optionsPane);
        optionsPane.setPadding(new Insets(10, 5, 15, 5));
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


