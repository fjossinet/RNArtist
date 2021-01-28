package io.github.fjossinet.rnartist;

import io.github.fjossinet.rnartist.gui.*;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.core.model.io.Rnaview;
import io.github.fjossinet.rnartist.io.ChimeraDriver;
import io.github.fjossinet.rnartist.model.DrawingLoaded;
import io.github.fjossinet.rnartist.model.DrawingLoadedFromFile;
import io.github.fjossinet.rnartist.model.DrawingLoadedFromRNArtistDB;
import io.github.fjossinet.rnartist.model.ExplorerItem;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.dizitart.no2.NitriteId;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.*;
import static io.github.fjossinet.rnartist.io.UtilsKt.awtColorToJavaFX;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;

public class RNArtist extends Application {

    public static final byte ELEMENT_SCOPE = 0, STRUCTURAL_DOMAIN_SCOPE = 1, BRANCH_SCOPE = 2;
    private Mediator mediator;
    private Stage stage;
    private int scrollCounter = 0;
    private FlowPane statusBar;
    private MenuButton allStructuresAvailable;
    private MenuItem updateSavedThemeItem, clearAll2DsItem, clearAll2DsExceptCurrentItem;
    private Button saveProject, focus3D, reload3D, paintSelectionin3D, paintSelectionAsCartoon, paintSelectionAsStick, showRibbon, hideRibbon;

