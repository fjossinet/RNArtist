package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.ResidueDrawing
import io.github.fjossinet.rnartist.core.model.getHTMLColorString
import javafx.application.Platform
import java.io.*
import java.net.HttpURLConnection
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

                    val data = URLEncoder.encode("command", "UTF-8") + "=" + URLEncoder.encode(command, "UTF-8")

                    val url = URL("$baseURL?$data")

                    var response:String? = null
                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "GET"
                        try {
                            if (command.trim().equals("version"))
                                Thread.sleep(3000)
                            inputStream.bufferedReader().use {
                                response = it.readText()
                            }
                        } catch (e: Exception) {
                            //mediator.tertiaryStructureButtonsPanel.chimeraConnected(false)
                        }
                    }
                    if (command.trim().equals("version")) {
                        Platform.runLater {
                            response?.trim()?.let {
                                //mediator.tertiaryStructureButtonsPanel.chimeraConnected(it.startsWith("UCSF ChimeraX version:"))
                            } ?: run {
                                //mediator.tertiaryStructureButtonsPanel.chimeraConnected(false)
                            }
                        }
                        Thread.sleep(100)
                    }
                    return null
                }
            }.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun selectResidues(positions: List<Int>) {
        mediator.currentDrawing.get()?.secondaryStructureDrawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? =  drawing.secondaryStructure.tertiaryStructure?.getNumberingSystem()
            if (numberingSystem != null) {
                val command =
                    StringBuffer("select /${chainName}:")
                for (pos in positions) command.append(numberingSystem[pos - 1]+",")
                postCommand(command.substring(0, command.length - 1))
            }
        }
    }

    override fun setFocus(positions: List<Int>) {
        mediator.currentDrawing.get()?.secondaryStructureDrawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? =  drawing.secondaryStructure.tertiaryStructure?.getNumberingSystem()
            if (numberingSystem != null) {
                val command = StringBuffer("view /${chainName}:")
                for (pos in positions) command.append(numberingSystem[pos - 1]+",")
                postCommand(command.substring(0, command.length - 1))
            }
        }
    }

    override fun color3D(residues: List<ResidueDrawing>) {
        mediator.currentDrawing.get()?.secondaryStructureDrawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? =  drawing.secondaryStructure.tertiaryStructure?.getNumberingSystem()
            if (numberingSystem != null) {
                val colors2residues = mutableMapOf<String, MutableList<ResidueDrawing>>()
                for (r in residues) {
                    val coloredResidues = colors2residues.getOrDefault(getHTMLColorString(r.getColor()), mutableListOf())
                    coloredResidues.add(r)
                    colors2residues[getHTMLColorString(r.getColor())] = coloredResidues
                }

                colors2residues.forEach { (colorCode, residues) ->
                    var command = StringBuffer("color /${chainName}:")
                    residues.forEach {
                        command.append(numberingSystem[it.absPos - 1]+",")
                    }
                    command = StringBuffer(command.removeSuffix(","))
                    command.append(" $colorCode")
                    postCommand(command.toString())
                }

            }
        }
    }

    open fun loadChimeraScript(scriptFile: File) {
        try {
            postCommand("open \"${scriptFile.absolutePath}\"")
            var bldFile = File(scriptFile.parent, "${scriptFile.name.split(".cxc").first()}.bld")
            postCommand("open \"${bldFile.absolutePath}\"")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

}