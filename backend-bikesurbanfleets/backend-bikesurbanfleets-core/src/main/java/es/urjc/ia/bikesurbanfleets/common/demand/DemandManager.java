/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.common.demand;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import es.urjc.ia.bikesurbanfleets.core.config.GlobalInfo;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 *
 * @author holger
 */
public class DemandManager {

    Demand dem = new Demand();

    double getTakeDemandStation(Month month, Day day, int hour) {
        return dem.m[month.ordinal()].d[day.ordinal()].getTakeDemandGlobal(hour);
    }

    double getTakeDemandGlobal(Month month, Day day, int hour) {
        return dem.m[month.ordinal()].d[day.ordinal()].getTakeDemandGlobal(hour);
    }

    double getReturnDemandStation(int stationID, Month month, Day day, int hour) {
        return dem.m[month.ordinal()].d[day.ordinal()].getReturnDemand(stationID, hour);
    }

    double getReturnDemandGlobal( Month month, Day day, int hour) {
        return dem.m[month.ordinal()].d[day.ordinal()].getReturnDemandGlobal( hour);
    }

    public void ReadData(String file) {
        String[] line = new String[1];
        try {
            FileReader filereader = new FileReader(file);
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    //.withIgnoreQuotations(true)
                    //.withEscapeChar('@')
                    .build();
            CSVReader csvReader
                    = new CSVReaderBuilder(filereader)
                            .withSkipLines(1)
                            .withCSVParser(parser)
                            .build();
            while ((line = csvReader.readNext()) != null) {
                int station = Integer.parseInt(line[0]);
                String date = line[1];
                int year = Integer.parseInt(date.substring(6, 10));
                int month = Integer.parseInt(date.substring(3, 5));
                int day = Integer.parseInt(date.substring(0, 2));
                int returnNum = Integer.parseInt(line[2]);
                int takeNum = Integer.parseInt(line[3]);
                int hour = Integer.parseInt(line[4]);
                String dayOfWeek = line[5];
                int weekday = Integer.parseInt(line[6]); //1 is weekday; 0 is weekend
                dem.add(station, month, dayOfWeek, hour, takeNum, returnNum);
            }
        } catch (Exception ex) {
            throw new RuntimeException("error reading demand data");
        }
    }

    public enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dic, Summer, Winter, All
    }

    public enum Day {
        Mon, Tue, Wen, Thu, Fri, Sat, Sun, Weekday, Weekend
    }

    class Demand {

        Monthdata m[];

        Demand() {
            m = new Monthdata[15];
            for (int i = 0; i < 15; i++) {
                m[i] = new Monthdata();
            }
        }

        void add(int station, int month, String day, int hour, int take, int ret) {
            int d;
            int mo;
            if (day.equals("lun")) {
                d = 0;
            } else if (day.equals("mar")) {
                d = 1;
            } else if (day.equals("mie")) {
                d = 2;
            } else if (day.equals("jue")) {
                d = 3;
            } else if (day.equals("vie")) {
                d = 4;
            } else if (day.equals("sab")) {
                d = 5;
            } else if (day.equals("dom")) {
                d = 6;
            } else {
                throw new RuntimeException("invalid day text:" + day);
            }
            if (month < 1 || month > 12) {
                throw new RuntimeException("invalid month:" + month);
            } else {
                mo = month - 1;
            }
            m[mo].add(station, d, hour, take, ret);
            //winter and summer
            if (mo < 3 || mo > 9) {
                m[13].add(station, d, hour, take, ret);
            } else {
                m[12].add(station, d, hour, take, ret);
            }
            //add to all
            m[14].add(station, d, hour, take, ret);
        }

    }

    class Monthdata {

        Monthdata() {
            d = new DayData[9];
            for (int i = 0; i < 9; i++) {
                d[i] = new DayData();
            }
        }

        DayData d[];

        void add(int station, int day, int hour, int take, int ret) {
            d[day].add(station, hour, take, ret);
            if (day < 5) {
                d[7].add(station, hour, take, ret);
            } else {
                d[8].add(station, hour, take, ret);
            }
        }
    }

    class DayData {

        StationData global = new StationData();
        HashMap<Integer, StationData> valueMap = new HashMap<Integer, StationData>();

        void add(int station, int hour, int take, int ret) {
            StationData d = valueMap.get(station);
            if (d == null) {
                d = new StationData();
                valueMap.put(station, d);
            }
            d.add(hour, take, ret);
            global.add(hour, take, ret);
        }

        double getTakeDemandGlobal(int hour) {
            return (global.h[hour][0] / (double) global.h[hour][2]);
        }

        double getReturnDemandGlobal(int hour) {
            return (global.h[hour][1] / (double) global.h[hour][2]);
        }

        double getTakeDemand(int station, int hour) {
            StationData d = valueMap.get(station);
            if (d == null) {
                throw new RuntimeException("no station demand data available; invalid program state");
            }
            return (d.h[hour][0] / (double) d.h[hour][2]);
        }

        double getReturnDemand(int station, int hour) {
            StationData d = valueMap.get(station);
            if (d == null) {
                throw new RuntimeException("no station demand data available; invalid program state");
            }
            return (d.h[hour][1] / (double) d.h[hour][2]);
        }
    }

    class StationData {

        int h[][] = new int[24][3];

        void add(int hour, int take, int ret) {
            h[hour][0] += take;
            h[hour][1] += ret;
            h[hour][2]++;
        }
    }

    public static void main(String[] args) throws Exception {

        String projectDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3S-Simulator";
        String demandDataPath = projectDir + "/../datosViajesBiciMad.csv";
        DemandManager demandManager = new DemandManager();
        demandManager.ReadData(demandDataPath);
        Month[] m = Month.values();
        Day[] d = Day.values();
        for (Month mm : m) {
            for (Day dd : d) {
                for (int i = 0; i < 24; i++) {
                    System.out.println(
                            "demand Month:" + mm + " day: " + dd + " hour: " + i
                            + "take: " + demandManager.getTakeDemandGlobal(mm, dd, i)
                            + "return: " + demandManager.getReturnDemandGlobal(mm, dd, i)
                            + "entries: " + demandManager.dem.m[mm.ordinal()].d[dd.ordinal()].global.h[i][2]);
                    

                }

            }
        }

    }
}
