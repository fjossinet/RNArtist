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

import static io.github.fjossinet.rnartist.core.model.io.ParsersKt.writePDB;
import static io.github.fjossinet.rnartist.core.model.io.UtilsKt.createTemporaryFile;

public class ChimeraDriver extends AbstractTertiaryViewerDriver {

    private String hostname = "127.0.0.1";
    private int port = 64514;
    private URL url;
    private File sessionToRestore;
    private List<String> lastSelectedResidues;

    public ChimeraDriver(final Mediator mediator) {
        super(mediator, RnartistConfig.getChimeraPath());
        this.mediator = mediator;
        this.mediator.setChimeraDriver(this);
        try {
            this.run(new String[]{"--start", "ReadStdin"}, null, null);
        } catch (IOException e) {
            if (e.getMessage().startsWith("Cannot run program")) {
                mediator.setChimeraDriver(null);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Problem with Chimera");
                alert.setHeaderText("Cannot Run the Chimera Program!");
                alert.setContentText("Check your Chimera Path in Toolbox -> Settings");
                alert.showAndWait();
            }
        }
    }

    public List<String> getLastSelectedResidues() {
        return lastSelectedResidues;
    }

    public void setRestServer(String hostname, int port) throws MalformedURLException {
        this.hostname = hostname;
        this.port = port;
        this.url = new URL("http://"+this.hostname+":"+this.port+"/run");
        this.showVersion();
        if (this.sessionToRestore != null) {
            this.postCommand("open " + this.sessionToRestore.getAbsolutePath());
            this.sessionToRestore = null;
        }
    }

    public void showVersion() {
        this.postCommand("version");
    }

