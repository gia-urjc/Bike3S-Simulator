package es.urjc.ia.bikesurbanfleets.common.util;

import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.util.Precision;

import java.util.Arrays;

public class MessageGuiFormatter {

    private static String ERROR_PATTERN = "[Error] ";
    private static String PERCENTAGE_PATTERN = "[Percentage of users appeared] ";

    /**
     * Takes a string and separate it in a list of string that will be sended one by one
     * to the gui via stdout
     * @param errors
     */
    public static void showErrorsForGui(String errors) {
        Arrays.asList(errors.split("\\r?\\n")).forEach(mes -> System.out.println(ERROR_PATTERN + mes));
    }

    public static void showErrorsForGui(Throwable e) {
        System.out.println(ERROR_PATTERN + "Exception: ");
        Arrays.asList(ExceptionUtils.getStackTrace(e).split("\\r?\\n")).forEach(message -> {
            System.out.println(ERROR_PATTERN + message);
        });
        System.out.println(ERROR_PATTERN);
    }

    public static void showPercentageForGui(double percentage) {
        System.out.println(PERCENTAGE_PATTERN + Precision.round(percentage, 2) + " Time:" + SimulationDateTime.getCurrentSimulationDateTime() );
    }
}
