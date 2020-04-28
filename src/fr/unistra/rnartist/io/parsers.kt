package fr.unistra.rnartist.io

import fr.unistra.rnartist.model.*
import org.jdom.Element
import org.jdom.input.SAXBuilder
import java.io.*
import java.text.NumberFormat
import java.util.*

@Throws(java.lang.Exception::class)
fun parseRnaml(f: File?): SecondaryStructure? {
    val builder = SAXBuilder(false)
    builder.validation = false
    builder.setFeature("http://xml.org/sax/features/validation", false)
    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    val document = builder.build(f)
    val root = document.rootElement
    var child: Element? = null
    var name: String? = null
    var m: RNA? = null
    var ss: SecondaryStructure? = null
    val i: Iterator<*> = root.children.iterator()
    while (i.hasNext()) {
        child = i.next() as Element?
        name = child!!.name
        if (name == "molecule") {
            var moleculeSequence = ""
            val moleculeName = "RNA"
            val sequence = child.getChild("sequence")
            if (sequence != null) {
                val seqdata = sequence.getChild("seq-data")
                if (seqdata != null) moleculeSequence = seqdata.value.trim { it <= ' ' }.replace("\\s+".toRegex(), "")
            }
            m = RNA(moleculeName, moleculeSequence)
            val bps: MutableList<BasePair> = ArrayList()
            val structure = child.getChild("structure")
            if (structure != null) {
                val str_annotation = structure.getChild("model").getChild("str-annotation")
                for (e in str_annotation.getChildren("base-pair")) {
                    val bp = e as Element?
                    var edge1 = Edge.WC
                    var edge2 = Edge.WC
                    edge1 = when (bp!!.getChild("edge-5p").text[0]) {
                        'S', 's' -> Edge.Sugar
                        'H' -> Edge.Hoogsteen
                        'W', '+', '-' -> Edge.WC
                        '!', '?' -> Edge.SingleHBond
                        else -> Edge.Unknown
                    }
                    edge2 = when (bp.getChild("edge-3p").text[0]) {
                        'S', 's' -> Edge.Sugar
                        'H' -> Edge.Hoogsteen
                        'W', '+', '-' -> Edge.WC
                        '!', '?' -> Edge.SingleHBond
                        else -> Edge.Unknown
                    }
                    var orientation = Orientation.cis
                    orientation = when (bp.getChild("bond-orientation").text.toUpperCase().toCharArray()[0]) {
                        'C' -> Orientation.cis
                        'T' -> Orientation.trans
                        else -> Orientation.Unknown
                    }
                    val l = Location(Location(bp.getChild("base-id-5p").getChild("base-id").getChild("position").text.toInt()), Location(bp.getChild("base-id-3p").getChild("base-id").getChild("position").text.toInt()))
                    bps.add(BasePair(l, edge1, edge2, orientation))
                }
            }
            ss = SecondaryStructure(m, null, bps)
        }
    }
    return ss
}

fun parseVienna(reader: Reader): SecondaryStructure? {
    val sequence = StringBuffer()
    val bn = StringBuffer()
    val name = StringBuffer()
    val `in` = BufferedReader(reader)
    var line: String? = null
    while (`in`.readLine().also { line = it } != null) {
        if (line!!.startsWith(">"))
            name.append(line!!.substring(1))
        else if (line!!.matches(Regex("^[A-Z]+$"))) {
            sequence.append(line)
        } else if (line!!.matches(Regex("^[\\.\\(\\)\\{\\}\\[\\]]+$"))) {
            bn.append(line)
        }
    }
    return SecondaryStructure(RNA(name.toString(),sequence.toString()), bracketNotation = bn.toString())
}

@Throws(Exception::class)
fun parseCT(reader: Reader): SecondaryStructure? {
    val sequence = StringBuffer()
    val bn = StringBuffer()
    val `in` = BufferedReader(reader)
    var line: String? = null
    while (`in`.readLine().also { line = it } != null) {
        line = line!!.trim { it <= ' ' }
        val tokens = line!!.split("\\s+".toRegex()).toTypedArray()
        if (tokens.size != 6 || !tokens[0].matches(Regex("-?\\d+(.\\d+)?"))) continue
        sequence.append(tokens[1])
        var base5: Int
        var base3: Int
        base5 = tokens[0].toInt()
        base3 = tokens[4].toInt()
        if (base3 != 0) {
            if (base5 <= base3) bn.append("(") else bn.append(")")
        } else {
            bn.append(".")
        }
    }
    return SecondaryStructure(RNA("A", sequence.toString()), bn.toString(), null)
}

