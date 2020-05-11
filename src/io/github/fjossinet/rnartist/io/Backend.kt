package io.github.fjossinet.rnartist.io

import com.google.gson.Gson
import io.github.fjossinet.rnartist.gui.Mediator
import io.github.fjossinet.rnartist.core.model.RnartistConfig
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import io.github.fjossinet.rnartist.core.model.Theme
import io.github.fjossinet.rnartist.core.model.WorkingSession
import io.github.fjossinet.rnartist.core.model.io.parseVienna
import javafx.concurrent.Task
import java.awt.geom.Rectangle2D
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import javax.imageio.ImageIO


object Backend {

    @JvmStatic
    @Throws(Exception::class)
    fun registerUser(name:String, country:String, labo:String?) {
        val userID = UUID.randomUUID().toString()
        val client: HttpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
                .uri(URI.create(RnartistConfig.website+"/api/register_user?id=${URLEncoder.encode(userID, "UTF-8")}&name=${URLEncoder.encode(name, "UTF-8")}&country=${URLEncoder.encode(country.trim(), "UTF-8")}&lab=${URLEncoder.encode(labo?.trim(), "UTF-8")}"))
                .build()
        val response: HttpResponse<String> = client.send(request,
                HttpResponse.BodyHandlers.ofString())
        //the id is saved only with the backend was online
        RnartistConfig.userID = userID
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getAllThemes():LinkedHashMap<String, String> {
        val client: HttpClient = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
                .uri(URI.create(RnartistConfig.website+"/api/all_themes"))
                .build()
        val response: HttpResponse<String> = client.send(request,
                HttpResponse.BodyHandlers.ofString())
        val gson = Gson()
        return gson.fromJson(response.body(), List::class.java) as LinkedHashMap<String, String>
    }

    @JvmStatic
    fun submitTheme(mediator: Mediator, name:String, theme:Map<String,String>) {

        val multipart = Multipart(URL(RnartistConfig.website+"/api/submit_theme"))
        multipart.addFormField("userID", RnartistConfig.userID!!)
        multipart.addFormField("name", name)
        for ((k,v) in theme) {
            multipart.addFormField(k, v)
        }

        val task = object: Task<Exception?>() {

            override fun call(): Exception? {
                mediator.secondaryStructureDrawing?.let { drawing ->
                    val previous_viewX = mediator.workingSession!!.viewX
                    val previous_viewY = mediator.workingSession!!.viewY
                    val previous_finalZoomLevel = mediator.workingSession!!.finalZoomLevel
                    try {
                        var ss = SecondaryStructureDrawing(
                            parseVienna(StringReader(">test\nUGCCAAXGCGCA\n(((.(...))))"))!!,
                            mediator.canvas2D.bounds,
                            Theme(
                                RnartistConfig.defaultTheme,
                                mediator.toolbox
                            ),
                            WorkingSession()
                        )
                        mediator.workingSession!!.viewX = 0.0
                        mediator.workingSession!!.viewY = 0.0
                        mediator.workingSession!!.finalZoomLevel = 1.45
                        mediator.workingSession!!.screen_capture = true
                        mediator.workingSession!!.screen_capture_area = Rectangle2D.Double(ss.getBounds().centerX * mediator.workingSession!!.finalZoomLevel - 200.0, ss.getBounds().centerY * mediator.workingSession!!.finalZoomLevel - 150.0, 400.0, 300.0)
                        val image = mediator.canvas2D.screenCapture(ss)
                        val pngFile = File.createTempFile("capture", ".png")
                        ImageIO.write(image, "PNG", pngFile)
                        multipart.addFilePart("capture", pngFile!!, pngFile!!.name, "image")
                        multipart.upload(null)
                    } catch (ex: Exception) {
                        mediator.workingSession!!.screen_capture = false
                        mediator.workingSession!!.screen_capture_area = null
                        mediator.workingSession!!.viewX = previous_viewX
                        mediator.workingSession!!.viewY = previous_viewY
                        mediator.workingSession!!.finalZoomLevel = previous_finalZoomLevel
                        ex.printStackTrace()
                        return ex
                    }
                    mediator.workingSession!!.screen_capture = false
                    mediator.workingSession!!.screen_capture_area = null
                    mediator.workingSession!!.viewX = previous_viewX
                    mediator.workingSession!!.viewY = previous_viewY
                    mediator.workingSession!!.finalZoomLevel = previous_finalZoomLevel
                }
                return null
            }
        }

        Thread(task).start();

    }
}

//Nice class from Ahmed Tarek https://bit.ly/2VIJfO3
class Multipart
/**
 * This constructor initializes a new HTTP POST request with content type
 * is set to multipart/form-data
 * @param url
 * *
 * @throws IOException
 */
@Throws(IOException::class)
constructor(url: URL) {

    companion object {
        private val LINE_FEED = "\r\n"
        private val maxBufferSize = 1024 * 1024
        private val charset = "UTF-8"
    }

    // creates a unique boundary based on time stamp
    private val boundary: String = "===" + System.currentTimeMillis() + "==="
    private val httpConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
    private val outputStream: OutputStream
    private val writer: PrintWriter

    init {

        httpConnection.setRequestProperty("Accept-Charset", "UTF-8")
        httpConnection.setRequestProperty("Connection", "Keep-Alive")
        httpConnection.setRequestProperty("Cache-Control", "no-cache")
        httpConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)
        httpConnection.setChunkedStreamingMode(maxBufferSize)
        httpConnection.doInput = true
        httpConnection.doOutput = true    // indicates POST method
        httpConnection.useCaches = false
        outputStream = httpConnection.outputStream
        writer = PrintWriter(OutputStreamWriter(outputStream, charset), true)
    }

