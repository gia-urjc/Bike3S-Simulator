package es.urjc.ia.bikesurbanfleets.common.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class JsonValidation {

    private final static String NODE_ERROR = "NODE_NOT_INSTALLED";
    private final static String OK_VALIDATION = "OK";


    public static String validate(ValidationParams params) throws IOException, InterruptedException, Exception {

        if(!checkNode()) {
            return NODE_ERROR;
        }
        ArrayList<String> command = new ArrayList<>();
        command.addAll(Arrays.asList("node", params.jsValidatorDir, "verify", "-i", params.jsonDir, "-s", params.schemaDir));
        System.out.println("Executing: node " + params.jsValidatorDir + " verify -i " + params.jsonDir + " -s " + params.schemaDir);
        ProcessBuilder pb = new ProcessBuilder(command);
        Process validationProcess = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(validationProcess.getInputStream()));
        String line;
        String output = "";
        boolean correct = false;
        while ((line = in.readLine()) != null) {
            if(line.equals(OK_VALIDATION)) {
                output += line;
                correct = true;
            }
            else {
                output += line + "\n";
            }
        }
        if(!correct) {
            throw new Exception(output);
        }
        validationProcess.waitFor();
        return output;
    }

    private static boolean checkNode() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(new ArrayList<>(Arrays.asList("node", "-v")));
        Process p = pb.start();
        int exitValue = p.waitFor();
        return exitValue == 0;
    }

    public static class ValidationParams {

        private String schemaDir;
        private String jsonDir;
        private String jsValidatorDir;

        public ValidationParams() {}

        public ValidationParams setSchemaDir(String schemaDir) {
            this.schemaDir = schemaDir;
            return this;
        }

        public ValidationParams setJsonDir(String jsonDir) {
            this.jsonDir = jsonDir;
            return this;
        }

        public ValidationParams setJsValidatorDir(String jsValidatorDir) {
            this.jsValidatorDir = jsValidatorDir;
            return this;
        }
    }

}
