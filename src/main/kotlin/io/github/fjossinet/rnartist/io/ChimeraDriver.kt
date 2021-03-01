package io.github.fjossinet.rnartist.io.github.fjossinet.rnartist.io

import io.github.fjossinet.rnartist.Mediator
import io.github.fjossinet.rnartist.core.model.Location
import io.github.fjossinet.rnartist.core.model.Residue3D
import io.github.fjossinet.rnartist.core.model.ResidueDrawing
import io.github.fjossinet.rnartist.core.model.RnartistConfig.chimeraHost
import io.github.fjossinet.rnartist.core.model.RnartistConfig.chimeraPath
import io.github.fjossinet.rnartist.core.model.RnartistConfig.chimeraPort
import io.github.fjossinet.rnartist.core.model.TertiaryStructure
import io.github.fjossinet.rnartist.core.model.io.copyFile
import io.github.fjossinet.rnartist.core.model.io.createTemporaryFile
import io.github.fjossinet.rnartist.core.model.io.parsePDB
import io.github.fjossinet.rnartist.io.*
import javafx.scene.control.Alert
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import javax.swing.SwingWorker


class ChimeraDriver(mediator:Mediator): AbstractTertiaryViewerDriver(mediator, chimeraPath) {

    enum class RENDERING {
        STICK, CARTOON, SHOW_RIBBON, HIDE_RIBBON
    }

    //the URL constructed from the host and port defined in the RNArtist settings
    var url: URL? = null

    //the pdbFile is given to Chimera to load and display the 3D structure
    var pdbFile: File? = null

    //the pdbFile is given to Chimera to load and display the 3D structure
    private var sessionFile: File? = null

    //the tertiary structures are constructed from the parsing of the PDB file. They're mainly used to get the numbering system for each molecular chain when some residues are selected in the Canvas2D.
    var tertiaryStructures: MutableList<TertiaryStructure>


