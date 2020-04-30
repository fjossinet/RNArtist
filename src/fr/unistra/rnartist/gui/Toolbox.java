package fr.unistra.rnartist.gui;

import com.google.gson.internal.StringMap;
import fr.unistra.rnartist.RnartistConfig;
import fr.unistra.rnartist.io.Backend;
import fr.unistra.rnartist.model.ThemeConfigurator;
import fr.unistra.rnartist.model.ThemeParameter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static fr.unistra.rnartist.model.DrawingsKt.*;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;

public class Toolbox implements ThemeConfigurator {

    private Stage stage;
    private Mediator mediator;
    private ColorPicker aPicker, uPicker, cPicker, gPicker, xPicker, _2dPicker, _3dPicker;
    private Spinner<Integer> haloWidth, _3dOpacity;
    private ComboBox<String> fontNames, residueBorder,
            secondaryInteractionWidth,
            tertiaryInteractionWidth, tertiaryInteractionStyle;
    private Spinner<Integer> moduloXRes, moduloYRes;
    private Spinner<Double> moduloSizeRes;
    private ObservableList<Theme> themesList;

    public Toolbox(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("Toolbox");
        this.createScene(stage);
        new Thread(new LoadThemesFromWebsite()).run();
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {

        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //this.createAllThemesPanel(root);
        this.createThemePanel(root);
        this.createSettingsPanel(root);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(440);
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
        fontNames.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.FontName.toString()));

        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(5);
        int row = 0, column = 0;
        Label b = new Label("Font");
        b.setStyle("-fx-font-size: 15");
        Separator separator = new Separator(Orientation.HORIZONTAL);
        grid.add(new VBox(b, separator), column, row, 6, 1);
        column = 0;
        grid.add(fontNames, column, ++row,6,1);

