package fr.unistra.rnartist.io;

import fr.unistra.rnartist.gui.Mediator;
import fr.unistra.rnartist.model.Location;
import fr.unistra.rnartist.model.Residue3D;

import java.io.File;
import java.util.*;

public abstract class AbstractTertiaryViewerDriver extends OnHangApplicationDriver implements TertiaryViewerDriver {

    public AbstractTertiaryViewerDriver(final Mediator mediator, String tertiaryViewerCommand) {
        super(mediator, tertiaryViewerCommand);
    }

    public void closeSession() {
    }

    public void restoreSession(File f) {
    }

    public void removeSelection(final List<Integer> positions) {
    }

    public void addFragment(File f, List<Residue3D> residues, int anchorResidue1, int anchorResidue2, boolean firstFragment) {
    }

    public void addInteraction(File f, Location interaction) {
    }

    public void addResidue(File f, int position, int anchorResidue) {
    }

    /**
     * Close the tertiary viewer
     */
    public void close() {
        this.process.destroy();
    }

    public void eraseModel() {
    }

    public void selectResidues(List<String> positions) {
    }

    public void residuesUnSelected(List<Integer> positions) {
    }

    public void selectionCleared() {
    }

    public abstract void saveSession(File file);
}
