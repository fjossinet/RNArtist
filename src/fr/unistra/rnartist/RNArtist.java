package fr.unistra.rnartist;

import fr.unistra.rnartist.gui.Canvas2D;
import fr.unistra.rnartist.gui.Mediator;
import fr.unistra.rnartist.gui.NewUserDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
import java.io.IOException;
import java.util.Optional;

public class RNArtist extends Application {

    private Mediator mediator;
    private Stage stage;
    private int scrollCounter = 0;
    private Button save;

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
        ToolBar toolBar = new ToolBar();
        root.setTop(toolBar);

        Button open = new Button("Open", new Glyph("FontAwesome", FontAwesome.Glyph.FOLDER_OPEN));
        toolBar.getItems().add(open);
        open.setOnAction(actionEvent -> {
            mediator.getProjectManager().getStage().show();
            mediator.getProjectManager().getStage().toFront();
        });

        save = new Button("Save", new Glyph("FontAwesome", FontAwesome.Glyph.SAVE));
        save.setDisable(true);
        toolBar.getItems().add(save);
        save.setOnAction(actionEvent -> {
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
                        NitriteId id = mediator.getEmbeddedDB().addProject(projectName.get().trim(), mediator.getCanvas2D().getSecondaryStructureDrawing().get(), mediator.getTertiaryStructure());
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

        toolBar.getItems().add(new Separator(Orientation.VERTICAL));

        Button toolbox = new Button("Toolbox", new Glyph("FontAwesome", FontAwesome.Glyph.WRENCH));
        toolBar.getItems().add(toolbox);
        toolbox.setOnAction(actionEvent -> {
            mediator.getToolbox().getStage().show();
            mediator.getToolbox().getStage().toFront();
        });

        Button ndbBrowser = new Button("Database", new Glyph("FontAwesome", FontAwesome.Glyph.DATABASE));
        toolBar.getItems().add(ndbBrowser);
        ndbBrowser.setOnAction(actionEvent -> {
            mediator.getEmbeddedDBGUI().getStage().show();
            mediator.getEmbeddedDBGUI().getStage().toFront();
        });

        toolBar.getItems().add(new Separator(Orientation.VERTICAL));

        Button about = new Button("About", new Glyph("FontAwesome", FontAwesome.Glyph.INFO_CIRCLE));
        toolBar.getItems().add(about);
        about.setOnAction(actionEvent -> {
        });

        stage.setScene(new Scene(root, screen.getBounds().getWidth(), screen.getBounds().getHeight()));
        stage.setTitle("RNArtist");
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(screenSize.getWidth()-600);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(300);
        this.stage.setY(0);
        this.stage.show();
        this.stage.toFront();

        mediator.getToolbox().getStage().show();
        mediator.getToolbox().getStage().toFront();

        mediator.getEmbeddedDBGUI().getStage().show();
        mediator.getEmbeddedDBGUI().getStage().toFront();


    }

    public void activateSaveButton() {
        this.save.setDisable(false);
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
