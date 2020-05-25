package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.io.Backend;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
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
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

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
    private ComboBox<String> fontNames, residuesWidth, phosphoDiesterWidth,
            secondaryInteractionWidth,
            tertiaryInteractionWidth, tertiaryInteractionStyle, displayLWSymbols;
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
        scene.getStylesheets().add(getClass().getClassLoader().getResource("io/github/fjossinet/rnartist/gui/css/toolbox.css").toExternalForm());

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(450);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(0);
        this.stage.setY(0);
    }

    private void createThemePanel(TabPane root) {

        VBox parent = new VBox();

        //++++++ form to save the theme

        GridPane savedThemesForm = new GridPane();
        savedThemesForm.setHgap(5);
        savedThemesForm.setVgap(5);
        savedThemesForm.setPadding(new Insets(5, 10, 5, 10));
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        savedThemesForm.getColumnConstraints().addAll(cc,new ColumnConstraints(), new ColumnConstraints());

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
                    //we mute the listeners to avoid to apply the saved theme automatically
                    try {
                        setMuted(true);
                        loadTheme(mediator.getEmbeddedDB().getTheme(newvalue.getValue()));
                        setMuted(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Button deleteSavedTheme = new Button("Delete");
        deleteSavedTheme.setDisable(true);
        this.savedThemesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pair<String, NitriteId>>() {
            @Override
            public void changed(ObservableValue<? extends Pair<String, NitriteId>> observableValue, Pair<String, NitriteId> stringNitriteIdPair, Pair<String, NitriteId> newValue) {
                if (newValue != null)
                    deleteSavedTheme.setDisable(false);
            }
        });
        deleteSavedTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Are you Sure to Delete your Theme?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    Pair<String, NitriteId> toDelete = savedThemesComboBox.getValue();
                    if (toDelete != null) {
                        savedThemes.remove(toDelete);
                        if (!savedThemes.isEmpty())
                            savedThemesComboBox.setValue(savedThemes.get(0));
                        mediator.getEmbeddedDB().deleteTheme(toDelete.getValue());
                    }
                }
            }
        });

        Button shareSavedTheme = new Button("Share");
        shareSavedTheme.setDisable(true);
        this.savedThemesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pair<String, NitriteId>>() {
            @Override
            public void changed(ObservableValue<? extends Pair<String, NitriteId>> observableValue, Pair<String, NitriteId> stringNitriteIdPair, Pair<String, NitriteId> newValue) {
                /*if (newValue != null)
                    shareSavedTheme.setDisable(false);*/
            }
        });
        shareSavedTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //Backend.submitTheme(mediator, themeNameField.getText().trim(), mediator.getToolbox().getTheme());
            }
        });

        Button saveTheme = new Button("Save");
        saveTheme.setDisable(true);
        this.savedThemesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pair<String, NitriteId>>() {
            @Override
            public void changed(ObservableValue<? extends Pair<String, NitriteId>> observableValue, Pair<String, NitriteId> stringNitriteIdPair, Pair<String, NitriteId> newValue) {
                if (newValue != null)
                    saveTheme.setDisable(false);
            }
        });

        saveTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Are you Sure to Update your Theme?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    Pair<String, NitriteId> toUpdate = savedThemesComboBox.getValue();
                    if (toUpdate != null) {
                        mediator.getEmbeddedDB().updateTheme(toUpdate.getValue(), getCurrentTheme().getParams());
                    }
                }
            }
        });

        Button saveAsTheme = new Button("Save As");

        saveAsTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Theme Saving");
                dialog.setHeaderText("Choose a Name for your Theme");

                Optional<String> response = dialog.showAndWait();

                response.ifPresent(name -> {
                    NitriteId id = mediator.getEmbeddedDB().addTheme(name, getCurrentTheme());
                    if (id != null) {
                        Pair theme = new Pair<>(name, id);
                        savedThemes.add(theme);
                        savedThemesComboBox.setValue(theme);
                    }
                });
            }
        });

        Button loadSavedTheme = new Button("Load");
        loadSavedTheme.setDisable(true);
        this.savedThemesComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pair<String, NitriteId>>() {
            @Override
            public void changed(ObservableValue<? extends Pair<String, NitriteId>> observableValue, Pair<String, NitriteId> stringNitriteIdPair, Pair<String, NitriteId> newValue) {
                if (newValue != null)
                    loadSavedTheme.setDisable(false);
            }
        });
        loadSavedTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Pair<String, NitriteId> value = savedThemesComboBox.getValue();
                //we mute the listeners to avoid to apply the saved theme automatically
                try {
                    setMuted(true);
                    loadTheme(mediator.getEmbeddedDB().getTheme(value.getValue()));
                    setMuted(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Label l = new Label("Saved Themes");
        l.setStyle("-fx-font-size: 20");
        GridPane.setConstraints(l, 0,0,5,1);
        savedThemesForm.getChildren().add(l);

        GridPane.setConstraints(savedThemesComboBox, 0,1,5,1);
        savedThemesForm.getChildren().add(savedThemesComboBox);

        GridPane.setConstraints(shareSavedTheme, 0,2,1,1);
        GridPane.setHalignment(shareSavedTheme,HPos.RIGHT);
        savedThemesForm.getChildren().add(shareSavedTheme);

        GridPane.setConstraints(saveTheme, 1,2,1,1);
        GridPane.setHalignment(saveTheme,HPos.RIGHT);
        savedThemesForm.getChildren().add(saveTheme);

        GridPane.setConstraints(saveAsTheme, 2,2,1,1);
        GridPane.setHalignment(saveAsTheme,HPos.RIGHT);
        savedThemesForm.getChildren().add(saveAsTheme);

        GridPane.setConstraints(deleteSavedTheme, 3,2,1,1);
        GridPane.setHalignment(deleteSavedTheme,HPos.RIGHT);
        savedThemesForm.getChildren().add(deleteSavedTheme);

        GridPane.setConstraints(loadSavedTheme, 4,2,1,1);
        GridPane.setHalignment(loadSavedTheme,HPos.RIGHT);
        savedThemesForm.getChildren().add(loadSavedTheme);

        parent.getChildren().add(savedThemesForm);

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
                        addThemeConfiguratorListener(mediator.getCurrent2DDrawing());
                        /*setMuted(true);
                        loadTheme(mediator.getCurrent2DDrawing().getTheme());
                        setMuted(false);*/
                        if (RnartistConfig.getFitDisplayOnSelection()) { //fit first since fit will center too
                            mediator.canvas2D.fitDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                        } else if (RnartistConfig.getCenterDisplayOnSelection()) {
                            mediator.canvas2D.centerDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                        }
                    } else if (newvalue.toString().equals("All Selected Elements")) {
                        for (Object o : structureElementsSelectedComboBox.getItems().subList(2, structureElementsSelectedComboBox.getItems().size())) {
                            addThemeConfiguratorListener((SecondaryStructureElement) o);
                        }
                    } else {
                        addThemeConfiguratorListener((SecondaryStructureElement) newvalue);
                        /*setMuted(true);
                        loadTheme((SecondaryStructureElement)newvalue);
                        setMuted(false);*/
                        if (RnartistConfig.getFitDisplayOnSelection()) { //fit first since fit will center too
                            mediator.canvas2D.fitDisplayOn(((SecondaryStructureElement) newvalue).getBounds2D());
                        } else if (RnartistConfig.getCenterDisplayOnSelection()) {
                            mediator.canvas2D.centerDisplayOn(((SecondaryStructureElement) newvalue).getBounds2D());
                        }
                    }
                    mediator.canvas2D.repaint();
                }
            }
        });

        GridPane targetForm = new GridPane();
        targetForm.setHgap(5);
        targetForm.setVgap(5);
        targetForm.setPadding(new Insets(0, 10, 5, 10));
        targetForm.setMaxWidth(Double.MAX_VALUE);
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        targetForm.getColumnConstraints().addAll(cc);

        l = new Label("Target");
        l.setStyle("-fx-font-size: 20");
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(l, 0,0,2,1);
        GridPane.setHalignment(l,HPos.LEFT);
        targetForm.getChildren().add(l);

        GridPane.setConstraints(this.structureElementsSelectedComboBox, 0,1,2,1);
        targetForm.getChildren().add(this.structureElementsSelectedComboBox);

        Button clearTheme = new Button("Clear Theme for Target");
        clearTheme.setMinWidth(Control.USE_PREF_SIZE);
        clearTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Object selectedStructureElement = structureElementsSelectedComboBox.getValue();
                if (selectedStructureElement.toString().equals("Full 2D")) {
                    mediator.getCurrent2DDrawing().clearThemes();
                } else if (selectedStructureElement.toString().equals("All Selected Elements")) {
                    for (Object _o: mediator.getStructureElementsSelected().subList(2,mediator.getStructureElementsSelected().size())) {
                        ((SecondaryStructureElement)_o).clearThemes();
                        //mediator.getExplorer().clearTheme((SecondaryStructureElement)_o);
                    }
                }
                else if (SecondaryStructureElement.class.isInstance(selectedStructureElement)) {
                    ((SecondaryStructureElement)selectedStructureElement).clearThemes();
                    //mediator.getExplorer().clearTheme((SecondaryStructureElement)selectedStructureElement);
                }
                mediator.canvas2D.repaint();
            }
        });

        GridPane.setConstraints(clearTheme, 0,2,1,1);
        clearTheme.setMaxWidth(Double.MAX_VALUE);
        targetForm.getChildren().add(clearTheme);

        Button applyTheme = new Button("Apply Theme to Target");
        applyTheme.setMinWidth(Control.USE_PREF_SIZE);
        applyTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Object selectedStructureElement = structureElementsSelectedComboBox.getValue();
                if (selectedStructureElement.toString().equals("Full 2D")) {
                    mediator.getCurrent2DDrawing().setTheme(getCurrentTheme());
                    for (Map.Entry<String,String> e:getCurrentTheme().getParams().entrySet())
                        mediator.getExplorer().setThemeParameter(mediator.getCurrent2DDrawing(), e.getKey(), e.getValue());
                } else if (selectedStructureElement.toString().equals("All Selected Elements")) {
                    for (Object _o: mediator.getStructureElementsSelected().subList(2,mediator.getStructureElementsSelected().size())) {
                        ((SecondaryStructureElement)_o).setTheme(getCurrentTheme());
                        for (Map.Entry<String,String> e:((SecondaryStructureElement)_o).getTheme().getParams().entrySet())
                            mediator.getExplorer().setThemeParameter((SecondaryStructureElement)_o, e.getKey(), e.getValue());
                    }
                }
                else if (SecondaryStructureElement.class.isInstance(selectedStructureElement)) {
                    ((SecondaryStructureElement)selectedStructureElement).setTheme(getCurrentTheme());
                    for (Map.Entry<String,String> e:((SecondaryStructureElement)selectedStructureElement).getTheme().getParams().entrySet())
                        mediator.getExplorer().setThemeParameter((SecondaryStructureElement)selectedStructureElement, e.getKey(), e.getValue());
                }
                mediator.canvas2D.repaint();
            }
        });

        GridPane.setConstraints(applyTheme, 1,2,1,1);
        applyTheme.setMaxWidth(Double.MAX_VALUE);
        targetForm.getChildren().add(applyTheme);

        parent.getChildren().add(targetForm);

        VBox currentThemeVBox =  new VBox();
        currentThemeVBox.setFillWidth(true);
        currentThemeVBox.setPadding(new Insets(10, 10, 10, 10));
        currentThemeVBox.setSpacing(10);

        //++++++ pane for the fonts
        Label title = new Label("Font");
        title.setStyle("-fx-font-size: 20");
        currentThemeVBox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane fontsPane = new GridPane();
        fontsPane.setHgap(10);
        fontsPane.setVgap(5);
        fontsPane.setPadding(new Insets(0, 0, 5, 0));
        fontsPane.setMaxWidth(Double.MAX_VALUE);

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        fontsPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(),new ColumnConstraints(),new ColumnConstraints(),new ColumnConstraints(), cc, new ColumnConstraints());

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

        Button applyFontName = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyFontName, 6,0,1,1);
        fontsPane.getChildren().add(applyFontName);

        applyFontName.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.FontName, fontNames.getValue());
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearFontName = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearFontName, 7,0,1,1);
        fontsPane.getChildren().add(clearFontName);

        clearFontName.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        deltaXRes = new Spinner<Integer>();
        deltaXRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultThemeParams.get(ThemeParameter.DeltaXRes.toString()))));
        deltaXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if (mediator.getCurrent2DDrawing() != null) {
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
                if (mediator.getCurrent2DDrawing() != null) {
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
                if (mediator.getCurrent2DDrawing() != null) {
                    fireThemeChange(ThemeParameter.DeltaFontSize, deltaFontSize.getValue().intValue());
                    mediator.getCanvas2D().repaint();
                }
            }
        });

        l = new Label("x");
        GridPane.setConstraints(l, 0,1,1,1);
        GridPane.setHalignment(l, HPos.LEFT);
        fontsPane.getChildren().add(l);
        GridPane.setConstraints(deltaXRes, 1,1,1,1);
        GridPane.setHalignment(deltaXRes, HPos.LEFT);
        fontsPane.getChildren().add(deltaXRes);

        l = new Label("y");
        GridPane.setConstraints(l, 2,1,1,1);
        GridPane.setHalignment(l, HPos.LEFT);
        fontsPane.getChildren().add(l);
        GridPane.setConstraints(deltaYRes, 3,1,1,1);
        GridPane.setHalignment(deltaYRes, HPos.LEFT);
        fontsPane.getChildren().add(deltaYRes);

        l = new Label("s");
        GridPane.setConstraints(l, 4,1,1,1);
        fontsPane.getChildren().add(l);
        GridPane.setHalignment(l, HPos.LEFT);
        GridPane.setConstraints(deltaFontSize, 5,1,1,1);
        fontsPane.getChildren().add(deltaFontSize);
        GridPane.setHalignment(deltaFontSize, HPos.LEFT);

        Button applyDeltas = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyDeltas, 6,1,1,1);
        fontsPane.getChildren().add(applyDeltas);

        applyDeltas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.DeltaXRes, deltaXRes.getValue());
                fireThemeChange(ThemeParameter.DeltaYRes, deltaYRes.getValue());
                fireThemeChange(ThemeParameter.DeltaFontSize, deltaFontSize.getValue().intValue());
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearDeltas = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearDeltas, 7,1,1,1);
        fontsPane.getChildren().add(clearDeltas);

        clearDeltas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        currentThemeVBox.getChildren().add(fontsPane);

        GridPane fontsPane2 = new GridPane();
        fontsPane2.setHgap(10);
        fontsPane2.setVgap(10);
        fontsPane2.setPadding(new Insets(0, 0, 10, 0));
        fontsPane2.setMaxWidth(Double.MAX_VALUE);

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        fontsPane2.getColumnConstraints().addAll(cc);

        l = new Label("Residue Character Opacity (%)");
        GridPane.setConstraints(l, 0,0,3,1);
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHalignment(l,HPos.LEFT);
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
        GridPane.setConstraints(this.residueCharOpacity, 0,1,1,1);
        this.residueCharOpacity.setMaxWidth(Double.MAX_VALUE);
        fontsPane2.getChildren().add(this.residueCharOpacity);

        Button applyCharOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyCharOpacity, 1,1,1,1);
        GridPane.setValignment(applyCharOpacity, VPos.TOP);
        fontsPane2.getChildren().add(applyCharOpacity);

        applyCharOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.ResidueCharOpacity,(int) ((double)(residueCharOpacity.getValue()) / 100.0 * 255.0));
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearCharOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearCharOpacity, 2,1,1,1);
        GridPane.setValignment(clearCharOpacity, VPos.TOP);
        fontsPane2.getChildren().add(clearCharOpacity);

        clearCharOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        l = new Label("Ticks Character Opacity (%)");
        GridPane.setConstraints(l, 0,2,3,1);
        GridPane.setHalignment(l,HPos.LEFT);
        l.setMaxWidth(Double.MAX_VALUE);
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
        GridPane.setConstraints(slider2, 0,3,1,1);
        slider2.setMaxWidth(Double.MAX_VALUE);
        fontsPane2.getChildren().add(slider2);

        Button applyTicksOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyTicksOpacity, 1,3,1,1);
        GridPane.setValignment(applyTicksOpacity, VPos.TOP);
        fontsPane2.getChildren().add(applyTicksOpacity);

        applyTicksOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //fireThemeChange(ThemeParameter.ResidueCharOpacity,(int) ((double)(residueCharOpacity.getValue()) / 100.0 * 255.0));
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearTicksOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearTicksOpacity, 2,3,1,1);
        GridPane.setValignment(clearTicksOpacity, VPos.TOP);
        fontsPane2.getChildren().add(clearTicksOpacity);

        clearTicksOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        currentThemeVBox.getChildren().add(fontsPane2);

        //++++++ pane for the Colors
        title = new Label("Colors");
        title.setStyle("-fx-font-size: 20");
        currentThemeVBox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane colorsPane = new GridPane();
        colorsPane.setHgap(10);
        colorsPane.setVgap(5);
        colorsPane.setPadding(new Insets(0, 0, 10, 0));
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        colorsPane.getColumnConstraints().addAll(new ColumnConstraints(),new ColumnConstraints(),cc);

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
        colorSchemeChoices.setValue("Choose a Color Scheme");
        colorSchemeChoices.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(colorSchemeChoices, 0,0,5,1);
        GridPane.setHalignment(colorSchemeChoices, HPos.LEFT);
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
        colorPicker1.setMinWidth(Control.USE_PREF_SIZE);

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

        l = new Label("Fill");
        GridPane.setConstraints(l, 1,1,1,1);
        GridPane.setHalignment(l,HPos.CENTER);
        colorsPane.getChildren().add(l);
        l = new Label("Character");
        l.setMinWidth(Control.USE_PREF_SIZE);
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
        letterColor1.setMinWidth(Control.USE_PREF_SIZE);
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

        Button applyStructuralElement1 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyStructuralElement1, 3,2,1,1);
        colorsPane.getChildren().add(applyStructuralElement1);

        applyStructuralElement1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (structuralElement1.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker1.getValue())) ; fireThemeChange(ThemeParameter.AChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker1.getValue())) ; fireThemeChange(ThemeParameter.UChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker1.getValue())) ; fireThemeChange(ThemeParameter.GChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker1.getValue())) ; fireThemeChange(ThemeParameter.CChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker1.getValue())) ; fireThemeChange(ThemeParameter.XChar, letterColor1.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "2D": fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker1.getValue())); break;
                    case "3D": fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker1.getValue())); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearStructuralElement1 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearStructuralElement1, 4,2,1,1);
        colorsPane.getChildren().add(clearStructuralElement1);

        clearStructuralElement1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
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

        Button applyStructuralElement2 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyStructuralElement2, 3,3,1,1);
        colorsPane.getChildren().add(applyStructuralElement2);

        applyStructuralElement2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (structuralElement2.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker2.getValue())) ; fireThemeChange(ThemeParameter.AChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker2.getValue())) ; fireThemeChange(ThemeParameter.UChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker2.getValue())) ; fireThemeChange(ThemeParameter.GChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker2.getValue())) ; fireThemeChange(ThemeParameter.CChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker2.getValue())) ; fireThemeChange(ThemeParameter.XChar, letterColor2.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "2D": fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker2.getValue())); break;
                    case "3D": fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker2.getValue())); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearStructuralElement2 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearStructuralElement2, 4,3,1,1);
        colorsPane.getChildren().add(clearStructuralElement2);

        clearStructuralElement2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
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

        Button applyStructuralElement3 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyStructuralElement3, 3,4,1,1);
        colorsPane.getChildren().add(applyStructuralElement3);

        applyStructuralElement3.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (structuralElement3.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker3.getValue())) ; fireThemeChange(ThemeParameter.AChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker3.getValue())) ; fireThemeChange(ThemeParameter.UChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker3.getValue())) ; fireThemeChange(ThemeParameter.GChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker3.getValue())) ; fireThemeChange(ThemeParameter.CChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker3.getValue())) ; fireThemeChange(ThemeParameter.XChar, letterColor3.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "2D": fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker3.getValue())); break;
                    case "3D": fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker3.getValue())); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearStructuralElement3 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearStructuralElement3, 4,4,1,1);
        colorsPane.getChildren().add(clearStructuralElement3);

        clearStructuralElement3.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
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

        Button applyStructuralElement4 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyStructuralElement4, 3,5,1,1);
        colorsPane.getChildren().add(applyStructuralElement4);

        applyStructuralElement4.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (structuralElement4.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker4.getValue())) ; fireThemeChange(ThemeParameter.AChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker4.getValue())) ; fireThemeChange(ThemeParameter.UChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker4.getValue())) ; fireThemeChange(ThemeParameter.GChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker4.getValue())) ; fireThemeChange(ThemeParameter.CChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker4.getValue())) ; fireThemeChange(ThemeParameter.XChar, letterColor4.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "2D": fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker4.getValue())); break;
                    case "3D": fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker4.getValue())); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearStructuralElement4 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearStructuralElement4, 4,5,1,1);
        colorsPane.getChildren().add(clearStructuralElement4);

        clearStructuralElement4.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
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

        Button applyStructuralElement5 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyStructuralElement5, 3,6,1,1);
        colorsPane.getChildren().add(applyStructuralElement5);

        applyStructuralElement5.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (structuralElement5.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker5.getValue())) ; fireThemeChange(ThemeParameter.AChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker5.getValue())) ; fireThemeChange(ThemeParameter.UChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker5.getValue())) ; fireThemeChange(ThemeParameter.GChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker5.getValue())) ; fireThemeChange(ThemeParameter.CChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker5.getValue())) ; fireThemeChange(ThemeParameter.XChar, letterColor5.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "2D": fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker5.getValue())); break;
                    case "3D": fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker5.getValue())); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearStructuralElement5 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearStructuralElement5, 4,6,1,1);
        colorsPane.getChildren().add(clearStructuralElement5);

        clearStructuralElement5.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
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

        Button applyStructuralElement6 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyStructuralElement6, 3,7,1,1);
        colorsPane.getChildren().add(applyStructuralElement6);

        applyStructuralElement6.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (structuralElement6.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker6.getValue())) ; fireThemeChange(ThemeParameter.AChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker6.getValue())) ; fireThemeChange(ThemeParameter.UChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker6.getValue())) ; fireThemeChange(ThemeParameter.GChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker6.getValue())) ; fireThemeChange(ThemeParameter.CChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker6.getValue())) ; fireThemeChange(ThemeParameter.XChar, letterColor6.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "2D": fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker6.getValue())); break;
                    case "3D": fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker6.getValue())); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearStructuralElement6 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearStructuralElement6, 4,7,1,1);
        colorsPane.getChildren().add(clearStructuralElement6);

        clearStructuralElement6.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
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

        Button applyStructuralElement7 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyStructuralElement7, 3,8,1,1);
        colorsPane.getChildren().add(applyStructuralElement7);

        applyStructuralElement7.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (structuralElement7.getValue()) {
                    case "A" : fireThemeChange(ThemeParameter.AColor, javaFXToAwt(colorPicker7.getValue())) ; fireThemeChange(ThemeParameter.AChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "U" : fireThemeChange(ThemeParameter.UColor, javaFXToAwt(colorPicker7.getValue())) ; fireThemeChange(ThemeParameter.UChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "G" : fireThemeChange(ThemeParameter.GColor, javaFXToAwt(colorPicker7.getValue())) ; fireThemeChange(ThemeParameter.GChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "C" : fireThemeChange(ThemeParameter.CColor, javaFXToAwt(colorPicker7.getValue())) ; fireThemeChange(ThemeParameter.CChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "X" : fireThemeChange(ThemeParameter.XColor, javaFXToAwt(colorPicker7.getValue())) ; fireThemeChange(ThemeParameter.XChar, letterColor7.getValue() == "White" ? Color.WHITE : Color.BLACK); break;
                    case "2D": fireThemeChange(ThemeParameter.SecondaryColor, javaFXToAwt(colorPicker7.getValue())); break;
                    case "3D": fireThemeChange(ThemeParameter.TertiaryColor, javaFXToAwt(colorPicker7.getValue())); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearStructuralElement7 = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearStructuralElement7, 4,8,1,1);
        colorsPane.getChildren().add(clearStructuralElement7);

        clearStructuralElement7.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        currentThemeVBox.getChildren().add(colorsPane);

        //++++++ pane for the Lines
        title = new Label("Lines");
        title.setStyle("-fx-font-size: 20");
        currentThemeVBox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane linesPane = new GridPane();
        linesPane.setHgap(10);
        linesPane.setVgap(5);
        linesPane.setPadding(new Insets(0, 0, 10, 0));

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        linesPane.getColumnConstraints().addAll(new ColumnConstraints(),cc);

        int row = 0;

        displayLWSymbols = new ComboBox<String>();
        displayLWSymbols.getItems().addAll("yes","no");
        displayLWSymbols.setMaxWidth(Double.MAX_VALUE);
        displayLWSymbols.setValue("yes");
        displayLWSymbols.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                fireThemeChange(ThemeParameter.DisplayLWSymbols, displayLWSymbols.getValue());
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setConstraints(displayLWSymbols, 0,row,1,1);
        linesPane.getChildren().add(displayLWSymbols);
        GridPane.setHalignment(displayLWSymbols,HPos.CENTER);

        l = new Label("Display Leontis-Westhof Symbols");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1, row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        Button applyDisplayLWSymbols = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyDisplayLWSymbols, 3, row,1,1);
        linesPane.getChildren().add(applyDisplayLWSymbols);

        applyDisplayLWSymbols.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.DisplayLWSymbols, displayLWSymbols.getValue());
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearDisplayLWSymbols = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearDisplayLWSymbols, 4,row,1,1);
        linesPane.getChildren().add(clearDisplayLWSymbols);

        clearDisplayLWSymbols.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

        residuesWidth = new ComboBox<>();
        residuesWidth.getItems().addAll("0", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0");
        residuesWidth.setValue(RnartistConfig.defaultThemeParams.get(ThemeParameter.ResidueBorder.toString()));
        residuesWidth.setCellFactory(new ShapeCellFactory());
        residuesWidth.setButtonCell(new ShapeCell());
        residuesWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireThemeChange(ThemeParameter.ResidueBorder,Double.parseDouble(residuesWidth.getValue()));
                mediator.getCanvas2D().repaint();
            }
        });
        residuesWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(residuesWidth, 0,row,1,1);
        linesPane.getChildren().add(residuesWidth);
        GridPane.setHalignment(residuesWidth,HPos.CENTER);

        l = new Label("Residues Line Width (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        Button applyResidueLineWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyResidueLineWidth, 3, row,1,1);
        linesPane.getChildren().add(applyResidueLineWidth);

        applyResidueLineWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.ResidueBorder,Double.parseDouble(residuesWidth.getValue()));
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearResidueLineWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearResidueLineWidth, 4,row,1,1);
        linesPane.getChildren().add(clearResidueLineWidth);

        clearResidueLineWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

        phosphoDiesterWidth = new ComboBox<>();
        phosphoDiesterWidth.getItems().addAll("0", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0");
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

        GridPane.setConstraints(phosphoDiesterWidth, 0,row,1,1);
        linesPane.getChildren().add(phosphoDiesterWidth);
        GridPane.setHalignment(phosphoDiesterWidth,HPos.CENTER);

        l = new Label("Phosphodiester Line Width (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        Button applyPhosphodiesterWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyPhosphodiesterWidth, 3, row,1,1);
        linesPane.getChildren().add(applyPhosphodiesterWidth);

        applyPhosphodiesterWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.PhosphodiesterWidth, Double.parseDouble(phosphoDiesterWidth.getValue()));
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearPhosphodiesterWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearPhosphodiesterWidth, 4,row,1,1);
        linesPane.getChildren().add(clearPhosphodiesterWidth);

        clearPhosphodiesterWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

        secondaryInteractionWidth = new ComboBox<>();
        secondaryInteractionWidth.getItems().addAll("0", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "3.0", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0");
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

        GridPane.setConstraints(secondaryInteractionWidth, 0, row,1,1);
        linesPane.getChildren().add(secondaryInteractionWidth);
        GridPane.setHalignment(secondaryInteractionWidth,HPos.CENTER);

        l = new Label("Secondaries Line Width (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        Button applySecondariesWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applySecondariesWidth, 3, row,1,1);
        linesPane.getChildren().add(applySecondariesWidth);

        applySecondariesWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.SecondaryInteractionWidth, Double.parseDouble(secondaryInteractionWidth.getValue()));
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearSecondariesWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearSecondariesWidth, 4,row,1,1);
        linesPane.getChildren().add(clearSecondariesWidth);

        clearSecondariesWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

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

        GridPane.setConstraints(tertiaryInteractionWidth, 0,row,1,1);
        linesPane.getChildren().add(tertiaryInteractionWidth);
        GridPane.setHalignment(tertiaryInteractionWidth,HPos.CENTER);

        l = new Label("Tertiaries Line Width (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1,row,2,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        Button applyTertiariesWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyTertiariesWidth, 3, row,1,1);
        linesPane.getChildren().add(applyTertiariesWidth);

        applyTertiariesWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.TertiaryInteractionWidth, tertiaryInteractionWidth.getValue());
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearTertiariesWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearTertiariesWidth, 4,row,1,1);
        linesPane.getChildren().add(clearTertiariesWidth);

        clearTertiariesWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

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

        GridPane.setConstraints(tertiaryInteractionStyle, 0,row,1,1);
        linesPane.getChildren().add(tertiaryInteractionStyle);
        GridPane.setHalignment(tertiaryInteractionStyle,HPos.CENTER);

        l = new Label("Tertiaries Line Style");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1, row,1,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        Button applyTertiariesStyle = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyTertiariesStyle, 3, row,1,1);
        linesPane.getChildren().add(applyTertiariesStyle);

        applyTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.TertiaryInteractionStyle, tertiaryInteractionStyle.getValue());
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearTertiariesStyle = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearTertiariesStyle, 4,row,1,1);
        linesPane.getChildren().add(clearTertiariesStyle);

        clearTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

        l = new Label("Secondaries Line Shift (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        l.setPadding(new Insets(10,0,0,0));
        GridPane.setConstraints(l, 0,row,5,1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        row++;

        secondaryInteractionShift = new Slider(0, 10, Double.parseDouble(RnartistConfig.defaultThemeParams.get(ThemeParameter.SecondaryInteractionShift.toString())));
        secondaryInteractionShift.setShowTickLabels(true);
        secondaryInteractionShift.setShowTickMarks(true);
        secondaryInteractionShift.setMajorTickUnit(5);
        secondaryInteractionShift.setMinorTickCount(1);
        secondaryInteractionShift.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.SecondaryInteractionShift, secondaryInteractionShift.getValue());
                mediator.getCanvas2D().repaint();
            }
        });
        secondaryInteractionShift.setShowTickMarks(true);
        secondaryInteractionShift.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(secondaryInteractionShift, 0,row,3,1);
        linesPane.getChildren().add(secondaryInteractionShift);
        GridPane.setHalignment(secondaryInteractionShift,HPos.CENTER);

        Button applySecondariesShift = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applySecondariesShift, 3, row,1,1);
        GridPane.setValignment(applySecondariesShift, VPos.TOP);
        linesPane.getChildren().add(applySecondariesShift);

        applySecondariesShift.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.SecondaryInteractionShift, secondaryInteractionShift.getValue());
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearSecondariesShift = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearSecondariesShift, 4,row,1,1);
        GridPane.setValignment(clearSecondariesShift, VPos.TOP);
        linesPane.getChildren().add(clearSecondariesShift);

        clearSecondariesShift.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

        l = new Label("Tertiaries Opacity (%)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        l.setPadding(new Insets(10,0,0,0));
        GridPane.setConstraints(l, 0, row,5,1);
        GridPane.setValignment(l,VPos.TOP);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        row++;

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

        GridPane.setConstraints(_3dOpacity, 0,row,3,1);
        linesPane.getChildren().add(_3dOpacity);
        GridPane.setHalignment(_3dOpacity,HPos.CENTER);

        Button applyTertiariesOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyTertiariesOpacity, 3, row,1,1);
        GridPane.setValignment(applyTertiariesOpacity, VPos.TOP);
        linesPane.getChildren().add(applyTertiariesOpacity);

        applyTertiariesOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.TertiaryOpacity, (int)((double)(_3dOpacity.getValue())/100.0*255));
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearTertiariesOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearTertiariesOpacity, 4,row,1,1);
        GridPane.setValignment(clearTertiariesOpacity, VPos.TOP);
        linesPane.getChildren().add(clearTertiariesOpacity);

        clearTertiariesOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        row++;

        l = new Label("Tertiaries Halo Size (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        l.setPadding(new Insets(10,0,0,0));
        GridPane.setConstraints(l, 0,row,5,1);
        GridPane.setValignment(l,VPos.TOP);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l,HPos.LEFT);

        row++;

        haloWidth = new Slider(0, 10, Double.parseDouble(RnartistConfig.defaultThemeParams.get(ThemeParameter.HaloWidth.toString())));;
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

        GridPane.setConstraints(haloWidth, 0,row,3,1);
        linesPane.getChildren().add(haloWidth);
        GridPane.setHalignment(haloWidth,HPos.CENTER);

        Button applyHaloWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyHaloWidth, 3, row,1,1);
        GridPane.setValignment(applyHaloWidth, VPos.TOP);
        linesPane.getChildren().add(applyHaloWidth);

        applyHaloWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireThemeChange(ThemeParameter.HaloWidth, haloWidth.getValue());
                mediator.getCanvas2D().repaint();
            }
        });

        Button clearHaloWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearHaloWidth, 4,row,1,1);
        GridPane.setValignment(clearHaloWidth, VPos.TOP);
        linesPane.getChildren().add(clearHaloWidth);

        clearHaloWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().repaint();
            }
        });

        currentThemeVBox.getChildren().add(linesPane);

        ScrollPane sp = new ScrollPane(currentThemeVBox);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        parent.getChildren().add(sp);
        VBox.setVgrow(sp,Priority.ALWAYS);

        Tab theme = new Tab("2D Theme", parent);
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
        Tab themes = new Tab("2D Layout", scrollPane);
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
        Tab settings = new Tab("RNArtist Settings", vbox);
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

    /**
     * Creates a new Theme according to the current state
     * @return
     */
    public Theme getCurrentTheme() {
        Map<String,String> params = new HashMap<>();
        List<ColorPicker> colorPickers = Arrays.asList(colorPicker1,colorPicker2,colorPicker3,colorPicker4,colorPicker5,colorPicker6,colorPicker7);
        List<ChoiceBox<String>> letterColors = Arrays.asList(letterColor1,letterColor2,letterColor3,letterColor4,letterColor5,letterColor6,letterColor7);
        int i = 0;
        for (ChoiceBox<String> box: Arrays.asList(structuralElement1,structuralElement2,structuralElement3,structuralElement4,structuralElement5,structuralElement6,structuralElement7)) {
            switch (box.getValue()) {
                case "A": params.put(ThemeParameter.AColor.toString(), getHTMLColorString(javaFXToAwt(colorPickers.get(i).getValue()))); params.put(ThemeParameter.AChar.toString(), (letterColors.get(i).getValue().equals("Black")) ? getHTMLColorString(Color.BLACK) : getHTMLColorString(Color.WHITE)) ; break;
                case "U": params.put(ThemeParameter.UColor.toString(), getHTMLColorString(javaFXToAwt(colorPickers.get(i).getValue()))); params.put(ThemeParameter.UChar.toString(), (letterColors.get(i).getValue().equals("Black")) ? getHTMLColorString(Color.BLACK) : getHTMLColorString(Color.WHITE)) ; break;
                case "G": params.put(ThemeParameter.GColor.toString(), getHTMLColorString(javaFXToAwt(colorPickers.get(i).getValue()))); params.put(ThemeParameter.GChar.toString(), (letterColors.get(i).getValue().equals("Black")) ? getHTMLColorString(Color.BLACK) : getHTMLColorString(Color.WHITE)) ; break;
                case "C": params.put(ThemeParameter.CColor.toString(), getHTMLColorString(javaFXToAwt(colorPickers.get(i).getValue()))); params.put(ThemeParameter.CChar.toString(), (letterColors.get(i).getValue().equals("Black")) ? getHTMLColorString(Color.BLACK) : getHTMLColorString(Color.WHITE)) ; break;
                case "X": params.put(ThemeParameter.XColor.toString(), getHTMLColorString(javaFXToAwt(colorPickers.get(i).getValue()))); params.put(ThemeParameter.XChar.toString(), (letterColors.get(i).getValue().equals("Black")) ? getHTMLColorString(Color.BLACK) : getHTMLColorString(Color.WHITE)) ; break;
                case "2D": params.put(ThemeParameter.SecondaryColor.toString(), getHTMLColorString(javaFXToAwt(colorPickers.get(i).getValue()))); break;
                case "3D": params.put(ThemeParameter.TertiaryColor.toString(), getHTMLColorString(javaFXToAwt(colorPickers.get(i).getValue()))); break;
            }
            i++;
        }
        params.put(ThemeParameter.TertiaryOpacity.toString(), ""+(int)(_3dOpacity.getValue()/100.0*255.0));
        params.put(ThemeParameter.ResidueCharOpacity.toString(), ""+(int)(residueCharOpacity.getValue()/100.0*255.0));
        params.put(ThemeParameter.PhosphodiesterWidth.toString(), phosphoDiesterWidth.getValue());
        params.put(ThemeParameter.SecondaryInteractionWidth.toString(), secondaryInteractionWidth.getValue());
        params.put(ThemeParameter.SecondaryInteractionShift.toString(), ""+secondaryInteractionShift.getValue());
        params.put(ThemeParameter.TertiaryInteractionWidth.toString(), tertiaryInteractionWidth.getValue());
        params.put(ThemeParameter.HaloWidth.toString(), ""+haloWidth.getValue());
        params.put(ThemeParameter.ResidueBorder.toString(), residuesWidth.getValue());
        params.put(ThemeParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue());
        params.put(ThemeParameter.DeltaXRes.toString(), ""+deltaXRes.getValue());
        params.put(ThemeParameter.DeltaYRes.toString(), ""+deltaYRes.getValue());
        params.put(ThemeParameter.DeltaFontSize.toString(), ""+deltaFontSize.getValue());
        params.put(ThemeParameter.FontName.toString(), fontNames.getValue());
        params.put(ThemeParameter.DisplayLWSymbols.toString(), displayLWSymbols.getValue());
        return new Theme(params);
    }

    public void loadTheme(Theme theme) {
        if (theme.getAColor() != null) {
            structuralElement1.setValue("A");
            colorPicker1.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getAColor())));
        }

        if (theme.getAChar() != null)
            letterColor1.setValue(getHTMLColorString(theme.getAChar()).toLowerCase().equals("#000000") ? "Black" : "White");

        if (theme.getUColor() != null) {
            structuralElement2.setValue("U");
            colorPicker2.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getUColor())));
        }

        if (theme.getUChar() != null)
            letterColor2.setValue(getHTMLColorString(theme.getUChar()).toLowerCase().equals("#000000") ? "Black" : "White");

        if (theme.getGColor() != null) {
            structuralElement3.setValue("G");
            colorPicker3.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getGColor())));
        }

        if (theme.getGChar() != null)
            letterColor3.setValue(getHTMLColorString(theme.getGChar()).toLowerCase().equals("#000000") ? "Black" : "White");

        if (theme.getCColor() != null) {
            structuralElement4.setValue("C");
            colorPicker4.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getCColor())));
        }

        if (theme.getCChar() != null)
            letterColor4.setValue(getHTMLColorString(theme.getCChar()).toLowerCase().equals("#000000") ? "Black" : "White");

        if (theme.getXColor() != null) {
            structuralElement5.setValue("X");
            colorPicker5.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getXColor())));
        }

        if (theme.getXChar() != null)
            letterColor5.setValue(getHTMLColorString(theme.getXChar()).toLowerCase().equals("#000000") ? "Black" : "White");

        if (theme.getSecondaryColor() != null) {
            structuralElement6.setValue("2D");
            colorPicker6.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getSecondaryColor())));
        }

        if (theme.getTertiaryColor() != null) {
            structuralElement7.setValue("3D");
            colorPicker7.setValue(javafx.scene.paint.Color.web(getHTMLColorString(theme.getTertiaryColor())));
        }

        if (theme.getTertiaryOpacity() != null)
            _3dOpacity.setValue(theme.getTertiaryOpacity()/255.0*100.0);

        if (theme.getResidueCharOpacity() != null)
            residueCharOpacity.setValue(theme.getResidueCharOpacity()/255.0*100.0);

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
            residuesWidth.setValue(""+theme.getResidueBorder());

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

        if (theme.getDisplayLWSymbols() != null)
            displayLWSymbols.setValue(theme.getDisplayLWSymbols() ? "yes" : "no" );
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

            residuesWidth.setValue(""+element.getResidueBorder());

            tertiaryInteractionStyle.setValue(element.getTertiaryInteractionStyle());

            fontNames.setValue(element.getFontName());

            deltaXRes.getValueFactory().setValue(element.getDeltaXRes());

            deltaYRes.getValueFactory().setValue(element.getDeltaYRes());

            deltaFontSize.getValueFactory().setValue(element.getDeltaFontSize());

            displayLWSymbols.setValue(element.displayLWSymbols() ? "yes" : "no");
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

    @Override
    public void fireThemeChange(@NotNull ThemeParameter param, @NotNull String newValue) {
        if (!this.getMuted()) {
            super.fireThemeChange(param, newValue);
            Object value = this.structureElementsSelectedComboBox.getValue();
            if (value != null) {
                if (value.toString().equals("All Selected Elements")) {
                    for (Object o : structureElementsSelectedComboBox.getItems().subList(2, structureElementsSelectedComboBox.getItems().size())) {
                        mediator.getExplorer().setThemeParameter((SecondaryStructureElement) o, param.toString(), "" + newValue);
                    }
                } else if (value.toString().equals("Full 2D")) {
                    mediator.getExplorer().setThemeParameter(mediator.getCurrent2DDrawing(), param.toString(), newValue);
                } else {
                    mediator.getExplorer().setThemeParameter((SecondaryStructureElement) value, param.toString(), newValue);
                }
            }
        }
    }

    @Override
    public void fireThemeChange(@NotNull ThemeParameter param, int newValue) {
        if (!this.getMuted()) {
            super.fireThemeChange(param, newValue);
            Object value = this.structureElementsSelectedComboBox.getValue();
            if (value != null) {
                if (value.toString().equals("All Selected Elements")) {
                    for (Object o : structureElementsSelectedComboBox.getItems().subList(2, structureElementsSelectedComboBox.getItems().size())) {
                        mediator.getExplorer().setThemeParameter((SecondaryStructureElement) o, param.toString(), "" + newValue);
                    }
                } else if (value.toString().equals("Full 2D")) {
                    mediator.getExplorer().setThemeParameter(mediator.getCurrent2DDrawing(), param.toString(), "" + newValue);
                } else {
                    mediator.getExplorer().setThemeParameter((SecondaryStructureElement) value, param.toString(), "" + newValue);
                }
            }
        }
    }

    @Override
    public void fireThemeChange(@NotNull ThemeParameter param, double newValue) {
        if (!this.getMuted()) {
            super.fireThemeChange(param, newValue);
            Object value = this.structureElementsSelectedComboBox.getValue();
            if (value != null) {
                if (value.toString().equals("All Selected Elements")) {
                    for (Object o : structureElementsSelectedComboBox.getItems().subList(2, structureElementsSelectedComboBox.getItems().size())) {
                        mediator.getExplorer().setThemeParameter((SecondaryStructureElement) o, param.toString(), "" + newValue);
                    }
                } else if (value.toString().equals("Full 2D")) {
                    mediator.getExplorer().setThemeParameter(mediator.getCurrent2DDrawing(), param.toString(), "" + newValue);
                } else {
                    mediator.getExplorer().setThemeParameter((SecondaryStructureElement) value, param.toString(), "" + newValue);
                }
            }
        }
    }

    @Override
    public void fireThemeChange(@NotNull ThemeParameter param, @NotNull Color newValue) {
        if (!this.getMuted()) {
            super.fireThemeChange(param, newValue);
            Object value = this.structureElementsSelectedComboBox.getValue();
            if (value != null) {
                if (value.toString().equals("All Selected Elements")) {
                    for (Object o : structureElementsSelectedComboBox.getItems().subList(2, structureElementsSelectedComboBox.getItems().size())) {
                        mediator.getExplorer().setThemeParameter((SecondaryStructureElement) o, param.toString(), getHTMLColorString(newValue));
                    }
                } else if (value.toString().equals("Full 2D")) {
                    mediator.getExplorer().setThemeParameter(mediator.getCurrent2DDrawing(), param.toString(), getHTMLColorString(newValue));
                } else {
                    mediator.getExplorer().setThemeParameter((SecondaryStructureElement) value, param.toString(), getHTMLColorString(newValue));
                }
            }
        }
    }
}


