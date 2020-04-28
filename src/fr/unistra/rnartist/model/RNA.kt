package fr.unistra.rnartist.model

import java.io.Serializable
import java.util.*
import kotlin.collections.HashSet

class Block(start:Int,end:Int):Serializable {

    val start = if (start < end) start else end
    val end = if (start > end) start else end
    val length = end-start+1
    val positions = (start..end).toList()

    fun contains(position:Int) = position in start..end

    override fun toString(): String {
        return "$start-$end"
    }
}

class Location:Serializable {

    var blocks = mutableListOf<Block>()
    val positions: Collection<Int>
        get() {
            val positions = arrayListOf<Int>()
            this.blocks.forEach {
                positions.addAll(it.positions)
            }
            return positions.sorted()
        }
    val start:Int
        get() {
            return this.blocks.first().start
        }
    val end:Int
        get() {
            return this.blocks.last().end
        }
    val length:Int
        get() {
            return this.blocks.sumBy { it.length }
        }
    val description:String
        get() {
            return this.blocks.joinToString { it.toString() }
        }

    private constructor() {
    }

    constructor(start:Int,end:Int):this() {
        this.blocks.add(Block(start,end))
    }

    constructor(pos:Int):this(pos, pos) {
    }

    constructor(description:String):this() {
        for (s in description.split(",")) {
            if (s.contains('-')) {
                val ends = s.trim().split('-').map { it.toInt() }
                this.blocks.add(Block(ends.first(), ends.last()))
            } else {
                this.blocks.add(Block(s.trim().toInt(), s.trim().toInt()))
            }
        }
        this.blocks.sortBy { it.start }
    }

    constructor(positions:IntArray):this() {
        this.blocks.addAll(toBlocks(positions))
    }

    constructor(l1: Location, l2: Location):this((l1.positions + l2.positions).distinct().sorted().toIntArray()) {
    }

    fun addPosition(position:Int) {
        val mutableSet = this.positions.toMutableSet()
        mutableSet.add(position)
        this.blocks.clear()
        this.blocks.addAll(toBlocks(mutableSet.toIntArray()))
    }

    fun remove(l: Location) {
        val difference: Location = this.differenceOf(l)
        this.blocks = difference.blocks
    }

    fun differenceOf(l: Location): Location {
        return Location((this.positions - l.positions).toIntArray())
    }


    fun contains(position:Int) = this.blocks.any { it.contains(position) }
}

class RNA(var name:String="A", seq:String):Serializable {

    val length:Int
        get() {
            return this.seq.length
        }

    private var _seq = java.lang.StringBuilder(seq)

    var seq:String = ""
        get() {
            return this._seq.toString()
        }

    fun addResidue(residue:String) {
        val unModifiedNucleotide = modifiedNucleotides.get(residue);
        if (unModifiedNucleotide != null)
            this._seq.append(unModifiedNucleotide);
        else {
            if ("ADE".equals(residue) || "A".equals(residue))
                this._seq.append("A");
            else if ("URA".equals(residue) || "URI".equals(residue) || "U".equals(residue))
                this._seq.append("U");
            else if ("GUA".equals(residue) || "G".equals(residue))
                this._seq.append("G");
            else if ("CYT".equals(residue) || "C".equals(residue))
                this._seq.append("C");
            else if ("a".equals(residue) || "u".equals(residue) || "g".equals(residue) || "c".equals(residue) || "t".equals(residue))
                this._seq.append(residue);
            else if ("X".equals(residue))
                this._seq.append("X")
            else if ("N".equals(residue))
                this._seq.append("N")
        }
    }

    fun getResidue(pos:Int):Char {
        return if (pos <= 0 || pos > this.length)
            throw RuntimeException("The position asked for is outside the molecule's boundaries")
        else
            this._seq[pos-1]
    }

    fun subSequence(l:Location):String {
        return this._seq.substring(l.start-1,l.end).toString()
    }

    override fun toString(): String {
        return name
    }
}

class BasePair(val location:Location, val edge5:Edge = Edge.WC, val edge3:Edge = Edge.WC, val orientation:Orientation = Orientation.cis):Serializable{

    override fun toString(): String {
        return "$location $orientation:$edge5:$edge3"
    }

}

class SingleStrand(val name:String?="MySingleStrand", start:Int, end:Int):Serializable {

    val location = Location(start,end)

    val length:Int
        get() {
            return this.location.length
        }
}

class Helix(val name:String?="MyHelix"):Serializable {

    val secondaryInteractions = HashSet<BasePair>()
    var junctionsLinked = Pair<Junction?, Junction?>(null,null)

    val location:Location
        get() {
            val positionsInHelix = this.secondaryInteractions.map { bp ->  arrayOf(bp.location.start, bp.location.end) }.toTypedArray().flatten()
            return Location(positions = positionsInHelix.toIntArray())
        }

