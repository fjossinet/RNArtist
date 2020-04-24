package fr.unistra.rnartist.io;

import fr.unistra.rnartist.gui.Mediator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

abstract public class Computation {

    protected Mediator mediator;

    protected Computation(Mediator mediator) {
        this.mediator = mediator;
    }

    protected String postData(String webservice, Map<String,String> data) throws Exception {
        StringBuffer allData = new StringBuffer();
        String answer = null;
        for (String key:data.keySet()) {
            if (allData.length() != 0)
                allData.append("&");
            allData.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(data.get(key), "UTF-8"));
        }
        try {
            URL url = null;
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(allData.toString());
            wr.flush();

            StringBuffer result = new StringBuffer();
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null)
                result.append(line+"\n");
            wr.close();
            rd.close();
            answer = result.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }

    /*protected boolean isDockerInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "docker");
            Process p = pb.start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            boolean ok = result.trim().matches("^.+docker$");
            if (!ok) {
                JOptionPane.showMessageDialog(null,
                        "You need to install Docker",
                        "Docker missing",
                        JOptionPane.WARNING_MESSAGE);
                IoUtils.openBrowser("https://www.docker.com");
            }
            return ok;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "You need to install Docker",
                    "Docker missing",
                    JOptionPane.WARNING_MESSAGE);
            IoUtils.openBrowser("https://www.docker.com");
            return false;
        }
    }*/

    /*protected boolean isAssemble2DockerImageInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "images");
            Process p = pb.start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            for (String l: result.split("\n"))
                if (l.startsWith("fjossinet/assemble2"))
                    return true;
            JOptionPane.showMessageDialog(null,
                    "Your need to install the Docker image fjossinet/assemble2",
                    "Docker Image missing",
                    JOptionPane.WARNING_MESSAGE);
            IoUtils.openBrowser("https://hub.docker.com/r/fjossinet/assemble2/");
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Your need to install the Docker image fjossinet/assemble2",
                    "Docker Image missing",
                    JOptionPane.WARNING_MESSAGE);
            IoUtils.openBrowser("https://hub.docker.com/r/fjossinet/assemble2/");
            return false;
        }
    }*/

}
