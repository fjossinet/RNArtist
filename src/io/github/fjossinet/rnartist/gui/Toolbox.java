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
import static io.github.fjossinet.rnartist.io.UtilsKt.awtColorToJavaFX;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;
import static java.util.stream.Collectors.toList;
import static javafx.collections.FXCollections.observableList;

public class Toolbox extends AbstractDrawingConfigurator {

    private Stage stage;
    private Mediator mediator;
    private ColorPicker colorPicker;
    private ChoiceBox<String> letterColor;
    private Slider opacity, secondaryInteractionShift;
    private ComboBox<String> fontNames, lineWidth, tertiaryInteractionStyle;
    private List<Button> recoverButtons;
    private ComboBox<Object> defaultTargetsComboBox, structureElementsSelectedComboBox;
    private ObservableList<org.apache.commons.lang3.tuple.Pair<String, NitriteId>> savedThemes = FXCollections.observableList(new ArrayList());
    private Spinner<Integer> deltaXRes, deltaYRes, deltaFontSize;
    private FlowPane junctionKnobs = new FlowPane();

    private ObservableList<ThemeFromWebsite> themesList;

    public Toolbox(Mediator mediator) {
        this.mediator = mediator;
        this.recoverButtons = new ArrayList<Button>();
        this.stage = new Stage();
        this.setMuted(true); //the modifications od the Controls will not be applied withiout chicking on the apply/clear buttons
        stage.setTitle("Toolbox");
        this.createScene(stage);
        for (Document theme : mediator.getEmbeddedDB().getThemes().find()) {
            this.savedThemes.add(org.apache.commons.lang3.tuple.Pair.of((String) theme.get("name"), theme.getId()));
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
        scene.getWindow().setWidth(400);
        scene.getWindow().setHeight(screenSize.getHeight());
        scene.getWindow().setX(0);
        scene.getWindow().setY(0);
    }

    private void createThemePanel(TabPane root) {

        VBox parent = new VBox();

        //++++list of default targets listening to theme modifications
        this.defaultTargetsComboBox = new ComboBox<Object>(FXCollections.observableList(Arrays.asList(
                "Full 2D",
                "All Helices",
                "All Single Strands",
                "All Junctions",
                "All PseudoKnots",
                "All Apical Loops",
                "All Inner Loops",
                "All 3-Way Junctions",
                "All 4-Way Junctions",
                "All Secondary Interactions",
                "All Tertiary Interactions",
                "All Residues",
                "All Adenines",
                "All Uridines",
                "All Guanines",
                "All Cytidines",
                "All Unknown Residues",
                "All LW Symbols",
                "All Regular Symbols",
                "All Phosphodiester Bonds")));

        this.defaultTargetsComboBox.setMaxWidth(Double.MAX_VALUE);
        this.defaultTargetsComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {

            public void changed(ObservableValue<? extends Object> ov,
                                final Object oldvalue, final Object newvalue) {
                removeDrawingConfigurationListeners();
                structureElementsSelectedComboBox.setValue(null);
                for (Button b : recoverButtons)
                    b.setDisable(true);
                if (newvalue != null) {
                    if (newvalue.toString().equals("Full 2D")) {
                        addDrawingConfigurationListener(mediator.getCurrent2DDrawing());
                    } else if (newvalue.toString().equals("All Helices"))
                        for (HelixDrawing h : mediator.getCurrent2DDrawing().getAllHelices()) {
                            addDrawingConfigurationListener(h);
                        }
                    else if (newvalue.toString().equals("All Single Strands"))
                        for (SingleStrandDrawing ss : mediator.getCurrent2DDrawing().getSingleStrands()) {
                            addDrawingConfigurationListener(ss);
                        }
                    else if (newvalue.toString().equals("All Junctions"))
                        for (JunctionDrawing jc : mediator.getCurrent2DDrawing().getAllJunctions()) {
                            addDrawingConfigurationListener(jc);
                        }
                    else if (newvalue.toString().equals("All PseudoKnots"))
                        for (PKnotDrawing pknot : mediator.getCurrent2DDrawing().getPknots()) {
                            addDrawingConfigurationListener(pknot);
                        }
                    else if (newvalue.toString().equals("All Apical Loops"))
                        for (JunctionDrawing jc : mediator.getCurrent2DDrawing().getAllJunctions()) {
                            if (jc.getJunctionCategory() == JunctionType.ApicalLoop)
                                addDrawingConfigurationListener(jc);
                        }
                    else if (newvalue.toString().equals("All Inner Loops"))
                        for (JunctionDrawing jc : mediator.getCurrent2DDrawing().getAllJunctions()) {
                            if (jc.getJunctionCategory() == JunctionType.InnerLoop)
                                addDrawingConfigurationListener(jc);
                        }
                    else if (newvalue.toString().equals("All 3-Way Junctions"))
                        for (JunctionDrawing jc : mediator.getCurrent2DDrawing().getAllJunctions()) {
                            if (jc.getJunctionCategory() == JunctionType.ThreeWay)
                                addDrawingConfigurationListener(jc);
                        }
                    else if (newvalue.toString().equals("All 4-Way Junctions"))
                        for (JunctionDrawing jc : mediator.getCurrent2DDrawing().getAllJunctions()) {
                            if (jc.getJunctionCategory() == JunctionType.FourWay)
                                addDrawingConfigurationListener(jc);
                        }
                    else if (newvalue.toString().equals("All Secondary Interactions"))
                        for (BaseBaseInteractionDrawing secondary : mediator.getCurrent2DDrawing().getAllSecondaryInteractions()) {
                            addDrawingConfigurationListener(secondary);
                        }
                    else if (newvalue.toString().equals("All Tertiary Interactions"))
                        for (TertiaryInteractionDrawing tertiary : mediator.getCurrent2DDrawing().getAllTertiaryInteractions()) {
                            addDrawingConfigurationListener(tertiary);
                        }
                    else if (newvalue.toString().equals("All Residues"))
                        for (ResidueDrawing r : mediator.getCurrent2DDrawing().getResidues()) {
                            addDrawingConfigurationListener(r);
                        }
                    else if (newvalue.toString().equals("All Adenines"))
                        for (ResidueDrawing r : mediator.getCurrent2DDrawing().getResidues()) {
                            if (r.getType() == SecondaryStructureType.A)
                                addDrawingConfigurationListener(r);
                        }
                    else if (newvalue.toString().equals("All Uridines"))
                        for (ResidueDrawing r : mediator.getCurrent2DDrawing().getResidues()) {
                            if (r.getType() == SecondaryStructureType.U)
                                addDrawingConfigurationListener(r);
                        }
                    else if (newvalue.toString().equals("All Guanines"))
                        for (ResidueDrawing r : mediator.getCurrent2DDrawing().getResidues()) {
                            if (r.getType() == SecondaryStructureType.G)
                                addDrawingConfigurationListener(r);
                        }
                    else if (newvalue.toString().equals("All Cytidines"))
                        for (ResidueDrawing r : mediator.getCurrent2DDrawing().getResidues()) {
                            if (r.getType() == SecondaryStructureType.C)
                                addDrawingConfigurationListener(r);
                        }
                    else if (newvalue.toString().equals("All Unknown Residues"))
                        for (ResidueDrawing r : mediator.getCurrent2DDrawing().getResidues()) {
                            if (r.getType() == SecondaryStructureType.X)
                                addDrawingConfigurationListener(r);
                        }
                    else if (newvalue.toString().equals("All Regular Symbols"))
                        for (LWSymbolDrawing s : mediator.getCurrent2DDrawing().getAllRegularSymbols()) {
                            addDrawingConfigurationListener(s);
                        }
                    else if (newvalue.toString().equals("All LW Symbols"))
                        for (LWSymbolDrawing s : mediator.getCurrent2DDrawing().getAllLWSymbols()) {
                            addDrawingConfigurationListener(s);
                        }
                    else if (newvalue.toString().equals("All Phosphodiester Bonds"))
                        for (PhosphodiesterBondDrawing p : mediator.getCurrent2DDrawing().getAllPhosphoBonds()) {
                            addDrawingConfigurationListener(p);
                        }
                }
            }
        });

        //+++++list of the structural elements selected listening to theme modifications
        this.structureElementsSelectedComboBox = new ComboBox<Object>(mediator.getStructureElementsSelected());
        this.structureElementsSelectedComboBox.setConverter(new StringConverter<Object>() {
            @Override
            public String toString(Object secondaryStructureElement) {
                if (secondaryStructureElement == null) {
                    return "";
                }
                if (DrawingElement.class.isInstance(secondaryStructureElement))
                    return ((DrawingElement) secondaryStructureElement).getType() + " " + ((DrawingElement) secondaryStructureElement).getName() + " " + ((DrawingElement) secondaryStructureElement).getLocation().getDescription();
                else if (String.class.isInstance(secondaryStructureElement)) {
                    return secondaryStructureElement.toString();
                }
                return "";
            }

            @Override
            public DrawingElement fromString(String s) {
                return null;
            }
        });
        this.structureElementsSelectedComboBox.setMaxWidth(Double.MAX_VALUE);
        this.structureElementsSelectedComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {

            public void changed(ObservableValue<? extends Object> ov,
                                final Object oldvalue, final Object newvalue) {
                removeDrawingConfigurationListeners();
                defaultTargetsComboBox.setValue(null);
                for (Button b : recoverButtons)
                    b.setDisable(true);
                if (newvalue != null) {
                    if (newvalue.toString().equals("All Selected Elements"))
                        for (Object o : structureElementsSelectedComboBox.getItems().subList(structureElementsSelectedComboBox.getItems().indexOf("All Selected Elements") + 1, structureElementsSelectedComboBox.getItems().size())) {
                            addDrawingConfigurationListener((DrawingElement) o);
                        }
                    else {
                        addDrawingConfigurationListener((DrawingElement) newvalue);
                        //if a single element is selected, we activate the buttons to recover theme parameters
                        for (Button b : recoverButtons)
                            b.setDisable(false);
                        if (RnartistConfig.getFitDisplayOnSelection()) { //fit first since fit will center too
                            mediator.canvas2D.fitDisplayOn(((DrawingElement) newvalue).getBounds2D());
                        } else if (RnartistConfig.getCenterDisplayOnSelection()) {
                            mediator.canvas2D.centerDisplayOn(((DrawingElement) newvalue).getBounds2D());
                        }
                    }
                }
            }
        });

