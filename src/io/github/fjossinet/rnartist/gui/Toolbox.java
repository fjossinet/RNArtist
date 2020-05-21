package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.io.Backend;
import io.github.fjossinet.rnartist.core.model.*;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getAWTColor;
import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;

public class Toolbox extends AbstractThemeConfigurator {

    private Stage stage;
    private Mediator mediator;
    private ColorPicker colorPicker1, colorPicker2, colorPicker3, colorPicker4, colorPicker5, colorPicker6, colorPicker7;
    private ChoiceBox<String>   structuralElement1 = new ChoiceBox<String>(),
                                structuralElement2 = new ChoiceBox<String>(),
                                structuralElement3 = new ChoiceBox<String>(),
                                structuralElement4 = new ChoiceBox<String>(),
                                structuralElement5 = new ChoiceBox<String>(),
                                structuralElement6 = new ChoiceBox<String>(),
                                structuralElement7 = new ChoiceBox<String>(),
                                letterColor1,letterColor2,letterColor3,letterColor4,letterColor5,letterColor6,letterColor7;
    private Slider _3dOpacity, haloWidth, residueCharOpacity, secondaryInteractionShift;
    private ComboBox<String> fontNames, residueBorder, phosphoDiesterWidth,
            secondaryInteractionWidth,
            tertiaryInteractionWidth, tertiaryInteractionStyle;
    private ComboBox<Object> structureElementsSelectedComboBox;
    private ComboBox<Pair<String, NitriteId>> savedThemesComboBox;
    private ObservableList<Pair<String, NitriteId>> savedThemes = FXCollections.observableList(new ArrayList());
    private Spinner<Integer> deltaXRes, deltaYRes, deltaFontSize;
    private FlowPane junctionKnobs = new FlowPane();

    private ObservableList<ThemeFromWebsite> themesList;