@Throws(java.lang.Exception::class)
fun parseBPSeq(reader: Reader?): SecondaryStructure? {
    val sequence = StringBuffer()
    val bn = StringBuffer()
    val `in` = BufferedReader(reader)
    var line: String? = null
    while (`in`.readLine().also { line = it } != null) {
        line = line!!.trim { it <= ' ' }
        val tokens = line!!.split(" ".toRegex()).toTypedArray()
        if (tokens.size != 3 || !tokens[0].matches(Regex("-?\\d+(.\\d+)?"))) continue
        sequence.append(tokens[1])
        var base5: Int
        var base3: Int
        base5 = tokens[0].toInt()
        base3 = tokens[2].toInt()
        if (base3 != 0) {
            if (base5 <= base3) bn.append("(") else bn.append(")")
        } else {
            bn.append(".")
        }
    }
    return SecondaryStructure(RNA("A", sequence.toString()), bn.toString(), null)
}

fun writePDB(ts:TertiaryStructure , exportNumberingSystem: Boolean, writer: Writer) {
    val pw = PrintWriter(writer)
    var atomID = 0
    val coordFormat = NumberFormat.getInstance(Locale.ENGLISH)
    coordFormat.minimumFractionDigits = 3
    coordFormat.maximumFractionDigits = 3
    for (residue in ts.residues) {
        for (a in residue.atoms) {
            if (a.hasCoordinatesFilled()) {
                pw.print(formatPDBField(6, "ATOM", LEFT_ALIGN))
                pw.print(formatPDBField(11 - 7 + 1, "" + ++atomID, RIGHT_ALIGN))
                pw.print("  ")
                pw.print(formatPDBField(16 - 13 + 1, a.name.replace('\'', '*'), LEFT_ALIGN))
                pw.print(formatPDBField(20 - 18 + 1, residue.name, RIGHT_ALIGN))
                pw.print(formatPDBField(1, " " + ts.rna.name.get(0), LEFT_ALIGN))
                if (exportNumberingSystem) pw.print(formatPDBField(26 - 23 + 1, residue.label!!, RIGHT_ALIGN)) else pw.print(formatPDBField(26 - 23 + 1, "" + residue.absolutePosition, RIGHT_ALIGN))
                pw.print(formatPDBField(1, "", LEFT_ALIGN))
                pw.print("   ")
                pw.print(formatPDBField(38 - 31 + 1, "" + coordFormat.format(a.x), RIGHT_ALIGN))
                pw.print(formatPDBField(46 - 39 + 1, "" + coordFormat.format(a.y), RIGHT_ALIGN))
                pw.print(formatPDBField(54 - 47 + 1, "" + coordFormat.format(a.z), RIGHT_ALIGN))
                pw.print(formatPDBField(60 - 55 + 1, "1.00", RIGHT_ALIGN))
                pw.print(formatPDBField(66 - 61 + 1, "100.00", RIGHT_ALIGN))
                pw.print(formatPDBField(10, "", LEFT_ALIGN))
                pw.print(formatPDBField(78 - 77 + 1, "" + a.name[0], RIGHT_ALIGN))
                pw.println(formatPDBField(2, "", LEFT_ALIGN))
            }
        }
    }
    pw.println("END   ")
    pw.close()
}

val LEFT_ALIGN = 0
val RIGHT_ALIGN = 1

private fun formatPDBField(finalSize: Int, word: String, align: Int): String? {
    val field = StringBuffer()
    if (align == LEFT_ALIGN) {
        field.append(word)
        for (i in 0 until finalSize - word.length) {
            field.append(" ")
        }
    } else {
        for (i in 0 until finalSize - word.length) {
            field.append(" ")
        }
        field.append(word)
    }
    return field.toString()
}

