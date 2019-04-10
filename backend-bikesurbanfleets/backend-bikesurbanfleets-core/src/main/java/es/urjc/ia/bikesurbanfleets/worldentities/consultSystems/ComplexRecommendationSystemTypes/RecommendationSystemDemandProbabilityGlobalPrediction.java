package es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.ComplexRecommendationSystemTypes;

import com.google.gson.JsonObject;
import es.urjc.ia.bikesurbanfleets.common.graphs.GeoPoint;
import static es.urjc.ia.bikesurbanfleets.common.util.ParameterReader.getParameters;
import es.urjc.ia.bikesurbanfleets.core.services.SimulationServices;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystem;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemParameters;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.RecommendationSystemType;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.Recommendation;
import es.urjc.ia.bikesurbanfleets.worldentities.consultSystems.StationUtilityData;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.InfrastructureManager;
import es.urjc.ia.bikesurbanfleets.worldentities.infraestructure.entities.Station;

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
@RecommendationSystemType("DEMAND_PROBABILITY_PREDICTION")
public class RecommendationSystemDemandProbabilityGlobalPrediction extends RecommendationSystemDemandProbabilityBased {

    @RecommendationSystemParameters
    public class RecommendationParameters {

        /**
         * It is the maximum distance in meters between the recommended stations
         * and the indicated geographical point.
         */
        private int maxDistanceRecommendation = 600;
        //this is meters per second corresponds aprox. to 4 and 20 km/h
        private double upperProbabilityBound = 0.999;
        private double desireableProbability = 0.6;
        private double maxStationsToReccomend = 30;

        private double factorProb = 2000;
        private double factorImp = 500D;
    }

    private RecommendationParameters parameters;
    public RecommendationSystemDemandProbabilityGlobalPrediction(JsonObject recomenderdef, SimulationServices ss) throws Exception {
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
        int i=0;
        for (StationUtilityData sd : stationdata) {
            if (i >= this.parameters.maxStationsToReccomend) {
                break;
            }
            if (sd.getProbabilityTake()> 0) {
                double util=recutils.getGlobalProbabilityImprovementIfTake(sd);
                sd.setUtility(util);
                addrent(sd, orderedlist);
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
                double util=recutils.getGlobalProbabilityImprovementIfReturn(sd);
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

    //take into account that distance newSD >= distance oldSD
    protected boolean betterOrSameRent(StationUtilityData newSD, StationUtilityData oldSD) {
        // if here newSD.getProbability() > oldSD.getProbability()
        if (newSD.getWalkdist()<= this.parameters.maxDistanceRecommendation) {
            if (oldSD.getProbabilityTake() > this.parameters.upperProbabilityBound && newSD.getProbabilityTake() > this.parameters.upperProbabilityBound) {
                return decideByGlobalUtilityrent(newSD, oldSD);
            }
            if (oldSD.getProbabilityTake() > this.parameters.upperProbabilityBound ) return false;
            if (newSD.getProbabilityTake() > this.parameters.upperProbabilityBound ) return true;
            
            if (oldSD.getProbabilityTake() > this.parameters.desireableProbability && newSD.getProbabilityTake() > this.parameters.desireableProbability) {
                return decideByGlobalUtilityrent(newSD, oldSD);
            }
            if (oldSD.getProbabilityTake() > this.parameters.desireableProbability ) return false;
            if (newSD.getProbabilityTake() > this.parameters.desireableProbability ) return true;
            if (newSD.getProbabilityTake() > oldSD.getProbabilityTake() ) return true;

            return false;
        }
        if (oldSD.getWalkdist() <= this.parameters.maxDistanceRecommendation) {
            return false;
        }
                return decideByGlobalUtilityrent(newSD, oldSD);
    }

    //take into account that distance newSD >= distance oldSD
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
