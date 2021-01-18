package io.github.fjossinet.rnartist;

import io.github.fjossinet.rnartist.gui.*;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.core.model.io.Rnaview;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.*;
import static io.github.fjossinet.rnartist.io.UtilsKt.awtColorToJavaFX;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;

public class RNArtist extends Application {

    public static final byte ELEMENT_SCOPE = 0, STRUCTURAL_DOMAIN_NO_TERTIARIES_SCOPE = 1,STRUCTURAL_DOMAIN_SCOPE = 2, BRANCH_SCOPE = 3;
    private Mediator mediator;
    private Stage stage;
    private int scrollCounter = 0;
    private FlowPane statusBar;
    private MenuButton allStructuresAvailable;
    private MenuItem updateSavedThemeItem, clearAll2DsItem, clearAll2DsExceptCurrentItem;

    //user defined global configurations
    private Slider detailsLevel;
    private boolean centerDisplayOnSelection = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
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

        //++++++ Toolbar
        ToolBar toolbar = new ToolBar();
        toolbar.setPadding(new Insets(5, 5, 5, 5));

        GridPane files = new GridPane();
        files.setVgap(5.0);
        files.setHgap(5.0);

        Label l = new Label("Load");
        GridPane.setConstraints(l, 0, 0);
        files.getChildren().add(l);