fun parsePDB(reader: Reader): List<TertiaryStructure>? {
    val tertiaryStructures: MutableList<TertiaryStructure> = ArrayList()
    val title = StringBuffer()
    val authors = StringBuffer()
    val pubDate = StringBuffer()
    val fullContent = StringBuffer()
    try {
        var ts: TertiaryStructure? = null
        var tag = "fake"
        var nucleic_chain_id = 1
        var protein_chain_id = 1
        var molecule_label = "fake"
        var rna: RNA? = null
        var nt_id = 0
        var aa_id = 0
        var resId = "fake"
        var r: Residue3D? = null
        //necessary to store the atoms parameters until the parser knowns if it parses a nucleotide or an aminoacid
        val atoms: MutableMap<String, FloatArray> = HashMap()
        var isInsideNucleicAcid = false
        var isInsideProtein = false
        var ter_tag = false
        var line: String? = null
        val input = BufferedReader(reader)
        while (input.readLine().also { line = it } != null) {
            fullContent.append(line)
            tag = try {
                line!!.substring(0, 6).trim { it <= ' ' }
            } catch (e: IndexOutOfBoundsException) {
                "fake"
            }
            if ((tag.equals("ATOM", ignoreCase = true) || tag.equals("HETATM", ignoreCase = true)) && !chainsIgnored.contains(line!!.substring(17, 21).trim { it <= ' ' }) && !residuesIgnored.contains(line!!.substring(12, 16).trim { it <= ' ' }) && line!!.substring(21, 22).trim { it <= ' ' }.length != 0 /*only if the ATOM or HETATM line precises a molecule name*/) {
                //with the following statement we're testing if we have a new molecule
                //we have a new molecule if the molecule name has changed (even if no TER tag has been meet the line before
                //we have a new molecule if the TER tag has been meet AND if the molecule name has changed (some PDB or PDB exported by PyMOL can have a TER tag in a middle of a molecular chain
                if (!molecule_label.equals(line!!.substring(21, 22).trim { it <= ' ' }, ignoreCase = true) || ter_tag && !molecule_label.equals(line!!.substring(21, 22).trim { it <= ' ' }, ignoreCase = true)) {
                    //name.length==0 for H2O, Magnesium ions, .... For residues not inside a macromolecule
                    if ((isInsideNucleicAcid || isInsideProtein) && molecule_label.length > 0) {
                        if (isInsideNucleicAcid) {
                            nucleic_chain_id++
                            isInsideNucleicAcid = false
                        } else {
                            protein_chain_id++
                            isInsideProtein = false
                        }
                    }
                    molecule_label = line!!.substring(21, 22).trim { it <= ' ' }
                    rna = null
                    ts = null
                    nt_id = 0
                    aa_id = 0
                    resId = line!!.substring(22, 27).trim { it <= ' ' }
                    r = null
                    ter_tag = false
                } else if (!resId.equals(line!!.substring(22, 27).trim { it <= ' ' }, ignoreCase = true) && isInsideNucleicAcid) r = null
                //residue is a nucleotide if the 04' atom is detected
                if (line!!.substring(12, 16).trim { it <= ' ' } == "O4*" || line!!.substring(12, 16).trim { it <= ' ' } == "O4'") {
                    nt_id++
                    resId = line!!.substring(22, 27).trim { it <= ' ' }
                    if (!isInsideNucleicAcid) {
                        isInsideNucleicAcid = true
                        isInsideProtein = false
                    }
                    if (rna == null && ts == null) {
                        rna = RNA(molecule_label, "")
                        ts = TertiaryStructure(rna)
                        tertiaryStructures.add(ts)
                    }
                    rna!!.addResidue(line!!.substring(17, 21).trim { it <= ' ' }.toUpperCase())
                    r = ts!!.addResidue3D(nt_id)
                    if (r == null) throw java.lang.Exception("Unknown residue " + line!!.substring(17, 21).trim { it <= ' ' }.toUpperCase())
                    r.label = resId
                    for ((key, value) in atoms) r.setAtomCoordinates(key, value[0], value[1], value[2])
                    atoms.clear()
                } else if (line!!.substring(12, 16).trim { it <= ' ' } == "CA") {
                    aa_id++
                    isInsideProtein = true
                    isInsideNucleicAcid = false
                }
                val coord = floatArrayOf(line!!.substring(30, 38).trim { it <= ' ' }.toFloat(), line!!.substring(38, 46).trim { it <= ' ' }.toFloat(), line!!.substring(46, 54).trim { it <= ' ' }.toFloat())
                if (r != null) {
                    r.setAtomCoordinates(line!!.substring(12, 16).trim { it <= ' ' }, coord[0], coord[1], coord[2])
                } else atoms[line!!.substring(12, 16).trim { it <= ' ' }] = coord
            } else if (tag.equals("TER", ignoreCase = true)) {
                ter_tag = true
            } else if (tag.equals("TITLE", ignoreCase = true)) {
                title.append(line!!.substring(6).trim { it <= ' ' })
            } else if (tag.equals("AUTHOR", ignoreCase = true)) {
                authors.append(line!!.substring(6).trim { it <= ' ' })
            } else if (tag.equals("JRNL", ignoreCase = true) && line!!.substring(12, 16).trim { it <= ' ' } == "REF") {
                if (line!!.substring(19, 34) != "TO BE PUBLISHED") {
                    pubDate.append(line!!.substring(62, 66).trim { it <= ' ' })
                } else {
                    pubDate.append("To be published")
                }
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }
    for (ts in tertiaryStructures) {
        var t = title.toString().toLowerCase()
        t = t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase()
        ts.title = t
        t = authors.toString().split(",".toRegex()).toTypedArray()[0]
        ts.authors = t
        ts.pubDate = pubDate.toString()
    }
    return tertiaryStructures
}

var residuesIgnored: MutableList<String> = mutableListOf(
        "MG",
        "K",
        "NA",
        "CL",
        "SR",
        "CD",
        "ACA"
)

var chainsIgnored: MutableList<String> = mutableListOf(
        "FMN",
        "PRF",
        "HOH",
        "MG",
        "OHX",
        "MN",
        "ZN",
        "SO4",
        "CA",
        "UNK",
        "N"
)