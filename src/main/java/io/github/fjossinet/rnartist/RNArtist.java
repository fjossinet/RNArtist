package io.github.fjossinet.rnartist;

import io.github.fjossinet.rnartist.gui.*;
import io.github.fjossinet.rnartist.io.Backend;
import io.github.fjossinet.rnartist.io.ChimeraDriver;
import io.github.fjossinet.rnartist.core.model.*;
import io.github.fjossinet.rnartist.core.model.io.Rnaview;
import io.github.fjossinet.rnartist.model.ExplorerItem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
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
import javafx.util.StringConverter;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.dizitart.no2.NitriteId;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.toJSON;
import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.toSVG;
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

    //user defined global configurations
    private Slider detailsLevel, tertiariesLevel;
    private byte scope = BRANCH_SCOPE;
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
        if (RnartistConfig.getUserID() == null)
            new RegisterDialog(this);
        this.mediator = new Mediator(this);

        Screen screen = Screen.getPrimary();
        BorderPane root = new BorderPane();

        //++++++ Menus
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        Menu _2dDrawing = new Menu("2D Drawing");
        Menu windowsMenu = new Menu("Windows");

        menuBar.getMenus().addAll(fileMenu, _2dDrawing, windowsMenu);

        //++++++++ Project Menu
        MenuItem newItem = new MenuItem("New/Open Project...");

        newItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getProjectManager().getStage().show();
                mediator.getProjectManager().getStage().toFront();
            }
        });

        MenuItem saveasItem = new MenuItem("Save Project As...");

        saveasItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
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
                            NitriteId id = mediator.getEmbeddedDB().addProject(projectName.get().trim(), mediator.getCurrent2DDrawing());
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
            }
        });

        MenuItem saveItem = new MenuItem("Save Project");

        saveItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });

        MenuItem shareLayoutItem = new MenuItem("Share Layout");

        shareLayoutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Backend.shareLayout(mediator);
            }
        });

        Menu loadDataMenu = new Menu("Load Data from...");

        MenuItem filesMenuItem = new MenuItem("Files...");

        filesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
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
                                    String source = "file:"+file.getName();
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
                                        int countBefore = secondaryStructureDrawings.size();
                                        for (SecondaryStructure structure : new Rnaview().annotate(file)) {
                                            if (!structure.getHelices().isEmpty()) {
                                                structure.getRna().setSource(source);
                                                structure.setSource(source);
                                                secondaryStructureDrawings.add(new SecondaryStructureDrawing(structure, new WorkingSession()));
                                            }
                                        }
                                        if (countBefore < secondaryStructureDrawings.size()) {//RNAVIEW was able to annotate at least one RNA molecule
                                            if (mediator.getChimeraDriver() != null)
                                                mediator.getChimeraDriver().loadTertiaryStructure(file);
                                        } else { //we generate an Exception to show the Alert dialog
                                            throw new Exception("RNAVIEW was not able to annotate the 3D structure stored in " + file.getName());
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
                                        for (SecondaryStructureDrawing drawing : loadData.get().getLeft())
                                            mediator.get_2DDrawingsLoaded().add(drawing);
                                        //we load and fit (only if not a drawing from a JSON file) on the last 2D loaded
                                        mediator.canvas2D.load2D(mediator.get_2DDrawingsLoaded().get(mediator.get_2DDrawingsLoaded().size() - 1));
                                        if (mediator.getCurrent2DDrawing().getWorkingSession().getViewX() == 0.0 && mediator.getCurrent2DDrawing().getWorkingSession().getViewY() == 0.0 && mediator.getCurrent2DDrawing().getWorkingSession().getFinalZoomLevel() == 1.0)//this test allows to detect JSON files exported from RNArtist with a focus on a region
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

        MenuItem databasesMenuItem = new MenuItem("Databases");

        databasesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
              @Override
              public void handle(ActionEvent actionEvent) {
                  mediator.getWebBrowser().getStage().show();
                  mediator.getWebBrowser().getStage().toFront();
              }
        });

        /*databasesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TextInputDialog dialog = new TextInputDialog("RF03160");
                dialog.setTitle("Download 2Ds from Rfam");
                dialog.setHeaderText("Enter your Rfam ID");
                dialog.setContentText("Your ID:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(id -> {
                    javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>, Exception>> loadData = new javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>, Exception>>() {

                        @Override
                        protected Pair<List<SecondaryStructureDrawing>, Exception> call() {
                            List<SecondaryStructureDrawing> secondaryStructureDrawings = new ArrayList<SecondaryStructureDrawing>();
                            for (SecondaryStructure ss : ParsersKt.parseStockholm(new Rfam().getEntry(id.trim()))) {
                                ss.getRna().setSource("db:rfam:" + id);
                                ss.setSource("db:rfam:" + id);
                                secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), new Theme(), new WorkingSession()));
                            }
                            return Pair.of(secondaryStructureDrawings, null);
                        }
                    };
                    loadData.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            try {
                                for (SecondaryStructureDrawing drawing : loadData.get().getLeft())
                                    mediator.get_2DDrawingsLoaded().add(drawing);
                                //we load and fit on the last 2D loaded
                                mediator.canvas2D.load2D(mediator.get_2DDrawingsLoaded().get(mediator.get_2DDrawingsLoaded().size() - 1));
                                mediator.canvas2D.fitDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    new Thread(loadData).start();
                });
            }
        });*/

        MenuItem scratchMenuItem = new MenuItem("Scratch");

        scratchMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });

        loadDataMenu.getItems().addAll(filesMenuItem, databasesMenuItem, scratchMenuItem);

        Menu exportMenu = new Menu("Export 2D As...");

        MenuItem exportSVGMenuItem = new MenuItem("SVG...");

        exportSVGMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    fileChooser.setInitialDirectory(file.getParentFile());
                    PrintWriter writer;
                    try {
                        writer = new PrintWriter(file);
                        writer.println(toSVG(mediator.getCurrent2DDrawing(), mediator.getCanvas2D().getBounds().width, mediator.getCanvas2D().getBounds().height, mediator.getRnartist().getTertiariesLevel()));
                        writer.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        MenuItem exportCTMenuItem = new MenuItem("CT...");

        exportCTMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });

        MenuItem exportViennaMenuItem = new MenuItem("Vienna...");

        exportViennaMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });

        MenuItem exportJSONMenuItem = new MenuItem("JSON...");

        exportJSONMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    fileChooser.setInitialDirectory(file.getParentFile());
                    PrintWriter writer;
                    try {
                        writer = new PrintWriter(file);
                        writer.println(toJSON(mediator.getCurrent2DDrawing()));
                        writer.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        exportMenu.getItems().addAll(exportSVGMenuItem, exportCTMenuItem, exportViennaMenuItem, exportJSONMenuItem);

        fileMenu.getItems().addAll(newItem, loadDataMenu, saveasItem, saveItem, exportMenu);

        Menu residuesMenu = new Menu("Residues...");
        _2dDrawing.getItems().add(residuesMenu);

        Menu shapeMenu = new Menu("Shape...");
        residuesMenu.getItems().add(shapeMenu);

        MenuItem showShape = new MenuItem("Show");
        shapeMenu.getItems().add(showShape);
        showShape.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "true");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "true");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t, scope);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t, scope);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        MenuItem hideShape = new MenuItem("Hide");
        shapeMenu.getItems().add(hideShape);
        hideShape.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.fulldetails, "false");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t, scope);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t, scope);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu nameMenu = new Menu("Name...");
        residuesMenu.getItems().add(nameMenu);

        Menu showName = new Menu("Show...");
        nameMenu.getItems().add(showName);

        MenuItem inWhiteName = new MenuItem("in White");
        showName.getItems().add(inWhiteName);
        inWhiteName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.WHITE));
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.WHITE));
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.WHITE));
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.WHITE));
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.WHITE));

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t, scope);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t, scope);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        MenuItem inBlackName = new MenuItem("in Black");
        showName.getItems().add(inBlackName);
        inBlackName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.BLACK));
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.BLACK));
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.BLACK));
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.BLACK));
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.color, getHTMLColorString(java.awt.Color.BLACK));

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t, scope);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t, scope);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        MenuItem hideName = new MenuItem("Hide");
        nameMenu.getItems().add(hideName);
        hideName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.fulldetails, "false");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.fulldetails, "false");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t, scope);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t, scope);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu symbolsMenu = new Menu("Symbols...");
        _2dDrawing.getItems().add(symbolsMenu);

        final MenuItem defaultSymbol = new MenuItem(" Default");
        final MenuItem lwSymbol = new MenuItem("LW");

        symbolsMenu.getItems().add(defaultSymbol);
        defaultSymbol.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "false");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t, scope);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t, scope);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        symbolsMenu.getItems().add(lwSymbol);
        lwSymbol.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.fulldetails, "true");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t, scope);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t, scope);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu colorSchemesMenu = new Menu("Colors...");
        _2dDrawing.getItems().add(colorSchemesMenu);

        Menu branchesColors = new Menu("Branches");
        colorSchemesMenu.getItems().add(branchesColors);

        MenuItem randomColors = new MenuItem("Random");
        branchesColors.getItems().add(randomColors);

        randomColors.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (Branch branch: mediator.getCurrent2DDrawing().getBranches()) {
                    Random rand = new Random();
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();
                    java.awt.Color c = new java.awt.Color(r, g, b);
                    Map<String, Map<String,String>> configuration = new HashMap<>();
                    Map<String,String> colorConfig = new HashMap<>();
                    colorConfig.put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(c));
                    Map<String,String> letterColorConfig = new HashMap<>();
                    letterColorConfig.put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                    configuration.put(SecondaryStructureType.Helix.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.SecondaryInteraction.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.Junction.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.AShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.A.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.UShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.U.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.GShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.G.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.CShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.C.toString(), letterColorConfig);
                    configuration.put(SecondaryStructureType.XShape.toString(), colorConfig);
                    configuration.put(SecondaryStructureType.X.toString(), letterColorConfig);
                    Theme theme = new Theme(configuration);
                    for (TreeItem<ExplorerItem> item: mediator.getExplorer().getTreeViewItemsFor(mediator.getExplorer().getTreeTableView().getRoot(),branch.getParent()))
                        mediator.getExplorer().applyTheme( item, theme ,BRANCH_SCOPE);
                }

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        MenuItem distanceColors = new MenuItem("Distance");
        branchesColors.getItems().add(distanceColors);

        distanceColors.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (Branch branch: mediator.getCurrent2DDrawing().getBranches()) {
                    java.awt.Color c = branch.getParent().getColor();
                    List<JunctionDrawing> junctions = branch.junctionsFromBranch();
                    for (JunctionDrawing junction:junctions) {
                            List<DrawingElement> path = junction.pathToRoot();
                        java.awt.Color interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate( javafx.scene.paint.Color.LIGHTGRAY, (double)(path.size()-1)/(double)branch.getBranchLength()));
                        junction.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                        for (ResidueDrawing r:junction.getResidues())
                            r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                        interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate( javafx.scene.paint.Color.LIGHTGRAY, (double)(path.size()-2)/(double)branch.getBranchLength()));
                        junction.getParent().getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                        for (ResidueDrawing r:junction.getParent().getResidues())
                            r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(interpolatedColor));
                    }
                }
                for (SingleStrandDrawing ss: mediator.getCurrent2DDrawing().getSingleStrands()) {
                    ss.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                    for (ResidueDrawing r: ss.getResidues())
                        r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                }
                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu colorSchemes = new Menu("Residues...");

        colorSchemesMenu.getItems().add(colorSchemes);

        for (String themeName : RnartistConfig.colorSchemes.keySet()) {
            MenuItem residueThemeItem = new MenuItem(themeName);
            residueThemeItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                    Theme t = new Theme(RnartistConfig.colorSchemes.get(themeName));

                    if (!selection.isEmpty()) {
                        for (TreeItem<ExplorerItem> item:selection)
                            mediator.getExplorer().applyTheme(item, t, scope);
                    } else
                        mediator.getExplorer().applyTheme(t, scope);

                    mediator.getCanvas2D().repaint();
                    mediator.getExplorer().refresh();
                }
            });
            colorSchemes.getItems().add(residueThemeItem);
        }

        //++++++++ Windows Menu
        MenuItem toolboxItem = new MenuItem("Settings");

        toolboxItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getSettings().getStage().show();
                mediator.getSettings().getStage().toFront();
            }
        });

        MenuItem explorerItem = new MenuItem("Explorer");

        explorerItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                mediator.getExplorer().getStage().show();
                mediator.getExplorer().getStage().toFront();
            }
        });

        MenuItem chimeraItem = new MenuItem("Chimera");

        chimeraItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (mediator.getChimeraDriver() != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Please Confirm");
                    alert.setHeaderText("A Chimera windows is already linked to RNArtist");
                    alert.setContentText("The new one will replace it for the linkage");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                        new ChimeraDriver(mediator);
                    }
                } else
                    new ChimeraDriver(mediator);
            }
        });

        windowsMenu.getItems().addAll(toolboxItem, explorerItem, chimeraItem);

        //++++++ Toolbar

        ToolBar toolbar = new ToolBar();

        toolbar.setPadding(new Insets(5, 10, 10, 10));

        GridPane structureSelection = new GridPane();
        structureSelection.setVgap(5.0);
        structureSelection.setHgap(5.0);

        Label l = new Label("2Ds Available");
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
                        if (drawing != mediator.getCurrent2DDrawing())
                            toDelete.add(drawing);
                    mediator.get_2DDrawingsLoaded().removeAll(toDelete);
                }
            }
        });

       allStructuresAvailable.getItems().addAll(new SeparatorMenuItem(), clearAll2DsItem, clearAll2DsExceptCurrentItem);

        GridPane.setHalignment(allStructuresAvailable, HPos.CENTER);
        GridPane.setConstraints(allStructuresAvailable, 0, 1);
        structureSelection.getChildren().add(allStructuresAvailable);

        GridPane allButtons = new GridPane();
        allButtons.setVgap(5.0);
        allButtons.setHgap(5.0);

        l = new Label("2D");
        GridPane.setConstraints(l, 0, 0);
        allButtons.getChildren().add(l);

        Button center2D = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.CROSSHAIRS));
        center2D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.isShiftDown()) {
                    setCenterDisplayOnSelection(!isCenterDisplayOnSelection());
                    if (isCenterDisplayOnSelection())
                        center2D.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.LOCK));
                    else
                        center2D.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.CROSSHAIRS));
                }
                else if (mediator.getCurrent2DDrawing() != null) {
                    java.awt.geom.Rectangle2D selectionFrame = mediator.getCanvas2D().getSelectionFrame();
                    if (selectionFrame == null)
                        mediator.getCanvas2D().centerDisplayOn(mediator.getCurrent2DDrawing().getFrame());
                    else
                        mediator.getCanvas2D().centerDisplayOn(selectionFrame);
                }
            }
        });
        center2D.setTooltip(new Tooltip("Center 2D"));
        GridPane.setConstraints(center2D, 1, 0);
        allButtons.getChildren().add(center2D);

        Button fit2D = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.ARROWS_ALT));
        fit2D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediator.getCanvas2D().fitStructure();
            }
        });
        fit2D.setTooltip(new Tooltip("Fit 2D"));
        GridPane.setConstraints(fit2D, 2, 0);
        allButtons.getChildren().add(fit2D);

        l = new Label("3D");
        GridPane.setConstraints(l, 0, 1);
        allButtons.getChildren().add(l);

        Button set3DPivot = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.UNDO));
        set3DPivot.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mediator.getCurrent2DDrawing() != null && mediator.getChimeraDriver() != null)
                    mediator.pivotInChimera();
            }
        });
        set3DPivot.setTooltip(new Tooltip("Define Selection as Pivot"));
        GridPane.setConstraints(set3DPivot, 1, 1);
        allButtons.getChildren().add(set3DPivot);

        Button focus3D = new Button(null, new Glyph("FontAwesome", FontAwesome.Glyph.EYE));
        focus3D.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mediator.getCurrent2DDrawing() != null && mediator.getChimeraDriver() != null)
                    mediator.focusInChimera();
            }
        });
        focus3D.setTooltip(new Tooltip("Focus 3D on Selection"));
        GridPane.setConstraints(focus3D, 2, 1);
        allButtons.getChildren().add(focus3D);

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

        GridPane detailsLevel = new GridPane();
        detailsLevel.setVgap(5.0);
        detailsLevel.setHgap(5.0);

        l = new Label("Details Level");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        detailsLevel.getChildren().add(l);

        this.detailsLevel = new Slider(1, 5, 1);
        this.detailsLevel.setShowTickLabels(true);
        this.detailsLevel.setShowTickMarks(true);
        this.detailsLevel.setSnapToTicks(true);
        this.detailsLevel.setMajorTickUnit(1);
        this.detailsLevel.setMinorTickCount(0);

        this.detailsLevel.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                List<StructuralDomain> structuralDomainsSelected = mediator.getCanvas2D().structuralDomainsSelected();
                Theme t = new Theme(RnartistConfig.themeDetailsLevel.get((int) RNArtist.this.detailsLevel.getValue()-1));
                //if at least one structural domain selected, apply level inside or from these elements
                if (!structuralDomainsSelected.isEmpty()) {
                    byte _scope = scope;
                    if (_scope == ELEMENT_SCOPE)
                        _scope = STRUCTURAL_DOMAIN_SCOPE; //not working well if ELEMENT SCOPE, since only parts of the details above lvl 1 can be applied
                    for (TreeItem<ExplorerItem> item : mediator.getExplorer().getSelectedTreeViewItems()) {
                        mediator.getExplorer().applyTheme(item, t, _scope);
                    }
                }
                //else apply branch scope along all branches and single strands drawn
                else {
                    List<DrawingElement> drawingElements = new ArrayList<>();
                    for (JunctionDrawing j:mediator.getWorkingSession().getBranchesDrawn())
                        drawingElements.add(j.getParent());
                    drawingElements.addAll(mediator.getWorkingSession().getSingleStrandsDrawn());
                    List<TreeItem<ExplorerItem>> hits = mediator.getExplorer().getAllTreeViewItems(null, mediator.getExplorer().getTreeTableView().getRoot(), drawingElement -> drawingElements.contains(drawingElement), RNArtist.BRANCH_SCOPE);
                    for (TreeItem<ExplorerItem> item: hits)
                        mediator.getExplorer().applyTheme(item, t, BRANCH_SCOPE);
                }
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setHalignment(this.detailsLevel, HPos.CENTER);
        GridPane.setConstraints(this.detailsLevel, 0, 1);
        detailsLevel.getChildren().add(this.detailsLevel);

        GridPane scopeLevel = new GridPane();
        scopeLevel.setVgap(5.0);
        scopeLevel.setHgap(5.0);
        l = new Label("Scope");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        scopeLevel.getChildren().add(l);

        HBox scopeButtons = new HBox();
        scopeButtons.setSpacing(5);

        ToggleButton currentElementScope = new ToggleButton(null, new FontIcon("fas-plus:15"));
        ToggleButton up2NextDomainsScope = new ToggleButton(null, new FontIcon("fas-compress:15"));
        ToggleButton branchesScope = new ToggleButton(null, new FontIcon("fas-expand:15"));

        currentElementScope.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                currentElementScope.setSelected(true);
                scope = ELEMENT_SCOPE;
                up2NextDomainsScope.setSelected(false);
                branchesScope.setSelected(false);
            }
        });
        currentElementScope.setTooltip(new Tooltip("Element selected"));
        scopeButtons.getChildren().add(currentElementScope);

        up2NextDomainsScope.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                up2NextDomainsScope.setSelected(true);
                scope = STRUCTURAL_DOMAIN_SCOPE;
                currentElementScope.setSelected(false);
                branchesScope.setSelected(false);
            }
        });
        up2NextDomainsScope.setTooltip(new Tooltip("Up to next Structural Domains"));
        scopeButtons.getChildren().add(up2NextDomainsScope);

        branchesScope.setSelected(true);
        branchesScope.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                branchesScope.setSelected(true);
                scope = BRANCH_SCOPE;
                currentElementScope.setSelected(false);
                up2NextDomainsScope.setSelected(false);
            }
        });
        branchesScope.setTooltip(new Tooltip("Along branches"));
        scopeButtons.getChildren().add(branchesScope);

        GridPane.setHalignment(scopeButtons, HPos.CENTER);
        GridPane.setConstraints(scopeButtons, 0, 1);
        scopeLevel.getChildren().add(scopeButtons);

        GridPane tertiariesLevel = new GridPane();
        tertiariesLevel.setVgap(5.0);
        tertiariesLevel.setHgap(5.0);

        l = new Label("Tertiaries Level");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        tertiariesLevel.getChildren().add(l);

        this.tertiariesLevel = new Slider(1, 3, 1);
        this.tertiariesLevel.setShowTickLabels(true);
        this.tertiariesLevel.setShowTickMarks(true);
        this.tertiariesLevel.setSnapToTicks(true);
        this.tertiariesLevel.setMajorTickUnit(1);
        this.tertiariesLevel.setMinorTickCount(0);

        this.tertiariesLevel.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 1.0) return "None";
                if (n == 2.0) return "Pknots";
                if (n == 3.0) return "All";

                return "";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "None":
                        return 1d;
                    case "Pknots":
                        return 2d;
                    case "All":
                        return 3d;

                    default:
                        return 0d;
                }
            }
        });

        this.tertiariesLevel.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                switch ((int)RNArtist.this.tertiariesLevel.getValue()) {
                    case 1: mediator.getWorkingSession().setTertiariesDisplayLevel(TertiariesDisplayLevel.None); break;
                    case 2: mediator.getWorkingSession().setTertiariesDisplayLevel(TertiariesDisplayLevel.Pknots); break;
                    case 3: mediator.getWorkingSession().setTertiariesDisplayLevel(TertiariesDisplayLevel.All); break;
                }
                mediator.getCanvas2D().repaint();
            }
        });

        GridPane.setHalignment(this.tertiariesLevel, HPos.CENTER);
        GridPane.setConstraints(this.tertiariesLevel, 0, 1);
        tertiariesLevel.getChildren().add(this.tertiariesLevel);

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

        Separator s = new Separator();
        s.setPadding(new Insets(0, 5, 0, 5));

        Separator s1 = new Separator();
        s.setPadding(new Insets(0, 5, 0, 5));

        Separator s2 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        Separator s3 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        Separator s4 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        toolbar.getItems().addAll(allButtons, s, scopeLevel, s1, detailsLevel, s2, tertiariesLevel, s3, selectionSize, selectionColor, s4, structureSelection);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(0, 0, 0, 0));
        vbox.setSpacing(5);
        vbox.getChildren().add(menuBar);
        vbox.getChildren().add(toolbar);

        root.setTop(vbox);

        //++++++ Canvas2D
        final SwingNode swingNode = new SwingNode();
        swingNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                AffineTransform at = new AffineTransform();
                at.translate(mediator.getWorkingSession().getViewX(), mediator.getWorkingSession().getViewY());
                at.scale(mediator.getWorkingSession().getFinalZoomLevel(), mediator.getWorkingSession().getFinalZoomLevel());
                for (JunctionKnob knob : mediator.getCanvas2D().getKnobs()) {
                    if (knob.contains(mouseEvent.getX(), mouseEvent.getY(), at))
                        return;
                }
                if (mediator.getCurrent2DDrawing() != null) {
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
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getCurrent2DDrawing().setQuickDraw(true);
                double transX = mouseEvent.getX() - mediator.getCanvas2D().getTranslateX();
                double transY = mouseEvent.getY() - mediator.getCanvas2D().getTranslateY();
                mediator.getWorkingSession().moveView(transX, transY);
                mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getCurrent2DDrawing().setQuickDraw(false);
                mediator.getCanvas2D().setTranslateX(0.0);
                mediator.getCanvas2D().setTranslateY(0.0);
                mediator.getCanvas2D().repaint();
            }
        });
        swingNode.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY && mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getCanvas2D().setTranslateX(mouseEvent.getX());
                mediator.getCanvas2D().setTranslateY(mouseEvent.getY());
            }
        });
        swingNode.setOnScroll(scrollEvent -> {
            if (mediator.getCanvas2D().getSecondaryStructureDrawing() != null) {
                mediator.getCurrent2DDrawing().setQuickDraw(true);
                scrollCounter++;

                Thread th = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        if (scrollCounter == 1) {
                            mediator.getCurrent2DDrawing().setQuickDraw(false);
                            mediator.getCanvas2D().repaint();
                        }
                        scrollCounter--;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
                th.setDaemon(true);
                th.start();
                Point2D.Double realMouse = new Point2D.Double(((double) scrollEvent.getX() - mediator.getWorkingSession().getViewX()) / mediator.getWorkingSession().getFinalZoomLevel(), ((double) scrollEvent.getY() - mediator.getWorkingSession().getViewY()) / mediator.getWorkingSession().getFinalZoomLevel());
                double notches = scrollEvent.getDeltaY();
                if (notches < 0)
                    mediator.getWorkingSession().setZoom(1.25);
                if (notches > 0)
                    mediator.getWorkingSession().setZoom(1.0 / 1.25);
                Point2D.Double newRealMouse = new Point2D.Double(((double) scrollEvent.getX() - mediator.getWorkingSession().getViewX()) / mediator.getWorkingSession().getFinalZoomLevel(), ((double) scrollEvent.getY() - mediator.getWorkingSession().getViewY()) / mediator.getWorkingSession().getFinalZoomLevel());
                mediator.getWorkingSession().moveView((newRealMouse.getX() - realMouse.getX()) * mediator.getWorkingSession().getFinalZoomLevel(), (newRealMouse.getY() - realMouse.getY()) * mediator.getWorkingSession().getFinalZoomLevel());
                mediator.getCanvas2D().repaint();
            }
        });
        createSwingContent(swingNode);
        root.setCenter(swingNode);

        //### Status Bar
        this.statusBar = new FlowPane();
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
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
        stage.setScene(scene);
        stage.setTitle("RNArtist");

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        int width = (int) (screenSize.getWidth() * 4.0 / 10.0);
        scene.getWindow().setWidth(screenSize.getWidth() - width);
        scene.getWindow().setHeight(screenSize.getHeight());
        scene.getWindow().setX(0);
        scene.getWindow().setY(0);

        mediator.getExplorer().getStage().show();
        mediator.getRnartist().getStage().show();
        mediator.getRnartist().getStage().toFront();
    }

    public boolean isCenterDisplayOnSelection() {
        return centerDisplayOnSelection;
    }

    public void setCenterDisplayOnSelection(boolean centerDisplayOnSelection) {
        this.centerDisplayOnSelection = centerDisplayOnSelection;
    }

    public byte getScope() {
        return scope;
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

    public Slider getDetailsLevel() {
        return detailsLevel;
    }

    public TertiariesDisplayLevel getTertiariesLevel() {
        switch ((int)RNArtist.this.tertiariesLevel.getValue()) {
            case 1: return TertiariesDisplayLevel.None;
            case 2: return TertiariesDisplayLevel.Pknots;
            default: return TertiariesDisplayLevel.All;
        }
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
