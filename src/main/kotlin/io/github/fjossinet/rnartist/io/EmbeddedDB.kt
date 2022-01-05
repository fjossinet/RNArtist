package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.core.io.*
import io.github.fjossinet.rnartist.core.model.*
import org.dizitart.no2.*
import org.dizitart.no2.Document.createDocument
import org.dizitart.no2.mapper.JacksonFacade
import java.io.File
import java.io.StringReader

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

    fun saveProjectAs(name: String, secondaryStructureDrawing: SecondaryStructureDrawing):NitriteId {
        val doc = createDocument("name",name)

        with (doc) {
            put(
                "rna", mutableMapOf<String, String>(
                    "name" to secondaryStructureDrawing.secondaryStructure.rna.name,
                    "seq" to secondaryStructureDrawing.secondaryStructure.rna.seq
                )
            )

            //STRUCTURE
            put("structure", dumpSecondaryStructure(secondaryStructureDrawing))

            //LAYOUT (the size and orientation of junctions)
            put("layout", dumpLayout(secondaryStructureDrawing))

            //THEME (colors, line width, full details,...)
            put("theme", dumpTheme(secondaryStructureDrawing))

            //WORKING SESSION
            put("session", dumpWorkingSession(secondaryStructureDrawing))
        }

        val r = this.userDB.getCollection("Projects").insert(doc)
        return r.first()
    }


    fun getProjects():NitriteCollection {
        return this.userDB.getCollection("Projects")
    }


}