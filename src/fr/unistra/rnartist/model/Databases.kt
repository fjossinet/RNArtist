package fr.unistra.rnartist.model

import fr.unistra.rnartist.gui.Mediator
import fr.unistra.rnartist.gui.RNArtist
import fr.unistra.rnartist.io.Rnaview
import fr.unistra.rnartist.io.parsePDB
import fr.unistra.rnartist.utils.RnartistConfig
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

enum class PDBQueryField {
    MINRES,
    MAXRES,
    MINDATE,
    MAXDATE,
    KEYWORDS,
    AUTHORS,
    PDBIDS,
    TITLE_CONTAINS,
    CONTAINS_RNA,
    CONTAINS_PROTEIN,
    CONTAINS_DNA,
    CONTAINS_HYBRID,
    EXPERIMENTAL_METHOD
}

class PDB() {

    fun getEntry(pdbID: String): Reader {
        val url = URL("http://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId=$pdbID")
        return StringReader(url.readText())
    }

    fun query(query: Map<PDBQueryField, Any?>): List<String> {
        val min_res:String = query.getOrDefault(PDBQueryField.MINRES, "1") as String
        val max_res:String = query.getOrDefault(PDBQueryField.MAXRES, "3") as String
        val min_date:String? = query.getOrDefault(PDBQueryField.MINDATE, null) as String?
        val max_date:String? = query.getOrDefault(PDBQueryField.MAXDATE, null) as String?
        val keywords:List<String> = query.getOrDefault(PDBQueryField.KEYWORDS, listOf<String>()) as List<String>
        val authors = query.getOrDefault(PDBQueryField.AUTHORS, listOf<String>()) as List<String>
        val pdb_ids = query.getOrDefault(PDBQueryField.PDBIDS, listOf<String>()) as List<String>
        val title_contains:List<String> = query.getOrDefault(PDBQueryField.TITLE_CONTAINS, listOf<String>()) as List<String>
        val contains_rna:Char = query.getOrDefault(PDBQueryField.CONTAINS_RNA, 'Y') as Char
        val contains_protein:Char = query.getOrDefault(PDBQueryField.CONTAINS_PROTEIN, 'Y') as Char
        val contains_dna:Char = query.getOrDefault(PDBQueryField.CONTAINS_DNA, 'N') as Char
        val contains_hybrid:Char = query.getOrDefault(PDBQueryField.CONTAINS_HYBRID, 'N') as Char
        val experimental_method:String = query.getOrDefault(PDBQueryField.EXPERIMENTAL_METHOD, "X-RAY") as String

        var refinementLevel = 0

        var post_data = """<?xml version="1.0" encoding="UTF-8"?>
<orgPdbCompositeQuery version="1.0">"""

        if (experimental_method == "X-RAY") {
            if (refinementLevel != 0)
                post_data += "\n<queryRefinement>\n<queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
            else
                post_data += "\n<queryRefinement>\n<queryRefinementLevel>$refinementLevel</queryRefinementLevel>"
            post_data += """
    <orgPdbQuery>
        <version>head</version>
        <queryType>org.pdb.query.simple.ResolutionQuery</queryType>
        <description>Resolution query</description>
        <refine.ls_d_res_high.comparator>between</refine.ls_d_res_high.comparator>
        <refine.ls_d_res_high.min>$min_res</refine.ls_d_res_high.min>
        <refine.ls_d_res_high.max>$max_res</refine.ls_d_res_high.max>
"""
            post_data += "</orgPdbQuery></queryRefinement>"
            refinementLevel += 1
        }
        if (max_date != null ||  min_date != null) {
            if (refinementLevel != 0)
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
            else
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel>"

            post_data += """
    <orgPdbQuery>
        <version>head</version>
        <queryType>org.pdb.query.simple.ReleaseDateQuery</queryType>
        <description>Release Date query</description>
        <refine.ls_d_res_high.comparator>between</refine.ls_d_res_high.comparator>"""

            if (min_date != null) {
                post_data += "<database_PDB_rev.date.min>$min_date</database_PDB_rev.date.min>"
            }
            if (max_date != null) {
                post_data += "<database_PDB_rev.date.max>$max_date</database_PDB_rev.date.maw>"
            }

            post_data += "</orgPdbQuery></queryRefinement>"
            refinementLevel += 1
        }

        for (w in title_contains) {
            if (refinementLevel != 0)
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
            else
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel>"

            post_data += """
    <orgPdbQuery> 
        <version>head</version>
        <queryType>org.pdb.query.simple.StructTitleQuery</queryType>
        <description>StructTitleQuery: struct.title.comparator=contains struct.title.value=$w</description>
        <struct.title.comparator>contains</struct.title.comparator>
        <struct.title.value>$w</struct.title.value>
    </orgPdbQuery>
</queryRefinement>"""

            refinementLevel += 1
        }

       if (!keywords.isEmpty()) {
            if (refinementLevel != 0)
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
            else
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel>"

            post_data += """
    <orgPdbQuery> 
        <version>head</version>
        <queryType>org.pdb.query.simple.AdvancedKeywordQuery</queryType>
        <description>Text Search for: '+" ".join(keywords)+'</description>
        <keywords>${keywords.joinToString(" ")}+'</keywords>
    </orgPdbQuery>
</queryRefinement>"""

            refinementLevel += 1
        }

        if (!pdb_ids.isEmpty()) {
            if (refinementLevel != 0)
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
            else
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel>"

            post_data += """
    <orgPdbQuery> 
        <version>head</version>
        <queryType>org.pdb.query.simple.AdvancedKeywordQuery</queryType>
        <description>Simple query for a list of PDB IDs ('${pdb_ids.size} IDs) :${pdb_ids.joinToString(" ")}</description>
        <structureIdList>${pdb_ids.joinToString(" ")}</structureIdList>
    </orgPdbQuery>
</queryRefinement>"""

            refinementLevel += 1
        }

        if (refinementLevel != 0)
            post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
        else
            post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel>"

        post_data += """
    <orgPdbQuery>
        <version>head</version>
        <queryType>org.pdb.query.simple.ExpTypeQuery</queryType>
        <description>Experimental Method is $experimental_method</description>
        <mvStructure.expMethod.value>$experimental_method</mvStructure.expMethod.value>
    </orgPdbQuery>
</queryRefinement>"""

        refinementLevel += 1

        for (author in authors) {
            if (refinementLevel != 0)
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
            else
                post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel>"

            post_data += """
    <orgPdbQuery>
        <version>head</version>
        <queryType>org.pdb.query.simple.AdvancedAuthorQuery</queryType>
        <description>Author Search: Author Search: audit_author.name=$author OR (citation_author.name=$author AND citation_author.citation_id=primary)</description>
        <exactMatch>false</exactMatch>
        <audit_author.name>$author</audit_author.name>
    </orgPdbQuery>
</queryRefinement>"""

            refinementLevel += 1
        }

        if (refinementLevel != 0)
            post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel><conjunctionType>and</conjunctionType>"
        else
            post_data += "\n<queryRefinement><queryRefinementLevel>$refinementLevel</queryRefinementLevel>"


        post_data += """
    <orgPdbQuery>
        <version>head</version>
        <queryType>org.pdb.query.simple.ChainTypeQuery</queryType>
        <description>Chain Type</description>
        <containsProtein>$contains_protein</containsProtein>
        <containsDna>$contains_dna</containsDna>
        <containsRna>$contains_rna</containsRna>
        <containsHybrid>$contains_hybrid</containsHybrid>
    </orgPdbQuery>
</queryRefinement>"""

        refinementLevel += 1

        post_data += "</orgPdbCompositeQuery>"

        var pdbIds = mutableListOf<String>()

        val mURL = URL("http://www.rcsb.org/pdb/rest/search")

        with(mURL.openConnection() as HttpURLConnection) {
            doOutput = true
            requestMethod = "POST"

            val wr = OutputStreamWriter(getOutputStream());
            wr.write(post_data);
            wr.flush();

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    pdbIds.add(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
            }
        }
        return pdbIds
    }

