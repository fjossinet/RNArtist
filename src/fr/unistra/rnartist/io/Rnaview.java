package fr.unistra.rnartist.io;

import fr.unistra.rnartist.gui.Mediator;
import fr.unistra.rnartist.gui.RNArtist;
import fr.unistra.rnartist.model.SecondaryStructure;
import fr.unistra.rnartist.model.TertiaryStructure;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;

import static fr.unistra.rnartist.io.ParsersKt.parseRnaml;
import static fr.unistra.rnartist.io.ParsersKt.writePDB;
import static fr.unistra.rnartist.io.UtilsKt.createTemporaryFile;
import static fr.unistra.rnartist.utils.RnartistConfig.getUserDir;

public class Rnaview extends Computation {

    public Rnaview(Mediator mediator) {
        super(mediator);
        if (System.getProperty("os.name").equals("Windows 10")) {
            File destFile = new File(getUserDir(), "rnaview.bat");
            if (!destFile.exists()) {
                try {
                    destFile.createNewFile();
                    URL inputUrl = RNArtist.class.getResource("/fr/unistra/ibmc/rnartist/io/rnaview.bat");
                    FileUtils.copyURLToFile(inputUrl, destFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public SecondaryStructure annotate(TertiaryStructure ts) throws Exception {
        if (System.getProperty("os.name").equals("Windows 10")) {
            File temp = createTemporaryFile("rnaview");
            PrintWriter writer = new PrintWriter(temp);
            writePDB(ts, true, writer);
            ProcessBuilder pb = new ProcessBuilder(new File(getUserDir(), "rnaview.bat").getAbsolutePath(), temp.getAbsolutePath());
            Process p = pb.start();
            p.waitFor();
            SecondaryStructure ss = parseRnaml(new File(temp.getParent(), temp.getName()+".xml"));
            //TODO check if RNAVIEW has modified the RNA -> newTS (see below)
            return ss;
        } else {
            File temp = createTemporaryFile("rnaview");
            PrintWriter writer = new PrintWriter(temp);
            writePDB(ts, true, writer);
            ProcessBuilder pb = new ProcessBuilder("docker", "run", "-v", temp.getParent()+":/data", "fjossinet/assemble2", "rnaview", "-p", "/data/"+temp.getName());
            Process p = pb.start();
            p.waitFor();
            SecondaryStructure ss = parseRnaml(new File(temp.getParent(), temp.getName()+".xml"));
            System.out.println(temp.getAbsolutePath());
            System.out.println(ss.getRna().getSeq());
            System.out.println(ts.getRna().getSeq());
            //TODO check if RNAVIEW has modified the RNA -> newTS (see below) like 1C0A
            return ss;
        } /*else {

            StringWriter writer = new StringWriter();
            FileParser.writePDBFile(ts.getResidues3D(), true, writer);
            TertiaryStructure newTS = null;
            Map<String, String> data = new Hashtable<String, String>();
            data.put("data", writer.toString());
            data.put("tool", "rnaview");
            String _2DAnnotation = this.postData("compute/2d", data);
            if (_2DAnnotation != null && _2DAnnotation.length() != 0) {
                BasicDBObject annotation = (BasicDBObject) ((BasicDBList) JSON.parse(_2DAnnotation)).get(0);
                BasicDBObject secondaryStructure = (BasicDBObject) annotation.get("2D");
                Iterator helices = ((BasicDBList) secondaryStructure.get("helices")).iterator(),
                        tertiaryInteractions = ((BasicDBList) secondaryStructure.get("tertiaryInteractions")).iterator();
                List<Location> helicalLocations = new ArrayList<Location>();
                List<MutablePair<Location, String>> tertiaryInteractionLocations = new ArrayList<MutablePair<Location, String>>();
                List<MutablePair<Location, String>> nonCanonicalSecondaryInteractions = new ArrayList<MutablePair<Location, String>>();
                while (helices.hasNext()) {
                    BasicDBObject helix = (BasicDBObject) helices.next();
                    Object location = helix.get("location");

                    BasicDBList ends = null;
                    if (BasicDBObject.class.isInstance(location))
                        ends = (BasicDBList) ((BasicDBObject) location).get("ends");
                    else
                        ends = (BasicDBList) location;

                    BasicDBList strand1 = (BasicDBList) ends.get(0), strand2 = (BasicDBList) ends.get(1);
                    helicalLocations.add(new Location(new Location((Integer) strand1.get(0), (Integer) strand1.get(1)), new Location((Integer) strand2.get(0), (Integer) strand2.get(1))));
                    if (helix.get("interactions") != null) {
                        Iterator nonCanonicals = ((BasicDBList) helix.get("interactions")).iterator();
                        while (nonCanonicals.hasNext()) {
                            BasicDBObject nonCanonical = (BasicDBObject) nonCanonicals.next();
                            Object _location = nonCanonical.get("location");

                            BasicDBList _ends = null;
                            if (BasicDBObject.class.isInstance(location))
                                _ends = (BasicDBList) ((BasicDBObject) _location).get("ends");
                            else
                                _ends = (BasicDBList) _location;

                            BasicDBList _edge1 = (BasicDBList) _ends.get(0), _edge2 = (BasicDBList) _ends.get(1);
                            String _type = nonCanonical.get("orientation") + "" + nonCanonical.get("edge1") + "" + nonCanonical.get("edge2");
                            nonCanonicalSecondaryInteractions.add(new MutablePair<Location, String>(new Location(new Location((Integer) _edge1.get(0)), new Location((Integer) _edge2.get(0))), _type.toUpperCase()));
                        }
                    }
                }
                while (tertiaryInteractions.hasNext()) {
                    BasicDBObject tertiaryInteraction = (BasicDBObject) tertiaryInteractions.next();
                    Object location = tertiaryInteraction.get("location");

                    BasicDBList ends = null;
                    if (BasicDBObject.class.isInstance(location))
                        ends = (BasicDBList) ((BasicDBObject) location).get("ends");
                    else
                        ends = (BasicDBList) location;
                    BasicDBList edge1 = (BasicDBList) ends.get(0), edge2 = (BasicDBList) ends.get(1);
                    String type = tertiaryInteraction.get("orientation") + "" + tertiaryInteraction.get("edge1") + "" + tertiaryInteraction.get("edge2");
                    tertiaryInteractionLocations.add(new MutablePair<Location, String>(new Location(new Location((Integer) edge1.get(0)), new Location((Integer) edge2.get(0))), type.toUpperCase()));
                }

                BasicDBObject rna = (BasicDBObject) secondaryStructure.get("rna");
                Molecule newMolecule = new Molecule(ts.getMolecule().getName(), (String) rna.get("sequence"));
                BasicDBObject new3D = (BasicDBObject) annotation.get("3D");
                if (new3D != null) {
                    BasicDBObject residues = (BasicDBObject) new3D.get("residues");
                    newTS = new TertiaryStructure(ts.getName());
                    newTS.setId(ts.getId());
                    newTS.setMolecule(newMolecule);
                    for (String key : residues.keySet()) {
                        BasicDBObject residue = (BasicDBObject) residues.get(key);
                        Residue3D residue3D = newTS.addResidue3D(Integer.parseInt(key));
                        BasicDBList atoms = (BasicDBList) residue.get("atoms");
                        Iterator itAtoms = atoms.iterator();
                        while (itAtoms.hasNext()) {
                            BasicDBObject atom = (BasicDBObject) itAtoms.next();
                            BasicDBList coords = (BasicDBList) atom.get("coords");
                            residue3D.setAtomCoordinates((String) atom.get("name"), Float.parseFloat("" + coords.get(0)), Float.parseFloat("" + coords.get(1)), Float.parseFloat("" + coords.get(2)));
                        }
                    }
                }
                SecondaryStructure ss = null;
                if (newTS != null) {
                    ss = new SecondaryStructure(mediator, newTS.getMolecule(), helicalLocations, nonCanonicalSecondaryInteractions, tertiaryInteractionLocations);
                    ss.setLinkedTs(newTS);
                } else {
                    ss = new SecondaryStructure(mediator, ts.getMolecule(), helicalLocations, nonCanonicalSecondaryInteractions, tertiaryInteractionLocations);
                    ss.setLinkedTs(ts);
                    ss.setName("Computed with RNAVIEW");
                }
                ss.setName("Computed with RNAVIEW");

                return ss;
                return null;
            }*/
        }
    }
