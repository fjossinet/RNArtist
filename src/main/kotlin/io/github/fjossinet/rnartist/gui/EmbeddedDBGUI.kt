package io.github.fjossinet.rnartist.gui
import io.github.fjossinet.rnartist.Mediator
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Screen
import javafx.stage.Stage
import org.dizitart.no2.Filter
import org.dizitart.no2.NitriteId
import org.dizitart.no2.filters.Filters
import java.io.File
import java.net.MalformedURLException

class EmbeddedDBGUI(val mediator:Mediator) {

    private var structures: ObservableList<Structure>
    private var listView: ListView<Structure>
    private var stage: Stage

    init {
        structures = FXCollections.observableArrayList()
        stage = Stage()
        stage.title = "Database"
        val vbox = VBox()
        vbox.isFillWidth = true
        listView = ListView(structures)
        listView.setCellFactory { StructureCell() }
        listView.selectionModel.selectedIndices.addListener(ListChangeListener {
            //mediator.getAllStructures().add((SecondaryStructure)mediator.getEmbeddedDB().getPDBSecondaryStructures().getById(listView.getSelectionModel().getSelectedItem().id).get("ss"));
        })
        val searchForm = GridPane()
        val cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        searchForm.columnConstraints.addAll(ColumnConstraints(), cc)
        searchForm.hgap = 5.0
        searchForm.vgap = 5.0
        searchForm.padding = Insets(10.0, 10.0, 10.0, 10.0)
        val title = Label("Title")
        searchForm.add(title, 0, 0)
        val titleField = TextField()
        searchForm.add(titleField, 1, 0)
        titleField.promptText = "RNA*"
        val authors = Label("Author")
        searchForm.add(authors, 0, 1)
        val authorsField = TextField()
        searchForm.add(authorsField, 1, 1)
        authorsField.promptText = "Doudna"
        val year = Label("Year")
        searchForm.add(year, 0, 2)
        val yearField = TextField()
        searchForm.add(yearField, 1, 2)
        yearField.promptText = "2000"
        val pdbID = Label("PDB ID")
        searchForm.add(pdbID, 0, 3)
        val pdbIDField = TextField()
        searchForm.add(pdbIDField, 1, 3)
        pdbIDField.promptText = "1EHZ"
        val search = Button("Search")
        searchForm.add(search, 0, 4, 2, 1)
        GridPane.setHalignment(search, HPos.CENTER)
        search.onMouseClicked = EventHandler {
            structures.clear()
            val filters = ArrayList<Filter>()
            if (pdbIDField.text.trim { it <= ' ' }.length != 0) filters.add(Filters.eq("pdbId",
                pdbIDField.text.trim { it <= ' ' }))
            if (authorsField.text.trim { it <= ' ' }.length != 0) filters.add(Filters.text("authors",
                authorsField.text.trim { it <= ' ' }))
            if (titleField.text.trim { it <= ' ' }.length != 0) filters.add(Filters.text("title",
                titleField.text.trim { it <= ' ' }))
            if (yearField.text.trim { it <= ' ' }.length != 0) filters.add(Filters.eq("pubDate",
                yearField.text.trim { it <= ' ' }))
            /*for (Document doc: mediator.getEmbeddedDB().getPDBSecondaryStructures().find(and(filters.toArray(new Filter[0])))) {
                                addStructure((String)doc.get("pdbId"), doc.getId(), (String)doc.get("title"), (String)doc.get("chain"), (String)doc.get("authors"), (String)doc.get("pubDate"));
                            }*/
        }
        vbox.children.add(searchForm)
        vbox.children.add(listView)
        VBox.setVgrow(listView, Priority.ALWAYS)
        val root = TabPane()
        root.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        val _3D = Tab("3D", vbox)
        root.tabs.add(_3D)
        val _2D = Tab("2D", VBox())
        root.tabs.add(_2D)
        val scene = Scene(root)
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        this.stage.width = 440.0
        this.stage.height = screenSize.height
        this.stage.x = screenSize.width - 440
        this.stage.y = 0.0
    }