    val length:Int
        get() {
            return this.location.length/2
        }
    val ends:List<Int>
        get() {
            val ends = arrayListOf<Int>()
            if (this.location.blocks.size == 1) {
                val b = this.location.blocks[0]
                ends.add(b.start)
                ends.add(b.start+b.length/2-1)
                ends.add(b.start+b.length/2)
                ends.add(b.end)
            }
            else
                for (b in this.location.blocks) {
                    ends.add(b.start)
                    ends.add(b.end)
                }
            return ends.sorted()
        }

    fun setJunction(junction:Junction) {
        this.junctionsLinked = if (this.junctionsLinked.first == null) this.junctionsLinked.copy(first = junction) else this.junctionsLinked.copy(second = junction)
    }

    fun getPairedPosition(position:Int): Int? {
        for (bp in this.secondaryInteractions) {
            if (bp.location.start == position) {
                return bp.location.end
            }
            if (bp.location.end == position) {
                return bp.location.start
            }
        }
        return null
    }
}

class Junction(val name:String?="MyJunction", val location:Location, val helicesLinked:List<Helix>):Serializable {

    val length:Int
        get() {
            return this.location.length
        }

    val type:JunctionType
        get() {
            return JunctionType.values().first { it.value == this.location.blocks.size }
        }

    init {
        for (h in helicesLinked) {
            h.setJunction(this)
        }
    }

}

class TertiaryStructure(val rna:RNA):Serializable {

    val residues: MutableList<Residue3D> = mutableListOf<Residue3D>()
    var title:String? = null
    var authors:String? = null
    var pubDate:String="To be published"
    var pdbId: String? = null
    var source:String? = null

    fun addResidue3D(absolutePosition: Int): Residue3D? {
        var r: Residue3D? = when(this.rna.getResidue(absolutePosition)) {
            'A' -> Adenine3D(absolutePosition)
            'U' -> Uracil3D(absolutePosition)
            'G' -> Guanine3D(absolutePosition)
            'C' -> Cytosine3D(absolutePosition)
            else -> null
        }
        if (r != null) {
            this.removeResidue3D(absolutePosition)
            residues.add(r)
        }
        return r
    }

    fun removeResidue3D(absolutePosition: Int) {
        for (r in residues)
            if (r.absolutePosition == absolutePosition) {
                residues.remove(r)
                return
            }
    }

    fun getResidue3DAt(position: Int): Residue3D? {
        for (r in residues) if (r.absolutePosition == position) return r
        return null
    }


}

abstract open class Residue3D(val name:String, val absolutePosition:Int):Serializable {

    val atoms: MutableList<Atom> = mutableListOf<Atom>()
    var label:String? = null
    var sugarPucker = 0

    open fun setAtomCoordinates(atomName: String, x: Float, y: Float, z: Float): Atom? {
        var atomName = atomName
        atomName = atomName.replace('*', '\'')
        if (atomName == "OP1" || atomName == "O1P") atomName = RiboNucleotide3D.O1P
        if (atomName == "OP2" || atomName == "O2P") atomName = RiboNucleotide3D.O2P
        if (atomName == "OP3" || atomName == "O3P") atomName = RiboNucleotide3D.O3P
        val a: Atom? = this.getAtom(atomName)
        a?.setCoordinates(x, y, z)
        return a
    }

    fun getAtom(atomName: String): Atom? {
        for (a in atoms) if (a.name == atomName) return a
        return null
    }

}

abstract open class RiboNucleotide3D(name: String, absolutePosition: Int) : Residue3D(name, absolutePosition) {



    override fun setAtomCoordinates(atomName: String, x: Float, y: Float, z: Float): Atom? {
        val a = super.setAtomCoordinates(atomName, x, y, z)
        //TODO each time some Atom coordinates are registered, check if a new TorsionAngle can be calculated
        return a
    }

    protected abstract fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom>

    companion object {
        const val C3ENDO = 0
        const val C2ENDO = 1
        const val C1 = "C1'"
        const val C2 = "C2'"
        const val C3 = "C3'"
        const val C4 = "C4'"
        const val C5 = "C5'"
        const val O2 = "O2'"
        const val O3 = "O3'"
        const val O4 = "O4'"
        const val O5 = "O5'"
        const val O1P = "O1P"
        const val O2P = "O2P"
        const val O3P = "O3P"
        const val O1A = "O1A"
        const val O2A = "O2A"
        const val O3A = "O3A"
        const val O1B = "O1B"
        const val O2B = "O2B"
        const val O3B = "O3B"
        const val O1G = "O1G"
        const val O2G = "O2G"
        const val O3G = "O3G"
        val P = arrayOf("P", "PA", "PB", "PG")
    }

