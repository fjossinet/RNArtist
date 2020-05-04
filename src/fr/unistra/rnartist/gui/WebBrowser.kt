package fr.unistra.rnartist.gui

import fr.unistra.rnartist.model.RnartistConfig
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Screen
import javafx.stage.Stage
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.w3c.dom.Node
import javax.swing.SwingWorker

class WebBrowser(val mediator: Mediator?) {

    var stage: Stage
    val root = TabPane()
    lateinit var rnartistEngine: WebEngine

    init{
        this.stage = Stage()
        createScene()
    }

    private fun createScene() {
        root.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        val rnartistThemesWebView = WebView();
        rnartistEngine = rnartistThemesWebView.getEngine()
        rnartistEngine.load(RnartistConfig.website)
        val rnartistThemes = Tab("RNArtist", rnartistThemesWebView)
        root.tabs.add(rnartistThemes)

        val ndb = Tab("NDB", ndbBrowser())
        root.tabs.add(ndb)

        val rnaCentral = Tab("RNA Central", rnacentralBrowser())
        root.tabs.add(rnaCentral)

        val mfold = Tab("MFold", mfoldBrowser())
        root.tabs.add(mfold)

        val scene = Scene(root)
        stage.scene = scene
        val screenSize = Screen.getPrimary().bounds
        this.stage.width = screenSize.width
        this.stage.height = screenSize.height/2
        this.stage.y = screenSize.height/2
    }

    private fun ndbBrowser(): VBox {
        val vbox = VBox();
        val browser = WebView()
        val webEngine = browser.engine
        webEngine.load("http://ndbserver.rutgers.edu/service/ndb/atlas/gallery/rna?polType=all&rnaFunc=all&protFunc=all&strGalType=rna&expMeth=all&seqType=all&galType=table&start=0&limit=50")
        webEngine.loadWorker.stateProperty().addListener(
                object: ChangeListener<Worker.State> {
                    override fun changed(p0: ObservableValue<out Worker.State>?, oldState: Worker.State?, newState: Worker.State?) {
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

                }
        )

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