package io.github.fjossinet.rnartist.gui

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.PDB
import io.github.fjossinet.rnartist.core.model.RNAGallery
import io.github.fjossinet.rnartist.core.model.RnartistConfig
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.core.model.io.parseJSON
import io.github.fjossinet.rnartist.io.ChimeraDriver
import javafx.concurrent.Task
import javafx.concurrent.Worker
import javafx.concurrent.WorkerStateEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Screen
import javafx.stage.Stage
import org.apache.commons.lang3.tuple.Pair
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.w3c.dom.Node
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.net.MalformedURLException
import javax.swing.SwingWorker

class WebBrowser(val mediator: Mediator) {

    var stage: Stage
    private val root = TabPane()
    private lateinit var rnartistEngine: WebEngine

    init{
        this.stage = Stage()
        createScene()
    }

    private fun createScene() {
        root.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        val pdb = Tab("Protein Data Bank", pdbBrowser())
        root.tabs.add(pdb)

        val rnaGallery = Tab("RNA Gallery", rnaGallery())
        root.tabs.add(rnaGallery)

        val rnaCentral = Tab("RNA Central", rnacentralBrowser())
        //root.tabs.add(rnaCentral)

        /*val mfold = Tab("MFold", mfoldBrowser())
        root.tabs.add(mfold)*/

        val scene = Scene(root)
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        this.stage.width = screenSize.width
        this.stage.height = screenSize.height/2
        this.stage.y = screenSize.height/2
    }

    private fun rnaGallery(): VBox {
        val vbox = VBox();
        vbox.spacing = 10.0
        vbox.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val browser = WebView()
        val webEngine = browser.engine

        val buttons = HBox()
        buttons.alignment = Pos.CENTER
        buttons.spacing = 10.0
        val home = Button("Home", Glyph("FontAwesome", FontAwesome.Glyph.HOME))
        home.onAction = EventHandler {
            webEngine.load(if (RnartistConfig.useOnlineRNAGallery) "https://github.com/fjossinet/RNAGallery/main/PDB/status.md" else File("${RnartistConfig.rnaGalleryPath}/PDB/status.md").toURI().toURL().toExternalForm())
        }
        val pdbID = TextField()
        pdbID.isEditable = false
        pdbID.isDisable = true
        val chainIds = ChoiceBox<String>()
        val loadInRNArtist = Button("Load 2D Drawing", Glyph("FontAwesome", FontAwesome.Glyph.SIGN_IN))
        loadInRNArtist.isDisable = true
        loadInRNArtist.onAction = EventHandler {
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    val loadData: Task<Pair<Pair<String, SecondaryStructureDrawing>?, java.lang.Exception?>> =
                        object : Task<Pair<Pair<String, SecondaryStructureDrawing>?, java.lang.Exception?>>() {
                            override fun call(): Pair<Pair<String, SecondaryStructureDrawing>?, java.lang.Exception?> {
                                try {
                                    val drawing = parseJSON(RNAGallery().getEntry(pdbID.text, chainIds.value))
                                    drawing!!.secondaryStructure.rna.name = chainIds.value
                                    return Pair.of(Pair.of(pdbID.text, drawing), null)
                                } catch (e:Exception) {
                                    return Pair.of(null, e)
                                }
                            }
                        }
                    loadData.onSucceeded = EventHandler {
                        try {
                            if (loadData.get().left != null) {
                                val tmpFile = File.createTempFile(loadData.get().left!!.left, ".pdb")
                                val writer: PrintWriter
                                try {
                                    val reader = PDB().getEntry(loadData.get().left!!.left)
                                    val buffer = StringBuilder()
                                    var intValueOfChar: Int
                                    while (reader.read().also { intValueOfChar = it } != -1) {
                                        buffer.append(intValueOfChar.toChar())
                                    }
                                    reader.close()
                                    writer = PrintWriter(tmpFile)
                                    writer.println(buffer.toString())
                                    writer.close()
                                } catch (e: FileNotFoundException) {
                                    e.printStackTrace()
                                }
                                mediator._2DDrawingsLoaded.add(loadData.get().left!!.right)
                                mediator.canvas2D.load2D(mediator._2DDrawingsLoaded[mediator._2DDrawingsLoaded.size - 1])

                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    Thread(loadData).start()
                    return null
                }
            }.execute()
        }
        buttons.children.addAll(home, Label("PDB ID"), pdbID, Label("Chain ID"), chainIds, loadInRNArtist)

        webEngine.load(if (RnartistConfig.useOnlineRNAGallery) "https://github.com/fjossinet/RNAGallery/main/PDB/status.md" else File("${RnartistConfig.rnaGalleryPath}/PDB/status.md").toURI().toURL().toExternalForm())
        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (webEngine.location.matches(Regex("https://www\\.rcsb\\.org/structure/...."))) {
                pdbID.text = webEngine.location.split("/").last()
                mediator.rnaGallery?.let {
                    it[pdbID.text]?.let {
                        chainIds.items.clear()
                        chainIds.items.addAll(it)
                        loadInRNArtist.isDisable = false
                    } ?: {
                        chainIds.items.clear()
                        loadInRNArtist.isDisable = true
                    }
                }
            }
        }

        vbox.children.addAll(buttons, browser)
        VBox.setVgrow(browser, Priority.ALWAYS)
        return vbox
    }