        GridPane targetForm = new GridPane();
        targetForm.setHgap(5);
        targetForm.setVgap(5);
        targetForm.setPadding(new Insets(10, 10, 10, 10));
        targetForm.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        targetForm.getColumnConstraints().addAll(cc);

        Label l = new Label("Target");
        l.setStyle("-fx-font-size: 20");
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(l, 0, 0, 5, 1);
        GridPane.setHalignment(l, HPos.LEFT);
        targetForm.getChildren().add(l);

        l = new Label("Default Targets");
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(l, 0, 1, 5, 1);
        GridPane.setHalignment(l, HPos.LEFT);
        targetForm.getChildren().add(l);

        GridPane.setConstraints(this.defaultTargetsComboBox, 0, 2, 2, 1);
        targetForm.getChildren().add(this.defaultTargetsComboBox);

        Button applyTheme = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        applyTheme.setMinWidth(Control.USE_PREF_SIZE);
        applyTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                Map<String, String> params = new HashMap<String, String>();
                params.put(DrawingConfigurationParameter.FontName.toString(), fontNames.getValue());
                params.put(DrawingConfigurationParameter.DeltaXRes.toString(), "" + deltaXRes.getValue());
                params.put(DrawingConfigurationParameter.DeltaYRes.toString(), "" + deltaYRes.getValue());
                params.put(DrawingConfigurationParameter.DeltaFontSize.toString(), "" + deltaFontSize.getValue().intValue());
                params.put(DrawingConfigurationParameter.Opacity.toString(), "" + (int) ((double) (opacity.getValue()) / 100.0 * 255.0));
                params.put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(javaFXToAwt(colorPicker.getValue())));
                params.put(DrawingConfigurationParameter.CharColor.toString(), getHTMLColorString(letterColor.getValue() == "White" ? Color.WHITE : Color.BLACK));
                params.put(DrawingConfigurationParameter.LineWidth.toString(), lineWidth.getValue());
                params.put(DrawingConfigurationParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue());
                params.put(DrawingConfigurationParameter.LineShift.toString(), "" + secondaryInteractionShift.getValue());
                applyDrawingConfiguration(new DrawingConfiguration(params));
                setMuted(true);
            }
        });

        GridPane.setConstraints(applyTheme, 2, 2, 1, 1);
        applyTheme.setMaxWidth(Double.MAX_VALUE);
        targetForm.getChildren().add(applyTheme);

        Button clearTheme = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        clearTheme.setMinWidth(Control.USE_PREF_SIZE);
        clearTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                clearDrawingConfiguration(mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        GridPane.setConstraints(clearTheme, 3, 2, 1, 1);
        clearTheme.setMaxWidth(Double.MAX_VALUE);
        targetForm.getChildren().add(clearTheme);

        l = new Label("Your Selections");
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(l, 0, 3, 5, 1);
        GridPane.setHalignment(l, HPos.LEFT);
        targetForm.getChildren().add(l);

        GridPane.setConstraints(this.structureElementsSelectedComboBox, 0, 4, 2, 1);
        targetForm.getChildren().add(this.structureElementsSelectedComboBox);

        applyTheme = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        applyTheme.setMinWidth(Control.USE_PREF_SIZE);
        applyTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                Map<String, String> params = new HashMap<String, String>();
                params.put(DrawingConfigurationParameter.FontName.toString(), fontNames.getValue());
                params.put(DrawingConfigurationParameter.DeltaXRes.toString(), "" + deltaXRes.getValue());
                params.put(DrawingConfigurationParameter.DeltaYRes.toString(), "" + deltaYRes.getValue());
                params.put(DrawingConfigurationParameter.DeltaFontSize.toString(), "" + deltaFontSize.getValue().intValue());
                params.put(DrawingConfigurationParameter.Opacity.toString(), "" + (int) ((double) (opacity.getValue()) / 100.0 * 255.0));
                params.put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(javaFXToAwt(colorPicker.getValue())));
                params.put(DrawingConfigurationParameter.CharColor.toString(), getHTMLColorString(letterColor.getValue() == "White" ? Color.WHITE : Color.BLACK));
                params.put(DrawingConfigurationParameter.LineWidth.toString(), lineWidth.getValue());
                params.put(DrawingConfigurationParameter.TertiaryInteractionStyle.toString(), tertiaryInteractionStyle.getValue());
                params.put(DrawingConfigurationParameter.LineShift.toString(), "" + secondaryInteractionShift.getValue());
                applyDrawingConfiguration(new DrawingConfiguration(params));
                setMuted(true);
            }
        });

        GridPane.setConstraints(applyTheme, 2, 4, 1, 1);
        applyTheme.setMaxWidth(Double.MAX_VALUE);
        targetForm.getChildren().add(applyTheme);

        clearTheme = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        clearTheme.setMinWidth(Control.USE_PREF_SIZE);
        clearTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                clearDrawingConfiguration(mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        GridPane.setConstraints(clearTheme, 3, 4, 1, 1);
        clearTheme.setMaxWidth(Double.MAX_VALUE);
        targetForm.getChildren().add(clearTheme);

        Button recoverTheme = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        recoverTheme.setMinWidth(Control.USE_PREF_SIZE);
        recoverTheme.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Map<String, String> params = ((DrawingElement) structureElementsSelectedComboBox.getValue()).getDrawingConfiguration().getParams();

            }
        });
        recoverTheme.setDisable(true);
        this.recoverButtons.add(recoverTheme);

        GridPane.setConstraints(recoverTheme, 4, 4, 1, 1);
        recoverTheme.setMaxWidth(Double.MAX_VALUE);
        targetForm.getChildren().add(recoverTheme);

        parent.getChildren().add(targetForm);

        VBox currentThemeVBox = new VBox();
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
        fontsPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), cc, new ColumnConstraints());

        fontNames = new ComboBox<>(
                observableList(Font.getFamilies().stream().distinct().collect(toList())));

        EventHandler eventHandler = (event) -> {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        setMuted(false);
                        fireDrawingConfigurationChange(DrawingConfigurationParameter.FontName, fontNames.getValue());
                        setMuted(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.FontName, fontNames.getValue());
                setMuted(true);
            }
        });

        Button clearFontName = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearFontName, 7, 0, 1, 1);
        fontsPane.getChildren().add(clearFontName);

        clearFontName.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.FontName, mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        Button recoverFontName = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        GridPane.setConstraints(recoverFontName, 8, 0, 1, 1);
        fontsPane.getChildren().add(recoverFontName);

        recoverFontName.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DrawingElement el = (DrawingElement) structureElementsSelectedComboBox.getValue();
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.FontName.toString()))
                    fontNames.setValue(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.FontName.toString()));
            }
        });
        recoverFontName.setDisable(true);
        this.recoverButtons.add(recoverFontName);

        deltaXRes = new Spinner<Integer>();
        deltaXRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.DeltaXRes.toString()))));
        deltaXRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaXRes, deltaXRes.getValue());
                setMuted(true);
            }
        });

        deltaYRes = new Spinner<Integer>();
        deltaYRes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-15, 15, Integer.parseInt(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.DeltaYRes.toString()))));
        deltaYRes.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaYRes, deltaYRes.getValue());
                setMuted(true);
            }
        });

        deltaFontSize = new Spinner<Integer>();
        deltaFontSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 5, Integer.parseInt(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.DeltaFontSize.toString()))));
        deltaFontSize.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaFontSize, deltaFontSize.getValue().intValue());
                setMuted(true);
            }
        });

        l = new Label("x");
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
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaXRes, deltaXRes.getValue());
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaYRes, deltaYRes.getValue());
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaFontSize, deltaFontSize.getValue().intValue());
                setMuted(true);
            }
        });

        Button clearDeltas = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        clearDeltas.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(clearDeltas, 7, 1, 1, 1);
        fontsPane.getChildren().add(clearDeltas);

        clearDeltas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaXRes, mouseEvent.isShiftDown());
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaYRes, mouseEvent.isShiftDown());
                fireDrawingConfigurationChange(DrawingConfigurationParameter.DeltaFontSize, mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        Button recoverDeltas = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        recoverDeltas.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(recoverDeltas, 8, 1, 1, 1);
        fontsPane.getChildren().add(recoverDeltas);

        recoverDeltas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DrawingElement el = (DrawingElement) structureElementsSelectedComboBox.getValue();
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.DeltaXRes.toString()))
                    deltaXRes.getValueFactory().setValue(Integer.parseInt(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.DeltaXRes.toString())));
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.DeltaYRes.toString()))
                    deltaYRes.getValueFactory().setValue(Integer.parseInt(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.DeltaYRes.toString())));
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.DeltaFontSize.toString()))
                    deltaFontSize.getValueFactory().setValue(Integer.parseInt(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.DeltaFontSize.toString())));
            }
        });
        recoverDeltas.setDisable(true);
        this.recoverButtons.add(recoverDeltas);

        currentThemeVBox.getChildren().add(fontsPane);

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
        colorsPane.getColumnConstraints().addAll(cc, new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints());

        colorPicker = new ColorPicker();
        colorPicker.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {

            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color newValue) {
                fireDrawingConfigurationChange(DrawingConfigurationParameter.Color, javaFXToAwt(colorPicker.getValue()));
            }
        });
        colorPicker.setValue(awtColorToJavaFX(getAWTColor(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.Color.toString()), 255)));
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setMinWidth(Control.USE_PREF_SIZE);

        l = new Label("Shape");
        GridPane.setConstraints(l, 0, 0, 1, 1);
        GridPane.setHalignment(l, HPos.CENTER);
        colorsPane.getChildren().add(l);
        l = new Label("Character");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1, 0, 1, 1);
        GridPane.setHalignment(l, HPos.CENTER);
        colorsPane.getChildren().add(l);

        GridPane.setConstraints(colorPicker, 0, 1, 1, 1);
        colorsPane.getChildren().add(colorPicker);
        GridPane.setHalignment(colorPicker, HPos.CENTER);
        this.letterColor = new ChoiceBox<String>();
        letterColor.setMaxWidth(Double.MAX_VALUE);
        letterColor.setMinWidth(Control.USE_PREF_SIZE);
        letterColor.getItems().addAll("White", "Black");
        letterColor.setValue(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.CharColor.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
        letterColor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                fireDrawingConfigurationChange(DrawingConfigurationParameter.CharColor, letterColor.getValue() == "White" ? Color.WHITE : Color.BLACK);
            }
        });

        GridPane.setConstraints(letterColor, 1, 1, 1, 1);
        colorsPane.getChildren().add(letterColor);
        GridPane.setHalignment(letterColor, HPos.CENTER);

        Button applyColor = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyColor, 2, 1, 1, 1);
        colorsPane.getChildren().add(applyColor);

        applyColor.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.Color, javaFXToAwt(colorPicker.getValue()));
                fireDrawingConfigurationChange(DrawingConfigurationParameter.CharColor, letterColor.getValue() == "White" ? Color.WHITE : Color.BLACK);
                setMuted(true);
            }
        });

        Button clearColor = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearColor, 3, 1, 1, 1);
        colorsPane.getChildren().add(clearColor);

        clearColor.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.Color, mouseEvent.isShiftDown());
                fireDrawingConfigurationChange(DrawingConfigurationParameter.CharColor, mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        Button recoverColor = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        recoverColor.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(recoverColor, 4, 1, 1, 1);
        colorsPane.getChildren().add(recoverColor);

        recoverColor.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DrawingElement el = (DrawingElement) structureElementsSelectedComboBox.getValue();
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.Color.toString()))
                    colorPicker.setValue(awtColorToJavaFX(getAWTColor(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.Color.toString()), 255)));
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.CharColor.toString()))
                    letterColor.setValue(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.CharColor.toString()).toLowerCase().equals("#ffffff") ? "White" : "Black");
            }
        });
        recoverColor.setDisable(true);
        this.recoverButtons.add(recoverColor);

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
        linesPane.getColumnConstraints().addAll(new ColumnConstraints(), cc);

        int row = 0;

        lineWidth = new ComboBox<>();
        lineWidth.getItems().addAll("0", "0.25", "0.5", "0.75", "1.0", "1.25", "1.5", "1.75", "2.0", "2.5", "3.0", "3.5", "4.0", "5.0", "6.0", "7.0", "8.0", "9.0", "10.0");
        lineWidth.setValue(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.LineWidth.toString()));
        lineWidth.setCellFactory(new ShapeCellFactory());
        lineWidth.setButtonCell(new ShapeCell());
        lineWidth.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireDrawingConfigurationChange(DrawingConfigurationParameter.LineWidth, Double.parseDouble(lineWidth.getValue()));
            }
        });
        lineWidth.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(lineWidth, 0, row, 1, 1);
        linesPane.getChildren().add(lineWidth);
        GridPane.setHalignment(lineWidth, HPos.CENTER);

        l = new Label("Line Width (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1, row, 2, 1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l, HPos.LEFT);

        Button applyLineWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyLineWidth, 3, row, 1, 1);
        linesPane.getChildren().add(applyLineWidth);

        applyLineWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.LineWidth, Double.parseDouble(lineWidth.getValue()));
                setMuted(true);
            }
        });

        Button clearLineWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearLineWidth, 4, row, 1, 1);
        linesPane.getChildren().add(clearLineWidth);

        clearLineWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.LineWidth, mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        Button recoverLineWidth = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        recoverLineWidth.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(recoverLineWidth, 5, row, 1, 1);
        linesPane.getChildren().add(recoverLineWidth);

        recoverLineWidth.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DrawingElement el = (DrawingElement) structureElementsSelectedComboBox.getValue();
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.LineWidth.toString()))
                    lineWidth.setValue(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.LineWidth.toString()));
            }
        });
        recoverLineWidth.setDisable(true);
        this.recoverButtons.add(recoverLineWidth);

        row++;

        tertiaryInteractionStyle = new ComboBox<>();
        tertiaryInteractionStyle.getItems().addAll("solid", "dashed");
        tertiaryInteractionStyle.setValue(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.TertiaryInteractionStyle.toString()));
        tertiaryInteractionStyle.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String old_val, String new_val) {
                fireDrawingConfigurationChange(DrawingConfigurationParameter.TertiaryInteractionStyle, tertiaryInteractionStyle.getValue());
            }
        });
        tertiaryInteractionStyle.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(tertiaryInteractionStyle, 0, row, 1, 1);
        linesPane.getChildren().add(tertiaryInteractionStyle);
        GridPane.setHalignment(tertiaryInteractionStyle, HPos.CENTER);

        l = new Label("Tertiaries Line Style");
        l.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(l, 1, row, 1, 1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l, HPos.LEFT);

        Button applyTertiariesStyle = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyTertiariesStyle, 3, row, 1, 1);
        linesPane.getChildren().add(applyTertiariesStyle);

        applyTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.TertiaryInteractionStyle, tertiaryInteractionStyle.getValue());
                setMuted(true);
            }
        });

        Button clearTertiariesStyle = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearTertiariesStyle, 4, row, 1, 1);
        linesPane.getChildren().add(clearTertiariesStyle);

        clearTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.TertiaryInteractionStyle, mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        Button recoverTertiariesStyle = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        recoverTertiariesStyle.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(recoverTertiariesStyle, 5, row, 1, 1);
        linesPane.getChildren().add(recoverTertiariesStyle);

        recoverTertiariesStyle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DrawingElement el = (DrawingElement) structureElementsSelectedComboBox.getValue();
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.TertiaryInteractionStyle.toString()))
                    tertiaryInteractionStyle.setValue(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.TertiaryInteractionStyle.toString()));
            }
        });
        recoverTertiariesStyle.setDisable(true);
        this.recoverButtons.add(recoverTertiariesStyle);

        row++;

        l = new Label("Secondaries Line Shift (px)");
        l.setMinWidth(Control.USE_PREF_SIZE);
        l.setPadding(new Insets(10, 0, 0, 0));
        GridPane.setConstraints(l, 0, row, 6, 1);
        linesPane.getChildren().add(l);
        GridPane.setHalignment(l, HPos.LEFT);

        row++;

        secondaryInteractionShift = new Slider(0, 10, Double.parseDouble(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.LineShift.toString())));
        secondaryInteractionShift.setShowTickLabels(true);
        secondaryInteractionShift.setShowTickMarks(true);
        secondaryInteractionShift.setMajorTickUnit(5);
        secondaryInteractionShift.setMinorTickCount(1);
        secondaryInteractionShift.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireDrawingConfigurationChange(DrawingConfigurationParameter.LineShift, secondaryInteractionShift.getValue());
            }
        });
        secondaryInteractionShift.setShowTickMarks(true);
        secondaryInteractionShift.setMaxWidth(Double.MAX_VALUE);

        GridPane.setConstraints(secondaryInteractionShift, 0, row, 3, 1);
        linesPane.getChildren().add(secondaryInteractionShift);
        GridPane.setHalignment(secondaryInteractionShift, HPos.CENTER);

        Button applySecondariesShift = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applySecondariesShift, 3, row, 1, 1);
        GridPane.setValignment(applySecondariesShift, VPos.TOP);
        linesPane.getChildren().add(applySecondariesShift);

        applySecondariesShift.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.LineShift, secondaryInteractionShift.getValue());
                setMuted(true);
            }
        });

        Button clearSecondariesShift = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearSecondariesShift, 4, row, 1, 1);
        GridPane.setValignment(clearSecondariesShift, VPos.TOP);
        linesPane.getChildren().add(clearSecondariesShift);

        clearSecondariesShift.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.LineShift, mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        Button recoverSecondariesShift = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        recoverSecondariesShift.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setValignment(recoverSecondariesShift, VPos.TOP);
        GridPane.setConstraints(recoverSecondariesShift, 5, row, 1, 1);
        linesPane.getChildren().add(recoverSecondariesShift);

        recoverSecondariesShift.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DrawingElement el = (DrawingElement) structureElementsSelectedComboBox.getValue();
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.LineShift.toString()))
                    secondaryInteractionShift.setValue(Double.parseDouble(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.LineShift.toString())));
            }
        });
        recoverSecondariesShift.setDisable(true);
        this.recoverButtons.add(recoverSecondariesShift);

        row++;

        currentThemeVBox.getChildren().add(linesPane);

        //++++++ pane for the misc options
        title = new Label("Misc");
        title.setStyle("-fx-font-size: 20");
        currentThemeVBox.getChildren().add(new VBox(title, new Separator(Orientation.HORIZONTAL)));

        GridPane miscPane = new GridPane();
        miscPane.setHgap(10);
        miscPane.setVgap(10);
        miscPane.setPadding(new Insets(0, 0, 10, 0));
        miscPane.setMaxWidth(Double.MAX_VALUE);

        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        miscPane.getColumnConstraints().addAll(cc);

        l = new Label("Opacity (%)");
        GridPane.setConstraints(l, 0, 0, 4, 1);
        l.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHalignment(l, HPos.LEFT);
        miscPane.getChildren().add(l);

        this.opacity = new Slider(0, 100, Integer.parseInt(RnartistConfig.defaultTheme.get(SecondaryStructureType.Full2D.toString()).get(DrawingConfigurationParameter.Opacity.toString())) / 255.0 * 100.0);
        this.opacity.setShowTickLabels(true);
        this.opacity.setShowTickMarks(true);
        this.opacity.setMajorTickUnit(50);
        this.opacity.setMinorTickCount(5);
        this.opacity.setShowTickMarks(true);
        this.opacity.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                fireDrawingConfigurationChange(DrawingConfigurationParameter.Opacity, (int) ((double) (opacity.getValue()) / 100.0 * 255.0));
            }
        });
        GridPane.setConstraints(this.opacity, 0, 1, 1, 1);
        this.opacity.setMaxWidth(Double.MAX_VALUE);
        miscPane.getChildren().add(this.opacity);

        Button applyOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE));
        GridPane.setConstraints(applyOpacity, 1, 1, 1, 1);
        GridPane.setValignment(applyOpacity, VPos.TOP);
        miscPane.getChildren().add(applyOpacity);

        applyOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.Opacity, (int) ((double) (opacity.getValue()) / 100.0 * 255.0));
                setMuted(true);
            }
        });

        Button clearOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
        GridPane.setConstraints(clearOpacity, 2, 1, 1, 1);
        GridPane.setValignment(clearOpacity, VPos.TOP);
        miscPane.getChildren().add(clearOpacity);

        clearOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                setMuted(false);
                fireDrawingConfigurationChange(DrawingConfigurationParameter.Opacity, mouseEvent.isShiftDown());
                setMuted(true);
            }
        });

        Button recoverOpacity = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYEDROPPER));
        GridPane.setConstraints(recoverOpacity, 3, 1, 1, 1);
        GridPane.setValignment(recoverOpacity, VPos.TOP);
        miscPane.getChildren().add(recoverOpacity);

        recoverOpacity.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DrawingElement el = (DrawingElement) structureElementsSelectedComboBox.getValue();
                if (el.getDrawingConfiguration().getParams().containsKey(DrawingConfigurationParameter.Opacity.toString()))
                    opacity.setValue(Integer.parseInt(el.getDrawingConfiguration().getParams().get(DrawingConfigurationParameter.Opacity.toString())) / 255.0 * 100.0);
            }
        });
        recoverOpacity.setDisable(true);
        this.recoverButtons.add(recoverOpacity);

        currentThemeVBox.getChildren().add(miscPane);

        ScrollPane sp = new ScrollPane(currentThemeVBox);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        parent.getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);

        Tab theme = new Tab("2D Theme", parent);
        root.getTabs().add(theme);
    }

    public ComboBox<Object> getStructureElementsSelectedComboBox() {
        return structureElementsSelectedComboBox;
    }

    public ComboBox<Object> getDefaultTargetsComboBox() {
        return defaultTargetsComboBox;
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

    public void addJunctionKnob(JunctionDrawing jc) {
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
            typeName = typeName.split("Way")[0] + " Way Junction";
        } else if (typeName.endsWith("Loop")) {
            typeName = typeName.split("Loop")[0] + " Loop";
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
        GridPane.setConstraints(chimeraLabel, 0, 0, 2, 1);

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
                    try {
                        RnartistConfig.save(null, null); //we save the chimera path, not the theme (perhaps the user is not interested to save the current theme)
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
        GridPane.setConstraints(userIDLabel, 0, 0, 2, 1);

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
        GridPane optionsPane = new GridPane();
        cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        optionsPane.getColumnConstraints().addAll(new ColumnConstraints(), cc);
        vbox.getChildren().add(optionsPane);
        optionsPane.setPadding(new Insets(10, 10, 10, 10));
        optionsPane.setHgap(10);
        optionsPane.setVgap(10);

        int row = 0;

        Label svgExport = new Label("Misc Settings");
        svgExport.setStyle("-fx-font-size: 15");
        optionsPane.getChildren().add(svgExport);
        GridPane.setConstraints(svgExport, 0, row++, 2, 1);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        optionsPane.getChildren().add(separator);
        GridPane.setConstraints(separator, 0, row++, 2, 1);

        CheckBox svgBrowserFix = new CheckBox();
        svgBrowserFix.setSelected(RnartistConfig.exportSVGWithBrowserCompatibility());
        svgBrowserFix.setOnAction(actionEvent -> {
            RnartistConfig.exportSVGWithBrowserCompatibility(svgBrowserFix.isSelected());
        });
        optionsPane.getChildren().add(svgBrowserFix);
        GridPane.setConstraints(svgBrowserFix, 0, row);

        Label l = new Label("Set Browser Compatibility for SVG Export");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1, row++);

        CheckBox defaultThemeOnExit = new CheckBox();
        defaultThemeOnExit.setSelected(RnartistConfig.saveCurrentThemeOnExit());
        defaultThemeOnExit.setOnAction(actionEvent -> {
            RnartistConfig.saveCurrentThemeOnExit(defaultThemeOnExit.isSelected());
        });
        optionsPane.getChildren().add(defaultThemeOnExit);
        GridPane.setConstraints(defaultThemeOnExit, 0, row);

        l = new Label("Set Current Theme as Default on Exit");
        optionsPane.getChildren().add(l);
        GridPane.setConstraints(l, 1, row++);

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

    @Override
    public void fireDrawingConfigurationChange(@NotNull DrawingConfigurationParameter param, @NotNull String newValue) {
        if (!this.getMuted()) {
            super.fireDrawingConfigurationChange(param, newValue);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }

    @Override
    public void fireDrawingConfigurationChange(@NotNull DrawingConfigurationParameter param, int newValue) {
        if (!this.getMuted()) {
            super.fireDrawingConfigurationChange(param, newValue);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }

    @Override
    public void fireDrawingConfigurationChange(@NotNull DrawingConfigurationParameter param, double newValue) {
        if (!this.getMuted()) {
            super.fireDrawingConfigurationChange(param, newValue);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }

    @Override
    public void fireDrawingConfigurationChange(@NotNull DrawingConfigurationParameter param, @NotNull Color newValue) {
        if (!this.getMuted()) {
            super.fireDrawingConfigurationChange(param, newValue);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }

    @Override
    public void fireDrawingConfigurationChange(@NotNull DrawingConfigurationParameter param, boolean fireChildren) {
        if (!this.getMuted()) {
            super.fireDrawingConfigurationChange(param, fireChildren);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }

    @Override
    public void applyTheme(Map<String, ? extends Map<String, String>> theme) {
        if (!this.getMuted()) {
            super.applyTheme(theme);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }

    @Override
    public void applyDrawingConfiguration(@NotNull DrawingConfiguration drawingConfiguration) {
        if (!this.getMuted()) {
            super.applyDrawingConfiguration(drawingConfiguration);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }

    @Override
    public void clearDrawingConfiguration(boolean clearChildrenDrawingConfiguration) {
        if (!this.getMuted()) {
            super.clearDrawingConfiguration(clearChildrenDrawingConfiguration);
            mediator.getCanvas2D().repaint();
            mediator.getExplorer().refresh();
        }
    }
}