    public Toolbox(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("Toolbox");
        this.createScene(stage);
        for (Document theme: mediator.getEmbeddedDB().getThemes().find()) {
            this.savedThemes.add(new Pair<String,NitriteId>((String)theme.get("name"), theme.getId())) ;
        }
        //new Thread(new LoadThemesFromWebsite()).run();
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {

        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //this.createAllThemesPanel(root);
        this.createThemePanel(root);
        this.createLayoutPanel(root);
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
        //+++++ list of saved themes
        this.savedThemesComboBox = new ComboBox<Pair<String,NitriteId>>(this.savedThemes);
        this.savedThemesComboBox.setConverter(new StringConverter<Pair<String,NitriteId>>() {
            @Override
            public String toString(Pair<String,NitriteId> theme) {
                if (theme != null)
                    return theme.getKey();
                return "";
            }

            @Override
            public Pair<String,NitriteId> fromString(String s) {
                return null;
            }
        });
        this.savedThemesComboBox.setMaxWidth(Double.MAX_VALUE);
        this.savedThemesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pair<String,NitriteId>>() {

            public void changed(ObservableValue<? extends Pair<String,NitriteId>> ov,
                                final Pair<String,NitriteId> oldvalue, final Pair<String,NitriteId> newvalue) {
                if (newvalue != null) {
                    loadTheme(mediator.getEmbeddedDB().getTheme(newvalue.getValue()));
                }
            }
        });

        Button applySavedTheme = new Button("Apply");
        applySavedTheme.setMinWidth(75);
        applySavedTheme.setDisable(true);
        this.savedThemesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pair<String, NitriteId>>() {
            @Override
            public void changed(ObservableValue<? extends Pair<String, NitriteId>> observableValue, Pair<String, NitriteId> stringNitriteIdPair, Pair<String, NitriteId> newValue) {
                if (newValue != null)
                    applySavedTheme.setDisable(false);
            }
        });
        applySavedTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                loadTheme(mediator.getEmbeddedDB().getTheme(savedThemesComboBox.getValue().getValue()));
            }
        });

        GridPane savedThemesForm = new GridPane();
        savedThemesForm.setHgap(5);
        savedThemesForm.setVgap(10);
        savedThemesForm.setPadding(new Insets(0, 0, 5, 0));

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        savedThemesForm.getColumnConstraints().addAll(cc, new ColumnConstraints());

        Label l = new Label("Your Saved Themes");
        GridPane.setConstraints(l, 0,0,2,1);
        savedThemesForm.getChildren().add(l);

        GridPane.setConstraints(savedThemesComboBox, 0,1,1,1);
        savedThemesForm.getChildren().add(savedThemesComboBox);

        GridPane.setConstraints(applySavedTheme, 1,1,1,1);
        savedThemesForm.getChildren().add(applySavedTheme);

        vbox.getChildren().add(savedThemesForm);

        //+++++list of the structural elements selected listening to theme modifications

        this.structureElementsSelectedComboBox = new ComboBox<Object>(mediator.getStructureElementsSelected());
        this.structureElementsSelectedComboBox.setConverter(new StringConverter<Object>() {
            @Override
            public String toString(Object secondaryStructureElement) {
                if (secondaryStructureElement == null) {
                    return "";
                }
                if (SecondaryStructureElement.class.isInstance(secondaryStructureElement))
                    return ((SecondaryStructureElement)secondaryStructureElement).getType()+" "+((SecondaryStructureElement)secondaryStructureElement).getName()+ " "+((SecondaryStructureElement)secondaryStructureElement).getLocation().getDescription();
                else if (String.class.isInstance(secondaryStructureElement)) {
                    return secondaryStructureElement.toString();
                }
                return "";
            }

            @Override
            public SecondaryStructureElement fromString(String s) {
                return null;
            }
        });
        this.structureElementsSelectedComboBox.setMaxWidth(Double.MAX_VALUE);
        this.structureElementsSelectedComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {

            public void changed(ObservableValue<? extends Object> ov,
                                final Object oldvalue, final Object newvalue) {
                removeThemeConfiguratorListeners();
                if (newvalue != null) {
                    if (newvalue.toString().equals("Full 2D")) {
                        addThemeConfiguratorListener(mediator.getSecondaryStructureDrawing().getTheme());
                        loadTheme(mediator.getSecondaryStructureDrawing().getTheme());
                    } else if (newvalue.toString().equals("All Selected Elements")) {
                        for (Object o : structureElementsSelectedComboBox.getItems().subList(2, structureElementsSelectedComboBox.getItems().size())) {
                            addThemeConfiguratorListener(((SecondaryStructureElement) o).getTheme());
                        }
                        loadTheme((SecondaryStructureElement)structureElementsSelectedComboBox.getItems().get(2));
                    } else {
                        addThemeConfiguratorListener(((SecondaryStructureElement) newvalue).getTheme());
                        loadTheme((SecondaryStructureElement)newvalue);
                    }
                    mediator.canvas2D.repaint();
                }
            }
        });

        VBox _vbox = new VBox();
        _vbox.setSpacing(5);
        _vbox.getChildren().add(new Label("Choose the Target"));
        _vbox.getChildren().add(this.structureElementsSelectedComboBox);

        vbox.getChildren().add(_vbox);

        //++++++ pane for the fonts
        Label title = new Label("Font");
        title.setStyle("-fx-font-size: 15");
        vbox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane fontsPane = new GridPane();
        fontsPane.setHgap(5);
        fontsPane.setVgap(10);
        fontsPane.setPadding(new Insets(0, 0, 5, 0));

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        fontsPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(),new ColumnConstraints(),new ColumnConstraints(),new ColumnConstraints(),cc);

        fontNames = new ComboBox<>(
                observableList(Font.getFamilies().stream().distinct().collect(toList())));

        EventHandler eventHandler = (event) -> {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        fireThemeChange(ThemeParameter.FontName, fontNames.getValue());
                        mediator.getCanvas2D().repaint();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        };

        fontNames.setOnAction(eventHandler);
        fontNames.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.FontName.toString()));
        fontNames.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(fontNames, 0,0,6,1);
        fontsPane.getChildren().add(fontNames);

        deltaXRes = new Spinner<Integer>();
        deltaXRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultThemeParams.get(ThemeParameter.DeltaXRes.toString()))));
        deltaXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getSecondaryStructureDrawing() != null) {
                    fireThemeChange(ThemeParameter.DeltaXRes, deltaXRes.getValue());
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        deltaYRes = new Spinner<Integer>();
        deltaYRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultThemeParams.get(ThemeParameter.DeltaYRes.toString()))));
        deltaYRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getSecondaryStructureDrawing() != null) {
                    fireThemeChange(ThemeParameter.DeltaYRes, deltaYRes.getValue());
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        deltaFontSize = new Spinner<Integer>();
        deltaFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 5, Integer.parseInt(RnartistConfig.defaultThemeParams.get(ThemeParameter.DeltaFontSize.toString()))));
        deltaFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getSecondaryStructureDrawing() != null) {
                    fireThemeChange(ThemeParameter.DeltaFontSize, deltaFontSize.getValue().intValue());
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        l = new Label("x");
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

        GridPane fontsPane2 = new GridPane();
        fontsPane2.setHgap(5);
        fontsPane2.setVgap(10);
        fontsPane2.setPadding(new Insets(0, 0, 10, 0));

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        fontsPane.getColumnConstraints().addAll(new ColumnConstraints(),cc);

        l = new Label("Residue Character Opacity (%)");
        GridPane.setConstraints(l, 0,0,4,1);
        GridPane.setValignment(l,VPos.TOP);
        fontsPane2.getChildren().add(l);
        this.residueCharOpacity = new Slider(0, 100, Integer.parseInt(RnartistConfig.defaultThemeParams.get(ThemeParameter.ResidueCharOpacity.toString()))/255.0*100.0);
        this.residueCharOpacity.setShowTickLabels(true);
        this.residueCharOpacity.setShowTickMarks(true);
        this.residueCharOpacity.setMajorTickUnit(50);
        this.residueCharOpacity.setMinorTickCount(5);
        this.residueCharOpacity.setShowTickMarks(true);
        this.residueCharOpacity.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.ResidueCharOpacity,(int) ((double)(residueCharOpacity.getValue()) / 100.0 * 255.0));
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(this.residueCharOpacity, 4,0,2,1);
        this.residueCharOpacity.setMaxWidth(Double.MAX_VALUE);
        fontsPane2.getChildren().add(this.residueCharOpacity);

        l = new Label("Ticks Character Opacity (%)");
        GridPane.setConstraints(l, 0,1,4,1);
        GridPane.setValignment(l,VPos.TOP);
        fontsPane2.getChildren().add(l);
        final Slider slider2 = new Slider(0, 100, 100);
        slider2.setShowTickLabels(true);
        slider2.setShowTickMarks(true);
        slider2.setMajorTickUnit(50);
        slider2.setMinorTickCount(5);
        slider2.setShowTickMarks(true);
        slider2.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //RnartistConfig.setResidueFading((int)(slider2.getValue()/100.0*255.0));
                //mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(slider2, 4,1,2,1);
        slider2.setMaxWidth(Double.MAX_VALUE);
        fontsPane2.getChildren().add(slider2);

        vbox.getChildren().add(fontsPane2);

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
                    Theme colors = RnartistConfig.defaultColorSchemes.get(colorSchemeChoices.getValue());
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
        colorPicker1.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color t1) {
                if (structuralElement1.getValue()!=null) {
                    switch (structuralElement1.getValue()) {
                        case "A":
                            fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker1.getValue()));
                            break;
                        case "U":
                            fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker1.getValue()));
                            break;
                        case "G":
                            fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker1.getValue()));
                            break;
                        case "C":
                            fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker1.getValue()));
                            break;
                        case "X":
                            fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker1.getValue()));
                            break;
                        case "2D":
                            fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker1.getValue()));
                            break;
                        case "3D":
                            fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker1.getValue()));
                            break;
                    }
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        colorPicker1.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultThemeParams.get(ThemeParameter.AColor.toString()))));
        colorPicker1.setMaxWidth(Double.MAX_VALUE);

        colorPicker2 = new ColorPicker();
        colorPicker2.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color t1) {
                if (structuralElement1.getValue() != null) {
                    switch (structuralElement2.getValue()) {
                        case "A":
                            fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker2.getValue()));
                            break;
                        case "U":
                            fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker2.getValue()));
                            break;
                        case "G":
                            fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker2.getValue()));
                            break;
                        case "C":
                            fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker2.getValue()));
                            break;
                        case "X":
                            fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker2.getValue()));
                            break;
                        case "2D":
                            fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker2.getValue()));
                            break;
                        case "3D":
                            fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker2.getValue()));
                            break;
                    }
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        colorPicker2.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultThemeParams.get(ThemeParameter.UColor.toString()))));
        colorPicker2.setMaxWidth(Double.MAX_VALUE);

        colorPicker3 = new ColorPicker();
        colorPicker3.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color t1) {
                if (structuralElement1.getValue() != null) {
                    switch (structuralElement3.getValue()) {
                        case "A":
                            fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker3.getValue()));
                            break;
                        case "U":
                            fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker3.getValue()));
                            break;
                        case "G":
                            fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker3.getValue()));
                            break;
                        case "C":
                            fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker3.getValue()));
                            break;
                        case "X":
                            fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker3.getValue()));
                            break;
                        case "2D":
                            fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker3.getValue()));
                            break;
                        case "3D":
                            fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker3.getValue()));
                            break;
                    }
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        colorPicker3.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultThemeParams.get(ThemeParameter.GColor.toString()))));
        colorPicker3.setMaxWidth(Double.MAX_VALUE);

        colorPicker4 = new ColorPicker();
        colorPicker4.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color t1) {
                if (structuralElement1.getValue() != null) {
                    switch (structuralElement4.getValue()) {
                        case "A":
                            fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker4.getValue()));
                            break;
                        case "U":
                            fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker4.getValue()));
                            break;
                        case "G":
                            fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker4.getValue()));
                            break;
                        case "C":
                            fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker4.getValue()));
                            break;
                        case "X":
                            fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker4.getValue()));
                            break;
                        case "2D":
                            fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker4.getValue()));
                            break;
                        case "3D":
                            fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker4.getValue()));
                            break;
                    }
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        colorPicker4.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultThemeParams.get(ThemeParameter.CColor.toString()))));
        colorPicker4.setMaxWidth(Double.MAX_VALUE);

        colorPicker5 = new ColorPicker();
        colorPicker5.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color t1) {
                if (structuralElement1.getValue() != null) {
                    switch (structuralElement5.getValue()) {
                        case "A":
                            fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker5.getValue()));
                            break;
                        case "U":
                            fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker5.getValue()));
                            break;
                        case "G":
                            fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker5.getValue()));
                            break;
                        case "C":
                            fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker5.getValue()));
                            break;
                        case "X":
                            fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker5.getValue()));
                            break;
                        case "2D":
                            fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker5.getValue()));
                            break;
                        case "3D":
                            fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker5.getValue()));
                            break;
                    }
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        colorPicker5.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultThemeParams.get(ThemeParameter.XColor.toString()))));
        colorPicker5.setMaxWidth(Double.MAX_VALUE);

        colorPicker6 = new ColorPicker();
        colorPicker6.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color t1) {
                if (structuralElement1.getValue() != null) {
                    switch (structuralElement6.getValue()) {
                        case "A":
                            fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker6.getValue()));
                            break;
                        case "U":
                            fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker6.getValue()));
                            break;
                        case "G":
                            fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker6.getValue()));
                            break;
                        case "C":
                            fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker6.getValue()));
                            break;
                        case "X":
                            fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker6.getValue()));
                            break;
                        case "2D":
                            fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker6.getValue()));
                            break;
                        case "3D":
                            fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker6.getValue()));
                            break;
                    }
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        colorPicker6.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultThemeParams.get(ThemeParameter.SecondaryColor.toString()))));
        colorPicker6.setMaxWidth(Double.MAX_VALUE);

        colorPicker7 = new ColorPicker();
        colorPicker7.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color t1) {
                if (structuralElement1.getValue() != null) {
                    switch (structuralElement7.getValue()) {
                        case "A":
                            fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker7.getValue()));
                            break;
                        case "U":
                            fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker7.getValue()));
                            break;
                        case "G":
                            fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker7.getValue()));
                            break;
                        case "C":
                            fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker7.getValue()));
                            break;
                        case "X":
                            fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker7.getValue()));
                            break;
                        case "2D":
                            fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker7.getValue()));
                            break;
                        case "3D":
                            fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker7.getValue()));
                            break;
                    }
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        colorPicker7.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultThemeParams.get(ThemeParameter.TertiaryColor.toString()))));
        colorPicker7.setMaxWidth(Double.MAX_VALUE);

        l = new Label("Background");
        GridPane.setConstraints(l, 1,1,1,1);
        GridPane.setHalignment(l,HPos.CENTER);
        colorsPane.getChildren().add(l);
        l = new Label("Character");
        GridPane.setConstraints(l, 2,1,1,1);
        GridPane.setHalignment(l,HPos.CENTER);
        colorsPane.getChildren().add(l);

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
        letterColor1.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement1.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                }
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
                    case "A": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor1.setDisable(false); letterColor1.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor1.setDisable(true); break;
                    case "3D": letterColor1.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

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
        letterColor2.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement2.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                }
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
                    case "A": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor2.setDisable(false); letterColor2.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor2.setDisable(true); break;
                    case "3D": letterColor2.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

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
        letterColor3.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement3.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                }
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
                    case "A": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor3.setDisable(false); letterColor3.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor3.setDisable(true); break;
                    case "3D": letterColor3.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

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
        letterColor4.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement4.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                }
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
                    case "A": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor4.setDisable(false); letterColor4.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor4.setDisable(true); break;
                    case "3D": letterColor4.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

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
        letterColor5.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (structuralElement5.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                }
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
                    case "A": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor5.setDisable(false); letterColor5.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor5.setDisable(true); break;
                    case "3D": letterColor5.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

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
                switch (structuralElement6.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                }
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
                    case "A": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor6.setDisable(false); letterColor6.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "2D": letterColor6.setDisable(true); break;
                    case "3D": letterColor6.setDisable(true); break;
                }
                mediator.canvas2D.repaint();
            }
        });

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
                switch (structuralElement7.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                }
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
                    case "A": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.AChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black"); break;
                    case "U": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.UChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "G": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.GChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "C": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.CChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
                    case "X": letterColor7.setDisable(false); letterColor7.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.XChar.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");break;
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
        linesPane.setPadding(new Insets(0, 0, 10, 0));

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        linesPane.getColumnConstraints().addAll(new ColumnConstraints(150),cc);

        int row = 0;

        l = new Label("Residues Border (px)");
        GridPane.setConstraints(l, 0,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        residueBorder = new ComboBox<>();
        residueBorder.getItems().addAll("0", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0");
        residueBorder.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.ResidueBorder.toString()));
        residueBorder.setCellFactory(new ShapeCellFactory());
        residueBorder.setButtonCell(new ShapeCell());
        residueBorder.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireThemeChange(ThemeParameter.ResidueBorder,Double.parseDouble(residueBorder.getValue()));
                mediator.getCanvas2D().repaint();
            }
        });
        residueBorder.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(residueBorder, 2,row,1,1);
        linesPane.getChildren().add(residueBorder);
        GridPane.setHalignment(residueBorder,HPos.CENTER);

        row++;

        l = new Label("Phosphodiester Line Width (px)");
        GridPane.setConstraints(l, 0,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        phosphoDiesterWidth = new ComboBox<>();
        phosphoDiesterWidth.getItems().addAll("0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0");
        phosphoDiesterWidth.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.PhosphodiesterWidth.toString()));
        phosphoDiesterWidth.setCellFactory(new ShapeCellFactory());
        phosphoDiesterWidth.setButtonCell(new ShapeCell());
        phosphoDiesterWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireThemeChange(ThemeParameter.PhosphodiesterWidth, Double.parseDouble(phosphoDiesterWidth.getValue()));
                mediator.getCanvas2D().repaint();
            }
        });
        phosphoDiesterWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(phosphoDiesterWidth, 2,row,1,1);
        linesPane.getChildren().add(phosphoDiesterWidth);
        GridPane.setHalignment(phosphoDiesterWidth,HPos.CENTER);

        row++;

        l = new Label("Secondaries Line Width (px)");
        GridPane.setConstraints(l, 0,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        secondaryInteractionWidth = new ComboBox<>();
        secondaryInteractionWidth.getItems().addAll("0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0");
        secondaryInteractionWidth.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.SecondaryInteractionWidth.toString()));
        secondaryInteractionWidth.setCellFactory(new ShapeCellFactory());
        secondaryInteractionWidth.setButtonCell(new ShapeCell());
        secondaryInteractionWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireThemeChange(ThemeParameter.SecondaryInteractionWidth, Double.parseDouble(secondaryInteractionWidth.getValue()));
                mediator.getCanvas2D().repaint();
            }
        });
        secondaryInteractionWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(secondaryInteractionWidth, 2,row,1,1);
        linesPane.getChildren().add(secondaryInteractionWidth);
        GridPane.setHalignment(secondaryInteractionWidth,HPos.CENTER);

        row++;

        l = new Label("Secondaries Line Shift (px)");
        GridPane.setConstraints(l, 0,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        secondaryInteractionShift = new Slider(0, 10, Double.parseDouble(RnartistConfig.defaultThemeParams.get(ThemeParameter.SecondaryInteractionShift.toString())));
        secondaryInteractionShift.setShowTickLabels(true);
        secondaryInteractionShift.setShowTickMarks(true);
        secondaryInteractionShift.setMajorTickUnit(50);
        secondaryInteractionShift.setMinorTickCount(5);
        secondaryInteractionShift.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.SecondaryInteractionShift, secondaryInteractionShift.getValue());
                mediator.getCanvas2D().repaint();
            }
        });
        secondaryInteractionShift.setShowTickMarks(true);
        secondaryInteractionShift.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(secondaryInteractionShift, 2,row,1,1);
        linesPane.getChildren().add(secondaryInteractionShift);
        GridPane.setHalignment(secondaryInteractionShift,HPos.CENTER);

        row++;

        l = new Label("Tertiaries Line Width (px)");
        GridPane.setConstraints(l, 0,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        tertiaryInteractionWidth = new ComboBox<>();
        tertiaryInteractionWidth.getItems().addAll("0", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0");
        tertiaryInteractionWidth.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.TertiaryInteractionWidth.toString()));
        tertiaryInteractionWidth.setCellFactory(new ShapeCellFactory());
        tertiaryInteractionWidth.setButtonCell(new ShapeCell());
        tertiaryInteractionWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireThemeChange(ThemeParameter.TertiaryInteractionWidth, tertiaryInteractionWidth.getValue());
                mediator.getCanvas2D().repaint();
            }
        });
        tertiaryInteractionWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(tertiaryInteractionWidth, 2,row,1,1);
        linesPane.getChildren().add(tertiaryInteractionWidth);
        GridPane.setHalignment(tertiaryInteractionWidth,HPos.CENTER);

        row++;

        l = new Label("Tertiaries Line Style");
        GridPane.setConstraints(l, 0,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        tertiaryInteractionStyle = new ComboBox<>();
        tertiaryInteractionStyle.getItems().addAll("solid", "dashed");
        tertiaryInteractionStyle.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.TertiaryInteractionStyle.toString()));
        tertiaryInteractionStyle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireThemeChange(ThemeParameter.TertiaryInteractionStyle, tertiaryInteractionStyle.getValue());
                mediator.getCanvas2D().repaint();
            }
        });
        tertiaryInteractionStyle.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(tertiaryInteractionStyle, 2,row,1,1);
        linesPane.getChildren().add(tertiaryInteractionStyle);
        GridPane.setHalignment(tertiaryInteractionStyle,HPos.CENTER);

        row++;

        l = new Label("Tertiaries Opacity (%)");
        GridPane.setConstraints(l, 0,row,2,1);
        GridPane.setValignment(l,VPos.TOP);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        _3dOpacity = new Slider(0, 100, Integer.parseInt(RnartistConfig.defaultThemeParams.get(ThemeParameter.TertiaryOpacity.toString()))/255.0*100.0);
        _3dOpacity.setShowTickLabels(true);
        _3dOpacity.setShowTickMarks(true);
        _3dOpacity.setMajorTickUnit(50);
        _3dOpacity.setMinorTickCount(5);
        _3dOpacity.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.TertiaryOpacity, (int)((double)(_3dOpacity.getValue())/100.0*255));
                mediator.getCanvas2D().repaint();
            }
        });
        _3dOpacity.setShowTickMarks(true);
        _3dOpacity.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(_3dOpacity, 2,row,1,1);
        linesPane.getChildren().add(_3dOpacity);
        GridPane.setHalignment(_3dOpacity,HPos.CENTER);

        row++;

        l = new Label("Tertiaries Halo Size (px)");
        GridPane.setConstraints(l, 0,row,2,1);
        GridPane.setValignment(l,VPos.TOP);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.RIGHT);

        haloWidth = new Slider(0, 20, Double.parseDouble(RnartistConfig.defaultThemeParams.get(ThemeParameter.HaloWidth.toString())));;
        haloWidth.setShowTickLabels(true);
        haloWidth.setShowTickMarks(true);
        haloWidth.setMajorTickUnit(5);
        haloWidth.setMinorTickCount(1);
        haloWidth.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.HaloWidth, haloWidth.getValue());
                mediator.getCanvas2D().repaint();
            }
        });
        haloWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(haloWidth, 2,row,1,1);
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

        Button saveTheme = new Button("Save this Theme");

        saveTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Theme Saving");
                dialog.setHeaderText("Choose a Name for your Theme");

                Optional<String> response = dialog.showAndWait();

                response.ifPresent(name -> {
                    NitriteId id = null;
                    if (structureElementsSelectedComboBox.getValue() == "Full 2D")
                        id = mediator.getEmbeddedDB().addTheme(name, mediator.getSecondaryStructureDrawing().getTheme());
                    else
                        id = mediator.getEmbeddedDB().addTheme(name, ((SecondaryStructureElement) structureElementsSelectedComboBox.getItems().get(structureElementsSelectedComboBox.getItems().size()-1)).getTheme());
                    if (id != null)
                        savedThemes.add(new Pair<>(name,id));
                });
            }
        });

        Button restoreParentTheme = new Button("Restore Parent Theme");

        restoreParentTheme.setDisable(true);

        this.structureElementsSelectedComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observableValue, Object o, Object newValue) {
                if (newValue.toString().equals("Full 2D")) {
                    restoreParentTheme.setDisable(true);
                } else {
                    restoreParentTheme.setDisable(false);
                }
            }
        });

        restoreParentTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (structureElementsSelectedComboBox.getValue().toString().equals("All Selected Elements")) {
                    for (Object el : structureElementsSelectedComboBox.getItems()) {
                        if (SecondaryStructureElement.class.isInstance(el)) {
                            ((SecondaryStructureElement)el).getTheme().clear();
                            loadTheme((SecondaryStructureElement)el);
                        }
                    }
                } else {
                    SecondaryStructureElement el = (SecondaryStructureElement)structureElementsSelectedComboBox.getValue();
                    el.getTheme().clear();
                    loadTheme(el);
                }
                mediator.getCanvas2D().repaint();
            }
        });
        saveForm.getChildren().add(restoreParentTheme);
        saveForm.getChildren().add(saveTheme);
        //saveForm.getChildren().add(shareOnline);

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

    public ComboBox<Object> getStructureElementsSelectedComboBox() {
        return structureElementsSelectedComboBox;
    }

    public ComboBox<Pair<String, NitriteId>> getSavedThemesComboBox() {
        return savedThemesComboBox;
    }

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
        TextField location = new TextField(jc.getLocation().getDescription());
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

    private void createAllThemesPanel(TabPane root) {
        VBox vbox =  new VBox();
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
                        RnartistConfig.save(null); //we save the chimera path, not the theme (perhaps the user is not interested to save the current theme)
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

        int row = 0;

        Label svgExport = new Label("Misc Settings");
        svgExport.setStyle("-fx-font-size: 15");
        optionsPane.getChildren().add(svgExport);
        GridPane.setConstraints(svgExport, 0,row++,2,1);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        optionsPane.getChildren().add(separator);
        GridPane.setConstraints(separator, 0, row++, 2, 1);

        CheckBox svgBrowserFix = new CheckBox();
        svgBrowserFix.setSelected(RnartistConfig.exportSVGWithBrowserCompatibility());
        svgBrowserFix.setOnAction(actionEvent -> {
            RnartistConfig.exportSVGWithBrowserCompatibility(svgBrowserFix.isSelected());
        });
        optionsPane.getChildren().add(svgBrowserFix);
        GridPane.setConstraints(svgBrowserFix, 0,row);

        Label l = new Label("Browser Compatibility for SVG");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1,row++);

        CheckBox showToolBar = new CheckBox();
        showToolBar.setSelected(true);
        showToolBar.setOnAction(actionEvent -> {
            mediator.getRnartist().showTopToolBars(showToolBar.isSelected());
        });
        optionsPane.getChildren().add(showToolBar);
        GridPane.setConstraints(showToolBar, 0,row);

        l = new Label("Show ToolBar");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1,row++);

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

    public void loadTheme(Theme theme) {
        if (theme.getAColor() != null) {
            structuralElement1.setValue("A");
            colorPicker1.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getAColor())));
            letterColor1.setValue(getHTMLColorString(theme.getAColor()).toLowerCase().equals("#000000") ? "Black" : "White");
        }

        if (theme.getUColor() != null) {
            structuralElement2.setValue("U");
            colorPicker2.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getUColor())));
            letterColor2.setValue(getHTMLColorString(theme.getUColor()).toLowerCase().equals("#000000") ? "Black" : "White");
        }

        if (theme.getGColor() != null) {
            structuralElement3.setValue("G");
            colorPicker3.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getGColor())));
            letterColor3.setValue(getHTMLColorString(theme.getGColor()).toLowerCase().equals("#000000") ? "Black" : "White");
        }

        if (theme.getCColor() != null) {
            structuralElement4.setValue("C");
            colorPicker4.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getCColor())));
            letterColor4.setValue(getHTMLColorString(theme.getCColor()).toLowerCase().equals("#000000") ? "Black" : "White");
        }

        if (theme.getXColor() != null) {
            structuralElement5.setValue("X");
            colorPicker5.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getXColor())));
            letterColor5.setValue(getHTMLColorString(theme.getXColor()).toLowerCase().equals("#000000") ? "Black" : "White");
        }

        if (theme.getSecondaryColor() != null) {
            structuralElement6.setValue("2D");
            colorPicker6.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getSecondaryColor())));
        }

        if (theme.getTertiaryColor() != null) {
            structuralElement7.setValue("3D");
            colorPicker7.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getTertiaryColor())));
        }

        if (theme.getTertiaryOpacity() != null)
            _3dOpacity.setValue(theme.getTertiaryOpacity());

        if (theme.getResidueCharOpacity() != null)
            residueCharOpacity.setValue(theme.getResidueCharOpacity());

        if (theme.getPhosphoDiesterWidth() != null)
            phosphoDiesterWidth.setValue(""+theme.getPhosphoDiesterWidth());

        if (theme.getSecondaryInteractionWidth() != null)
            secondaryInteractionWidth.setValue(""+theme.getSecondaryInteractionWidth());

        if (theme.getSecondaryInteractionShift() != null)
            secondaryInteractionShift.setValue(theme.getSecondaryInteractionShift());

        if (theme.getTertiaryInteractionWidth() != null)
            tertiaryInteractionWidth.setValue(""+theme.getTertiaryInteractionWidth());

        if (theme.getHaloWidth() != null)
            haloWidth.setValue(theme.getHaloWidth());

        if (theme.getResidueBorder() != null)
            residueBorder.setValue(""+theme.getResidueBorder());

        if (theme.getTertiaryInteractionStyle() != null)
            tertiaryInteractionStyle.setValue(theme.getTertiaryInteractionStyle());

        if (theme.getFontName() != null)
            fontNames.setValue(theme.getFontName());

        if (theme.getDeltaXRes() != null)
            deltaXRes.getValueFactory().setValue(theme.getDeltaXRes());

        if (theme.getDeltaYRes() != null)
            deltaYRes.getValueFactory().setValue(theme.getDeltaYRes());

        if (theme.getDeltaFontSize() != null)
            deltaFontSize.getValueFactory().setValue(theme.getDeltaFontSize());
    }


    public void loadTheme(SecondaryStructureElement element) {
            structuralElement1.setValue("A");
            colorPicker1.setValue(javafx.scene.paint.Color.web(getHTMLColorString(element.getAColor())));
            letterColor1.setValue(getHTMLColorString(element.getAColor()).toLowerCase().equals("#000000") ? "Black" : "White");

            structuralElement2.setValue("U");
            colorPicker2.setValue(javafx.scene.paint.Color.web(getHTMLColorString(element.getUColor())));
            letterColor2.setValue(getHTMLColorString(element.getUColor()).toLowerCase().equals("#000000") ? "Black" : "White");

            structuralElement3.setValue("G");
            colorPicker3.setValue(javafx.scene.paint.Color.web(getHTMLColorString(element.getGColor())));
            letterColor3.setValue(getHTMLColorString(element.getGColor()).toLowerCase().equals("#000000") ? "Black" : "White");

            structuralElement4.setValue("C");
            colorPicker4.setValue(javafx.scene.paint.Color.web(getHTMLColorString(element.getCColor())));
            letterColor4.setValue(getHTMLColorString(element.getCColor()).toLowerCase().equals("#000000") ? "Black" : "White");

            structuralElement5.setValue("X");
            colorPicker5.setValue(javafx.scene.paint.Color.web(getHTMLColorString(element.getXColor())));
            letterColor5.setValue(getHTMLColorString(element.getXColor()).toLowerCase().equals("#000000") ? "Black" : "White");

            structuralElement6.setValue("2D");
            colorPicker6.setValue(javafx.scene.paint.Color.web(getHTMLColorString(element.getSecondaryColor())));

            structuralElement7.setValue("3D");
            colorPicker7.setValue(javafx.scene.paint.Color.web(getHTMLColorString(element.getTertiaryColor())));

            _3dOpacity.setValue(element.getTertiaryOpacity());

            residueCharOpacity.setValue(element.getResidueCharOpacity());

            phosphoDiesterWidth.setValue(""+element.getPhosphodiesterWidth());

            secondaryInteractionWidth.setValue(""+element.getSecondaryInteractionWidth());

            secondaryInteractionShift.setValue(element.getSecondaryInteractionShift());

            tertiaryInteractionWidth.setValue(""+element.getTertiaryInteractionWidth());

            haloWidth.setValue(element.getHaloWidth());

            residueBorder.setValue(""+element.getResidueBorder());

            tertiaryInteractionStyle.setValue(element.getTertiaryInteractionStyle());

            fontNames.setValue(element.getFontName());

            deltaXRes.getValueFactory().setValue(element.getDeltaXRes());

            deltaYRes.getValueFactory().setValue(element.getDeltaYRes());

            deltaFontSize.getValueFactory().setValue(element.getDeltaFontSize());
    }

    public Map<String,String> getTheme() {
        Map<String, String> theme = new HashMap<String, String>();
        /*theme.put(ThemeParameter.AColor.toString(), this.getAColor());
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
        theme.put(ThemeParameter.ResidueCharOpacity.toString(), ""+(int)(residueCharOpacity.getValue()*255.0/100.0));
        theme.put(ThemeParameter.HaloWidth.toString(), ""+(int)haloWidth.getValue());
        theme.put(ThemeParameter.PhosphodiesterWidth.toString(), phosphoDiesterWidth.getValue());
        theme.put(ThemeParameter.SecondaryInteractionWidth.toString(), secondaryInteractionWidth.getValue());
        theme.put(ThemeParameter.SecondaryInteractionShift.toString(), secondaryInteractionShift.getValue());
        theme.put(ThemeParameter.TertiaryInteractionWidth.toString(), tertiaryInteractionWidth.getValue());
        theme.put(ThemeParameter.ResidueBorder.toString(), residueBorder.getValue());
        theme.put(ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue());
        theme.put(ThemeParameter.FontName.toString(), fontNames.getValue());
        theme.put(ThemeParameter.DeltaXRes.toString(), deltaXRes.getValue());
        theme.put(ThemeParameter.DeltaYRes.toString(), deltaYRes.getValue());
        theme.put(ThemeParameter.DeltaFontSize.toString(), deltaFontSize.getValue());*/
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
            return new Image(RnartistConfig.getWebsite()+"/captures/" +this.picture);
        }
    }

    private class LoadThemesFromWebsite extends Task<Exception> {

        @Override
        protected Exception call() throws Exception {
            themesList.clear();
            try {
                for (Map.Entry<String,String> t:Backend.getAllThemes().entrySet()) {
                    themesList.add(new ThemeFromWebsite(t.getKey(), t.getValue()));
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