    init {
        sugarPucker = C3ENDO
        for (p in P) atoms.add(Atom(p))
        atoms.add(Atom(O1P))
        atoms.add(Atom(O2P))
        atoms.add(Atom(O3P))
        //if tri-phosphate (i.e. SARS PDB provided with S2S)
        atoms.add(Atom(O1A))
        atoms.add(Atom(O2A))
        atoms.add(Atom(O3A))
        atoms.add(Atom(O1B))
        atoms.add(Atom(O2B))
        atoms.add(Atom(O3B))
        atoms.add(Atom(O1G))
        atoms.add(Atom(O2G))
        atoms.add(Atom(O3G))
        atoms.add(Atom(C1))
        atoms.add(Atom(C2))
        atoms.add(Atom(O2))
        atoms.add(Atom(C3))
        atoms.add(Atom(O3))
        atoms.add(Atom(C4))
        atoms.add(Atom(O4))
        atoms.add(Atom(C5))
        atoms.add(Atom(O5))
        atoms.addAll(getDefaultBaseAtoms(false))
    }
}

class Adenine3D(absolutePosition: Int) : RiboNucleotide3D("A", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
                "N9", 5.671f, -4.305f, 1.390f,
                "C8", 4.358f, -4.673f, 1.330f,
                "N7", 3.565f, -3.717f, 0.950f,
                "C5", 4.410f, -2.640f, 0.750f,
                "N6", 2.967f, -0.828f, 0.050f,
                "C6", 4.189f, -1.313f, 0.340f,
                "N1", 5.256f, -0.506f, 0.240f,
                "C2", 6.465f, -0.989f, 0.540f,
                "N3", 6.800f, -2.209f, 0.930f,
                "C4", 5.707f, -2.984f, 1.010f)
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom = Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates((atoms[i + 1] as Float), (atoms[i + 2] as Float), (atoms[i + 3] as Float))
            ret.add(a)
            i += 4
        }
        return ret
    }
}

class Cytosine3D(absolutePosition: Int) : RiboNucleotide3D("C", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
                "N1", 5.671f, -4.305f, 1.390f,
                "C6", 4.403f, -4.822f, 1.380f,
                "C5", 3.339f, -4.065f, 1.030f,
                "N4", 2.610f, -1.903f, 0.310f,
                "C4", 3.603f, -2.696f, 0.670f,
                "N3", 4.845f, -2.198f, 0.680f,
                "O2", 7.062f, -2.556f, 1.060f,
                "C2", 5.900f, -2.980f, 1.040f
        )
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom = Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates((atoms[i + 1] as Float), (atoms[i + 2] as Float), (atoms[i + 3] as Float))
            ret.add(a)
            i += 4
        }
        return ret
    }
}
class Guanine3D(absolutePosition: Int) : RiboNucleotide3D("G", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
                "N9", 5.671f, -4.305f, 1.390f,
                "C8", 4.338f, -4.651f, 1.320f,
                "N7", 3.550f, -3.676f, 0.940f,
                "C5", 4.420f, -2.604f, 0.740f,
                "O6", 3.067f, -0.759f, 0.040f,
                "C6", 4.148f, -1.276f, 0.330f,
                "N1", 5.325f, -0.513f, 0.260f,
                "N2", 7.579f, -0.093f, 0.420f,
                "C2", 6.597f, -0.986f, 0.550f,
                "N3", 6.848f, -2.225f, 0.940f,
                "C4", 5.712f, -2.974f, 1.010f
        )
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom = Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates((atoms[i + 1] as Float), (atoms[i + 2] as Float), (atoms[i + 3] as Float))
            ret.add(a)
            i += 4
        }
        return ret
    }
}

class Uracil3D(absolutePosition: Int) : RiboNucleotide3D("U", absolutePosition) {
    override fun getDefaultBaseAtoms(withDefaultCoordinates: Boolean): List<Atom> {
        val atoms = arrayOf<Any>(
                "N1", 5.671f, -4.305f, 1.390f,
                "C6", 4.402f, -4.837f, 1.380f,
                "C5", 3.337f, -4.092f, 1.040f,
                "O4", 2.584f, -1.954f, 0.320f,
                "C4", 3.492f, -2.709f, 0.660f,
                "N3", 4.805f, -2.261f, 0.690f,
                "O2", 7.028f, -2.502f, 1.040f,
                "C2", 5.913f, -3.000f, 1.040f
        )
        val ret: MutableList<Atom> = ArrayList()
        var i = 0
        while (i < atoms.size) {
            val a: Atom = Atom(atoms[i] as String)
            if (withDefaultCoordinates) a.setCoordinates((atoms[i + 1] as Float), (atoms[i + 2] as Float), (atoms[i + 3] as Float))
            ret.add(a)
            i += 4
        }
        return ret
    }
}

class Atom(val name:String):Serializable {

    var coordinates: FloatArray? = null
    var x:Float? = null
        get() {
            return this.coordinates!![0]
        }
    var y:Float? = null
        get() {
            return this.coordinates!![1]
        }
    var z:Float? = null
        get() {
            return this.coordinates!![2]
        }

