package es.urjc.ia.bikesurbanfleets.common.demand;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author holger
 */
public class DemandAnalysisFromHistoricData {

    Data SD = new Data();

//reads the json file and filters out all elements that are withing intmintime and in tmaxtime and writes them to new file
    //reads only the maxroutes entries
    public void ReadFileJson(String file) throws IOException {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("Read file:" + file);
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        File f = new File(file);
        InputStream is = new FileInputStream(f);

        JSONTokener jt = new JSONTokener(is);
        int total = 0;
        int totalWTT = 0;
        int[] timearray = new int[121];
        int i;
        for (i = 0; i < timearray.length; i++) {
            timearray[i] = 0;
        }
        if (jt.nextClean() != 0) {
            jt.back();
        }
        while (jt.more()) {
            Object ob = (Object) jt.nextValue();
            if (ob == JSONObject.NULL) {
                break;
            }
            if (!(ob instanceof JSONObject)) {
                throw new RuntimeException("error jason");
            }
            JSONObject json = (JSONObject) ob;
            total++;

            // get the entry:
            int user_type = json.getInt("user_type");

            if (user_type != 3) {
                int traveltime = json.getInt("travel_time");
                String date = json.getJSONObject("unplug_hourTime").getString("$date");
                int oristation = json.getInt("idunplug_station");
                int deststation = json.getInt("idplug_station");
                int takeyear = Integer.parseInt(date.substring(0, 4));
                int takemonth = Integer.parseInt(date.substring(5, 7));
                int takeday = Integer.parseInt(date.substring(8, 10));
                int takehour = Integer.parseInt(date.substring(11, 13));
                Calendar c = Calendar.getInstance();
                c.set(takeyear, takemonth - 1, takeday, takehour, 0);
                //get return date
                double aux = traveltime / 3600D;
                c.add(Calendar.HOUR_OF_DAY, (int) Math.floor(aux));
                int retyear = c.get(Calendar.YEAR);
                int retmonth = c.get(Calendar.MONTH) + 1;
                int retday = c.get(Calendar.DAY_OF_MONTH);
                int rethour = c.get(Calendar.HOUR_OF_DAY);
                SD.add(oristation, takeyear, takemonth, takeday, takehour, true);
                SD.add(deststation, retyear, retmonth, retday, rethour, false);
            }
            if (total % 1000 == 0) {
                System.out.println("" + total + " " + totalWTT);
            }
            if (jt.nextClean() != 0) {
                jt.back();
            }
        }
    }

    public void writedata(String fileout) throws IOException {
        //write data
        //set the writer
        Writer writer = new FileWriter(fileout);
        CSVWriter csvWriter = new CSVWriter(writer,
                ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        //Now set the String array for writing
        String[] record = new String[8];

        //setup header
        record[0] = "Station";
        record[1] = "dia";
        record[2] = "mes";
        record[3] = "año";
        record[4] = "hora";
        record[5] = "NumUsuariosCojenBiciEnEstacion";
        record[6] = "NumUsuariosReturnBiciAEstacion";
        record[7] = "Dia semana (lun=1)";
        csvWriter.writeNext(record);

        //now write all records
        for (int station : SD.valueMap.keySet()) {

            StatData sd = SD.valueMap.get(station);
            for (int year : sd.daydata.keySet()) {
                HashMap<Integer, HashMap<Integer, int[][]>> md = sd.daydata.get(year);
                for (int month : md.keySet()) {
                    HashMap<Integer, int[][]> dd = md.get(month);
                    for (int day : dd.keySet()) {
                        int[][] dat = dd.get(day);
                        for (int j = 0; j < 24; j++) {
                            record[0] = station + "";
                            record[1] = day + "";
                            record[2] = month + "";
                            record[3] = year + "";
                            record[4] = j + "";
                            record[5] = dat[j][0] + "";
                            record[6] = dat[j][1] + "";
                            Calendar c = Calendar.getInstance();
                            c.set(year, month - 1, day);

                            int dayofweek = c.get(Calendar.DAY_OF_WEEK);
                            switch (dayofweek) {
                                case 1:
                                    record[7] = "dom";
                                    break;
                                case 2:
                                    record[7] = "lun";
                                    break;
                                case 3:
                                    record[7] = "mar";
                                    break;
                                case 4:
                                    record[7] = "mie";
                                    break;
                                case 5:
                                    record[7] = "jue";
                                    break;
                                case 6:
                                    record[7] = "vie";
                                    break;
                                case 7:
                                    record[7] = "sab";
                                    break;
                            }
                            csvWriter.writeNext(record);

                        }
                    }
                }
            }
        }

        writer.close();
    }

    class StatData {

        HashMap<Integer, HashMap<Integer, HashMap<Integer, int[][]>>> daydata = new HashMap<Integer, HashMap<Integer, HashMap<Integer, int[][]>>>();

        //if take==true, a bike has been taken, else a bike is returned
        void add(int takeyear, int takemonth, int takeday, int takehour, boolean take) {
            HashMap<Integer, HashMap<Integer, int[][]>> md = daydata.get(takeyear);
            if (md == null) {
                md = new HashMap<Integer, HashMap<Integer, int[][]>>();
                daydata.put(takeyear, md);
            }
            HashMap<Integer, int[][]> dd = md.get(takemonth);
            if (dd == null) {
                dd = new HashMap<Integer, int[][]>();
                md.put(takemonth, dd);
            }
            int[][] hd = dd.get(takeday);
            if (hd == null) {
                hd = new int[24][2];
                for (int i = 0; i < 24; i++) {
                    hd[i][0] = 0;
                    hd[i][1] = 0;
                }
                dd.put(takeday, hd);
            }
            if (take) {
                hd[takehour][0]++;
            } else {
                hd[takehour][1]++;
            }
        }
    }

    class Data {

        HashMap<Integer, StatData> valueMap = new HashMap<Integer, StatData>();
        //if take ==true a bike is taken
        //else it is returned

        void add(int station, int takeyear, int takemonth, int takeday, int takehour, boolean take) {
            StatData sd = valueMap.get(station);
            if (sd == null) {
                sd = new StatData();
                valueMap.put(station, sd);
            }
            sd.add(takeyear, takemonth, takeday, takehour, take);
        }
    }

    public static void main(String[] args) throws Exception {
        DemandAnalysisFromHistoricData da=new DemandAnalysisFromHistoricData();
        String projectDir = "/Users/holger/workspace/BikeProjects/Data_madrid/Datos Uso bicis/";
        da.ReadFileJson(projectDir+"201708_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201709_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201710_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201711_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201712_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201801_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201802_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201803_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201804_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201805_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201806_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201807_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201808_Usage_Bicimad.json");
        da.ReadFileJson(projectDir+"201809_Usage_Bicimad.json");
       
        da.writedata(projectDir+"demandData.csv");

    }
}
