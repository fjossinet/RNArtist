package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.core.model.io.Project;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseProject;
import static io.github.fjossinet.rnartist.core.model.io.UtilsKt.createTemporaryFile;

public class ProjectsPanel {

    private Mediator mediator;
    private ObservableList<ProjectPanel> projectPanels;
    private GridView<ProjectPanel> gridview;
    private Stage stage;

    public ProjectsPanel(Mediator mediator) {
        this.mediator = mediator;
        this.stage = new Stage();
        stage.setTitle("RNArtist Projects");
        stage.setOnCloseRequest(windowEvent -> {
            if (mediator.getCurrent2DDrawing() != null) { //the user has decided to cancel its idea to open an other project
                mediator.getRnartist().getStage().show();
                mediator.getRnartist().getStage().toFront();
            }
        });

        this.projectPanels = FXCollections.observableArrayList();
        this.projectPanels.add(new NewProjectPanel());
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

    public void addProject(NitriteId id, String name) {
        this.projectPanels.add(new ProjectPanel(id, name));
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
        private Label name = new Label();
        private VBox content;
        private HBox border;

        public ProjectCell() {
            this.content = new VBox();
            this.content.setSpacing(5);
            this.content.setAlignment(Pos.CENTER);
            border = new HBox();
            border.getChildren().add(icon);
            border.setStyle("-fx-border-color: lightgray; -fx-border-width: 4px;");
            this.content.getChildren().add(border);
            this.content.getChildren().add(name);
            name.setTextFill(Color.BLACK);
            name.setStyle("-fx-font-weight: bold");
            this.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (ProjectCell.this.getItem().name.equals("New Project")) {
                        stage.hide();
                        mediator.get_2DDrawingsLoaded().clear();
                        mediator.getExplorer().getStage().show();
                        mediator.getRnartist().getStage().show();
                        mediator.getRnartist().getStage().toFront();
                    } else {
                        stage.hide();
                        mediator.get_2DDrawingsLoaded().clear();
                        mediator.getExplorer().getStage().show();
                        mediator.getRnartist().getStage().show();
                        mediator.getRnartist().getStage().toFront();
                        javafx.concurrent.Task<Pair<SecondaryStructureDrawing, Exception>> loadData = new javafx.concurrent.Task<Pair<SecondaryStructureDrawing, Exception>>() {

                            @Override
                            protected Pair<SecondaryStructureDrawing, Exception> call() {
                                try {
                                    Project project = mediator.getEmbeddedDB().getProject(ProjectCell.this.getItem().id);
                                    return Pair.of(parseProject(project), null);
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
                                        mediator.get_2DDrawingsLoaded().clear();
                                        mediator.getExplorer().getStage().show();
                                        mediator.getRnartist().getStage().show();
                                        mediator.getRnartist().getStage().toFront();
                                        mediator.get_2DDrawingsLoaded().add(loadData.get().getLeft());
                                        mediator.canvas2D.load2D(mediator.get_2DDrawingsLoaded().get(mediator.get_2DDrawingsLoaded().size() - 1));

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
                name.setText(projectPanel.name);
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

    class NewProjectPanel extends ProjectPanel {
        public NewProjectPanel() {
            this.name = "New Project";
        }

        Image getImage() {
            try {
                return new Image(new File(new File(new File(mediator.getEmbeddedDB().getRootDir(),"images"),"user"),"New Project.png").toURI().toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