    fun setCoordinates(x: Float, y: Float, z: Float) {
        if (this.coordinates == null) {
            this.coordinates = floatArrayOf(x, y, z)
        } else {
            (this.coordinates as FloatArray)[0] = x
            (this.coordinates as FloatArray)[1] = y
            (this.coordinates as FloatArray)[2] = z
        }
    }

    fun hasCoordinatesFilled() = this.coordinates != null

}

class SecondaryStructure(val rna:RNA, bracketNotation:String? = null, basePairs:List<BasePair>? = null):Serializable {

    val tertiaryInteractions = mutableSetOf<BasePair>()
    val helices = mutableListOf<Helix>()
    val junctions = mutableListOf<Junction>()
    var title:String? = null
    var authors:String? = null
    var pubDate:String="To be published"
    var pdbId: String? = null
    var source:String? = null

    val secondaryInteractions:List<BasePair>
        get() {
            val interactions = mutableListOf<BasePair>()
            for (h in this.helices) {
                interactions.addAll(h.secondaryInteractions)
            }
            return interactions
        }

    val length:Int
        get() {
            return this.rna.seq.length
        }

    init {
        var bps: MutableList<BasePair>

        if (basePairs != null) {
            bps = basePairs.toMutableList()
        } else if (bracketNotation != null) {
            bps = toBasePairs(bracketNotation)
        } else {
            bps = arrayListOf<BasePair>()
        }

        if (!bps.isEmpty()) {
            bps.sortBy { it.location.start }
            val bpInHelix = mutableSetOf<BasePair>()
            BASEPAIRS@ for (i in 0 until bps.size-1) { //for each basepair with gather the successive stacked basepairs
                if (bps[i].edge3 == Edge.SingleHBond || bps[i].edge5 == Edge.SingleHBond) //we can increase the stringency here, like only the canonical
                    continue
                var start1 = bps[i].location.start
                var end1 = bps[i].location.end
                for (h in this.helices) { //if an helix as already the same basepair, we will construct the same helix with less basepairs, so stop
                    for (bb in h.secondaryInteractions) {
                        if (bb.location.start == start1 && bb.location.end == end1)
                            continue@BASEPAIRS
                    }
                }
                var j = i+1
                while (j < bps.size) {
                    val start2 = bps[j].location.start
                    val end2 = bps[j].location.end
                    if (bps[j].edge3 == Edge.SingleHBond || bps[j].edge5 == Edge.SingleHBond) { //we can increase the stringency here, like only the canonical
                        j++
                        continue
                    }
                    if (start1 + 1 == start2 && end1 - 1 == end2) { //if successive basepair with the last one, extension of the current helix
                        bpInHelix.add(bps[i])
                        bpInHelix.add(bps[j])
                        start1 = bps[j].location.start
                        end1 = bps[j].location.end
                    } else if (start2 > start1+1) { //since the base-pairs are sorted, we will never get more succcessive stacked bp. We can restart with the next basepairs in the list
                        if (!bpInHelix.isEmpty()) {
                            val h = Helix()
                            for (bp in bpInHelix) {
                                h.secondaryInteractions.add(bp)
                            }
                            this.helices.add(h)
                            bpInHelix.clear()
                        }
                        continue@BASEPAIRS
                    }
                    j++
                }
                if (!bpInHelix.isEmpty()) {
                    val h = Helix()
                    for (bp in bpInHelix) {
                        h.secondaryInteractions.add(bp)
                    }
                    this.helices.add(h)
                    bpInHelix.clear()
                }
            }
            if (!bpInHelix.isEmpty()) {
                val h = Helix()
                for (bp in bpInHelix) {
                    h.secondaryInteractions.add(bp)
                }
                this.helices.add(h)
                bpInHelix.clear()
            }

            for (i in 0 until this.rna.length) {
                var pknots = mutableListOf<Helix>()
                for (h in this.helices) {
                    if (h.location.contains(i)) {
                        pknots.add(h)
                    }
                }
                val longest = pknots.maxBy { it -> it.length }
                for (h in pknots) {
                    if (h != longest)
                        this.helices.remove(h)
                }
            }

            var pknots = mutableListOf<Helix>()
            I@ for (i in 0 until this.helices.size-1) {
                for (j in i+1 until this.helices.size) {
                    if (this.helices[i].location.start > this.helices[j].location.start && this.helices[i].location.start < this.helices[j].location.end && this.helices[i].location.end > this.helices[j].location.end || this.helices[j].location.start > this.helices[i].location.start && this.helices[j].location.start < this.helices[i].location.end && this.helices[j].location.end > this.helices[i].location.end) {
                        pknots.add(if (this.helices[i].length > this.helices[j].length) this.helices[j] else this.helices[i])
                        continue@I
                    }
                }
            }

            for (pknot in pknots) {
                this.helices.removeAll { it == pknot}
            }

            //now the tertiary interactions
            bps.removeAll {secondaryInteractions.contains(it)}
            for (bp in bps) {
                this.tertiaryInteractions.add(bp)
            }
            //now the junctions
            this.findJunctions()
        }

    }