    fun feedEmbeddedDatabase(mediator: Mediator) {
        val pdb = PDB()
        val pdbIds = pdb.query(mapOf<PDBQueryField, Any>())
        val idsWithPbs: MutableList<String> = ArrayList()
        val f = File(File(RnartistConfig.getUserDir(), "db"), "idsWithPb.txt")
        if (!f.exists()) f.createNewFile() else {
            val `in` = BufferedReader(FileReader(f))
            var l: String? = null
            while (`in`.readLine().also { l = it } != null) {
                idsWithPbs.add(l!!.trim { it <= ' ' })
            }
            `in`.close()
        }
        val buff = BufferedWriter(FileWriter(f, true))
        val rnaview = Rnaview(mediator)
        for (id in pdbIds) {
            println("Parsing $id")
            if (idsWithPbs.contains(id)) {
                continue
            }
            if (mediator.embeddedDB.getPDBSecondaryStructure(id) == null) {
                for (ts in parsePDB(pdb.getEntry(id))!!.iterator()) {
                    ts.pdbId = id
                    try {
                        val ss = rnaview.annotate(ts)
                        ss.pdbId = ts.pdbId
                        ss.title = ts.title
                        ss.authors = ts.authors
                        ss.pubDate = ts.pubDate
                        ss.rna.name = ts.rna.name
                        mediator.embeddedDB.addPDBSecondaryStructure(ss)
                        if (ss.rna.length != ts.rna.length) //RNAVIEW can remove some residues silently
                            mediator.embeddedDB.addPDBTertiaryStructure(ts)
                        println("############# Secondary Structure Stored ################")
                        println("############# ${mediator.embeddedDB.getPDBSecondaryStructures().find().size()} Secondary Structures Stored ################")
                    } catch (ex: java.lang.Exception) {
                        if (!idsWithPbs.contains(id)) {
                            idsWithPbs.add(id)
                            buff.write("$id\n")
                            buff.flush()
                            println(ex.message)
                        }
                    }
                }
            } else {
                println("Already Stored")
            }
        }

    }


}

