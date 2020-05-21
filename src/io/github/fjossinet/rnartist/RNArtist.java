package io.github.fjossinet.rnartist;

import io.github.fjossinet.rnartist.gui.Canvas2D;
import io.github.fjossinet.rnartist.gui.RegisterDialog;
import io.github.fjossinet.rnartist.io.ChimeraDriver;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.core.model.io.Rnaview;
import io.github.fjossinet.rnartist.model.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.dizitart.no2.NitriteId;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class RNArtist extends Application {

    private Mediator mediator;
    private Stage stage;
    private int scrollCounter = 0;
    private Button saveAs, save;
    private VBox topToolBars;
    private FlowPane statusBar;
    private ChoiceBox<SecondaryStructureDrawing> allStructuresChoices;
    private TreeTableView<StructuralItem> explorer;

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
                    RnartistConfig.save(null);
                    Platform.exit();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                windowEvent.consume();
            }
        });
        RnartistConfig.load();
        if (RnartistConfig.getUserID() == null)
            new RegisterDialog(this);
        this.mediator = new Mediator(this);
        RnartistConfig.save(null);
        final SwingNode swingNode = new SwingNode();
        swingNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.isControlDown()) {
                if (mediator.getSecondaryStructureDrawing() != null) {
                    AffineTransform at = new AffineTransform();
                    at.translate(mediator.getWorkingSession().getViewX(), mediator.getWorkingSession().getViewY());
                    at.scale(mediator.getWorkingSession().getFinalZoomLevel(), mediator.getWorkingSession().getFinalZoomLevel());
                    for (JunctionCircle jc : mediator.getSecondaryStructureDrawing().getAllJunctions()) {
                        if (jc.getCircle() != null && at.createTransformedShape(jc.getCircle()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            mediator.addToSelection(Mediator.SelectionEmitter.CANVAS2D, true, jc);
                            mediator.getCanvas2D().repaint();
                            return;
                        }
                    }
                    List<ResidueCircle> residues = mediator.getSecondaryStructureDrawing().getResidues();
                    for (ResidueCircle c : residues) {
                        if (c.getCircle() != null && at.createTransformedShape(c.getCircle()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            mediator.addToSelection(Mediator.SelectionEmitter.CANVAS2D, true, c);
                            mediator.getCanvas2D().repaint();
                            return;
                        }
                    }
                    //no hit found
                    mediator.addToSelection(Mediator.SelectionEmitter.CANVAS2D, true, null);
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        swingNode.setOnMouseDragged(mouseEvent -> {
            if (mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getTheme().setQuickDraw(true);
                double transX = mouseEvent.getX() - mediator.getCanvas2D().getTranslateX();
                double transY = mouseEvent.getY() - mediator.getCanvas2D().getTranslateY();
                mediator.getWorkingSession().moveView(transX, transY);
                mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMouseReleased(mouseEvent -> {
            if (mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getTheme().setQuickDraw(false);
                mediator.getCanvas2D().setTranslateX(0.0);
                mediator.getCanvas2D().setTranslateY(0.0);
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMousePressed(mouseEvent -> {
                mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
        });
        swingNode.setOnScroll(scrollEvent -> {
            if (mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getTheme().setQuickDraw(true);
                scrollCounter++;

                Thread th = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        if (scrollCounter == 1) {
                            mediator.getTheme().setQuickDraw(false);
                            mediator.getCanvas2D().repaint();
                        }
                        scrollCounter--;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
                th.setDaemon(true);
                th.start();
                Point2D.Double realMouse = new Point2D.Double(((double)scrollEvent.getX()-mediator.getWorkingSession().getViewX())/mediator.getWorkingSession().getFinalZoomLevel(),((double)scrollEvent.getY()-mediator.getWorkingSession().getViewY())/mediator.getWorkingSession().getFinalZoomLevel());
                double notches = scrollEvent.getDeltaY();
                if (notches < 0)
                    mediator.getWorkingSession().setZoom(1.25);
                if (notches > 0)
                    mediator.getWorkingSession().setZoom(1.0/1.25);
                Point2D.Double newRealMouse = new Point2D.Double(((double)scrollEvent.getX()-mediator.getWorkingSession().getViewX())/mediator.getWorkingSession().getFinalZoomLevel(),((double)scrollEvent.getY()-mediator.getWorkingSession().getViewY())/mediator.getWorkingSession().getFinalZoomLevel());
                mediator.getWorkingSession().moveView((newRealMouse.getX()-realMouse.getX())*mediator.getWorkingSession().getFinalZoomLevel(),(newRealMouse.getY()-realMouse.getY())*mediator.getWorkingSession().getFinalZoomLevel());
                mediator.getCanvas2D().repaint();
            }
        });
        createSwingContent(swingNode);
        Screen screen = Screen.getPrimary();
        BorderPane root = new BorderPane();

        //##### Explorer
        this.explorer = new TreeTableView<StructuralItem>();
        this.explorer.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        TreeTableColumn<StructuralItem, String> nameCol = new TreeTableColumn<StructuralItem, String>("Name");
        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<StructuralItem, String>("name"));
        TreeTableColumn<StructuralItem, String> typeCol = new TreeTableColumn<StructuralItem, String>("Type");
        typeCol.setCellValueFactory(new TreeItemPropertyValueFactory<StructuralItem, String>("type"));
        TreeTableColumn<StructuralItem, Location> locationCol = new TreeTableColumn<StructuralItem, Location>("Location");
        locationCol.setCellValueFactory(new TreeItemPropertyValueFactory<StructuralItem, Location>("location"));
        this.explorer.getColumns().addAll(nameCol,typeCol, locationCol);
        this.explorer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.explorer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (!explorer.getSelectionModel().getSelectedItems().isEmpty()) {
                    int i = 0;
                    for (TreeItem<StructuralItem> item : explorer.getSelectionModel().getSelectedItems()) {
                        if (item.getValue().getSecondaryStructureElement() != null) {
                            mediator.addToSelection(Mediator.SelectionEmitter.EXPLORER, i == 0, item.getValue().getSecondaryStructureElement()); //if the first element added to the selection, we clear the previous selection
                            i++;
                        }
                    }
                    mediator.canvas2D.repaint();
                } else {
                    //no selection
                    mediator.addToSelection(Mediator.SelectionEmitter.EXPLORER, true,null);
                    mediator.canvas2D.repaint();
                }
            }
        });

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8);
        splitPane.getItems().addAll(swingNode, this.explorer);

        root.setCenter(splitPane);

        //## TOOLBARS
        this.topToolBars = new VBox();
        topToolBars.setPadding(new Insets(5.0,10.0,5.0,10.0));
        topToolBars.setSpacing(5.0);
        root.setTop(topToolBars);

        //## TOOLBAR 1
        FlowPane toolBar1 = new FlowPane();
        topToolBars.getChildren().add(toolBar1);

        //### Project Icons

        GridPane project = new GridPane();
        project.setPadding(new Insets(0, 10, 0, 10));
        project.setVgap(5);
        project.setHgap(5);

        Label projectTitle = new Label("Project");
        projectTitle.setStyle("-fx-font-size: 15");
        GridPane.setConstraints(projectTitle, 0, 0);
        GridPane.setColumnSpan(projectTitle, 5);
        GridPane.setHalignment(projectTitle, HPos.CENTER);
        project.getChildren().add(projectTitle);

        Separator sepHor = new Separator();
        sepHor.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepHor, 0, 1);
        GridPane.setColumnSpan(sepHor, 5);
        project.getChildren().add(sepHor);

        Button open = new Button("New/Open", new Glyph("FontAwesome", FontAwesome.Glyph.FOLDER_OPEN));
        open.setOnAction(actionEvent -> {
            mediator.getProjectManager().getStage().show();
            mediator.getProjectManager().getStage().toFront();
        });
        GridPane.setConstraints(open, 0, 2);
        project.getChildren().add(open);

        Separator sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 1, 2);
        GridPane.setRowSpan(sepVert1, 1);
        project.getChildren().add(sepVert1);

        saveAs = new Button("Save As", new Glyph("FontAwesome", FontAwesome.Glyph.SAVE));
        saveAs.setDisable(true);
        saveAs.setOnAction(actionEvent -> {
            if (mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getWorkingSession().setScreen_capture(true);
                mediator.getWorkingSession().setScreen_capture_area(new java.awt.geom.Rectangle2D.Double(mediator.getCanvas2D().getBounds().getCenterX() - 200, mediator.getCanvas2D().getBounds().getCenterY() - 100, 400.0, 200.0));
                mediator.getCanvas2D().repaint();
                TextInputDialog dialog = new TextInputDialog("My Project");
                dialog.initModality(Modality.NONE);
                dialog.setTitle("Project Saving");
                dialog.setHeaderText("Fit your 2D preview into the black rectangle before saving.");
                dialog.setContentText("Please enter your project name:");
                Optional<String> projectName = dialog.showAndWait();
                if (projectName.isPresent()) {
                    BufferedImage image = mediator.getCanvas2D().screenCapture(null);
                    if (image != null) {
                        NitriteId id = mediator.getEmbeddedDB().addProject(projectName.get().trim(), mediator.getSecondaryStructureDrawing());
                        File pngFile = new File(new File(new File(mediator.getEmbeddedDB().getRootDir(), "images"), "user"), id.toString() + ".png");
                        try {
                            ImageIO.write(image, "PNG", pngFile);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        mediator.getProjectManager().addProject(id, projectName.get().trim());
                    }
                    mediator.getWorkingSession().setScreen_capture(false);
                    mediator.getWorkingSession().setScreen_capture_area(null);
                    mediator.getCanvas2D().repaint();
                } else {
                    mediator.getWorkingSession().setScreen_capture(false);
                    mediator.getWorkingSession().setScreen_capture_area(null);
                    mediator.getCanvas2D().repaint();
                }
            }
        });
        GridPane.setConstraints(saveAs, 2, 2);
        project.getChildren().add(saveAs);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 3, 2);
        GridPane.setRowSpan(sepVert1, 1);
        project.getChildren().add(sepVert1);

        save = new Button("Save", new Glyph("FontAwesome", FontAwesome.Glyph.SAVE));
        save.setDisable(true);
        save.setOnAction(actionEvent -> {

        });
        GridPane.setConstraints(save, 4, 2);
        project.getChildren().add(save);

        toolBar1.getChildren().add(project);

        toolBar1.getChildren().add(new Separator(Orientation.VERTICAL));

        //### Load 2D Icons

        GridPane load2D = new GridPane();
        load2D.setPadding(new Insets(0, 10, 0, 10));
        load2D.setVgap(5);
        load2D.setHgap(5);

        Label load2DTitle = new Label("Load Data from...");
        load2DTitle.setStyle("-fx-font-size: 15");
        load2D.setConstraints(load2DTitle, 0, 0);
        load2D.setColumnSpan(load2DTitle, 5);
        load2D.setHalignment(load2DTitle, HPos.CENTER);
        load2D.getChildren().add(load2DTitle);

        sepHor = new Separator();
        sepHor.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepHor, 0, 1);
        GridPane.setColumnSpan(sepHor, 5);
        load2D.getChildren().add(sepHor);

        Button openFile = new Button("File", new Glyph("FontAwesome", FontAwesome.Glyph.FILE));
        openFile.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage );
            if (file != null) {
                fileChooser.setInitialDirectory(file.getParentFile());
                javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>,Exception>> loadData = new javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>,Exception>>() {

                    @Override
                    protected Pair<List<SecondaryStructureDrawing>,Exception> call() {
                        SecondaryStructure ss = null;
                        List<SecondaryStructureDrawing> secondaryStructureDrawings = new ArrayList<SecondaryStructureDrawing>();
                        try {
                            if (file.getName().endsWith(".ct")) {
                                ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseCT(new FileReader(file));
                                if (ss != null) {
                                    Theme t = new Theme(RnartistConfig.defaultThemeParams);
                                    if (mediator.getToolbox().getSavedThemesComboBox().getValue() != null) {
                                        t = mediator.getEmbeddedDB().getTheme(mediator.getToolbox().getSavedThemesComboBox().getValue().getValue());
                                    }
                                    ss.getRna().setSource(file.getName());
                                    secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), t, new WorkingSession()));
                                }
                            } else if (file.getName().endsWith(".bpseq")) {
                                ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseBPSeq(new FileReader(file));
                                if (ss != null) {
                                    Theme t = new Theme(RnartistConfig.defaultThemeParams);
                                    if (mediator.getToolbox().getSavedThemesComboBox().getValue() != null) {
                                        t = mediator.getEmbeddedDB().getTheme(mediator.getToolbox().getSavedThemesComboBox().getValue().getValue());
                                    }
                                    ss.getRna().setSource(file.getName());
                                    secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), t, new WorkingSession()));
                                }
                            } else if (file.getName().endsWith(".fasta") || file.getName().endsWith(".fas") || file.getName().endsWith(".vienna")) {
                                ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseVienna(new FileReader(file));
                                if (ss != null) {
                                    Theme t = new Theme(RnartistConfig.defaultThemeParams);
                                    if (mediator.getToolbox().getSavedThemesComboBox().getValue() != null) {
                                        t = mediator.getEmbeddedDB().getTheme(mediator.getToolbox().getSavedThemesComboBox().getValue().getValue());
                                    }
                                    ss.getRna().setSource(file.getName());
                                    secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), t, new WorkingSession()));
                                }

                            } else if (file.getName().endsWith(".pdb")) {
                                int countBefore = secondaryStructureDrawings.size();
                                for (TertiaryStructure ts : io.github.fjossinet.rnartist.core.model.io.ParsersKt.parsePDB(new FileReader(file))) {
                                    try {
                                        ss = new Rnaview().annotate(ts);
                                        if (ss != null) {
                                            Theme t = new Theme(RnartistConfig.defaultThemeParams);
                                            if (mediator.getToolbox().getSavedThemesComboBox().getValue() != null) {
                                                t = mediator.getEmbeddedDB().getTheme(mediator.getToolbox().getSavedThemesComboBox().getValue().getValue());
                                            }
                                            ss.getRna().setSource(file.getName());
                                            ss.setTertiaryStructure(ts);
                                            secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), t, new WorkingSession()));
                                        }
                                    } catch (FileNotFoundException exception) {
                                        //do nothing, RNAVIEW can have problem to annotate some RNA (no 2D for example)
                                    }
                                }
                                if (countBefore < secondaryStructureDrawings.size()) {//RNAVIEW was able to annotate at least one RNA molecule
                                    if (mediator.getChimeraDriver() != null)
                                        mediator.getChimeraDriver().loadTertiaryStructure(file);
                                } else { //we generate an Exception to show the Alert dialog
                                    throw new Exception("RNAVIEW was not able to annotate the 3D structure stored in "+file.getName());
                                }

                            } else if (file.getName().endsWith(".stk") || file.getName().endsWith(".stockholm")) {
                                for (SecondaryStructure _ss : io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseStockholm(new FileReader(file))) {
                                    _ss.getRna().setSource(file.getName());
                                    Theme t = new Theme(RnartistConfig.defaultThemeParams);
                                    if (mediator.getToolbox().getSavedThemesComboBox().getValue() != null) {
                                        t = mediator.getEmbeddedDB().getTheme(mediator.getToolbox().getSavedThemesComboBox().getValue().getValue());
                                    }
                                    secondaryStructureDrawings.add(new SecondaryStructureDrawing(_ss, mediator.getCanvas2D().getBounds(), t, new WorkingSession()));
                                }
                            }
                        } catch (Exception e) {
                            return Pair.of(secondaryStructureDrawings,e);
                        }
                        return Pair.of(secondaryStructureDrawings,null);
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
                                for (SecondaryStructureDrawing drawing : loadData.get().getLeft()) {
                                    mediator.getAllStructures().add(drawing);
                                }
                                allStructuresChoices.setValue(mediator.getAllStructures().get(mediator.getAllStructures().size() - 1));
                                mediator.getCanvas2D().fitDisplayOn(mediator.getSecondaryStructureDrawing().getBounds());
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
        });
        GridPane.setConstraints(openFile, 0, 2);
        load2D.getChildren().add(openFile);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 1, 2);
        GridPane.setRowSpan(sepVert1, 1);
        load2D.getChildren().add(sepVert1);

        Button databases = new Button("Databases", new Glyph("FontAwesome", FontAwesome.Glyph.GLOBE));
        databases.setOnAction(actionEvent -> {
            mediator.getWebBrowser().getStage().show();
            mediator.getWebBrowser().getStage().toFront();
            mediator.getWebBrowser().showTab(1);
        });
        GridPane.setConstraints(databases, 2, 2);
        load2D.getChildren().add(databases);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 3, 2);
        GridPane.setRowSpan(sepVert1, 1);
        load2D.getChildren().add(sepVert1);

        Button fromScratch = new Button("Scratch", new Glyph("FontAwesome", FontAwesome.Glyph.MAGIC));
        fromScratch.setOnAction(actionEvent -> {

        });
        GridPane.setConstraints(fromScratch, 4, 2);
        load2D.getChildren().add(fromScratch);

        toolBar1.getChildren().add(load2D);

        toolBar1.getChildren().add(new Separator(Orientation.VERTICAL));

        //### Windows Icons
        GridPane windows = new GridPane();
        windows.setPadding(new Insets(0, 10, 0, 10));
        windows.setVgap(5);
        windows.setHgap(5);

        Label viewTitle = new Label("Windows");
        viewTitle.setStyle("-fx-font-size: 15");
        windows.setConstraints(viewTitle, 0, 0);
        windows.setColumnSpan(viewTitle, 5);
        windows.setHalignment(viewTitle, HPos.CENTER);
        windows.getChildren().add(viewTitle);

        sepHor = new Separator();
        sepHor.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepHor, 0, 1);
        GridPane.setColumnSpan(sepHor, 5);
        windows.getChildren().add(sepHor);

        Button toolbox = new Button("Toolbox", new Glyph("FontAwesome", FontAwesome.Glyph.WRENCH));
        toolbox.setOnAction(actionEvent -> {
            mediator.getToolbox().getStage().show();
            mediator.getToolbox().getStage().toFront();
        });
        GridPane.setConstraints(toolbox, 0, 2);
        windows.getChildren().add(toolbox);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 1, 2);
        GridPane.setRowSpan(sepVert1, 1);
        windows.getChildren().add(sepVert1);

        Button webBrowser = new Button("WebBrowser", new Glyph("FontAwesome", FontAwesome.Glyph.GLOBE));
        webBrowser.setOnAction(actionEvent -> {
            mediator.getWebBrowser().getStage().show();
            mediator.getWebBrowser().getStage().toFront();
        });
        GridPane.setConstraints(webBrowser, 2, 2);
        windows.getChildren().add(webBrowser);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 3, 2);
        GridPane.setRowSpan(sepVert1, 1);
        windows.getChildren().add(sepVert1);

        Button chimera = new Button("Chimera", new Glyph("FontAwesome", FontAwesome.Glyph.GLOBE));
        chimera.setOnAction(actionEvent -> {
            if (mediator.getChimeraDriver() != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Please Confirm");
                alert.setHeaderText("A Chimera windows is already linked to RNArtist");
                alert.setContentText("The new one will replace it for the linkage");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                    new ChimeraDriver(mediator);
                }
            } else {
                new ChimeraDriver(mediator);
            }
        });
        GridPane.setConstraints(chimera, 4, 2);
        windows.getChildren().add(chimera);

        toolBar1.getChildren().add(windows);

        toolBar1.getChildren().add(new Separator(Orientation.VERTICAL));

        topToolBars.getChildren().add(new Separator(Orientation.HORIZONTAL));

        //## TOOLBAR 2
        FlowPane toolBar2 = new FlowPane();
        toolBar2.setPadding(new Insets(0,10,0,10));
        toolBar2.setHgap(20);

        topToolBars.getChildren().add(toolBar2);

        GridPane loadAndExport = new GridPane();
        loadAndExport.setVgap(5.0);
        loadAndExport.setHgap(5.0);
        Label l = new Label("View 2D for");
        GridPane.setHalignment(l, HPos.RIGHT);
        GridPane.setConstraints(l,0,0);
        loadAndExport.getChildren().add(l);

        this.allStructuresChoices = new ChoiceBox<SecondaryStructureDrawing>();
        this.allStructuresChoices.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getCanvas2D().load2D(allStructuresChoices.getValue());
            }
        });
        this.allStructuresChoices.setItems(mediator.getAllStructures());
        this.allStructuresChoices.setMaxWidth(200);
        GridPane.setConstraints(this.allStructuresChoices,1,0,3,1);
        loadAndExport.getChildren().add(this.allStructuresChoices);

        l = new Label("Export 2D as");
        GridPane.setHalignment(l, HPos.RIGHT);
        GridPane.setConstraints(l,0,1);
        loadAndExport.getChildren().add(l);

        Button svg = new Button("SVG");
        svg.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                fileChooser.setInitialDirectory(file.getParentFile());
                PrintWriter writer;
                try {
                    writer = new PrintWriter(file);
                    writer.println(mediator.getSecondaryStructureDrawing().asSVG());
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        svg.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(svg,1,1);
        loadAndExport.getChildren().add(svg);

        Button ct = new Button("CT");
        ct.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(ct,2,1);
        loadAndExport.getChildren().add(ct);

        Button vienna = new Button("VIENNA");
        vienna.setMaxWidth(Double.MAX_VALUE);
        GridPane.setConstraints(vienna,3,1);
        loadAndExport.getChildren().add(vienna);

        toolBar2.getChildren().add(loadAndExport);

        GridPane center_fit = new GridPane();
        center_fit.setVgap(5.0);
        center_fit.setHgap(5.0);

        Button center2D = new Button("Center 2D on Display", new Glyph("FontAwesome", FontAwesome.Glyph.CROSSHAIRS));
        center2D.setOnAction(actionEvent -> {
            if (mediator.getSecondaryStructureDrawing() != null)
                mediator.canvas2D.centerDisplayOn(mediator.getSecondaryStructureDrawing().getBounds());
        });
        center2D.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHalignment(center2D, HPos.CENTER);
        GridPane.setConstraints(center2D,0,0);
        center_fit.getChildren().add(center2D);

        Button fit2D = new Button("Fit 2D to Display", new Glyph("FontAwesome", FontAwesome.Glyph.ARROWS_ALT));
        fit2D.setOnAction(actionEvent -> {
            mediator.canvas2D.fitDisplayOn(mediator.getSecondaryStructureDrawing().getBounds());
        });
        fit2D.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHalignment(fit2D, HPos.CENTER);
        GridPane.setConstraints(fit2D,0,1);
        center_fit.getChildren().add(fit2D);

        toolBar2.getChildren().add(center_fit);

        GridPane residueOpacity = new GridPane();
        residueOpacity.setVgap(5.0);
        residueOpacity.setHgap(5.0);

        l = new Label("Unselected Residues Opacity (%)");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l,0,0);
        residueOpacity.getChildren().add(l);

        final Slider slider = new Slider(0, 100, (int)(RnartistConfig.getSelectionFading()/255.0*100.0));
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.setShowTickMarks(true);
        slider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                RnartistConfig.setSelectionFading((int)(slider.getValue()/100.0*255.0));
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setHalignment(slider, HPos.CENTER);
        GridPane.setConstraints(slider,0,1);
        residueOpacity.getChildren().add(slider);

        toolBar2.getChildren().add(residueOpacity);

        GridPane globalOptions = new GridPane();
        globalOptions.setVgap(5.0);
        globalOptions.setHgap(5.0);

        l = new Label("2D Options");
        GridPane.setHalignment(l, HPos.RIGHT);
        GridPane.setConstraints(l,0,0);
        globalOptions.getChildren().add(l);

        List<Option> _2DOptions = new ArrayList<Option>();
        _2DOptions.add(new CenterDisplayOnSelection());
        _2DOptions.add(new FitDisplayOnSelection());
        _2DOptions.add(new DisplayTertiariesInSelection());
        _2DOptions.add(new DisplayLWSymbols());
        CheckComboBox<Option> _2DGlobalOptions = new CheckComboBox<Option>(FXCollections.observableList(_2DOptions));
        for (Option o:_2DOptions) {
            if (o.isChecked())
                _2DGlobalOptions.getCheckModel().check(o);
        }
        _2DGlobalOptions.getCheckModel().getCheckedItems().addListener(new ListChangeListener<Option>() {
            public void onChanged(ListChangeListener.Change<? extends Option> c) {
                for (Option o:_2DGlobalOptions.getItems()) {
                    o.check(_2DGlobalOptions.getCheckModel().getCheckedItems().contains(o));
                }
            }
        });

        _2DGlobalOptions.setMaxWidth(200);
        _2DGlobalOptions.setTitle("Choose an Option");
        GridPane.setHalignment(_2DGlobalOptions, HPos.CENTER);
        GridPane.setConstraints(_2DGlobalOptions,1,0);
        globalOptions.getChildren().add(_2DGlobalOptions);

        l = new Label("3D Options");
        GridPane.setHalignment(l, HPos.RIGHT);
        GridPane.setConstraints(l,0,1);
        globalOptions.getChildren().add(l);

        List<String> _3DOptions = new ArrayList<String>();
        _3DOptions.add("Center 3D on Selection");
        _3DOptions.add("Focus 3D on Selection");
        CheckComboBox _3DGlobalOptions = new CheckComboBox(FXCollections.observableList(_3DOptions));
        _3DGlobalOptions.setMaxWidth(200);
        _3DGlobalOptions.setTitle("Choose an Option");
        GridPane.setHalignment(_3DGlobalOptions, HPos.CENTER);
        GridPane.setConstraints(_3DGlobalOptions,1,1);
        globalOptions.getChildren().add(_3DGlobalOptions);

        toolBar2.getChildren().add(globalOptions);

        //### Status Bar
        this.statusBar = new FlowPane();
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.setPadding(new Insets(5,10,5,10));
        statusBar.setHgap(10);

        Label release = new Label(RnartistConfig.getRnartistRelease());
        statusBar.getChildren().add(release);

        Button twitter = new Button("Follow Us", new Glyph("FontAwesome", FontAwesome.Glyph.TWITTER));
        twitter.setOnAction(actionEvent -> {
            this.getHostServices().showDocument("https://twitter.com/rnartist_app");
        });
        statusBar.getChildren().add(twitter);

        root.setBottom(statusBar);
        Scene scene = new Scene(root, screen.getBounds().getWidth(), screen.getBounds().getHeight());
        scene.getStylesheets().add(getClass().getClassLoader().getResource("io/github/fjossinet/rnartist/gui/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("RNArtist");

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(screenSize.getWidth()-360);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(360);
        this.stage.setY(0);

        mediator.getProjectManager().getStage().show();
        mediator.getProjectManager().getStage().toFront();
    }

    public void activateSaveButtons() {
        this.save.setDisable(false);
        this.saveAs.setDisable(false);
    }

    public void clearSelectionInExplorer() {
        this.explorer.getSelectionModel().clearSelection();
    }

    public void selectInExplorer(SecondaryStructureElement sse) {
        TreeItem item = this.getTreeViewItemFor(this.explorer.getRoot(), sse);
        if (item != null) {
            item.setExpanded(true);
            TreeItem parent = item.getParent();
            while (parent != null) {
                parent.setExpanded(true);
                parent = parent.getParent();
            }
            explorer.scrollTo(explorer.getRow(item));
            this.explorer.getSelectionModel().select(item);
        }
    }

    private TreeItem getTreeViewItemFor(TreeItem<StructuralItem> item , SecondaryStructureElement sse) {
        if (item != null && item.getValue().getSecondaryStructureElement() == sse)
            return item;

        for (TreeItem<StructuralItem> child : item.getChildren()){
            TreeItem<String> _item = getTreeViewItemFor(child, sse);
            if( _item != null)
                return _item;
        }
        return null;
    }

    public void loadIntoExplorer(SecondaryStructureDrawing drawing) {
        TreeItem<StructuralItem> root = new TreeItem<StructuralItem>(new SecondaryStructureItem(drawing));
        root.setExpanded(true);
        for (HelixLine h: drawing.getHelices()) {
            TreeItem<StructuralItem> helixItem = new TreeItem<StructuralItem>(new HelixItem(h));
            root.getChildren().add(helixItem);
            for (JunctionCircle jc:drawing.getBranches()) {
                if (jc.getInHelix().equals(h.getHelix())) {
                    for (JunctionCircle _jc: jc.junctionsFromBranch()) {
                        TreeItem<StructuralItem> junctionItem = new TreeItem<StructuralItem>(new JunctionItem(_jc));
                        helixItem.getChildren().addAll(junctionItem);
                        for (HelixLine _h: _jc.getHelices()) {
                            TreeItem<StructuralItem> _helixItem = new TreeItem<StructuralItem>(new HelixItem(_h));
                            junctionItem.getChildren().add(_helixItem);
                            for (SecondaryInteractionLine interaction: _h.getSecondaryInteractions()) {
                                TreeItem<StructuralItem> interactionItem = new TreeItem<StructuralItem>(new SecondaryInteractionItem(interaction));
                                _helixItem.getChildren().addAll(interactionItem);
                                for (ResidueCircle r: drawing.getResidues()) {
                                    if (r.getParent() == interaction) {
                                        TreeItem<StructuralItem> residueItem = new TreeItem<StructuralItem>(new ResidueItem(r));
                                        interactionItem.getChildren().addAll(residueItem);
                                    }
                                }
                            }
                        }
                        for (ResidueCircle r: drawing.getResidues()) {
                            if (r.getParent() == _jc) {
                                TreeItem<StructuralItem> residueItem = new TreeItem<StructuralItem>(new ResidueItem(r));
                                junctionItem.getChildren().addAll(residueItem);
                            }
                        }
                    }
                    break;
                }
            }
            for (SecondaryInteractionLine interaction: h.getSecondaryInteractions()) {
                TreeItem<StructuralItem> interactionItem = new TreeItem<StructuralItem>(new SecondaryInteractionItem(interaction));
                helixItem.getChildren().addAll(interactionItem);
                for (ResidueCircle r: drawing.getResidues()) {
                    if (r.getParent() == interaction) {
                        TreeItem<StructuralItem> residueItem = new TreeItem<StructuralItem>(new ResidueItem(r));
                        interactionItem.getChildren().addAll(residueItem);
                    }
                }
            }
        }
        for (TertiaryInteractionLine interaction:drawing.getTertiaryInteractions()) {
            TreeItem<StructuralItem> interactionItem = new TreeItem<StructuralItem>(new TertiaryInteractionItem(interaction));
            root.getChildren().addAll(interactionItem);
        }
        this.explorer.setRoot(root);
    }

    public void showTopToolBars(boolean show) {
        if (show)
            ((BorderPane)stage.getScene().getRoot()).setTop(this.topToolBars);
        else
            ((BorderPane)stage.getScene().getRoot()).setTop(null);
    }

    public void showStatusBar(boolean show) {
        if (show)
            ((BorderPane)stage.getScene().getRoot()).setBottom(this.statusBar);
        else
            ((BorderPane)stage.getScene().getRoot()).setBottom(null);
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

    private abstract class Option {

        protected String title;

        protected Option(String title) {
            this.title = title;
        }

        abstract protected void check(boolean check);

        abstract protected boolean isChecked();

        @Override
        public String toString() {
            return this.title;
        }
    }

    private class DisplayTertiariesInSelection extends Option {

        public DisplayTertiariesInSelection() {
            super("Display Tertiary Interactions for Selection");
        }

        @Override
        protected boolean isChecked() {
            return RnartistConfig.getDisplayTertiariesInSelection();
        }

        @Override
        protected void check(boolean check) {
            RnartistConfig.setDisplayTertiariesInSelection(check);
        }
    }

    private class CenterDisplayOnSelection extends Option {

        public CenterDisplayOnSelection() {
            super("Center Display on Selection");
        }

        @Override
        protected boolean isChecked() {
            return RnartistConfig.getCenterDisplayOnSelection();
        }

        @Override
        protected void check(boolean check) {
            RnartistConfig.setCenterDisplayOnSelection(check);
        }
    }

    private class FitDisplayOnSelection extends Option {

        public FitDisplayOnSelection() {
            super("Fit Display on Selection");
        }

        @Override
        protected boolean isChecked() {
            return RnartistConfig.getFitDisplayOnSelection();
        }

        @Override
        protected void check(boolean check) {
            RnartistConfig.setFitDisplayOnSelection(check);
        }
    }

    private class DisplayLWSymbols extends Option {

        public DisplayLWSymbols() {
            super("Display LW Symbols");
        }

        @Override
        protected boolean isChecked() {
            return RnartistConfig.getDisplayLWSymbols();
        }

        @Override
        protected void check(boolean check) {
            RnartistConfig.setDisplayLWSymbols(check);
            mediator.getCanvas2D().repaint();
        }
    }

}