    /**
    Return the position paired to the position given as argument. Return nil if this position is not paired at all.
     **/
    fun getPairedPosition(position:Int): Int? {
        for (h in this.helices) {
            for (bp in h.secondaryInteractions) {
                if (bp.location.start == position) {
                    return bp.location.end
                }
                if (bp.location.end == position) {
                    return bp.location.start
                }
            }
        }
        for (bp in tertiaryInteractions) {
            if (bp.location.start == position) {
                return bp.location.end
            }
            if (bp.location.end == position) {
                return bp.location.start
            }
        }
        return null
    }

    /**
    Return the next end of an helix (its paired position and the helix itself) after the position given as argument (along the sequence).
    Useful to get the next helix after an helix.
     **/
    fun getNextHelixEnd(position:Int): Triple<Int, Int, Helix>? {
        var minNextEnd = this.length //the next end is the lowest 3' position of an helix right after the position given as argument
        var pairedPosition = -1
        lateinit var helix:Helix

        for (h in this.helices) {
            if (h.ends[0] > position && h.ends[0] < minNextEnd) {
                minNextEnd = h.ends[0]
                pairedPosition = h.ends[3]
                helix = h
            }
            if (h.ends[2] > position && h.ends[2] < minNextEnd) {
                minNextEnd = h.ends[2]
                pairedPosition = h.ends[1]
                helix = h
            }
        }
        if (minNextEnd == length) {
            return null
        }
        return Triple(minNextEnd, pairedPosition, helix)
    }

    fun findJunctions() {
        this.junctions.clear()
        for (h in this.helices) {
            h.junctionsLinked = Pair<Junction?, Junction?>(null,null)
        }
        var positionsInJunction = mutableListOf<Int>()
        var helicesLinked = mutableListOf<Helix>()
        for (h in this.helices) {
            //one side of the helix
            var pos = h.ends[1] //3'-end
            if (this.junctions.filter {it.location.contains(pos)}.isEmpty()) { //already in a junction?
                LOOP@ do {
                    val nextHelix = this.getNextHelixEnd(pos)
                    if (nextHelix != null) {
                        positionsInJunction.addAll(pos..nextHelix.first)
                        helicesLinked.add(nextHelix.third)
                        pos = nextHelix.second
                    } else { //not a junction
                        positionsInJunction = mutableListOf<Int>()
                        helicesLinked = mutableListOf<Helix>()
                        break@LOOP
                    }
                } while (pos != h.ends[1])

                if (!positionsInJunction.isEmpty())
                    this.junctions.add(Junction(location=Location(positions=positionsInJunction.toIntArray()), helicesLinked=helicesLinked))
            }

            //the other side (of the river ;-) )
            positionsInJunction = mutableListOf<Int>()
            helicesLinked = mutableListOf<Helix>()
            pos = h.ends[3] //3'-end
            if (this.junctions.filter {it.location.contains(pos)}.isEmpty()) { //already in a junction?
                LOOP@ do {
                    val nextHelix = this.getNextHelixEnd(pos)
                    if (nextHelix != null) {
                        positionsInJunction.addAll(pos..nextHelix.first)
                        helicesLinked.add(nextHelix.third)
                        pos = nextHelix.second
                    } else { //not a junction
                        positionsInJunction = mutableListOf<Int>()
                        helicesLinked = mutableListOf<Helix>()
                        break@LOOP
                    }
                } while (pos != h.ends[3])

                if (!positionsInJunction.isEmpty())
                    this.junctions.add(Junction(location=Location(positions=positionsInJunction.toIntArray()), helicesLinked=helicesLinked))
            }
        }
    }

    fun toBracketNotation(): String {
        val bn = CharArray(this.rna.length)
        for (i in 0 until this.rna.length) bn[i] = '.'
        for (helix in this.helices) {
            for (bp in helix.secondaryInteractions) {
                bn[bp.location.start - 1] = '('
                bn[bp.location.end - 1] = ')'
            }
        }
        for (bp in this.tertiaryInteractions) {
            bn[bp.location.start - 1] = '('
            bn[bp.location.end - 1] = ')'
        }
        return String(bn)
    }

}

fun toBlocks(positions:IntArray):MutableList<Block> {
    val blocks = arrayListOf<Block>()
    val sortedPositions = positions.sorted()
    var length = 0
    var i = 0
    var start = sortedPositions.first()

    while (i < sortedPositions.size-1) {
        if (sortedPositions[i]+1 == sortedPositions[i+1]) {
            length += 1
        } else {
            blocks.add(Block(start, start+length))
            length = 0
            start = sortedPositions[i+1]
        }
        i += 1
    }
    blocks.add(Block(start, start+length))
    return blocks
}

