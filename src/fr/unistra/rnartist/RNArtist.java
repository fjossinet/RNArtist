package fr.unistra.rnartist;

import fr.unistra.rnartist.gui.Canvas2D;
import fr.unistra.rnartist.gui.JunctionKnob;
import fr.unistra.rnartist.gui.Mediator;
import fr.unistra.rnartist.gui.RegisterDialog;
import fr.unistra.rnartist.io.ChimeraDriver;
import fr.unistra.rnartist.model.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.geometry.*;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
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

public class RNArtist extends Application {

    private Mediator mediator;
    private Stage stage;
    private int scrollCounter = 0;
    private Button saveAs, save, export;
    private VBox topToolBars;
    private FlowPane statusBar;

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
                    RnartistConfig.saveConfig(mediator.getTheme());
                    Platform.exit();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                windowEvent.consume();
            }
        });
        RnartistConfig.loadConfig();
        if (RnartistConfig.getUserID() == null)
            new RegisterDialog(this);
        this.mediator = new Mediator(this);
        RnartistConfig.saveConfig(mediator.getTheme());
        final SwingNode swingNode = new SwingNode();
        swingNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.isControlDown()) {
                mediator.getGraphicsContext().getSelectedResidues().clear();
                mediator.getToolbox().unselectJunctionKnobs();
                for (JunctionCircle jc : mediator.getCanvas2D().getSecondaryStructureDrawing().get().getAllJunctions()) {
                    AffineTransform at = new AffineTransform();
                    at.translate(mediator.getGraphicsContext().getViewX(), mediator.getGraphicsContext().getViewY());
                    at.scale(mediator.getGraphicsContext().getFinalZoomLevel(), mediator.getGraphicsContext().getFinalZoomLevel());
                    if (jc.getCircle() != null && at.createTransformedShape(jc.getCircle()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                        mediator.getGraphicsContext().getSelectedResidues().addAll(mediator.getCanvas2D().getSecondaryStructureDrawing().get().getResiduesFromAbsPositions(jc.getInHelix().getLocation().getPositions()));
                        for (HelixLine h:jc.getHelices())
                            mediator.getGraphicsContext().getSelectedResidues().addAll(mediator.getCanvas2D().getSecondaryStructureDrawing().get().getResiduesFromAbsPositions(h.getHelix().getLocation().getPositions()));
                        mediator.getGraphicsContext().getSelectedResidues().addAll(mediator.getCanvas2D().getSecondaryStructureDrawing().get().getResiduesFromAbsPositions(jc.getJunction().getLocation().getPositions()));
                        VBox knobFound = null;
                        for (Node child:mediator.getToolbox().getJunctionKnobs().getChildren()) {
                            JunctionKnob knob = (JunctionKnob)((VBox)child).getChildren().get(0);
                            if (knob.getJunctionCircle() == jc) {
                                knobFound = (VBox)child;
                                knob.select();
                                break;
                            }
                        }
                        if (knobFound != null) {
                            mediator.getToolbox().getJunctionKnobs().getChildren().remove(knobFound);
                            mediator.getToolbox().getJunctionKnobs().getChildren().add(0,knobFound);
                        }
                        break;
                    }
                    List<ResidueCircle> residues = mediator.getCanvas2D().getSecondaryStructureDrawing().get().getResidues();
                    for (ResidueCircle c : residues) {
                        if (c.getCircle() != null && at.createTransformedShape(c.getCircle()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            mediator.getGraphicsContext().getSelectedResidues().addAll(mediator.getCanvas2D().getSecondaryStructureDrawing().get().getResiduesFromAbsPositions(List.of(c.getAbsPos())));
                            List<String> positions = new ArrayList<String>(1);
                            positions.add(mediator.getTertiaryStructure() != null && mediator.getTertiaryStructure().getResidue3DAt(c.getAbsPos()) != null ? mediator.getTertiaryStructure().getResidue3DAt(c.getAbsPos()).getLabel() : "" + (c.getAbsPos() + 1));
                            if (mediator.getChimeraDriver() != null)
                                mediator.getChimeraDriver().selectResidues(positions);
                            break;
                        }
                    }
                }
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMouseDragged(mouseEvent -> {
            if (mediator.getGraphicsContext() != null) {
                mediator.getTheme().setQuickDraw(true);
                double transX = mouseEvent.getX() - mediator.getCanvas2D().getTranslateX();
                double transY = mouseEvent.getY() - mediator.getCanvas2D().getTranslateY();
                mediator.getGraphicsContext().moveView(transX, transY);
                mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMouseReleased(mouseEvent -> {
                mediator.getTheme().setQuickDraw(false);
                mediator.getCanvas2D().setTranslateX(0.0);
                mediator.getCanvas2D().setTranslateY(0.0);
                mediator.getCanvas2D().repaint();
        });
        swingNode.setOnMousePressed(mouseEvent -> {
                mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
        });
        swingNode.setOnScroll(scrollEvent -> {
            if (mediator.getGraphicsContext() != null) {
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
                Point2D.Double realMouse = new Point2D.Double(((double)scrollEvent.getX()-mediator.getGraphicsContext().getViewX())/mediator.getGraphicsContext().getFinalZoomLevel(),((double)scrollEvent.getY()-mediator.getGraphicsContext().getViewY())/mediator.getGraphicsContext().getFinalZoomLevel());
                double notches = scrollEvent.getDeltaY();
                if (notches < 0)
                    mediator.getGraphicsContext().setZoom(1.25);
                if (notches > 0)
                    mediator.getGraphicsContext().setZoom(1.0/1.25);
                Point2D.Double newRealMouse = new Point2D.Double(((double)scrollEvent.getX()-mediator.getGraphicsContext().getViewX())/mediator.getGraphicsContext().getFinalZoomLevel(),((double)scrollEvent.getY()-mediator.getGraphicsContext().getViewY())/mediator.getGraphicsContext().getFinalZoomLevel());
                this.mediator.getGraphicsContext().moveView((newRealMouse.getX()-realMouse.getX())*mediator.getGraphicsContext().getFinalZoomLevel(),(newRealMouse.getY()-realMouse.getY())*mediator.getGraphicsContext().getFinalZoomLevel());
                mediator.getCanvas2D().repaint();
            }
        });
        createSwingContent(swingNode);
        Screen screen = Screen.getPrimary();
        BorderPane root = new BorderPane();
        root.setCenter(swingNode);

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
            if (mediator.getCanvas2D().getSecondaryStructureDrawing().get() != null) {
                mediator.getGraphicsContext().setScreen_capture(true);
                mediator.getGraphicsContext().setScreen_capture_area(new java.awt.geom.Rectangle2D.Double(mediator.getCanvas2D().getBounds().getCenterX() - 200, mediator.getCanvas2D().getBounds().getCenterY() - 100, 400.0, 200.0));
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
                        NitriteId id = mediator.getEmbeddedDB().addProject(projectName.get().trim(), mediator.getCanvas2D().getSecondaryStructureDrawing().get(), mediator.getTertiaryStructure(), mediator.getTheme(), mediator.getGraphicsContext());
                        File pngFile = new File(new File(new File(mediator.getEmbeddedDB().getRootDir(), "images"), "user"), id.toString() + ".png");
                        try {
                            ImageIO.write(image, "PNG", pngFile);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        mediator.getProjectManager().addProject(id, projectName.get().trim());
                    }
                    mediator.getGraphicsContext().setScreen_capture(false);
                    mediator.getGraphicsContext().setScreen_capture_area(null);
                    mediator.getCanvas2D().repaint();
                } else {
                    mediator.getGraphicsContext().setScreen_capture(false);
                    mediator.getGraphicsContext().setScreen_capture_area(null);
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

        Label load2DTitle = new Label("Load 2D from...");
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
                SecondaryStructure ss = null;
                if (file.getName().endsWith(".ct")) {
                    try {
                        ss = fr.unistra.rnartist.model.io.ParsersKt.parseCT(new FileReader(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (file.getName().endsWith(".bpseq")) {
                    try {
                        ss = fr.unistra.rnartist.model.io.ParsersKt.parseBPSeq(new FileReader(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }  else if (file.getName().endsWith(".fasta") || file.getName().endsWith(".fas") || file.getName().endsWith(".vienna")) {
                    try {
                        ss = fr.unistra.rnartist.model.io.ParsersKt.parseVienna(new FileReader(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (ss != null)
                    mediator.getCanvas2D().load2D(ss);
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

        Button websites = new Button("Websites", new Glyph("FontAwesome", FontAwesome.Glyph.GLOBE));
        websites.setOnAction(actionEvent -> {
            mediator.getWebBrowser().getStage().show();
            mediator.getWebBrowser().getStage().toFront();
            mediator.getWebBrowser().showTab(1);
        });
        GridPane.setConstraints(websites, 2, 2);
        load2D.getChildren().add(websites);

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
        toolBar2.setHgap(10);

        topToolBars.getChildren().add(toolBar2);

        Label _2DTitle = new Label("2D");
        _2DTitle.setStyle("-fx-font-size: 15");
        //toolBar2.getChildren().add(_2DTitle);

        VBox vbox = new VBox();
        vbox.setSpacing(5.0);
        vbox.setAlignment(Pos.CENTER);

        Button center2D = new Button("Center", new Glyph("FontAwesome", FontAwesome.Glyph.CROSSHAIRS));
        vbox.getChildren().add(center2D);
        center2D.setOnAction(actionEvent -> {
            mediator.canvas2D.center2D();
        });
        center2D.setMaxWidth(Double.MAX_VALUE);

        Button fit2D = new Button("Fit", new Glyph("FontAwesome", FontAwesome.Glyph.ARROWS_ALT));
        vbox.getChildren().add(fit2D);
        fit2D.setOnAction(actionEvent -> {
            mediator.canvas2D.fit2D();
        });
        fit2D.setMaxWidth(Double.MAX_VALUE);

        toolBar2.getChildren().add(vbox);

        vbox = new VBox();
        vbox.setSpacing(5.0);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(new Label("Selection Fading (%)"));
        Slider slider = new Slider(0, 100, RnartistConfig.getSelectionFading()/255.0*100.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                RnartistConfig.setSelectionFading((int)((double)(new_val.intValue())/100.0*255));
                mediator.getCanvas2D().repaint();
            }
        });
        slider.setShowTickMarks(true);
        vbox.getChildren().add(slider);
        toolBar2.getChildren().add(vbox);

        vbox = new VBox();
        vbox.setSpacing(5.0);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(new Label("Residue Fading (%)"));
        slider = new Slider(0, 100, RnartistConfig.getSelectionFading()/255.0*100.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                RnartistConfig.setSelectionFading((int)((double)(new_val.intValue())/100.0*255));
                mediator.getCanvas2D().repaint();
            }
        });
        slider.setShowTickMarks(true);
        vbox.getChildren().add(slider);
        toolBar2.getChildren().add(vbox);

        vbox = new VBox();
        vbox.setSpacing(5.0);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(new Label("Ticks Fading (%)"));
        slider = new Slider(0, 100, RnartistConfig.getSelectionFading()/255.0*100.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                RnartistConfig.setSelectionFading((int)((double)(new_val.intValue())/100.0*255));
                mediator.getCanvas2D().repaint();
            }
        });
        slider.setShowTickMarks(true);
        vbox.getChildren().add(slider);
        toolBar2.getChildren().add(vbox);

        export = new Button("Export as SVG", new Glyph("FontAwesome", FontAwesome.Glyph.UPLOAD));
        toolBar2.getChildren().add(export);
        export.setDisable(true);
        export.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                fileChooser.setInitialDirectory(file.getParentFile());
                PrintWriter writer;
                try {
                    writer = new PrintWriter(file);
                    writer.println(mediator.getCanvas2D().getSecondaryStructureDrawing().get().asSVG());
                    writer.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

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

        stage.setScene(new Scene(root, screen.getBounds().getWidth(), screen.getBounds().getHeight()));
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
        this.export.setDisable(false);
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

}
