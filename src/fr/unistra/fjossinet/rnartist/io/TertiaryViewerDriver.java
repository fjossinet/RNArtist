package fr.unistra.fjossinet.rnartist.io;

import fr.unistra.fjossinet.rnartist.model.Location;
import fr.unistra.fjossinet.rnartist.model.Residue3D;

import java.io.File;
import java.util.List;


public interface TertiaryViewerDriver extends Driver {

    public void loadTertiaryStructure(File f);

    public void closeSession();

    public void restoreSession(File f);

    public void addFragment(File f, List<Residue3D> residues, int anchorResidue1, int anchorResidue2, boolean firstFragment);

    public void addInteraction(File f, Location interaction);

    public void addResidue(File f, int position, int anchorResidue);

    public void removeSelection(final List<Integer> positions);

    public void eraseModel();
}