    private String postCommand(final String command) {
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

    public String getHost() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public void closeSession() {
        mediator.setTertiaryStructure(null);
        this.evaluate("close session");
    }

    public void restoreSession(File f) {
        this.evaluate("open " + f.getAbsolutePath());
    }

    public void loadTertiaryStructure(File f) {
        this.evaluate("open " + f.getAbsolutePath());
    }

    public void loadTertiaryStructure(File f, int layer) {
        this.evaluate("close "+layer);
        this.evaluate("open "+layer+" "+ f.getAbsolutePath());
    }

    public void loadRefinedModel(File f) {
        this.evaluate("close 2");
        this.evaluate("open 2 "+f.getAbsolutePath());
    }

    public void removeSelection(final List<Integer> positions) {
        for (int pos: positions)
            this.evaluate("delete #0:"+pos);
    }

    public void close() {
        this.evaluate("stop noask");
    }

    @Override
    public void saveSession(File file) {
        this.evaluate("save "+file.getAbsolutePath());
    }

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

    public void turny(int angle) {
        this.evaluate("turn y 1 "+angle);
    }

    public void synchronizeFrom() throws Exception {
        File f = createTemporaryFile("export.pdb");
        PrintWriter commandInput = new PrintWriter((new OutputStreamWriter(new BufferedOutputStream(process.getOutputStream()))), true);
        commandInput.println("write relative 0 format pdb 0 " + f.getAbsolutePath());
        Thread.sleep(3000);
        //List<TertiaryStructure> structures = FileParser.parsePDB(mediator, new FileReader(f));
        TertiaryStructure newTS = new TertiaryStructure(mediator.getRna());
        if (mediator.getTertiaryStructure() != null)
            newTS.setPdbId(mediator.getTertiaryStructure().getPdbId());
        mediator.setTertiaryStructure(newTS);
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

    public void setPivot(List<String> positions, String chainName) {
        final StringBuffer command = new StringBuffer("cofr #0:");
        for (String pos:positions)
            command.append(pos+"."+chainName+",");
        this.evaluate(command.substring(0,command.length()-1));
    }

    public void setFocus(List<String> positions, String chainName) {
        final StringBuffer command = new StringBuffer("focus #0:");
        for (String pos:positions)
            command.append(pos+"."+chainName+",");
        this.evaluate(command.substring(0,command.length()-1));
    }

    public void synchronizeTo() throws Exception {
        File f = createTemporaryFile("model.pdb");
        writePDB(mediator.getTertiaryStructure(), true, new PrintWriter(f));
        evaluate("open 1 " +f.getAbsolutePath());
        evaluate("delete #0");
        evaluate("combine #0,1 modelId 0 name model close true");
    }

    public void eraseModel() {
        this.evaluate("close 0");
    }

    public void addResidue(File f, int position, int anchorResidue) {
        this.evaluate("open "+f.getAbsolutePath());
        this.evaluate("match #1:"+anchorResidue+" #0:"+anchorResidue);
        this.evaluate("delete #1:"+anchorResidue);
        this.evaluate("combine #0,1 modelId 0 name model close true");
        //this.synchronizeFrom(false); //we need to to that since Chimera has oved atoms to do the match
    }

    public void addInteraction(File f, Location interaction) {
        this.evaluate("open "+f.getAbsolutePath());
        this.evaluate("match #1:"+interaction.getStart()+","+interaction.getEnd()+" #0:"+interaction.getStart()+","+interaction.getEnd());
        this.evaluate("delete #0:"+interaction.getStart()); //the old interaction is removed
        this.evaluate("delete #0:"+interaction.getEnd());
        this.evaluate("combine #0,1 modelId 0 name model close true");
        //this.synchronizeFrom(false); //we need to to that since Chimera has oved atoms to do the match
    }

    public void addFragment(File f, List<Residue3D> residues, int anchorResidue1, int anchorResidue2, boolean firstFragment) {
        this.evaluate("open " + f.getAbsolutePath());
        if (anchorResidue1 != -1 && anchorResidue2 != -1)
            this.evaluate("match #1:"+anchorResidue1+","+anchorResidue2+" #0:"+anchorResidue1+","+anchorResidue2);
        else if (anchorResidue1 != -1) {
            this.evaluate("match #1:"+anchorResidue1+" #0:"+anchorResidue1);
        }
        else if (anchorResidue2 != -1)
            this.evaluate("match #1:"+anchorResidue2+" #0:"+anchorResidue2);
        if (!firstFragment) {
            for (Residue3D r:residues)
                if (r.getAbsolutePosition() != anchorResidue1 && r.getAbsolutePosition() != anchorResidue2)
                    this.evaluate("delete #0:"+r.getAbsolutePosition());

            if (anchorResidue1 != -1)
                this.evaluate("delete #1:"+anchorResidue1);
            if (anchorResidue2 != -1)
                this.evaluate("delete #1:"+anchorResidue2);
        }
        this.evaluate("combine #0,1 modelId 0 name model close true");
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
        this.evaluate("open " + f.getAbsolutePath());
        this.evaluate("match #1:"+residues.get(0).getAbsolutePosition()+","+residues.get(1).getAbsolutePosition()+" #0:"+residues.get(0).getAbsolutePosition()+","+residues.get(1).getAbsolutePosition());
        for (Residue3D r:residues)
            this.evaluate("delete #0:"+r.getAbsolutePosition());
        this.evaluate("combine #0,1 modelId 0 name model close true");
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
        /*if (toBedeleted.size() == 2 && mediator.getCanvas2D().getSecondaryStructureDrawing().getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(toBedeleted.get(0).getAbsolutePosition())).getAbsolutePosition() == toBedeleted.get(1).getAbsolutePosition()) {
            anchorResidue1 = toBedeleted.get(0).getAbsolutePosition();
            anchorResidue2 = toBedeleted.get(1).getAbsolutePosition();
        }*/
        else {
            List<Integer> positions = new ArrayList<Integer>();
            for (Residue3D r: toBedeleted)
                positions.add(r.getAbsolutePosition());
            anchorResidue1 = (Integer) JOptionPane.showInputDialog(null, "Choose the anchor position", "Choose the anchor position", JOptionPane.PLAIN_MESSAGE, null, positions.toArray(new Integer[]{}), positions.get(0));
        }

        this.evaluate("open " + f.getAbsolutePath());
        if (anchorResidue1 != -1 && anchorResidue2 != -1)
            this.evaluate("match #1:"+anchorResidue1+","+anchorResidue2+" #0:"+anchorResidue1+","+anchorResidue2);
        else if (anchorResidue1 != -1) {
            this.evaluate("match #1:"+anchorResidue1+" #0:"+anchorResidue1);
        }
        else if (anchorResidue2 != -1)
            this.evaluate("match #1:"+anchorResidue2+" #0:"+anchorResidue2);
        if (!firstFragment) {
            for (Residue3D r:toBedeleted)
                this.evaluate("delete #0:"+r.getAbsolutePosition());
        }
        this.evaluate("combine #0,1 modelId 0 name model close true");
        List<String> positions = new ArrayList<String>();
        for (Residue3D residue3D:computedResidues)
            positions.add(residue3D.getLabel());
        if (anchorResidue1 != -1) //we are not selecting the anchor residues to more easily remove the fragment just added without to remove the anchor residues
            positions.remove(""+anchorResidue1);
        if (anchorResidue2 != -1) //we are not selecting the anchor residues to more easily remove the fragment just added without to remove the anchor residues
            positions.remove(""+anchorResidue2);
        this.selectResidues(positions);
    }

    public void selectResidues(List<String> positions, String chainName) {
        final StringBuffer command = new StringBuffer("select #0,2:");  //the #2 is to select residues for the refined structure (if any)
        for (String pos:positions)
            command.append(pos+"."+chainName+",");
        this.evaluate(command.substring(0,command.length()-1));
        this.lastSelectedResidues = positions;
    }

    public void selectResidues(List<String> positions, int layer) {
        final StringBuffer command = new StringBuffer("select #"+layer+":");
        for (String pos:positions)
            command.append(pos+",");
        this.evaluate(command.substring(0,command.length()-1));
        this.lastSelectedResidues = positions;
    }

    public void showResidues(List<String> positions) {
        final StringBuffer command = new StringBuffer("show #0,2:");  //the #2 is to select residues for the refined structure (if any)
        for (String pos:positions)
            command.append(pos+",");
        this.evaluate(command.substring(0, command.length() - 1));
    }

    public void selectionCleared() {
        this.evaluate("~select");
        if (this.lastSelectedResidues != null)
            this.lastSelectedResidues.clear();
    }

    public void color3D(List<ResidueDrawing> residues) {
        /*final StringBuffer command = new StringBuffer();
        for (ResidueCircle r: residues)
            command.append("color "+((float)r.getColor().getRed()/(float)255)+","+(float)r.getColor().getGreen()/(float)255+","+(float)r.getColor().getBlue()/(float)255+","+(float)r.getColor().getAlpha()/(float)255+" #0,2:"+(mediator.getTertiaryStructure().getResidue3DAt(r.getAbsPos()) != null ? mediator.getTertiaryStructure().getResidue3DAt(r.getAbsPos()).getLabel() : "") +"; ");
        this.evaluate(command.toString());*/
    }
}
