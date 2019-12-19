/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;
import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;
import java.text.Bidi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author holger
 */
@RecommendationSystemType("DEMAND_cost_simple")
public class RecommendationSystemDemandProbabilityCostSimple extends RecommendationSystemDemandProbabilityBased {

    public class RecommendationParameters {


        private double desireableProbability = 0.8;
        private double MaxCostValue = 6000 ;
        private double maxStationsToReccomend = 30;

        @Override
        public String toString() {
            return  "desireableProbability"+ desireableProbability+ ", MaxCostValue=" + MaxCostValue  + ", maxStationsToReccomend=" + maxStationsToReccomend  ;
        }
     }

    public String getParameterString() {
        return "RecommendationSystemDemandProbabilityCostSimple Parameters{" + super.getParameterString() + this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;
    private CostCalculatorSimple scc;

    public RecommendationSystemDemandProbabilityCostSimple(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        super(recomenderdef, ss);
        //***********Parameter treatment*****************************
        //if this recomender has parameters this is the right declaration
        //if no parameters are used this code just has to be commented
        //"getparameters" is defined in USER such that a value of Parameters 
        // is overwritten if there is a values specified in the jason description of the recomender
        // if no value is specified in jason, then the orriginal value of that field is mantained
        // that means that teh paramerts are all optional
        // if you want another behaviour, then you should overwrite getParameters in this calss
        this.parameters = new RecommendationParameters();
        getParameters(recomenderdef, this.parameters);
        scc=new CostCalculatorSimple(
                parameters.MaxCostValue, 
                straightLineWalkingVelocity, 
                straightLineCyclingVelocity, 
                probutils, 0, 0);
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i = 0;
        boolean goodfound = false;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityTake() > 0) {
                if (sd.getProbabilityTake() > this.parameters.desireableProbability && sd.getWalkdist() <= maxdistance) {
                    goodfound = true;
                }
                List<StationUtilityData> lookedlist = new ArrayList<>();
                double cost = scc.calculateCostRentSimple(sd, sd.getProbabilityTake(), sd.getWalkTime());
                sd.setTotalCost(cost);
                addrent(sd, orderedlist, maxdistance);
                if (goodfound) {
                    i++;
                }
            }
        }
  /*      //test probability
        List<StationUtilityData> orderedlist2 = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            addrentprob(sd, orderedlist2, maxdistance);
        }
        if ((orderedlist.isEmpty() != orderedlist2.isEmpty()) || (!orderedlist.isEmpty() &&
                orderedlist.get(0).getStation().getId() != orderedlist2.get(0).getStation().getId())) {
            System.out.println("!!!Different take:");

        }
        System.out.println("prob:");
        printAux(orderedlist2, true);
*/
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i = 0;
        boolean goodfound = false;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityReturn() > 0) {
                if (sd.getProbabilityReturn() > this.parameters.desireableProbability) {
                    goodfound = true;
                }
                List<StationUtilityData> lookedlist = new ArrayList<>();
                double cost = scc.calculateCostReturnSimple(sd, sd.getProbabilityReturn(), sd.getBiketime(), sd.getWalkTime());
                sd.setTotalCost(cost);
                addreturn(sd, orderedlist);
                if (goodfound) {
                    i++;
                }
            }
        }
 /*       //test probability
        List<StationUtilityData> orderedlist2 = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            addreturnprob(sd, orderedlist2);
        }
        if ((orderedlist.isEmpty() != orderedlist2.isEmpty()) || (!orderedlist.isEmpty() &&
                orderedlist.get(0).getStation().getId() != orderedlist2.get(0).getStation().getId())) {
            System.out.println("!!!Different return:");

        }
        System.out.println("prob:");
        printAux(orderedlist2, false);
*/
        return orderedlist;
    }

   
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
 /*       if (newSD.getProbabilityTake() >= this.parameters.desireableProbability
                && oldSD.getProbabilityTake() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityTake() < this.parameters.desireableProbability
                && oldSD.getProbabilityTake() >= this.parameters.desireableProbability) {
            return false;
        }
   */     return (newSD.getTotalCost() < oldSD.getTotalCost());
    }

    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
  /*      if (newSD.getProbabilityReturn() >= this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityReturn() < this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return false;
        }
  */     return newSD.getTotalCost() < oldSD.getTotalCost();
    }
    ///////////////////////
    //methods for comparing with probability
    ///////////////////////
    protected boolean betterOrSameRentprob(StationUtilityData newSD, StationUtilityData oldSD) {
 /*       if (newSD.getProbabilityTake() >= this.parameters.desireableProbability
                && oldSD.getProbabilityTake() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityTake() < this.parameters.desireableProbability
                && oldSD.getProbabilityTake() >= this.parameters.desireableProbability) {
            return false;
        }
   */     double timediff = (newSD.getWalkTime() - oldSD.getWalkTime());
        double probdiff = (newSD.getProbabilityTake() - oldSD.getProbabilityTake()) * this.parameters.MaxCostValue;
        return probdiff > timediff;
    }

    protected boolean betterOrSameReturnprob(StationUtilityData newSD, StationUtilityData oldSD) {
  /*      if (newSD.getProbabilityReturn() >= this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() < this.parameters.desireableProbability) {
            return true;
        }
        if (newSD.getProbabilityReturn() < this.parameters.desireableProbability
                && oldSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return false;
        }
  */      double timediff = ((newSD.getBiketime() + newSD.getWalkTime())
                - (oldSD.getBiketime() + oldSD.getWalkTime()));
        double probdiff = (newSD.getProbabilityReturn() - oldSD.getProbabilityReturn()) * this.parameters.MaxCostValue;
        return probdiff > timediff;
    }


    protected void addrentprob(StationUtilityData newSD, List<StationUtilityData> temp, double maxdistance) {
        int i = 0;
        for (; i < temp.size(); i++) {
            StationUtilityData oldSD=temp.get(i);
            if (newSD.getWalkdist() <= maxdistance && oldSD.getWalkdist() > maxdistance)  break;
            if (newSD.getWalkdist() > maxdistance && oldSD.getWalkdist() <= maxdistance)  continue;
            if (betterOrSameRentprob(newSD, oldSD)) {
                break;
            }
        }
        temp.add(i, newSD);
    }

    protected void addreturnprob(StationUtilityData d, List<StationUtilityData> temp) {
        int i = 0;
        for (; i < temp.size(); i++) {
            if (betterOrSameReturnprob(d, temp.get(i))) {
                break;
            }
        }
        temp.add(i, d);
    }

    void printAux(List<StationUtilityData> su, boolean take) {
        if (printHints) {
            int max = Math.min(3, su.size());
            //     if (su.get(0).getStation().getId()==8) max=173;
            //     else return;

            if (take) {
                System.out.println("Time (take):" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");

                if (su.get(0).getProbabilityTake() < 0.6) {
                    System.out.format("[Info] LOW PROB Take %9.8f %n", su.get(0).getProbabilityTake());
                }
                System.out.println("             id av ca   wtime    prob   cost");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    double cost = s.getWalkTime() + 6000 * (1 - s.getProbabilityTake());
                    System.out.format("%-3d Station %3d %2d %2d %7.1f %6.5f %9.2f ",
                            i + 1,
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.getWalkTime(),
                            s.getProbabilityTake(),
                            cost);
                    System.out.println("");
                }
            } else {
                System.out.println("Time (return):" + SimulationDateTime.getCurrentSimulationDateTime() + "(" + SimulationDateTime.getCurrentSimulationInstant() + ")");
                if (su.get(0).getBiketime()>10000)System.out.println("Biketime high");
                if (su.get(0).getProbabilityReturn() < 0.6) {
                    System.out.format("[Info] LOW PROB Return %9.8f %n", su.get(0).getProbabilityReturn());
                }
                System.out.println("             id av ca   wtime   btime    prob   cost");
                for (int i = 0; i < max; i++) {
                    StationUtilityData s = su.get(i);
                    double cost = s.getWalkTime() + s.getBiketime() + 6000 * (1 - s.getProbabilityReturn());
                    System.out.format("%-3d Station %3d %2d %2d %7.1f %7.1f %6.5f %9.2f ",
                            i + 1,
                            s.getStation().getId(),
                            s.getStation().availableBikes(),
                            s.getStation().getCapacity(),
                            s.getWalkTime(),
                            s.getBiketime(),
                            s.getProbabilityReturn(),
                            cost);
                    System.out.println("");
                }
            }
            System.out.println();
        }
    }
}
