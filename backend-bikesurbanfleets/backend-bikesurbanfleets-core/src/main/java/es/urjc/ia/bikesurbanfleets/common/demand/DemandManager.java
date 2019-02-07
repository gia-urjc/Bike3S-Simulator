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
import com.opencsv.RFC4180Parser;
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

    double getReturnDemandGlobal(int stationID, Month month, Day day, int hour) {
        return dem.m[month.ordinal()].d[day.ordinal()].getReturnDemand(stationID, hour);
    }

    public void ReadData(String file) {
        CSVReader reader;
        try {
        FileReader filereader = new FileReader(file);
        RFC4180Parser parser
                = new RFC4180Parser();
              /*          .withSeparator(',')
                        .withQuoteChar('"') 
                        .build();
   */     CSVReader csvReader 
                = new CSVReaderBuilder(filereader)
                        .withSkipLines(1)
                        .withCSVParser(parser)
                        .build();
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                int station = Integer.parseInt(line[0]);
                String date = line[1];
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(5, 7));
                int day = Integer.parseInt(date.substring(8, 10));
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

        Monthdata m[] = new Monthdata[12];

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
            } else if (day.equals( "sab")) {
                d = 5;
            } else if (day.equals("dom")) {
                d = 6;
            } else {
                throw new RuntimeException("invalid day text");
            }
            if (month < 1 || month > 12) {
                throw new RuntimeException("invalid day text");
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

        DayData d[] = new DayData[9];

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
        }

        double getTakeDemandGlobal(int hour) {
            return (global.h[hour][0] / (double) global.numEntries);
        }

        double getReturnDemandGlobal(int hour) {
            return (global.h[hour][1] / (double) global.numEntries);
        }

        double getTakeDemand(int station, int hour) {
            StationData d = valueMap.get(station);
            if (d == null) {
                throw new RuntimeException("no station demand data available; invalid program state");
            }
            return (d.h[hour][0] / (double) d.numEntries);
        }

        double getReturnDemand(int station, int hour) {
            StationData d = valueMap.get(station);
            if (d == null) {
                throw new RuntimeException("no station demand data available; invalid program state");
            }
            return (d.h[hour][1] / (double) d.numEntries);
        }
    }

    class StationData {

        int h[][] = new int[24][2];
        int numEntries = 0;

        void add(int hour, int take, int ret) {
            h[hour][0] += take;
            h[hour][1] += ret;
            numEntries++;
        }
    }
}
