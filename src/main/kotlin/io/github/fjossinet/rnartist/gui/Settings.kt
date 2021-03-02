package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.RnartistConfig
import io.github.fjossinet.rnartist.core.model.RnartistConfig.chimeraHost
import io.github.fjossinet.rnartist.core.model.RnartistConfig.chimeraPort
import io.github.fjossinet.rnartist.core.model.RnartistConfig.exportSVGWithBrowserCompatibility
import io.github.fjossinet.rnartist.core.model.RnartistConfig.isChimeraX
import io.github.fjossinet.rnartist.core.model.RnartistConfig.rnaGalleryPath
import io.github.fjossinet.rnartist.core.model.RnartistConfig.save
import io.github.fjossinet.rnartist.core.model.RnartistConfig.useOnlineRNAGallery
import io.github.fjossinet.rnartist.core.model.RnartistConfig.website
import io.github.fjossinet.rnartist.io.Backend.getAllThemes
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.io.ChimeraDriver
import io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.io.ChimeraXDriver
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.shape.Line
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.stage.*
import javafx.util.Callback
import org.controlsfx.control.GridCell
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.kordamp.ikonli.javafx.FontIcon
import java.io.File
import java.util.stream.Collectors
import javax.swing.SwingWorker

class Settings(mediator: Mediator) {

    var stage: Stage
    private var fontNames: ComboBox<String>
    private var deltaXRes: Spinner<Int>
    private  var deltaYRes:Spinner<Int>
    private  var deltaFontSize:Spinner<Int>