    inner private class StructureCell : ListCell<Structure?>() {
        private val gridPane = GridPane()
        private val title = Label()
        private val descriptionLabel = Label()
        private val authors = Label()
        private val structureIcon = ImageView()
        private val content = AnchorPane()
        protected override fun updateItem(item: Structure?, empty: Boolean) {
            super.updateItem(item, empty)
            graphic = null
            text = null
            contentDisplay = ContentDisplay.LEFT
            if (!empty) {
                item?.let { item ->
                    title.text = item.title
                    structureIcon.image = item.image
                    descriptionLabel.text = item.pdbId + ", chain " + item.chain
                    authors.text = item.authors + " et al, " + item.pubDate
                    graphic = content
                    contentDisplay = ContentDisplay.GRAPHIC_ONLY
                }
            }
        }

        init {
            structureIcon.fitWidth = 75.0
            structureIcon.isPreserveRatio = true
            GridPane.setConstraints(structureIcon, 0, 0, 1, 3)
            GridPane.setValignment(structureIcon, VPos.TOP)
            //
            title.style = "-fx-font-weight: bold; -fx-font-size: 1.2em;"
            GridPane.setConstraints(title, 1, 0)
            //
            GridPane.setConstraints(authors, 1, 1)
            GridPane.setColumnSpan(authors, Int.MAX_VALUE)
            //
            GridPane.setConstraints(descriptionLabel, 1, 2)
            GridPane.setColumnSpan(descriptionLabel, Int.MAX_VALUE)
            //
            gridPane.columnConstraints.add(ColumnConstraints(USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                Priority.NEVER,
                HPos.LEFT,
                true))
            gridPane.columnConstraints.add(ColumnConstraints(USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                Priority.ALWAYS,
                HPos.LEFT,
                true))
            gridPane.columnConstraints.add(ColumnConstraints(USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                Priority.NEVER,
                HPos.LEFT,
                true))
            gridPane.columnConstraints.add(ColumnConstraints(USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                Priority.NEVER,
                HPos.LEFT,
                true))
            gridPane.rowConstraints.add(RowConstraints(USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                Priority.NEVER,
                VPos.CENTER,
                true))
            gridPane.rowConstraints.add(RowConstraints(USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                Priority.NEVER,
                VPos.CENTER,
                true))
            gridPane.rowConstraints.add(RowConstraints(USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                USE_COMPUTED_SIZE,
                Priority.ALWAYS,
                VPos.CENTER,
                true))
            gridPane.hgap = 6.0
            gridPane.vgap = 6.0
            gridPane.children.setAll(structureIcon, title, descriptionLabel, authors)
            AnchorPane.setTopAnchor(gridPane, 0.0)
            AnchorPane.setLeftAnchor(gridPane, 0.0)
            AnchorPane.setBottomAnchor(gridPane, 0.0)
            AnchorPane.setRightAnchor(gridPane, 0.0)
            content.children.add(gridPane)
            this.onMouseClicked =
                EventHandler { //SecondaryStructure ss = (SecondaryStructure)mediator.getEmbeddedDB().getPDBSecondaryStructures().getById(StructureCell.this.getItem().id).get("ss");
                    //mediator.get_2DDrawingsLoaded().add(new SecondaryStructureDrawing(ss, new WorkingSession()));
                    //String[] tokens = ss.getSource().split("db:pdb:");
                    //Document doc = mediator.getEmbeddedDB().getPDBTertiaryStructure(tokens[tokens.length-1],ss.getRna().getName());
                    try {
                        /*if (doc != null)
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
                                    mediator.setTertiaryStructure(ts);
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
                                }*/
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    private inner class Structure(
        val pdbId: String,
        val id: NitriteId,
        val title: String,
        val chain: String,
        val authors: String,
        val pubDate: String,
    ) {
        val image: Image?
            get() {
                try {
                    return Image(File(File(File(mediator.embeddedDB.rootDir, "images"), "pdb"),
                        "$pdbId.jpg").toURI().toURL().toString(), 150.0, 150.0, false, false)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
                return null
            }

    }

    fun addStructure(pdbId: String, id: NitriteId, title: String, chain: String, authors: String, pubDate: String) {
        structures!!.add(Structure(pdbId, id, title, chain, authors, pubDate))
    }
}