class NDB {

    fun listPDBFileNames():List<String> {
        var files = mutableSetOf<String>()
        val pattern = "([0-9].+?\\.pdb[0-9])".toRegex()
        val matches = pattern.findAll(URL("http://ndbserver.rutgers.edu/files/ftp/NDB/coordinates/na-biol/").readText())
        matches.forEach { matchResult ->
            files.add(matchResult.value)
        }
        return files.toList()
    }

    fun getEntry(pdbFileName: String): Reader {
        val url = URL("http://ndbserver.rutgers.edu/files/ftp/NDB/coordinates/na-biol/$pdbFileName")
        return StringReader(url.readText())
    }

    fun feedEmbeddedDatabase(mediator: Mediator) {
        val pdbFileNames: List<String> = this.listPDBFileNames()
        val filesWithPbs = mutableListOf<String>()
        val filesParsed = mutableListOf<String>()
        val idsWithPb = File(File(RnartistConfig.getUserDir(), "db"), "idsWithPb.txt")
        if (!idsWithPb.exists()) idsWithPb.createNewFile() else {
            val filesWithPbBuff = BufferedReader(FileReader(idsWithPb))
            var l: String? = null
            while (filesWithPbBuff.readLine().also { l = it } != null) {
                filesWithPbs.add(l!!.trim { it <= ' ' })
            }
            filesWithPbBuff.close()
        }
        val filesProcessed = File(File(RnartistConfig.getUserDir(), "db"), "filesProcessed.txt")
        if (!filesProcessed.exists()) filesProcessed.createNewFile() else {
            val filesProcessedBuff = BufferedReader(FileReader(filesProcessed))
            var l: String? = null
            while (filesProcessedBuff.readLine().also { l = it } != null) {
                filesParsed.add(l!!.trim { it <= ' ' })
            }
            filesProcessedBuff.close()
        }
        val filesWithPbWriter = BufferedWriter(FileWriter(idsWithPb, true))
        val filesProcessedWriter = BufferedWriter(FileWriter(filesProcessed, true))
        val rnaview = Rnaview(mediator)
        for (fileName in pdbFileNames) {
            println("Parsing $fileName")
            val id = fileName.split(".pdb".toRegex()).toTypedArray()[0].toUpperCase()
            if (filesParsed.contains(fileName)) {
                println("File Already Stored")
                continue
            } else if (filesWithPbs.contains(fileName)) {
                println("File with problem")
                continue
            }
            try {
                for (ts in parsePDB(this.getEntry(fileName))!!.iterator()) {
                    ts.pdbId = id
                    if (mediator.embeddedDB.getPDBSecondaryStructure(id, ts.rna.name) != null) {
                        println("chain " + ts.rna.name + " for " + id + " already stored") //we can have several times the same chain in the PDB
                        if (!filesParsed.contains(fileName)) {
                            filesParsed.add(fileName)
                            filesProcessedWriter.write("$fileName\n")
                            filesProcessedWriter.flush()
                        }
                        continue
                    }
                    try {
                        val ss = rnaview.annotate(ts)
                        ss.pdbId = ts.pdbId
                        ss.title = ts.title
                        ss.authors = ts.authors
                        ss.pubDate = ts.pubDate
                        ss.rna.name = ts.rna.name
                        mediator.embeddedDB.addPDBSecondaryStructure(ss) //RNAVIEW can remove some residues silently
                        if (ss.rna.length != ts.rna.length)
                            mediator.embeddedDB.addPDBTertiaryStructure(ts)
                        println("############# Secondary Structure Stored ################")
                        println("############# ${mediator.embeddedDB.getPDBSecondaryStructures().find().size()} Secondary Structures Stored ################")
                        if (!filesParsed.contains(fileName)) {
                            filesParsed.add(fileName)
                            filesProcessedWriter.write("$fileName\n")
                            filesProcessedWriter.flush()
                        }
                    } catch (ex: Exception) {
                        if (!filesWithPbs.contains(fileName)) {
                            filesWithPbs.add(fileName)
                            filesWithPbWriter.write("$fileName\n")
                            filesWithPbWriter.flush()
                            println("File with problem")
                        }
                    }
                }
            } catch (e: Exception) {
                if (!filesWithPbs.contains(fileName)) {
                    filesWithPbs.add(fileName)
                    filesWithPbWriter.write("$fileName\n")
                    filesWithPbWriter.flush()
                    println("File with problem")
                }
            }
        }
    }


}