    init {
        tertiaryStructures = ArrayList()
        try {
            url = URL("http://" + chimeraHost + ":" + chimeraPort + "/run")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    fun connectToExecutable() {
        try {
            this.run(arrayOf("--start", "ReadStdin"), null, null)
            url = null
        } catch (e: IOException) {
            if (e.message!!.startsWith("Cannot run program")) {
                val alert = Alert(Alert.AlertType.WARNING)
                alert.title = "Problem with Chimera"
                alert.headerText = "Cannot Run the Chimera Program!"
                alert.contentText = "Check your Chimera Path in Settings"
                alert.showAndWait()
            }
        }
    }

    @Throws(MalformedURLException::class)
    fun connectToRestServer() {
        url = URL("http://" + chimeraHost + ":" + chimeraPort + "/run")
        showVersion()
        this.process = null
    }

    fun showVersion() {
        postCommand("version")
    }

    /**
     * The command is either sent to the process or to the REST server
     * @param command
     * @return
     */
    private fun postCommand(command: String): String? {
        if (url == null && this.process == null) return null
        try {
            object : SwingWorker<Any?, Any?>() {
                @Throws(Exception::class)
                override fun doInBackground(): Any? {
                    if (url != null) {
                        val data = URLEncoder.encode("command", "UTF-8") + "=" + URLEncoder.encode(command, "UTF-8")
                        val conn = url!!.openConnection()
                        conn.doOutput = true
                        val wr = OutputStreamWriter(conn.getOutputStream())
                        wr.write(data)
                        wr.flush()
                        val result = StringBuffer()
                        //Get the response
                        val rd = BufferedReader(InputStreamReader(conn.getInputStream()))
                        var line: String
                        while (rd.readLine().also { line = it } != null) result.append("""
    $line
    
    """.trimIndent())
                        wr.close()
                        rd.close()
                        //return result.toString();
                    } else evaluate(command)
                    return null
                }
            }.execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun closeSession() {
        pdbFile = null
        sessionFile = null
        tertiaryStructures!!.clear()
        postCommand("close session")
    }

    override fun restoreSession(sessionFile: File, pdbFile: File) {
        try {
            postCommand("open " + sessionFile.absolutePath)
            this.sessionFile = sessionFile
            this.pdbFile = pdbFile
            tertiaryStructures!!.addAll(parsePDB(FileReader(pdbFile)))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun loadTertiaryStructure(f: File) {
        try {
            pdbFile = f
            tertiaryStructures!!.addAll(parsePDB(FileReader(f)))
            postCommand("open " + f.absolutePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    fun loadTertiaryStructure(f: File, layer: Int) {
        try {
            pdbFile = f
            tertiaryStructures!!.addAll(parsePDB(FileReader(f)))
            postCommand("close $layer")
            postCommand("open " + layer + " " + f.absolutePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    fun reloadTertiaryStructure() {
        sessionFile?.let {
            if (it.exists())
                postCommand("open " + it.absolutePath)
        } ?: run {
            pdbFile?.let {
                if (it.exists())
                    postCommand("open " + it.absolutePath)
            }
        }
    }

    override fun close() {
        postCommand("stop noask")
    }

    @Throws(IOException::class)
    override fun saveSession(sessionFile: File, pdbFile: File) {
        if (!tertiaryStructures!!.isEmpty() && this.pdbFile != null) {
            postCommand("save " + sessionFile.absolutePath)
            if (this.pdbFile!!.absolutePath != pdbFile.absolutePath) //to avoid to clash with the same fiel id the project is updated (then no need to copy the file, it is already there
                copyFile(this.pdbFile!!, pdbFile)
        }
    }

    fun represent(mode: RENDERING, positions: List<Int>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures!!) {
                if (ts.rna.name == chainName) {
                    numberingSystem = ts.getNumberingSystem()
                    break
                }
            }
            if (numberingSystem != null) {
                val atomsSpec = StringBuffer()
                if (!positions.isEmpty()) {
                    atomsSpec.append("#0:")
                    for (pos in positions) atomsSpec.append(numberingSystem[pos - 1] + "." + chainName + ",")
                    atomsSpec.deleteCharAt(atomsSpec.length - 1)
                }
                when (mode) {
                    RENDERING.CARTOON -> {
                        val command = StringBuffer("nucleotides side tube/slab $atomsSpec")
                        postCommand(command.toString())
                    }
                    RENDERING.STICK -> {
                        val command = StringBuffer("nucleotides sidechain atoms $atomsSpec")
                        postCommand(command.toString())
                        println(command.toString())
                    }
                    RENDERING.SHOW_RIBBON -> {
                        val command = StringBuffer("ribbon $atomsSpec")
                        postCommand(command.toString())
                        println(command.toString())
                    }
                    RENDERING.HIDE_RIBBON -> {
                        val command = StringBuffer("~ribbon $atomsSpec")
                        postCommand(command.toString())
                        println(command.toString())
                    }
                }
            }
        }
    }

    fun selectResidues(positions: List<Int>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures!!) {
                if (ts.rna.name == chainName) {
                    numberingSystem = ts.getNumberingSystem()
                    break
                }
            }
            if (numberingSystem != null) {
                val command =
                    StringBuffer("select #0,2:") //the #2 is to select residues for the refined structure (if any)
                for (pos in positions) command.append(numberingSystem[pos - 1] + "." + chainName + ",")
                postCommand(command.substring(0, command.length - 1))
            }
        }
    }

    fun selectResidues(positions: List<Int>, layer: Int) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures!!) if (ts.rna.name == chainName) {
                numberingSystem = ts.getNumberingSystem()
                break
            }
            if (numberingSystem != null) {
                val command = StringBuffer("select #$layer:")
                for (pos in positions) command.append(numberingSystem[pos - 1] + ",")
                postCommand(command.substring(0, command.length - 1))
            }
        }
    }

    fun showResidues(positions: List<String>) {
        val command = StringBuffer("show #0,2:") //the #2 is to select residues for the refined structure (if any)
        for (pos in positions) command.append("$pos,")
        postCommand(command.substring(0, command.length - 1))
    }

    override fun selectionCleared() {
        postCommand("~select")
    }

    fun color3D(residues: List<ResidueDrawing>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures!!) if (ts.rna.name == chainName) {
                numberingSystem = ts.getNumberingSystem()
                break
            }
            if (numberingSystem != null) {
                val command = StringBuffer()
                for (r in residues) command.append("color " + r.getColor().red.toFloat() / 255.toFloat() + "," + r.getColor().green
                    .toFloat() / 255.toFloat() + "," + r.getColor().blue.toFloat() / 255.toFloat() + "," + r.getColor().alpha
                    .toFloat() / 255.toFloat() + " #0,2:" + numberingSystem[r.absPos - 1] + "." + chainName + "; ")
                postCommand(command.toString())
            }
        }
    }

    fun setPivot(positions: List<Int>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures) if (ts.rna.name == chainName) {
                numberingSystem = ts.getNumberingSystem()
                break
            }
            val command = StringBuffer("cofr #0:")
            for (pos in positions) command.append(numberingSystem!![pos - 1] + "." + chainName + ",")
            postCommand(command.substring(0, command.length - 1))
        }
    }

    fun setFocus(positions: List<Int>) {
        mediator.drawingDisplayed.get()?.drawing?.let { drawing ->
            val chainName: String = drawing.secondaryStructure.rna.name
            var numberingSystem: List<String>? = null
            for (ts in tertiaryStructures) if (ts.rna.name == chainName) {
                numberingSystem = ts.getNumberingSystem()
                break
            }
            val command = StringBuffer("focus #0:")
            for (pos in positions) command.append(numberingSystem!![pos - 1] + "." + chainName + ",")
            postCommand(command.substring(0, command.length - 1))
        }
    }

    //+++++++ Methods used for 3D modeling (not available with RNArtist at now)

    //+++++++ Methods used for 3D modeling (not available with RNArtist at now)
    @Throws(Exception::class)
    fun importStructure(modelID: Int, relativeID: Int) {
        /*val f = createTemporaryFile("export.pdb")
        val commandInput = PrintWriter(OutputStreamWriter(BufferedOutputStream(process.getOutputStream())), true)
        if (relativeID != -1) commandInput.println("write relative " + relativeID + " format pdb " + modelID + " " + f.absolutePath) else commandInput.println(
            "write format pdb " + modelID + " " + f.absolutePath)
        Thread.sleep(3000)
        mediator.getAssemble().loadTertiaryStructures(FileParser.parsePDB(mediator, new FileReader(f)));*/
    }

    @Throws(Exception::class)
    fun synchronizeFrom() {
        /*val f = createTemporaryFile("export.pdb")
        val commandInput = PrintWriter(OutputStreamWriter(BufferedOutputStream(process.getOutputStream())), true)
        commandInput.println("write relative 0 format pdb 0 " + f.absolutePath)
        Thread.sleep(3000)
        //List<TertiaryStructure> structures = FileParser.parsePDB(mediator, new FileReader(f));
        val newTS = TertiaryStructure(mediator.rna)
        if (mediator.getTertiaryStructure() != null)
            newTS.setPdbId(mediator.getTertiaryStructure().getPdbId());
        mediator.setTertiaryStructure(newTS);
        for (TertiaryStructure ts:structures)
            for (Residue3D residue3D:ts.getResidues()) {
                Residue3D _residue3D = newTS.addResidue3D(Integer.parseInt(residue3D.getLabel())); //IMPORTANT HERE!!!! CHIMERA HAS TO USE THE LABEL OF THE RESIDUE AS ITS ABSOLUTE POSITION.
                for (Atom a: residue3D.getAtoms())
                    if (a.hasCoordinatesFilled())
                        _residue3D.setAtomCoordinates(a.getName(),a.getX(),a.getY(),a.getZ());
            }
        mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Synchronization done!!", null, null);
        mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();*/
    }

    fun loadRefinedModel(f: File) {
        postCommand("close 2")
        postCommand("open 2 " + f.absolutePath)
    }

    override fun removeSelection(positions: List<Int>) {
        for (pos in positions) postCommand("delete #0:$pos")
    }

    @Throws(Exception::class)
    fun synchronizeTo() {
        val f = createTemporaryFile("model.pdb")
        //writePDB(mediator.getTertiaryStructure(), true, new PrintWriter(f));
        postCommand("open 1 " + f.absolutePath)
        postCommand("delete #0")
        postCommand("combine #0,1 modelId 0 name model close true")
    }

    override fun eraseModel() {
        postCommand("close 0")
    }

    override fun addResidue(f: File, position: Int, anchorResidue: Int) {
        postCommand("open " + f.absolutePath)
        postCommand("match #1:$anchorResidue #0:$anchorResidue")
        postCommand("delete #1:$anchorResidue")
        postCommand("combine #0,1 modelId 0 name model close true")
        //this.synchronizeFrom(false); //we need to to that since Chimera has oved atoms to do the match
    }

   override fun addInteraction(f: File, interaction: Location) {
        postCommand("open " + f.absolutePath)
        postCommand("match #1:" + interaction.start + "," + interaction.end + " #0:" + interaction.start + "," + interaction.end)
        postCommand("delete #0:" + interaction.start) //the old interaction is removed
        postCommand("delete #0:" + interaction.end)
        postCommand("combine #0,1 modelId 0 name model close true")
        //this.synchronizeFrom(false); //we need to to that since Chimera has oved atoms to do the match
    }

    /*public void addFragment(File f, List<Residue3D> residues, int anchorResidue1, int anchorResidue2, boolean firstFragment) {
        this.postCommand("open " + f.getAbsolutePath());
        if (anchorResidue1 != -1 && anchorResidue2 != -1)
            this.postCommand("match #1:"+anchorResidue1+","+anchorResidue2+" #0:"+anchorResidue1+","+anchorResidue2);
        else if (anchorResidue1 != -1) {
            this.postCommand("match #1:"+anchorResidue1+" #0:"+anchorResidue1);
        }
        else if (anchorResidue2 != -1)
            this.postCommand("match #1:"+anchorResidue2+" #0:"+anchorResidue2);
        if (!firstFragment) {
            for (Residue3D r:residues)
                if (r.getAbsolutePosition() != anchorResidue1 && r.getAbsolutePosition() != anchorResidue2)
                    this.postCommand("delete #0:"+r.getAbsolutePosition());

            if (anchorResidue1 != -1)
                this.postCommand("delete #1:"+anchorResidue1);
            if (anchorResidue2 != -1)
                this.postCommand("delete #1:"+anchorResidue2);
        }
        this.postCommand("combine #0,1 modelId 0 name model close true");
        List<String> positions = new ArrayList<String>();
        for (Residue3D residue3D:residues)
            positions.add(residue3D.getLabel());
        if (anchorResidue1 != -1) //we are not selecting the anchor residues to more easily remove the fragment just added without to remove the anchor residues
            positions.remove(""+anchorResidue1);
        if (anchorResidue2 != -1) //we are not selecting the anchor residues to more easily remove the fragment just added without to remove the anchor residues
            positions.remove(""+anchorResidue2);
        this.selectResidues(positions);
    }

    public void substituteBaseBaseInteraction(File f, List<Residue3D> residues) {
        this.postCommand("open " + f.getAbsolutePath());
        this.postCommand("match #1:"+residues.get(0).getAbsolutePosition()+","+residues.get(1).getAbsolutePosition()+" #0:"+residues.get(0).getAbsolutePosition()+","+residues.get(1).getAbsolutePosition());
        for (Residue3D r:residues)
            this.postCommand("delete #0:"+r.getAbsolutePosition());
        this.postCommand("combine #0,1 modelId 0 name model close true");
        List<String> positions = new ArrayList<String>();
        for (Residue3D residue3D:residues)
            positions.add(residue3D.getLabel());
        this.selectResidues(positions);
    }

    public void addFragment(File f, List<Residue3D> computedResidues, List<Residue3D> previousResidues, boolean firstFragment) {
        //We first search for residues already in the 3D scene
        int anchorResidue1 = -1, anchorResidue2 = -1;
        List<Residue3D> toBedeleted = new ArrayList<Residue3D>();
        for (Residue3D r: computedResidues) {
            for (Residue3D already: previousResidues) {
                if (r.getAbsolutePosition() == already.getAbsolutePosition())
                    toBedeleted.add(already);
            }
        }

        if (toBedeleted.size() == 1)
            anchorResidue1 = toBedeleted.get(0).getAbsolutePosition();
        if (toBedeleted.size() == 2 && mediator.getCanvas2D().getSecondaryStructureDrawing().getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(toBedeleted.get(0).getAbsolutePosition())).getAbsolutePosition() == toBedeleted.get(1).getAbsolutePosition()) {
            anchorResidue1 = toBedeleted.get(0).getAbsolutePosition();
            anchorResidue2 = toBedeleted.get(1).getAbsolutePosition();
        }
        else {
            List<Integer> positions = new ArrayList<Integer>();
            for (Residue3D r: toBedeleted)
                positions.add(r.getAbsolutePosition());
            anchorResidue1 = (Integer) JOptionPane.showInputDialog(null, "Choose the anchor position", "Choose the anchor position", JOptionPane.PLAIN_MESSAGE, null, positions.toArray(new Integer[]{}), positions.get(0));
        }

        this.postCommand("open " + f.getAbsolutePath());
        if (anchorResidue1 != -1 && anchorResidue2 != -1)
            this.postCommand("match #1:"+anchorResidue1+","+anchorResidue2+" #0:"+anchorResidue1+","+anchorResidue2);
        else if (anchorResidue1 != -1) {
            this.postCommand("match #1:"+anchorResidue1+" #0:"+anchorResidue1);
        }
        else if (anchorResidue2 != -1)
            this.postCommand("match #1:"+anchorResidue2+" #0:"+anchorResidue2);
        if (!firstFragment) {
            for (Residue3D r:toBedeleted)
                this.postCommand("delete #0:"+r.getAbsolutePosition());
        }
        this.postCommand("combine #0,1 modelId 0 name model close true");
        List<String> positions = new ArrayList<String>();
        for (Residue3D residue3D:computedResidues)
            positions.add(residue3D.getLabel());
        if (anchorResidue1 != -1) //we are not selecting the anchor residues to more easily remove the fragment just added without to remove the anchor residues
            positions.remove(""+anchorResidue1);
        if (anchorResidue2 != -1) //we are not selecting the anchor residues to more easily remove the fragment just added without to remove the anchor residues
            positions.remove(""+anchorResidue2);
        this.selectResidues(positions);
    }*/
}

interface Driver {
    /**
     * Launch the program
     *
     * @param options    options to give to the program at start (at least, an empty array of String)
     * @param inputFile  input file for the program (can be null)
     * @param outputFile output file for the program (can be null)
     * @return the program's output as a File object
     */
    @Throws(IOException::class)
    fun run(options: Array<String>, inputFile: File?, outputFile: File?)

