/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.demandManager;

import java.time.LocalDateTime;

/**
 *
 * @author holger
 */
public abstract class DemandManager {
 
    public static enum Month {
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

    public static enum WeekDay {
        Mon, Tue, Wed, Thu, Fri, Sat, Sun, Weekday, Weekend;

        public static WeekDay toDemandMangerDay(java.time.DayOfWeek d) {
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

    public abstract double getStationTakeRateIntervall(int stationID, LocalDateTime start, double endtimeoffset) ;
    public abstract double getStationReturnRateIntervall(int stationID, LocalDateTime start, double endtimeoffset) ;
    public abstract double getGlobalTakeRateIntervall(LocalDateTime start, double endtimeoffset) ;
    public abstract double getGlobalReturnRateIntervall(LocalDateTime start, double endtimeoffset) ;

    public abstract double getStationTakeRatePerHour(int stationID, LocalDateTime t) ;
    public abstract double getStationReturnRatePerHour(int stationID, LocalDateTime t) ;

    public abstract double getGlobalTakeRatePerHour(LocalDateTime t) ;
    public abstract double getGlobalReturnRatePerHour(LocalDateTime t) ;
   
    public abstract double getStationTakeRatePerHour(int stationID, Month month, WeekDay day, int hour) ;
    public abstract double getStationReturnRatePerHour(int stationID, Month month, WeekDay day, int hour) ;

    public abstract double getGlobalTakeRatePerHour(Month month, WeekDay day, int hour) ;
    public abstract double getGlobalReturnRatePerHour(Month month, WeekDay day, int hour) ;

}
