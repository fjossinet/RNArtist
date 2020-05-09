package fr.unistra.rnartist.gui;

import com.google.gson.internal.StringMap;
import fr.unistra.rnartist.io.Backend;
import fr.unistra.rnartist.model.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static fr.unistra.rnartist.model.DrawingsKt.*;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;

public class Toolbox implements ThemeConfigurator {

    private Stage stage;
    private Mediator mediator;
    private ColorPicker colorPicker1, colorPicker2, colorPicker3, colorPicker4, colorPicker5, colorPicker6, colorPicker7;
    private ChoiceBox<String> structuralElement1,structuralElement2,structuralElement3,structuralElement4,structuralElement5,structuralElement6,structuralElement7,
                                letterColor1,letterColor2,letterColor3,letterColor4,letterColor5,letterColor6,letterColor7;
    private Slider _3dOpacity, haloWidth;
    private ComboBox<String> fontNames, residueBorder,
            secondaryInteractionWidth,
            tertiaryInteractionWidth, tertiaryInteractionStyle;
    private Spinner<Integer> deltaXRes, deltaYRes, deltaFontSize;
    private ObservableList<Theme> themesList;
    private ObservableList<Residue> residues;

    public Toolbox(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("Toolbox");
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
        this.createLayoutPanel(root);
        this.createAnnotationsPanel(root);
        this.createThemePanel(root);
        this.createSettingsPanel(root);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(360);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(0);
        this.stage.setY(0);
    }

    private void createThemePanel(TabPane root) {

        VBox vbox =  new VBox();
        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        vbox.setSpacing(10);

        //++++++ pane for the fonts
        Label title = new Label("Font");
        title.setStyle("-fx-font-size: 15");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane fontsPane = new GridPane();
        fontsPane.setHgap(5);
        fontsPane.setVgap(10);
        fontsPane.setPadding(new Insets(0, 0, 20, 0));

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        fontsPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(),new ColumnConstraints(),new ColumnConstraints(),new ColumnConstraints(),cc);

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
        fontNames.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(fontNames, 0,0,6,1);
        fontsPane.getChildren().add(fontNames);