    /**
     * Close the external application
     */
    fun close()

    /**
     * Send a message to the running external program
     */
    fun evaluate(message: String)
}

abstract class AbstractDriver protected constructor(
    protected var mediator: Mediator,
    /**
     * The external program's fuall path+name+some options
     */
    protected var program: String,
) : Driver {
    /**
     * Creates a new Driver for an external program defined with its name, full path and options
     *
     * @param program the program's name and its full path. Some options can also be precised at this level.
     */
    init {
        program = program
    }
}

/**
 * A driver for an application staying alive until an explicit close message.
 * This Driver is designed to launch and communicate with a graphical application for example
 */
abstract class OnHangApplicationDriver protected constructor(mediator: Mediator, program: String) :
    AbstractDriver(mediator, program) {
    protected var process: Process? = null

    @Throws(IOException::class)
    override fun run(options: Array<String>, inputFile: File?, outputFile: File?) {
        val command = arrayOfNulls<String>(options.size + 1 /*1 is for the command name*/)
        command[0] = program
        var i = 0
        while (i < options.size) {
            command[i + 1] = options[i]
            i++
        }
        val b = StringBuffer(command[0])
        for (k in 1 until command.size) {
            b.append(" ")
            b.append(command[k])
        }
        val pb = ProcessBuilder(*command)
        pb.redirectErrorStream(true)
        process = pb.start()
    }

    override fun evaluate(command: String) {
        if (process != null) {
            object : org.jdesktop.swingworker.SwingWorker<Any?, Any?>() {
                private var commandInput: PrintWriter? = null

                @Throws(java.lang.Exception::class)
                override fun doInBackground(): Any? {
                    commandInput = PrintWriter(OutputStreamWriter(BufferedOutputStream(process!!.outputStream)), true)
                    commandInput!!.println(command)
                    return null
                }
            }.execute()
            object : org.jdesktop.swingworker.SwingWorker<Any?, Any?>() {
                private var commandOutput: BufferedReader? = null
                @Throws(java.lang.Exception::class)
                public override fun doInBackground(): Any? {
                    commandOutput = BufferedReader(InputStreamReader(process!!.inputStream))
                    try {
                        commandOutput!!.readLine()
                        commandOutput!!.reset()
                    } catch (e: IOException) {
                        println(e.message)
                        return null
                    }
                    return null
                }
            }.execute()
            object : org.jdesktop.swingworker.SwingWorker<Any?, Any?>() {
                private var errorStream: BufferedReader? = null
                @Throws(java.lang.Exception::class)
                public override fun doInBackground(): Any? {
                    errorStream = BufferedReader(InputStreamReader(process!!.errorStream))
                    try {
                        errorStream!!.readLine()
                        errorStream!!.reset()
                    } catch (e: IOException) {
                        println(e.message)
                        return null
                    }
                    return null
                }
            }.execute()
        }
    }
}

abstract class AbstractTertiaryViewerDriver(mediator: Mediator, tertiaryViewerCommand: String) :
    OnHangApplicationDriver(mediator, tertiaryViewerCommand), TertiaryViewerDriver {
    override fun closeSession() {}
    override fun restoreSession(sessionFile: File, pdbFile: File) {}
    override fun removeSelection(positions: List<Int>) {}
    override fun addFragment(
        f: File,
        residues: List<Residue3D>,
        anchorResidue1: Int,
        anchorResidue2: Int,
        firstFragment: Boolean,
    ) {
    }

    override fun addInteraction(f: File, interaction: Location) {}
    override fun addResidue(f: File, position: Int, anchorResidue: Int) {}

    /**
     * Close the tertiary viewer
     */
    override fun close() {
        process?.destroy()
    }

    override fun eraseModel() {}

    open fun selectionCleared() {}

    @Throws(IOException::class)
    abstract fun saveSession(sessioNFile: File, pdbFile: File)
}

interface TertiaryViewerDriver : Driver {
    fun loadTertiaryStructure(f: File)
    fun closeSession()
    fun restoreSession(sessionFile: File, pdbFile: File)
    fun addFragment(
        f: File,
        residues: List<Residue3D>,
        anchorResidue1: Int,
        anchorResidue2: Int,
        firstFragment: Boolean,
    )

    fun addInteraction(f: File, interaction: Location)
    fun addResidue(f: File, position: Int, anchorResidue: Int)
    fun removeSelection(positions: List<Int>)
    fun eraseModel()
}