    //user defined global configurations
    private Slider detailsLevel;
    private boolean centerDisplayOnSelection = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("prism.lcdtext", "false"); // to avoid to have the font "scratched"
        this.stage = stage;
        this.stage.setOnCloseRequest(windowEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(RNArtist.this.stage);
            alert.initModality(Modality.WINDOW_MODAL);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure to exit RNArtist?");

            Stage alerttStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alerttStage.setAlwaysOnTop(true);
            alerttStage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    Platform.exit();
                    RnartistConfig.save();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                windowEvent.consume();
            }
        });
        RnartistConfig.load();
        this.mediator = new Mediator(this);

        Screen screen = Screen.getPrimary();
        BorderPane root = new BorderPane();

        //++++++ top Toolbar
        ToolBar toolbar = new ToolBar();
        toolbar.setPadding(new Insets(5, 5, 5, 5));

        GridPane loadFiles = new GridPane();
        loadFiles.setVgap(5.0);
        loadFiles.setHgap(5.0);

        Label l = new Label("Load");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0, 2, 1);
        loadFiles.getChildren().add(l);

        Button loadData = new Button(null, new FontIcon("fas-sign-in-alt:15"));
        loadData.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FileChooser fileChooser = new FileChooser();
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                if (files != null) {
                    for (File file : files) {
                        fileChooser.setInitialDirectory(file.getParentFile());
                        javafx.concurrent.Task<Pair<Pair<List<SecondaryStructureDrawing>, File>, Exception>> loadData = new javafx.concurrent.Task<Pair<Pair<List<SecondaryStructureDrawing>, File>, Exception>>() {

                            @Override
                            protected Pair<Pair<List<SecondaryStructureDrawing>, File>, Exception> call() {
                                SecondaryStructure ss = null;
                                List<SecondaryStructureDrawing> secondaryStructureDrawings = new ArrayList<SecondaryStructureDrawing>();
                                try {
                                    String source = "file:"+file.getAbsolutePath();
                                    if (file.getName().endsWith(".json")) {
                                        SecondaryStructureDrawing drawing = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseJSON(new FileReader(file));
                                        if (drawing != null) {
                                            drawing.getSecondaryStructure().getRna().setSource(source);
                                            drawing.getSecondaryStructure().setSource(source);
                                            secondaryStructureDrawings.add(drawing);
                                        }
                                    }
                                    if (file.getName().endsWith(".ct")) {
                                        ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseCT(new FileReader(file));
                                        if (ss != null) {
                                            ss.getRna().setSource(source);
                                            ss.setSource(source);
                                            secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, new WorkingSession()));
                                        }
                                    } else if (file.getName().endsWith(".bpseq")) {
                                        ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseBPSeq(new FileReader(file));
                                        if (ss != null) {
                                            ss.getRna().setSource(source);
                                            ss.setSource(source);
                                            secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, new WorkingSession()));
                                        }
                                    } else if (file.getName().endsWith(".fasta") || file.getName().endsWith(".fas") || file.getName().endsWith(".fa") || file.getName().endsWith(".vienna")) {
                                        ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseVienna(new FileReader(file));
                                        if (ss != null) {
                                            ss.getRna().setSource(source);
                                            ss.setSource(source);
                                            secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, new WorkingSession()));
                                        }

                                    } else if (file.getName().endsWith(".xml") || file.getName().endsWith(".rnaml")) {
                                        for (SecondaryStructure structure : io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseRnaml(file)) {
                                            if (!structure.getHelices().isEmpty()) {
                                                structure.getRna().setSource(source);
                                                structure.setSource(source);
                                                secondaryStructureDrawings.add(new SecondaryStructureDrawing(structure, new WorkingSession()));
                                            }
                                        }

                                    } else if (file.getName().matches(".+\\.pdb[0-9]?")) {
                                        if (! (RnartistConfig.isDockerInstalled() && RnartistConfig.isAssemble2DockerImageInstalled())) {
                                            throw new Exception("You cannot use PDB loadFiles, it seems that RNArtist cannot find the RNAVIEW algorithm on your computer.\n Possible causes:\n- the tool Docker is not installed\n- the tool Docker is not running\n- the docker image fjossinet/assemble2 is not installed");
                                        }
                                        for (SecondaryStructure structure : new Rnaview().annotate(file)) {
                                            if (!structure.getHelices().isEmpty()) {
                                                structure.getRna().setSource(source);
                                                secondaryStructureDrawings.add(new SecondaryStructureDrawing(structure, new WorkingSession()));
                                            }
                                        }
                                    } else if (file.getName().endsWith(".stk") || file.getName().endsWith(".stockholm")) {
                                        for (SecondaryStructure structure : io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseStockholm(new FileReader(file))) {
                                            structure.getRna().setSource(source);
                                            structure.setSource(source);
                                            secondaryStructureDrawings.add(new SecondaryStructureDrawing(structure, new WorkingSession()));
                                        }
                                    }
                                } catch (Exception e) {
                                    return Pair.of(Pair.of(secondaryStructureDrawings, file), e);
                                }
                                return Pair.of(Pair.of(secondaryStructureDrawings, file), null);
                            }
                        };
                        loadData.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent workerStateEvent) {
                                try {
                                    if (loadData.get().getRight() != null) {
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setTitle("File Parsing error");
                                        alert.setHeaderText(loadData.get().getRight().getMessage());
                                        alert.setContentText("If this problem persists, you can send the exception stacktrace below to fjossinet@gmail.com");
                                        StringWriter sw = new StringWriter();
                                        PrintWriter pw = new PrintWriter(sw);
                                        loadData.get().getRight().printStackTrace(pw);
                                        String exceptionText = sw.toString();

                                        Label label = new Label("The exception stacktrace was:");

                                        TextArea textArea = new TextArea(exceptionText);
                                        textArea.setEditable(false);
                                        textArea.setWrapText(true);

                                        textArea.setMaxWidth(Double.MAX_VALUE);
                                        textArea.setMaxHeight(Double.MAX_VALUE);
                                        GridPane.setVgrow(textArea, Priority.ALWAYS);
                                        GridPane.setHgrow(textArea, Priority.ALWAYS);

                                        GridPane expContent = new GridPane();
                                        expContent.setMaxWidth(Double.MAX_VALUE);
                                        expContent.add(label, 0, 0);
                                        expContent.add(textArea, 0, 1);
                                        alert.getDialogPane().setExpandableContent(expContent);
                                        alert.showAndWait();
                                    } else {
                                        for (SecondaryStructureDrawing drawing : loadData.get().getLeft().getLeft())
                                            mediator.getDrawingsLoaded().add(new DrawingLoadedFromFile(mediator, drawing,loadData.get().getLeft().getRight()));
                                        //we load and fit (only if not a drawing from a JSON file) on the last 2D loaded
                                        mediator.getDrawingDisplayed().set(mediator.getDrawingsLoaded().get(mediator.getDrawingsLoaded().size() - 1));
                                        if (mediator.getViewX() == 0.0 && mediator.getViewY() == 0.0 && mediator.getZoomLevel() == 1.0)//this test allows to detect JSON loadFiles exported from RNArtist with a focus on a region
                                            mediator.canvas2D.fitStructure(null);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        new Thread(loadData).start();

                    }
                }
            }
        });
        loadData.setTooltip(new Tooltip("Load 2D from file"));
        GridPane.setConstraints(loadData, 0, 1);
        loadFiles.getChildren().add(loadData);

        Button loadProject = new Button(null, new FontIcon("fas-grip-horizontal:15"));
        loadProject.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getProjectsPanel().getStage().show();
                mediator.getProjectsPanel().getStage().toFront();
            }
        });
        loadProject.setTooltip(new Tooltip("Load Project"));
        GridPane.setConstraints(loadProject, 1, 1);
        loadFiles.getChildren().add(loadProject);

        GridPane saveFiles = new GridPane();
        saveFiles.setVgap(5.0);
        saveFiles.setHgap(5.0);

        l = new Label("Save");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0, 3, 1);
        saveFiles.getChildren().add(l);

        Button saveProjectAs = new Button(null, new FontIcon("fas-database:15"));
        saveProjectAs.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        saveProjectAs.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mediator.getDrawingDisplayed().isNotNull().get()) {
                    mediator.getWorkingSession().set_screen_capture(true);
                    mediator.getWorkingSession().setScreen_capture_area(new java.awt.geom.Rectangle2D.Double(mediator.getCanvas2D().getBounds().getCenterX() - 200, mediator.getCanvas2D().getBounds().getCenterY() - 100, 400.0, 200.0));
                    mediator.getCanvas2D().repaint();
                    TextInputDialog dialog = new TextInputDialog("My Project");
                    dialog.initModality(Modality.NONE);
                    dialog.setTitle("Project Saving");
                    dialog.setHeaderText("Keep right mouse button pressed and drag the rectangle to define your project icon.");
                    dialog.setContentText("Project name:");
                    Optional<String> projectName = dialog.showAndWait();
                    if (projectName.isPresent()) {
                        try {
                            NitriteId id = mediator.getProjectsPanel().saveProjectAs(projectName.get().trim(), mediator.getCanvas2D().screenCapture(null));
                            mediator.getWorkingSession().set_screen_capture(false);
                            mediator.getWorkingSession().setScreen_capture_area(null);
                            SecondaryStructureDrawing drawing = mediator.getEmbeddedDB().getProject(id); //we reload it from the DB to have a copy of the drawing and not the same object
                            mediator.getDrawingsLoaded().add(0, new DrawingLoadedFromRNArtistDB(mediator, drawing, id, projectName.get().trim()));
                            mediator.getDrawingDisplayed().set(mediator.getDrawingsLoaded().get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mediator.getWorkingSession().set_screen_capture(false);
                        mediator.getWorkingSession().setScreen_capture_area(null);
                        mediator.getCanvas2D().repaint();
                    }
                }
            }
        });
        saveProjectAs.setTooltip(new Tooltip("Save Project As..."));
        GridPane.setConstraints(saveProjectAs, 0, 1);
        saveFiles.getChildren().add(saveProjectAs);

        this.saveProject = new Button(null, new FontIcon("fas-sync:15"));
        this.saveProject.setDisable(true);
        this.saveProject.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    mediator.getProjectsPanel().saveProject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        this.saveProject.setTooltip(new Tooltip("Update Project in DB"));
        GridPane.setConstraints(saveProject, 1, 1);
        saveFiles.getChildren().add(saveProject);

        Button export2D = new Button(null, new FontIcon("fas-sign-out-alt:15"));
        export2D.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        export2D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    if (file.getName().endsWith(".svg")) {
                        fileChooser.setInitialDirectory(file.getParentFile());
                        PrintWriter writer;
                        try {
                            writer = new PrintWriter(file);
                            writer.println(toSVG(mediator.getDrawingDisplayed().get().getDrawing(), mediator.getCanvas2D().getBounds().width, mediator.getCanvas2D().getBounds().height));
                            writer.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (file.getName().endsWith(".json")) {
                        fileChooser.setInitialDirectory(file.getParentFile());
                        PrintWriter writer;
                        try {
                            writer = new PrintWriter(file);
                            writer.println(toJSON(mediator.getDrawingDisplayed().get().getDrawing()));
                            writer.close();
                            mediator.getChimeraDriver().saveSession(new File(file.getParentFile(), file.getName().split(".")[0]+".py"), new File(file.getParentFile(), file.getName().split(".")[0]+".pdb"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        export2D.setTooltip(new Tooltip("Export 2D to file"));
        GridPane.setConstraints(export2D, 2, 1);
        saveFiles.getChildren().add(export2D);

        GridPane tertiaryStructureButtons = new GridPane();
        tertiaryStructureButtons.setVgap(5.0);
        tertiaryStructureButtons.setHgap(5.0);

        GridPane selectionSize = new GridPane();
        selectionSize.setVgap(5.0);
        selectionSize.setHgap(5.0);

        l = new Label("Selection Width (px)");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        selectionSize.getChildren().add(l);

        final Slider sliderSize = new Slider(1, 3, RnartistConfig.getSelectionWidth());
        sliderSize.setShowTickLabels(true);
        sliderSize.setShowTickMarks(true);
        sliderSize.setSnapToTicks(true);
        sliderSize.setMajorTickUnit(1);
        sliderSize.setMinorTickCount(0);

        sliderSize.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                RnartistConfig.setSelectionWidth((int) sliderSize.getValue());
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setHalignment(sliderSize, HPos.CENTER);
        GridPane.setConstraints(sliderSize, 0, 1);
        selectionSize.getChildren().add(sliderSize);

        GridPane selectionColor = new GridPane();
        selectionColor.setVgap(5.0);
        selectionColor.setHgap(5.0);

        l = new Label("Selection Color");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        selectionColor.getChildren().add(l);

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(awtColorToJavaFX(RnartistConfig.getSelectionColor()));
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observableValue, javafx.scene.paint.Color color, javafx.scene.paint.Color newValue) {
                java.awt.Color c = javaFXToAwt(colorPicker.getValue());
                RnartistConfig.setSelectionColor(c);
                mediator.canvas2D.repaint();
            }
        });
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setMinWidth(Control.USE_PREF_SIZE);
        GridPane.setConstraints(colorPicker, 0, 1);
        selectionColor.getChildren().add(colorPicker);

        GridPane structureSelection = new GridPane();
        structureSelection.setVgap(5.0);
        structureSelection.setHgap(5.0);

        l = new Label("2Ds Available");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        structureSelection.getChildren().add(l);

        this.allStructuresAvailable = new MenuButton("Choose a 2D");

        this.clearAll2DsItem = new MenuItem("Clear All");
        this.clearAll2DsItem.setDisable(true);
        clearAll2DsItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Are you Sure to Remove all the 2Ds from your Project?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    mediator.getDrawingsLoaded().clear();
                    stage.setTitle("RNArtist");
                }
            }
        });

        this.clearAll2DsExceptCurrentItem = new MenuItem("Clear All Except Current");
        this.clearAll2DsExceptCurrentItem.setDisable(true);
        clearAll2DsExceptCurrentItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Are you Sure to Remove all the Remaining 2Ds from your Project?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    List<DrawingLoaded> toDelete = new ArrayList<DrawingLoaded>();
                    for (DrawingLoaded drawingLoaded : mediator.getDrawingsLoaded())
                        if (drawingLoaded.getDrawing() != mediator.getDrawingDisplayed().get().getDrawing())
                            toDelete.add(drawingLoaded);
                    mediator.getDrawingsLoaded().removeAll(toDelete);
                }
            }
        });

        allStructuresAvailable.getItems().addAll(new SeparatorMenuItem(), clearAll2DsItem, clearAll2DsExceptCurrentItem);

        GridPane.setHalignment(allStructuresAvailable, HPos.CENTER);
        GridPane.setConstraints(allStructuresAvailable, 0, 1);
        structureSelection.getChildren().add(allStructuresAvailable);

        Separator s1 = new Separator();
        s1.setPadding(new Insets(0, 5, 0, 5));

        Separator s2 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        Separator s3 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        Separator s4 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        toolbar.getItems().addAll(loadFiles, s1, saveFiles, s2, structureSelection, s3, tertiaryStructureButtons, s4, selectionSize, selectionColor);

        root.setTop(toolbar);

        //++++++ left Toolbar

        GridPane leftToolBar = new GridPane();
        leftToolBar.setAlignment(Pos.TOP_CENTER);
        leftToolBar.setPadding(new Insets(5.0));
        leftToolBar.setVgap(5);
        leftToolBar.setHgap(5);

        int row = 0;

        Separator s = new Separator();
        s.setPadding(new Insets(5, 0, 5, 0));
        s.getStyleClass().add("thick-separator");
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        l = new Label("2D");
        leftToolBar.add(l, 0, row++, 2, 1);
        GridPane.setHalignment(l, HPos.CENTER);

        s = new Separator();
        s.setPadding(new Insets(5, 0, 5, 0));
        s.getStyleClass().add("thick-separator");
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        Button center2D = new Button(null, new FontIcon("fas-crosshairs:15"));
        center2D.setMaxWidth(Double.MAX_VALUE);
        center2D.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        center2D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.isShiftDown()) {
                    setCenterDisplayOnSelection(!isCenterDisplayOnSelection());
                    if (isCenterDisplayOnSelection())
                        center2D.setGraphic(new FontIcon("fas-lock:15"));
                    else
                        center2D.setGraphic(new FontIcon("fas-crosshairs:15"));
                }
                else {
                    java.awt.geom.Rectangle2D selectionFrame = mediator.getCanvas2D().getSelectionFrame();
                    if (selectionFrame == null)
                        mediator.getCanvas2D().centerDisplayOn(mediator.getDrawingDisplayed().get().getDrawing().getFrame());
                    else
                        mediator.getCanvas2D().centerDisplayOn(selectionFrame);
                }
            }
        });
        center2D.setTooltip(new Tooltip("Focus 2D on Selection"));

        Button fit2D = new Button(null, new FontIcon("fas-expand-arrows-alt:15"));
        fit2D.setMaxWidth(Double.MAX_VALUE);
        fit2D.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        fit2D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().fitStructure(mediator.getCanvas2D().getSelectionFrame());
            }
        });
        fit2D.setTooltip(new Tooltip("Fit 2D"));

        leftToolBar.add(center2D, 0, row);
        GridPane.setHalignment(center2D, HPos.CENTER);
        leftToolBar.add(fit2D, 1, row++);
        GridPane.setHalignment(fit2D, HPos.CENTER);

        Button showTertiaries = new Button(null, new FontIcon("fas-eye:15"));
        showTertiaries.setMaxWidth(Double.MAX_VALUE);
        showTertiaries.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        showTertiaries.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.fulldetails, "true");

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    for (TreeItem<ExplorerItem> selectedItem: mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems()) {
                        if (ResidueDrawing.class.isInstance(selectedItem.getValue().getDrawingElement()))
                            starts.add(selectedItem.getParent()); //if the user has selected single residues, this allows to display its tertiary interactions, since a tertiary can be a parent of a residue in the explorer
                        starts.add(selectedItem);
                    }
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }
                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();

            }
        });
        showTertiaries.setTooltip(new Tooltip("Show Tertiaries"));

        Button hideTertiaries = new Button(null, new FontIcon("fas-eye-slash:15"));
        hideTertiaries.setMaxWidth(Double.MAX_VALUE);
        hideTertiaries.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        hideTertiaries.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.fulldetails, "false");
                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }
                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();

            }
        });

        hideTertiaries.setTooltip(new Tooltip("Hide Tertiaries"));

        leftToolBar.add(showTertiaries, 0, row);
        GridPane.setHalignment(showTertiaries, HPos.CENTER);
        leftToolBar.add(hideTertiaries, 1, row++);
        GridPane.setHalignment(hideTertiaries, HPos.CENTER);

        s = new Separator();
        s.setPadding(new Insets(5, 0, 5, 0));
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        Button levelDetails1 = new Button("1");
        levelDetails1.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        levelDetails1.setMaxWidth(Double.MAX_VALUE);
        levelDetails1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "false");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        Button levelDetails2 = new Button("2");
        levelDetails2.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        levelDetails2.setMaxWidth(Double.MAX_VALUE);
        levelDetails2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "false");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        leftToolBar.add(levelDetails1, 0, row);
        GridPane.setHalignment(levelDetails1, HPos.CENTER);
        leftToolBar.add(levelDetails2, 1, row++);
        GridPane.setHalignment(levelDetails2, HPos.CENTER);

        Button levelDetails3 = new Button("3");
        levelDetails3.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        levelDetails3.setMaxWidth(Double.MAX_VALUE);
        levelDetails3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "false");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        Button levelDetails4 = new Button("4");
        levelDetails4.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        levelDetails4.setMaxWidth(Double.MAX_VALUE);
        levelDetails4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "false");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        leftToolBar.add(levelDetails3, 0, row);
        GridPane.setHalignment(levelDetails1, HPos.CENTER);
        leftToolBar.add(levelDetails4, 1, row++);
        GridPane.setHalignment(levelDetails2, HPos.CENTER);

        Button levelDetails5 = new Button("5");
        levelDetails5.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        levelDetails5.setMaxWidth(Double.MAX_VALUE);
        levelDetails5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "true");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        leftToolBar.add(levelDetails5, 0, row++);
        GridPane.setHalignment(levelDetails5, HPos.CENTER);

        s = new Separator();
        s.setPadding(new Insets(5, 0, 5, 0));
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        Button syncColors = new Button(null, new FontIcon("fas-unlock:12"));

        ColorPicker AColorPicker = new ColorPicker();
        AColorPicker.setMaxWidth(Double.MAX_VALUE);
        ColorPicker UColorPicker = new ColorPicker();
        UColorPicker.setMaxWidth(Double.MAX_VALUE);
        ColorPicker GColorPicker = new ColorPicker();
        GColorPicker.setMaxWidth(Double.MAX_VALUE);
        ColorPicker CColorPicker = new ColorPicker();
        CColorPicker.setMaxWidth(Double.MAX_VALUE);

        Button ALabel = new Button("A");
        ALabel.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        ALabel.setMaxWidth(Double.MAX_VALUE);
        ALabel.setUserData("white");
        ALabel.setTextFill(Color.WHITE);
        Button ULabel = new Button("U");
        ULabel.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        ULabel.setMaxWidth(Double.MAX_VALUE);
        ULabel.setUserData("white");
        ULabel.setTextFill(Color.WHITE);
        Button GLabel = new Button("G");
        GLabel.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        GLabel.setUserData("white");
        GLabel.setTextFill(Color.WHITE);
        GLabel.setMaxWidth(Double.MAX_VALUE);
        Button CLabel = new Button("C");
        CLabel.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        CLabel.setMaxWidth(Double.MAX_VALUE);
        CLabel.setUserData("white");
        CLabel.setTextFill(Color.WHITE);

        Button pickColorScheme = new Button(null, new FontIcon("fas-swatchbook:15"));
        pickColorScheme.setMaxWidth(Double.MAX_VALUE);
        pickColorScheme.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));

        Button paintResidues = new Button(null, new FontIcon("fas-fill:15"));
        paintResidues.setMaxWidth(Double.MAX_VALUE);
        paintResidues.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        paintResidues.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        leftToolBar.add(pickColorScheme, 0, row);
        GridPane.setHalignment(pickColorScheme, HPos.CENTER);
        leftToolBar.add(paintResidues, 1, row++);
        GridPane.setHalignment(paintResidues, HPos.CENTER);

        ALabel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Color c = null;
                if (ALabel.getUserData().equals("black")) {
                    ALabel.setUserData("white");
                    c = Color.WHITE;
                    ALabel.setTextFill(Color.WHITE);
                    if ("lock".equals(syncColors.getUserData())) {
                        ULabel.setUserData("white");
                        ULabel.setTextFill(Color.WHITE);
                        GLabel.setUserData("white");
                        GLabel.setTextFill(Color.WHITE);
                        CLabel.setUserData("white");
                        CLabel.setTextFill(Color.WHITE);
                    }
                } else {
                    ALabel.setUserData("black");
                    c = Color.BLACK;
                    ALabel.setTextFill(Color.BLACK);
                    if ("lock".equals(syncColors.getUserData())) {
                        ULabel.setUserData("black");
                        ULabel.setTextFill(Color.BLACK);
                        GLabel.setUserData("black");
                        GLabel.setTextFill(Color.BLACK);
                        CLabel.setUserData("black");
                        CLabel.setTextFill(Color.BLACK);
                    }
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        leftToolBar.add(ALabel, 0, row);
        GridPane.setHalignment(ALabel, HPos.CENTER);
        leftToolBar.add(AColorPicker, 1, row++);
        GridPane.setHalignment(AColorPicker, HPos.CENTER);

        AColorPicker.getStyleClass().add("button");
        AColorPicker.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        AColorPicker.setStyle("-fx-color-label-visible: false ;");
        AColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ALabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));

                if ("lock".equals(syncColors.getUserData())) {
                    GColorPicker.setValue(AColorPicker.getValue());
                    GLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                    UColorPicker.setValue(AColorPicker.getValue());
                    ULabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                    CColorPicker.setValue(AColorPicker.getValue());
                    CLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));

                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        ULabel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Color c = null;
                if (ULabel.getUserData().equals("black")) {
                    ULabel.setUserData("white");
                    c = Color.WHITE;
                    ULabel.setTextFill(Color.WHITE);
                    if ("lock".equals(syncColors.getUserData())) {
                        ALabel.setUserData("white");
                        ALabel.setTextFill(Color.WHITE);
                        GLabel.setUserData("white");
                        GLabel.setTextFill(Color.WHITE);
                        CLabel.setUserData("white");
                        CLabel.setTextFill(Color.WHITE);
                    }
                } else {
                    ULabel.setUserData("black");
                    c = Color.BLACK;
                    ULabel.setTextFill(Color.BLACK);
                    if ("lock".equals(syncColors.getUserData())) {
                        ALabel.setUserData("black");
                        ALabel.setTextFill(Color.BLACK);
                        GLabel.setUserData("black");
                        GLabel.setTextFill(Color.BLACK);
                        CLabel.setUserData("black");
                        CLabel.setTextFill(Color.BLACK);
                    }
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        leftToolBar.add(ULabel, 0, row);
        GridPane.setHalignment(ULabel, HPos.CENTER);
        leftToolBar.add(UColorPicker, 1, row++);
        GridPane.setHalignment(UColorPicker, HPos.CENTER);

        UColorPicker.getStyleClass().add("button");
        UColorPicker.setStyle("-fx-color-label-visible: false ;");
        UColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ULabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));

                if ("lock".equals(syncColors.getUserData())) {
                    AColorPicker.setValue(UColorPicker.getValue());
                    ALabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    GColorPicker.setValue(UColorPicker.getValue());
                    GLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    CColorPicker.setValue(UColorPicker.getValue());
                    CLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        GLabel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Color c = null;
                if (GLabel.getUserData().equals("black")) {
                    GLabel.setUserData("white");
                    c = Color.WHITE;
                    GLabel.setTextFill(Color.WHITE);
                    if ("lock".equals(syncColors.getUserData())) {
                        ULabel.setUserData("white");
                        ULabel.setTextFill(Color.WHITE);
                        ALabel.setUserData("white");
                        ALabel.setTextFill(Color.WHITE);
                        CLabel.setUserData("white");
                        CLabel.setTextFill(Color.WHITE);
                    }
                } else {
                    GLabel.setUserData("black");
                    c = Color.BLACK;
                    GLabel.setTextFill(Color.BLACK);
                    if ("lock".equals(syncColors.getUserData())) {
                        ULabel.setUserData("black");
                        ULabel.setTextFill(Color.BLACK);
                        ALabel.setUserData("black");
                        ALabel.setTextFill(Color.BLACK);
                        CLabel.setUserData("black");
                        CLabel.setTextFill(Color.BLACK);
                    }
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        leftToolBar.add(GLabel, 0, row);
        GridPane.setHalignment(GLabel, HPos.CENTER);
        leftToolBar.add(GColorPicker, 1, row++);
        GridPane.setHalignment(GColorPicker, HPos.CENTER);

        GColorPicker.getStyleClass().add("button");
        GColorPicker.setStyle("-fx-color-label-visible: false ;");
        GColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                GLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));

                if ("lock".equals(syncColors.getUserData())) {
                    AColorPicker.setValue(GColorPicker.getValue());
                    ALabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                    UColorPicker.setValue(GColorPicker.getValue());
                    ULabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                    CColorPicker.setValue(GColorPicker.getValue());
                    CLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        CLabel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Color c = null;
                if (CLabel.getUserData().equals("black")) {
                    CLabel.setUserData("white");
                    c = Color.WHITE;
                    CLabel.setTextFill(Color.WHITE);
                    if ("lock".equals(syncColors.getUserData())) {
                        ULabel.setUserData("white");
                        ULabel.setTextFill(Color.WHITE);
                        GLabel.setUserData("white");
                        GLabel.setTextFill(Color.WHITE);
                        ALabel.setUserData("white");
                        ALabel.setTextFill(Color.WHITE);
                    }
                } else {
                    CLabel.setUserData("black");
                    c = Color.BLACK;
                    CLabel.setTextFill(Color.BLACK);
                    if ("lock".equals(syncColors.getUserData())) {
                        ULabel.setUserData("black");
                        ULabel.setTextFill(Color.BLACK);
                        GLabel.setUserData("black");
                        GLabel.setTextFill(Color.BLACK);
                        ALabel.setUserData("black");
                        ALabel.setTextFill(Color.BLACK);
                    }
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        leftToolBar.add(CLabel, 0, row);
        GridPane.setHalignment(CLabel, HPos.CENTER);
        leftToolBar.add(CColorPicker, 1, row++);
        GridPane.setHalignment(CColorPicker, HPos.CENTER);

        CColorPicker.getStyleClass().add("button");
        CColorPicker.setStyle("-fx-color-label-visible: false ;");
        CColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                CLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));

                if ("lock".equals(syncColors.getUserData())) {
                    AColorPicker.setValue(CColorPicker.getValue());
                    ALabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                    UColorPicker.setValue(CColorPicker.getValue());
                    ULabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                    GColorPicker.setValue(CColorPicker.getValue());
                    GLabel.setStyle("-fx-background-color: "+getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                }

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(CColorPicker.getValue())));
                if ("lock".equals(syncColors.getUserData())) {
                    t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ALabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(AColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(ULabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(UColorPicker.getValue())));
                    t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GLabel.getUserData().equals("black")? Color.BLACK : Color.WHITE)));
                    t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(GColorPicker.getValue())));
                }

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        //++++++++++ we init the color buttons with a random scheme

        Map<String, Map<String, String>> scheme = (Map<String, Map<String, String>>) RnartistConfig.colorSchemes.values().stream().toArray()[new Random().nextInt(RnartistConfig.colorSchemes.size())];

        AColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.AShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
        ALabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.AShape.toString()).get(DrawingConfigurationParameter.color.toString()));
        if (scheme.get(SecondaryStructureType.A.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
            ALabel.setUserData("white");
            ALabel.setTextFill(Color.WHITE);
        } else {
            ALabel.setUserData("black");
            ALabel.setTextFill(Color.BLACK);
        }

        UColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.UShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
        ULabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.UShape.toString()).get(DrawingConfigurationParameter.color.toString()));
        if (scheme.get(SecondaryStructureType.U.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
            ULabel.setUserData("white");
            ULabel.setTextFill(Color.WHITE);
        } else {
            ULabel.setUserData("black");
            ULabel.setTextFill(Color.BLACK);
        }

        GColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.GShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
        GLabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.GShape.toString()).get(DrawingConfigurationParameter.color.toString()));
        if (scheme.get(SecondaryStructureType.G.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
            GLabel.setUserData("white");
            GLabel.setTextFill(Color.WHITE);
        } else {
            GLabel.setUserData("black");
            GLabel.setTextFill(Color.BLACK);
        }

        CColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.CShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
        CLabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.CShape.toString()).get(DrawingConfigurationParameter.color.toString()));
        if (scheme.get(SecondaryStructureType.C.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
            CLabel.setUserData("white");
            CLabel.setTextFill(Color.WHITE);
        } else {
            CLabel.setUserData("black");
            CLabel.setTextFill(Color.BLACK);
        }

        pickColorScheme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                Map<String, Map<String, String>> scheme = (Map<String, Map<String, String>>) RnartistConfig.colorSchemes.values().stream().toArray()[new Random().nextInt(RnartistConfig.colorSchemes.size())];

                AColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.AShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
                ALabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.AShape.toString()).get(DrawingConfigurationParameter.color.toString()));
                if (scheme.get(SecondaryStructureType.A.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
                    ALabel.setUserData("white");
                    ALabel.setTextFill(Color.WHITE);
                } else {
                    ALabel.setUserData("black");
                    ALabel.setTextFill(Color.BLACK);
                }

                UColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.UShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
                ULabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.UShape.toString()).get(DrawingConfigurationParameter.color.toString()));
                if (scheme.get(SecondaryStructureType.U.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
                    ULabel.setUserData("white");
                    ULabel.setTextFill(Color.WHITE);
                } else {
                    ULabel.setUserData("black");
                    ULabel.setTextFill(Color.BLACK);
                }

                GColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.GShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
                GLabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.GShape.toString()).get(DrawingConfigurationParameter.color.toString()));
                if (scheme.get(SecondaryStructureType.G.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
                    GLabel.setUserData("white");
                    GLabel.setTextFill(Color.WHITE);
                } else {
                    GLabel.setUserData("black");
                    GLabel.setTextFill(Color.BLACK);
                }

                CColorPicker.setValue(awtColorToJavaFX(DrawingsKt.getAWTColor(scheme.get(SecondaryStructureType.CShape.toString()).get(DrawingConfigurationParameter.color.toString()), 255)));
                CLabel.setStyle("-fx-background-color: "+scheme.get(SecondaryStructureType.CShape.toString()).get(DrawingConfigurationParameter.color.toString()));
                if (scheme.get(SecondaryStructureType.C.toString()).get(DrawingConfigurationParameter.color.toString()).equals(getHTMLColorString(java.awt.Color.WHITE))) {
                    CLabel.setUserData("white");
                    CLabel.setTextFill(Color.WHITE);
                } else {
                    CLabel.setUserData("black");
                    CLabel.setTextFill(Color.BLACK);
                }
            }
        });

        //++++++++++

        //syncColors.setMaxWidth(Double.MAX_VALUE);
        syncColors.setUserData("unlock");
        syncColors.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        syncColors.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if ("unlock".equals(syncColors.getUserData())) {
                    syncColors.setGraphic(new FontIcon("fas-lock:12"));
                    syncColors.setUserData("lock");
                } else {
                    syncColors.setGraphic(new FontIcon("fas-unlock:12"));
                    syncColors.setUserData("unlock");
                }
            }
        });

        leftToolBar.add(syncColors, 0, row++, 2, 1);
        GridPane.setHalignment(syncColors, HPos.CENTER);

        s = new Separator();
        s.setPadding(new Insets(5, 0, 5, 0));
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        Button lineWidth1 = new Button(null, null);
        lineWidth1.setMaxWidth(Double.MAX_VALUE);
        Line line = new Line(0, 10, 10, 10);
        line.setStrokeWidth(0.25);
        lineWidth1.setGraphic(line);
        lineWidth1.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineWidth1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.linewidth, "0.25");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.linewidth, "0.25");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        Button lineWidth2 = new Button(null, null);
        lineWidth2.setMaxWidth(Double.MAX_VALUE);
        line = new Line(0, 10, 10, 10);
        line.setStrokeWidth(0.5);
        lineWidth2.setGraphic(line);
        lineWidth2.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineWidth2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.linewidth, "0.5");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.linewidth, "0.5");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        leftToolBar.add(lineWidth1, 0, row);
        GridPane.setHalignment(lineWidth1, HPos.CENTER);
        leftToolBar.add(lineWidth2, 1, row++);
        GridPane.setHalignment(lineWidth2, HPos.CENTER);

        Button lineWidth3 = new Button(null, null);
        lineWidth3.setMaxWidth(Double.MAX_VALUE);
        line = new Line(0, 10, 10, 10);
        line.setStrokeWidth(0.75);
        lineWidth3.setGraphic(line);
        lineWidth3.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineWidth3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.linewidth, "0.75");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.linewidth, "0.75");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        Button lineWidth4 = new Button(null, null);
        lineWidth4.setMaxWidth(Double.MAX_VALUE);
        line = new Line(0, 10, 10, 10);
        line.setStrokeWidth(1.0);
        lineWidth4.setGraphic(line);
        lineWidth4.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineWidth4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.linewidth, "1.0");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.linewidth, "1.0");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        leftToolBar.add(lineWidth3, 0, row);
        GridPane.setHalignment(lineWidth3, HPos.CENTER);
        leftToolBar.add(lineWidth4, 1, row++);
        GridPane.setHalignment(lineWidth4, HPos.CENTER);

        Button lineWidth5 = new Button(null, null);
        lineWidth5.setMaxWidth(Double.MAX_VALUE);
        line = new Line(0, 10, 10, 10);
        line.setStrokeWidth(2.0);
        lineWidth5.setGraphic(line);
        lineWidth5.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineWidth5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.linewidth, "2.0");
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.linewidth, "2.0");
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.linewidth, "2.0");
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.linewidth, "2.0");
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.linewidth, "2.0");
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.linewidth, "2.0");
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.linewidth, "2.0");

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });

        ColorPicker lineColorPicker = new ColorPicker(Color.BLACK);
        lineColorPicker.disableProperty().bind(Bindings.when(mediator.getDrawingDisplayed().isNull()).then(true).otherwise(false));
        lineColorPicker.setMaxWidth(Double.MAX_VALUE);
        lineColorPicker.getStyleClass().add("button");
        lineColorPicker.setStyle("-fx-color-label-visible: false ;");
        lineColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                List<TreeItem<ExplorerItem>> hits = new ArrayList<>();
                List<TreeItem<ExplorerItem>> starts = new ArrayList<>();
                byte scope = BRANCH_SCOPE;
                if (mediator.getExplorer().getTreeTableView().getSelectionModel().isEmpty() || mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems().size() == 1 && mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItem() == mediator.getExplorer().getTreeTableView().getRoot() ) {
                    starts.add(mediator.getExplorer().getTreeTableView().getRoot());
                }
                else {
                    starts.addAll(mediator.getExplorer().getTreeTableView().getSelectionModel().getSelectedItems());
                    scope = STRUCTURAL_DOMAIN_SCOPE;
                }

                Theme t = new Theme();
                t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(lineColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(lineColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(lineColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.SecondaryInteraction, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(lineColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.PhosphodiesterBond, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(lineColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.TertiaryInteraction, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(lineColorPicker.getValue())));
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.color, getHTMLColorString(javaFXToAwt(lineColorPicker.getValue())));

                for (TreeItem<ExplorerItem> start:starts)
                    mediator.getExplorer().applyTheme(start, t, scope);

                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        leftToolBar.add(lineWidth5, 0, row);
        GridPane.setHalignment(lineWidth5, HPos.CENTER);
        leftToolBar.add(lineColorPicker, 1, row++);
        GridPane.setHalignment(lineColorPicker, HPos.CENTER);

        s = new Separator();
        s.setPadding(new Insets(10, 0, 5, 0));
        s.getStyleClass().add("thick-separator");
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        l = new Label("3D");
        leftToolBar.add(l, 0, row++, 2, 1);
        GridPane.setHalignment(l, HPos.CENTER);

        s = new Separator();
        s.setPadding(new Insets(5, 0, 5, 0));
        s.getStyleClass().add("thick-separator");
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        this.reload3D = new Button(null, new FontIcon("fas-redo:15"));
        this.reload3D.setDisable(true);
        this.reload3D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getChimeraDriver().reloadTertiaryStructure();
            }
        });
        this.reload3D.setTooltip(new Tooltip("Reload 3D"));

        this.focus3D = new Button(null, new FontIcon("fas-crosshairs:15"));
        this.focus3D.setDisable(true);
        this.focus3D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.focusInChimera();
            }
        });
        this.focus3D.setTooltip(new Tooltip("Focus 3D on Selection"));

        leftToolBar.add(this.reload3D, 0, row);
        GridPane.setHalignment(this.reload3D, HPos.CENTER);
        leftToolBar.add(this.focus3D, 1, row++);
        GridPane.setHalignment(this.focus3D, HPos.CENTER);

        this.paintSelectionin3D = new Button(null, new FontIcon("fas-fill:15"));
        this.paintSelectionin3D.setDisable(true);
        this.paintSelectionin3D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getChimeraDriver().color3D(mediator.canvas2D.getSelectedResidues());
            }
        });
        this.paintSelectionin3D.setTooltip(new Tooltip("Paint 3D selection"));

        leftToolBar.add(this.paintSelectionin3D, 0, row++);
        GridPane.setHalignment(this.paintSelectionin3D, HPos.CENTER);

        s = new Separator();
        s.setPadding(new Insets(5, 0, 5, 0));
        leftToolBar.add(s, 0, row++, 2, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        this.paintSelectionAsCartoon = new Button("SC", null);
        this.paintSelectionAsCartoon.setDisable(true);
        this.paintSelectionAsCartoon.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getChimeraDriver().represent(ChimeraDriver.CARTOON, mediator.getCanvas2D().getSelectedPositions());
            }
        });
        this.paintSelectionAsCartoon.setTooltip(new Tooltip("Selection as Cartoon"));

        this.paintSelectionAsStick = new Button("SS", null);
        this.paintSelectionAsStick.setDisable(true);
        this.paintSelectionAsStick.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getChimeraDriver().represent(ChimeraDriver.STICK, mediator.getCanvas2D().getSelectedPositions());
            }
        });
        this.paintSelectionAsStick.setTooltip(new Tooltip("Selection as Stick"));

        leftToolBar.add(this.paintSelectionAsCartoon, 0, row);
        GridPane.setHalignment(this.paintSelectionAsCartoon, HPos.CENTER);
        leftToolBar.add(this.paintSelectionAsStick, 1, row++);
        GridPane.setHalignment(this.paintSelectionAsStick, HPos.CENTER);

        this.showRibbon = new Button("SR", null);
        this.showRibbon.setDisable(true);
        this.showRibbon.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getChimeraDriver().represent(ChimeraDriver.RIBBON_SHOW, mediator.getCanvas2D().getSelectedPositions());
            }
        });
        this.showRibbon.setTooltip(new Tooltip("Show Ribbon"));

        this.hideRibbon = new Button("HR", null);
        this.hideRibbon.setDisable(true);
        this.hideRibbon.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getChimeraDriver().represent(ChimeraDriver.RIBBON_HIDE, mediator.getCanvas2D().getSelectedPositions());
            }
        });
        this.hideRibbon.setTooltip(new Tooltip("Hide Ribbon"));

        leftToolBar.add(this.showRibbon, 0, row);
        GridPane.setHalignment(this.showRibbon, HPos.CENTER);
        leftToolBar.add(this.hideRibbon, 1, row++);
        GridPane.setHalignment(this.hideRibbon, HPos.CENTER);

        root.setLeft(leftToolBar);

        //++++++ Canvas2D
        final SwingNode swingNode = new SwingNode();
        swingNode.setOnMouseMoved(mouseEvent -> {
            if (mediator.getDrawingDisplayed().isNotNull().get())
                mediator.getDrawingDisplayed().get().getDrawing().setQuickDraw(false); //a trick if after the scroll event the quickdraw is still true
        });
        swingNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mediator.getDrawingDisplayed().isNotNull().get() && !mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().is_screen_capture()) {
                AffineTransform at = new AffineTransform();
                at.translate(mediator.getViewX(), mediator.getViewY());
                at.scale(mediator.getZoomLevel(), mediator.getZoomLevel());
                for (JunctionKnob knob : mediator.getDrawingDisplayed().get().getKnobs()) {
                    if (knob.contains(mouseEvent.getX(), mouseEvent.getY(), at))
                        return;
                }
                if (mediator.getDrawingDisplayed().isNotNull().get()) {

                    for (HelixDrawing h: mediator.getWorkingSession().getHelicesDrawn()) {
                        Shape shape = h.getSelectionFrame();
                        if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            for (ResidueDrawing r:h.getResidues()) {
                                shape = r.getSelectionFrame();
                                if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                    if (!mediator.getCanvas2D().isSelected(r) && !mediator.getCanvas2D().isSelected(r.getParent()) && !mediator.getCanvas2D().isSelected(r.getParent().getParent())  ) {
                                        mediator.getCanvas2D().addToSelection(r);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent()) && !mediator.getCanvas2D().isSelected(r.getParent().getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r);
                                        mediator.getCanvas2D().addToSelection(r.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent().getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r.getParent());
                                        mediator.getCanvas2D().addToSelection(r.getParent().getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    }
                                }
                            }
                            for (SecondaryInteractionDrawing interaction:h.getSecondaryInteractions()) {
                                shape = interaction.getSelectionFrame();
                                if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                    if (!mediator.getCanvas2D().isSelected(interaction) && !mediator.getCanvas2D().isSelected(interaction.getParent())) {
                                        mediator.getCanvas2D().addToSelection(interaction);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(interaction.getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(interaction);
                                        mediator.getCanvas2D().addToSelection(interaction.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    }
                                }
                            }
                            if (!mediator.getCanvas2D().isSelected(h)) {
                                mediator.getCanvas2D().addToSelection(h);
                                mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                return;
                            } else {
                                DrawingElement p = h.getParent();
                                while (p != null && mediator.getCanvas2D().isSelected(p)) {
                                    p = p.getParent();
                                }
                                if (p == null) {
                                    mediator.getCanvas2D().addToSelection(h);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement ->drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                } else {
                                    mediator.getCanvas2D().addToSelection(p);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                }
                                return;
                            }
                        }
                    }

                    for (JunctionDrawing j:mediator.getWorkingSession().getJunctionsDrawn()) {
                        Shape shape = j.getSelectionFrame();
                        if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            for (ResidueDrawing r:j.getResidues()) {
                                shape = r.getSelectionFrame();
                                if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                    if (!mediator.getCanvas2D().isSelected(r) && !mediator.getCanvas2D().isSelected(r.getParent())) {
                                        mediator.getCanvas2D().addToSelection(r);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r);
                                        mediator.getCanvas2D().addToSelection(r.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    }
                                }
                            }
                            if (!mediator.getCanvas2D().isSelected(j)) {
                                mediator.getCanvas2D().addToSelection(j);
                                mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                return;
                            } else {
                                DrawingElement p = j.getParent();
                                while (p != null && mediator.getCanvas2D().isSelected(p)) {
                                    p = p.getParent();
                                }
                                if (p == null) {
                                    mediator.getCanvas2D().addToSelection(j);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement !=null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                } else {
                                    mediator.getCanvas2D().addToSelection(p);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement !=null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                }
                                return;
                            }
                        }
                    }

                    for (SingleStrandDrawing ss: mediator.getWorkingSession().getSingleStrandsDrawn()) {
                        Shape shape = ss.getSelectionFrame();
                        if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            for (ResidueDrawing r:ss.getResidues()) {
                                shape = r.getSelectionFrame();
                                if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                    if (!mediator.getCanvas2D().isSelected(r) && !mediator.getCanvas2D().isSelected(r.getParent())) {
                                        mediator.getCanvas2D().addToSelection(r);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r);
                                        mediator.getCanvas2D().addToSelection(r.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isSelected(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    if (mouseEvent.getClickCount() == 2) {
                        //no selection
                        mediator.getCanvas2D().clearSelection();
                        mediator.getExplorer().clearSelection();
                    }
                }
            }
        });
        swingNode.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getDrawingDisplayed().isNotNull().get()) {
                if (mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().is_screen_capture()) {
                    double transX = mouseEvent.getX() - mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().getScreen_capture_transX();
                    double transY = mouseEvent.getY() - mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().getScreen_capture_transY();
                    mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().setScreen_capture_transX(mouseEvent.getX());
                    mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().setScreen_capture_transY(mouseEvent.getY());
                    AffineTransform at = new AffineTransform();
                    at.translate(transX, transY);
                    mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().setScreen_capture_area(at.createTransformedShape(mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().getScreen_capture_area()).getBounds());
                }
                else {
                    mediator.getDrawingDisplayed().get().getDrawing().setQuickDraw(true);
                    double transX = mouseEvent.getX() - mediator.getCanvas2D().getTranslateX();
                    double transY = mouseEvent.getY() - mediator.getCanvas2D().getTranslateY();
                    mediator.getWorkingSession().moveView(transX, transY);
                    mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                    mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
                }
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getDrawingDisplayed().isNotNull().get()) {
                if (mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().is_screen_capture()) {
                    mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().setScreen_capture_transX(0.0);
                    mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().setScreen_capture_transY(0.0);
                } else {
                    mediator.getDrawingDisplayed().get().getDrawing().setQuickDraw(false);
                    mediator.getCanvas2D().setTranslateX(0.0);
                    mediator.getCanvas2D().setTranslateY(0.0);
                }
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getDrawingDisplayed().isNotNull().get()) {
                if (mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().is_screen_capture()) {
                    mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().setScreen_capture_transX(mouseEvent.getX());
                    mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().setScreen_capture_transY(mouseEvent.getY());
                } else {
                    mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                    mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
                }
            }
        });
        swingNode.setOnScroll(scrollEvent -> {
            if (mediator.getDrawingDisplayed().isNotNull().get() && !mediator.getDrawingDisplayed().get().getDrawing().getWorkingSession().is_screen_capture()) {
                mediator.getDrawingDisplayed().get().getDrawing().setQuickDraw(true);
                scrollCounter++;
                Thread th = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        if (scrollCounter == 1) {
                            mediator.getDrawingDisplayed().get().getDrawing().setQuickDraw(false);
                            mediator.getCanvas2D().repaint();
                        }
                        scrollCounter--;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
                th.setDaemon(true);
                th.start();
                Point2D.Double realMouse = new Point2D.Double(((double) scrollEvent.getX() - mediator.getViewX()) / mediator.getZoomLevel(), ((double) scrollEvent.getY() - mediator.getViewY()) / mediator.getZoomLevel());
                double notches = scrollEvent.getDeltaY();
                if (notches < 0)
                    mediator.getWorkingSession().setZoom(1.25);
                if (notches > 0)
                    mediator.getWorkingSession().setZoom(1.0 / 1.25);
                Point2D.Double newRealMouse = new Point2D.Double(((double) scrollEvent.getX() - mediator.getViewX()) / mediator.getZoomLevel(), ((double) scrollEvent.getY() - mediator.getViewY()) / mediator.getZoomLevel());
                mediator.getWorkingSession().moveView((newRealMouse.getX() - realMouse.getX()) * mediator.getZoomLevel(), (newRealMouse.getY() - realMouse.getY()) * mediator.getZoomLevel());
                mediator.getCanvas2D().repaint();
            }
        });
        createSwingContent(swingNode);
        root.setCenter(swingNode);

        //### Status Bar
        this.statusBar = new FlowPane();
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setHgap(20);

        Label release = new Label(RnartistConfig.getRnartistRelease());
        statusBar.getChildren().add(release);

        boolean status = RnartistConfig.isDockerInstalled() && RnartistConfig.isAssemble2DockerImageInstalled();
        Label dockerStatus = new Label(null,  status ? new FontIcon("fas-check-circle:15") : new FontIcon("fas-exclamation-circle:15"));
        dockerStatus.setTooltip(new Tooltip(status ? "I found RNAVIEW! You can open PDB files" : "RNAVIEW not found! You cannot open PDB files!"));
        if (status)
            ((FontIcon)dockerStatus.getGraphic()).setFill(Color.GREEN);
        else
            ((FontIcon)dockerStatus.getGraphic()).setFill(Color.RED);
        Timeline checkDockerStatus = new Timeline(new KeyFrame(Duration.seconds(30),
                new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        boolean status = RnartistConfig.isDockerInstalled() && RnartistConfig.isAssemble2DockerImageInstalled();
                        dockerStatus.setGraphic(status ? new FontIcon("fas-check-circle:15") : new FontIcon("fas-exclamation-circle:15"));
                        dockerStatus.setTooltip(new Tooltip(status ? "I found RNAVIEW! You can open PDB files" : "RNAVIEW not found! You cannot open PDB files!"));
                        if (status)
                            ((FontIcon)dockerStatus.getGraphic()).setFill(Color.GREEN);
                        else
                            ((FontIcon)dockerStatus.getGraphic()).setFill(Color.RED);
                    }
                }));
        checkDockerStatus.setCycleCount(Timeline.INDEFINITE);
        checkDockerStatus.play();

        statusBar.getChildren().add(dockerStatus);

        Button shutdown = new Button(null, new FontIcon("fas-power-off:15"));
        shutdown.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(RNArtist.this.stage);
            alert.initModality(Modality.WINDOW_MODAL);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure to exit RNArtist?");

            Stage alerttStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alerttStage.setAlwaysOnTop(true);
            alerttStage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    Platform.exit();
                    RnartistConfig.save();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                actionEvent.consume();
            }
        });
        statusBar.getChildren().add(shutdown);

        FlowPane windowsBar = new FlowPane();
        windowsBar.setAlignment(Pos.CENTER_LEFT);
        windowsBar.setPadding(new Insets(5, 10, 5, 10));
        windowsBar.setHgap(10);

        Button settings = new Button(null, new FontIcon("fas-cog:15"));
        settings.setOnAction(actionEvent -> {
            mediator.getSettings().getStage().show();
            mediator.getSettings().getStage().toFront();
        });
        windowsBar.getChildren().add(settings);

        Button explorer = new Button(null, new FontIcon("fas-th-list:15"));
        explorer.setOnAction(actionEvent -> {
            mediator.getExplorer().getStage().show();
            mediator.getExplorer().getStage().toFront();
        });
        windowsBar.getChildren().add(explorer);

        Button browser = new Button(null, new FontIcon("fas-globe-europe:15"));
        browser.setOnAction(actionEvent -> {
            mediator.getWebBrowser().getStage().show();
            mediator.getWebBrowser().getStage().toFront();
        });
        windowsBar.getChildren().add(browser);

        GridPane bar = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        bar.getColumnConstraints().addAll(cc, new ColumnConstraints());

        bar.add(windowsBar, 0,0);
        GridPane.setFillWidth(windowsBar, true);
        GridPane.setHalignment(windowsBar, HPos.LEFT);
        bar.add(this.statusBar, 1,0);
        GridPane.setHalignment(this.statusBar, HPos.RIGHT);

        root.setBottom(bar);
        Scene scene = new Scene(root, screen.getBounds().getWidth(), screen.getBounds().getHeight());
        stage.setScene(scene);
        stage.setTitle("RNArtist");

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        int width = (int) (screenSize.getWidth() * 4.0 / 10.0);
        scene.getWindow().setWidth(screenSize.getWidth() - width);
        scene.getWindow().setHeight(screenSize.getHeight());
        scene.getWindow().setX(0);
        scene.getWindow().setY(0);

        scene.getStylesheets().add("io/github/fjossinet/rnartist/gui/css/main.css");

        new SplashWindow(this.mediator);

    }

    public boolean isCenterDisplayOnSelection() {
        return centerDisplayOnSelection;
    }

    public void setCenterDisplayOnSelection(boolean centerDisplayOnSelection) {
        this.centerDisplayOnSelection = centerDisplayOnSelection;
    }

    public MenuItem getClearAll2DsItem() {
        return clearAll2DsItem;
    }

    public MenuItem getClearAll2DsExceptCurrentItem() {
        return clearAll2DsExceptCurrentItem;
    }

    public MenuButton getAllStructuresAvailableMenu() {
        return this.allStructuresAvailable;
    }

    public void showStatusBar(boolean show) {
        if (show)
            ((BorderPane) stage.getScene().getRoot()).setBottom(this.statusBar);
        else
            ((BorderPane) stage.getScene().getRoot()).setBottom(null);
    }

    public Stage getStage() {
        return stage;
    }

    public Button getSaveProjectButton() {
        return saveProject;
    }

    public Button getFocus3DButton() {
        return focus3D;
    }

    public Button getReload3D() {
        return reload3D;
    }

    public Button getPaintSelectionin3D() {
        return paintSelectionin3D;
    }

    public Button getPaintSelectionAsCartoon() {
        return paintSelectionAsCartoon;
    }

    public Button getPaintSelectionAsStick() {
        return paintSelectionAsStick;
    }

    public Button getShowRibbon() {
        return showRibbon;
    }

    public Button getHideRibbon() {
        return hideRibbon;
    }

    public Button getPaintSelectionin3DButton() {
        return paintSelectionin3D;
    }

    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Canvas2D canvas = new Canvas2D(mediator);
                swingNode.setContent(canvas);
            }
        });
    }

}
