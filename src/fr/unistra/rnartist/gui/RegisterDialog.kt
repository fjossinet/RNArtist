package fr.unistra.rnartist.gui

import fr.unistra.rnartist.RNArtist
import fr.unistra.rnartist.io.Backend
import fr.unistra.rnartist.model.RnartistConfig
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.concurrent.Task
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.StageStyle
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.Locale


class RegisterDialog(application: Application): Dialog<ButtonType>() {

    init {
        //this.headerText = "Please Register to Generate your User ID."
        this.headerText = null
        this.initStyle(StageStyle.UNDECORATED)
        val cancelButtonType = ButtonType("Cancel", ButtonData.CANCEL_CLOSE)
        val registerButtonType = ButtonType("Register", ButtonData.APPLY)
        this.getDialogPane().getButtonTypes().addAll(cancelButtonType, registerButtonType)

        val root = StackPane()
        val form = GridPane()
        val constraints = ColumnConstraints()
        constraints.hgrow = Priority.NEVER
        form.getColumnConstraints().addAll(ColumnConstraints(), constraints)

        form.hgap = 10.0
        form.vgap = 10.0
        form.padding = Insets(10.0, 10.0, 10.0, 10.0)

        val header = Text("Please Register to Generate your User ID.")
        header.font = Font.font ("Verdana", 20.0);
        GridPane.setHalignment(header, HPos.CENTER);
        val subheader = Text("It will be used to publish your 2D themes/layouts on the")
        subheader.font = Font.font ("Verdana", 15.0);
        val link = Hyperlink("RNArtist Website")
        link.font = Font.font ("Verdana", 15.0)
        link.setOnAction { actionEvent ->
            application.hostServices.showDocument(RnartistConfig.website)
        }
        val im = ImageView()
        im.image = Image("fr/unistra/rnartist/io/images/logo.png")
        form.add(im, 0, 0, 2, 1)
        form.add(header, 0, 1, 2, 1)
        val flow = FlowPane()
        flow.children.addAll(
                subheader,link
        )
        form.add(flow, 0, 2, 2, 1)

        val name = TextField()
        name.setPromptText("your name")

        val labo = TextField()
        labo.setPromptText("your laboratory (if any)")

        val countries = FXCollections.observableArrayList<String>()
        val country = ComboBox(countries)

        val locales1 = Locale.getISOCountries()
        for (countrylist in locales1) {
            val obj = Locale("", countrylist)
            val city = arrayOf(obj.displayCountry)
            for (x in city.indices) {
                countries.add(obj.displayCountry)
            }
        }
        country.setItems(countries)
        country.setMaxWidth(Double.MAX_VALUE);

        val labelName = Label("Name")
        labelName.setTextFill(Color.web("black"));
        form.add(labelName, 0, 3)
        form.add(name, 1, 3)
        val labelLabo = Label("Laboratory")
        labelLabo.setTextFill(Color.web("black"));
        form.add(labelLabo, 0, 4)
        form.add(labo, 1, 4)
        val labelCountry = Label("Country")
        labelCountry.setTextFill(Color.web("black"));
        form.add(labelCountry, 0, 5)
        form.add(country, 1, 5)

        root.children.add(form)

        this.getDialogPane().setContent(root)
        val pi = ProgressIndicator()
        val box = VBox(pi)
        box.alignment = Pos.CENTER

        while (RnartistConfig.userID == null) {
            val result = this.showAndWait()
            if (RnartistConfig.userID != null) {

            }
            else if (result.isPresent) {
                if (result.get().buttonData == ButtonData.CANCEL_CLOSE) {
                    if (RnartistConfig.userID == null) {
                        val alert = Alert(Alert.AlertType.INFORMATION)
                        alert.title = "For your Information"
                        alert.headerText = "You will use RNArtist without any User ID."
                        alert.contentText = "If you want to share online your themes or layouts, you will be able to register and get a User ID later."
                        alert.showAndWait()
                    }
                    break
                } else if (result.get().buttonData == ButtonData.APPLY && (name.text.trim().length == 0 || country.value == null)) {
                    if (name.text.trim().length == 0)
                        labelName.setTextFill(Color.web("red"));
                    else
                        labelName.setTextFill(Color.web("black"));
                    if (country.value == null)
                        labelCountry.setTextFill(Color.web("red"));
                    else
                        labelCountry.setTextFill(Color.web("black"));
                }
                else if (result.get().buttonData == ButtonData.APPLY && name.text.trim().length != 0 && country.value != null) {
                    labelName.setTextFill(Color.web("black"));
                    labelCountry.setTextFill(Color.web("black"));
                    form.isDisable = true
                    root.children.add(box)
                    //pi.progressProperty().bind(task.progressProperty())
                    val task = object: Task<Pair<Boolean, Exception?>>() {

                        override fun call():Pair<Boolean, Exception?> {
                            try {
                                Backend.registerUser(name.text.trim(), country.value.trim(), labo.text.trim())
                            } catch (e:Exception) {
                                e.printStackTrace()
                                return Pair(false,e)
                            }
                            return Pair(true,null)
                        }
                    }
                    task.setOnSucceeded { workerStateEvent ->
                        if (task.get().first) {
                            val registerButton = this.getDialogPane().lookupButton(registerButtonType) as Button
                            registerButton.fire()
                        } else {
                            form.isDisable = false
                            root.children.remove(box)
                            val alert = Alert(Alert.AlertType.ERROR)
                            alert.setTitle("Registration problem")
                            alert.setHeaderText(task.get().second?.message)
                            alert.setContentText("If this problem persists, you can send the exception stacktrace below to fjossinet@gmail.com")
                            val sw = StringWriter()
                            val pw = PrintWriter(sw)
                            task.get().second?.printStackTrace(pw)
                            val exceptionText = sw.toString()

                            val label = Label("The exception stacktrace was:")

                            val textArea = TextArea(exceptionText)
                            textArea.isEditable = false
                            textArea.isWrapText = true

                            textArea.maxWidth = Double.MAX_VALUE
                            textArea.maxHeight = Double.MAX_VALUE
                            GridPane.setVgrow(textArea, Priority.ALWAYS)
                            GridPane.setHgrow(textArea, Priority.ALWAYS)

                            val expContent = GridPane()
                            expContent.maxWidth = Double.MAX_VALUE
                            expContent.add(label, 0, 0)
                            expContent.add(textArea, 0, 1)
                            alert.dialogPane.expandableContent = expContent;
                            alert.showAndWait()
                        }
                    }
                    Thread(task).start();
                }
            }
        }

    }

}