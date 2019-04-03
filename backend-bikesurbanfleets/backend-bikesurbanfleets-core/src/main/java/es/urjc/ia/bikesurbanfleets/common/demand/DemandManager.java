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
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 *
 * @author holger
 */
public class DemandManager {

    //if no demand data is available, the average of all stations in the same period is returned
    //this is the minimum demand that is assumed if teh obtained demand is 0
    private final static double MIN_DEMAND = 0.05;

    public enum Month {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dic, Summer, Winter, All;

        public static Month toDemandMangerMonth(java.time.Month m) {
            switch (m) {
                case JANUARY:
                    return Jan;
                case FEBRUARY:
                    return Feb;
                case MARCH:
                    return Mar;
                case APRIL:
                    return Apr;
                case MAY:
                    return May;
                case JUNE:
                    return Jun;
                case JULY:
                    return Jul;
                case AUGUST:
                    return Aug;
                case SEPTEMBER:
                    return Sep;
                case OCTOBER:
                    return Oct;
                case NOVEMBER:
                    return Nov;
                case DECEMBER:
                    return Dic;
            }
            return null;
        }
    }

    public enum Day {
        Mon, Tue, Wed, Thu, Fri, Sat, Sun, Weekday, Weekend;

        public static Day toDemandMangerDay(java.time.DayOfWeek d) {
            switch (d) {
                case MONDAY:
                    return Mon;
                case TUESDAY:
                    return Tue;
                case WEDNESDAY:
                    return Wed;
                case THURSDAY:
                    return Thu;
                case FRIDAY:
                    return Fri;
                case SATURDAY:
                    return Sat;
                case SUNDAY:
                    return Sun;
            }
            return null;
        }
    }

    private class DemandResult {

        boolean hasdemand;
        double demand;

        DemandResult(boolean hasdemand, double demand) {
            this.hasdemand = hasdemand;
            this.demand = demand;
        }
    }
    Demand dem = new Demand();

    public double getTakeDemandStation(int stationID, LocalDateTime t) {
        return getTakeDemandStation(stationID, Month.toDemandMangerMonth(t.getMonth()), Day.toDemandMangerDay(t.getDayOfWeek()), t.getHour());
    }

    public double getReturnDemandStation(int stationID, LocalDateTime t) {
        return getReturnDemandStation(stationID, Month.toDemandMangerMonth(t.getMonth()), Day.toDemandMangerDay(t.getDayOfWeek()), t.getHour());
    }

    public double getTakeDemandGlobal(LocalDateTime t) {
        return getTakeDemandGlobal(Month.toDemandMangerMonth(t.getMonth()), Day.toDemandMangerDay(t.getDayOfWeek()), t.getHour());
    }

    public double getReturnDemandGlobal(LocalDateTime t) {
        return getReturnDemandGlobal(Month.toDemandMangerMonth(t.getMonth()), Day.toDemandMangerDay(t.getDayOfWeek()), t.getHour());
    }

    public double getTakeDemandStation(int stationID, Month month, Day day, int hour) {
        DemandResult r = dem.getDemandStation(stationID, month, day, hour, true);
        double result;
        if (!r.hasdemand) {
            result = dem.getAverageStationDemand(month, day, hour, true);
        } else {
            result = r.demand;
        }
        if (result < MIN_DEMAND) {
            return MIN_DEMAND;
        }
        return result;
    }

    public double getReturnDemandStation(int stationID, Month month, Day day, int hour) {
        DemandResult r = dem.getDemandStation(stationID, month, day, hour, false);
        double result;
        if (!r.hasdemand) {
            result = dem.getAverageStationDemand(month, day, hour, false);
        } else {
            result = r.demand;
        }
        if (result < MIN_DEMAND) {
            return MIN_DEMAND;
        }
        return result;
    }

    public double getTakeDemandGlobal(Month month, Day day, int hour) {
        double result = dem.getDemandGlobal(month, day, hour, true);
        if (result < MIN_DEMAND) {
            return MIN_DEMAND;
        }
        return result;
    }

