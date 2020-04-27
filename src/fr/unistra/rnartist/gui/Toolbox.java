package fr.unistra.rnartist.gui;

import fr.unistra.rnartist.RnartistConfig;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;

public class Toolbox {

    private Stage stage;
    private Mediator mediator;
    private ColorPicker aPicker, uPicker, cPicker, gPicker, xPicker, _2dPicker, _3dPicker;
    private Spinner<Integer> haloWidth, haloOpacity, _3dOpacity;
    private ComboBox<String> fontNames, residueBorder,
            secondaryInteractionWidth,
            tertiaryInteractionWidth, tertiaryInteractionStyle;
    private Spinner<Integer> moduloXRes, moduloYRes;
    private Spinner<Double> moduloSizeRes;

    public Toolbox(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("Toolbox");
        this.createScene(stage);
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {

        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.createThemePanel(root);
        this.createAllThemesPanel(root);
        this.create3DViewerPanel(root);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(300);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(0);
        this.stage.setY(0);
    }

    private void createThemePanel(TabPane root) {
        final HBox hbox = new HBox();
        hbox.setPadding(new javafx.geometry.Insets(10, 12, 15, 12));
        hbox.setSpacing(10);

        fontNames = new ComboBox<>(
                observableList(Font.getFamilies().stream().distinct().collect(toList())));

        EventHandler<ActionEvent> eventHandler = (event) -> {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        mediator.getCanvas2D().repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        };

        fontNames.setOnAction(eventHandler);
        fontNames.setValue(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.FontName.toString()));

        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(5);
        int row = 0, column = 0;
        Label b = new Label("Font");
        b.setStyle("-fx-font-size: 20");
        Separator separator = new Separator(Orientation.HORIZONTAL);
        grid.add(new VBox(b, separator), column, row, 6, 1);
        column = 0;
        grid.add(fontNames, column, ++row,6,1);

        moduloXRes = new Spinner<Integer>();
        moduloXRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 10, Integer.parseInt(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.ModuloXRes.toString()))));
        moduloXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        moduloYRes = new Spinner<Integer>();
        moduloYRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 10, Integer.parseInt(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.ModuloYRes.toString()))));
        moduloYRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        moduloSizeRes = new Spinner<Double>();
        moduloSizeRes.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 1.5, Float.parseFloat(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.ModuloSizeRes.toString())), 0.1));
        moduloSizeRes.valueProperty().addListener(new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        column = 0;
        grid.add(new Label("x"), column, ++row, 1, 1);
        grid.add(moduloXRes, ++column, row, 1, 1);
        grid.add(new Label("y"), ++column, row, 1, 1);
        grid.add(moduloYRes, ++column, row, 1, 1);
        grid.add(new Label("s"), ++column, row, 1, 1);
        grid.add(moduloSizeRes, ++column, row, 1, 1);

        column = 0;

        b = new Label("Colors");
        b.setStyle("-fx-font-size: 20");
        separator = new Separator(Orientation.HORIZONTAL);
        grid.add(new VBox(b, separator), column, ++row, 6, 1);

        column = 0;

        java.util.List<String> colorSchemes = new ArrayList<String>();
        colorSchemes.add("Candies");
        colorSchemes.add("Grapes");
        colorSchemes.add("Metal");

        ComboBox<String> colorSchemeChoices = new ComboBox<String>(
                observableList(colorSchemes));

        eventHandler = (event) -> {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    Map<String, String> colors = RnartistConfig.defaultColorSchemes.get(colorSchemeChoices.getValue());
                    try {
                        loadTheme(colors);
                        mediator.getCanvas2D().repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        };

        colorSchemeChoices.setOnAction(eventHandler);
        colorSchemeChoices.setValue("Candies");
        grid.add(new Label("Schemes"), column, ++row,2,1);
        ++column;
        ++column;
        grid.add(colorSchemeChoices, column, row,4,1);

        column = 0;
        aPicker = new ColorPicker();
        aPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }
        });
        aPicker.setStyle("-fx-color-label-visible: false ;");
        aPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.AColor.toString())));
        grid.add(new Label("A"), column, ++row, 1, 1);
        grid.add(aPicker, ++column , row, 1, 1);

        uPicker = new ColorPicker();
        uPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }
        });
        uPicker.setStyle("-fx-color-label-visible: false ;");
        uPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.UColor.toString())));
        grid.add(new Label("U"),++column , row, 1, 1);
        grid.add(uPicker, ++column, row, 2, 1);

        xPicker = new ColorPicker();
        xPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        xPicker.setStyle("-fx-color-label-visible: false ;");
        xPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.XColor.toString())));
        grid.add(new Label("X"), ++column, row, 1, 1);
        grid.add(xPicker, ++column, row, 2, 1);

        column = 0;
        gPicker = new ColorPicker();
        gPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        gPicker.setStyle("-fx-color-label-visible: false ;");
        gPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.GColor.toString())));
        grid.add(new Label("G"), column, ++row, 1, 1);
        grid.add(gPicker, ++column, row, 2, 1);

        cPicker = new ColorPicker();
        cPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        cPicker.setStyle("-fx-color-label-visible: false ;");
        cPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.CColor.toString())));
        grid.add(new Label("C"), ++column, row, 1, 1);
        grid.add(cPicker, ++column, row, 2, 1);

        column = 0;
        _2dPicker = new ColorPicker();
        _2dPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        _2dPicker.setStyle("-fx-color-label-visible: false ;");
        _2dPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.SecondaryColor.toString())));
        grid.add(new Label("Secondaries"), column, ++row, 2,1);
        ++column;
        ++column;
        grid.add(_2dPicker, ++column, row, 2, 1);

        column = 0;
        _3dPicker = new ColorPicker();
        _3dPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        _3dPicker.setStyle("-fx-color-label-visible: false ;");
        _3dPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.TertiaryColor.toString())));
        grid.add(new Label("Tertiaries"), column, ++row, 2,1);
        ++column;
        ++column;
        grid.add(_3dPicker, ++column, row, 2, 1);

        column = 0;
        b = new Label("Lines");
        b.setStyle("-fx-font-size: 20");
        separator = new Separator(Orientation.HORIZONTAL);
        grid.add(new VBox(b, separator), column, ++row, 6, 1);
        column = 0;
        grid.add(new Label("Residues"), column, ++row, 2,1);
        residueBorder = new ComboBox<>();
        residueBorder.getItems().addAll("0", "1", "2", "3", "4");
        residueBorder.setValue(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.ResidueBorder.toString()));
        residueBorder.setCellFactory(new ShapeCellFactory());
        residueBorder.setButtonCell(new ShapeCell());
        ++column;
        grid.add(residueBorder, ++column, row,2,1);
        residueBorder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });
        column = 0;
        grid.add(new Label("Secondaries"), column, ++row, 2,1);

        secondaryInteractionWidth = new ComboBox<>();
        secondaryInteractionWidth.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9");
        secondaryInteractionWidth.setValue(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.SecondaryInteractionWidth.toString()));
        secondaryInteractionWidth.setCellFactory(new ShapeCellFactory());
        secondaryInteractionWidth.setButtonCell(new ShapeCell());
        ++column;
        grid.add(secondaryInteractionWidth, ++column, row,2,1);
        secondaryInteractionWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });

        column = 0;
        grid.add(new Label("Tertiaries"), column, ++row, 2,1);
        column = 0;
        tertiaryInteractionWidth = new ComboBox<>();
        tertiaryInteractionWidth.getItems().addAll("0","1", "2", "3", "4", "5", "6", "7");
        tertiaryInteractionWidth.setValue(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.TertiaryInteractionWidth.toString()));
        tertiaryInteractionWidth.setCellFactory(new ShapeCellFactory());
        tertiaryInteractionWidth.setButtonCell(new ShapeCell());
        grid.add(tertiaryInteractionWidth, column, ++row,2,1);
        tertiaryInteractionWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });

        tertiaryInteractionStyle = new ComboBox<>();
        tertiaryInteractionStyle.getItems().addAll("Solid", "Dashed");
        tertiaryInteractionStyle.setValue(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.TertiaryInteractionStyle.toString()));
        tertiaryInteractionStyle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });
        ++column;
        grid.add(tertiaryInteractionStyle, ++column, row,3,1);
        ++column;
        ++column;
        _3dOpacity = new Spinner<Integer>();
        _3dOpacity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, Integer.parseInt(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.TertiaryOpacity.toString()))));
        _3dOpacity.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        grid.add(_3dOpacity, ++column, row, 1, 1);

        column = 0;
        b = new Label("Misc");
        b.setStyle("-fx-font-size: 20");
        separator = new Separator(Orientation.HORIZONTAL);
        grid.add(new VBox(b, separator), column, ++row, 6, 1);
        column = 0;
        grid.add(new Label("Tertiaries Halo"), column, ++row, 2,1);
        ++column;
        grid.add(new Label("s"), ++column, row, 1, 1);
        haloWidth = new Spinner<Integer>();
        haloWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, Integer.parseInt(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.HaloWidth.toString()))));
        haloWidth.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        grid.add(haloWidth, ++column, row, 1, 1);
        grid.add(new Label("o"), ++column, row, 1, 1);
        haloOpacity = new Spinner<Integer>();
        haloOpacity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, Integer.parseInt(RnartistConfig.defaultTheme.get(RnartistConfig.ThemeParameter.HaloOpacity.toString()))));
        haloOpacity.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        grid.add(haloOpacity, ++column, row, 1, 1);
        hbox.getChildren().add(grid);

        ScrollPane sp = new ScrollPane(hbox);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        GridPane saveForm = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        saveForm.getColumnConstraints().addAll(new ColumnConstraints(),cc);
        saveForm.setHgap(5);
        saveForm.setVgap(10);
        saveForm.setPadding(new Insets(10, 10, 10, 10));

        Label title = new Label("Name");
        saveForm.add(title, 0, 0);

        TextField nameField = new TextField();
        saveForm.add(nameField, 1, 0);
        nameField.setPromptText("Choose a Theme Name");

        Button save = new Button("Save My Current Theme");
        saveForm.add(save, 0, 1,2,1);
        GridPane.setHalignment(save, HPos.CENTER);

        save.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e)  {
                NitriteId id = mediator.getEmbeddedDB().addTheme(nameField.getText().trim(), "Fabrice Jossinet",  mediator.getToolbox().getTheme());
                //themesList.add(0,new Theme(id, nameField.getText().trim(), "Fabrice Jossinet"));
            }
        });

        VBox vbox =  new VBox();
        vbox.setFillWidth(true);
        vbox.getChildren().add(saveForm);
        vbox.getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        Tab theme = new Tab("Current Theme", vbox);
        root.getTabs().add(theme);
    }

    private void createAllThemesPanel(TabPane root) {
        VBox vbox =  new VBox();
        vbox.setFillWidth(true);

        ObservableList<Theme> themesList = FXCollections.observableArrayList();

        for (Document theme: this.mediator.getEmbeddedDB().getThemes().find()) {
            themesList.add(new Theme(theme.getId(), (String)theme.get("name"), (String)theme.get("author")));
        }

        ListView<Theme> listView = new ListView<Theme>(themesList);
        listView.setCellFactory(new Callback<ListView<Theme>, ListCell<Theme>>() {
            @Override
            public ListCell<Theme> call(ListView<Theme> lv) {
                return new ThemeCell();
            }
        });
        listView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> change) {

            }
        });

        GridPane reloadForm = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        reloadForm.getColumnConstraints().addAll(new ColumnConstraints(),cc);
        reloadForm.setHgap(5);
        reloadForm.setVgap(10);
        reloadForm.setPadding(new Insets(10, 10, 10, 10));

        /*Label title = new Label("Name");
        reloadForm.add(title, 0, 0);

        TextField nameField = new TextField();
        reloadForm.add(nameField, 1, 0);
        nameField.setPromptText("Choose a Theme Name");*/

        Button reload = new Button("Reload");
        reloadForm.add(reload, 0, 0,2,1);
        GridPane.setHalignment(reload, HPos.CENTER);

        reload.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e)  {

            }
        });

        vbox.getChildren().add(reloadForm);
        vbox.getChildren().add(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);

        Tab themes = new Tab("All Themes", vbox);
        root.getTabs().add(themes);
    }

    private void create3DViewerPanel(TabPane root) {
        Tab _3dViewer = new Tab("3D Viewer", new VBox());
        root.getTabs().add(_3dViewer);
    }

    public void loadTheme(Map<String,String> theme) {
        aPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(RnartistConfig.ThemeParameter.AColor.toString(), aPicker.getValue().toString())));
        uPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(RnartistConfig.ThemeParameter.UColor.toString(), uPicker.getValue().toString())));
        gPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(RnartistConfig.ThemeParameter.GColor.toString(), gPicker.getValue().toString())));
        cPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(RnartistConfig.ThemeParameter.CColor.toString(), cPicker.getValue().toString())));
        xPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(RnartistConfig.ThemeParameter.XColor.toString(), xPicker.getValue().toString())));
        _2dPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(RnartistConfig.ThemeParameter.SecondaryColor.toString(), _2dPicker.getValue().toString())));
        _3dPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(RnartistConfig.ThemeParameter.TertiaryColor.toString(), _3dPicker.getValue().toString())));
        _3dOpacity.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(RnartistConfig.ThemeParameter.TertiaryOpacity.toString(), _3dOpacity.getValue().toString())));
        haloOpacity.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(RnartistConfig.ThemeParameter.HaloOpacity.toString(), haloOpacity.getValue().toString())));
        haloWidth.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(RnartistConfig.ThemeParameter.HaloWidth.toString(), haloWidth.getValue().toString())));
        secondaryInteractionWidth.setValue(theme.getOrDefault(RnartistConfig.ThemeParameter.SecondaryInteractionWidth.toString(), ""+getSecondaryInteractionWidth()));
        tertiaryInteractionWidth.setValue(theme.getOrDefault(RnartistConfig.ThemeParameter.TertiaryInteractionWidth.toString(),""+getTertiaryInteractionWidth()));
        residueBorder.setValue(theme.getOrDefault(RnartistConfig.ThemeParameter.ResidueBorder.toString(),""+getResidueBorder()));
        tertiaryInteractionStyle.setValue(theme.getOrDefault(RnartistConfig.ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue()));
        fontNames.setValue(theme.getOrDefault(RnartistConfig.ThemeParameter.FontName.toString(), getFontName()));
        moduloXRes.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(RnartistConfig.ThemeParameter.ModuloXRes.toString(), ""+getModuloXRes())));
        moduloYRes.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(RnartistConfig.ThemeParameter.ModuloYRes.toString(), ""+getModuloYRes())));
        moduloSizeRes.getValueFactory().setValue(Double.parseDouble(theme.getOrDefault(RnartistConfig.ThemeParameter.ModuloSizeRes.toString(), ""+getModuloSizeRes())));
    }

    public Map<String,String> getTheme() {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put(RnartistConfig.ThemeParameter.AColor.toString(), aPicker.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.UColor.toString(), uPicker.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.GColor.toString(), gPicker.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.CColor.toString(), cPicker.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.XColor.toString(), xPicker.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.SecondaryColor.toString(), _2dPicker.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.TertiaryColor.toString(), _3dPicker.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.TertiaryOpacity.toString(), _3dOpacity.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.HaloOpacity.toString(), haloOpacity.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.HaloWidth.toString(), haloWidth.getValue().toString());
        configuration.put(RnartistConfig.ThemeParameter.SecondaryInteractionWidth.toString(), ""+getSecondaryInteractionWidth());
        configuration.put(RnartistConfig.ThemeParameter.TertiaryInteractionWidth.toString(), ""+getTertiaryInteractionWidth());
        configuration.put(RnartistConfig.ThemeParameter.ResidueBorder.toString(), ""+getResidueBorder());
        configuration.put(RnartistConfig.ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue());
        configuration.put(RnartistConfig.ThemeParameter.FontName.toString(), getFontName());
        configuration.put(RnartistConfig.ThemeParameter.ModuloXRes.toString(), ""+getModuloXRes());
        configuration.put(RnartistConfig.ThemeParameter.ModuloYRes.toString(), ""+getModuloYRes());
        configuration.put(RnartistConfig.ThemeParameter.ModuloSizeRes.toString(), ""+getModuloSizeRes());
        return configuration;
    }

    class ShapeCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            }
            else {
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
                case "1":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(1);
                    break;
                case "2":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(2);
                    break;
                case "3":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(3);
                    break;
                case "4":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(4);
                    break;
                case "5":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(5);
                    break;
                case "6":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(6);
                    break;
                case "7":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(7);
                    break;
                case "8":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(8);
                    break;
                case "9":
                    shape = new Line(0, 10, 20, 10);
                    shape.setStrokeWidth(9);
                    break;
                case "10":
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

    public Spinner<Integer> getHaloWidth() {
        return haloWidth;
    }

    public Spinner<Integer> getHaloOpacity() {
        return haloOpacity;
    }

    public Spinner<Integer> get_3dOpacity() {
        return _3dOpacity;
    }

    public Color getAColor() {
        return aPicker.getValue();
    }

    public Color getUColor() {
        return uPicker.getValue();
    }

    public Color getCColor() {
        return cPicker.getValue();
    }

    public Color getGColor() {
        return gPicker.getValue();
    }

    public Color getXColor() {
        return xPicker.getValue();
    }

    public Color get2dInteractionColor() {
        return _2dPicker.getValue();
    }

    public Color get3dInteractionColor() {
        return _3dPicker.getValue();
    }

    public int getResidueBorder() {
        return Integer.parseInt(residueBorder.getValue());
    }

    public int getSecondaryInteractionWidth() {
        return Integer.parseInt(secondaryInteractionWidth.getValue());
    }

    public int getTertiaryInteractionWidth() {
        return Integer.parseInt(tertiaryInteractionWidth.getValue());
    }

    public int getModuloXRes() {
        return moduloXRes.getValue();
    }

    public int getModuloYRes() {
        return moduloYRes.getValue();
    }

    public float getModuloSizeRes() {
        return moduloSizeRes.getValue().floatValue();
    }

    public byte getTertiaryInteractionStyle() {
        return tertiaryInteractionStyle.getValue().equals("Dashed") ? RnartistConfig.DASHED : RnartistConfig.SOLID;
    }

    public String getFontName() {
        return fontNames.getValue();
    }

    public class ThemeCell extends ListCell<Theme> {

        private final GridPane gridPane = new GridPane();
        private final Label name = new Label();
        private final Label author = new Label();
        private final AnchorPane content = new AnchorPane();
        private final Button share;

        public ThemeCell() {
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");
            GridPane.setConstraints(name, 1, 0);
            GridPane.setConstraints(author, 1, 1);
            HBox buttons = new HBox();
            buttons.setAlignment(Pos.CENTER_RIGHT);
            buttons.setSpacing(5);
            buttons.setPadding(new Insets(5,5,5,5));
            buttons.getChildren().add(new Button("",new Glyph("FontAwesome", FontAwesome.Glyph.EYE)));
            this.share = new Button("",new Glyph("FontAwesome", FontAwesome.Glyph.GLOBE));
            this.share.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    mediator.getToolbox().getTheme();
                }
            });
            buttons.getChildren().add(share);
            buttons.getChildren().add(new Button("",new Glyph("FontAwesome", FontAwesome.Glyph.SAVE)));
            GridPane.setConstraints(buttons, 0, 2,2,1);
            gridPane.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true));
            gridPane.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, VPos.CENTER, true));
            gridPane.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, VPos.CENTER, true));
            gridPane.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.CENTER, true));
            gridPane.setHgap(6);
            gridPane.setVgap(6);
            gridPane.getChildren().setAll(name, author, buttons);
            AnchorPane.setTopAnchor(gridPane, 0d);
            AnchorPane.setLeftAnchor(gridPane, 0d);
            AnchorPane.setBottomAnchor(gridPane, 0d);
            AnchorPane.setRightAnchor(gridPane, 0d);
            content.getChildren().add(gridPane);
        }

        @Override
        protected void updateItem(Theme item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(null);
            setText(null);
            setContentDisplay(ContentDisplay.LEFT);
            if (!empty && item != null) {
                name.setText(item.name);
                author.setText(item.author);
                setGraphic(content);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }


    }

    class Theme {

        private NitriteId id;
        private String name;
        private String author;

        public Theme(NitriteId id, String name, String author) {
            this.id = id;
            this.name = name;
            this.author = author;
        }
    }

}
