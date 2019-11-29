package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationDateTime;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("DEMAND_cost")
public class RecommendationSystemDemandProbabilityCost extends RecommendationSystemDemandProbabilityBased {

    public class RecommendationParameters {

        private double maxDistanceRecommendation=600;
        private double minimumMarginProbability = 0.001;
        private double minProbBestNeighbourRecommendation = 0.5;
        private double desireableProbability = 0.8;
        private double penalisationfactorrent = 1;
        private double penalisationfactorreturn = 1;
        private double maxStationsToReccomend = 30;
        private double unsucesscostRent = 5500; //with calculator2bis=between 4000 and 6000
        private double unsucesscostReturn = 5500; //with calculator2bis=between 4000 and 6000
        private double MaxCostValue = 5000; //with calculator2bis=0

        @Override
        public String toString() {
            return "maxDistanceRecommendation=" + maxDistanceRecommendation + ", MaxCostValue=" + MaxCostValue + ", minimumMarginProbability=" + minimumMarginProbability + ", minProbBestNeighbourRecommendation=" + minProbBestNeighbourRecommendation + ", desireableProbability=" + desireableProbability + ", penalisationfactorrent=" + penalisationfactorrent + ", penalisationfactorreturn=" + penalisationfactorreturn + ", maxStationsToReccomend=" + maxStationsToReccomend + ", unsucesscostRent=" + unsucesscostRent + ", unsucesscostReturn=" + unsucesscostReturn;
        }
    }

    public String getParameterString() {
        return "RecommendationSystemDemandProbabilityCost Parameters{" + super.getParameterString() + this.parameters.toString() + "}";
    }

    private RecommendationParameters parameters;
    private ComplexCostCalculatorNew ucc;

    public RecommendationSystemDemandProbabilityCost(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        ucc = new ComplexCostCalculatorNew(parameters.minimumMarginProbability, parameters.MaxCostValue, parameters.unsucesscostRent,
                parameters.unsucesscostReturn,
                parameters.penalisationfactorrent, parameters.penalisationfactorreturn, straightLineWalkingVelocity,
                straightLineCyclingVelocity, parameters.minProbBestNeighbourRecommendation,
                parameters.maxDistanceRecommendation, probutils, 0, 0);
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
                try {
                    double cost = ucc.calculateCostRentHeuristicNow(sd, stationdata, maxdistance);

                    sd.setTotalCost(cost);
                    addrent(sd, orderedlist, maxdistance);
                    if (goodfound) {
                        i++;
                    }
                } catch (BetterFirstStationException e) {
                    System.out.println("Better neighbour");

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
            System.out.println("!!!Different comming:");

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
                try {
                    double cost = ucc.calculateCostReturnHeuristicNow(sd, userdestination, stationdata);
                    sd.setTotalCost(cost);
                    addreturn(sd, orderedlist);
                    if (goodfound) {
                        i++;
                    }
                } catch (BetterFirstStationException e) {
                    System.out.println("Better neighbour");

                }
            }

        }
  /*      //test probability
        List<StationUtilityData> orderedlist2 = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            addreturnprob(sd, orderedlist2);
        }
        if ((orderedlist.isEmpty() != orderedlist2.isEmpty()) || (!orderedlist.isEmpty() &&
                orderedlist.get(0).getStation().getId() != orderedlist2.get(0).getStation().getId())) {
            System.out.println("!!!Different comming:");

        }
        System.out.println("prob:");
        printAux(orderedlist2, false);
*/
        return orderedlist;
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD,double maxdistance) {
        if (newSD.getWalkdist() <= maxdistance && oldSD.getWalkdist() > maxdistance) {
            return true;
        } else if (newSD.getWalkdist() > maxdistance && oldSD.getWalkdist() <= maxdistance) {
            return false;
        } else {
            return (newSD.getTotalCost() < oldSD.getTotalCost());
        }
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        return (newSD.getTotalCost() < oldSD.getTotalCost()); 
    }

    protected boolean betterOrSameRentDecideSimilarprob(StationUtilityData newSD, StationUtilityData oldSD) {
        double timediff = (newSD.getWalkTime() - oldSD.getWalkTime());
        double probdiff = (newSD.getProbabilityTake() - oldSD.getProbabilityTake()) * 6000;
        if (probdiff > timediff) {
            return true;
        }
        return false;
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameRentprob(StationUtilityData newSD, StationUtilityData oldSD, double maxdistance) {
        if (newSD.getWalkdist() <= maxdistance
                && oldSD.getWalkdist() <= maxdistance) {
            if (oldSD.getProbabilityTake() >= this.parameters.desireableProbability
                    && newSD.getProbabilityTake() >= this.parameters.desireableProbability) {
                return betterOrSameRentDecideSimilarprob(newSD, oldSD);
            }
            if (newSD.getProbabilityTake() >= this.parameters.desireableProbability) {
                return true;
            }
            if (oldSD.getProbabilityTake() >= this.parameters.desireableProbability) {
                return false;
            }
            if (oldSD.getProbabilityTake() >= newSD.getProbabilityTake()) {
                return false;
            }
            return true;
        }
        if (oldSD.getWalkdist() <= maxdistance) {
            return false;
        }
        if (newSD.getWalkdist() <= maxdistance) {
            return true;
        }
        return betterOrSameRentDecideSimilarprob(newSD, oldSD);
    }

    protected boolean betterOrSameReturnDecideSimilarprob(StationUtilityData newSD, StationUtilityData oldSD) {

        double timediff = ((newSD.getBiketime() + newSD.getWalkTime())
                - (oldSD.getBiketime() + oldSD.getWalkTime()));
        double probdiff = (newSD.getProbabilityReturn() - oldSD.getProbabilityReturn()) * 6000;
        if (probdiff > timediff) {
            return true;
        }
        return false;
    }

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameReturnprob(StationUtilityData newSD, StationUtilityData oldSD) {
         if (oldSD.getProbabilityReturn() >= this.parameters.desireableProbability
                && newSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return betterOrSameReturnDecideSimilarprob(newSD, oldSD);
        }
        if (newSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return true;
        }
        if (oldSD.getProbabilityReturn() >= this.parameters.desireableProbability) {
            return false;
        }
        return betterOrSameReturnDecideSimilarprob(newSD, oldSD);
    }

    protected void addrentprob(StationUtilityData d, List<StationUtilityData> temp, double maxdistance) {
        int i = 0;
        for (; i < temp.size(); i++) {
            if (betterOrSameRentprob(d, temp.get(i), maxdistance)) {
                break;
            }
        }
        temp.add(i, d);
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