    public double getReturnDemandGlobal(Month month, Day day, int hour) {
        double result = dem.getDemandGlobal(month, day, hour, false);
        if (result < MIN_DEMAND) {
            return MIN_DEMAND;
        }
        return result;
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
                int day = Integer.parseInt(line[1]);
                int month = Integer.parseInt(line[2]);
                int year = Integer.parseInt(line[3]);
                int hour = Integer.parseInt(line[4]);
                int takeNum = Integer.parseInt(line[5]);
                int returnNum = Integer.parseInt(line[6]);
                String dayOfWeek = line[7];
                /*     if (day==5 && month==10) {
                    System.out.println();
                    for (String s : line) {
                        System.out.print(s + " ");
                    }
                 */
                dem.add(station, month, dayOfWeek, hour, takeNum, returnNum);
                //     }
            }
            dem.setGlobalDemand();
        } catch (Exception ex) {
            throw new RuntimeException("error reading demand data");
        }
    }

    private class Demand {

        HashMap< Integer, StationData> stationMap;
        HashMap< Month, HashMap<Day, double[][]>> globalDemand;
        int numberstations;

        int getNumberStations() {
            return stationMap.size();
        }

        Demand() {
            stationMap = new HashMap< Integer, StationData>();
        }

        void add(int station, int month, String day, int hour, int take, int ret) {
            //first convert day and month
            Day d;
            Month m;
            if (day.equals("lun")) {
                d = Day.Mon;
            } else if (day.equals("mar")) {
                d = Day.Tue;
            } else if (day.equals("mie")) {
                d = Day.Wed;
            } else if (day.equals("jue")) {
                d = Day.Thu;
            } else if (day.equals("vie")) {
                d = Day.Fri;
            } else if (day.equals("sab")) {
                d = Day.Sat;
            } else if (day.equals("dom")) {
                d = Day.Sun;
            } else {
                throw new RuntimeException("invalid day text:" + day);
            }
            if (month == 1) {
                m = Month.Jan;
            } else if (month == 2) {
                m = Month.Feb;
            } else if (month == 3) {
                m = Month.Mar;
            } else if (month == 4) {
                m = Month.Apr;
            } else if (month == 5) {
                m = Month.May;
            } else if (month == 6) {
                m = Month.Jun;
            } else if (month == 7) {
                m = Month.Jul;
            } else if (month == 8) {
                m = Month.Aug;
            } else if (month == 9) {
                m = Month.Sep;
            } else if (month == 10) {
                m = Month.Oct;
            } else if (month == 11) {
                m = Month.Nov;
            } else if (month == 12) {
                m = Month.Dic;
            } else {
                throw new RuntimeException("invalid month:" + month);
            }
            if (hour < 0 || hour > 23) {
                throw new RuntimeException("invalid hour:" + hour);
            }
            //now add to station
            StationData sd = stationMap.get(station);
            if (sd == null) {
                sd = new StationData();
                stationMap.put(station, sd);
            }
            sd.add(m, d, hour, take, ret);
        }

        void setGlobalDemand() {
            globalDemand = new HashMap< Month, HashMap<Day, double[][]>>(15);
            for (int key : stationMap.keySet()) {
                StationData stationdata = stationMap.get(key);

                for (Month stationmonth : stationdata.monthMap.keySet()) {
                    MonthData stationmonthdata = stationdata.monthMap.get(stationmonth);

                    HashMap<Day, double[][]> globalmonthdata = globalDemand.get(stationmonth);
                    if (globalmonthdata == null) {
                        globalmonthdata = new HashMap<Day, double[][]>();
                        globalDemand.put(stationmonth, globalmonthdata);
                    }

                    for (Day stationday : stationmonthdata.dayMap.keySet()) {
                        DayData stationdaydata = stationmonthdata.dayMap.get(stationday);

                        double[][] globaldaydata = globalmonthdata.get(stationday);
                        if (globaldaydata == null) {
                            globaldaydata = new double[24][2];
                            for (int i = 0; i < 14; i++) {
                                globaldaydata[i][0] = 0;
                                globaldaydata[i][1] = 0;
                            }
                            globalmonthdata.put(stationday, globaldaydata);
                        }

                        for (int i = 0; i < 24; i++) {
                            //                               if (i==0 && stationmonth==Month.Jan && stationday==Day.Mon){
                            //                                 System.out.println("add");
                            //                           } 
                            globaldaydata[i][0] = globaldaydata[i][0]
                                    + ((double) stationdaydata.data[i][0] / (double) stationdaydata.data[i][2]);
                            globaldaydata[i][1] = globaldaydata[i][1]
                                    + ((double) stationdaydata.data[i][1] / (double) stationdaydata.data[i][2]);
                        }
                    }
                }
            }
        }

        double getAverageStationDemand(Month month, Day day, int hour, boolean take) {
            return (getDemandGlobal(month, day, hour, take) / stationMap.size());
        }

        //if take==true returns the take demand otherwise returns the return demand
        double getDemandGlobal(Month month, Day day, int hour, boolean take) {
            HashMap<Day, double[][]> globalmonthdata = globalDemand.get(month);
            if (globalmonthdata == null) {
                throw new RuntimeException("no global demand available for this period");
            }

            double[][] globaldaydata = globalmonthdata.get(day);
            if (globaldaydata == null) {
                throw new RuntimeException("no global demand available for this period");
            }

            if (take) {
                return globaldaydata[hour][0];
            } else {
                return globaldaydata[hour][1];
            }
        }

        //if take==true returns the take demand otherwise returns the return demand
        DemandResult getDemandStation(int station, Month month, Day day, int hour, boolean take) {
            StationData sd = stationMap.get(station);
            if (sd == null) {
                return new DemandResult(false, Double.NaN);
            }
            return sd.getDemand(month, day, hour, take);
        }

        double getEntries(int station, Month month, Day day, int hour) {
            StationData sd = stationMap.get(station);
            if (sd == null) {
                throw new RuntimeException("entries do not exist");
            }
            return sd.getEntries(month, day, hour);
        }

    }

    //class for storing the demand for one sinle station or also for the sum of all stations
    class StationData {

        HashMap<Month, MonthData> monthMap;

        StationData() {
            monthMap = new HashMap<Month, MonthData>(15);
        }

        private void addData(Month month, Day day, int hour, int take, int ret) {
            MonthData data = monthMap.get(month);
            if (data == null) {
                data = new MonthData();
                monthMap.put(month, data);
            }
            data.add(day, hour, take, ret);
        }

        void add(Month month, Day day, int hour, int take, int ret) {
            //add the data for the month
            addData(month, day, hour, take, ret);

            //add winter or summer 
            if (month == Month.Nov || month == Month.Dic || month == Month.Jan || month == Month.Feb || month == Month.Mar) {
                addData(Month.Winter, day, hour, take, ret);
            } else {
                addData(Month.Summer, day, hour, take, ret);
            }
            //add to allmonth
            addData(Month.All, day, hour, take, ret);
        }

        //if take==true returns the take demand otherwise returns the return demand
        DemandResult getDemand(Month month, Day day, int hour, boolean take) {
            MonthData data = monthMap.get(month);
            if (data == null) {
                return new DemandResult(false, Double.NaN);
            }
            return data.getDemand(day, hour, take);
        }

        double getEntries(Month month, Day day, int hour) {
            MonthData data = monthMap.get(month);
            if (data == null) {
                throw new RuntimeException("no demand data available: month " + month);
            }
            return data.getEntries(day, hour);
        }
    }

    class MonthData {

        HashMap<Day, DayData> dayMap;

        MonthData() {
            dayMap = new HashMap<Day, DayData>(9);
        }

        private void addData(Day day, int hour, int take, int ret) {
            DayData data = dayMap.get(day);
            if (data == null) {
                data = new DayData();
                dayMap.put(day, data);
            }
            data.add(hour, take, ret);
        }

        void add(Day day, int hour, int take, int ret) {
            //add to the day
            addData(day, hour, take, ret);
            //add weekend or weekday
            if (day == Day.Sat || day == Day.Sun) {
                addData(Day.Weekend, hour, take, ret);
            } else {
                addData(Day.Weekday, hour, take, ret);
            }
        }

        //if take==true returns the take demand otherwise returns the return demand
        DemandResult getDemand(Day day, int hour, boolean take) {
            DayData data = dayMap.get(day);
            if (data == null) {
                return new DemandResult(false, Double.NaN);
            }
            return data.getDemand(hour, take);
        }

        double getEntries(Day day, int hour) {
            DayData data = dayMap.get(day);
            if (data == null) {
                throw new RuntimeException("no demand data available: day");
            }
            return data.getEntries(hour);
        }
    }

    class DayData {

        int[][] data;

        DayData() {
            data = new int[24][3];
            for (int i = 0; i < 24; i++) {
                data[i][0] = 0;
                data[i][1] = 0;
                data[i][2] = 0;
            }
        }

        void add(int hour, int take, int ret) {
            data[hour][0] += take;
            data[hour][1] += ret;
            data[hour][2]++;
        }
        //if take==true returns the take demand otherwise returns the return demand

        DemandResult getDemand(int hour, boolean take) {
            if (take) {
                return new DemandResult(true, data[hour][0] / (double) data[hour][2]);
            } else {
                return new DemandResult(true, data[hour][1] / (double) data[hour][2]);
            }
        }

        double getEntries(int hour) {
            return data[hour][2];
        }
    }

    public static void main(String[] args) throws Exception {

        String projectDir = "/Users/holger/workspace/BikeProjects/Bike3S/Bike3S-Simulator";
        String demandDataPath = projectDir + "/../demandDataMadrid0817_0918.csv";
        DemandManager demandManager = new DemandManager();
        demandManager.ReadData(demandDataPath);
        Month[] m = Month.values();
        Day[] d = Day.values();
        System.out.println("!!!!!Station demand:");

        Month mm = Month.Oct;
        Day dd = Day.Thu;
        for (int i = 0; i < 24; i++) {
            for (Integer si : demandManager.dem.stationMap.keySet()) {
                double take = demandManager.getTakeDemandStation(si, mm, dd, i);
                double ret = demandManager.getReturnDemandStation(si, mm, dd, i);
                System.out.println(
                        "Station : " + si + " : demand Month: " + mm + " : day: " + dd + " : hour: " + i
                        + " : take: " + take
                        + " : return: " + ret
                        + " : entries: " + demandManager.dem.getEntries(si, mm, dd, i));

            }
        }

        /*      int stationsum = 0;
        for (Month mm : m) {
            for (Day dd : d) {
                for (int i = 0; i < 24; i++) {
                    for (Integer si : demandManager.dem.stationMap.keySet()) {
                        DemandResult take = demandManager.getTakeDemandStation(si, mm, dd, i);
                        DemandResult ret = demandManager.getReturnDemandStation(si, mm, dd, i);
                        if (!take.hasdemand || !ret.hasdemand) {
                            System.out.println(
                                    "Station : " + si + " : demand Month: " + mm + " : day: " + dd + " : hour: " + i
                                    + " : take: not avail."
                                    + " : return: not avail."
                                    + " : entries: not avail.");

                        } else {
                            System.out.println(
                                    "Station : " + si + " : demand Month: " + mm + " : day: " + dd + " : hour: " + i
                                    + " : take: " + take.demand
                                    + " : return: " + ret.demand
                                    + " : entries: " + demandManager.dem.getEntries(si, mm, dd, i));
                            stationsum++;
                        }
                    }
                }
            }
        }
        System.out.println("!!!!!Station demand:");
        for (Month mm : m) {
            for (Day dd : d) {
                for (int i = 0; i < 24; i++) {
                    DemandResult take = demandManager.getTakeDemandGlobal(mm, dd, i);
                    DemandResult ret = demandManager.getReturnDemandGlobal(mm, dd, i);
                    if (!take.hasdemand || !ret.hasdemand) {
                        System.out.println(
                                "Total demand :  : demand Month: " + mm + " : day: " + dd + " : hour: " + i
                                + " : take: not avail."
                                + " : return: not avail.");

                    } else {
                        //            if (mm!=Month.Jan && mm!=Month.Feb && mm!=Month.Winter && mm!=Month.All) continue;
                        System.out.println(
                                "Total demand :  : demand Month: " + mm + " : day: " + dd + " : hour: " + i
                                + " : take: " + take.demand
                                + " : return: " + ret.demand);
                    }
                }
            }

        }
         */
    }

}
