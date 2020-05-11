package io.github.fjossinet.rnartist.io;

import java.io.File;
import java.io.IOException;

/**
 * This class communicates with an external program launched
 * from the command line
 */
public interface Driver {

    /**
     * Launch the program
     *
     * @param options    options to give to the program at start (at least, an empty array of String)
     * @param inputFile  input file for the program (can be null)
     * @param outputFile output file for the program (can be null)
     * @return the program's output as a File object
     */
    abstract public void run(final String[] options, final File inputFile, final File outputFile) throws IOException;

    /**
     * Close the external application
     */
    abstract public void close();

    /**
     * Send a message to the running external program
     */
    abstract public void evaluate(String message);

}