        Button loadData = new Button(null, new FontIcon("fas-sign-in-alt:15"));
        loadData.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FileChooser fileChooser = new FileChooser();
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                if (files != null) {
                    for (File file : files) {
                        fileChooser.setInitialDirectory(file.getParentFile());
                        javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>, Exception>> loadData = new javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>, Exception>>() {

                            @Override
                            protected Pair<List<SecondaryStructureDrawing>, Exception> call() {
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

                                    } else if (file.getName().endsWith(".pdb")) {
                                        if (! (RnartistConfig.isDockerInstalled() && RnartistConfig.isAssemble2DockerImageInstalled())) {
                                            throw new Exception("You cannot use PDB files, it seems that RNArtist cannot find the RNAVIEW algorithm on your computer.\n Possible causes:\n- the tool Docker is not installed\n- the tool Docker is not running\n- the docker image fjossinet/assemble2 is not installed");
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
                                    return Pair.of(secondaryStructureDrawings, e);
                                }
                                return Pair.of(secondaryStructureDrawings, null);
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
                                        mediator.getProjectsPanel().currentProject().set(null);
                                        for (SecondaryStructureDrawing drawing : loadData.get().getLeft())
                                            mediator.get_2DDrawingsLoaded().add(drawing);
                                        //we load and fit (only if not a drawing from a JSON file) on the last 2D loaded
                                        mediator.canvas2D.load2D(mediator.get_2DDrawingsLoaded().get(mediator.get_2DDrawingsLoaded().size() - 1), null, null);
                                        if (mediator.getViewX() == 0.0 && mediator.getViewY() == 0.0 && mediator.getZoomLevel() == 1.0)//this test allows to detect JSON files exported from RNArtist with a focus on a region
                                            mediator.canvas2D.fitStructure();
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
        GridPane.setConstraints(loadData, 1, 0);
        files.getChildren().add(loadData);

        Button loadProject = new Button(null, new FontIcon("fas-grip-horizontal:15"));
        loadProject.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getProjectsPanel().getStage().show();
                mediator.getProjectsPanel().getStage().toFront();
            }
        });
        loadProject.setTooltip(new Tooltip("Load Project"));
        GridPane.setConstraints(loadProject, 2, 0);
        files.getChildren().add(loadProject);

        l = new Label("Save");
        GridPane.setConstraints(l, 0, 1);
        files.getChildren().add(l);

        Button saveProjectAs = new Button(null, new FontIcon("fas-database:15"));
        saveProjectAs.disableProperty().bind(Bindings.when(mediator.getSecondaryStructureDrawingProperty().isNull()).then(true).otherwise(false));
        saveProjectAs.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mediator.getSecondaryStructureDrawingProperty().isNotNull().get()) {
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
                            mediator.getProjectsPanel().saveProjectAs(projectName.get().trim(), mediator.getCanvas2D().screenCapture(null));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mediator.getWorkingSession().set_screen_capture(false);
                        mediator.getWorkingSession().setScreen_capture_area(null);
                        mediator.getCanvas2D().repaint();
                    } else {
                        mediator.getWorkingSession().set_screen_capture(false);
                        mediator.getWorkingSession().setScreen_capture_area(null);
                        mediator.getCanvas2D().repaint();
                    }
                }
            }
        });
        saveProjectAs.setTooltip(new Tooltip("Save Project As..."));
        GridPane.setConstraints(saveProjectAs, 1, 1);
        files.getChildren().add(saveProjectAs);

        Button saveProject = new Button(null, new FontIcon("fas-sync:15"));
        saveProject.disableProperty().bind(Bindings.when(mediator.getProjectsPanel().currentProject().isNull()).then(true).otherwise(false));
        saveProject.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    mediator.getProjectsPanel().saveProject();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveProject.setTooltip(new Tooltip("Update Project in DB"));
        GridPane.setConstraints(saveProject, 2, 1);
        files.getChildren().add(saveProject);

        Button export2D = new Button(null, new FontIcon("fas-sign-out-alt:15"));
        export2D.disableProperty().bind(Bindings.when(mediator.getSecondaryStructureDrawingProperty().isNull()).then(true).otherwise(false));
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
                            writer.println(toSVG(mediator.getSecondaryStructureDrawingProperty().get(), mediator.getCanvas2D().getBounds().width, mediator.getCanvas2D().getBounds().height));
                            writer.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else if (file.getName().endsWith(".json")) {
                        fileChooser.setInitialDirectory(file.getParentFile());
                        PrintWriter writer;
                        try {
                            writer = new PrintWriter(file);
                            writer.println(toJSON(mediator.getSecondaryStructureDrawingProperty().get()));
                            writer.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        export2D.setTooltip(new Tooltip("Export 2D to file"));
        GridPane.setConstraints(export2D, 3, 1);
        files.getChildren().add(export2D);

        GridPane _2D3DButtons = new GridPane();
        _2D3DButtons.setVgap(5.0);
        _2D3DButtons.setHgap(5.0);

        l = new Label("2D");
        GridPane.setConstraints(l, 0, 0);
        _2D3DButtons.getChildren().add(l);

        Button center2D = new Button(null, new FontIcon("fas-crosshairs:15"));
        center2D.disableProperty().bind(Bindings.when(mediator.getSecondaryStructureDrawingProperty().isNull()).then(true).otherwise(false));
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
                        mediator.getCanvas2D().centerDisplayOn(mediator.getSecondaryStructureDrawingProperty().get().getFrame());
                    else
                        mediator.getCanvas2D().centerDisplayOn(selectionFrame);
                    mediator.getChimeraDriver().setFocus(mediator.getCanvas2D().getSelectionAbsPositions(), mediator.getSecondaryStructureDrawingProperty().get().getSecondaryStructure().getRna().getName());
                }
            }
        });
        center2D.setTooltip(new Tooltip("Focus 2D on Selection"));
        GridPane.setConstraints(center2D, 1, 0);
        _2D3DButtons.getChildren().add(center2D);

        Button fit2D = new Button(null, new FontIcon("fas-expand-arrows-alt:15"));
        fit2D.disableProperty().bind(Bindings.when(mediator.getSecondaryStructureDrawingProperty().isNull()).then(true).otherwise(false));
        fit2D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().fitStructure();
            }
        });
        fit2D.setTooltip(new Tooltip("Fit 2D"));
        GridPane.setConstraints(fit2D, 2, 0);
        _2D3DButtons.getChildren().add(fit2D);

        l = new Label("3D");
        GridPane.setConstraints(l, 0, 1);
        _2D3DButtons.getChildren().add(l);

        Button focus3D = new Button(null, new FontIcon("fas-crosshairs:15"));
        focus3D.disableProperty().bind(Bindings.when(mediator.getSecondaryStructureDrawingProperty().isNull()).then(true).otherwise(false));
        focus3D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.focusInChimera();
            }
        });
        focus3D.setTooltip(new Tooltip("Focus 3D on Selection"));
        GridPane.setConstraints(focus3D, 1, 1);
        _2D3DButtons.getChildren().add(focus3D);

        Button set3DPivot = new Button(null, new FontIcon("fas-thumbtack:15"));
        set3DPivot.disableProperty().bind(Bindings.when(mediator.getSecondaryStructureDrawingProperty().isNull()).then(true).otherwise(false));
        set3DPivot.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.pivotInChimera();
            }
        });
        set3DPivot.setTooltip(new Tooltip("Define Selection as Pivot"));
        GridPane.setConstraints(set3DPivot, 2, 1);
        _2D3DButtons.getChildren().add(set3DPivot);

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
                if (result.get() == ButtonType.OK)
                    mediator.get_2DDrawingsLoaded().clear();
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
                    List<SecondaryStructureDrawing> toDelete = new ArrayList<SecondaryStructureDrawing>();
                    for (SecondaryStructureDrawing drawing : mediator.get_2DDrawingsLoaded())
                        if (drawing != mediator.getSecondaryStructureDrawingProperty().get())
                            toDelete.add(drawing);
                    mediator.get_2DDrawingsLoaded().removeAll(toDelete);
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

        toolbar.getItems().addAll(files, s1, _2D3DButtons, s2, selectionSize, selectionColor, s3, structureSelection);

        root.setTop(toolbar);

        //++++++ Canvas2D
        final SwingNode swingNode = new SwingNode();
        swingNode.setOnMouseMoved(mouseEvent -> {
            if (mediator.getSecondaryStructureDrawingProperty().isNotNull().get())
                mediator.getSecondaryStructureDrawingProperty().get().setQuickDraw(false); //a trick if after the scroll event the quickdraw is still true
        });
        swingNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mediator.getSecondaryStructureDrawingProperty().isNotNull().get() && !mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().is_screen_capture()) {
                AffineTransform at = new AffineTransform();
                at.translate(mediator.getViewX(), mediator.getViewY());
                at.scale(mediator.getZoomLevel(), mediator.getZoomLevel());
                for (JunctionKnob knob : mediator.getCanvas2D().getKnobs()) {
                    if (knob.contains(mouseEvent.getX(), mouseEvent.getY(), at))
                        return;
                }
                if (mediator.getSecondaryStructureDrawingProperty().isNotNull().get()) {
                    for (JunctionDrawing j:mediator.getWorkingSession().getJunctionsDrawn()) {
                        Shape shape = j.getSelectionFrame();
                        if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            for (ResidueDrawing r:j.getResidues()) {
                                shape = r.getSelectionFrame();
                                if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                    if (!mediator.getCanvas2D().isSelected(r) && !mediator.getCanvas2D().isSelected(r.getParent())) {
                                        mediator.getCanvas2D().addToSelection(r);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r);
                                        mediator.getCanvas2D().addToSelection(r.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    }
                                }
                            }
                            if (!mediator.getCanvas2D().isSelected(j)) {
                                mediator.getCanvas2D().addToSelection(j);
                                mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                return;
                            } else {
                                DrawingElement p = j.getParent();
                                while (p != null && mediator.getCanvas2D().isSelected(p)) {
                                    p = p.getParent();
                                }
                                if (p == null) {
                                    mediator.getCanvas2D().addToSelection(j);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement !=null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                } else {
                                    mediator.getCanvas2D().addToSelection(p);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement !=null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                }
                                return;
                            }
                        }
                    }

                    for (HelixDrawing h: mediator.getWorkingSession().getHelicesDrawn()) {
                        Shape shape = h.getSelectionFrame();
                        if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            for (ResidueDrawing r:h.getResidues()) {
                                shape = r.getSelectionFrame();
                                if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                    if (!mediator.getCanvas2D().isSelected(r) && !mediator.getCanvas2D().isSelected(r.getParent()) && !mediator.getCanvas2D().isSelected(r.getParent().getParent())  ) {
                                        mediator.getCanvas2D().addToSelection(r);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent()) && !mediator.getCanvas2D().isSelected(r.getParent().getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r);
                                        mediator.getCanvas2D().addToSelection(r.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent().getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r.getParent());
                                        mediator.getCanvas2D().addToSelection(r.getParent().getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    }
                                }
                            }
                            for (SecondaryInteractionDrawing interaction:h.getSecondaryInteractions()) {
                                shape = interaction.getSelectionFrame();
                                if (shape != null && at.createTransformedShape(shape).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                    if (!mediator.getCanvas2D().isSelected(interaction) && !mediator.getCanvas2D().isSelected(interaction.getParent())) {
                                        mediator.getCanvas2D().addToSelection(interaction);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(interaction.getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(interaction);
                                        mediator.getCanvas2D().addToSelection(interaction.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    }
                                }
                            }
                            if (!mediator.getCanvas2D().isSelected(h)) {
                                mediator.getCanvas2D().addToSelection(h);
                                mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                return;
                            } else {
                                DrawingElement p = h.getParent();
                                while (p != null && mediator.getCanvas2D().isSelected(p)) {
                                    p = p.getParent();
                                }
                                if (p == null) {
                                    mediator.getCanvas2D().addToSelection(h);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement ->drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                } else {
                                    mediator.getCanvas2D().addToSelection(p);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
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
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
                                        return;
                                    } else if (!mediator.getCanvas2D().isSelected(r.getParent())) {
                                        mediator.getCanvas2D().removeFromSelection(r);
                                        mediator.getCanvas2D().addToSelection(r.getParent());
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> drawingElement != null && mediator.canvas2D.isInSelection(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false, RNArtist.BRANCH_SCOPE);
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
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getSecondaryStructureDrawingProperty().isNotNull().get()) {
                if (mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().is_screen_capture()) {
                    double transX = mouseEvent.getX() - mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().getScreen_capture_transX();
                    double transY = mouseEvent.getY() - mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().getScreen_capture_transY();
                    mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().setScreen_capture_transX(mouseEvent.getX());
                    mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().setScreen_capture_transY(mouseEvent.getY());
                    AffineTransform at = new AffineTransform();
                    at.translate(transX, transY);
                    mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().setScreen_capture_area(at.createTransformedShape(mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().getScreen_capture_area()).getBounds());
                }
                else {
                    mediator.getSecondaryStructureDrawingProperty().get().setQuickDraw(true);
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
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getSecondaryStructureDrawingProperty().isNotNull().get()) {
                if (mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().is_screen_capture()) {
                    mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().setScreen_capture_transX(0.0);
                    mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().setScreen_capture_transY(0.0);
                } else {
                    mediator.getSecondaryStructureDrawingProperty().get().setQuickDraw(false);
                    mediator.getCanvas2D().setTranslateX(0.0);
                    mediator.getCanvas2D().setTranslateY(0.0);
                }
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getSecondaryStructureDrawingProperty().isNotNull().get()) {
                if (mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().is_screen_capture()) {
                    mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().setScreen_capture_transX(mouseEvent.getX());
                    mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().setScreen_capture_transY(mouseEvent.getY());
                } else {
                    mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                    mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
                }
            }
        });
        swingNode.setOnScroll(scrollEvent -> {
            if (mediator.getSecondaryStructureDrawingProperty().isNotNull().get() && !mediator.getSecondaryStructureDrawingProperty().get().getWorkingSession().is_screen_capture()) {
                mediator.getSecondaryStructureDrawingProperty().get().setQuickDraw(true);
                scrollCounter++;
                Thread th = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        if (scrollCounter == 1) {
                            mediator.getSecondaryStructureDrawingProperty().get().setQuickDraw(false);
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