        deltaXRes = new Spinner<Integer>();
        deltaXRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.DeltaXRes.toString()))));
        deltaXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        deltaYRes = new Spinner<Integer>();
        deltaYRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.DeltaYRes.toString()))));
        deltaYRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        deltaFontSize = new Spinner<Integer>();
        deltaFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 5, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.DeltaFontSize.toString()))));
        deltaFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        Label l = new Label("x");
        GridPane.setConstraints(l, 0,1,1,1);
        fontsPane.getChildren().add(l);
        GridPane.setConstraints(deltaXRes, 1,1,1,1);
        fontsPane.getChildren().add(deltaXRes);

        l = new Label("y");
        GridPane.setConstraints(l, 2,1,1,1);
        fontsPane.getChildren().add(l);
        GridPane.setConstraints(deltaYRes, 3,1,1,1);
        fontsPane.getChildren().add(deltaYRes);

        l = new Label("s");
        GridPane.setConstraints(l, 4,1,1,1);
        fontsPane.getChildren().add(l);
        GridPane.setConstraints(deltaFontSize, 5,1,1,1);
        fontsPane.getChildren().add(deltaFontSize);

        vbox.getChildren().add(fontsPane);

        //++++++ pane for the Colors
        title = new Label("Colors");
        title.setStyle("-fx-font-size: 15");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane colorsPane = new GridPane();
        colorsPane.setHgap(10);
        colorsPane.setVgap(10);
        colorsPane.setPadding(new Insets(0, 0, 20, 0));
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        colorsPane.getColumnConstraints().addAll(new ColumnConstraints(),cc,cc);

        java.util.List<String> colorSchemes = new ArrayList<String>();
        for (String colorSchemeName:RnartistConfig.defaultColorSchemes.keySet())
            colorSchemes.add(colorSchemeName);

        ComboBox<String> colorSchemeChoices = new ComboBox<String>(
                observableList(colorSchemes));

        eventHandler = (event) -> {
                    Map<String, String> colors = RnartistConfig.defaultColorSchemes.get(colorSchemeChoices.getValue());
                    try {
                        loadTheme(colors);
                        mediator.getCanvas2D().repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
        };

        colorSchemeChoices.setOnAction(eventHandler);
        colorSchemeChoices.setValue("Choose a Scheme");
        colorSchemeChoices.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(colorSchemeChoices, 1,0,2,1);
        colorsPane.getChildren().add(colorSchemeChoices);

        colorPicker1 = new ColorPicker();
        colorPicker1.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }
        });
        colorPicker1.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(ThemeParameter.AColor.toString()))));
        colorPicker1.setMaxWidth(Double.MAX_VALUE);

        colorPicker2 = new ColorPicker();
        colorPicker2.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }
        });
        colorPicker2.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(ThemeParameter.UColor.toString()))));
        colorPicker2.setMaxWidth(Double.MAX_VALUE);

        colorPicker3 = new ColorPicker();
        colorPicker3.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        colorPicker3.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(ThemeParameter.GColor.toString()))));
        colorPicker3.setMaxWidth(Double.MAX_VALUE);

        colorPicker4 = new ColorPicker();
        colorPicker4.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        colorPicker4.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(ThemeParameter.CColor.toString()))));
        colorPicker4.setMaxWidth(Double.MAX_VALUE);

        colorPicker5 = new ColorPicker();
        colorPicker5.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        colorPicker5.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(ThemeParameter.XColor.toString()))));
        colorPicker5.setMaxWidth(Double.MAX_VALUE);

        colorPicker6 = new ColorPicker();
        colorPicker6.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }
        });
        colorPicker6.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(ThemeParameter.SecondaryColor.toString()))));
        colorPicker6.setMaxWidth(Double.MAX_VALUE);

        colorPicker7 = new ColorPicker();
        colorPicker7.setOnAction(new EventHandler() {
            @Override
            public void handle(javafx.event.Event event) {
                mediator.getCanvas2D().repaint();
            }

        });
        colorPicker7.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryColor.toString()))));
        colorPicker7.setMaxWidth(Double.MAX_VALUE);

        l = new Label("Background");
        GridPane.setConstraints(l, 1,1,1,1);
        GridPane.setHalignment(l,HPos.CENTER);
        colorsPane.getChildren().add(l);
        l = new Label("Character");
        GridPane.setConstraints(l, 2,1,1,1);
        GridPane.setHalignment(l,HPos.CENTER);
        colorsPane.getChildren().add(l);

        this.structuralElement1 = new ChoiceBox();
        structuralElement1.getItems().addAll("A","U","G","C","X","2D","3D");
        structuralElement1.setValue("A");
        GridPane.setConstraints(structuralElement1, 0,2,1,1);
        colorsPane.getChildren().add(structuralElement1);
        GridPane.setHalignment(structuralElement1,HPos.CENTER);
        GridPane.setConstraints(colorPicker1, 1,2,1,1);
        colorsPane.getChildren().add(colorPicker1);
        GridPane.setHalignment(colorPicker1,HPos.CENTER);
        this.letterColor1 = new ChoiceBox<String>();
        letterColor1.setMaxWidth(Double.MAX_VALUE);
        letterColor1.getItems().addAll("White", "Black");
        letterColor1.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(letterColor1, 2,2,1,1);
        colorsPane.getChildren().add(letterColor1);
        GridPane.setHalignment(letterColor1,HPos.CENTER);
        this.structuralElement1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement1.getValue()) {
                    case "A": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor1.setDisable(true); break;
                    case "3D": letterColor1.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

        this.structuralElement2 = new ChoiceBox();
        structuralElement2.getItems().addAll("A","U","G","C","X","2D","3D");
        structuralElement2.setValue("U");
        GridPane.setConstraints(structuralElement2, 0,3,1,1);
        colorsPane.getChildren().add(structuralElement2);
        GridPane.setHalignment(structuralElement2,HPos.CENTER);
        GridPane.setConstraints(colorPicker2, 1,3,1,1);
        colorsPane.getChildren().add(colorPicker2);
        GridPane.setHalignment(colorPicker2,HPos.CENTER);
        this.letterColor2 = new ChoiceBox<String>();
        letterColor2.setMaxWidth(Double.MAX_VALUE);
        letterColor2.getItems().addAll("White", "Black");
        letterColor2.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(letterColor2, 2,3,1,1);
        colorsPane.getChildren().add(letterColor2);
        GridPane.setHalignment(letterColor2,HPos.CENTER);
        this.structuralElement2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement2.getValue()) {
                    case "A": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor2.setDisable(true); break;
                    case "3D": letterColor2.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

        this.structuralElement3 = new ChoiceBox();
        structuralElement3.getItems().addAll("A","U","G","C","X","2D","3D");
        structuralElement3.setValue("G");
        GridPane.setConstraints(structuralElement3, 0,4,1,1);
        colorsPane.getChildren().add(structuralElement3);
        GridPane.setHalignment(structuralElement3,HPos.CENTER);
        GridPane.setConstraints(colorPicker3, 1,4,1,1);
        colorsPane.getChildren().add(colorPicker3);
        GridPane.setHalignment(colorPicker3,HPos.CENTER);
        this.letterColor3 = new ChoiceBox<String>();
        letterColor3.setMaxWidth(Double.MAX_VALUE);
        letterColor3.getItems().addAll("White", "Black");
        letterColor3.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(letterColor3, 2,4,1,1);
        colorsPane.getChildren().add(letterColor3);
        GridPane.setHalignment(letterColor3,HPos.CENTER);
        this.structuralElement3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement3.getValue()) {
                    case "A": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor3.setDisable(true); break;
                    case "3D": letterColor3.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

        this.structuralElement4 = new ChoiceBox();
        structuralElement4.getItems().addAll("A","U","G","C","X","2D","3D");
        structuralElement4.setValue("C");
        GridPane.setConstraints(structuralElement4, 0,5,1,1);
        colorsPane.getChildren().add(structuralElement4);
        GridPane.setHalignment(structuralElement4,HPos.CENTER);
        GridPane.setConstraints(colorPicker4, 1,5,1,1);
        colorsPane.getChildren().add(colorPicker4);
        GridPane.setHalignment(colorPicker4,HPos.CENTER);
        this.letterColor4 = new ChoiceBox<String>();
        letterColor4.setMaxWidth(Double.MAX_VALUE);
        letterColor4.getItems().addAll("White", "Black");
        letterColor4.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(letterColor4, 2,5,1,1);
        colorsPane.getChildren().add(letterColor4);
        GridPane.setHalignment(letterColor4,HPos.CENTER);
        this.structuralElement4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement4.getValue()) {
                    case "A": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor4.setDisable(true); break;
                    case "3D": letterColor4.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

        this.structuralElement5 = new ChoiceBox();
        structuralElement5.getItems().addAll("A","U","G","C","X","2D","3D");
        structuralElement5.setValue("X");
        GridPane.setConstraints(structuralElement5, 0,6,1,1);
        colorsPane.getChildren().add(structuralElement5);
        GridPane.setHalignment(structuralElement5,HPos.CENTER);
        GridPane.setConstraints(colorPicker5, 1,6,1,1);
        colorsPane.getChildren().add(colorPicker5);
        GridPane.setHalignment(colorPicker5,HPos.CENTER);
        this.letterColor5 = new ChoiceBox<String>();
        letterColor5.setMaxWidth(Double.MAX_VALUE);
        letterColor5.getItems().addAll("White", "Black");
        letterColor5.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(letterColor5, 2,6,1,1);
        colorsPane.getChildren().add(letterColor5);
        GridPane.setHalignment(letterColor5,HPos.CENTER);

        this.structuralElement5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement5.getValue()) {
                    case "A": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor5.setDisable(true); break;
                    case "3D": letterColor5.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

        this.structuralElement6 = new ChoiceBox();
        structuralElement6.getItems().addAll("A","U","G","C","X","2D","3D");
        structuralElement6.setValue("2D");
        GridPane.setConstraints(structuralElement6, 0,7,1,1);
        colorsPane.getChildren().add(structuralElement6);
        GridPane.setHalignment(structuralElement6,HPos.CENTER);
        GridPane.setConstraints(colorPicker6, 1,7,1,1);
        colorsPane.getChildren().add(colorPicker6);
        GridPane.setHalignment(colorPicker6,HPos.CENTER);
        this.letterColor6  = new <String>ChoiceBox();
        letterColor6.setMaxWidth(Double.MAX_VALUE);
        letterColor6.getItems().addAll("White", "Black");
        letterColor6.setValue("White");
        letterColor6.setDisable(true);
        letterColor6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(letterColor6, 2,7,1,1);
        colorsPane.getChildren().add(letterColor6);
        GridPane.setHalignment(letterColor6,HPos.CENTER);

        this.structuralElement6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement6.getValue()) {
                    case "A": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor6.setDisable(true); break;
                    case "3D": letterColor6.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

        this.structuralElement7 = new ChoiceBox<String>();
        structuralElement7.getItems().addAll("A","U","G","C","X","2D","3D");
        structuralElement7.setValue("3D");
        GridPane.setConstraints(structuralElement7, 0,8,1,1);
        colorsPane.getChildren().add(structuralElement7);
        GridPane.setHalignment(structuralElement7,HPos.CENTER);
        GridPane.setConstraints(colorPicker7, 1,8,1,1);
        colorsPane.getChildren().add(colorPicker7);
        GridPane.setHalignment(colorPicker7,HPos.CENTER);
        this.letterColor7 = new ChoiceBox<String>();
        letterColor7.setMaxWidth(Double.MAX_VALUE);
        letterColor7.getItems().addAll("White", "Black");
        letterColor7.setValue("White");
        letterColor7.setDisable(true);
        letterColor7.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(letterColor7, 2,8,1,1);
        colorsPane.getChildren().add(letterColor7);
        GridPane.setHalignment(letterColor7,HPos.CENTER);

        this.structuralElement7.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement7.getValue()) {
                    case "A": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor7.setDisable(true); break;
                    case "3D": letterColor7.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

        vbox.getChildren().add(colorsPane);

        //++++++ pane for the Lines
        title = new Label("Lines");
        title.setStyle("-fx-font-size: 15");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane linesPane = new GridPane();
        linesPane.setHgap(10);
        linesPane.setVgap(10);
        linesPane.setPadding(new Insets(0, 0, 20, 0));

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        linesPane.getColumnConstraints().addAll(new ColumnConstraints(150),cc);

        l = new Label("Residue Borders (px)");
        GridPane.setConstraints(l, 0,0,1,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        residueBorder = new ComboBox<>();
        residueBorder.getItems().addAll("0", "1", "2", "3", "4");
        residueBorder.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.ResidueBorder.toString()));
        residueBorder.setCellFactory(new ShapeCellFactory());
        residueBorder.setButtonCell(new ShapeCell());
        residueBorder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });
        residueBorder.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(residueBorder, 1,0,1,1);
        linesPane.getChildren().add(residueBorder);
        GridPane.setHalignment(residueBorder,HPos.CENTER);

        l = new Label("2D Width (px)");
        GridPane.setConstraints(l, 0,1,1,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        secondaryInteractionWidth = new ComboBox<>();
        secondaryInteractionWidth.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9");
        secondaryInteractionWidth.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.SecondaryInteractionWidth.toString()));
        secondaryInteractionWidth.setCellFactory(new ShapeCellFactory());
        secondaryInteractionWidth.setButtonCell(new ShapeCell());
        secondaryInteractionWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });
        secondaryInteractionWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(secondaryInteractionWidth, 1,1,1,1);
        linesPane.getChildren().add(secondaryInteractionWidth);
        GridPane.setHalignment(secondaryInteractionWidth,HPos.CENTER);

        l = new Label("3D Width (px)");
        GridPane.setConstraints(l, 0,2,1,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        tertiaryInteractionWidth = new ComboBox<>();
        tertiaryInteractionWidth.getItems().addAll("0","1", "2", "3", "4", "5", "6", "7");
        tertiaryInteractionWidth.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryInteractionWidth.toString()));
        tertiaryInteractionWidth.setCellFactory(new ShapeCellFactory());
        tertiaryInteractionWidth.setButtonCell(new ShapeCell());
        tertiaryInteractionWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });
        tertiaryInteractionWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(tertiaryInteractionWidth, 1,2,1,1);
        linesPane.getChildren().add(tertiaryInteractionWidth);
        GridPane.setHalignment(tertiaryInteractionWidth,HPos.CENTER);

        l = new Label("3D Style");
        GridPane.setConstraints(l, 0,3,1,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        tertiaryInteractionStyle = new ComboBox<>();
        tertiaryInteractionStyle.getItems().addAll("solid", "dashed");
        tertiaryInteractionStyle.setValue(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryInteractionStyle.toString()));
        tertiaryInteractionStyle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                mediator.getCanvas2D().repaint();
            }
        });
        tertiaryInteractionStyle.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(tertiaryInteractionStyle, 1,3,1,1);
        linesPane.getChildren().add(tertiaryInteractionStyle);
        GridPane.setHalignment(tertiaryInteractionStyle,HPos.CENTER);

        l = new Label("3D Opacity (%)");
        GridPane.setConstraints(l, 0,4,1,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        _3dOpacity = new Slider(0, 100, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.TertiaryOpacity.toString()))/255.0*100.0);
        _3dOpacity.setShowTickLabels(true);
        _3dOpacity.setShowTickMarks(true);
        _3dOpacity.setMajorTickUnit(50);
        _3dOpacity.setMinorTickCount(5);
        _3dOpacity.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        _3dOpacity.setShowTickMarks(true);
        _3dOpacity.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(_3dOpacity, 1,4,1,1);
        linesPane.getChildren().add(_3dOpacity);
        GridPane.setHalignment(_3dOpacity,HPos.CENTER);

        l = new Label("Halo Width (px)");
        GridPane.setConstraints(l, 0,5,1,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        haloWidth = new Slider(0, 20, Integer.parseInt(RnartistConfig.defaultTheme.get(ThemeParameter.HaloWidth.toString())));;
        haloWidth.setShowTickLabels(true);
        haloWidth.setShowTickMarks(true);
        haloWidth.setMajorTickUnit(5);
        haloWidth.setMinorTickCount(1);
        haloWidth.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        haloWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(haloWidth, 1,5,1,1);
        linesPane.getChildren().add(haloWidth);
        GridPane.setHalignment(haloWidth,HPos.CENTER);

        vbox.getChildren().add(linesPane);

        //++++++ form to save the theme
        FlowPane saveForm = new FlowPane();
        saveForm.setAlignment(Pos.CENTER);
        saveForm.setHgap(10);
        saveForm.setPadding(new Insets(10, 10, 10, 10));

        TextField themeNameField = new TextField();
        themeNameField.setPromptText("Choose a Theme Name");

        Button shareOnline = new Button("Share");

        shareOnline.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Backend.submitTheme(mediator, themeNameField.getText().trim(), mediator.getToolbox().getTheme());
            }
        });

        Button saveTheme = new Button("Set as Default");

        saveTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                try {
                    RnartistConfig.saveConfig(mediator.getToolbox().getTheme());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Theme Saved!");
                    alert.setHeaderText("Your current theme is now the default one!");
                    alert.setContentText(null);
                    alert.showAndWait();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        Button restoreDefault = new Button("Restore Default");

        restoreDefault.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                loadTheme(RnartistConfig.defaultTheme);
                mediator.getCanvas2D().repaint();
            }
        });
        saveForm.getChildren().add(restoreDefault);
        saveForm.getChildren().add(saveTheme);
        saveForm.getChildren().add(shareOnline);

        Button themesWebpage = new Button("See User Community Themes");
        themesWebpage.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                mediator.getWebBrowser().getStage().show();
                mediator.getWebBrowser().getStage().toFront();
                mediator.getWebBrowser().showTab(0);
                mediator.getWebBrowser().rnartistEngine.load(RnartistConfig.getWebsite()+"/themes");
            }
        });
        //saveForm.add(themesWebpage, 0, 1,6,1);
        GridPane.setHalignment(themesWebpage,HPos.CENTER);

        ScrollPane sp = new ScrollPane(vbox);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        VBox parent = new VBox();
        parent.getChildren().add(sp);
        VBox.setVgrow(sp,Priority.ALWAYS);
        parent.getChildren().add(saveForm);
        Tab theme = new Tab("Theme", parent);
        root.getTabs().add(theme);
    }

    private FlowPane junctionKnobs = new FlowPane();

    private void createLayoutPanel(TabPane root) {
        junctionKnobs.setPadding(new Insets(5, 5, 5, 5));
        junctionKnobs.setVgap(5);
        junctionKnobs.setHgap(5);
        ScrollPane scrollPane = new ScrollPane(junctionKnobs);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Tab themes = new Tab("Layout", scrollPane);
        root.getTabs().add(themes);
    }

    public void addJunctionKnob(JunctionCircle jc) {
        VBox vbox = new VBox();
        vbox.setMaxWidth(160.0);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        vbox.setSpacing(5);
        JunctionKnob knob = new JunctionKnob(jc, this.mediator);
        vbox.getChildren().add(knob);
        TextField name = new TextField(jc.getJunction().getName());
        vbox.getChildren().add(name);
        name.setAlignment(Pos.CENTER);
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            knob.getJunctionCircle().getJunction().setName(newValue);
        });
        TextField location = new TextField(jc.getJunction().getLocation().getDescription());
        location.setEditable(false);
        vbox.getChildren().add(location);
        location.setAlignment(Pos.CENTER);
        var typeName = jc.getJunction().getType().name();
        if (typeName.endsWith("Way")) {
            typeName = typeName.split("Way")[0]+" Way Junction";
        } else if (typeName.endsWith("Loop")) {
            typeName = typeName.split("Loop")[0]+" Loop";
        }
        Text type = new Text(typeName);
        type.setFont(Font.font(null, FontWeight.BOLD, 15));
        vbox.getChildren().add(type);
        junctionKnobs.getChildren().add(vbox);
    }

    public FlowPane getJunctionKnobs() {
        return junctionKnobs;
    }

    public void unselectJunctionKnobs() {
        for (Node node:junctionKnobs.getChildren()) {
            ((JunctionKnob)((VBox)node).getChildren().get(0)).unselect();
        }
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

        //---- Chimera
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
        GridPane.setConstraints(chimeraLabel, 0,0,2,1);

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
                        RnartistConfig.saveConfig(null); //we save the chimera path, not the theme (perhaps the user is not interested to save the current theme)
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //---- User ID
        GridPane userIDPane = new GridPane();
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        userIDPane.getColumnConstraints().addAll(cc, new ColumnConstraints());
        vbox.getChildren().add(userIDPane);
        userIDPane.setPadding(new Insets(10, 10, 10, 10));
        userIDPane.setHgap(10);
        userIDPane.setVgap(10);

        Label userIDLabel = new Label("Your User ID");
        userIDLabel.setStyle("-fx-font-size: 15");
        userIDPane.getChildren().add(userIDLabel);
        GridPane.setConstraints(userIDLabel, 0,0,2,1);

        TextField userID = new TextField();
        userID.setEditable(false);
        userID.setPromptText("Click on register to get a User ID");
        if (RnartistConfig.getUserID() != null)
            userID.setText(RnartistConfig.getUserID());
        userIDPane.getChildren().add(userID);
        GridPane.setConstraints(userID, 0,1);

        Button register = new Button("Register");
        if (RnartistConfig.getUserID() != null)
            register.setDisable(true);
        userIDPane.getChildren().add(register);
        GridPane.setConstraints(register, 1, 1);
        register.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new RegisterDialog(mediator.getRnartist());
                if (RnartistConfig.getUserID()!= null) {
                    userID.setText(RnartistConfig.getUserID());
                    register.setDisable(true);
                }
            }
        });

        //---- Bunch of options
        GridPane optionsPane = new GridPane();
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        optionsPane.getColumnConstraints().addAll(new ColumnConstraints(),cc);
        vbox.getChildren().add(optionsPane);
        optionsPane.setPadding(new Insets(10, 10, 10, 10));
        optionsPane.setHgap(10);
        optionsPane.setVgap(10);

        Label svgExport = new Label("Misc Options");
        svgExport.setStyle("-fx-font-size: 15");
        optionsPane.getChildren().add(svgExport);
        GridPane.setConstraints(svgExport, 0,0,2,1);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        optionsPane.getChildren().add(separator);
        GridPane.setConstraints(separator, 0, 1, 2, 1);

        CheckBox svgBrowserFix = new CheckBox();
        svgBrowserFix.setSelected(RnartistConfig.exportSVGWithBrowserCompatibility());
        svgBrowserFix.setOnAction(actionEvent -> {
            RnartistConfig.exportSVGWithBrowserCompatibility(svgBrowserFix.isSelected());
        });
        optionsPane.getChildren().add(svgBrowserFix);
        GridPane.setConstraints(svgBrowserFix, 0,2);

        Label l = new Label("Browser Compatibility for SVG");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1,2);

        CheckBox showToolBar = new CheckBox();
        showToolBar.setSelected(true);
        showToolBar.setOnAction(actionEvent -> {
            mediator.getRnartist().showTopToolBars(showToolBar.isSelected());
        });
        optionsPane.getChildren().add(showToolBar);
        GridPane.setConstraints(showToolBar, 0,3);

        l = new Label("Show ToolBar");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1,3);

        /*CheckBox showStatusBar = new CheckBox();
        showStatusBar.setSelected(true);
        showStatusBar.setOnAction(actionEvent -> {
            mediator.getRnartist().showStatusBar(showStatusBar.isSelected());
        });
        optionsPane.getChildren().add(showStatusBar);
        GridPane.setConstraints(showStatusBar, 0,4);

        l = new Label("Show Status Bar");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1,4);*/
    }

    public ObservableList<Residue> getResidues() {
        return residues;
    }

    private void createAnnotationsPanel(TabPane root) {
        TableView<Residue> tableView = new TableView<Residue>();
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mediator.getCanvas2D().getSecondaryStructureDrawing().get().getWorkingSession().getSelectedResidues().clear();
                for (Residue r:tableView.getSelectionModel().getSelectedItems())
                    mediator.getCanvas2D().getSecondaryStructureDrawing().get().getWorkingSession().getSelectedResidues().addAll(mediator.canvas2D.getSecondaryStructureDrawing().get().getResiduesFromAbsPositions(List.of(r.getPosition())));
                mediator.getCanvas2D().getSecondaryStructureDrawing().get().getWorkingSession().centerFrameOnSelection(mediator.getCanvas2D().getBounds());
            } else {
                mediator.getCanvas2D().getSecondaryStructureDrawing().get().getWorkingSession().getSelectedResidues().clear();
            }
            mediator.getCanvas2D().repaint();
        });

        TableColumn<Residue, Integer> positionCol= new TableColumn<Residue, Integer>("Position");
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));

        TableColumn<Residue, Character> nameCol= new TableColumn<Residue, Character>("Residue");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Residue, String> labelCol= new TableColumn<Residue, String>("Label");
        labelCol.setCellValueFactory(new PropertyValueFactory<>("label"));

        TableColumn<Residue, String> modifiedCol= new TableColumn<Residue, String>("Modified As");
        //modifiedCol.setCellValueFactory(new PropertyValueFactory<>("modified"));

        tableView.getColumns().addAll(positionCol,nameCol,labelCol, modifiedCol);
        residues = FXCollections.observableArrayList();
        tableView.setItems(residues);
        Tab annotations = new Tab("Annotations",  tableView);
        root.getTabs().add(annotations);
    }

    public void loadTheme(Map<String,String> theme) {
        structuralElement1.setValue("A");
        colorPicker1.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.AColor.toString(), colorPicker1.getValue().toString())));
        letterColor1.setValue(theme.getOrDefault(ThemeParameter.AChar.toString(), letterColor1.getValue().toString()).toLowerCase().equals("#000000") ? "Black" : "White");
        structuralElement2.setValue("U");
        colorPicker2.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.UColor.toString(), colorPicker2.getValue().toString())));
        letterColor2.setValue(theme.getOrDefault(ThemeParameter.UChar.toString(), letterColor2.getValue().toString()).toLowerCase().equals("#000000") ? "Black" : "White");
        structuralElement3.setValue("G");
        colorPicker3.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.GColor.toString(), colorPicker4.getValue().toString())));
        letterColor3.setValue(theme.getOrDefault(ThemeParameter.GChar.toString(), letterColor3.getValue().toString()).toLowerCase().equals("#000000") ? "Black" : "White");
        structuralElement4.setValue("C");
        letterColor4.setValue(theme.getOrDefault(ThemeParameter.CChar.toString(), letterColor4.getValue().toString()).toLowerCase().equals("#000000") ? "Black" : "White");
        colorPicker4.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.CColor.toString(), colorPicker3.getValue().toString())));
        structuralElement5.setValue("X");
        letterColor5.setValue(theme.getOrDefault(ThemeParameter.XChar.toString(), letterColor5.getValue().toString()).toLowerCase().equals("#000000") ? "Black" : "White");
        colorPicker5.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.XColor.toString(), colorPicker5.getValue().toString())));
        structuralElement6.setValue("2D");
        colorPicker6.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.SecondaryColor.toString(), colorPicker6.getValue().toString())));
        structuralElement7.setValue("3D");
        colorPicker7.setValue(javafx.scene.paint.Color.web(theme.getOrDefault(ThemeParameter.TertiaryColor.toString(), colorPicker7.getValue().toString())));
        _3dOpacity.setValue((int)(100.0*255.0/Integer.parseInt(theme.getOrDefault(ThemeParameter.TertiaryOpacity.toString(), ""+(int)(_3dOpacity.getValue()*255.0/100.0)))));
        secondaryInteractionWidth.setValue(theme.getOrDefault(ThemeParameter.SecondaryInteractionWidth.toString(), ""+getSecondaryInteractionWidth()));
        tertiaryInteractionWidth.setValue(theme.getOrDefault(ThemeParameter.TertiaryInteractionWidth.toString(),""+getTertiaryInteractionWidth()));
        haloWidth.setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.HaloWidth.toString(),""+getHaloWidth())));
        residueBorder.setValue(theme.getOrDefault(ThemeParameter.ResidueBorder.toString(),""+getResidueBorder()));
        tertiaryInteractionStyle.setValue(theme.getOrDefault(ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue()));
        fontNames.setValue(theme.getOrDefault(ThemeParameter.FontName.toString(), getFontName()));
        deltaXRes.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.DeltaXRes.toString(), ""+getDeltaXRes())));
        deltaYRes.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.DeltaYRes.toString(), ""+getDeltaYRes())));
        deltaFontSize.getValueFactory().setValue(Integer.parseInt(theme.getOrDefault(ThemeParameter.DeltaFontSize.toString(), ""+getDeltaFontSize())));
    }

    public Map<String,String> getTheme() {
        Map<String, String> theme = new HashMap<String, String>();
        theme.put(ThemeParameter.AColor.toString(), this.getAColor());
        theme.put(ThemeParameter.AChar.toString(), this.getAChar());
        theme.put(ThemeParameter.UColor.toString(), this.getUColor());
        theme.put(ThemeParameter.UChar.toString(), this.getUChar());
        theme.put(ThemeParameter.GColor.toString(), this.getGColor());
        theme.put(ThemeParameter.GChar.toString(), this.getGChar());
        theme.put(ThemeParameter.CColor.toString(), this.getCColor());
        theme.put(ThemeParameter.CChar.toString(), this.getCChar());
        theme.put(ThemeParameter.XColor.toString(), this.getXColor());
        theme.put(ThemeParameter.XChar.toString(), this.getXChar());
        theme.put(ThemeParameter.SecondaryColor.toString(), this.getSecondaryInteractionColor());
        theme.put(ThemeParameter.TertiaryColor.toString(), this.getTertiaryInteractionColor());
        theme.put(ThemeParameter.TertiaryOpacity.toString(), ""+(int)(_3dOpacity.getValue()*255.0/100.0));
        theme.put(ThemeParameter.HaloWidth.toString(), ""+(int)haloWidth.getValue());
        theme.put(ThemeParameter.SecondaryInteractionWidth.toString(), ""+getSecondaryInteractionWidth());
        theme.put(ThemeParameter.TertiaryInteractionWidth.toString(), ""+getTertiaryInteractionWidth());
        theme.put(ThemeParameter.ResidueBorder.toString(), ""+getResidueBorder());
        theme.put(ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue());
        theme.put(ThemeParameter.FontName.toString(), getFontName());
        theme.put(ThemeParameter.DeltaXRes.toString(), ""+getDeltaXRes());
        theme.put(ThemeParameter.DeltaYRes.toString(), ""+getDeltaYRes());
        theme.put(ThemeParameter.DeltaFontSize.toString(), ""+getDeltaFontSize());
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
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(0);
                    break;
                case "1":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(1);
                    break;
                case "2":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(2);
                    break;
                case "3":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(3);
                    break;
                case "4":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(4);
                    break;
                case "5":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(5);
                    break;
                case "6":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(6);
                    break;
                case "7":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(7);
                    break;
                case "8":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(8);
                    break;
                case "9":
                    shape = new Line(0, 10, 50, 10);
                    shape.setStrokeWidth(9);
                    break;
                case "10":
                    shape = new Line(0, 10, 50, 10);
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
        return (int)haloWidth.getValue();
    }

    @Override
    public int getTertiaryOpacity() {
        return (int)((double)(_3dOpacity.getValue())/100.0*255);
    }

    @Override
    public String getAColor() {
        if (structuralElement1.getValue() == "A")
            return getHTMLColorString(javaFXToAwt(colorPicker1.getValue()));
        if (structuralElement2.getValue() == "A")
            return getHTMLColorString(javaFXToAwt(colorPicker2.getValue()));
        if (structuralElement3.getValue() == "A")
            return getHTMLColorString(javaFXToAwt(colorPicker3.getValue()));
        if (structuralElement4.getValue() == "A")
            return getHTMLColorString(javaFXToAwt(colorPicker4.getValue()));
        if (structuralElement5.getValue() == "A")
            return getHTMLColorString(javaFXToAwt(colorPicker5.getValue()));
        if (structuralElement6.getValue() == "A")
            return getHTMLColorString(javaFXToAwt(colorPicker6.getValue()));
        if (structuralElement7.getValue() == "A")
            return getHTMLColorString(javaFXToAwt(colorPicker7.getValue()));
        else
            return getHTMLColorString(Color.LIGHT_GRAY);
    }

    @Override
    public String getAChar() {
        if (structuralElement1.getValue() == "A")
            return getHTMLColorString(letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement2.getValue() == "A")
            return getHTMLColorString(letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement3.getValue() == "A")
            return getHTMLColorString(letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement4.getValue() == "A")
            return getHTMLColorString(letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement5.getValue() == "A")
            return getHTMLColorString(letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement6.getValue() == "A")
            return getHTMLColorString(letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement7.getValue() == "A")
            return getHTMLColorString(letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK);
        else
            return getHTMLColorString(Color.WHITE);
    }

    @Override
    public String getUColor() {
        if (structuralElement1.getValue() == "U")
            return getHTMLColorString(javaFXToAwt(colorPicker1.getValue()));
        if (structuralElement2.getValue() == "U")
            return getHTMLColorString(javaFXToAwt(colorPicker2.getValue()));
        if (structuralElement3.getValue() == "U")
            return getHTMLColorString(javaFXToAwt(colorPicker3.getValue()));
        if (structuralElement4.getValue() == "U")
            return getHTMLColorString(javaFXToAwt(colorPicker4.getValue()));
        if (structuralElement5.getValue() == "U")
            return getHTMLColorString(javaFXToAwt(colorPicker5.getValue()));
        if (structuralElement6.getValue() == "U")
            return getHTMLColorString(javaFXToAwt(colorPicker6.getValue()));
        if (structuralElement7.getValue() == "U")
            return getHTMLColorString(javaFXToAwt(colorPicker7.getValue()));
        else
            return getHTMLColorString(Color.LIGHT_GRAY);
    }

    @Override
    public String getUChar() {
        if (structuralElement1.getValue() == "U")
            return getHTMLColorString(letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement2.getValue() == "U")
            return getHTMLColorString(letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement3.getValue() == "U")
            return getHTMLColorString(letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement4.getValue() == "U")
            return getHTMLColorString(letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement5.getValue() == "U")
            return getHTMLColorString(letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement6.getValue() == "U")
            return getHTMLColorString(letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement7.getValue() == "U")
            return getHTMLColorString(letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK);
        else
            return getHTMLColorString(Color.WHITE);
    }

    @Override
    public String getCColor() {
        if (structuralElement1.getValue() == "C")
            return getHTMLColorString(javaFXToAwt(colorPicker1.getValue()));
        if (structuralElement2.getValue() == "C")
            return getHTMLColorString(javaFXToAwt(colorPicker2.getValue()));
        if (structuralElement3.getValue() == "C")
            return getHTMLColorString(javaFXToAwt(colorPicker3.getValue()));
        if (structuralElement4.getValue() == "C")
            return getHTMLColorString(javaFXToAwt(colorPicker4.getValue()));
        if (structuralElement5.getValue() == "C")
            return getHTMLColorString(javaFXToAwt(colorPicker5.getValue()));
        if (structuralElement6.getValue() == "C")
            return getHTMLColorString(javaFXToAwt(colorPicker6.getValue()));
        if (structuralElement7.getValue() == "C")
            return getHTMLColorString(javaFXToAwt(colorPicker7.getValue()));
        else {
            return getHTMLColorString(Color.LIGHT_GRAY);
        }
    }

    @Override
    public String getCChar() {
        if (structuralElement1.getValue() == "C")
            return getHTMLColorString(letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement2.getValue() == "C")
            return getHTMLColorString(letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement3.getValue() == "C")
            return getHTMLColorString(letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement4.getValue() == "C")
            return getHTMLColorString(letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement5.getValue() == "C")
            return getHTMLColorString(letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement6.getValue() == "C")
            return getHTMLColorString(letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement7.getValue() == "C")
            return getHTMLColorString(letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK);
        else
            return getHTMLColorString(Color.WHITE);
    }

    @Override
    public String getGColor() {
        if (structuralElement1.getValue() == "G")
            return getHTMLColorString(javaFXToAwt(colorPicker1.getValue()));
        if (structuralElement2.getValue() == "G")
            return getHTMLColorString(javaFXToAwt(colorPicker2.getValue()));
        if (structuralElement3.getValue() == "G")
            return getHTMLColorString(javaFXToAwt(colorPicker3.getValue()));
        if (structuralElement4.getValue() == "G")
            return getHTMLColorString(javaFXToAwt(colorPicker4.getValue()));
        if (structuralElement5.getValue() == "G")
            return getHTMLColorString(javaFXToAwt(colorPicker5.getValue()));
        if (structuralElement6.getValue() == "G")
            return getHTMLColorString(javaFXToAwt(colorPicker6.getValue()));
        if (structuralElement7.getValue() == "G")
            return getHTMLColorString(javaFXToAwt(colorPicker7.getValue()));
        else
            return getHTMLColorString(Color.LIGHT_GRAY);
    }

    @Override
    public String getGChar() {
        if (structuralElement1.getValue() == "G")
            return getHTMLColorString(letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement2.getValue() == "G")
            return getHTMLColorString(letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement3.getValue() == "G")
            return getHTMLColorString(letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement4.getValue() == "G")
            return getHTMLColorString(letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement5.getValue() == "G")
            return getHTMLColorString(letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement6.getValue() == "G")
            return getHTMLColorString(letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement7.getValue() == "G")
            return getHTMLColorString(letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK);
        else
            return getHTMLColorString(Color.WHITE);
    }

    @Override
    public String getXColor() {
        if (structuralElement1.getValue() == "X")
            return getHTMLColorString(javaFXToAwt(colorPicker1.getValue()));
        if (structuralElement2.getValue() == "X")
            return getHTMLColorString(javaFXToAwt(colorPicker2.getValue()));
        if (structuralElement3.getValue() == "X")
            return getHTMLColorString(javaFXToAwt(colorPicker3.getValue()));
        if (structuralElement4.getValue() == "X")
            return getHTMLColorString(javaFXToAwt(colorPicker4.getValue()));
        if (structuralElement5.getValue() == "X")
            return getHTMLColorString(javaFXToAwt(colorPicker5.getValue()));
        if (structuralElement6.getValue() == "X")
            return getHTMLColorString(javaFXToAwt(colorPicker6.getValue()));
        if (structuralElement7.getValue() == "X")
            return getHTMLColorString(javaFXToAwt(colorPicker7.getValue()));
        else
            return getHTMLColorString(Color.LIGHT_GRAY);
    }

    @Override
    public String getXChar() {
        if (structuralElement1.getValue() == "X")
            return getHTMLColorString(letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement2.getValue() == "X")
            return getHTMLColorString(letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement3.getValue() == "X")
            return getHTMLColorString(letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement4.getValue() == "X")
            return getHTMLColorString(letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement5.getValue() == "X")
            return getHTMLColorString(letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement6.getValue() == "X")
            return getHTMLColorString(letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK);
        if (structuralElement7.getValue() == "X")
            return getHTMLColorString(letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK);
        else
            return getHTMLColorString(Color.WHITE);
    }

    @Override
    public String getSecondaryInteractionColor() {
        if (structuralElement1.getValue() == "2D")
            return getHTMLColorString(javaFXToAwt(colorPicker1.getValue()));
        if (structuralElement2.getValue() == "2D")
            return getHTMLColorString(javaFXToAwt(colorPicker2.getValue()));
        if (structuralElement3.getValue() == "2D")
            return getHTMLColorString(javaFXToAwt(colorPicker3.getValue()));
        if (structuralElement4.getValue() == "2D")
            return getHTMLColorString(javaFXToAwt(colorPicker4.getValue()));
        if (structuralElement5.getValue() == "2D")
            return getHTMLColorString(javaFXToAwt(colorPicker5.getValue()));
        if (structuralElement6.getValue() == "2D")
            return getHTMLColorString(javaFXToAwt(colorPicker6.getValue()));
        if (structuralElement7.getValue() == "2D")
            return getHTMLColorString(javaFXToAwt(colorPicker7.getValue()));
        else
            return getHTMLColorString(Color.LIGHT_GRAY);
    }

    @Override
    public String getTertiaryInteractionColor() {
        if (structuralElement1.getValue() == "3D")
            return getHTMLColorString(javaFXToAwt(colorPicker1.getValue()));
        if (structuralElement2.getValue() == "3D")
            return getHTMLColorString(javaFXToAwt(colorPicker2.getValue()));
        if (structuralElement3.getValue() == "3D")
            return getHTMLColorString(javaFXToAwt(colorPicker3.getValue()));
        if (structuralElement4.getValue() == "3D")
            return getHTMLColorString(javaFXToAwt(colorPicker4.getValue()));
        if (structuralElement5.getValue() == "3D")
            return getHTMLColorString(javaFXToAwt(colorPicker5.getValue()));
        if (structuralElement6.getValue() == "3D")
            return getHTMLColorString(javaFXToAwt(colorPicker6.getValue()));
        if (structuralElement7.getValue() == "3D")
            return getHTMLColorString(javaFXToAwt(colorPicker7.getValue()));
        else
            return getHTMLColorString(Color.LIGHT_GRAY);
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
    public int getDeltaXRes() {
        return deltaXRes.getValue();
    }

    @Override
    public int getDeltaYRes() {
        return deltaYRes.getValue();
    }

    @Override
    public int getDeltaFontSize() {
        return deltaFontSize.getValue().intValue();
    }

    @Override
    public String getTertiaryInteractionStyle() {
        return tertiaryInteractionStyle.getValue();
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

    private javafx.scene.paint.Color awtColorToJavaFX(java.awt.Color c) {
        return javafx.scene.paint.Color.rgb(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 255.0);
    }

}