    private fun pdbBrowser(): VBox {
        val vbox = VBox();
        vbox.spacing = 10.0
        vbox.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val browser = WebView()
        val webEngine = browser.engine

        val buttons = HBox()
        buttons.alignment = Pos.CENTER
        buttons.spacing = 10.0
        val home = Button("Home", Glyph("FontAwesome", FontAwesome.Glyph.HOME))
        home.onAction = EventHandler { webEngine.load("https://www.rcsb.org") }
        val pdbID = TextField()
        pdbID.isEditable = false
        pdbID.isDisable = true
        val chainIds = ChoiceBox<String>()
        val loadInRNArtist = Button("Load 2D Drawing", Glyph("FontAwesome", FontAwesome.Glyph.SIGN_IN))
        loadInRNArtist.isDisable = true
        loadInRNArtist.onAction = EventHandler {
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    val loadData: Task<Pair<Pair<String, SecondaryStructureDrawing>?, java.lang.Exception?>> =
                        object : Task<Pair<Pair<String, SecondaryStructureDrawing>?, java.lang.Exception?>>() {
                            override fun call(): Pair<Pair<String, SecondaryStructureDrawing>?, java.lang.Exception?> {
                                try {
                                    val drawing = parseJSON(RNAGallery().getEntry(pdbID.text, chainIds.value))
                                    drawing!!.secondaryStructure.rna.name = chainIds.value
                                    return Pair.of(Pair.of(pdbID.text, drawing), null)
                                } catch (e:Exception) {
                                    return Pair.of(null, e)
                                }
                            }
                        }
                    loadData.onSucceeded = EventHandler {
                        try {
                            if (loadData.get().left != null) {
                                val tmpFile = File.createTempFile(loadData.get().left!!.left, ".pdb")
                                val writer: PrintWriter
                                try {
                                    val reader = PDB().getEntry(loadData.get().left!!.left)
                                    val buffer = StringBuilder()
                                    var intValueOfChar: Int
                                    while (reader.read().also { intValueOfChar = it } != -1) {
                                        buffer.append(intValueOfChar.toChar())
                                    }
                                    reader.close()
                                    writer = PrintWriter(tmpFile)
                                    writer.println(buffer.toString())
                                    writer.close()
                                } catch (e: FileNotFoundException) {
                                    e.printStackTrace()
                                }
                                mediator._2DDrawingsLoaded.add(loadData.get().left!!.right)
                                mediator.canvas2D.load2D(mediator._2DDrawingsLoaded[mediator._2DDrawingsLoaded.size - 1])
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    Thread(loadData).start()
                    return null
                }
            }.execute()
        }
        buttons.children.addAll(home, Label("PDB ID"), pdbID, Label("Chain ID"), chainIds, loadInRNArtist)

        webEngine.load("https://www.rcsb.org")
        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                if (webEngine.location.matches(Regex("https://www\\.rcsb\\.org/structure/...."))) {
                    pdbID.text = webEngine.location.split("/").last()
                    mediator.rnaGallery?.let {
                        it[pdbID.text]?.let {
                            chainIds.items.clear()
                            chainIds.items.addAll(it)
                            loadInRNArtist.isDisable = false
                        } ?: {
                            chainIds.items.clear()
                            loadInRNArtist.isDisable = true
                        }
                    }
                }
            }
        }


