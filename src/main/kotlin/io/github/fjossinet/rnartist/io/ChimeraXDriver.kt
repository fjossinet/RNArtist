package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.core.model.ResidueDrawing
import io.github.fjossinet.rnartist.core.model.TertiaryStructure
import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import io.github.fjossinet.rnartist.core.io.copyFile
import io.github.fjossinet.rnartist.core.io.createTemporaryFile
import io.github.fjossinet.rnartist.core.io.parsePDB
import io.github.fjossinet.rnartist.io.javaFXToAwt
import javafx.scene.control.Alert
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import javax.swing.SwingWorker


class ChimeraXDriver(mediator:Mediator):ChimeraDriver(mediator) {

    override fun postCommand(command: String): String? {
        if (baseURL == null) return null
        try {
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    println(command)
                    val data = URLEncoder.encode("command", "UTF-8") + "=" + URLEncoder.encode(command, "UTF-8")

                    println(baseURL+"?"+data)

                    val url = URL(baseURL+"?"+data)

                    var response:String ? = null
                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "GET"
                        inputStream.bufferedReader().use {
                            response = it.readText()
                        }
                    }

                    println(response)

                    return null
                }
            }.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun loadTertiaryStructure(f: File) {
        try {
            pdbFile = f
            tertiaryStructures.addAll(parsePDB(FileReader(f)))
            postCommand("open \"${f.absolutePath}\"")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun reloadTertiaryStructure() {
        sessionFile?.let {
            if (it.exists())
                postCommand("open \"${it.absolutePath}\"")
        } ?: run {
            pdbFile?.let {
                if (it.exists())
                    postCommand("open \"${it.absolutePath}\"")
            }
        }
    }

    override fun selectResidues(positions: List<Int>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures) {
                if (ts.rna.name == chainName) {
                    numberingSystem = ts.getNumberingSystem()
                    break
                }
            }
            if (numberingSystem != null) {
                val command =
                    StringBuffer("select /${chainName}:")
                for (pos in positions) command.append(numberingSystem[pos - 1]+",")
                postCommand(command.substring(0, command.length - 1))
            }
        }
    }

    override fun setFocus(positions: List<Int>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures) if (ts.rna.name == chainName) {
                numberingSystem = ts.getNumberingSystem()
                break
            }
            if (numberingSystem != null) {
                val command = StringBuffer("view /${chainName}:")
                for (pos in positions) command.append(numberingSystem[pos - 1]+",")
                postCommand(command.substring(0, command.length - 1))
            }
        }
    }

    override fun color3D(residues: List<ResidueDrawing>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures!!) if (ts.rna.name == chainName) {
                numberingSystem = ts.getNumberingSystem()
                break
            }
            if (numberingSystem != null) {
                val colors2residues = mutableMapOf<String, MutableList<ResidueDrawing>>()
                for (r in residues) {
                    val coloredResidues = colors2residues.getOrDefault(getHTMLColorString(r.getColor()), mutableListOf<ResidueDrawing>())
                    coloredResidues.add(r)
                    colors2residues[getHTMLColorString(r.getColor())] = coloredResidues
                }

                colors2residues.forEach { colorCode, residues ->
                    var command = StringBuffer("color /${chainName}:")
                    residues.forEach {
                        command.append(numberingSystem[it.absPos - 1]+",")
                    }
                    command = StringBuffer(command.removeSuffix(","))
                    command.append(" ${colorCode}")
                    postCommand(command.toString())
                }

            }
        }
    }

}