package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.RNArtist;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.model.DrawingLoadedFromRNArtistDB;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static io.github.fjossinet.rnartist.core.model.io.UtilsKt.createTemporaryFile;

public class ProjectsPanel {

    private Mediator mediator;
    private ObservableList<ProjectPanel> projectPanels;
    private GridView<ProjectPanel> gridview;
    private Stage stage;

    public ProjectsPanel(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("Saved Projects");
        stage.setOnCloseRequest(windowEvent -> {
            if (mediator.getDrawingDisplayed().isNotNull().get()) { //the user has decided to cancel its idea to open an other project
                mediator.getRnartist().getStage().show();
                mediator.getRnartist().getStage().toFront();
            }
        });

        this.projectPanels = FXCollections.observableArrayList();
        for (Document project: this.mediator.getEmbeddedDB().getProjects().find()) {
            this.projectPanels.add(new ProjectPanel(project.getId(), (String)project.get("name")));
        }
        this.createScene(stage);
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(1360);
        this.stage.setHeight(2*screenSize.getHeight()/3);
        final double newX = (screenSize.getWidth() - this.stage.getWidth()) / 2;
        final double newY = (screenSize.getHeight() - this.stage.getHeight()) / 2;
        this.stage.setX(newX);
        this.stage.setY(newY);
    }

    public Stage getStage() {
        return stage;
    }

    public NitriteId saveProjectAs(String name, BufferedImage image) throws IOException {
        NitriteId id = mediator.getEmbeddedDB().saveProjectAs(name, mediator.getDrawingDisplayed().get().getDrawing());
        File pngFile = new File(new File(new File(mediator.getEmbeddedDB().getRootDir(), "images"), "user"), id.toString() + ".png");
        ImageIO.write(image, "PNG", pngFile);
        File chimera_sessions = new File(mediator.getEmbeddedDB().getRootDir(), "chimera_sessions");
        if (!chimera_sessions.exists())
            chimera_sessions.mkdir();
        mediator.getChimeraDriver().saveSession(new File(chimera_sessions, id.toString()), new File(chimera_sessions, id.toString()+".pdb"));
        this.projectPanels.add(new ProjectPanel(id, name));
        return id;
    }

    public void saveProject() throws IOException {
        mediator.getEmbeddedDB().saveProject(((DrawingLoadedFromRNArtistDB)mediator.getDrawingDisplayed().get()).getId(), mediator.getDrawingDisplayed().get().getDrawing());
        File chimera_sessions = new File(mediator.getEmbeddedDB().getRootDir(), "chimera_sessions");
        mediator.getChimeraDriver().saveSession(new File(chimera_sessions,((DrawingLoadedFromRNArtistDB)mediator.getDrawingDisplayed().get()).getId().toString()), new File(chimera_sessions, ((DrawingLoadedFromRNArtistDB)mediator.getDrawingDisplayed().get()).getId().toString()+".pdb"));
    }

    private void createScene(Stage stage) {
        gridview = new GridView<ProjectPanel>(projectPanels);
        gridview.setPadding( new javafx.geometry.Insets(20, 20, 20, 20));
        gridview.setHorizontalCellSpacing(20);
        gridview.setVerticalCellSpacing(20);
        gridview.setCellWidth(400);
        gridview.setCellHeight(200);
        gridview.setStyle("-fx-background-color: lightgray;");

        gridview.setCellFactory(new Callback<GridView<ProjectPanel>, GridCell<ProjectPanel>>() {
            public GridCell<ProjectPanel> call(GridView<ProjectPanel> gridView) {
                return new ProjectCell();
            }
        });
        Scene scene = new Scene(gridview);
        stage.setScene(scene);
    }

    class ProjectCell extends GridCell<ProjectPanel> {

        private ImageView icon = new ImageView();
        private Label projectName = new Label();
        private VBox content;
        private HBox border;
        private HBox titleBar;

