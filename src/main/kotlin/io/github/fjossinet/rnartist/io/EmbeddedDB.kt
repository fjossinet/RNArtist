package io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.core.model.*
import io.github.fjossinet.rnartist.core.model.io.*
import org.dizitart.no2.*
import org.dizitart.no2.Document.createDocument
import java.io.File

class EmbeddedDB() {

    var rootDir = File(getUserDir(),"db")
    private var userDB:Nitrite

    init {
        val dataFile = File(rootDir,"user")
        this.userDB = Nitrite.builder()
                .compressed()
                .filePath(dataFile.absolutePath)
                .openOrCreate()
    }

    fun getProject(id: NitriteId): SecondaryStructureDrawing? {
        val doc = this.userDB.getCollection("Projects").getById(id) as Document
        val rna = doc.get("rna") as Map<String,String>
        val secondaryStructure = SecondaryStructure(
            RNA(
                rna["name"] as String,
                rna["seq"] as String
            ), source = "db:rnartist:${id.toString()}")

        val structure = doc.get("structure") as Map<String, Map<String,Map<String, String>>>
        val helices = structure["helices"] as Map<String,Map<String, String>>
        val singleStrands = structure["single-strands"] as Map<String,Map<String, String>>
        val secondaries = structure["secondaries"] as Map<String,Map<String, String>>
        val tertiaries = structure["tertiaries"] as Map<String,Map<String, String>>

        for ((location, tertiary) in tertiaries) {
            secondaryStructure.tertiaryInteractions.add(BasePair(Location(location), Edge.valueOf(tertiary["edge5"]!!), Edge.valueOf(
                tertiary["edge3"]!!), Orientation.valueOf(tertiary["orientation"]!!)))
        }

        for ((_, singleStrand) in singleStrands) {
            val location = Location(singleStrand["location"]!!)
            val ss = SingleStrand(singleStrand["name"]!!, location.start, location.end)
            secondaryStructure.singleStrands.add(ss)
        }

        for ((_, helix) in helices) {
            val h = Helix(helix["name"]!!)
            val location = Location(helix["location"]!!)
            for (i in location.start..location.start+location.length/2-1) {
                val l = Location(Location(i), Location(location.end-(i-location.start)))
                val secondary = secondaries[l.toString()]!!
                h.secondaryInteractions.add(BasePair(l, Edge.valueOf(secondary["edge5"]!!), Edge.valueOf(secondary["edge3"]!!), Orientation.valueOf(
                    secondary["orientation"]!!)))
            }
            secondaryStructure.helices.add(h)
        }

        val junctions = structure["junctions"] as Map<String,Map<String, String>>
        for ((_, junction) in junctions) {
            val linkedHelices = mutableListOf<Helix>()
            junction["linked-helices"]!!.split(" ").forEach {
                for (h in secondaryStructure.helices) {
                    if (h.start == Integer.parseInt(it)) {
                        linkedHelices.add(h)
                        break
                    }
                }
            }
            val location = Location(junction["location"]!!)
            val j = Junction(junction["name"]!!, location, linkedHelices)
            secondaryStructure.junctions.add(j)
        }

        //We link the helices to their junctions
        for ((start, helix) in helices) {
            val h = secondaryStructure.helices.find { it.start == Integer.parseInt(start) }!!
            helix["first-junction-linked"]?.let { startPos ->
                h.setJunction(secondaryStructure.junctions.find { it.start == Integer.parseInt(startPos) }!!)
            }

            helix["second-junction-linked"]?.let { startPos ->
                h.setJunction(secondaryStructure.junctions.find { it.start == Integer.parseInt(startPos) }!!)
            }
        }

        val pknots = structure["pknots"] as Map<String,Map<String, String>>

        for ((_, pknot) in pknots) {

            val pk = Pknot(pknot["name"]!!)

            for (h in secondaryStructure.helices) {
                if (h.start ==  Integer.parseInt(pknot["helix"]!!)) {
                    pk.helix = h
                    break
                }
            }
            pknot["tertiaries"]?.split(" ")?.forEach { l ->
                val location = Location(l)
                for (tertiary in secondaryStructure.tertiaryInteractions) {
                    if (tertiary.location.start == location.start && tertiary.location.end == location.end) {
                        pk.tertiaryInteractions.add(tertiary)
                        break;
                    }
                }
            }
            secondaryStructure.pknots.add(pk)
            secondaryStructure.tertiaryInteractions.removeAll(pk.tertiaryInteractions)
        }

        secondaryStructure.source = "Project ${doc.get("name")}"

        val layout = doc.get("layout") as Map<String, Map<String, String>>

        val theme = doc.get("theme") as Map<String, Map<String, Map<String, String>>>

        val workingSession = doc.get("session") as Map<String, String>

        val ws = WorkingSession()
        ws.viewX = workingSession["view-x"]!!.toDouble()
        ws.viewY = workingSession["view-y"]!!.toDouble()
        ws.zoomLevel = workingSession["final-zoom-lvl"]!!.toDouble()

        return parseProject(Project(secondaryStructure, layout, theme, ws))
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

    fun saveProject(id:NitriteId, secondaryStructureDrawing: SecondaryStructureDrawing) {
        val doc = this.userDB.getCollection("Projects").getById(id) as Document

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

        this.userDB.getCollection("Projects").update(doc)
    }

    fun removeProject(id:NitriteId) {
        val doc = this.userDB.getCollection("Projects").getById(id) as Document
        this.userDB.getCollection("Projects").remove(doc)
    }


    fun getProjects():NitriteCollection {
        return this.userDB.getCollection("Projects")
    }


}