        moduloXRes = new Spinner<Integer>();
        moduloXRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 10, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.ModuloXRes.toString()))));
        moduloXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        moduloYRes = new Spinner<Integer>();
        moduloYRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 10, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.ModuloYRes.toString()))));
        moduloYRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        moduloSizeRes = new Spinner<Double>();
        moduloSizeRes.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 1.5, Float.parseFloat(RnartistConfig.defaultTheme.get(ThemeParameter.ModuloSizeRes.toString())), 0.1));
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
        b.setStyle("-fx-font-size: 15");
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
                    Map<String, String> colors = defaultColorSchemes.get(colorSchemeChoices.getValue());
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
        aPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(ThemeParameter.AColor.toString())));
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
        uPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(ThemeParameter.UColor.toString())));
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
        xPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(ThemeParameter.XColor.toString())));
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
        gPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(ThemeParameter.GColor.toString())));
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
        cPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(ThemeParameter.CColor.toString())));
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
        _2dPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(ThemeParameter.SecondaryColor.toString())));
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
        _3dPicker.setValue(Color.web(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryColor.toString())));
        grid.add(new Label("Tertiaries"), column, ++row, 2,1);
        ++column;
        ++column;
        grid.add(_3dPicker, ++column, row, 2, 1);

        column = 0;
        b = new Label("Lines");
        b.setStyle("-fx-font-size: 15");
        separator = new Separator(Orientation.HORIZONTAL);
        grid.add(new VBox(b, separator), column, ++row, 6, 1);
        column = 0;
        grid.add(new Label("Residues"), column, ++row, 2,1);
        residueBorder = new ComboBox<>();
        residueBorder.getItems().addAll("0", "1", "2", "3", "4");
        residueBorder.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.ResidueBorder.toString()));
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
        secondaryInteractionWidth.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.SecondaryInteractionWidth.toString()));
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
        tertiaryInteractionWidth.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryInteractionWidth.toString()));
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
        tertiaryInteractionStyle.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryInteractionStyle.toString()));
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
        _3dOpacity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryOpacity.toString()))));
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
        b.setStyle("-fx-font-size: 15");
        separator = new Separator(Orientation.HORIZONTAL);
        grid.add(new VBox(b, separator), column, ++row, 6, 1);
        column = 0;
        grid.add(new Label("Tertiaries Halo"), column, ++row, 2,1);
        ++column;
        grid.add(new Label("s"), ++column, row, 1, 1);
        haloWidth = new Spinner<Integer>();
        haloWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.HaloWidth.toString()))));
        haloWidth.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        grid.add(haloWidth, ++column, row, 1, 1);
        hbox.getChildren().add(grid);

        ScrollPane sp = new ScrollPane(hbox);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        GridPane saveForm = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        saveForm.getColumnConstraints().addAll(new ColumnConstraints(),cc,new ColumnConstraints(),new ColumnConstraints());
        saveForm.setHgap(5);
        saveForm.setVgap(10);
        saveForm.setPadding(new Insets(10, 10, 10, 10));

        Label title = new Label("Name");
        saveForm.add(title, 0, 0);

        TextField nameField = new TextField();
        saveForm.add(nameField, 1, 0);
        nameField.setPromptText("Choose a Theme Name");

        Button shareOnline = new Button("Share my Theme");

        shareOnline.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Backend.submitTheme(mediator, nameField.getText().trim(), mediator.getToolbox().getTheme());
            }
        });
        saveForm.add(shareOnline, 2, 0);

        Button themesWebpage = new Button("User Community Themes");
        themesWebpage.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                mediator.getWebBrowser().getStage().show();
                mediator.getWebBrowser().getStage().toFront();
                mediator.getWebBrowser().showTab(0);
                mediator.getWebBrowser().rnartistEngine.load(RnartistConfig.getWebsite()+"/themes");
            }
        });
        saveForm.add(themesWebpage, 0, 1,3,1);
        GridPane.setHalignment(themesWebpage,HPos.CENTER);

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

        themesList = FXCollections.observableArrayList();

        GridView<Theme> gridView = new GridView<Theme>(themesList);
        gridView.setHorizontalCellSpacing(5);
        gridView.setVerticalCellSpacing(5);
        gridView.setCellWidth(400.0);
        gridView.setCellHeight(300.0);
        gridView.setCellFactory(new Callback<GridView<Theme>, GridCell<Theme>>() {
            @Override
            public GridCell<Theme> call(GridView<Theme> lv) {
                return new ThemeCell();
            }
        });

        GridPane reloadForm = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        reloadForm.getColumnConstraints().addAll(new ColumnConstraints(),cc);
        reloadForm.setHgap(5);
        reloadForm.setVgap(5);
        reloadForm.setPadding(new Insets(10, 10, 10, 10));

        Button reload = new Button("Reload");
        reloadForm.add(reload, 0, 0,2,1);
        GridPane.setHalignment(reload, HPos.CENTER);

        reload.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e)  {
                new Thread(new LoadThemesFromWebsite()).run();
            }
        });

        vbox.getChildren().add(gridView);
        vbox.getChildren().add(reloadForm);
        VBox.setVgrow(gridView, Priority.ALWAYS);

        Tab themes = new Tab("All Themes", vbox);
        root.getTabs().add(themes);
    }

    private void createSettingsPanel(TabPane root) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setSpacing(10);
        GridPane chimeraPane = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        chimeraPane.getColumnConstraints().addAll(cc, new ColumnConstraints());
        vbox.getChildren().add(chimeraPane);
        chimeraPane.setPadding(new Insets(10, 10, 10, 10));
        chimeraPane.setHgap(10);
        chimeraPane.setVgap(10);
        Tab settings = new Tab("Settings", vbox);
        root.getTabs().add(settings);
        Label chimeraLabel = new Label("UCSF Chimera Path");
        chimeraLabel.setStyle("-fx-font-size: 15");
        chimeraPane.getChildren().add(chimeraLabel);
        GridPane.setConstraints(chimeraLabel, 0,0);
        TextField chimeraPath = new TextField();
        chimeraPath.setEditable(false);
        chimeraPath.setText(RnartistConfig.getChimeraPath());
        chimeraPane.getChildren().add(chimeraPath);
        GridPane.setConstraints(chimeraPath, 0,1);
        Button chimeraSearch = new Button("Browse");
        chimeraPane.getChildren().add(chimeraSearch);
        GridPane.setConstraints(chimeraSearch, 1,1);
        chimeraSearch.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FileChooser fileChooser = new FileChooser();
                File f = fileChooser.showOpenDialog(null );
                if (f!= null) {
                    if (f.getName().endsWith(".app")) //MacOSX
                        chimeraPath.setText(f.getAbsolutePath()+"/Contents/MacOS/chimera");
                    else
                        chimeraPath.setText(f.getAbsolutePath());
                    RnartistConfig.setChimeraPath(chimeraPath.getText());
                    try {
                        RnartistConfig.saveConfig(mediator);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        vbox.getChildren().add(new Separator(Orientation.HORIZONTAL));

    }

    public void loadTheme(Map<String,String> theme) {
        aPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.AColor.toString(), aPicker.getValue().toString())));
        uPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.UColor.toString(), uPicker.getValue().toString())));
        gPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.GColor.toString(), gPicker.getValue().toString())));
        cPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.CColor.toString(), cPicker.getValue().toString())));
        xPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.XColor.toString(), xPicker.getValue().toString())));
        _2dPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.SecondaryColor.toString(), _2dPicker.getValue().toString())));
        _3dPicker.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.TertiaryColor.toString(), _3dPicker.getValue().toString())));
        _3dOpacity.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.TertiaryOpacity.toString(), _3dOpacity.getValue().toString())));
        haloWidth.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.HaloWidth.toString(), haloWidth.getValue().toString())));
        secondaryInteractionWidth.setValue(theme.getOrDefault(ThemeParameter.SecondaryInteractionWidth.toString(), ""+getSecondaryInteractionWidth()));
        tertiaryInteractionWidth.setValue(theme.getOrDefault(ThemeParameter.TertiaryInteractionWidth.toString(),""+getTertiaryInteractionWidth()));
        residueBorder.setValue(theme.getOrDefault(ThemeParameter.ResidueBorder.toString(),""+getResidueBorder()));
        tertiaryInteractionStyle.setValue(theme.getOrDefault(ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue()));
        fontNames.setValue(theme.getOrDefault(ThemeParameter.FontName.toString(), getFontName()));
        moduloXRes.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.ModuloXRes.toString(), ""+getModuloXRes())));
        moduloYRes.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.ModuloYRes.toString(), ""+getModuloYRes())));
        moduloSizeRes.getValueFactory().setValue(Double.parseDouble(theme.getOrDefault(ThemeParameter.ModuloSizeRes.toString(), ""+getModuloSizeRes())));
    }

    public Map<String,String> getTheme() {
        Map<String, String> theme = new HashMap<String, String>();
        theme.put(ThemeParameter.AColor.toString(), aPicker.getValue().toString());
        theme.put(ThemeParameter.UColor.toString(), uPicker.getValue().toString());
        theme.put(ThemeParameter.GColor.toString(), gPicker.getValue().toString());
        theme.put(ThemeParameter.CColor.toString(), cPicker.getValue().toString());
        theme.put(ThemeParameter.XColor.toString(), xPicker.getValue().toString());
        theme.put(ThemeParameter.SecondaryColor.toString(), _2dPicker.getValue().toString());
        theme.put(ThemeParameter.TertiaryColor.toString(), _3dPicker.getValue().toString());
        theme.put(ThemeParameter.TertiaryOpacity.toString(), _3dOpacity.getValue().toString());
        theme.put(ThemeParameter.HaloWidth.toString(), haloWidth.getValue().toString());
        theme.put(ThemeParameter.SecondaryInteractionWidth.toString(), ""+getSecondaryInteractionWidth());
        theme.put(ThemeParameter.TertiaryInteractionWidth.toString(), ""+getTertiaryInteractionWidth());
        theme.put(ThemeParameter.ResidueBorder.toString(), ""+getResidueBorder());
        theme.put(ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue());
        theme.put(ThemeParameter.FontName.toString(), getFontName());
        theme.put(ThemeParameter.ModuloXRes.toString(), ""+getModuloXRes());
        theme.put(ThemeParameter.ModuloYRes.toString(), ""+getModuloYRes());
        theme.put(ThemeParameter.ModuloSizeRes.toString(), ""+getModuloSizeRes());
        return theme;
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

    @Override
    public int getHaloWidth() {
        return haloWidth.getValue();
    }

    @Override
    public int getTertiaryOpacity() {
        return _3dOpacity.getValue();
    }

    @Override
    public String getAColor() {
        return ""+javaFXToAwt(aPicker.getValue()).getRGB();
    }

    @Override
    public String getUColor() {
        return ""+javaFXToAwt(uPicker.getValue()).getRGB();
    }

    @Override
    public String getCColor() {
        return ""+javaFXToAwt(cPicker.getValue()).getRGB();
    }

    @Override
    public String getGColor() {
        return ""+javaFXToAwt(gPicker.getValue()).getRGB();
    }

    @Override
    public String getXColor() {
        return ""+javaFXToAwt(xPicker.getValue()).getRGB();
    }

    @Override
    public String getSecondaryInteractionColor() {
        return ""+javaFXToAwt(_2dPicker.getValue()).getRGB();
    }

    @Override
    public String getTertiaryInteractionColor() {
        return ""+javaFXToAwt(_3dPicker.getValue()).getRGB();
    }

    @Override
    public int getResidueBorder() {
        return Integer.parseInt(residueBorder.getValue());
    }

    @Override
    public int getSecondaryInteractionWidth() {
        return Integer.parseInt(secondaryInteractionWidth.getValue());
    }

    @Override
    public int getTertiaryInteractionWidth() {
        return Integer.parseInt(tertiaryInteractionWidth.getValue());
    }

    @Override
    public int getModuloXRes() {
        return moduloXRes.getValue();
    }

    @Override
    public int getModuloYRes() {
        return moduloYRes.getValue();
    }

    @Override
    public float getModuloSizeRes() {
        return moduloSizeRes.getValue().floatValue();
    }

    @Override
    public byte getTertiaryInteractionStyle() {
        return tertiaryInteractionStyle.getValue().equals("Dashed") ? DASHED : SOLID;
    }

    @Override
    public String getFontName() {
        return fontNames.getValue();
    }

    public class ThemeCell extends GridCell<Theme> {

        private final ImageView themeCapture = new ImageView();
        private final AnchorPane content = new AnchorPane();

        public ThemeCell() {
            content.getChildren().add(themeCapture);
        }

        @Override
        protected void updateItem(Theme item, boolean empty) {
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

    public class Theme {

        private String picture;
        private String name;

        public Theme(String picture, String name) {
            this.picture = picture;
            this.name = name;
        }

        public Image getImage() {
            return new Image(RnartistConfig.getWebsite()+"/captures/" +this.picture);
        }
    }

    private class LoadThemesFromWebsite extends Task<Exception> {

        @Override
        protected Exception call() throws Exception {
            themesList.clear();
            try {
                for (StringMap<String> t:Backend.getAllThemes()) {
                    themesList.add(new Theme(t.get("picture"), t.get("name")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private java.awt.Color javaFXToAwt(javafx.scene.paint.Color c) {
        return new java.awt.Color((float)c.getRed(),
                (float)c.getGreen(),
                (float)c.getBlue(),
                (float)c.getOpacity());
    }

}


