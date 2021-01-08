package io.github.fjossinet.rnartist.gui;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteId;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.parsePDB;
import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.writePDB;
import static io.github.fjossinet.rnartist.core.model.io.UtilsKt.createTemporaryFile;
import static org.dizitart.no2.filters.Filters.*;

public class EmbeddedDBGUI {

    private Mediator mediator;
    private ObservableList<Structure> structures;
    private ListView<Structure> listView;
    private Stage stage;

    public EmbeddedDBGUI(Mediator mediator) {
        this.mediator = mediator;
        this.structures = FXCollections.observableArrayList();
        this.stage = new Stage();
        stage.setTitle("Database");
        this.createScene(stage);
    }

    public Stage getStage() {
        return stage;
    }

    private void createScene(Stage stage) {
        VBox vbox =  new VBox();
        vbox.setFillWidth(true);

        listView = new ListView<>(structures);
        listView.setCellFactory(new Callback<ListView<Structure>, ListCell<Structure>>() {
            @Override
            public ListCell<Structure> call(ListView<Structure> lv) {
                return new StructureCell();
            }
        });
        listView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> change) {
                //mediator.getAllStructures().add((SecondaryStructure)mediator.getEmbeddedDB().getPDBSecondaryStructures().getById(listView.getSelectionModel().getSelectedItem().id).get("ss"));
            }
        });

        GridPane searchForm = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        searchForm.getColumnConstraints().addAll(new ColumnConstraints(),cc);
        searchForm.setHgap(5);
        searchForm.setVgap(5);
        searchForm.setPadding(new Insets(10, 10, 10, 10));

        Label title = new Label("Title");
        searchForm.add(title, 0, 0);

        TextField titleField = new TextField();
        searchForm.add(titleField, 1, 0);
        titleField.setPromptText("RNA*");

        Label authors = new Label("Author");
        searchForm.add(authors, 0, 1);

        TextField authorsField = new TextField();
        searchForm.add(authorsField, 1, 1);
        authorsField.setPromptText("Doudna");

        Label year = new Label("Year");
        searchForm.add(year, 0, 2);

        TextField yearField = new TextField();
        searchForm.add(yearField, 1, 2);
        yearField.setPromptText("2000");

        Label pdbID = new Label("PDB ID");
        searchForm.add(pdbID, 0, 3);

        TextField pdbIDField = new TextField();
        searchForm.add(pdbIDField, 1, 3);
        pdbIDField.setPromptText("1EHZ");

        Button search = new Button("Search");
        searchForm.add(search, 0, 4,2,1);
        GridPane.setHalignment(search,HPos.CENTER);

        search.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent e)  {
                structures.clear();
                    ArrayList<Filter> filters = new ArrayList<Filter>();
                    if (pdbIDField.getText().trim().length() != 0)
                        filters.add(eq("pdbId", pdbIDField.getText().trim()));
                    if (authorsField.getText().trim().length() != 0)
                        filters.add(text("authors", authorsField.getText().trim()) );
                    if (titleField.getText().trim().length() != 0)
                        filters.add(text("title", titleField.getText().trim()) );
                    if (yearField.getText().trim().length() != 0)
                        filters.add(eq("pubDate", yearField.getText().trim()) );
                    for (Document doc: mediator.getEmbeddedDB().getPDBSecondaryStructures().find(and(filters.toArray(new Filter[0])))) {
                        addStructure((String)doc.get("pdbId"), doc.getId(), (String)doc.get("title"), (String)doc.get("chain"), (String)doc.get("authors"), (String)doc.get("pubDate"));
                    }
            }
        });
        vbox.getChildren().add(searchForm);
        vbox.getChildren().add(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);

        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab _3D = new Tab("3D", vbox);
        root.getTabs().add(_3D);

        Tab _2D = new Tab("2D", new VBox());
        root.getTabs().add(_2D);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        this.stage.setWidth(440);
        this.stage.setHeight(screenSize.getHeight());
        this.stage.setX(screenSize.getWidth()-440);
        this.stage.setY(0);
    }

    public class StructureCell extends ListCell<Structure> {

        private final GridPane gridPane = new GridPane();
        private final Label title = new Label();
        private final Label descriptionLabel = new Label();
        private final Label authors = new Label();
        private final ImageView structureIcon = new ImageView();
        private final AnchorPane content = new AnchorPane();

        public StructureCell() {
            structureIcon.setFitWidth(75);
            structureIcon.setPreserveRatio(true);
            GridPane.setConstraints(structureIcon, 0, 0, 1, 3);
            GridPane.setValignment(structureIcon, VPos.TOP);
            //
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");
            GridPane.setConstraints(title, 1, 0);
            //
            GridPane.setConstraints(authors, 1, 1);
            GridPane.setColumnSpan(authors, Integer.MAX_VALUE);
            //
            GridPane.setConstraints(descriptionLabel, 1, 2);
            GridPane.setColumnSpan(descriptionLabel, Integer.MAX_VALUE);
            //
            gridPane.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true));
            gridPane.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true));
            gridPane.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, VPos.CENTER, true));
            gridPane.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.NEVER, VPos.CENTER, true));
            gridPane.getRowConstraints().add(new RowConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.CENTER, true));
            gridPane.setHgap(6);
            gridPane.setVgap(6);
            gridPane.getChildren().setAll(structureIcon, title, descriptionLabel, authors);
            AnchorPane.setTopAnchor(gridPane, 0d);
            AnchorPane.setLeftAnchor(gridPane, 0d);
            AnchorPane.setBottomAnchor(gridPane, 0d);
            AnchorPane.setRightAnchor(gridPane, 0d);
            content.getChildren().add(gridPane);
            this.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    SecondaryStructure ss = (SecondaryStructure)mediator.getEmbeddedDB().getPDBSecondaryStructures().getById(StructureCell.this.getItem().id).get("ss");
                    mediator.get_2DDrawingsLoaded().add(new SecondaryStructureDrawing(ss, new WorkingSession()));
                    String[] tokens = ss.getSource().split("db:pdb:");
                    Document doc = mediator.getEmbeddedDB().getPDBTertiaryStructure(tokens[tokens.length-1],ss.getRna().getName());
                    try {
                        if (doc != null)
                            mediator.setTertiaryStructure((TertiaryStructure) doc.get("ts"));
                        else {
                            URL url = new URL("http://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId=" + tokens[tokens.length-1]);
                            StringBuffer content = new StringBuffer();
                            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                            String str = null;
                            while ((str = in.readLine()) != null) {
                                content.append(str + "\n");
                            }
                            in.close();
                            //mediator.canvas2D.getMessagingSystem().addSingleStep("PDB entry downloaded.", null, null);
                            mediator.getCanvas2D().repaint();
                            for (TertiaryStructure ts: parsePDB(new StringReader(content.toString()))) {
                                if (ts.getRna().getName().equals(ss.getRna().getName())) {
                                    mediator.setTertiaryStructure(ts);
                                    break;
                                }
                            }
                        }
                        if (mediator.getChimeraDriver() != null) {
                            File tmpF = createTemporaryFile("ts.pdb");
                            writePDB(mediator.getTertiaryStructure(), true, new FileWriter(tmpF));
                            mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        protected void updateItem(Structure item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(null);
            setText(null);
            setContentDisplay(ContentDisplay.LEFT);
            if (!empty && item != null) {
                title.setText(item.title);
                structureIcon.setImage(item.getImage());
                descriptionLabel.setText(item.pdbId+", chain "+item.chain);
                authors.setText(item.authors+" et al, "+item.pubDate);
                setGraphic(content);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }
    }

    class Structure {

        private NitriteId id;
        private String pdbId;
        private String title;
        private String chain;
        private String authors;
        private String pubDate;

        public Structure(String pdbId, NitriteId id, String title, String chain, String authors,String pubDate) {
            this.pdbId = pdbId;
            this.id = id;
            this.title = title;
            this.chain = chain;
            this.authors = authors;
            this.pubDate = pubDate;
        }

        public Image getImage() {
            try {
                return new Image(new File(new File(new File(mediator.getEmbeddedDB().getRootDir(),"images"),"pdb"),pdbId+".jpg").toURI().toURL().toString(),150, 150, false, false);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void addStructure(String pdbId, NitriteId id, String title, String chain, String authors, String pubDate) {
         this.structures.add(new Structure(pdbId, id, title, chain, authors, pubDate));
    }

}