        vbox.children.addAll(buttons,browser)
        VBox.setVgrow(browser, Priority.ALWAYS)
        return vbox
    }

    private fun ndbBrowser(): VBox {
        val vbox = VBox();
        val browser = WebView()
        val webEngine = browser.engine
        webEngine.load("http://ndbserver.rutgers.edu/service/ndb/atlas/gallery/rna?polType=all&rnaFunc=all&protFunc=all&strGalType=rna&expMeth=all&seqType=all&galType=table&start=0&limit=50")
        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                val doc = webEngine.document
                val h2s = doc.getElementsByTagName("h2")
                for (i in 0 until h2s.length) {
                    val h2 = h2s.item(i) as Node
                    val content = h2.textContent
                    if (content.matches(Regex("NDB ID:.+PDB ID:.+"))) {
                        //currentPDBID = content.split("PDB ID:").toTypedArray()[1].trim { it <= ' ' }.substring(0, 4)
                        //loadInAssemble.setText("Load $currentPDBID")
                        //loadInAssemble.setDisable(false)
                    }
                }
            }
        }

        val buttons = HBox()
        val home = Button("Home", Glyph("FontAwesome", FontAwesome.Glyph.HOME))
        home.onAction = EventHandler { webEngine.load("http://ndbserver.rutgers.edu/service/ndb/atlas/gallery/rna?polType=all&rnaFunc=all&protFunc=all&strGalType=rna&expMeth=all&seqType=all&galType=table&start=0&limit=50") }
        val loadInRNArtist = Button("Load", Glyph("FontAwesome", FontAwesome.Glyph.SIGN_IN))
        loadInRNArtist.isDisable = true
        loadInRNArtist.onAction = EventHandler {
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    //loadPDBID(currentPDBID)
                    return null
                }
            }.execute()
        }
        buttons.children.addAll(home,loadInRNArtist)
        vbox.children.addAll(buttons,browser)
        VBox.setVgrow(browser, Priority.ALWAYS)
        return vbox
    }

    private fun rnacentralBrowser(): VBox {
        val vbox = VBox();
        val browser = WebView()
        val webEngine = browser.engine
        webEngine.load("http://rnacentral.org")
        webEngine.loadWorker.stateProperty().addListener { _, _, newState ->
            if (newState == Worker.State.SUCCEEDED) {
                val doc = webEngine.document
                val allPs = doc.getElementsByTagName("p")
                for (i in 0 until allPs.length) {
                    val p = allPs.item(i) as Node
                    val content = p.textContent
                    if (content.matches(Regex("Dot-bracket notation"))) {
                        println(p.nextSibling.firstChild.textContent)
                    }
                }
            }
        }
        val buttons = HBox()
        val home = Button("Home", Glyph("FontAwesome", FontAwesome.Glyph.HOME))
        home.onAction = EventHandler { webEngine.load("http://rnacentral.org") }
        val loadInRNArtist = Button("Load", Glyph("FontAwesome", FontAwesome.Glyph.SIGN_IN))
        loadInRNArtist.isDisable = true
        loadInRNArtist.onAction = EventHandler {
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    return null
                }
            }.execute()
        }
        buttons.children.addAll(home,loadInRNArtist)
        vbox.children.addAll(buttons,browser)
        VBox.setVgrow(browser, Priority.ALWAYS)
        return vbox
    }

    private fun mfoldBrowser(): VBox {
        val vbox = VBox();
        val browser = WebView()
        val webEngine = browser.engine
        webEngine.load("http://mfold.rna.albany.edu/?q=mfold/RNA-Folding-Form")
        val buttons = HBox()
        val home = Button("Home", Glyph("FontAwesome", FontAwesome.Glyph.HOME))
        home.onAction = EventHandler { webEngine.load("http://mfold.rna.albany.edu/?q=mfold/RNA-Folding-Form") }
        val loadInRNArtist = Button("Load", Glyph("FontAwesome", FontAwesome.Glyph.SIGN_IN))
        loadInRNArtist.isDisable = true
        loadInRNArtist.onAction = EventHandler {
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    return null
                }
            }.execute()
        }
        buttons.children.addAll(home,loadInRNArtist)
        vbox.children.addAll(buttons,browser)
        VBox.setVgrow(browser, Priority.ALWAYS)
        return vbox
    }

    fun showTab(index:Int) {
        this.root.selectionModel.select(index)
    }
}