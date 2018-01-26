package es.urjc.ia.bikesurbanfleets.common.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class JsonValidation {

    private final static String NODE_ERROR = "NODE_NOT_INSTALLED";
    private final static String OK_VALIDATION = "OK";


    public static String validate(String schemaDir, String jsonDir, String jsValidatorDir) throws IOException, InterruptedException {

        if(!checkNode()) {
            return NODE_ERROR;
        }
        ArrayList<String> command = new ArrayList<>();
        command.addAll(Arrays.asList("node", jsValidatorDir, "verify", "-i", jsonDir, "-s", schemaDir));
        System.out.println("Executing: node " + jsValidatorDir + " verify -i " + jsonDir + " -s " + schemaDir);
        ProcessBuilder pb = new ProcessBuilder(command);
        Process validationProcess = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(validationProcess.getInputStream()));
        String line;
        String output = "";
        while ((line = in.readLine()) != null) {
            if(line.equals(OK_VALIDATION)) {
                output += line;
            }
            else {
                output += line + "\n";
            }
        }
        validationProcess.waitFor();
        return output;
    }

    private static boolean checkNode() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(new ArrayList<>(Arrays.asList("node", "-help")));
        Process p = pb.start();
        int exitValue = p.waitFor();
        return exitValue == 0;
    }

}
