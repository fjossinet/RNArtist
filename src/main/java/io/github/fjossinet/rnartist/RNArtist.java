package io.github.fjossinet.rnartist;

import io.github.fjossinet.rnartist.gui.*;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.github.fjossinet.rnartist.core.model.DrawingsKt.getHTMLColorString;
import static io.github.fjossinet.rnartist.io.UtilsKt.awtColorToJavaFX;
import static io.github.fjossinet.rnartist.io.UtilsKt.javaFXToAwt;

public class RNArtist extends Application {

    private Mediator mediator;
    private Stage stage;
    private int scrollCounter = 0;
    private FlowPane statusBar;
    private Menu userThemesMenu, currentThemeMenu;
    private MenuButton allStructuresAvailable;
    private MenuItem updateSavedThemeItem, clearAll2DsItem, clearAll2DsExceptCurrentItem;
    private Slider detailsLevel, selectionLevel;

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
                    if (RnartistConfig.saveCurrentThemeOnExit())
                        RnartistConfig.save(mediator.getTheme().getConfigurations(), (org.apache.commons.lang3.tuple.Pair<String, NitriteId>) currentThemeMenu.getUserData());
                    else
                        RnartistConfig.save(null, null);
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
        RnartistConfig.save(null, null);

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

        Menu loadDataMenu = new Menu("Load Data from...");

        MenuItem filesMenuItem = new MenuItem("Files...");

        filesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                List<File> files = fileChooser.showOpenMultipleDialog(stage);
                for (File file:files) {
                    fileChooser.setInitialDirectory(file.getParentFile());
                    javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>, Exception>> loadData = new javafx.concurrent.Task<Pair<List<SecondaryStructureDrawing>, Exception>>() {

                        @Override
                        protected Pair<List<SecondaryStructureDrawing>, Exception> call() {
                            SecondaryStructure ss = null;
                            List<SecondaryStructureDrawing> secondaryStructureDrawings = new ArrayList<SecondaryStructureDrawing>();
                            try {
                                if (file.getName().endsWith(".ct")) {
                                    ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseCT(new FileReader(file));
                                    if (ss != null) {
                                        ss.getRna().setSource(file.getName());
                                        secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), new Theme(), new WorkingSession()));
                                    }
                                } else if (file.getName().endsWith(".bpseq")) {
                                    ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseBPSeq(new FileReader(file));
                                    if (ss != null) {
                                        ss.getRna().setSource(file.getName());
                                        secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), new Theme(), new WorkingSession()));
                                    }
                                } else if (file.getName().endsWith(".fasta") || file.getName().endsWith(".fas") || file.getName().endsWith(".vienna")) {
                                    ss = io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseVienna(new FileReader(file));
                                    if (ss != null) {
                                        ss.getRna().setSource(file.getName());
                                        secondaryStructureDrawings.add(new SecondaryStructureDrawing(ss, mediator.getCanvas2D().getBounds(), new Theme(), new WorkingSession()));
                                    }

                                } else if (file.getName().endsWith(".xml") || file.getName().endsWith(".rnaml")) {
                                    for (SecondaryStructure structure: io.github.fjossinet.rnartist.core.model.io.ParsersKt.parseRnaml(file)) {
                                        if (!structure.getHelices().isEmpty()) {
                                            structure.getRna().setSource(file.getName());
                                            secondaryStructureDrawings.add(new SecondaryStructureDrawing(structure, mediator.getCanvas2D().getBounds(), new Theme(), new WorkingSession()));
                                        }
                                    }

                                } else if (file.getName().endsWith(".pdb")) {
                                    int countBefore = secondaryStructureDrawings.size();
                                    for (SecondaryStructure structure: new Rnaview().annotate(file)) {
                                        if (!structure.getHelices().isEmpty()) {
                                            structure.getRna().setSource(file.getName());
                                            secondaryStructureDrawings.add(new SecondaryStructureDrawing(structure, mediator.getCanvas2D().getBounds(), new Theme(), new WorkingSession()));
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
                                        structure.getRna().setSource(file.getName());
                                        secondaryStructureDrawings.add(new SecondaryStructureDrawing(structure, mediator.getCanvas2D().getBounds(), new Theme(), new WorkingSession()));
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
                                    //we load and fit on the last 2D loaded
                                    mediator.canvas2D.load2D(mediator.get_2DDrawingsLoaded().get(mediator.get_2DDrawingsLoaded().size() - 1));
                                    mediator.canvas2D.fitDisplayOn(mediator.getCurrent2DDrawing().getBounds());
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

        MenuItem databasesMenuItem = new MenuItem("Databases...");

        databasesMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

            }
        });

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
                        writer.println(mediator.getCurrent2DDrawing().asSVG());
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

        exportMenu.getItems().addAll(exportSVGMenuItem, exportCTMenuItem, exportViennaMenuItem);

        fileMenu.getItems().addAll(newItem, loadDataMenu, saveasItem, saveItem, exportMenu);

        //++++++++ Themes Menu

        this.currentThemeMenu = new Menu("Current Configuration...");

        MenuItem saveAsCurrentTheme = new MenuItem("Save As new Theme");

        saveAsCurrentTheme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Theme Saving");
                dialog.setHeaderText("Choose a Name for your Theme");

                Optional<String> response = dialog.showAndWait();

                response.ifPresent(name -> {
                    NitriteId id = mediator.getEmbeddedDB().addTheme(name, mediator.getTheme());
                    if (id != null) {
                        org.apache.commons.lang3.tuple.Pair<String, NitriteId> theme = org.apache.commons.lang3.tuple.Pair.of(name, id);
                        userThemesMenu.getItems().add(createSavedThemeItem(theme));
                        setCurrentTheme(theme); //this will reload the theme, but without any impact
                    }
                });
            }
        });

        this.updateSavedThemeItem = new MenuItem("Update");
        if (RnartistConfig.getLastThemeSavedId() != null)
            this.updateSavedThemeItem.setUserData(RnartistConfig.getLastThemeSavedId());
        else
            this.updateSavedThemeItem.setDisable(true);
        updateSavedThemeItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure to update your Theme?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    org.apache.commons.lang3.tuple.Pair<String, NitriteId> theme = (org.apache.commons.lang3.tuple.Pair<String, NitriteId>) updateSavedThemeItem.getUserData();
                    mediator.getEmbeddedDB().updateTheme(theme.getValue(), mediator.getTheme().getConfigurations());
                }
            }
        });

        currentThemeMenu.getItems().addAll(updateSavedThemeItem, saveAsCurrentTheme);

        Menu residuesMenu = new Menu("Residues...");

        Menu shapeMenu = new Menu("Shape...");
        residuesMenu.getItems().add(shapeMenu);

        MenuItem showShape = new MenuItem("Show");
        shapeMenu.getItems().add(showShape);
        showShape.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.FullDetails, "true");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t);

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
                t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.FullDetails, "false");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu nameMenu = new Menu("Name...");
        residuesMenu.getItems().add(nameMenu);

        MenuItem showName = new MenuItem("Show");
        nameMenu.getItems().add(showName);
        showName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.FullDetails, "true");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.FullDetails, "true");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t);

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
                t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.FullDetails, "false");
                t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.FullDetails, "false");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu symbolsMenu = new Menu("Symbols...");

        final MenuItem defaultSymbol = new MenuItem(" Default");
        final MenuItem lwSymbol = new MenuItem("LW");

        symbolsMenu.getItems().add(defaultSymbol);
        defaultSymbol.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.FullDetails, "false");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t);

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
                t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.FullDetails, "true");

                if (!selection.isEmpty()) {
                    for (TreeItem<ExplorerItem> item:selection)
                        mediator.getExplorer().applyTheme(item, t);
                } else if (mediator.getCurrent2DDrawing() != null)
                    mediator.getExplorer().applyTheme(t);

                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        Menu colorsThemeMenu = new Menu("Colors...");

        MenuItem branchingColors = new MenuItem("Branching");

        branchingColors.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (Branch branch: mediator.getCurrent2DDrawing().getBranches()) {
                    java.awt.Color c = branch.getParent().getColor();
                    List<JunctionDrawing> junctions = branch.junctionsFromBranch();
                    for (JunctionDrawing junction:junctions) {
                            List<DrawingElement> path = junction.pathToRoot();
                        java.awt.Color interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate( javafx.scene.paint.Color.LIGHTGRAY, (double)(path.size()-1)/(double)branch.getBranchLength()));
                        junction.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(interpolatedColor));
                        for (ResidueDrawing r:junction.getResidues())
                            r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(interpolatedColor));
                        interpolatedColor = javaFXToAwt(awtColorToJavaFX(c).interpolate( javafx.scene.paint.Color.LIGHTGRAY, (double)(path.size()-2)/(double)branch.getBranchLength()));
                        junction.getParent().getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(interpolatedColor));
                        for (ResidueDrawing r:junction.getParent().getResidues())
                            r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(interpolatedColor));
                    }
                }
                for (SingleStrandDrawing ss: mediator.getCurrent2DDrawing().getSingleStrands()) {
                    ss.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                    for (ResidueDrawing r: ss.getResidues())
                        r.getDrawingConfiguration().getParams().put(DrawingConfigurationParameter.Color.toString(), getHTMLColorString(java.awt.Color.BLACK));
                }
                mediator.getCanvas2D().repaint();
                mediator.getExplorer().refresh();
            }
        });

        colorsThemeMenu.getItems().add(branchingColors);

        Menu colorSchemes = new Menu("Schemes...");

        colorsThemeMenu.getItems().add(colorSchemes);

        for (String themeName : RnartistConfig.colorSchemes.keySet()) {
            MenuItem residueThemeItem = new MenuItem(themeName);
            residueThemeItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                    Theme t = new Theme(RnartistConfig.colorSchemes.get(themeName));

                    if (!selection.isEmpty()) {
                        for (TreeItem<ExplorerItem> item:selection)
                            mediator.getExplorer().applyTheme(item, t);
                    } else
                        mediator.getExplorer().applyTheme(t);

                    mediator.getCanvas2D().repaint();
                    mediator.getExplorer().refresh();
                }
            });
            colorSchemes.getItems().add(residueThemeItem);
        }

        this.userThemesMenu = new Menu("Your Themes...");

        for (Document doc : mediator.getEmbeddedDB().getThemes().find())
            userThemesMenu.getItems().add(createSavedThemeItem(org.apache.commons.lang3.tuple.Pair.of((String) doc.get("name"), doc.getId())));

        Menu communityThemesMenu = new Menu("Community Themes");

        _2dDrawing.getItems().addAll(residuesMenu, symbolsMenu, colorsThemeMenu, this.currentThemeMenu, userThemesMenu);

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
                    RnartistConfig.setCenterDisplayOnSelection(!RnartistConfig.getCenterDisplayOnSelection());
                    if (RnartistConfig.getCenterDisplayOnSelection())
                        center2D.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.LOCK));
                    else
                        center2D.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.CROSSHAIRS));
                }
                else if (mediator.getCurrent2DDrawing() != null) {
                    if (mediator.getCurrent2DDrawing().getSelection().isEmpty())
                        mediator.getCanvas2D().centerDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                    else
                        mediator.getCanvas2D().centerDisplayOn(mediator.getCurrent2DDrawing().getSelectionBounds());
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
                if (mouseEvent.isShiftDown()) {
                    RnartistConfig.setFitDisplayOnSelection(!RnartistConfig.getFitDisplayOnSelection());
                    if (RnartistConfig.getFitDisplayOnSelection()) {
                        fit2D.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.LOCK));
                    }
                    else
                        fit2D.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.ARROWS_ALT));
                }
                else if (mediator.getCurrent2DDrawing() != null) {
                    if (mediator.getCurrent2DDrawing().getSelection().isEmpty())
                        mediator.getCanvas2D().fitDisplayOn(mediator.getCurrent2DDrawing().getBounds());
                    else
                        mediator.getCanvas2D().fitDisplayOn(mediator.getCurrent2DDrawing().getSelectionBounds());
                }
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

        l = new Label("Selection Halo (px)");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        selectionSize.getChildren().add(l);

        final Slider sliderSize = new Slider(0, 20, RnartistConfig.getSelectionSize());
        sliderSize.setShowTickLabels(true);
        sliderSize.setShowTickMarks(true);
        sliderSize.setMajorTickUnit(10);
        sliderSize.setMinorTickCount(5);
        sliderSize.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                RnartistConfig.setSelectionSize((int) sliderSize.getValue());
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
                ObservableList<TreeItem<ExplorerItem>> selection = mediator.getExplorer().getSelectedTreeViewItems();
                Theme t = new Theme(new HashMap<String, Map<String,String>>());
                switch ((int) RNArtist.this.detailsLevel.getValue()) {
                    case 1:
                        t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.FullDetails, "false");

                        if (!selection.isEmpty()) {
                            for (TreeItem<ExplorerItem> item:selection)
                                mediator.getExplorer().applyTheme(item, t);
                        } else {
                            List<DrawingElement> drawingElements = new ArrayList<>();
                            for (JunctionDrawing j:mediator.getWorkingSession().getBranchesDrawn())
                                drawingElements.add(j.getParent());
                            drawingElements.addAll(mediator.getWorkingSession().getSingleStrandsDrawn());
                            List<TreeItem<ExplorerItem>> hits = mediator.getExplorer().getAllTreeViewItems(null, mediator.getExplorer().getTreeTableView().getRoot(), drawingElement -> drawingElements.contains(drawingElement));
                            for (TreeItem<ExplorerItem> item: hits)
                                mediator.getExplorer().applyTheme(item, t);
                        }
                        break;
                    case 2: {
                        t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.FullDetails, "false");

                        if (!selection.isEmpty()) {
                            for (TreeItem<ExplorerItem> item:selection)
                                mediator.getExplorer().applyTheme(item, t);
                        } else {
                            List<DrawingElement> drawingElements = new ArrayList<>();
                            for (JunctionDrawing j:mediator.getWorkingSession().getBranchesDrawn())
                                drawingElements.add(j.getParent());
                            drawingElements.addAll(mediator.getWorkingSession().getSingleStrandsDrawn());
                            List<TreeItem<ExplorerItem>> hits = mediator.getExplorer().getAllTreeViewItems(null, mediator.getExplorer().getTreeTableView().getRoot(), drawingElement -> drawingElements.contains(drawingElement));
                            for (TreeItem<ExplorerItem> item: hits)
                                mediator.getExplorer().applyTheme(item, t);
                        }
                    };
                    break;
                    case 3: {
                        t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.FullDetails, "false");
                        t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.FullDetails, "false");

                        if (!selection.isEmpty()) {
                            for (TreeItem<ExplorerItem> item:selection)
                                mediator.getExplorer().applyTheme(item, t);
                        } else {
                            List<DrawingElement> drawingElements = new ArrayList<>();
                            for (JunctionDrawing j:mediator.getWorkingSession().getBranchesDrawn())
                                drawingElements.add(j.getParent());
                            drawingElements.addAll(mediator.getWorkingSession().getSingleStrandsDrawn());
                            List<TreeItem<ExplorerItem>> hits = mediator.getExplorer().getAllTreeViewItems(null, mediator.getExplorer().getTreeTableView().getRoot(), drawingElement -> drawingElements.contains(drawingElement));
                            for (TreeItem<ExplorerItem> item: hits)
                                mediator.getExplorer().applyTheme(item, t);
                        }
                    } ; break;
                    case 4: {
                        t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.FullDetails, "false");

                        if (!selection.isEmpty()) {
                            for (TreeItem<ExplorerItem> item:selection)
                                mediator.getExplorer().applyTheme(item, t);
                        } else {
                            List<DrawingElement> drawingElements = new ArrayList<>();
                            for (JunctionDrawing j:mediator.getWorkingSession().getBranchesDrawn())
                                drawingElements.add(j.getParent());
                            drawingElements.addAll(mediator.getWorkingSession().getSingleStrandsDrawn());
                            List<TreeItem<ExplorerItem>> hits = mediator.getExplorer().getAllTreeViewItems(null, mediator.getExplorer().getTreeTableView().getRoot(), drawingElement -> drawingElements.contains(drawingElement));
                            for (TreeItem<ExplorerItem> item: hits)
                                mediator.getExplorer().applyTheme(item, t);
                        }
                    } ; break;
                    case 5 : {
                        t.setConfigurationFor(SecondaryStructureType.Helix, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.Junction, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.SingleStrand, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.AShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.A, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.UShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.U, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.GShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.G, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.CShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.C, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.XShape, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.X, DrawingConfigurationParameter.FullDetails, "true");
                        t.setConfigurationFor(SecondaryStructureType.InteractionSymbol, DrawingConfigurationParameter.FullDetails, "true");

                        if (!selection.isEmpty()) {
                            for (TreeItem<ExplorerItem> item:selection)
                                mediator.getExplorer().applyTheme(item, t);
                        } else {
                            List<DrawingElement> drawingElements = new ArrayList<>();
                            for (JunctionDrawing j:mediator.getWorkingSession().getBranchesDrawn())
                                drawingElements.add(j.getParent());
                            drawingElements.addAll(mediator.getWorkingSession().getSingleStrandsDrawn());
                            List<TreeItem<ExplorerItem>> hits = mediator.getExplorer().getAllTreeViewItems(null, mediator.getExplorer().getTreeTableView().getRoot(), drawingElement -> drawingElements.contains(drawingElement));
                            for (TreeItem<ExplorerItem> item: hits)
                                mediator.getExplorer().applyTheme(item, t);
                        }
                    } ; break;
                }
                mediator.getExplorer().refresh();
                mediator.getCanvas2D().repaint();
            }
        });
        GridPane.setHalignment(this.detailsLevel, HPos.CENTER);
        GridPane.setConstraints(this.detailsLevel, 0, 1);
        detailsLevel.getChildren().add(this.detailsLevel);

        GridPane selectionLevel = new GridPane();
        selectionLevel.setVgap(5.0);
        selectionLevel.setHgap(5.0);

        l = new Label("Selection Level");
        GridPane.setHalignment(l, HPos.CENTER);
        GridPane.setConstraints(l, 0, 0);
        selectionLevel.getChildren().add(l);

        this.selectionLevel = new Slider(1, 3, 1);
        this.selectionLevel.setShowTickLabels(true);
        this.selectionLevel.setShowTickMarks(true);
        this.selectionLevel.setSnapToTicks(true);
        this.selectionLevel.setMajorTickUnit(1);
        this.selectionLevel.setMinorTickCount(0);

        GridPane.setHalignment(this.selectionLevel, HPos.CENTER);
        GridPane.setConstraints(this.selectionLevel, 0, 1);
        selectionLevel.getChildren().add(this.selectionLevel);

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

        Separator s2 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        Separator s3 = new Separator();
        s2.setPadding(new Insets(0, 5, 0, 5));

        toolbar.getItems().addAll(allButtons, s, detailsLevel, s2, selectionLevel, selectionSize, selectionColor, s3, structureSelection);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(0, 0, 0, 0));
        vbox.setSpacing(5);
        vbox.getChildren().add(menuBar);
        vbox.getChildren().add(toolbar);

        root.setTop(vbox);

        //++++++ Canvas2D
        final SwingNode swingNode = new SwingNode();
        swingNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.isControlDown() || mouseEvent.isAltDown()) {
                mediator.getCanvas2D().clearSelection();
                mediator.getExplorer().clearSelection();
                return;
            } else if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                AffineTransform at = new AffineTransform();
                at.translate(mediator.getWorkingSession().getViewX(), mediator.getWorkingSession().getViewY());
                at.scale(mediator.getWorkingSession().getFinalZoomLevel(), mediator.getWorkingSession().getFinalZoomLevel());
                for (Knob knob : mediator.getCanvas2D().getKnobs()) {
                    if (knob.contains(mouseEvent.getX(), mouseEvent.getY(), at))
                        return;
                }
                if (mediator.getCurrent2DDrawing() != null) {
                    for (JunctionDrawing j:mediator.getWorkingSession().getJunctionsDrawn()) {
                        for (ResidueDrawing c : j.getResidues()) {
                            if ((c.isFullDetails() || c.residueLetter.isFullDetails()) && at.createTransformedShape(c.getCircle()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                if (!c.getSelected()) {
                                    mediator.getCanvas2D().addToSelection(c);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> c.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                } else {
                                    DrawingElement p = c.getParent();
                                    while (p != null && p.getSelected()) {
                                        p = p.getParent();
                                    }
                                    if (p == null) {
                                        mediator.getCanvas2D().clearSelection();
                                        mediator.getCanvas2D().addToSelection(c);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> c.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                    } else {
                                        DrawingElement _p = p;
                                        mediator.getCanvas2D().addToSelection(p);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> _p.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                    }
                                }
                                return;
                            }
                        }
                        if (at.createTransformedShape(j.getCircle()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            if (!j.getSelected()) {
                                mediator.getCanvas2D().addToSelection(j);
                                mediator.getExplorer().selectAllTreeViewItems(drawingElement -> j.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                            } else {
                                DrawingElement p = j.getParent();
                                while (p != null && p.getSelected()) {
                                    p = p.getParent();
                                }
                                if (p == null) {
                                    mediator.getCanvas2D().clearSelection();
                                    mediator.getCanvas2D().addToSelection(j);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> j.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                } else {
                                    DrawingElement _p = p;
                                    mediator.getCanvas2D().addToSelection(p);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> _p.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                }
                            }
                            return;
                        }
                    }

                    for (HelixDrawing h: mediator.getWorkingSession().getHelicesDrawn()) {
                        for (ResidueDrawing c : h.getResidues()) {
                            if ((c.isFullDetails() || c.residueLetter.isFullDetails()) && at.createTransformedShape(c.getCircle()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                                if (!c.getSelected()) {
                                    mediator.getCanvas2D().addToSelection(c);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> c.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                } else {
                                    DrawingElement p = c.getParent();
                                    while (p != null && p.getSelected()) {
                                        p = p.getParent();
                                    }
                                    if (p == null) {
                                        mediator.getCanvas2D().clearSelection();
                                        mediator.getCanvas2D().addToSelection(c);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> c.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                    } else {
                                        DrawingElement _p = p;
                                        mediator.getCanvas2D().addToSelection(p);
                                        mediator.getExplorer().selectAllTreeViewItems(drawingElement -> _p.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                    }
                                }
                                return;
                            }
                        }
                        if (at.createTransformedShape(h.getBounds2D()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                            if (!h.getSelected()) {
                                mediator.getCanvas2D().addToSelection(h);
                                mediator.getExplorer().selectAllTreeViewItems(drawingElement -> h.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                            } else {
                                DrawingElement p = h.getParent();
                                while (p != null && p.getSelected()) {
                                    p = p.getParent();
                                }
                                if (p == null) {
                                    mediator.getCanvas2D().clearSelection();
                                    mediator.getCanvas2D().addToSelection(h);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> h.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                } else {
                                    DrawingElement _p = p;
                                    mediator.getCanvas2D().addToSelection(p);
                                    mediator.getExplorer().selectAllTreeViewItems(drawingElement -> _p.equals(drawingElement), Arrays.asList(mediator.getExplorer().getTreeTableView().getRoot()), false);
                                }
                            }
                            return;
                        }
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
        scene.getStylesheets().add(getClass().getClassLoader().getResource("io/github/fjossinet/rnartist/gui/css/main.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("RNArtist");

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        int width = (int) (screenSize.getWidth() * 4.0 / 10.0);
        scene.getWindow().setWidth(screenSize.getWidth() - width);
        scene.getWindow().setHeight(screenSize.getHeight());
        scene.getWindow().setX(0);
        scene.getWindow().setY(0);

        mediator.getProjectManager().getStage().show();
        mediator.getProjectManager().getStage().toFront();
    }

    private Menu createSavedThemeItem(org.apache.commons.lang3.tuple.Pair<String, NitriteId> theme) {
        Menu savedThemeMenu = new Menu(theme.getKey());

        MenuItem loadSavedTheme = new MenuItem("Load");
        loadSavedTheme.setUserData(theme);
        loadSavedTheme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                setCurrentTheme((org.apache.commons.lang3.tuple.Pair<String, NitriteId>) loadSavedTheme.getUserData());
            }
        });

        MenuItem deleteSavedTheme = new MenuItem("Delete");
        deleteSavedTheme.setUserData(theme);
        deleteSavedTheme.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                org.apache.commons.lang3.tuple.Pair<String, NitriteId> theme = (org.apache.commons.lang3.tuple.Pair<String, NitriteId>) deleteSavedTheme.getUserData();
                mediator.getEmbeddedDB().deleteTheme(theme.getValue());
                userThemesMenu.getItems().remove(savedThemeMenu);
                if (currentThemeMenu.getUserData().equals(deleteSavedTheme.getUserData())) { //the theme removed was the current theme
                    updateSavedThemeItem.setDisable(true);
                    currentThemeMenu.setUserData(null);
                }
            }
        });

        MenuItem shareCurrentTheme = new MenuItem("Share...");
        shareCurrentTheme.setDisable(true);

        savedThemeMenu.getItems().addAll(loadSavedTheme, deleteSavedTheme, shareCurrentTheme);
        return savedThemeMenu;
    }

    public void setCurrentTheme(org.apache.commons.lang3.tuple.Pair<String, NitriteId> theme) {
        //mediator.getCurrent2DDrawing().setTheme(mediator.getEmbeddedDB().getTheme(theme.getValue()));
        updateSavedThemeItem.setUserData(theme); //to have the theme reference to update it for the user
        updateSavedThemeItem.setDisable(false);
        currentThemeMenu.setUserData(theme);
        currentThemeMenu.setDisable(false);
    }

    public Menu getCurrentThemeMenu() {
        return currentThemeMenu;
    }

    public MenuItem getUpdateSavedThemeItem() {
        return updateSavedThemeItem;
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

}