fun toBasePairs(bracketNotation: String):MutableList<BasePair> {
    val basePairs = arrayListOf<BasePair>()
    var pos = 0
    val lastPos = arrayListOf<Int>()
    val lastLeft = arrayListOf<Edge>()
    loop@ for (c in bracketNotation) {
        pos++
        when (c) {
            '(' -> {lastLeft.add(Edge.WC) && lastPos.add(pos)}
            '{' -> lastLeft.add(Edge.Sugar) && lastPos.add(pos)
            '[' -> lastLeft.add(Edge.Hoogsteen) && lastPos.add(pos)
            ')' -> {
                val _lastPos = lastPos.removeAt(lastPos.size-1)
                val _location =  Location(_lastPos, pos)
                val _lastLeft = lastLeft.removeAt(lastLeft.size-1)
                basePairs.add(BasePair(location = _location, edge5 = _lastLeft, edge3 = Edge.WC))
            }
            '}' -> {
                val _lastPos = lastPos.removeAt(lastPos.size-1)
                val _location =  Location(_lastPos, pos)
                val _lastLeft = lastLeft.removeAt(lastLeft.size-1)
                basePairs.add(BasePair(location = _location, edge5 = _lastLeft, edge3 = Edge.Sugar))
            }
            ']' -> {
                val _lastPos = lastPos.removeAt(lastPos.size-1)
                val _location =  Location(_lastPos, pos)
                val _lastLeft = lastLeft.removeAt(lastLeft.size-1)
                basePairs.add(BasePair(location = _location, edge5 = _lastLeft, edge3 = Edge.Hoogsteen))
            }
            else -> continue@loop
        }
    }
    return basePairs
}

fun randomRNA(size:Int):RNA {
    val residues = listOf<Char>('A','U','G','C')
    val seq = (1..size)
            .map { i -> kotlin.random.Random.nextInt(0, residues.size) }
            .map(residues::get)
            .joinToString("")
    return RNA("random rna", seq)
}

enum class Edge {
    WC, Hoogsteen, Sugar, SingleHBond, Unknown;
}

enum class Orientation {
    cis, trans, Unknown
}

enum class JunctionType(val value:Int) {
    ApicalLoop(1),
    InnerLoop(2),
    ThreeWay(3),
    FourWay(4),
    FiveWay(5),
    SixWay(6),
    SevenWay(7),
    EightWay(8),
    NineWay(9),
    TenWay(10),
    ElevenWay(11),
    TwelveWay(12),
    ThirteenWay(13),
    FourteenWay(14),
    FiveteenWay(15),
    SixteenWay(16),
    Flower(17)
}