    init {
        stage = Stage()
        stage.title = "Settings"
        stage.onCloseRequest = EventHandler { windowEvent: WindowEvent? -> save() }
        val root = TabPane()
        root.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        val vbox = VBox()
        vbox.isFillWidth = true
        vbox.padding = Insets(10.0, 10.0, 10.0, 10.0)
        val settings = Tab("Global Settings", vbox)
        root.tabs.add(settings)

        //---- Chimera
        var title = Label("UCSF Chimera")
        title.style = "-fx-font-size: 20"
        vbox.children.add(VBox(title, Separator(Orientation.HORIZONTAL)))
        val chimeraPane = GridPane()
        for (i in 0..5) {
            val constraints = ColumnConstraints()
            if (i == 3) constraints.hgrow = Priority.ALWAYS
            chimeraPane.columnConstraints.add(constraints)
        }
        chimeraPane.padding = Insets(10.0, 5.0, 15.0, 5.0)
        chimeraPane.hgap = 5.0
        chimeraPane.vgap = 5.0
        vbox.children.add(chimeraPane)
        var hostLabel = Label("Host")
        chimeraPane.children.add(hostLabel)
        GridPane.setConstraints(hostLabel, 0, 0)
        var hostValue = TextField(chimeraHost)
        chimeraPane.children.add(hostValue)
        GridPane.setConstraints(hostValue, 1, 0)
        var portLabel = Label("Port")
        chimeraPane.children.add(portLabel)
        GridPane.setConstraints(portLabel, 2, 0)
        var portValue = TextField("" + chimeraPort)
        chimeraPane.children.add(portValue)
        GridPane.setConstraints(portValue, 3, 0)
        var isX = CheckBox("Chimera X")
        isX.isSelected = isChimeraX
        chimeraPane.children.add(isX)
        GridPane.setConstraints(isX, 4, 0)
        var connect2ChimeraRest = Button("Connect")
        connect2ChimeraRest.maxWidth = Double.MAX_VALUE
        chimeraPane.children.add(connect2ChimeraRest)
        GridPane.setConstraints(connect2ChimeraRest, 5, 0)
        connect2ChimeraRest.onMouseClicked = EventHandler {
            try {
                chimeraHost = hostValue.text.trim { it <= ' ' }
                chimeraPort = portValue.text.trim { it <= ' ' }.toInt()
                isChimeraX = isX.isSelected
                if (isChimeraX)
                    mediator.chimeraDriver = ChimeraXDriver(mediator)
                else
                    mediator.chimeraDriver = ChimeraDriver(mediator)
                mediator.chimeraDriver.connectToRestServer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //---- RNA GAllery
        title = Label("RNA Gallery")
        title.style = "-fx-font-size: 20"
        vbox.children.add(VBox(title, Separator(Orientation.HORIZONTAL)))
        val rnaGalleryPane = GridPane()
        var cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        rnaGalleryPane.columnConstraints.addAll(cc, ColumnConstraints())
        rnaGalleryPane.padding = Insets(10.0, 5.0, 15.0, 5.0)
        rnaGalleryPane.hgap = 5.0
        rnaGalleryPane.vgap = 5.0
        vbox.children.add(rnaGalleryPane)
        val galleryPath = TextField()
        galleryPath.isEditable = false
        galleryPath.text = rnaGalleryPath
        rnaGalleryPane.children.add(galleryPath)
        GridPane.setConstraints(galleryPath, 0, 1)
        val gallerySearch = Button("Browse")
        rnaGalleryPane.children.add(gallerySearch)
        GridPane.setConstraints(gallerySearch, 1, 1)
        gallerySearch.onMouseClicked = EventHandler {
            val directoryChooser = DirectoryChooser()
            val f = directoryChooser.showDialog(mediator.rnartist.stage)
            if (f != null) {
                if (f.isDirectory && File(f, "PDB").exists()) {
                    galleryPath.text = f.absolutePath
                    rnaGalleryPath = galleryPath.text
                } else {
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Problem with your RNA Gallery"
                    alert.headerText = "The directory selected doesn't look like an RNA Gallery"
                    val box = HBox()
                    box.alignment = Pos.CENTER
                    box.spacing = 10.0
                    box.children.add(Label("You need to download and select a copy of the RNA Gallery project."))
                    val button = Button(null, FontIcon("fas-download:15"))
                    button.onAction =
                        EventHandler { mediator!!.rnartist.hostServices.showDocument("https://github.com/fjossinet/RNAGallery") }
                    box.children.add(button)
                    alert.dialogPane.content = box
                    alert.showAndWait()
                }
            }
        }
        val useOnlineGallery = CheckBox("Use online gallery")
        useOnlineGallery.isSelected = useOnlineRNAGallery
        galleryPath.isDisable = useOnlineRNAGallery
        gallerySearch.isDisable = useOnlineRNAGallery
        useOnlineGallery.onAction = EventHandler {
            useOnlineRNAGallery = useOnlineGallery.isSelected
            galleryPath.isDisable = useOnlineRNAGallery
            gallerySearch.isDisable = useOnlineRNAGallery
        }
        rnaGalleryPane.children.add(useOnlineGallery)
        GridPane.setConstraints(useOnlineGallery, 0, 2, 2, 1)

        //++++++ pane for the fonts
        title = Label("Font")
        title.style = "-fx-font-size: 20"
        vbox.children.add(VBox(title, Separator(Orientation.HORIZONTAL)))
        val fontsPane = GridPane()
        fontsPane.padding = Insets(10.0, 5.0, 15.0, 5.0)
        fontsPane.maxWidth = Double.MAX_VALUE
        fontsPane.hgap = 5.0
        fontsPane.vgap = 5.0
        vbox.children.add(fontsPane)
        cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        fontsPane.columnConstraints.addAll(ColumnConstraints(),
            ColumnConstraints(),
            ColumnConstraints(),
            ColumnConstraints(),
            ColumnConstraints(),
            cc,
            ColumnConstraints())
        fontNames = ComboBox(
            FXCollections.observableList(Font.getFamilies().stream().distinct().collect(Collectors.toList())))
        /*val eventHandler: EventHandler<*> = label@ EventHandler { event: Event? ->
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    return@label null
                }
            }.execute()
        }
        fontNames.setOnAction(eventHandler)*/
        fontNames.maxWidth = Double.MAX_VALUE
        GridPane.setConstraints(fontNames, 0, 0, 6, 1)
        fontsPane.children.add(fontNames)
        val applyFontName = Button(null, Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE))
        GridPane.setConstraints(applyFontName, 6, 0, 1, 1)
        fontsPane.children.add(applyFontName)
        applyFontName.onMouseClicked = EventHandler { }
        deltaXRes = Spinner()
        deltaXRes!!.valueProperty().addListener { observable, oldValue, newValue -> }
        deltaYRes = Spinner<Int>()
        deltaYRes.valueProperty().addListener(ChangeListener<Int?> { observable, oldValue, newValue -> })
        deltaFontSize = Spinner<Int>()
        deltaFontSize.valueProperty().addListener(ChangeListener<Int?> { observable, oldValue, newValue -> })
        var l = Label("x")
        GridPane.setConstraints(l, 0, 1, 1, 1)
        GridPane.setHalignment(l, HPos.LEFT)
        fontsPane.children.add(l)
        GridPane.setConstraints(deltaXRes, 1, 1, 1, 1)
        GridPane.setHalignment(deltaXRes, HPos.LEFT)
        fontsPane.children.add(deltaXRes)
        l = Label("y")
        GridPane.setConstraints(l, 2, 1, 1, 1)
        GridPane.setHalignment(l, HPos.LEFT)
        fontsPane.children.add(l)
        GridPane.setConstraints(deltaYRes, 3, 1, 1, 1)
        GridPane.setHalignment(deltaYRes, HPos.LEFT)
        fontsPane.children.add(deltaYRes)
        l = Label("s")
        GridPane.setConstraints(l, 4, 1, 1, 1)
        fontsPane.children.add(l)
        GridPane.setHalignment(l, HPos.LEFT)
        GridPane.setConstraints(deltaFontSize, 5, 1, 1, 1)
        fontsPane.children.add(deltaFontSize)
        GridPane.setHalignment(deltaFontSize, HPos.LEFT)
        val applyDeltas = Button(null, Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE))
        applyDeltas.minWidth = Control.USE_PREF_SIZE
        GridPane.setConstraints(applyDeltas, 6, 1, 1, 1)
        fontsPane.children.add(applyDeltas)
        applyDeltas.onMouseClicked = EventHandler { }

        //---- Bunch of options
        title = Label("Misc Settings")
        title.style = "-fx-font-size: 20"
        vbox.children.add(VBox(title, Separator(Orientation.HORIZONTAL)))
        val optionsPane = GridPane()
        cc = ColumnConstraints()
        cc.hgrow = Priority.ALWAYS
        optionsPane.columnConstraints.addAll(ColumnConstraints(), cc)
        vbox.children.add(optionsPane)
        optionsPane.padding = Insets(10.0, 5.0, 15.0, 5.0)
        optionsPane.hgap = 5.0
        optionsPane.vgap = 5.0
        var row = 0
        val svgBrowserFix = CheckBox()
        svgBrowserFix.isSelected = exportSVGWithBrowserCompatibility()
        svgBrowserFix.onAction = EventHandler { actionEvent: ActionEvent? ->
            exportSVGWithBrowserCompatibility(svgBrowserFix.isSelected)
        }
        optionsPane.children.add(svgBrowserFix)
        GridPane.setConstraints(svgBrowserFix, 0, row)
        l = Label("Set Browser Compatibility for SVG Export")
        optionsPane.children.add(l)
        GridPane.setConstraints(l, 1, row++)
        val scene = Scene(root)
        scene.stylesheets.add("io/github/fjossinet/rnartist/gui/css/main.css")
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        scene.window.width = screenSize.width / 3
        scene.window.height = screenSize.height / 2
        scene.window.x = screenSize.width / 2 - screenSize.width / 4
        scene.window.y = screenSize.height / 2 - screenSize.height / 4
    }

}