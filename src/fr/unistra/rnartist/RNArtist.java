package fr.unistra.rnartist;

import fr.unistra.rnartist.gui.Canvas2D;
import fr.unistra.rnartist.gui.Mediator;
import fr.unistra.rnartist.gui.NewUserDialog;
import fr.unistra.rnartist.io.ChimeraDriver;
import fr.unistra.rnartist.model.SecondaryStructure;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.*;
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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class RNArtist extends Application {

    private Mediator mediator;
    private Stage stage;
    private int scrollCounter = 0;
    private Button saveAs, save, export;

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
                    RnartistConfig.saveConfig(mediator);
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
            new NewUserDialog(this);
        this.mediator = new Mediator(this);
        RnartistConfig.saveConfig(mediator);
        final SwingNode swingNode = new SwingNode();
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

        //## TOOLBAR
        HBox toolBar = new HBox();
        root.setTop(toolBar);

        GridPane project = new GridPane();
        project.setPadding(new Insets(10, 10, 10, 10));
        project.setVgap(5);
        project.setHgap(5);

        Label projectTitle = new Label("Project", new Glyph("FontAwesome", FontAwesome.Glyph.ARCHIVE));
        projectTitle.setStyle("-fx-font-size: 15");
        GridPane.setConstraints(projectTitle, 0, 0);
        GridPane.setColumnSpan(projectTitle, 7);
        GridPane.setHalignment(projectTitle, HPos.CENTER);
        project.getChildren().add(projectTitle);

        Separator sepHor = new Separator();
        sepHor.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepHor, 0, 1);
        GridPane.setColumnSpan(sepHor, 7);
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

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 5, 2);
        GridPane.setRowSpan(sepVert1, 1);
        project.getChildren().add(sepVert1);

        export = new Button("Export", new Glyph("FontAwesome", FontAwesome.Glyph.UPLOAD));
        export.setDisable(true);
        export.setOnAction(actionEvent -> {

        });
        GridPane.setConstraints(export, 6, 2);
        project.getChildren().add(export);

        toolBar.getChildren().add(project);

        toolBar.getChildren().add(new Separator(Orientation.VERTICAL));

        GridPane load2D = new GridPane();
        load2D.setPadding(new Insets(10, 10, 10, 10));
        load2D.setVgap(5);
        load2D.setHgap(5);

        Label load2DTitle = new Label("Load 2D from...", new Glyph("FontAwesome", FontAwesome.Glyph.DOWNLOAD));
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
            File f = fileChooser.showOpenDialog(null );
            if (f!= null) {
                SecondaryStructure ss = null;
                if (f.getName().endsWith(".ct")) {
                    try {
                        ss = fr.unistra.rnartist.model.io.ParsersKt.parseCT(new FileReader(f));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (f.getName().endsWith(".bpseq")) {
                    try {
                        ss = fr.unistra.rnartist.model.io.ParsersKt.parseBPSeq(new FileReader(f));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }  else if (f.getName().endsWith(".fasta") || f.getName().endsWith(".fas") || f.getName().endsWith(".vienna")) {
                    try {
                        ss = fr.unistra.rnartist.model.io.ParsersKt.parseVienna(new FileReader(f));
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

        toolBar.getChildren().add(load2D);

        toolBar.getChildren().add(new Separator(Orientation.VERTICAL));

        GridPane view = new GridPane();
        view.setPadding(new Insets(10, 10, 10, 10));
        view.setVgap(5);
        view.setHgap(5);

        Label viewTitle = new Label("View", new Glyph("FontAwesome", FontAwesome.Glyph.EYE));
        viewTitle.setStyle("-fx-font-size: 15");
        view.setConstraints(viewTitle, 0, 0);
        view.setColumnSpan(viewTitle, 7);
        view.setHalignment(viewTitle, HPos.CENTER);
        view.getChildren().add(viewTitle);

        sepHor = new Separator();
        sepHor.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepHor, 0, 1);
        GridPane.setColumnSpan(sepHor, 7);
        view.getChildren().add(sepHor);

        Button toolbox = new Button("Toolbox", new Glyph("FontAwesome", FontAwesome.Glyph.WRENCH));
        toolbox.setOnAction(actionEvent -> {
            mediator.getToolbox().getStage().show();
            mediator.getToolbox().getStage().toFront();
        });
        GridPane.setConstraints(toolbox, 0, 2);
        view.getChildren().add(toolbox);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 1, 2);
        GridPane.setRowSpan(sepVert1, 1);
        view.getChildren().add(sepVert1);

        Button webBrowser = new Button("WebBrowser", new Glyph("FontAwesome", FontAwesome.Glyph.GLOBE));
        webBrowser.setOnAction(actionEvent -> {
            mediator.getWebBrowser().getStage().show();
            mediator.getWebBrowser().getStage().toFront();
        });
        GridPane.setConstraints(webBrowser, 2, 2);
        view.getChildren().add(webBrowser);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 3, 2);
        GridPane.setRowSpan(sepVert1, 1);
        view.getChildren().add(sepVert1);

        Button chimera = new Button("UCSF Chimera", new Glyph("FontAwesome", FontAwesome.Glyph.GLOBE));
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
        view.getChildren().add(chimera);

        sepVert1 = new Separator();
        sepVert1.setOrientation(Orientation.VERTICAL);
        sepVert1.setValignment(VPos.CENTER);
        GridPane.setConstraints(sepVert1, 5, 2);
        GridPane.setRowSpan(sepVert1, 1);
        view.getChildren().add(sepVert1);

        Button about = new Button("About", new Glyph("FontAwesome", FontAwesome.Glyph.INFO_CIRCLE));
        about.setOnAction(actionEvent -> {
        });
        GridPane.setConstraints(about, 6, 2);
        view.getChildren().add(about);

        toolBar.getChildren().add(view);

        toolBar.getChildren().add(new Separator(Orientation.VERTICAL));

        stage.setScene(new Scene(root, screen.getBounds().getWidth(), screen.getBounds().getHeight()));
        stage.setTitle("RNArtist");
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(screenSize.getWidth()-440);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(440);
        this.stage.setY(0);

        mediator.getProjectManager().getStage().show();
        mediator.getProjectManager().getStage().toFront();
    }

    public void activateSaveButtons() {
        this.save.setDisable(false);
        this.saveAs.setDisable(false);
        this.export.setDisable(false);
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