val modifiedNucleotides: MutableMap<String, String> = mutableMapOf<String, String>(
    "T" to "U",
    "PSU" to "U",
    "I" to "A",
    "+A" to "A",
    "+C" to "C",
    "+G" to "G",
    "+I" to "A",
    "+T" to "U",
    "+U" to "U",
    "PU" to "A",
    "YG" to "G",
    "1AP" to "G",
    "1MA" to "A",
    "1MG" to "G",
    "2DA" to "A",
    "2DT" to "U",
    "2MA" to "A",
    "2MG" to "G",
    "4SC" to "C",
    "4SU" to "U",
    "5IU" to "U",
    "5MC" to "C",
    "5MU" to "U",
    "5NC" to "C",
    "6MP" to "A",
    "7MG" to "G",
    "A23" to "A",
    "AD2" to "A",
    "AET" to "A",
    "AMD" to "A",
    "AMP" to "A",
    "APN" to "A",
    "ATP" to "A",
    "AZT" to "U",
    "CCC" to "C",
    "CMP" to "A",
    "CPN" to "C",
    "DAD" to "A",
    "DCT" to "C",
    "DDG" to "G",
    "DG3" to "G",
    "DHU" to "U",
    "DOC" to "C",
    "EDA" to "A",
    "G7M" to "G",
    "GDP" to "G",
    "GNP" to "G",
    "GPN" to "G",
    "GTP" to "G",
    "GUN" to "G",
    "H2U" to "U",
    "HPA" to "A",
    "IPN" to "U",
    "M2G" to "G",
    "MGT" to "G",
    "MIA" to "A",
    "OMC" to "C",
    "OMG" to "G",
    "OMU" to "U",
    "ONE" to "U",
    "P2U" to "U",
    "PGP" to "G",
    "PPU" to "A",
    "PRN" to "A",
    "PST" to "U",
    "QSI" to "A",
    "QUO" to "G",
    "RIA" to "A",
    "SAH" to "A",
    "SAM" to "A",
    "T23" to "U",
    "T6A" to "A",
    "TAF" to "U",
    "TLC" to "U",
    "TPN" to "U",
    "TSP" to "U",
    "TTP" to "U",
    "UCP" to "U",
    "VAA" to "A",
    "YYG" to "G",
    "70U" to "U",
    "12A" to "A",
    "2MU" to "U",
    "127" to "U",
    "125" to "U",
    "126" to "U",
    "MEP" to "U",
    "TLN" to "U",
    "ADP" to "A",
    "TTE" to "U",
    "PYO" to "U",
    "SUR" to "U",
    "PSD" to "A",
    "S4U" to "U",
    "CP1" to "C",
    "TP1" to "U",
    "NEA" to "A",
    "GCK" to "C",
    "CH" to "C",
    "EDC" to "G",
    "DFC" to "C",
    "DFG" to "G",
    "DRT" to "U",
    "2AR" to "A",
    "8OG" to "G",
    "IG" to "G",
    "IC" to "C",
    "IGU" to "G",
    "IMC" to "C",
    "GAO" to "G",
    "UAR" to "U",
    "CAR" to "C",
    "PPZ" to "A",
    "M1G" to "G",
    "ABR" to "A",
    "ABS" to "A",
    "S6G" to "G",
    "HEU" to "U",
    "P" to "G",
    "DNR" to "C",
    "MCY" to "C",
    "TCP" to "U",
    "LGP" to "G",
    "GSR" to "G",
    "E" to "A",
    "GSS" to "G",
    "THX" to "U",
    "6CT" to "U",
    "TEP" to "G",
    "GN7" to "G",
    "FAG" to "G",
    "PDU" to "U",
    "MA6" to "A",
    "UMP" to "U",
    "SC" to "C",
    "GS" to "G",
    "TS" to "U",
    "AS" to "A",
    "ATD" to "U",
    "T3P" to "U",
    "5AT" to "U",
    "MMT" to "U",
    "SRA" to "A",
    "6HG" to "G",
    "6HC" to "C",
    "6HT" to "U",
    "6HA" to "A",
    "55C" to "C",
    "U8U" to "U",
    "BRO" to "U",
    "BRU" to "U",
    "5IT" to "U",
    "ADI" to "A",
    "5CM" to "C",
    "IMP" to "G",
    "THM" to "U",
    "URI" to "U",
    "AMO" to "A",
    "FHU" to "U",
    "TSB" to "A",
    "CMR" to "C",
    "RMP" to "A",
    "SMP" to "A",
    "5HT" to "U",
    "RT" to "U",
    "MAD" to "A",
    "OXG" to "G",
    "UDP" to "U",
    "6MA" to "A",
    "5IC" to "C",
    "SPT" to "U",
    "TGP" to "G",
    "BLS" to "A",
    "64T" to "U",
    "CB2" to "C",
    "DCP" to "C",
    "ANG" to "G",
    "BRG" to "G",
    "Z" to "A",
    "AVC" to "A",
    "5CG" to "G",
    "UDP" to "U",
    "UMS" to "U",
    "BGM" to "G",
    "SMT" to "U",
    "DU" to "U",
    "CH1" to "C",
    "GH3" to "G",
    "GNG" to "G",
    "TFT" to "U",
    "U3H" to "U",
    "MRG" to "G",
    "ATM" to "U",
    "GOM" to "A",
    "UBB" to "U",
    "A66" to "A",
    "T66" to "U",
    "C66" to "C",
    "3ME" to "A",
    "A3P" to "A",
    "ANP" to "A",
    "FA2" to "A",
    "9DG" to "G",
    "GMU" to "U",
    "UTP" to "U",
    "5BU" to "U",
    "APC" to "A",
    "DI" to "A",
    "UR3" to "U",
    "3DA" to "A",
    "DDY" to "C",
    "TTD" to "U",
    "TFO" to "U",
    "TNV" to "U",
    "MTU" to "U",
    "6OG" to "G",
    "E1X" to "A",
    "FOX" to "A",
    "CTP" to "C",
    "D3T" to "U",
    "TPC" to "C",
    "7DA" to "A",
    "7GU" to "U",
    "2PR" to "A",
    "CBR" to "C",
    "I5C" to "C",
    "5FC" to "C",
    "GMS" to "G",
    "2BT" to "U",
    "8FG" to "G",
    "MNU" to "U",
    "AGS" to "A",
    "NMT" to "U",
    "NMS" to "U",
    "UPG" to "U",
    "G2P" to "G",
    "2NT" to "U",
    "EIT" to "U",
    "TFE" to "U",
    "P2T" to "U",
    "2AT" to "U",
    "2GT" to "U",
    "2OT" to "U",
    "BOE" to "U",
    "SFG" to "G",
    "CSL" to "A",
    "PPW" to "G",
    "IU" to "U",
    "D5M" to "A",
    "ZDU" to "U",
    "DGT" to "U",
    "UD5" to "U",
    "S4C" to "C",
    "DTP" to "A",
    "5AA" to "A",
    "2OP" to "A",
    "PO2" to "A",
    "DC" to "C",
    "DA" to "A",
    "LOF" to "A",
    "ACA" to "A",
    "BTN" to "A",
    "PAE" to "A",
    "SPS" to "A",
    "TSE" to "A",
    "A2M" to "A",
    "NCO" to "A",
    "A5M" to "C",
    "M5M" to "C",
    "S2M" to "U",
    "MSP" to "A",
    "P1P" to "A",
    "N6G" to "G",
    "MA7" to "A",
    "FE2" to "G",
    "AKG" to "G",
    "SIN" to "G",
    "PR5" to "G",
    "GOL" to "G",
    "XCY" to "G",
    "5HU" to "U",
    "CME" to "C",
    "EGL" to "G",
    "LC" to "C",
    "LHU" to "U",
    "LG" to "G",
    "PUY" to "U",
    "PO4" to "U",
    "PQ1" to "U",
    "ROB" to "U",
    "O2C" to "C",
    "C30" to "C",
    "C31" to "C",
    "C32" to "C",
    "C33" to "C",
    "C34" to "C",
    "C35" to "C",
    "C36" to "C",
    "C37" to "C",
    "C38" to "C",
    "C39" to "C",
    "C40" to "C",
    "C41" to "C",
    "C42" to "C",
    "C43" to "C",
    "C44" to "C",
    "C45" to "C",
    "C46" to "C",
    "C47" to "C",
    "C48" to "C",
    "C49" to "C",
    "C50" to "C",
    "A30" to "A",
    "A31" to "A",
    "A32" to "A",
    "A33" to "A",
    "A34" to "A",
    "A35" to "A",
    "A36" to "A",
    "A37" to "A",
    "A38" to "A",
    "A39" to "A",
    "A40" to "A",
    "A41" to "A",
    "A42" to "A",
    "A43" to "A",
    "A44" to "A",
    "A45" to "A",
    "A46" to "A",
    "A47" to "A",
    "A48" to "A",
    "A49" to "A",
    "A50" to "A",
    "G30" to "G",
    "G31" to "G",
    "G32" to "G",
    "G33" to "G",
    "G34" to "G",
    "G35" to "G",
    "G36" to "G",
    "G37" to "G",
    "G38" to "G",
    "G39" to "G",
    "G40" to "G",
    "G41" to "G",
    "G42" to "G",
    "G43" to "G",
    "G44" to "G",
    "G45" to "G",
    "G46" to "G",
    "G47" to "G",
    "G48" to "G",
    "G49" to "G",
    "G50" to "G",
    "T30" to "U",
    "T31" to "U",
    "T32" to "U",
    "T33" to "U",
    "T34" to "U",
    "T35" to "U",
    "T36" to "U",
    "T37" to "U",
    "T38" to "U",
    "T39" to "U",
    "T40" to "U",
    "T41" to "U",
    "T42" to "U",
    "T43" to "U",
    "T44" to "U",
    "T45" to "U",
    "T46" to "U",
    "T47" to "U",
    "T48" to "U",
    "T49" to "U",
    "T50" to "U",
    "U30" to "U",
    "U31" to "U",
    "U32" to "U",
    "U33" to "U",
    "U34" to "U",
    "U35" to "U",
    "U36" to "U",
    "U37" to "U",
    "U38" to "U",
    "U39" to "U",
    "U40" to "U",
    "U41" to "U",
    "U42" to "U",
    "U43" to "U",
    "U44" to "U",
    "U45" to "U",
    "U46" to "U",
    "U47" to "U",
    "U48" to "U",
    "U49" to "U",
    "U50" to "U",
    "UFP" to "U",
    "UFR" to "U",
    "UCL" to "U",
    "3DR" to "U",
    "CBV" to "C",
    "HFA" to "A",
    "MMA" to "A",
    "DCZ" to "C",
    "GNE" to "C",
    "A1P" to "A",
    "6IA" to "A",
    "CTG" to "G",
    "5FU" to "U",
    "2AD" to "A",
    "T2T" to "U",
    "XUG" to "G",
    "2ST" to "U",
    "5PY" to "U",
    "4PC" to "C",
    "US1" to "U",
    "M5C" to "C",
    "DG" to "G",
    "DA" to "A",
    "DT" to "U",
    "DC" to "C",
    "P5P" to "A",
    "FMU" to "U",
    "YMP" to "A",
    "RTP" to "G",
    "TM2" to "U",
    "SSU" to "U",
    "N5M" to "C",
    "N5C" to "C",
    "8AN" to "A",
    "3AT" to "A",
    "DM1" to "X",
    "DM2" to "X",
    "DM5" to "X"
)

fun main() {
    NDB().listPDBFileNames()
}
