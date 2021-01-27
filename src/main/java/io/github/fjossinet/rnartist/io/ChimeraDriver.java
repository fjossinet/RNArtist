package io.github.fjossinet.rnartist.io;

import io.github.fjossinet.rnartist.Mediator;
import io.github.fjossinet.rnartist.core.model.*;
import javafx.scene.control.Alert;

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.parsePDB;
import static io.github.fjossinet.rnartist.core.model.io.UtilsKt.copyFile;
import static io.github.fjossinet.rnartist.core.model.io.UtilsKt.createTemporaryFile;

public class ChimeraDriver extends AbstractTertiaryViewerDriver {

    public static final byte STICK = 0, RIBBON_SHOW = 1, RIBBON_HIDE = 2, CARTOON = 3;

    //the URL constructed from the host and port defined in the RNArtist settings
    private URL url;
    //the pdbFile is given to Chimera to load and display the 3D structure
    private File pdbFile;
    //the pdbFile is given to Chimera to load and display the 3D structure
    private File sessionFile;
    //the tertiary structures are constructed from the parsing of the PDB file. They're mainly used to get the numbering system for each molecular chain when some residues are selected in the Canvas2D.
    private List<TertiaryStructure> tertiaryStructures;


    public ChimeraDriver(final Mediator mediator) {
        super(mediator, RnartistConfig.getChimeraPath());
        this.mediator = mediator;
        this.tertiaryStructures = new ArrayList<>();
        try {
            this.url = new URL("http://" + RnartistConfig.getChimeraHost() + ":" + RnartistConfig.getChimeraPort() + "/run");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public void connectToExecutable() {
        try {
            this.run(new String[]{"--start", "ReadStdin"}, null, null);
            this.url = null;
        } catch (IOException e) {
            if (e.getMessage().startsWith("Cannot run program")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Problem with Chimera");
                alert.setHeaderText("Cannot Run the Chimera Program!");
                alert.setContentText("Check your Chimera Path in Settings");
                alert.showAndWait();
            }
        }
    }

    public void connectToRestServer() throws MalformedURLException {
        this.url = new URL("http://"+RnartistConfig.getChimeraHost()+":"+RnartistConfig.getChimeraPort()+"/run");
        this.showVersion();
        this.process = null;
    }

    public void showVersion() {
        this.postCommand("version");
    }

    /**
     * The command is either sent to the process or to the REST server
     * @param command
     * @return
     */
    private String postCommand(final String command) {
        if (this.url == null && this.process == null)
            return null;
        try {
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    if (url != null) {
                        String data = URLEncoder.encode("command", "UTF-8") + "=" + URLEncoder.encode(command, "UTF-8");
                        URLConnection conn = url.openConnection();
                        conn.setDoOutput(true);
                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                        wr.write(data.toString());
                        wr.flush();

                        StringBuffer result = new StringBuffer();
                        //Get the response
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;

                        while ((line = rd.readLine()) != null)
                            result.append(line+"\n");
                        wr.close();
                        rd.close();
                        //return result.toString();
                    }
                    else
                        evaluate(command);
                    return null;
                }
            }.execute();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeSession() {
        this.pdbFile = null;
        this.sessionFile = null;
        this.tertiaryStructures.clear();
        this.postCommand("close session");
    }

    public void restoreSession(File sessionFile, File pdbFile) {
        try {
            this.postCommand("open " + sessionFile.getAbsolutePath());
            this.sessionFile = sessionFile;
            this.pdbFile = pdbFile;
            this.tertiaryStructures.addAll(parsePDB(new FileReader(pdbFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadTertiaryStructure(File f) {
        try {
            this.pdbFile = f;
            this.tertiaryStructures.addAll(parsePDB(new FileReader(f)));
            this.postCommand("open " + f.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadTertiaryStructure(File f, int layer) {
        try {
            this.pdbFile = f;
            this.tertiaryStructures.addAll(parsePDB(new FileReader(f)));
            this.postCommand("close "+layer);
            this.postCommand("open "+layer+" "+ f.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void reloadTertiaryStructure() {
        if (this.sessionFile.exists())
            this.postCommand("open " + sessionFile.getAbsolutePath());
        else if (this.pdbFile.exists())
            this.postCommand("open " + this.pdbFile.getAbsolutePath());
    }

    public void close() {
        this.postCommand("stop noask");
    }

    @Override
    public void saveSession(File sessionFile, File pdbFile) throws IOException {
        if (!this.tertiaryStructures.isEmpty() && this.pdbFile != null) {
            this.postCommand("save " + sessionFile.getAbsolutePath());
            if (!this.pdbFile.getAbsolutePath().equals(pdbFile.getAbsolutePath())) //to avoid to clash with the same fiel id the project is updated (then no need to copy the file, it is already there
                copyFile(this.pdbFile, pdbFile);
        }
    }

    public void represent(byte mode, List<Integer> positions) {
        String chainName = mediator.getDrawingDisplayed().get().getDrawing().getSecondaryStructure().getRna().getName();
        List<String> numberingSystem = null;
        for (TertiaryStructure ts:this.tertiaryStructures) {
            if (ts.getRna().getName().equals(chainName)) {
                numberingSystem = ts.getNumberingSystem();
                break;
            }
        }
        if (numberingSystem != null) {
            final StringBuffer atomsSpec = new StringBuffer();
            if (!positions.isEmpty()) {
                atomsSpec.append("#0:");
                for (int pos : positions)
                    atomsSpec.append(numberingSystem.get(pos - 1) + "." + chainName + ",");
                atomsSpec.deleteCharAt(atomsSpec.length()-1);
            }


            //2. the mode chosen
            switch (mode) {
                case CARTOON: StringBuffer command = new StringBuffer("nucleotides side tube/slab "+atomsSpec.toString());
                    this.postCommand(command.toString());
                    break;
                case STICK:
                    command = new StringBuffer("nucleotides sidechain atoms "+atomsSpec.toString());
                    this.postCommand(command.toString());
                    System.out.println(command.toString());
                    break;
                case RIBBON_SHOW:
                    command = new StringBuffer("ribbon "+atomsSpec.toString());
                    this.postCommand(command.toString());
                    System.out.println(command.toString());
                    break;
                case RIBBON_HIDE:
                    command = new StringBuffer("~ribbon "+atomsSpec.toString());
                    this.postCommand(command.toString());
                    System.out.println(command.toString());
                    break;

            }

        }
    }

    public void selectResidues(List<Integer> positions) {
        String chainName = mediator.getDrawingDisplayed().get().getDrawing().getSecondaryStructure().getRna().getName();
        List<String> numberingSystem = null;
        for (TertiaryStructure ts:this.tertiaryStructures) {
            if (ts.getRna().getName().equals(chainName)) {
                numberingSystem = ts.getNumberingSystem();
                break;
            }
        }
        if (numberingSystem != null) {
            final StringBuffer command = new StringBuffer("select #0,2:");  //the #2 is to select residues for the refined structure (if any)
            for (int pos : positions)
                command.append(numberingSystem.get(pos - 1) + "." + chainName + ",");
            this.postCommand(command.substring(0, command.length() - 1));
        }
    }

    public void selectResidues(List<Integer> positions, int layer) {
        String chainName = mediator.getDrawingDisplayed().get().getDrawing().getSecondaryStructure().getRna().getName();
        List<String> numberingSystem = null;
        for (TertiaryStructure ts:this.tertiaryStructures)
            if (ts.getRna().getName().equals(chainName)) {
                numberingSystem = ts.getNumberingSystem();
                break;
            }
        if (numberingSystem != null) {
            final StringBuffer command = new StringBuffer("select #" + layer + ":");
            for (int pos : positions)
                command.append(numberingSystem.get(pos - 1) + ",");
            this.postCommand(command.substring(0, command.length() - 1));
        }
    }

    public void showResidues(List<String> positions) {
        final StringBuffer command = new StringBuffer("show #0,2:");  //the #2 is to select residues for the refined structure (if any)
        for (String pos:positions)
            command.append(pos+",");
        this.postCommand(command.substring(0, command.length() - 1));
    }

    public void selectionCleared() {
        this.postCommand("~select");
    }

    public void color3D(List<ResidueDrawing> residues) {
        String chainName = mediator.getDrawingDisplayed().get().getDrawing().getSecondaryStructure().getRna().getName();
        List<String> numberingSystem = null;
        for (TertiaryStructure ts:this.tertiaryStructures)
            if (ts.getRna().getName().equals(chainName)) {
                numberingSystem = ts.getNumberingSystem();
                break;
            }
        if (numberingSystem != null) {
            final StringBuffer command = new StringBuffer();
            for (ResidueDrawing r : residues)
                command.append("color " + ((float) r.getColor().getRed() / (float) 255) + "," + (float) r.getColor().getGreen() / (float) 255 + "," + (float) r.getColor().getBlue() / (float) 255 + "," + (float) r.getColor().getAlpha() / (float) 255 + " #0,2:" + numberingSystem.get(r.getAbsPos() - 1) + "." + chainName + "; ");
            this.postCommand(command.toString());
        }
    }

    public void setPivot(List<Integer> positions) {
        String chainName = mediator.getDrawingDisplayed().get().getDrawing().getSecondaryStructure().getRna().getName();
        List<String> numberingSystem = null;
        for (TertiaryStructure ts:this.tertiaryStructures)
            if (ts.getRna().getName().equals(chainName)) {
                numberingSystem = ts.getNumberingSystem();
                break;
            }
        final StringBuffer command = new StringBuffer("cofr #0:");
        for (int pos:positions)
            command.append(numberingSystem.get(pos-1)+"."+chainName+",");
        this.postCommand(command.substring(0,command.length()-1));
    }

    public void setFocus(List<Integer> positions) {
        String chainName = mediator.getDrawingDisplayed().get().getDrawing().getSecondaryStructure().getRna().getName();
        List<String> numberingSystem = null;
        for (TertiaryStructure ts:this.tertiaryStructures)
            if (ts.getRna().getName().equals(chainName)) {
                numberingSystem = ts.getNumberingSystem();
                break;
            }
        final StringBuffer command = new StringBuffer("focus #0:");
        for (int pos:positions)
            command.append(numberingSystem.get(pos-1)+"."+chainName+",");
        this.postCommand(command.substring(0,command.length()-1));
    }

    public URL getUrl() {
        return url;
    }

    public File getPdbFile() {
        return pdbFile;
    }

    public List<TertiaryStructure> getTertiaryStructures() {
        return tertiaryStructures;
    }

    //+++++++ Methods used for 3D modeling (not available with RNArtist at now)

    public void importStructure(int modelID, int relativeID) throws Exception {
        File f = createTemporaryFile("export.pdb");
        PrintWriter commandInput = new PrintWriter((new OutputStreamWriter(new BufferedOutputStream(process.getOutputStream()))), true);
        if (relativeID != -1)
            commandInput.println("write relative "+relativeID+" format pdb "+modelID+" " + f.getAbsolutePath());
        else
            commandInput.println("write format pdb "+modelID+" " + f.getAbsolutePath());
        Thread.sleep(3000);
        //mediator.getAssemble().loadTertiaryStructures(FileParser.parsePDB(mediator, new FileReader(f)));
    }

    public void synchronizeFrom() throws Exception {
        File f = createTemporaryFile("export.pdb");
        PrintWriter commandInput = new PrintWriter((new OutputStreamWriter(new BufferedOutputStream(process.getOutputStream()))), true);
        commandInput.println("write relative 0 format pdb 0 " + f.getAbsolutePath());
        Thread.sleep(3000);
        //List<TertiaryStructure> structures = FileParser.parsePDB(mediator, new FileReader(f));
        TertiaryStructure newTS = new TertiaryStructure(mediator.getRna());
        /*if (mediator.getTertiaryStructure() != null)
            newTS.setPdbId(mediator.getTertiaryStructure().getPdbId());
        mediator.setTertiaryStructure(newTS);*/
        /*for (TertiaryStructure ts:structures)
            for (Residue3D residue3D:ts.getResidues()) {
                Residue3D _residue3D = newTS.addResidue3D(Integer.parseInt(residue3D.getLabel())); //IMPORTANT HERE!!!! CHIMERA HAS TO USE THE LABEL OF THE RESIDUE AS ITS ABSOLUTE POSITION.
                for (Atom a: residue3D.getAtoms())
                    if (a.hasCoordinatesFilled())
                        _residue3D.setAtomCoordinates(a.getName(),a.getX(),a.getY(),a.getZ());
            }*/
        //mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Synchronization done!!", null, null);
        //mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
    }
    public void loadRefinedModel(File f) {
        this.postCommand("close 2");
        this.postCommand("open 2 "+f.getAbsolutePath());
    }

    public void removeSelection(final List<Integer> positions) {
        for (int pos: positions)
            this.postCommand("delete #0:"+pos);
    }

    public void synchronizeTo() throws Exception {
        File f = createTemporaryFile("model.pdb");
        //writePDB(mediator.getTertiaryStructure(), true, new PrintWriter(f));
        postCommand("open 1 " +f.getAbsolutePath());
        postCommand("delete #0");
        postCommand("combine #0,1 modelId 0 name model close true");
    }

    public void eraseModel() {
        this.postCommand("close 0");
    }

    public void addResidue(File f, int position, int anchorResidue) {
        this.postCommand("open "+f.getAbsolutePath());
        this.postCommand("match #1:"+anchorResidue+" #0:"+anchorResidue);
        this.postCommand("delete #1:"+anchorResidue);
        this.postCommand("combine #0,1 modelId 0 name model close true");
        //this.synchronizeFrom(false); //we need to to that since Chimera has oved atoms to do the match
    }

    public void addInteraction(File f, Location interaction) {
        this.postCommand("open "+f.getAbsolutePath());
        this.postCommand("match #1:"+interaction.getStart()+","+interaction.getEnd()+" #0:"+interaction.getStart()+","+interaction.getEnd());
        this.postCommand("delete #0:"+interaction.getStart()); //the old interaction is removed
        this.postCommand("delete #0:"+interaction.getEnd());
        this.postCommand("combine #0,1 modelId 0 name model close true");
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
