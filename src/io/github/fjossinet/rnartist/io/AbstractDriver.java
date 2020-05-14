package io.github.fjossinet.rnartist.io;

import io.github.fjossinet.rnartist.Mediator;

public abstract class AbstractDriver implements Driver {

    /**
     * The external program's fuall path+name+some options
     */
    protected String program;
    protected Mediator mediator;

    /**
     * Creates a new Driver for an external program defined with its name, full path and options
     *
     * @param program the program's name and its full path. Some options can also be precised at this level.
     */
    protected AbstractDriver(final Mediator mediator, final String program) {
        this.mediator = mediator;
        this.program = program;
    }
}