        public ProjectCell() {
            this.content = new VBox();
            this.content.setSpacing(5);
            this.content.setAlignment(Pos.CENTER);
            this.border = new HBox();
            this.border.getChildren().add(icon);
            this.border.setStyle("-fx-border-color: lightgray; -fx-border-width: 4px;");
            this.content.getChildren().add(border);
            this.titleBar = new HBox();
            this.titleBar.setSpacing(5);
            this.titleBar.setAlignment(Pos.CENTER);
            this.titleBar.getChildren().add(projectName);
            Label deleteProject = new Label(null, new FontIcon("fas-trash:15"));
            ((FontIcon)deleteProject.getGraphic()).setFill(Color.BLACK);
            this.titleBar.getChildren().add(deleteProject);
            this.content.getChildren().add(this.titleBar);
            this.projectName.setTextFill(Color.BLACK);
            this.projectName.setStyle("-fx-font-weight: bold");
            deleteProject.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.initOwner(ProjectsPanel.this.stage);
                    alert.initModality(Modality.WINDOW_MODAL);
                    alert.setTitle("Confirm Deletion");
                    alert.setHeaderText(null);
                    alert.setContentText("Are you sure to delete this project?");

                    Stage alerttStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alerttStage.setAlwaysOnTop(true);
                    alerttStage.toFront();

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        javafx.concurrent.Task<Exception> deleteProject = new javafx.concurrent.Task<Exception>() {
                            @Override
                            protected Exception call() {
                                try {
                                    mediator.getEmbeddedDB().removeProject(ProjectCell.this.getItem().id);
                                    return null;
                                } catch (Exception e) {
                                    return e;
                                }
                            }
                        };
                        deleteProject.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent workerStateEvent) {
                                try {
                                    if (deleteProject.get() != null) {
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setTitle("Project deletion error");
                                        alert.setHeaderText(deleteProject.get().getMessage());
                                        alert.setContentText("If this problem persists, you can send the exception stacktrace below to fjossinet@gmail.com");
                                        StringWriter sw = new StringWriter();
                                        PrintWriter pw = new PrintWriter(sw);
                                        deleteProject.get().printStackTrace(pw);
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
                                        projectPanels.clear();
                                        for (Document project: mediator.getEmbeddedDB().getProjects().find()) {
                                            projectPanels.add(new ProjectPanel(project.getId(), (String)project.get("name")));
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        new Thread(deleteProject).start();
                    } else {
                        event.consume();
                    }
                }
            });
            icon.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    javafx.concurrent.Task<Pair<SecondaryStructureDrawing, Exception>> loadData = new javafx.concurrent.Task<Pair<SecondaryStructureDrawing, Exception>>() {

                        @Override
                        protected Pair<SecondaryStructureDrawing, Exception> call() {
                            try {
                                return Pair.of(mediator.getEmbeddedDB().getProject(ProjectCell.this.getItem().id), null);
                            } catch (Exception e) {
                                return Pair.of(null, e);
                            }
                        }
                    };
                    loadData.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            try {
                                if (loadData.get().getRight() != null) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Project loading error");
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
                                    stage.hide();
                                    mediator.getExplorer().getStage().show();
                                    mediator.getRnartist().getStage().show();
                                    mediator.getRnartist().getStage().toFront();

                                    mediator.getDrawingsLoaded().add(new DrawingLoadedFromRNArtistDB(mediator, loadData.get().getLeft(),ProjectCell.this.getItem().id, ProjectCell.this.getItem().name));
                                    mediator.getDrawingDisplayed().set(mediator.getDrawingsLoaded().get(mediator.getDrawingsLoaded().size() - 1));

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

            this.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    border.setStyle("-fx-border-color: darkgray; -fx-border-width: 4px;");
                }
            });

            this.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    border.setStyle("-fx-border-color: lightgray; -fx-border-width: 4px;");
                }
            });
        }

        @Override
        protected void updateItem(ProjectPanel projectPanel, boolean empty) {
            super.updateItem(projectPanel, empty);
            setGraphic(null);
            setText(null);
            if (!empty && projectPanel != null) {
                projectName.setText(projectPanel.name);
                icon.setImage(projectPanel.getImage());
                setGraphic(this.content);
            }
        }
    }

    class ProjectPanel {

        protected NitriteId id;
        protected String name;

        ProjectPanel() {

        }

        ProjectPanel(NitriteId id, String name) {
            this.id = id;
            this.name = name;
        }

        Image getImage() {
            try {
                return new Image(new File(new File(new File(mediator.getEmbeddedDB().getRootDir(),"images"),"user"),id.toString()+".png").toURI().toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}
