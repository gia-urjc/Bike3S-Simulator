package es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.complex;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.services.RecommendationSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.StationManager;
import es.urjc.ia.bikesurbanfleets.worldentities.stations.entities.Station;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a system which recommends the user the stations to which he
 * should go to contribute with system rebalancing. Then, this recommendation
 * system gives the user a list of stations ordered descending by the
 * "resources/capacityº" ratio.
 *
 * @author IAgroup
 *
 */
@RecommendationSystemType("DEMAND_PROBABILITY_GLOBAL_PREDICTION")
public class RecommendationSystemDemandProbabilityGlobalPrediction extends RecommendationSystemDemandProbabilityBased {

    public static class RecommendationParameters extends RecommendationSystemDemandProbabilityBased.RecommendationParameters{
        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6;
        private double maxStationsToReccomend = 30;
        private double factorProb = 2000;
        private double factorImp = 500D;
    }

    private RecommendationParameters parameters;
    public RecommendationSystemDemandProbabilityGlobalPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
        //***********Parameter treatment*****************************
        //parameters are read in the superclass
        //afterwards, they have to be cast to this parameters class
        super(recomenderdef, ss, new RecommendationParameters());
        this.parameters= (RecommendationParameters)(super.parameters);
    }

    
     @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, double maxdistance) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i=0;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityTake()> 0) {
                double util=probutils.getGlobalProbabilityImprovementIfTake(sd);
                sd.setUtility(util);
                addrent(sd, orderedlist, maxdistance);
                i++;
            }
        }
        return orderedlist;
    }

        @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        int i=0;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityReturn()> 0) {
                double util=probutils.getGlobalProbabilityImprovementIfReturn(sd);
                sd.setUtility(util);
                addreturn(sd, orderedlist);
                i++;
            }
        }
        return orderedlist;
    }

    private boolean decideByGlobalUtilityrent(StationUtilityData newSD, StationUtilityData oldSD) {
            double timediff = (newSD.getWalkTime()- oldSD.getWalkTime());
            double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
            double probdiff = (newSD.getProbabilityTake() - oldSD.getProbabilityTake())* this.parameters.factorProb;
            if ((utildiff+probdiff) > (timediff)) {
                    return true;
                }
                return false;
    }

    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
            if (oldSD.getProbabilityTake() >= this.parameters.upperProbabilityBound && newSD.getProbabilityTake() >= this.parameters.upperProbabilityBound) {
                return decideByGlobalUtilityrent(newSD, oldSD);
            }
            if (oldSD.getProbabilityTake() >= this.parameters.upperProbabilityBound ) return false;
            if (newSD.getProbabilityTake() >= this.parameters.upperProbabilityBound ) return true;
            
            if (oldSD.getProbabilityTake() >= this.parameters.desireableProbability && newSD.getProbabilityTake() >= this.parameters.desireableProbability) {
                return decideByGlobalUtilityrent(newSD, oldSD);
            }
            if (oldSD.getProbabilityTake() >= this.parameters.desireableProbability ) return false;
            if (newSD.getProbabilityTake() >= this.parameters.desireableProbability ) return true;
            
            return newSD.getProbabilityTake() > oldSD.getProbabilityTake() ;
    }

    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
            double timediff = (newSD.getWalkTime()+newSD.getBiketime() - (oldSD.getWalkTime()+oldSD.getBiketime()));
            double utildiff = (newSD.getUtility() - oldSD.getUtility()) * this.parameters.factorImp;
            double probdiff = (newSD.getProbabilityReturn()- oldSD.getProbabilityReturn())* this.parameters.factorProb;
            if ((utildiff+probdiff) > (timediff)) {
                    return true;
                }
                return false;
     }
}
