package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

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
@RecommendationSystemType("DEMAND_PROBABILITY_TIME")
public class RecommendationSystemDemandProbabilityTime extends RecommendationSystemDemandProbabilityBased {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;

        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6;

        private double probfactor = 8000D;

        @Override
        public String toString() {
            return "maxDistanceRecommendation=" + maxDistanceRecommendation + ", upperProbabilityBound=" + upperProbabilityBound + ", desireableProbability=" + desireableProbability + ", probfactor=" + probfactor ;
        }
    }
    public String getParameterString(){
        return "RecommendationSystemDemandProbabilityTime Parameters{"+ super.getParameterString() + this.parameters.toString() + "}";
    }
    private RecommendationParameters parameters;

    public RecommendationSystemDemandProbabilityTime(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsRent(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            addrent(sd, orderedlist);
        }
        return orderedlist;
    }

    @Override
    protected List<StationUtilityData> specificOrderStationsReturn(List<StationUtilityData> stationdata, List<Station> allstations, GeoPoint currentuserposition, GeoPoint userdestination) {
        List<StationUtilityData> orderedlist = new ArrayList<>();
        for (StationUtilityData sd : stationdata) {
            addreturn(sd, orderedlist);
        }
        return orderedlist;
    }
    protected boolean betterOrSameRentDecideSimilar(StationUtilityData newSD, StationUtilityData oldSD){
               double timediff = (newSD.getWalkTime()- oldSD.getWalkTime());
                double probdiff = (newSD.getProbabilityTakeAfterTake()- oldSD.getProbabilityTakeAfterTake()) * this.parameters.probfactor;
                if (probdiff > timediff) {
                    return true;
                }
                return false;
    
    /*        if (newSD.getWalkdist()/newSD.getProbabilityTake()<oldSD.getWalkdist()/oldSD.getProbabilityTake()){
                return true;
            }
            return false;
    */}
   
    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation
                && oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            if (oldSD.getProbabilityTakeAfterTake() >= this.parameters.desireableProbability
                    && newSD.getProbabilityTakeAfterTake() >= this.parameters.desireableProbability) {
                return betterOrSameRentDecideSimilar(newSD, oldSD);
            }
            if (newSD.getProbabilityTakeAfterTake() >= this.parameters.desireableProbability) {
                return true;
            }
            if (oldSD.getProbabilityTakeAfterTake() >= this.parameters.desireableProbability) {
                return false;
            }
            if (oldSD.getProbabilityTakeAfterTake() >= newSD.getProbabilityTakeAfterTake()) {
                return false;
            }
            return true;
         }
        if (oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
        if (newSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return true;
        }
                return betterOrSameRentDecideSimilar(newSD, oldSD);
    }

    protected boolean betterOrSameReturnDecideSimilar(StationUtilityData newSD, StationUtilityData oldSD){
   
        double timediff = ((newSD.getBiketime() + newSD.getWalkTime())
                    - (oldSD.getBiketime() + oldSD.getWalkTime()));
        double probdiff = (newSD.getProbabilityReturnAfterReturn()- oldSD.getProbabilityReturnAfterReturn()) * this.parameters.probfactor;
            if (probdiff > timediff) {
                return true;
            }
            return false;
    /*
            if (newSD.getWalkdist()/newSD.getProbabilityReturn()<oldSD.getWalkdist()/oldSD.getProbabilityReturn()){
                return true;
            }
            return false;
    */}
    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameReturn(StationUtilityData newSD, StationUtilityData oldSD) {
        /*        if (oldSD.getProbabilityReturn() > this.parameters.upperProbabilityBound) {
            return false;
        }
        if (newSD.getProbabilityReturn() <= oldSD.getProbabilityReturn()) {
            return false;
        }
        // if here  newSD.getProbability() > oldSD.getProbability()
         */ if (oldSD.getProbabilityReturnAfterReturn() >= this.parameters.desireableProbability
                && newSD.getProbabilityReturnAfterReturn() >= this.parameters.desireableProbability) {
               return betterOrSameReturnDecideSimilar(newSD, oldSD);
         }
        if (newSD.getProbabilityReturnAfterReturn() >= this.parameters.desireableProbability) {
            return true;
        }
        if (oldSD.getProbabilityReturnAfterReturn() >= this.parameters.desireableProbability) {
            return false;
        }
               return betterOrSameReturnDecideSimilar(newSD, oldSD);
    }
}
