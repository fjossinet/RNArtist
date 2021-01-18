package io.github.fjossinet.rnartist.io;

import io.github.fjossinet.rnartist.Mediator;
import org.jdesktop.swingworker.SwingWorker;

import java.io.*;

/**
 * A driver for an application staying alive until an explicit close message.
 * This Driver is designed to launch and communicate with a graphical application for example
 */
public abstract class OnHangApplicationDriver extends AbstractDriver {

    protected Process process;

    protected OnHangApplicationDriver(final Mediator mediator, final String program) {
        super(mediator, program);
    }

    public void run(final String[] options, File inputFile, File outputFile) throws IOException {
        String[] command = new String[options.length + 1 /*1 is for the command name*/];
        command[0] = program;
        int i = 0;
        for (; i < options.length; i++)
            command[i + 1] = options[i];
        StringBuffer b = new StringBuffer(command[0]);
        for (int k = 1; k < command.length; k++) {
            b.append(" ");
            b.append(command[k]);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        this.process = pb.start();
    }

    public void evaluate(final String command) {
        if (this.process != null) {

            new SwingWorker() {
                private PrintWriter commandInput;

                protected Object doInBackground() throws Exception {
                    this.commandInput = new PrintWriter((new OutputStreamWriter(new BufferedOutputStream(process.getOutputStream()))), true);
                    this.commandInput.println(command);
                    return null;
                }

            }.execute();

            new SwingWorker() {
                private BufferedReader commandOutput;

                public Object doInBackground() throws Exception {
                    this.commandOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    try {
                        this.commandOutput.readLine();
                        this.commandOutput.reset();
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        return null;
                    }
                    return null;
                }
            }.execute();

            new SwingWorker() {
                private BufferedReader errorStream;

                public Object doInBackground() throws Exception {
                    this.errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    try {
                        this.errorStream.readLine();
                        this.errorStream.reset();

                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        return null;
                    }
                    return null;
                }
            }.execute();
        }
    }
}
