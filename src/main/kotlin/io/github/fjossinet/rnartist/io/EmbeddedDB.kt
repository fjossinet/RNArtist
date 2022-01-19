package io.github.fjossinet.rnartist.io

import com.google.gson.Gson
import io.github.fjossinet.rnartist.core.io.*
import io.github.fjossinet.rnartist.core.model.*
import org.dizitart.no2.*
import org.dizitart.no2.Document.createDocument
import org.dizitart.no2.mapper.JacksonFacade
import java.io.File
import java.io.StringReader
import java.util.HashMap

class EmbeddedDB {

    var rootDir = File(getUserDir(),"db")
    private var userDB:Nitrite

    init {
        val dataFile = File(rootDir,"user")
        this.userDB = Nitrite.builder()
                .compressed()
                .filePath(dataFile.absolutePath)
                .openOrCreate()
    }

    fun getProject(id: NitriteId): SecondaryStructureDrawing {
        val doc = this.userDB.getCollection("Projects").getById(id) as Document
        val json = JacksonFacade().toJson(doc)
        return parseJSON(StringReader(json))
    }

    fun getThemeAsJSON(id: NitriteId):  Map<String, Map<String, Map<String, String>>> {
        val document = this.userDB.getCollection("Projects").getById(id) as Document
        val json = JacksonFacade().toJson(document)
        val gson = Gson()
        val map: Map<String, Any> = HashMap()
        val doc = gson.fromJson(StringReader(json), map.javaClass)
        return doc["theme"] as Map<String, Map<String, Map<String, String>>>
    }


    fun getProjects():NitriteCollection {
        return this.userDB.getCollection("Projects")
    }


}