    /**
     * Adds a form field to the request
     * @param name  field name
     * *
     * @param value field value
     */
    fun addFormField(name: String, value: String) {
        writer.append("--").append(boundary).append(LINE_FEED)
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"")
                .append(LINE_FEED)
        writer.append(LINE_FEED)
        writer.append(value).append(LINE_FEED)
        writer.flush()
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName  - name attribute in <input type="file" name="..."></input>
     * *
     * @param uploadFile - a File to be uploaded
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun addFilePart(fieldName: String, uploadFile: File, fileName: String, fileType: String) {
        writer.append("--").append(boundary).append(LINE_FEED)
        writer.append("Content-Disposition: file; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED)
        writer.append("Content-Type: ").append(fileType).append(LINE_FEED)
        writer.append(LINE_FEED)
        writer.flush()

        val inputStream = FileInputStream(uploadFile)
        inputStream.copyTo(outputStream, maxBufferSize)

        outputStream.flush()
        inputStream.close()
        writer.append(LINE_FEED)
        writer.flush()
    }

    /**
     * Adds a header field to the request.
     * @param name  - name of the header field
     * *
     * @param value - value of the header field
     */
    fun addHeaderField(name: String, value: String) {
        writer.append(name + ": " + value).append(LINE_FEED)
        writer.flush()
    }

    /**
     * Upload the file and receive a response from the server.
     * @param onFileUploadedListener
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun upload(onFileUploadedListener: OnFileUploadedListener?) {
        writer.append(LINE_FEED).flush()
        writer.append("--").append(boundary).append("--")
                .append(LINE_FEED)
        writer.close()

        try {
            // checks server's status code first
            val status = httpConnection.responseCode
            if (status == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(httpConnection
                        .inputStream))
                val response = reader.use(BufferedReader::readText)
                httpConnection.disconnect()
                onFileUploadedListener?.onFileUploadingSuccess(response)
            } else {
                onFileUploadedListener?.onFileUploadingFailed(status)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    interface OnFileUploadedListener {
        fun onFileUploadingSuccess(response: String)

        fun onFileUploadingFailed(responseCode: Int)
    }

}